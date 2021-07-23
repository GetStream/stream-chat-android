package com.getstream.sdk.chat.viewmodel.messages

import androidx.annotation.UiThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.view.messages.MessageListItemWrapper
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import java.util.Date

/**
 * It's common for messaging UIs to interleave and group messages
 *
 * - subsequent messages from the same user are typically grouped
 * - read state is typically shown (either as part of the message or separately)
 * - date separators are common
 * - typing indicators are typically shown at the bottom
 *
 * The MessageListItemLiveData class merges the livedata objects for messages, read state and typing
 *
 * It improves upon the previous Java code in the sample app in a few ways
 * - Kotlin
 * - Typing indicators can be turned on/off
 * - Date separators can be turned off and are configurable
 * - Read state matches to the right message
 * - Leverages MediatorLiveData to improve handling of null values
 * - Efficient algorithm for updating read state
 * - Efficient code for updating typing state
 * - Makes the MessageListItem immutable to prevent future bugs
 * - Improved test coverage
 *
 * @param getCurrentUserId lambda which lazily provides the user id who is currently authenticated
 * @param messages a livedata object with the messages
 * @param readsLd a livedata object with the read state per user
 * @param typingLd a livedata object with the users who are currently typing
 * @param isThread if we are in a thread or not. if in a thread we add a threadSeperator in position 1 of the item list
 * @param dateSeparatorHandler function to compare previous and current message and return if we should insert a date separator
 *
 * Here's an example:
 *
 * MessageListItemLiveData(currentUser, messagesLd, readsLd, typingLd, false) { previous, message ->
 *   return if (previous==null) {
 *       true
 *   } else {
 *       (message.getCreatedAtOrThrow().time - previous.getCreatedAtOrThrow().time) > (60 * 60 * 3)
 *   }
 * }
 *
 */
internal class MessageListItemLiveData(
    private val currentUser: LiveData<User?>,
    messages: LiveData<List<Message>>,
    private val readsLd: LiveData<List<ChannelUserRead>>,
    private val typingLd: LiveData<List<User>>? = null,
    private val isThread: Boolean = false,
    private val dateSeparatorHandler: MessageListViewModel.DateSeparatorHandler? = null,
) : MediatorLiveData<MessageListItemWrapper>() {

    private var hasNewMessages: Boolean = false
    private var loadingMoreInProgress: Boolean = false
    private var messageItemsBase = listOf<MessageListItem>()
    private var messageItemsWithReads = listOf<MessageListItem>()
    private var typingUsers = listOf<User>()
    private var typingItems = listOf<MessageListItem>()

    private var lastMessageID = ""

    init {
        configMessagesChange(messages, currentUser)
        configReadsChange(readsLd, currentUser)
        typingLd?.let { typing -> configTypingChange(typing, currentUser) }
    }

    private fun configMessagesChange(messages: LiveData<List<Message>>, getCurrentUser: LiveData<User?>) {
        val messagesChange = getCurrentUser.changeOnUserLoaded(messages) { changedMessages, currentUser ->
            messagesChanged(changedMessages, currentUser!!.id)
        }

        addSource(messagesChange) { value ->
            this.value = value
        }
    }

    private fun configReadsChange(readsLd: LiveData<List<ChannelUserRead>>, getCurrentUser: LiveData<User?>) {
        val readChange = getCurrentUser.changeOnUserLoaded(readsLd) { changedReads, currentUser ->
            readsChanged(changedReads, currentUser!!.id)
        }

        addSource(readChange) { value ->
            this.value = value
        }
    }

    private fun configTypingChange(typingLd: LiveData<List<User>>, getCurrentUser: LiveData<User?>) {
        val typingChange: LiveData<MessageListItemWrapper?> =
            getCurrentUser.changeOnUserLoaded(typingLd, ::handleTypingUsersChange)

        addSource(typingChange) { value ->
            value?.let { this.value = it }
        }
    }

    internal fun handleTypingUsersChange(typingUsers: List<User>, currentUser: User?): MessageListItemWrapper? {
        val newTypingUsers = typingUsers.filter { typingUser ->
            typingUser.id != currentUser?.id
        }

        return if (newTypingUsers != this.typingUsers) {
            typingChanged(newTypingUsers)
        } else {
            null
        }
    }

    @UiThread
    internal fun messagesChanged(messages: List<Message>, currentUserId: String): MessageListItemWrapper {
        messageItemsBase = groupMessages(messages, currentUserId)
        messageItemsWithReads = addReads(messageItemsBase, readsLd.value, currentUserId)
        val out = getLoadingMoreItems() + messageItemsWithReads + typingItems
        return wrapMessages(out, hasNewMessages)
    }

    @UiThread
    internal fun readsChanged(reads: List<ChannelUserRead>, currentUserId: String): MessageListItemWrapper {
        messageItemsWithReads = addReads(messageItemsBase, reads, currentUserId)
        val out = getLoadingMoreItems() + messageItemsWithReads + typingItems
        return wrapMessages(out)
    }

    /**
     * Typing changes are the most common changes on the message list
     * Note how they don't recompute the message list, but only add to the end
     */
    @UiThread
    internal fun typingChanged(newTypingUsers: List<User>): MessageListItemWrapper {
        typingUsers = newTypingUsers
        typingItems = usersAsTypingItems(newTypingUsers)
        return wrapMessages(getLoadingMoreItems() + messageItemsWithReads + typingItems)
    }

    /**
     * Loading more indicator item should be added at the beginning of the items to indicate
     * a pending request for the next page of messages.
     */
    @UiThread
    internal fun loadingMoreChanged(loadingMoreInProgress: Boolean) {
        this.loadingMoreInProgress = loadingMoreInProgress
        messageItemsWithReads = messageItemsWithReads.filter {
            it !is MessageListItem.LoadingMoreIndicatorItem
        }
        val out = getLoadingMoreItems() + messageItemsWithReads
        value = wrapMessages(out)
    }

    private fun getLoadingMoreItems() = if (loadingMoreInProgress) {
        listOf<MessageListItem>(MessageListItem.LoadingMoreIndicatorItem)
    } else {
        emptyList()
    }

    /**
     * We could speed this up further in the case of a new message by only recomputing the last 2 items
     * It's fast enough though
     */
    private fun groupMessages(messages: List<Message>?, currentUserId: String): List<MessageListItem> {
        hasNewMessages = false
        if (messages == null || messages.isEmpty()) return emptyList()

        val newLastMessageId: String = messages[messages.size - 1].id
        if (newLastMessageId != lastMessageID) {
            hasNewMessages = true
        }
        lastMessageID = newLastMessageId

        val items = mutableListOf<MessageListItem>()
        var previousMessage: Message? = null
        val topIndex = 0.coerceAtLeast(messages.size - 1)

        for ((i, message) in messages.withIndex()) {
            var nextMessage: Message? = null
            if (i + 1 <= topIndex) {
                nextMessage = messages[i + 1]
            }

            // thread separator
            if (i == 1 && isThread) {
                items.add(
                    MessageListItem.ThreadSeparatorItem(
                        date = message.getCreatedAtOrThrow(),
                        messageCount = messages.size - 1,
                    )
                )
            }

            // date separator
            val shouldAddDateSeparator = dateSeparatorHandler?.shouldAddDateSeparator(previousMessage, message) ?: false
            if (shouldAddDateSeparator) {
                items.add(MessageListItem.DateSeparatorItem(message.getCreatedAtOrThrow()))
            }

            // determine the position (top, middle, bottom)
            val user = message.user
            val positions = mutableListOf<MessageListItem.Position>()
            if (previousMessage == null || previousMessage.user != user || shouldAddDateSeparator) {
                positions.add(MessageListItem.Position.TOP)
            }
            if (nextMessage == null || nextMessage.user != user) {
                positions.add(MessageListItem.Position.BOTTOM)
            }
            if (previousMessage != null && nextMessage != null) {
                if (previousMessage.user == user && nextMessage.user == user) {
                    positions.add(MessageListItem.Position.MIDDLE)
                }
            }

            items.add(
                MessageListItem.MessageItem(
                    message,
                    positions,
                    isMine = message.user.id == currentUserId,
                    isThreadMode = isThread,
                )
            )
            previousMessage = message
        }
        return items.toList()
    }

    /**
     * Reads changing is the second most common change on the message item list
     * Since the most common scenario is that someone read to the end, we start by matching the end of the list
     * We also sort the read state for easier merging of the lists
     */
    private fun addReads(
        messages: List<MessageListItem>,
        reads: List<ChannelUserRead>?,
        currentUserId: String,
    ): List<MessageListItem> {
        if (reads == null || messages.isEmpty()) return messages
        // filter your own read status and sort by last read
        val sortedReads = reads.filter { it.user.id != currentUserId }.sortedBy { it.lastRead }.toMutableList()
        if (sortedReads.isEmpty()) return messages

        val messagesCopy = messages.toMutableList()

        // start at the end, optimized for the most common scenario that most people are watching the chat
        for ((i, messageItem) in messages.reversed().withIndex()) {
            if (messageItem is MessageListItem.MessageItem) {
                messageItem.message.createdAt?.let {
                    while (sortedReads.isNotEmpty()) {
                        // use the list of sorted reads
                        val last = sortedReads.last()
                        if (it.before(last.lastRead) || it == last.lastRead) {
                            // we got a match
                            sortedReads.removeLast()
                            val reversedIndex = messages.size - i - 1
                            val messageItemCopy = messagesCopy[reversedIndex] as MessageListItem.MessageItem
                            val readBy = listOf(last) + messageItemCopy.messageReadBy
                            val updatedMessageItem = messageItem.copy(messageReadBy = readBy)
                            // update the message in the message copy
                            messagesCopy[reversedIndex] = updatedMessageItem
                        } else {
                            // search further in the past for matches
                            break
                        }
                    }
                }
            }
        }

        return addMessageReadFlags(messagesCopy, reads, currentUserId)
    }

    /**
     * Returns a list of [MessageListItem.MessageItem] with populated [MessageListItem.MessageItem.isMessageRead]
     * field. A message is considered "read" if at least one user (except the current user) has read further in
     * the channel than this message. Since the most common scenario is that someone read to the end,
     * [MessageListItem.MessageItem] has [MessageListItem.MessageItem.isMessageRead] field set to "true" by default,
     * which allows us to avoid excessive allocations.
     */
    private fun addMessageReadFlags(
        messages: List<MessageListItem>,
        reads: List<ChannelUserRead>,
        currentUserId: String,
    ): List<MessageListItem> {
        val lastRead = reads
            .filter { it.user.id != currentUserId }
            .mapNotNull { it.lastRead }
            .maxOrNull() ?: return messages

        return messages.map { messageListItem ->
            if (messageListItem is MessageListItem.MessageItem) {
                val isMessageRead = messageListItem.message
                    .createdAt
                    ?.let { it <= lastRead }
                    ?: false

                if (messageListItem.isMessageRead != isMessageRead) {
                    messageListItem.copy(isMessageRead = isMessageRead)
                } else {
                    messageListItem
                }
            } else {
                messageListItem
            }
        }
    }

    private fun wrapMessages(
        items: List<MessageListItem>,
        hasNewMessages: Boolean = false,
    ): MessageListItemWrapper {
        return MessageListItemWrapper(
            items = items,
            isThread = isThread,
            isTyping = typingUsers.isNotEmpty(),
            hasNewMessages = hasNewMessages,
        )
    }

    private fun usersAsTypingItems(users: List<User>): List<MessageListItem> {
        return if (users.isNotEmpty()) {
            listOf(MessageListItem.TypingItem(users))
        } else {
            emptyList()
        }
    }

    private fun <T, U> LiveData<User?>.changeOnUserLoaded(data: LiveData<T>, func: (T, User?) -> U): LiveData<U> {
        return Transformations.switchMap(this) { user ->
            Transformations.map(data) { type ->
                func(type, user)
            }
        }
    }
}

public fun Message.getCreatedAtOrThrow(): Date {
    val created = createdAt ?: createdLocallyAt
    return checkNotNull(created) { "a message needs to have a non null value for either createdAt or createdLocallyAt" }
}
