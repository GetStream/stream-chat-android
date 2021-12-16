package io.getstream.chat.android.offline.channel

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.SendActionRequest
import io.getstream.chat.android.client.api.models.WatchChannelRequest
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.ChannelHiddenEvent
import io.getstream.chat.android.client.events.ChannelTruncatedEvent
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.ChannelUserBannedEvent
import io.getstream.chat.android.client.events.ChannelUserUnbannedEvent
import io.getstream.chat.android.client.events.ChannelVisibleEvent
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.ConnectingEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.ErrorEvent
import io.getstream.chat.android.client.events.GlobalUserBannedEvent
import io.getstream.chat.android.client.events.GlobalUserUnbannedEvent
import io.getstream.chat.android.client.events.HealthEvent
import io.getstream.chat.android.client.events.MarkAllReadEvent
import io.getstream.chat.android.client.events.MemberAddedEvent
import io.getstream.chat.android.client.events.MemberRemovedEvent
import io.getstream.chat.android.client.events.MemberUpdatedEvent
import io.getstream.chat.android.client.events.MessageDeletedEvent
import io.getstream.chat.android.client.events.MessageReadEvent
import io.getstream.chat.android.client.events.MessageUpdatedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationChannelDeletedEvent
import io.getstream.chat.android.client.events.NotificationChannelMutesUpdatedEvent
import io.getstream.chat.android.client.events.NotificationChannelTruncatedEvent
import io.getstream.chat.android.client.events.NotificationInviteAcceptedEvent
import io.getstream.chat.android.client.events.NotificationInviteRejectedEvent
import io.getstream.chat.android.client.events.NotificationInvitedEvent
import io.getstream.chat.android.client.events.NotificationMarkReadEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.NotificationMutesUpdatedEvent
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.events.ReactionDeletedEvent
import io.getstream.chat.android.client.events.ReactionNewEvent
import io.getstream.chat.android.client.events.ReactionUpdateEvent
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.TypingStopEvent
import io.getstream.chat.android.client.events.UnknownEvent
import io.getstream.chat.android.client.events.UserDeletedEvent
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.events.UserUpdatedEvent
import io.getstream.chat.android.client.extensions.enrichWithCid
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.client.utils.map
import io.getstream.chat.android.client.utils.recover
import io.getstream.chat.android.client.utils.toUnitResult
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.experimental.channel.logic.ChannelLogic
import io.getstream.chat.android.offline.experimental.channel.state.ChannelMutableState
import io.getstream.chat.android.offline.experimental.channel.thread.logic.ThreadLogic
import io.getstream.chat.android.offline.experimental.channel.thread.state.ThreadMutableState
import io.getstream.chat.android.offline.extensions.addMyReaction
import io.getstream.chat.android.offline.extensions.isPermanent
import io.getstream.chat.android.offline.extensions.removeMyReaction
import io.getstream.chat.android.offline.message.MessageSendingService
import io.getstream.chat.android.offline.message.MessageSendingServiceFactory
import io.getstream.chat.android.offline.message.attachment.AttachmentUploader
import io.getstream.chat.android.offline.message.isEphemeral
import io.getstream.chat.android.offline.message.wasCreatedAfter
import io.getstream.chat.android.offline.message.wasCreatedBeforeOrAt
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
    messageSendingServiceFactory: MessageSendingServiceFactory = MessageSendingServiceFactory(),
) {
    public val channelType: String by mutableState::channelType
    public val channelId: String by mutableState::channelId
    public val cid: String by mutableState::cid

    private val editJobs = mutableMapOf<String, Job>()

    private var lastMarkReadEvent: Date? by mutableState::lastMarkReadEvent
    private var lastKeystrokeAt: Date? by mutableState::lastKeystrokeAt
    private var lastStartTypingEvent: Date? by mutableState::lastStartTypingEvent
    private val channelClient = client.channel(channelType, channelId)

    private var keystrokeParentMessageId: String? = null

    private val logger = ChatLogger.get("ChatDomain ChannelController")

    private val threadControllerMap: ConcurrentHashMap<String, ThreadController> = ConcurrentHashMap()

    private val messageSendingService: MessageSendingService =
        messageSendingServiceFactory.create(domainImpl, this, client.channel(cid))

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
    public val channelConfig: StateFlow<Config> by mutableState::channelConfig
    public val recoveryNeeded: Boolean by mutableState::recoveryNeeded

    internal fun getThread(threadState: ThreadMutableState, threadLogic: ThreadLogic): ThreadController =
        threadControllerMap.getOrPut(threadState.parentId) {
            ThreadController(
                threadState,
                threadLogic,
                client
            ).also { domainImpl.scope.launch { it.loadOlderMessages() } }
        }

    internal suspend fun keystroke(parentId: String?): Result<Boolean> {
        if (!mutableState.channelConfig.value.typingEventsEnabled) return Result(false)
        lastKeystrokeAt = Date()
        if (lastStartTypingEvent == null || lastKeystrokeAt!!.time - lastStartTypingEvent!!.time > 3000) {
            lastStartTypingEvent = lastKeystrokeAt

            val channelClient = client.channel(channelType = channelType, channelId = channelId)
            val result = if (parentId != null) {
                channelClient.keystroke(parentId)
            } else {
                channelClient.keystroke()
            }.await()
            return result.map { true.also { keystrokeParentMessageId = parentId } }
        }
        return Result(false)
    }

    internal suspend fun stopTyping(parentId: String?): Result<Boolean> {
        if (!mutableState.channelConfig.value.typingEventsEnabled) return Result(false)
        if (lastStartTypingEvent != null) {
            lastStartTypingEvent = null
            lastKeystrokeAt = null

            val channelClient = client.channel(channelType = channelType, channelId = channelId)
            val result = if (parentId != null) {
                channelClient.stopTyping(parentId)
            } else {
                channelClient.stopTyping()
            }.await()

            return result.map { true.also { keystrokeParentMessageId = null } }
        }
        return Result(false)
    }

    /**
     * Marks the channel as read by the current user
     *
     * @return whether the channel was marked as read or not
     */
    internal fun markRead(): Boolean {
        if (!mutableState.channelConfig.value.readEventsEnabled) {
            return false
        }

        // throttle the mark read
        val messages = mutableState.sortedMessages.value

        if (messages.isEmpty()) {
            logger.logI("No messages; nothing to mark read.")
            return false
        }

        return messages
            .last()
            .let { it.createdAt ?: it.createdLocallyAt }
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

    private fun removeMessagesBefore(date: Date) {
        mutableState._messages.value = mutableState._messages.value.filter { it.value.wasCreatedAfter(date) }
    }

    internal suspend fun hide(clearHistory: Boolean): Result<Unit> {
        channelLogic.setHidden(true)
        val result = channelClient.hide(clearHistory).await()
        if (result.isSuccess) {
            if (clearHistory) {
                val now = Date()
                mutableState.hideMessagesBefore = now
                removeMessagesBefore(now)
                domainImpl.repos.deleteChannelMessagesBefore(cid, now)
                domainImpl.repos.setHiddenForChannel(cid, true, now)
            } else {
                domainImpl.repos.setHiddenForChannel(cid, true)
            }
        }
        return result
    }

    internal suspend fun show(): Result<Unit> {
        channelLogic.setHidden(false)
        val result = channelClient.show().await()
        if (result.isSuccess) {
            domainImpl.repos.setHiddenForChannel(cid, false)
        }
        return result
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

    /** Delete the channel action. Fires an API request. */
    internal suspend fun delete(): Result<Unit> = channelClient.delete().await().toUnitResult()

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

    internal suspend fun sendMessage(message: Message): Result<Message> = messageSendingService.sendNewMessage(message)

    internal suspend fun retrySendMessage(message: Message): Result<Message> =
        messageSendingService.sendMessage(message)

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
        val message = mutableState._messages.value[messageId]
        if (message != null) {
            val newAttachments = message.attachments.map { attachment ->
                if (attachment.uploadId == uploadId) {
                    attachment.copy(uploadState = newState)
                } else {
                    attachment
                }
            }
            val updatedMessage = message.copy(attachments = newAttachments.toMutableList())
            val newMessages = mutableState._messages.value + (updatedMessage.id to updatedMessage)
            mutableState._messages.value = newMessages
        }
    }

    internal suspend fun handleSendMessageSuccess(processedMessage: Message): Message {
        return processedMessage
            .let { message -> message.enrichWithCid(cid) }
            .copy(syncStatus = SyncStatus.COMPLETED)
            .also { domainImpl.repos.insertMessage(it) }
            .also { upsertMessage(it) }
    }

    internal suspend fun handleSendMessageFail(message: Message, error: ChatError): Message {
        logger.logE(
            "Failed to send message with id ${message.id} and text ${message.text}: $error",
            error
        )

        return message.copy(
            syncStatus = if (error.isPermanent()) {
                SyncStatus.FAILED_PERMANENTLY
            } else {
                SyncStatus.SYNC_NEEDED
            },
            updatedLocallyAt = Date(),
        )
            .also { domainImpl.repos.insertMessage(it) }
            .also { upsertMessage(it) }
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

    internal suspend fun sendGiphy(message: Message): Result<Message> {
        val request = SendActionRequest(
            message.cid,
            message.id,
            message.type,
            mapOf(KEY_MESSAGE_ACTION to MESSAGE_ACTION_SEND)
        )
        val result = domainImpl.runAndRetry { channelClient.sendAction(request) }
        removeLocalMessage(message)
        return if (result.isSuccess) {
            Result(result.data())
        } else {
            Result(result.error())
        }
    }

    internal suspend fun shuffleGiphy(message: Message): Result<Message> {
        val request = SendActionRequest(
            message.cid,
            message.id,
            message.type,
            mapOf(KEY_MESSAGE_ACTION to MESSAGE_ACTION_SHUFFLE)
        )
        val result = domainImpl.runAndRetry { channelClient.sendAction(request) }

        return if (result.isSuccess) {
            val processedMessage: Message = result.data()
            processedMessage.apply {
                syncStatus = SyncStatus.COMPLETED
                domainImpl.repos.insertMessage(this)
            }
            upsertMessage(processedMessage)
            Result(processedMessage)
        } else {
            Result(result.error())
        }
    }

    internal suspend fun sendImage(file: File): Result<String> {
        return client.sendImage(channelType, channelId, file).await()
    }

    internal suspend fun sendFile(file: File): Result<String> {
        return client.sendFile(channelType, channelId, file).await()
    }

    /**
     * sendReaction posts the reaction on local storage
     * message reaction count should increase, latest reactions and own_reactions should be updated
     *
     * If you're online we make the API call to sync to the server
     * If the request fails we retry according to the retry policy set on the repo
     */
    internal suspend fun sendReaction(reaction: Reaction, enforceUnique: Boolean): Result<Reaction> {
        val currentUser = domainImpl.user.value ?: return Result(ChatError("Current user null in Chatdomain"))

        reaction.apply {
            user = currentUser
            userId = currentUser.id
            syncStatus = SyncStatus.IN_PROGRESS
            this.enforceUnique = enforceUnique
        }
        val online = domainImpl.isOnline()
        // insert the message into local storage

        if (!online) {
            reaction.syncStatus = SyncStatus.SYNC_NEEDED
        }
        if (enforceUnique) {
            // remove all user's reactions to the message
            domainImpl.repos.updateReactionsForMessageByDeletedDate(
                userId = currentUser.id,
                messageId = reaction.messageId,
                deletedAt = Date()
            )
        }
        // update flow
        val currentMessage = getMessage(reaction.messageId)?.copy()
        currentMessage?.let {
            it.addMyReaction(reaction, enforceUnique = enforceUnique)
            upsertMessage(it)
            domainImpl.repos.insertMessage(it)
        }

        if (online) {
            val result = domainImpl.runAndRetry { client.sendReaction(reaction, enforceUnique) }
            return if (result.isSuccess) {
                reaction.syncStatus = SyncStatus.COMPLETED
                domainImpl.repos.insertReaction(reaction)
                Result(result.data())
            } else {
                logger.logE(
                    "Failed to send reaction of type ${reaction.type} on messge ${reaction.messageId}",
                    result.error()
                )

                if (result.error().isPermanent()) {
                    reaction.syncStatus = SyncStatus.FAILED_PERMANENTLY
                } else {
                    reaction.syncStatus = SyncStatus.SYNC_NEEDED
                }
                domainImpl.repos.insertReaction(reaction)
                Result(result.error())
            }
        }
        return Result(reaction)
    }

    internal suspend fun deleteReaction(reaction: Reaction): Result<Message> {
        val currentUser = domainImpl.user.value ?: return Result(ChatError("Current user null in Chatdomain"))

        val online = domainImpl.isOnline()
        reaction.apply {
            user = currentUser
            userId = currentUser.id
            syncStatus = SyncStatus.IN_PROGRESS
            deletedAt = Date()
        }
        if (!online) {
            reaction.syncStatus = SyncStatus.SYNC_NEEDED
        }

        domainImpl.repos.insertReaction(reaction)

        // update flow
        val currentMessage = getMessage(reaction.messageId)?.copy()
        currentMessage?.apply { removeMyReaction(reaction) }
            ?.also {
                upsertMessage(it)
                domainImpl.repos.insertMessage(it)
            }

        if (online) {
            val result = domainImpl.runAndRetry { client.deleteReaction(reaction.messageId, reaction.type) }
            return if (result.isSuccess) {
                reaction.syncStatus = SyncStatus.COMPLETED
                domainImpl.repos.insertReaction(reaction)
                Result(result.data())
            } else {
                if (result.error().isPermanent()) {
                    reaction.syncStatus = SyncStatus.FAILED_PERMANENTLY
                } else {
                    reaction.syncStatus = SyncStatus.SYNC_NEEDED
                }
                domainImpl.repos.insertReaction(reaction)
                Result(result.error())
            }
        }

        return if (currentMessage != null) {
            Result(currentMessage)
        } else {
            Result(ChatError("Local message was not found"))
        }
    }

    // This one needs to be public for flows such as running a message action

    internal fun upsertMessage(message: Message) {
        channelLogic.upsertMessages(listOf(message))
    }

    private fun upsertEventMessage(message: Message) {
        // make sure we don't lose ownReactions
        getMessage(message.id)?.let {
            message.ownReactions = it.ownReactions
        }
        channelLogic.upsertMessages(listOf(message))
    }

    public fun getMessage(messageId: String): Message? {
        val copy = mutableState._messages.value
        var message = copy[messageId]

        if (mutableState.hideMessagesBefore != null) {
            if (message != null && message.wasCreatedBeforeOrAt(mutableState.hideMessagesBefore)) {
                message = null
            }
        }

        return message
    }

    private fun removeLocalMessage(message: Message) {
        mutableState._messages.value = mutableState._messages.value - message.id
    }

    public fun clean() {
        domainImpl.scope.launch {
            // cleanup your own typing state
            val now = Date()
            if (lastStartTypingEvent != null && now.time - lastStartTypingEvent!!.time > 5000) {
                stopTyping(keystrokeParentMessageId)
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

    internal fun setTyping(userId: String, event: ChatEvent?) {
        val copy = mutableState._typing.value.toMutableMap()
        if (event == null) {
            copy.remove(userId)
        } else {
            copy[userId] = event
        }
        domainImpl.user.value?.id.let(copy::remove)
        mutableState._typing.value = copy.toMap()
    }

    internal suspend fun handleEvents(events: List<ChatEvent>) {
        for (event in events) {
            handleEvent(event)
        }
    }

    internal fun handleEvent(event: ChatEvent) {
        when (event) {
            is NewMessageEvent -> {
                upsertEventMessage(event.message)
                channelLogic.incrementUnreadCountIfNecessary(event.message)
                channelLogic.setHidden(false)
            }
            is MessageUpdatedEvent -> {
                event.message.apply {
                    replyTo = mutableState._messages.value[replyMessageId]
                }.let(::upsertEventMessage)

                channelLogic.setHidden(false)
            }
            is MessageDeletedEvent -> {
                if (event.hardDelete) {
                    removeLocalMessage(event.message)
                } else {
                    upsertEventMessage(event.message)
                }
                channelLogic.setHidden(false)
            }
            is NotificationMessageNewEvent -> {
                upsertEventMessage(event.message)
                channelLogic.incrementUnreadCountIfNecessary(event.message)
                channelLogic.setHidden(false)
            }
            is ReactionNewEvent -> {
                upsertMessage(event.message)
            }
            is ReactionUpdateEvent -> {
                upsertMessage(event.message)
            }
            is ReactionDeletedEvent -> {
                upsertMessage(event.message)
            }
            is MemberRemovedEvent -> {
                deleteMember(event.user.id)
            }
            is MemberAddedEvent -> {
                upsertMember(event.member)
            }
            is MemberUpdatedEvent -> {
                upsertMember(event.member)
            }
            is NotificationAddedToChannelEvent -> {
                upsertMembers(event.channel.members)
            }
            is UserPresenceChangedEvent -> {
                upsertUserPresence(event.user)
            }
            is UserUpdatedEvent -> {
                upsertUser(event.user)
            }
            is UserStartWatchingEvent -> {
                upsertWatcher(event.user)
                channelLogic.setWatcherCount(event.watcherCount)
            }
            is UserStopWatchingEvent -> {
                deleteWatcher(event.user)
                channelLogic.setWatcherCount(event.watcherCount)
            }
            is ChannelUpdatedEvent -> {
                channelLogic.updateChannelData(event.channel)
            }
            is ChannelUpdatedByUserEvent -> {
                channelLogic.updateChannelData(event.channel)
            }
            is ChannelHiddenEvent -> {
                channelLogic.setHidden(true)
            }
            is ChannelVisibleEvent -> {
                channelLogic.setHidden(false)
            }
            is ChannelDeletedEvent -> {
                removeMessagesBefore(event.createdAt)
                mutableState._channelData.value = mutableState.channelData.value.copy(deletedAt = event.createdAt)
            }
            is ChannelTruncatedEvent,
            is NotificationChannelTruncatedEvent,
            -> {
                removeMessagesBefore(event.createdAt)
            }
            is TypingStopEvent -> {
                setTyping(event.user.id, null)
            }
            is TypingStartEvent -> {
                setTyping(event.user.id, event)
            }
            is MessageReadEvent -> {
                updateRead(ChannelUserRead(event.user, event.createdAt))
            }
            is NotificationMarkReadEvent -> {
                updateRead(ChannelUserRead(event.user, event.createdAt))
            }
            is MarkAllReadEvent -> {
                updateRead(ChannelUserRead(event.user, event.createdAt))
            }
            is NotificationInviteAcceptedEvent -> {
                upsertMember(event.member)
                channelLogic.updateChannelData(event.channel)
            }
            is NotificationInviteRejectedEvent -> {
                upsertMember(event.member)
                channelLogic.updateChannelData(event.channel)
            }
            is NotificationChannelMutesUpdatedEvent -> {
                mutableState._muted.value = event.me.channelMutes.any { mute ->
                    mute.channel.cid == cid
                }
            }
            is ChannelUserBannedEvent,
            is ChannelUserUnbannedEvent,
            is NotificationChannelDeletedEvent,
            is NotificationInvitedEvent,
            is NotificationRemovedFromChannelEvent,
            is ConnectedEvent,
            is ConnectingEvent,
            is DisconnectedEvent,
            is ErrorEvent,
            is GlobalUserBannedEvent,
            is GlobalUserUnbannedEvent,
            is HealthEvent,
            is NotificationMutesUpdatedEvent,
            is UnknownEvent,
            is UserDeletedEvent,
            -> Unit // Ignore these events
        }
    }

    private fun upsertUserPresence(user: User) {
        val userId = user.id
        // members and watchers have users
        val members = mutableState._members.value
        val watchers = mutableState._watchers.value
        val member = members[userId]?.copy()
        val watcher = watchers[userId]
        if (member != null) {
            member.user = user
            upsertMember(member)
        }
        if (watcher != null) {
            upsertWatcher(user)
        }
    }

    private fun upsertUser(user: User) {
        upsertUserPresence(user)
        // channels have users
        val userId = user.id
        val channelData = mutableState._channelData.value
        if (channelData != null) {
            if (channelData.createdBy.id == userId) {
                channelData.createdBy = user
            }
        }

        // updating messages is harder
        // user updates don't happen frequently, it's probably ok for this update to be sluggish
        // if it turns out to be slow we can do a simple reverse index from user -> message
        val messages = mutableState._messages.value
        val changedMessages = mutableListOf<Message>()
        for (message in messages.values) {
            var changed = false
            if (message.user.id == userId) {
                message.user = user
                changed = true
            }
            for (reaction in message.ownReactions) {
                if (reaction.user!!.id == userId) {
                    reaction.user = user
                    changed = true
                }
            }
            for (reaction in message.latestReactions) {
                if (reaction.user!!.id == userId) {
                    reaction.user = user
                    changed = true
                }
            }
            if (changed) changedMessages.add(message)
        }
        if (changedMessages.isNotEmpty()) {
            channelLogic.upsertMessages(changedMessages)
        }
    }

    private fun deleteWatcher(user: User) {
        mutableState._watchers.value = mutableState._watchers.value - user.id
    }

    private fun upsertWatcher(user: User) {
        mutableState._watchers.value = mutableState._watchers.value + mapOf(user.id to user)
    }

    private fun deleteMember(userId: String) {
        mutableState._members.value = mutableState._members.value - userId
    }

    private fun upsertMembers(members: List<Member>) {
        mutableState._members.value = mutableState._members.value + members.associateBy { it.user.id }
    }

    private fun upsertMember(member: Member) {
        upsertMembers(listOf(member))
    }

    private fun updateRead(
        read: ChannelUserRead,
    ) {
        channelLogic.updateReads(listOf(read))
    }

    internal fun updateDataFromChannel(c: Channel) = channelLogic.updateDataFromChannel(c)

    internal suspend fun editMessage(message: Message): Result<Message> {
        // TODO: should we rename edit message into update message to be similar to llc?
        val online = domainImpl.isOnline()
        val messageToBeEdited = message.copy()

        messageToBeEdited.updatedLocallyAt = Date()

        messageToBeEdited.syncStatus = if (!online) SyncStatus.SYNC_NEEDED else SyncStatus.IN_PROGRESS

        // Update flow
        upsertMessage(messageToBeEdited)

        // Update Room State
        domainImpl.repos.insertMessage(messageToBeEdited)

        if (online) {
            val runnable = {
                client.updateMessage(messageToBeEdited)
            }
            // updating a message should cancel prior runnables editing the same message...
            // cancel previous message jobs
            editJobs[message.id]?.cancelAndJoin()
            val job = domainImpl.scope.async { domainImpl.runAndRetry(runnable) }
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

    internal suspend fun deleteMessage(message: Message, hard: Boolean = false): Result<Message> {
        val online = domainImpl.isOnline()
        val messageToBeDeleted = message.copy(deletedAt = Date())
        messageToBeDeleted.syncStatus = if (!online) SyncStatus.SYNC_NEEDED else SyncStatus.IN_PROGRESS

        // Update flow
        upsertMessage(messageToBeDeleted)

        // Update Room State
        domainImpl.repos.insertMessage(messageToBeDeleted)

        if (online) {
            val runnable = {
                client.deleteMessage(messageToBeDeleted.id, hard)
            }
            val result = domainImpl.runAndRetry(runnable)
            if (result.isSuccess) {
                val deletedMessage = result.data()
                deletedMessage.syncStatus = SyncStatus.COMPLETED
                upsertMessage(deletedMessage)
                domainImpl.repos.insertMessage(deletedMessage)
                return Result(deletedMessage)
            } else {
                val failureMessage = messageToBeDeleted.copy(
                    syncStatus = if (result.error().isPermanent()) {
                        SyncStatus.FAILED_PERMANENTLY
                    } else {
                        SyncStatus.SYNC_NEEDED
                    }
                )
                upsertMessage(failureMessage)
                domainImpl.repos.insertMessage(failureMessage)
                return Result(result.error())
            }
        }
        return Result(messageToBeDeleted)
    }

    public fun toChannel(): Channel {
        // recreate a channel object from the various observables.
        val channelData = mutableState._channelData.value ?: ChannelData(channelType, channelId)

        val messages = mutableState.sortedMessages.value
        val members = mutableState._members.value.values.toList()
        val watchers = mutableState._watchers.value.values.toList()
        val reads = mutableState._reads.value.values.toList()
        val watcherCount = mutableState._watcherCount.value

        val channel = channelData.toChannel(messages, members, reads, watchers, watcherCount)
        channel.config = mutableState.channelConfig.value
        channel.unreadCount = mutableState._unreadCount.value
        channel.lastMessageAt =
            mutableState.lastMessageAt.value ?: messages.lastOrNull()?.let { it.createdAt ?: it.createdLocallyAt }
        channel.hidden = mutableState._hidden.value

        return channel
    }

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
        upsertMessage(message)
        loadOlderMessages(messageId, newerMessagesOffset)
        loadNewerMessages(messageId, olderMessagesOffset)
        return Result(message)
    }

    internal fun replyMessage(repliedMessage: Message?) {
        mutableState._repliedMessage.value = repliedMessage
    }

    internal fun cancelJobs() = messageSendingService.cancelJobs()

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

    internal companion object {
        private const val KEY_MESSAGE_ACTION = "image_action"
        private const val MESSAGE_ACTION_SHUFFLE = "shuffle"
        private const val MESSAGE_ACTION_SEND = "send"
    }
}
