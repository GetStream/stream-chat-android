package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import io.getstream.chat.android.client.ChatClient
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
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.controller.helper.MessageHelper
import io.getstream.chat.android.livedata.request.QueryChannelPaginationRequest
import java.io.File
import io.getstream.chat.android.offline.channel.ChannelController as FlowChannelController

internal class ChannelControllerImpl(
    override val channelType: String,
    override val channelId: String,
    val client: ChatClient,
    val domainImpl: ChatDomainImpl,
    messageHelper: MessageHelper = MessageHelper(),
) : ChannelController {
    private val flowChannelController = FlowChannelController(channelType, channelId, client, domainImpl, messageHelper)

    override val repliedMessage: LiveData<Message?> = flowChannelController.repliedMessage.asLiveData()

    internal val unfilteredMessages = flowChannelController.unfilteredMessages

    override val messages: LiveData<List<Message>> = flowChannelController.messages.asLiveData()
    override val messagesState = flowChannelController.messagesState.asLiveData()
    override val oldMessages: LiveData<List<Message>> = flowChannelController.oldMessages.asLiveData()
    override val watcherCount: LiveData<Int> = flowChannelController.watcherCount.asLiveData()
    override val watchers: LiveData<List<User>> = flowChannelController.watchers.asLiveData()
    override val typing: LiveData<TypingEvent> = flowChannelController.typing.asLiveData()
    override val reads: LiveData<List<ChannelUserRead>> = flowChannelController.reads.asLiveData()
    override val read: LiveData<ChannelUserRead?> = flowChannelController.read.asLiveData()
    override val unreadCount: LiveData<Int?> = flowChannelController.unreadCount.asLiveData()
    override val members: LiveData<List<Member>> = flowChannelController.members.asLiveData()
    override val channelData: LiveData<ChannelData> = flowChannelController.channelData.asLiveData()
    override val hidden: LiveData<Boolean> = flowChannelController.hidden.asLiveData()
    override val muted: LiveData<Boolean> = flowChannelController.muted.asLiveData()
    override val loading: LiveData<Boolean> = flowChannelController.loading.asLiveData()
    override val loadingOlderMessages: LiveData<Boolean> = flowChannelController.loadingOlderMessages.asLiveData()
    override val loadingNewerMessages: LiveData<Boolean> = flowChannelController.loadingNewerMessages.asLiveData()
    override val endOfOlderMessages: LiveData<Boolean> = flowChannelController.endOfOlderMessages.asLiveData()
    override val endOfNewerMessages: LiveData<Boolean> = flowChannelController.endOfNewerMessages.asLiveData()

    override var recoveryNeeded = flowChannelController.recoveryNeeded
    override val cid = flowChannelController.cid

    fun getThread(threadId: String): ThreadControllerImpl = flowChannelController.getThread(threadId)
    fun keystroke(parentId: String?): Result<Boolean> = flowChannelController.keystroke(parentId)
    fun stopTyping(parentId: String?): Result<Boolean> = flowChannelController.stopTyping(parentId)
    internal fun markRead(): Boolean = flowChannelController.markRead()

    suspend fun hide(clearHistory: Boolean): Result<Unit> = flowChannelController.hide(clearHistory)
    suspend fun show(): Result<Unit> = flowChannelController.show()
    suspend fun leave(): Result<Unit> = flowChannelController.leave()
    suspend fun delete(): Result<Unit> = flowChannelController.delete()
    suspend fun watch(limit: Int = 30) = flowChannelController.watch(limit)
    suspend fun loadOlderMessages(limit: Int = 30): Result<Channel> = flowChannelController.loadOlderMessages(limit)
    suspend fun loadOlderMessages(messageId: String, limit: Int): Result<Channel> =
        flowChannelController.loadOlderMessages(messageId, limit)

    suspend fun loadNewerMessages(messageId: String, limit: Int): Result<Channel> =
        flowChannelController.loadNewerMessages(messageId, limit)

    suspend fun loadNewerMessages(limit: Int = 30): Result<Channel> = flowChannelController.loadNewerMessages(limit)

    suspend fun runChannelQuery(pagination: QueryChannelPaginationRequest): Result<Channel> =
        flowChannelController.runChannelQuery(pagination)

    suspend fun runChannelQueryOnline(pagination: QueryChannelPaginationRequest): Result<Channel> =
        flowChannelController.runChannelQueryOnline(pagination)

    suspend fun sendMessage(
        message: Message,
        attachmentTransformer: ((at: Attachment, file: File) -> Attachment)? = null,
    ): Result<Message> = flowChannelController.sendMessage(message, attachmentTransformer)

    suspend fun cancelMessage(message: Message): Result<Boolean> = flowChannelController.cancelMessage(message)

    internal suspend fun uploadAttachment(
        attachment: Attachment,
        attachmentTransformer: ((at: Attachment, file: File) -> Attachment)? = null,
        progressCallback: ProgressCallback? = null,
    ): Result<Attachment> = flowChannelController.uploadAttachment(attachment, attachmentTransformer, progressCallback)

    suspend fun sendGiphy(message: Message): Result<Message> = flowChannelController.sendGiphy(message)
    suspend fun shuffleGiphy(message: Message): Result<Message> = flowChannelController.shuffleGiphy(message)

    suspend fun sendImage(file: File): Result<String> = flowChannelController.sendImage(file)
    suspend fun sendFile(file: File): Result<String> = flowChannelController.sendFile(file)

    suspend fun sendReaction(reaction: Reaction, enforceUnique: Boolean): Result<Reaction> =
        flowChannelController.sendReaction(reaction, enforceUnique)

    suspend fun deleteReaction(reaction: Reaction): Result<Message> = flowChannelController.deleteReaction(reaction)

    internal fun upsertMessage(message: Message) = flowChannelController.upsertMessage(message)

    override fun getMessage(messageId: String): Message? = flowChannelController.getMessage(messageId)
    override fun clean() = flowChannelController.clean()

    fun setTyping(userId: String, event: ChatEvent?) = flowChannelController.setTyping(userId, event)
    fun isHidden(): Boolean = flowChannelController.isHidden()

    internal suspend fun handleEvents(events: List<ChatEvent>) = flowChannelController.handleEvents(events)
    internal fun handleEvent(event: ChatEvent) = flowChannelController.handleEvent(event)

    fun upsertMembers(members: List<Member>) = flowChannelController.upsertMembers(members)
    fun upsertMember(member: Member) = flowChannelController.upsertMember(member)

    fun updateLiveDataFromChannel(c: Channel) = flowChannelController.updateLiveDataFromChannel(c)

    suspend fun editMessage(message: Message): Result<Message> = flowChannelController.editMessage(message)
    suspend fun deleteMessage(message: Message): Result<Message> = flowChannelController.deleteMessage(message)

    override fun toChannel(): Channel = flowChannelController.toChannel()

    internal fun loadOlderThreadMessages(
        threadId: String,
        limit: Int,
        firstMessage: Message? = null,
    ): Result<List<Message>> = flowChannelController.loadOlderThreadMessages(threadId, limit, firstMessage)

    internal suspend fun loadMessageById(
        messageId: String,
        newerMessagesOffset: Int,
        olderMessagesOffset: Int,
    ): Result<Message> = flowChannelController.loadMessageById(messageId, newerMessagesOffset, olderMessagesOffset)

    internal fun replyMessage(repliedMessage: Message?) = flowChannelController.replyMessage(repliedMessage)
}
