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
import io.getstream.chat.android.offline.channel.MessageHelper
import io.getstream.chat.android.offline.request.QueryChannelPaginationRequest
import kotlinx.coroutines.flow.map
import java.io.File
import io.getstream.chat.android.offline.channel.ChannelController as ChannelControllerStateFlow

internal class ChannelControllerImpl(private val channelControllerStateFlow: ChannelControllerStateFlow) :
    ChannelController {

    internal constructor(
        channelType: String,
        channelId: String,
        client: ChatClient,
        domainImpl: ChatDomainImpl,
        messageHelper: MessageHelper = MessageHelper(),
    ) : this(
        ChannelControllerStateFlow(
            channelType,
            channelId,
            client,
            domainImpl.chatDomainStateFlow,
            messageHelper
        )
    )

    override val channelType: String = channelControllerStateFlow.channelType
    override val channelId: String = channelControllerStateFlow.channelId

    override val repliedMessage: LiveData<Message?> = channelControllerStateFlow.repliedMessage.asLiveData()
    override val messages: LiveData<List<Message>> = channelControllerStateFlow.messages.asLiveData()
    override val messagesState = channelControllerStateFlow.messagesState.asLiveData()
    override val oldMessages: LiveData<List<Message>> = channelControllerStateFlow.oldMessages.asLiveData()
    override val watcherCount: LiveData<Int> = channelControllerStateFlow.watcherCount.asLiveData()
    override val watchers: LiveData<List<User>> = channelControllerStateFlow.watchers.asLiveData()
    override val typing: LiveData<TypingEvent> = channelControllerStateFlow.typing.asLiveData()
    override val reads: LiveData<List<ChannelUserRead>> = channelControllerStateFlow.reads.asLiveData()
    override val read: LiveData<ChannelUserRead?> = channelControllerStateFlow.read.asLiveData()
    override val unreadCount: LiveData<Int?> = channelControllerStateFlow.unreadCount.asLiveData()
    override val members: LiveData<List<Member>> = channelControllerStateFlow.members.asLiveData()
    override val channelData: LiveData<ChannelData> = channelControllerStateFlow.channelData.map(::ChannelData).asLiveData()
    override val hidden: LiveData<Boolean> = channelControllerStateFlow.hidden.asLiveData()
    override val muted: LiveData<Boolean> = channelControllerStateFlow.muted.asLiveData()
    override val loading: LiveData<Boolean> = channelControllerStateFlow.loading.asLiveData()
    override val loadingOlderMessages: LiveData<Boolean> = channelControllerStateFlow.loadingOlderMessages.asLiveData()
    override val loadingNewerMessages: LiveData<Boolean> = channelControllerStateFlow.loadingNewerMessages.asLiveData()
    override val endOfOlderMessages: LiveData<Boolean> = channelControllerStateFlow.endOfOlderMessages.asLiveData()
    override val endOfNewerMessages: LiveData<Boolean> = channelControllerStateFlow.endOfNewerMessages.asLiveData()

    override var recoveryNeeded = channelControllerStateFlow.recoveryNeeded
    override val cid = channelControllerStateFlow.cid

    fun getThread(threadId: String): ThreadControllerImpl =
        ThreadControllerImpl(channelControllerStateFlow.getThread(threadId))

    fun keystroke(parentId: String?): Result<Boolean> = channelControllerStateFlow.keystroke(parentId)
    fun stopTyping(parentId: String?): Result<Boolean> = channelControllerStateFlow.stopTyping(parentId)
    internal fun markRead(): Boolean = channelControllerStateFlow.markRead()

    suspend fun hide(clearHistory: Boolean): Result<Unit> = channelControllerStateFlow.hide(clearHistory)
    suspend fun show(): Result<Unit> = channelControllerStateFlow.show()
    suspend fun leave(): Result<Unit> = channelControllerStateFlow.leave()
    suspend fun delete(): Result<Unit> = channelControllerStateFlow.delete()
    suspend fun watch(limit: Int = 30) = channelControllerStateFlow.watch(limit)
    suspend fun loadOlderMessages(limit: Int = 30): Result<Channel> =
        channelControllerStateFlow.loadOlderMessages(limit)

    suspend fun loadOlderMessages(messageId: String, limit: Int): Result<Channel> =
        channelControllerStateFlow.loadOlderMessages(messageId, limit)

    suspend fun loadNewerMessages(messageId: String, limit: Int): Result<Channel> =
        channelControllerStateFlow.loadNewerMessages(messageId, limit)

    suspend fun loadNewerMessages(limit: Int = 30): Result<Channel> =
        channelControllerStateFlow.loadNewerMessages(limit)

    suspend fun runChannelQuery(pagination: QueryChannelPaginationRequest): Result<Channel> =
        channelControllerStateFlow.runChannelQuery(pagination)

    suspend fun runChannelQueryOnline(pagination: QueryChannelPaginationRequest): Result<Channel> =
        channelControllerStateFlow.runChannelQueryOnline(pagination)

    suspend fun sendMessage(
        message: Message,
        attachmentTransformer: ((at: Attachment, file: File) -> Attachment)? = null,
    ): Result<Message> = channelControllerStateFlow.sendMessage(message, attachmentTransformer)

    suspend fun cancelMessage(message: Message): Result<Boolean> = channelControllerStateFlow.cancelMessage(message)

    internal suspend fun uploadAttachment(
        attachment: Attachment,
        attachmentTransformer: ((at: Attachment, file: File) -> Attachment)? = null,
        progressCallback: ProgressCallback? = null,
    ): Result<Attachment> =
        channelControllerStateFlow.uploadAttachment(attachment, attachmentTransformer, progressCallback)

    suspend fun sendGiphy(message: Message): Result<Message> = channelControllerStateFlow.sendGiphy(message)
    suspend fun shuffleGiphy(message: Message): Result<Message> = channelControllerStateFlow.shuffleGiphy(message)

    suspend fun sendImage(file: File): Result<String> = channelControllerStateFlow.sendImage(file)
    suspend fun sendFile(file: File): Result<String> = channelControllerStateFlow.sendFile(file)

    suspend fun sendReaction(reaction: Reaction, enforceUnique: Boolean): Result<Reaction> =
        channelControllerStateFlow.sendReaction(reaction, enforceUnique)

    suspend fun deleteReaction(reaction: Reaction): Result<Message> =
        channelControllerStateFlow.deleteReaction(reaction)

    internal fun upsertMessage(message: Message) = channelControllerStateFlow.upsertMessage(message)

    override fun getMessage(messageId: String): Message? = channelControllerStateFlow.getMessage(messageId)
    override fun clean() = channelControllerStateFlow.clean()

    fun setTyping(userId: String, event: ChatEvent?) = channelControllerStateFlow.setTyping(userId, event)
    fun isHidden(): Boolean = channelControllerStateFlow.isHidden()

    internal suspend fun handleEvents(events: List<ChatEvent>) = channelControllerStateFlow.handleEvents(events)
    internal fun handleEvent(event: ChatEvent) = channelControllerStateFlow.handleEvent(event)

    fun upsertMembers(members: List<Member>) = channelControllerStateFlow.upsertMembers(members)
    fun upsertMember(member: Member) = channelControllerStateFlow.upsertMember(member)
    suspend fun removeMembers(vararg userIds: String): Result<Channel> =
        channelControllerStateFlow.removeMembers(*userIds)

    fun updateLiveDataFromChannel(c: Channel) = channelControllerStateFlow.updateDataFromChannel(c)

    suspend fun editMessage(message: Message): Result<Message> = channelControllerStateFlow.editMessage(message)
    suspend fun deleteMessage(message: Message): Result<Message> = channelControllerStateFlow.deleteMessage(message)

    override fun toChannel(): Channel = channelControllerStateFlow.toChannel()

    internal fun loadOlderThreadMessages(
        threadId: String,
        limit: Int,
        firstMessage: Message? = null,
    ): Result<List<Message>> = channelControllerStateFlow.loadOlderThreadMessages(threadId, limit, firstMessage)

    internal suspend fun loadMessageById(
        messageId: String,
        newerMessagesOffset: Int,
        olderMessagesOffset: Int,
    ): Result<Message> = channelControllerStateFlow.loadMessageById(messageId, newerMessagesOffset, olderMessagesOffset)

    internal fun replyMessage(repliedMessage: Message?) = channelControllerStateFlow.replyMessage(repliedMessage)
}
