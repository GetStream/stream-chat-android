package io.getstream.chat.android.offline.channel

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.WatchChannelRequest
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.extensions.isPermanent
import io.getstream.chat.android.client.extensions.retry
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.client.utils.recover
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.experimental.channel.logic.ChannelLogic
import io.getstream.chat.android.offline.experimental.channel.state.ChannelMutableState
import io.getstream.chat.android.offline.experimental.channel.thread.logic.ThreadLogic
import io.getstream.chat.android.offline.experimental.channel.thread.state.ThreadMutableState
import io.getstream.chat.android.offline.message.attachment.AttachmentUploader
import io.getstream.chat.android.offline.message.isEphemeral
import io.getstream.chat.android.offline.request.QueryChannelPaginationRequest
import io.getstream.chat.android.offline.thread.ThreadController
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import java.util.Date
import java.util.concurrent.ConcurrentHashMap

@OptIn(ExperimentalStreamChatApi::class)
public class ChannelController internal constructor(
    private val mutableState: ChannelMutableState,
    private val channelLogic: ChannelLogic,
    private val client: ChatClient,
    @VisibleForTesting
    internal val domainImpl: ChatDomainImpl,
    private val attachmentUploader: AttachmentUploader = AttachmentUploader(client),
) {
    public val channelType: String by mutableState::channelType
    public val channelId: String by mutableState::channelId
    public val cid: String by mutableState::cid

    private val editJobs = mutableMapOf<String, Job>()

    private var lastMarkReadEvent: Date? by mutableState::lastMarkReadEvent
    private var lastKeystrokeAt: Date? by mutableState::lastKeystrokeAt
    private var lastStartTypingEvent: Date? by mutableState::lastStartTypingEvent
    private val channelClient = client.channel(channelType, channelId)

    private var keystrokeParentMessageId: String? by mutableState::keystrokeParentMessageId

    private val logger = ChatLogger.get("ChatDomain ChannelController")

    private val threadControllerMap: ConcurrentHashMap<String, ThreadController> = ConcurrentHashMap()

    internal val unfilteredMessages by mutableState::messageList
    internal val hideMessagesBefore by mutableState::hideMessagesBefore

    public val messages: StateFlow<List<Message>> = mutableState.messages
    public val repliedMessage: StateFlow<Message?> = mutableState.repliedMessage
    public val messagesState: StateFlow<MessagesState> = mutableState.messagesState.map {
        when (it) {
            io.getstream.chat.android.offline.experimental.channel.state.MessagesState.Loading -> MessagesState.Loading
            io.getstream.chat.android.offline.experimental.channel.state.MessagesState.NoQueryActive -> MessagesState.NoQueryActive
            io.getstream.chat.android.offline.experimental.channel.state.MessagesState.OfflineNoResults -> MessagesState.OfflineNoResults
            is io.getstream.chat.android.offline.experimental.channel.state.MessagesState.Result -> MessagesState.Result(
                it.messages
            )
        }
    }.stateIn(domainImpl.scope, SharingStarted.Eagerly, MessagesState.NoQueryActive)
    public val oldMessages: StateFlow<List<Message>> by mutableState::oldMessages
    public val watcherCount: StateFlow<Int> by mutableState::watcherCount
    public val watchers: StateFlow<List<User>> by mutableState::watchers
    public val typing: StateFlow<TypingEvent> by mutableState::typing
    public val reads: StateFlow<List<ChannelUserRead>> by mutableState::reads
    public val read: StateFlow<ChannelUserRead?> by mutableState::read
    public val unreadCount: StateFlow<Int?> by mutableState::unreadCount
    public val members: StateFlow<List<Member>> by mutableState::members
    public val channelData: StateFlow<ChannelData> by mutableState::channelData
    public val hidden: StateFlow<Boolean> by mutableState::hidden
    public val muted: StateFlow<Boolean> by mutableState::muted
    public val loading: StateFlow<Boolean> by mutableState::loading
    public val loadingOlderMessages: StateFlow<Boolean> by mutableState::loadingOlderMessages
    public val loadingNewerMessages: StateFlow<Boolean> by mutableState::loadingNewerMessages
    public val endOfOlderMessages: StateFlow<Boolean> by mutableState::endOfOlderMessages
    public val endOfNewerMessages: StateFlow<Boolean> by mutableState::endOfNewerMessages
    public val channelConfig: StateFlow<Config> by mutableState::_channelConfig
    public val recoveryNeeded: Boolean by mutableState::recoveryNeeded

    internal fun getThread(threadState: ThreadMutableState, threadLogic: ThreadLogic): ThreadController =
        threadControllerMap.getOrPut(threadState.parentId) {
            ThreadController(
                threadState,
                threadLogic,
                client
            ).also { domainImpl.scope.launch { it.loadOlderMessages() } }
        }

    /**
     * Marks the channel as read by the current user
     *
     * @return whether the channel was marked as read or not
     */
    internal fun markRead(): Boolean {
        if (!mutableState._channelConfig.value.readEventsEnabled) {
            return false
        }

        // throttle the mark read
        val messages = mutableState.sortedMessages.value

        if (messages.isEmpty()) {
            logger.logI("No messages; nothing to mark read.")
            return false
        }

        return messages.last().createdAt
            .let { lastMessageDate ->
                val shouldUpdate =
                    lastMarkReadEvent == null || lastMessageDate?.after(lastMarkReadEvent) == true

                if (!shouldUpdate) {
                    logger.logI("Last message date [$lastMessageDate] is not after last read event [$lastMarkReadEvent]; no need to update.")
                    return false
                }

                lastMarkReadEvent = lastMessageDate

                // update live data with new read
                domainImpl.user.value?.let { currentUser ->
                    updateRead(ChannelUserRead(currentUser, lastMarkReadEvent))
                }

                shouldUpdate
            }
    }

    internal suspend fun hide(clearHistory: Boolean): Result<Unit> {
        channelLogic.setHidden(true)
        val result = channelClient.hide(clearHistory).await()
        if (result.isSuccess) {
            if (clearHistory) {
                val now = Date()
                mutableState.hideMessagesBefore = now
                channelLogic.removeMessagesBefore(now)
                domainImpl.repos.deleteChannelMessagesBefore(cid, now)
                domainImpl.repos.setHiddenForChannel(cid, true, now)
            } else {
                domainImpl.repos.setHiddenForChannel(cid, true)
            }
        }
        return result
    }

    internal suspend fun show(): Result<Unit> {
        return channelClient.show().await()
    }

    /** Leave the channel action. Fires an API request. */
    internal suspend fun leave(): Result<Unit> {
        val result = domainImpl.user.value?.let { currentUser ->
            channelClient.removeMembers(currentUser.id).await()
        }

        return if (result?.isSuccess == true) {
            Result(Unit)
        } else {
            Result(result?.error() ?: ChatError("Current user null"))
        }
    }

    internal suspend fun watch(limit: Int = 30) {
        // Otherwise it's too easy for devs to create UI bugs which DDOS our API
        if (mutableState._loading.value) {
            logger.logI("Another request to watch this channel is in progress. Ignoring this request.")
            return
        }
        runChannelQuery(QueryChannelPaginationRequest(limit).toWatchChannelRequest(domainImpl.userPresence))
    }

    /** Loads a list of messages before the oldest message in the current list. */
    internal suspend fun loadOlderMessages(limit: Int = 30): Result<Channel> {
        return runChannelQuery(channelLogic.olderWatchChannelRequest(limit = limit, baseMessageId = null))
    }

    /** Loads a list of messages after the newest message in the current list. */
    internal suspend fun loadNewerMessages(limit: Int = 30): Result<Channel> {
        return runChannelQuery(channelLogic.newerWatchChannelRequest(limit = limit, baseMessageId = null))
    }

    /** Loads a list of messages before the message with particular message id. */
    private suspend fun loadOlderMessages(messageId: String, limit: Int): Result<Channel> {
        return runChannelQuery(channelLogic.olderWatchChannelRequest(limit = limit, baseMessageId = messageId))
    }

    /** Loads a list of messages after the message with particular message id. */
    private suspend fun loadNewerMessages(messageId: String, limit: Int): Result<Channel> {
        return runChannelQuery(channelLogic.newerWatchChannelRequest(limit = limit, baseMessageId = messageId))
    }

    private suspend fun runChannelQuery(request: WatchChannelRequest): Result<Channel> {
        val preconditionResult = channelLogic.onQueryChannelPrecondition(channelType, channelId, request)
        if (preconditionResult.isError) {
            return Result.error(preconditionResult.error())
        }

        val offlineChannel = channelLogic.runChannelQueryOffline(request)

        val onlineResult = client.queryChannelInternal(channelType, channelId, request).await().also { result ->
            channelLogic.onQueryChannelResult(result, channelType, channelId, request)
        }

        return when {
            onlineResult.isSuccess -> onlineResult
            offlineChannel != null -> Result.success(offlineChannel)
            else -> onlineResult
        }
    }

    internal suspend fun sendMessage(message: Message): Result<Message> = channelClient.sendMessage(message).await()

    internal suspend fun retrySendMessage(message: Message): Result<Message> =
        channelClient.sendMessage(message, true).await()

    internal suspend fun uploadAttachments(
        message: Message,
    ): List<Attachment> {
        return try {
            message.attachments.map { attachment ->
                if (attachment.uploadState != Attachment.UploadState.Success) {
                    attachmentUploader.uploadAttachment(
                        channelType,
                        channelId,
                        attachment,
                        ProgressCallbackImpl(message.id, attachment.uploadId!!)
                    )
                        .recover { error -> attachment.apply { uploadState = Attachment.UploadState.Failed(error) } }
                        .data()
                } else {
                    attachment
                }
            }.toMutableList()
        } catch (e: Exception) {
            message.attachments.map {
                if (it.uploadState != Attachment.UploadState.Success) {
                    it.uploadState = Attachment.UploadState.Failed(ChatError(e.message, e))
                }
                it
            }.toMutableList()
        }.also { attachments ->
            message.attachments = attachments
            // TODO refactor this place. A lot of side effects happening here.
            //  We should extract it to entity that will handle logic of uploading only.
            if (message.attachments.any { attachment -> attachment.uploadState is Attachment.UploadState.Failed }) {
                message.syncStatus = SyncStatus.FAILED_PERMANENTLY
            }
            // RepositoryFacade::insertMessage is implemented as upsert, therefore we need to delete the message first
            domainImpl.repos.deleteChannelMessage(message)
            domainImpl.repos.insertMessage(message)
            upsertMessage(message)
        }
    }

    private fun updateAttachmentUploadState(messageId: String, uploadId: String, newState: Attachment.UploadState) {
        val message = mutableState.messageList.value.firstOrNull { it.id == messageId }
        if (message != null) {
            val newAttachments = message.attachments.map { attachment ->
                if (attachment.uploadId == uploadId) {
                    attachment.copy(uploadState = newState)
                } else {
                    attachment
                }
            }
            val updatedMessage = message.copy(attachments = newAttachments.toMutableList())
            val newMessages =
                mutableState.messageList.value.associateBy(Message::id) + (updatedMessage.id to updatedMessage)
            mutableState._messages.value = newMessages
        }
    }

    /**
     * Cancels ephemeral Message.
     * Removes message from the offline storage and memory and notifies about update.
     */
    internal suspend fun cancelEphemeralMessage(message: Message): Result<Boolean> {
        require(message.isEphemeral()) { "Only ephemeral message can be canceled" }
        domainImpl.repos.deleteChannelMessage(message)
        removeLocalMessage(message)
        return Result(true)
    }

    internal suspend fun sendImage(file: File): Result<String> {
        return client.sendImage(channelType, channelId, file).await()
    }

    internal suspend fun sendFile(file: File): Result<String> {
        return client.sendFile(channelType, channelId, file).await()
    }

    // This one needs to be public for flows such as running a message action

    internal fun upsertMessage(message: Message) {
        channelLogic.upsertMessages(listOf(message))
    }

    public fun getMessage(messageId: String): Message? = channelLogic.getMessage(messageId)

    internal fun removeLocalMessage(message: Message) {
        channelLogic.removeLocalMessage(message)
    }

    public fun clean() {
        domainImpl.scope.launch {
            // cleanup your own typing state
            val now = Date()
            if (lastStartTypingEvent != null && now.time - lastStartTypingEvent!!.time > 5000) {
                keystrokeParentMessageId?.let {
                    channelClient.stopTyping(it)
                } ?: channelClient.stopTyping()
            }

            // Cleanup typing events that are older than 15 seconds
            var copy = mutableState._typing.value
            var changed = false
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.SECOND, -15)
            val old = calendar.time
            for ((userId, typing) in copy.toList()) {
                if (typing.createdAt.before(old)) {
                    copy = copy - userId
                    changed = true
                }
            }
            if (changed) {
                mutableState._typing.value = copy
            }
        }
    }

    internal fun setTyping(userId: String, event: ChatEvent?) = channelLogic.setTyping(userId, event)

    internal fun handleEvents(events: List<ChatEvent>) = channelLogic.handleEvents(events)

    internal fun handleEvent(event: ChatEvent) = channelLogic.handleEvent(event)

    private fun updateRead(read: ChannelUserRead) = channelLogic.updateReads(listOf(read))

    internal fun updateDataFromChannel(c: Channel) = channelLogic.updateDataFromChannel(c)

    /**
     * Edits the specified message. Local storage is updated immediately.
     * The API request is retried according to the retry policy specified on the chatDomain.
     *
     * @param message The message to edit.
     *
     * @return Executable async [Call] responsible for editing a message.
     *
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */
    @Deprecated(
        message = "ChatDomain.editMessage is deprecated. Use function ChatClient::updateMessage instead",
        replaceWith = ReplaceWith(
            expression = "ChatClient.instance().updateMessage(message)",
            imports = arrayOf("io.getstream.chat.android.client.ChatClient")
        ),
        level = DeprecationLevel.WARNING
    )
    internal suspend fun editMessage(message: Message): Result<Message> {
        // TODO: should we rename edit message into update message to be similar to llc?
        val online = domainImpl.isOnline()
        val messageToBeEdited = message.copy(
            updatedLocallyAt = Date(),
            syncStatus = if (!online) SyncStatus.SYNC_NEEDED else SyncStatus.IN_PROGRESS
        )

        // Update flow
        upsertMessage(messageToBeEdited)

        // Update Room State
        domainImpl.repos.insertMessage(messageToBeEdited)

        if (online) {
            // updating a message should cancel prior runnables editing the same message...
            // cancel previous message jobs
            editJobs[message.id]?.cancelAndJoin()
            // TODO: Will be removed after migrating ChatDomain
            val job = domainImpl.scope.async {
                client.updateMessageInternal(messageToBeEdited).retry(domainImpl.scope, client.retryPolicy).await()
            }
            editJobs[message.id] = job
            val result = job.await()
            if (result.isSuccess) {
                val editedMessage = result.data()
                editedMessage.syncStatus = SyncStatus.COMPLETED
                upsertMessage(editedMessage)
                domainImpl.repos.insertMessage(editedMessage)

                return Result(editedMessage)
            } else {
                val failedMessage = messageToBeEdited.copy(
                    syncStatus = if (result.error().isPermanent()) {
                        SyncStatus.FAILED_PERMANENTLY
                    } else {
                        SyncStatus.SYNC_NEEDED
                    },
                    updatedLocallyAt = Date(),
                )

                upsertMessage(failedMessage)
                domainImpl.repos.insertMessage(failedMessage)
                return Result(result.error())
            }
        }
        return Result(messageToBeEdited)
    }

    public fun toChannel(): Channel = mutableState.toChannel()

    internal suspend fun loadMessageById(
        messageId: String,
        newerMessagesOffset: Int,
        olderMessagesOffset: Int,
    ): Result<Message> {
        val message = client.getMessage(messageId).await()
            .takeIf { it.isSuccess }
            ?.data()
            ?: domainImpl.repos.selectMessage(messageId)
            ?: return Result(ChatError("Error while fetching message from backend. Message id: $messageId"))
        channelLogic.storeMessageLocally(listOf(message))
        upsertMessage(message)
        loadOlderMessages(messageId, newerMessagesOffset)
        loadNewerMessages(messageId, olderMessagesOffset)
        return Result(message)
    }

    internal fun replyMessage(repliedMessage: Message?) {
        mutableState._repliedMessage.value = repliedMessage
    }

    public sealed class MessagesState {
        /** The ChannelController is initialized but no query is currently running.
         * If you know that a query will be started you typically want to display a loading icon.
         */
        public object NoQueryActive : MessagesState()

        /** Indicates we are loading the first page of results.
         * We are in this state if ChannelController.loading is true
         * For seeing if we're loading more results have a look at loadingNewerMessages and loadingOlderMessages
         *
         * @see loading
         * @see loadingNewerMessages
         * @see loadingOlderMessages
         */
        public object Loading : MessagesState()

        /** If we are offline and don't have channels stored in offline storage, typically displayed as an error condition. */
        public object OfflineNoResults : MessagesState()

        /** The list of messages, loaded either from offline storage or an API call.
         * Observe chatDomain.online to know if results are currently up to date
         * @see ChatDomainImpl.connectionState
         */
        public data class Result(val messages: List<Message>) : MessagesState()
    }

    internal inner class ProgressCallbackImpl(private val messageId: String, private val uploadId: String) :
        ProgressCallback {
        override fun onSuccess(url: String?) {
            updateAttachmentUploadState(messageId, uploadId, Attachment.UploadState.Success)
        }

        override fun onError(error: ChatError) {
            updateAttachmentUploadState(messageId, uploadId, Attachment.UploadState.Failed(error))
        }

        override fun onProgress(bytesUploaded: Long, totalBytes: Long) {
            updateAttachmentUploadState(
                messageId,
                uploadId,
                Attachment.UploadState.InProgress(bytesUploaded, totalBytes)
            )
        }
    }
}
