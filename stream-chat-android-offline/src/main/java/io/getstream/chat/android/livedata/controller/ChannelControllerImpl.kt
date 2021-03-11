package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChannelData
import io.getstream.chat.android.livedata.request.QueryChannelPaginationRequest
import java.io.File
import java.util.Date
import io.getstream.chat.android.offline.ChannelControllerImpl as NewChannelControllerImpl

internal class ChannelControllerImpl(
    private val delegate: NewChannelControllerImpl,
) : ChannelController {

    override val channelType: String
        get() = delegate.channelType

    override val channelId: String
        get() = delegate.channelId

    override val repliedMessage: LiveData<Message?> = delegate.repliedMessage.asLiveData()

    internal val hideMessagesBefore: Date?
        get() = delegate.hideMessagesBefore

    val unfilteredMessages = delegate.unfilteredMessages.asLiveData()

    override val messages: LiveData<List<Message>> = delegate.messages.asLiveData()

    override val messagesState: LiveData<ChannelController.MessagesState> = delegate.messagesState.asLiveData()

    override val oldMessages: LiveData<List<Message>> = delegate.oldMessages.asLiveData()

    /** the number of people currently watching the channel */
    override val watcherCount: LiveData<Int> = delegate.watcherCount.asLiveData()

    /** the list of users currently watching this channel */
    override val watchers: LiveData<List<User>> = delegate.watchers.asLiveData()

    /** who is currently typing (current user is excluded from this) */
    override val typing: LiveData<TypingEvent> = delegate.typing.asLiveData()

    /** how far every user in this channel has read */
    override val reads: LiveData<List<ChannelUserRead>> = delegate.reads.asLiveData()

    /** read status for the current user */
    override val read: LiveData<ChannelUserRead?> = delegate.read.asLiveData()

    /**
     * unread count for this channel, calculated based on read state (this works even if you're offline)
     */
    override val unreadCount: LiveData<Int?> = delegate.unreadCount.asLiveData()

    /** the list of members of this channel */
    override val members: LiveData<List<Member>> = delegate.members.asLiveData()

    /** LiveData object with the channel data */
    override val channelData: LiveData<ChannelData> = delegate.channelData.asLiveData()

    /** if the channel is currently hidden */
    override val hidden: LiveData<Boolean> = delegate.hidden.asLiveData()

    /** if the channel is currently muted */
    override val muted: LiveData<Boolean> = delegate.muted.asLiveData()

    /** if we are currently loading */
    override val loading: LiveData<Boolean> = delegate.loading.asLiveData()

    /** if we are currently loading older messages */
    override val loadingOlderMessages: LiveData<Boolean> = delegate.loadingOlderMessages.asLiveData()

    /** if we are currently loading newer messages */
    override val loadingNewerMessages: LiveData<Boolean> = delegate.loadingNewerMessages.asLiveData()

    /** set to true if there are no more older messages to load */
    override val endOfOlderMessages: LiveData<Boolean> = delegate.endOfOlderMessages.asLiveData()

    /** set to true if there are no more newer messages to load */
    override val endOfNewerMessages: LiveData<Boolean> = delegate.endOfNewerMessages.asLiveData()

    override val recoveryNeeded: Boolean
        get() = delegate.recoveryNeeded

    override val cid: String
        get() = delegate.cid

    fun getThread(threadId: String): ThreadControllerImpl = ThreadControllerImpl(delegate.getThread(threadId))

    fun keystroke(parentId: String?): Result<Boolean> = delegate.keystroke(parentId)

    fun stopTyping(parentId: String?): Result<Boolean> = delegate.stopTyping(parentId)

    /**
     * Marks the channel as read by the current user
     *
     * @return whether the channel was marked as read or not
     */
    internal fun markRead(): Boolean = delegate.markRead()

    suspend fun hide(clearHistory: Boolean): Result<Unit> = delegate.hide(clearHistory)

    suspend fun show(): Result<Unit> = delegate.show()

    suspend fun leave(): Result<Unit> = delegate.leave()

    suspend fun delete(): Result<Unit> = delegate.delete()

    suspend fun watch(limit: Int = 30) = delegate.watch(limit)

    /**
     *  Loads a list of messages before the oldest message in the current list.
     */
    suspend fun loadOlderMessages(limit: Int = 30): Result<Channel> = delegate.loadOlderMessages(limit)

    /**
     *  Loads a list of messages after the newest message in the current list.
     */
    suspend fun loadNewerMessages(limit: Int = 30): Result<Channel> = delegate.loadNewerMessages(limit)

    /**
     *  Loads a list of messages before the message with particular message id.
     */
    suspend fun loadOlderMessages(messageId: String, limit: Int): Result<Channel> =
        delegate.loadOlderMessages(messageId, limit)

    /**
     *  Loads a list of messages after the message with particular message id.
     */
    suspend fun loadNewerMessages(messageId: String, limit: Int): Result<Channel> =
        delegate.loadNewerMessages(messageId, limit)

    suspend fun runChannelQuery(pagination: QueryChannelPaginationRequest): Result<Channel> =
        delegate.runChannelQuery(pagination)

    suspend fun runChannelQueryOnline(pagination: QueryChannelPaginationRequest): Result<Channel> =
        delegate.runChannelQueryOnline(pagination)

    /**
     * - Generate an ID
     * - Insert the message into offline storage with sync status set to Sync Needed
     * - If we're online do the send message request
     * - If the request fails we retry according to the retry policy set on the repo
     */
    suspend fun sendMessage(
        message: Message,
        attachmentTransformer: ((at: Attachment, file: File) -> Attachment)? = null,
    ): Result<Message> = delegate.sendMessage(message, attachmentTransformer)

    /**
     * Upload the attachment.upload file for the given attachment
     * Structure of the resulting attachment object can be adjusted using the attachmentTransformer
     */
    internal suspend fun uploadAttachment(
        attachment: Attachment,
        attachmentTransformer: ((at: Attachment, file: File) -> Attachment)? = null,
        progressCallback: ProgressCallback? = null,
    ): Result<Attachment> = delegate.uploadAttachment(attachment, attachmentTransformer, progressCallback)

    /**
     * Cancels ephemeral Message.
     * Removes message from the offline storage and memory and notifies about update.
     */
    suspend fun cancelMessage(message: Message): Result<Boolean> = delegate.cancelMessage(message)

    suspend fun sendGiphy(message: Message): Result<Message> = delegate.sendGiphy(message)

    suspend fun shuffleGiphy(message: Message): Result<Message> = delegate.shuffleGiphy(message)

    suspend fun sendImage(file: File): Result<String> = delegate.sendImage(file)

    suspend fun sendFile(file: File): Result<String> = delegate.sendFile(file)

    /**
     * sendReaction posts the reaction on local storage
     * message reaction count should increase, latest reactions and own_reactions should be updated
     *
     * If you're online we make the API call to sync to the server
     * If the request fails we retry according to the retry policy set on the repo
     */
    suspend fun sendReaction(reaction: Reaction, enforceUnique: Boolean): Result<Reaction> =
        delegate.sendReaction(reaction, enforceUnique)

    suspend fun deleteReaction(reaction: Reaction): Result<Message> = delegate.deleteReaction(reaction)

    // This one needs to be public for flows such as running a message action

    internal fun upsertMessage(message: Message) = delegate.upsertMessage(message)

    override fun getMessage(messageId: String): Message? = delegate.getMessage(messageId)

    override fun clean() = delegate.clean()

    fun setTyping(userId: String, event: ChatEvent?) = delegate.setTyping(userId, event)

    internal suspend fun handleEvents(events: List<ChatEvent>) = delegate.handleEvents(events)

    fun isHidden(): Boolean = delegate.isHidden()

    internal suspend fun handleEvent(event: ChatEvent) = delegate.handleEvent(event)

    fun upsertMembers(members: List<Member>) = delegate.upsertMembers(members)

    fun upsertMember(member: Member) = delegate.upsertMember(member)

    fun updateLiveDataFromChannel(c: Channel) = delegate.updateLiveDataFromChannel(c)

    suspend fun editMessage(message: Message): Result<Message> = delegate.editMessage(message)

    suspend fun deleteMessage(message: Message): Result<Message> = delegate.deleteMessage(message)

    override fun toChannel(): Channel = delegate.toChannel()

    internal fun loadOlderThreadMessages(
        threadId: String,
        limit: Int,
        firstMessage: Message? = null,
    ): Result<List<Message>> = delegate.loadOlderThreadMessages(threadId, limit, firstMessage)

    internal suspend fun loadMessageById(
        messageId: String,
        newerMessagesOffset: Int,
        olderMessagesOffset: Int,
    ): Result<Message> = delegate.loadMessageById(messageId, newerMessagesOffset, olderMessagesOffset)

    internal fun replyMessage(repliedMessage: Message?) = delegate.replyMessage(repliedMessage)
}
