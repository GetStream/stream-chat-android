/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.getstream.sdk.chat.viewmodel.messages

import androidx.annotation.UiThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.combineWith
import com.getstream.sdk.chat.utils.extensions.getCreatedAtOrThrow
import com.getstream.sdk.chat.utils.extensions.shouldShowMessageFooter
import com.getstream.sdk.chat.view.messages.MessageListItemWrapper
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.common.state.DeletedMessageVisibility
import io.getstream.chat.android.common.state.MessageFooterVisibility
import io.getstream.chat.android.ui.ChatUI

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
 * @param currentUser Lambda which lazily provides the user id who is currently authenticated.
 * @param messages A livedata object with the messages.
 * @param readsLd A livedata object with the read state per user.
 * @param typingLd A livedata object with the users who are currently typing.
 * @param isThread If we are in a thread or not. If in a thread, we add a thread seperator in position 1 of the
 * item list.
 * @param dateSeparatorHandler Function to compare previous and current message and return if we should insert a
 * date separator.
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
 * @param deletedMessageVisibility Controls when deleted messages are shown.
 * @param messageFooterVisibility Controls when the message footer is shown.
 * @param messagePositionHandlerProvider Provider for a handler to determine the position of a message within a group.
 */

@Suppress("LongParameterList", "TooManyFunctions")
internal class MessageListItemLiveData(
    private val currentUser: LiveData<User?>,
    messages: LiveData<List<Message>>,
    private val readsLd: LiveData<List<ChannelUserRead>>,
    private val typingLd: LiveData<List<User>>? = null,
    private val isThread: Boolean = false,
    private val dateSeparatorHandler: MessageListViewModel.DateSeparatorHandler? = null,
    private val deletedMessageVisibility: LiveData<DeletedMessageVisibility>,
    private val messageFooterVisibility: LiveData<MessageFooterVisibility>,
    private val messagePositionHandlerProvider: () -> MessageListViewModel.MessagePositionHandler,
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

    /**
     * Emits a value from this [MediatorLiveData] class when either
     * the user gets updated, the deleted message visibility or
     * message footer visibility gets changed.
     */
    private fun configMessagesChange(messages: LiveData<List<Message>>, getCurrentUser: LiveData<User?>) {
        val messagesChange = getCurrentUser
            .combineWith(deletedMessageVisibility) { user, _ ->
                user
            }
            .combineWith(messageFooterVisibility) { user, _ ->
                user
            }
            .changeOnUserLoaded(messages) { changedMessages, currentUser ->
                if (currentUser != null) {
                    messagesChanged(
                        changedMessages,
                        currentUser.id
                    )
                } else null
            }

        addSource(messagesChange) { value ->
            if (value != null) {
                this.value = value
            }
        }
    }

    private fun configReadsChange(readsLd: LiveData<List<ChannelUserRead>>, getCurrentUser: LiveData<User?>) {
        val readChange = getCurrentUser.changeOnUserLoaded(readsLd) { changedReads, currentUser ->
            if (currentUser != null) {
                readsChanged(changedReads, currentUser.id)
            } else {
                null
            }
        }

        addSource(readChange) { value ->
            if (value != null) {
                this.value = value
            }
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
        return wrapMessages(buildItemsList(), hasNewMessages)
    }

    @UiThread
    internal fun readsChanged(reads: List<ChannelUserRead>, currentUserId: String): MessageListItemWrapper {
        messageItemsWithReads = addReads(messageItemsBase, reads, currentUserId)
        return wrapMessages(buildItemsList())
    }

    /**
     * Typing changes are the most common changes on the message list.
     * Note how they don't recompute the message list, but only add to the end.
     */
    @UiThread
    internal fun typingChanged(newTypingUsers: List<User>): MessageListItemWrapper {
        typingUsers = newTypingUsers
        typingItems = usersAsTypingItems(newTypingUsers)
        return wrapMessages(buildItemsList())
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

        value = wrapMessages(buildItemsList())
    }

    /**
     * Builds a list of items we show in the View, based on the current state.
     *
     * We add the loading item at the top, if we're currently loading more data and the typing item at the bottom, if
     * there are users who are typing.
     *
     * @return Full list of [MessageListItem] to represent the state.
     */
    private fun buildItemsList(): List<MessageListItem> {
        return getLoadingMoreItems() + messageItemsWithReads + typingItems
    }

    private fun getLoadingMoreItems() = if (loadingMoreInProgress) {
        listOf<MessageListItem>(MessageListItem.LoadingMoreIndicatorItem)
    } else {
        emptyList()
    }

    /**
     * Filters out or leaves in deleted messages based on their visibility
     * set by the user.
     */
    private fun filterDeletedMessages(messages: List<Message>?): List<Message>? {
        return when (deletedMessageVisibility.value) {
            DeletedMessageVisibility.VISIBLE_FOR_CURRENT_USER ->
                messages?.filter { it.deletedAt == null || it.user.id == currentUser.value?.id }
            DeletedMessageVisibility.ALWAYS_HIDDEN -> messages?.filter { it.deletedAt == null }
            else -> messages
        }
    }

    /**
     * We could speed this up further in the case of a new message by only recomputing the last 2 items
     * It's fast enough though.
     */
    @Suppress("ComplexMethod")
    private fun groupMessages(messages: List<Message>?, currentUserId: String): List<MessageListItem> {

        val filteredMessages = filterDeletedMessages(messages)

        hasNewMessages = false
        if (filteredMessages == null || filteredMessages.isEmpty()) return emptyList()

        val newLastMessageId: String = filteredMessages[filteredMessages.size - 1].id
        if (newLastMessageId != lastMessageID) {
            hasNewMessages = true
        }
        lastMessageID = newLastMessageId

        val items = mutableListOf<MessageListItem>()
        var previousMessage: Message? = null
        val topIndex = 0.coerceAtLeast(filteredMessages.size - 1)

        for ((i, message) in filteredMessages.withIndex()) {
            var nextMessage: Message? = null
            if (i + 1 <= topIndex) {
                nextMessage = filteredMessages[i + 1]
            }

            // thread separator
            if (i == 1 && isThread) {
                items.add(
                    MessageListItem.ThreadSeparatorItem(
                        date = message.getCreatedAtOrThrow(),
                        messageCount = filteredMessages.size - 1,
                    )
                )
            }

            // date separator
            val shouldAddDateSeparator = dateSeparatorHandler?.shouldAddDateSeparator(previousMessage, message) ?: false
            if (shouldAddDateSeparator) {
                items.add(MessageListItem.DateSeparatorItem(message.getCreatedAtOrThrow()))
            }

            // determine the position (top, middle, bottom)
            val positions = messagePositionHandlerProvider().handleMessagePosition(
                prevMessage = previousMessage,
                message = message,
                nextMessage = nextMessage,
                isAfterDateSeparator = shouldAddDateSeparator,
            )

            // determine if footer is shown or not
            val shouldShowMessageFooter = messageFooterVisibility.value?.shouldShowMessageFooter(
                message,
                positions.contains(MessageListItem.Position.BOTTOM),
                nextMessage
            ) ?: false

            items.add(
                MessageListItem.MessageItem(
                    message,
                    positions,
                    isMine = message.user.id == currentUserId,
                    isThreadMode = isThread,
                    showMessageFooter = shouldShowMessageFooter
                )
            )
            previousMessage = message
        }

        // thread placeholder and a date separator (if enabled) when a message has zero replies
        if (isThread && items.size == 1) {
            if (ChatUI.showDateSeparatorInEmptyThread) {
                val message = messages?.firstOrNull()

                if (message != null) {
                    items.add(MessageListItem.DateSeparatorItem(message.getCreatedAtOrThrow()))
                }
            }
            items.add(MessageListItem.ThreadPlaceholderItem)
        }

        return items.toList()
    }

    /**
     * Reads changing is the second most common change on the message item list.
     * Since the most common scenario is that someone read to the end, we start by matching the end of the list.
     * We also sort the read state for easier merging of the lists.
     */
    @Suppress("ReturnCount", "NestedBlockDepth")
    private fun addReads(
        messages: List<MessageListItem>,
        reads: List<ChannelUserRead>?,
        currentUserId: String,
    ): List<MessageListItem> {
        if (reads == null || messages.isEmpty()) return messages
        // filter your own read status and sort by last read
        val sortedReads = reads
            .filter { it.user.id != currentUserId }
            .sortedBy { it.lastRead }
            .toMutableList()
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
