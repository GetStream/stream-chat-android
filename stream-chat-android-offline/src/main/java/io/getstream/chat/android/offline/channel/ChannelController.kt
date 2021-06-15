package io.getstream.chat.android.offline.channel

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.api.models.SendActionRequest
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.ChannelHiddenEvent
import io.getstream.chat.android.client.events.ChannelTruncatedEvent
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.ChannelVisibleEvent
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.MarkAllReadEvent
import io.getstream.chat.android.client.events.MemberAddedEvent
import io.getstream.chat.android.client.events.MemberRemovedEvent
import io.getstream.chat.android.client.events.MemberUpdatedEvent
import io.getstream.chat.android.client.events.MessageDeletedEvent
import io.getstream.chat.android.client.events.MessageReadEvent
import io.getstream.chat.android.client.events.MessageUpdatedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationChannelTruncatedEvent
import io.getstream.chat.android.client.events.NotificationInviteAcceptedEvent
import io.getstream.chat.android.client.events.NotificationInviteRejectedEvent
import io.getstream.chat.android.client.events.NotificationMarkReadEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.ReactionDeletedEvent
import io.getstream.chat.android.client.events.ReactionNewEvent
import io.getstream.chat.android.client.events.ReactionUpdateEvent
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.TypingStopEvent
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
import io.getstream.chat.android.client.utils.mapSuspend
import io.getstream.chat.android.client.utils.recover
import io.getstream.chat.android.client.utils.recoverSuspend
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.extensions.addMyReaction
import io.getstream.chat.android.offline.extensions.inOffsetWith
import io.getstream.chat.android.offline.extensions.isPermanent
import io.getstream.chat.android.offline.extensions.removeMyReaction
import io.getstream.chat.android.offline.message.MessageSendingService
import io.getstream.chat.android.offline.message.MessageSendingServiceFactory
import io.getstream.chat.android.offline.message.NEVER
import io.getstream.chat.android.offline.message.attachment.AttachmentUploader
import io.getstream.chat.android.offline.message.attachment.AttachmentUrlValidator
import io.getstream.chat.android.offline.message.attachment.generateUploadId
import io.getstream.chat.android.offline.message.getMessageType
import io.getstream.chat.android.offline.message.isEphemeral
import io.getstream.chat.android.offline.message.shouldIncrementUnreadCount
import io.getstream.chat.android.offline.message.wasCreatedAfter
import io.getstream.chat.android.offline.message.wasCreatedBeforeOrAt
import io.getstream.chat.android.offline.model.ChannelConfig
import io.getstream.chat.android.offline.request.QueryChannelPaginationRequest
import io.getstream.chat.android.offline.thread.ThreadController
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import java.util.Date
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max

public class ChannelController internal constructor(
    public val channelType: String,
    public val channelId: String,
    private val client: ChatClient,
    @VisibleForTesting
    internal val domainImpl: ChatDomainImpl,
    private val attachmentUrlValidator: AttachmentUrlValidator = AttachmentUrlValidator(),
    messageSendingServiceFactory: MessageSendingServiceFactory = MessageSendingServiceFactory(),
) {
    private val editJobs = mutableMapOf<String, Job>()

    private val _messages = MutableStateFlow<Map<String, Message>>(emptyMap())
    private val _watcherCount = MutableStateFlow(0)
    private val _typing = MutableStateFlow<Map<String, ChatEvent>>(emptyMap())
    private val _reads = MutableStateFlow<Map<String, ChannelUserRead>>(emptyMap())
    private val _read = MutableStateFlow<ChannelUserRead?>(null)
    private val _endOfNewerMessages = MutableStateFlow(false)
    private val _endOfOlderMessages = MutableStateFlow(false)
    private val _loading = MutableStateFlow(false)
    private val _hidden = MutableStateFlow(false)
    private val _muted = MutableStateFlow(false)
    private val _watchers = MutableStateFlow<Map<String, User>>(emptyMap())
    private val _members = MutableStateFlow<Map<String, Member>>(emptyMap())
    private val _loadingOlderMessages = MutableStateFlow(false)
    private val _loadingNewerMessages = MutableStateFlow(false)
    private val _channelData = MutableStateFlow<ChannelData?>(null)
    private val _oldMessages = MutableStateFlow<Map<String, Message>>(emptyMap())
    private val lastMessageAt = MutableStateFlow<Date?>(null)
    private val _repliedMessage = MutableStateFlow<Message?>(null)
    private val _unreadCount = MutableStateFlow(0)

    public val repliedMessage: StateFlow<Message?> = _repliedMessage
    internal var hideMessagesBefore: Date? = null
    internal val unfilteredMessages: StateFlow<List<Message>> =
        _messages.map { it.values.toList() }.stateIn(domainImpl.scope, SharingStarted.Eagerly, emptyList())

    /** a list of messages sorted by message.createdAt */
    private val sortedVisibleMessages: StateFlow<List<Message>> =
        messagesTransformation(_messages).stateIn(domainImpl.scope, SharingStarted.Eagerly, emptyList())
    public val messages: StateFlow<List<Message>> = sortedVisibleMessages

    private val _messagesState: StateFlow<MessagesState> =
        _loading.combine(sortedVisibleMessages) { loading: Boolean, messages: List<Message> ->
            when {
                loading -> MessagesState.Loading
                messages.isEmpty() -> MessagesState.OfflineNoResults
                else -> MessagesState.Result(messages)
            }
        }.stateIn(domainImpl.scope, SharingStarted.Eagerly, MessagesState.NoQueryActive)
    public val messagesState: StateFlow<MessagesState> = _messagesState

    public val oldMessages: StateFlow<List<Message>> = messagesTransformation(_oldMessages)

    private fun messagesTransformation(messages: MutableStateFlow<Map<String, Message>>): StateFlow<List<Message>> {
        return messages.map { messageMap ->
            messageMap.values
                .asSequence()
                .filter { it.parentId == null || it.showInChannel }
                .filter { it.user.id == domainImpl.currentUser.id || !it.shadowed }
                .filter { hideMessagesBefore == null || it.wasCreatedAfter(hideMessagesBefore) }
                .sortedBy { it.createdAt ?: it.createdLocallyAt }
                .toList()
        }.stateIn(domainImpl.scope, SharingStarted.Eagerly, emptyList())
    }

    /** the number of people currently watching the channel */
    public val watcherCount: StateFlow<Int> = _watcherCount

    /** the list of users currently watching this channel */
    public val watchers: StateFlow<List<User>> = _watchers.map { it.values.sortedBy(User::createdAt) }
        .stateIn(domainImpl.scope, SharingStarted.Eagerly, emptyList())

    /** who is currently typing (current user is excluded from this) */
    public val typing: StateFlow<TypingEvent> = _typing
        .map {
            val userList = it.values
                .sortedBy(ChatEvent::createdAt)
                .mapNotNull { event ->
                    when (event) {
                        is TypingStartEvent -> event.user.takeIf { user -> user != domainImpl.currentUser }
                        else -> null
                    }
                }

            TypingEvent(channelId, userList)
        }.stateIn(domainImpl.scope, SharingStarted.Eagerly, TypingEvent(channelId, emptyList()))

    /** how far every user in this channel has read */
    public val reads: StateFlow<List<ChannelUserRead>> = _reads
        .map { it.values.sortedBy(ChannelUserRead::lastRead) }
        .stateIn(domainImpl.scope, SharingStarted.Eagerly, emptyList())

    /** read status for the current user */
    public val read: StateFlow<ChannelUserRead?> = _read

    /** unread count for this channel, calculated based on read state (this works even if you're offline)*/
    public val unreadCount: StateFlow<Int?> = _unreadCount

    /** the list of members of this channel */
    public val members: StateFlow<List<Member>> = _members
        .map { it.values.sortedBy(Member::createdAt) }
        .stateIn(domainImpl.scope, SharingStarted.Eagerly, emptyList())

    /** StateFlow object with the channel data */
    public val channelData: StateFlow<ChannelData> = _channelData.filterNotNull()
        .stateIn(domainImpl.scope, SharingStarted.Eagerly, ChannelData(type = channelType, channelId = channelId))

    /** if the channel is currently hidden */
    public val hidden: StateFlow<Boolean> = _hidden

    /** if the channel is currently muted */
    public val muted: StateFlow<Boolean> = _muted

    /** if we are currently loading */
    public val loading: StateFlow<Boolean> = _loading

    /** if we are currently loading older messages */
    public val loadingOlderMessages: StateFlow<Boolean> = _loadingOlderMessages

    /** if we are currently loading newer messages */
    public val loadingNewerMessages: StateFlow<Boolean> = _loadingNewerMessages

    /** set to true if there are no more older messages to load */
    public val endOfOlderMessages: StateFlow<Boolean> = _endOfOlderMessages

    /** set to true if there are no more newer messages to load */
    public val endOfNewerMessages: StateFlow<Boolean> = _endOfNewerMessages

    public var recoveryNeeded: Boolean = false
        private set
    private var lastMarkReadEvent: Date? = null
    private var lastKeystrokeAt: Date? = null
    private var lastStartTypingEvent: Date? = null
    private val channelClient = client.channel(channelType, channelId)
    public val cid: String = "%s:%s".format(channelType, channelId)

    private var keystrokeParentMessageId: String? = null

    private val logger = ChatLogger.get("ChatDomain ChannelController")

    private val threadControllerMap: ConcurrentHashMap<String, ThreadController> = ConcurrentHashMap()

    private val messageSendingService: MessageSendingService =
        messageSendingServiceFactory.create(domainImpl, this, client.channel(cid))

    internal fun getThread(threadId: String): ThreadController = threadControllerMap.getOrPut(threadId) {
        ThreadController(threadId, this, domainImpl)
            .also { domainImpl.scope.launch { it.loadOlderMessages() } }
    }

    private fun getConfig(): Config {
        return domainImpl.getChannelConfig(channelType)
    }

    internal suspend fun keystroke(parentId: String?): Result<Boolean> {
        if (!getConfig().isTypingEvents) return Result(false)
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
        if (!getConfig().isTypingEvents) return Result(false)
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
        if (!getConfig().isReadEvents) {
            return false
        }

        // throttle the mark read
        val messages = sortedMessages()

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
                updateRead(ChannelUserRead(domainImpl.currentUser, lastMarkReadEvent))

                shouldUpdate
            }
    }

    private fun sortedMessages(): List<Message> {
        // sorted ascending order, so the oldest messages are at the beginning of the list
        var messages = emptyList<Message>()
        _messages.value.let { mapOfMessages ->
            messages = mapOfMessages.values
                .sortedBy { it.createdAt ?: it.createdLocallyAt }
                .filter { hideMessagesBefore == null || it.wasCreatedAfter(hideMessagesBefore) }
        }
        return messages
    }

    private fun removeMessagesBefore(date: Date) {
        _messages.value = _messages.value.filter { it.value.wasCreatedAfter(date) }
    }

    internal suspend fun hide(clearHistory: Boolean): Result<Unit> {
        setHidden(true)
        val result = channelClient.hide(clearHistory).await()
        if (result.isSuccess) {
            if (clearHistory) {
                val now = Date()
                hideMessagesBefore = now
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
        setHidden(false)
        val result = channelClient.show().await()
        if (result.isSuccess) {
            domainImpl.repos.setHiddenForChannel(cid, false)
        }
        return result
    }

    internal suspend fun leave(): Result<Unit> {
        val result = channelClient.removeMembers(domainImpl.currentUser.id).await()

        return if (result.isSuccess) {
            // Remove from query controllers
            for (activeQuery in domainImpl.getActiveQueries()) {
                activeQuery.removeChannel(cid)
            }
            Result(Unit)
        } else {
            Result(result.error())
        }
    }

    internal suspend fun delete(): Result<Unit> {
        val result = channelClient.delete().await()

        return if (result.isSuccess) {
            // Remove from query controllers
            for (activeQuery in domainImpl.getActiveQueries()) {
                activeQuery.removeChannel(cid)
            }
            // Remove messages from repository
            val now = Date()
            domainImpl.repos.deleteChannelMessagesBefore(cid, now)
            Result(Unit)
        } else {
            Result(result.error())
        }
    }

    internal suspend fun watch(limit: Int = 30) {
        // Otherwise it's too easy for devs to create UI bugs which DDOS our API
        if (_loading.value) {
            logger.logI("Another request to watch this channel is in progress. Ignoring this request.")
            return
        }
        runChannelQuery(QueryChannelPaginationRequest(limit))
    }

    private fun getLoadMoreBaseMessageId(direction: Pagination): String? {
        val messages = sortedMessages()
        return if (messages.isNotEmpty()) {
            when (direction) {
                Pagination.GREATER_THAN_OR_EQUAL,
                Pagination.GREATER_THAN,
                -> {
                    messages.last().id
                }
                Pagination.LESS_THAN,
                Pagination.LESS_THAN_OR_EQUAL,
                -> {
                    messages.first().id
                }
            }
        } else {
            null
        }
    }

    /**
     *  Loads a list of messages before the oldest message in the current list.
     */
    internal suspend fun loadOlderMessages(limit: Int = 30): Result<Channel> {
        return runChannelQuery(
            QueryChannelPaginationRequest(limit).apply {
                getLoadMoreBaseMessageId(Pagination.LESS_THAN)?.let {
                    messageFilterDirection = Pagination.LESS_THAN
                    messageFilterValue = it
                }
            }
        )
    }

    /**
     *  Loads a list of messages after the newest message in the current list.
     */
    internal suspend fun loadNewerMessages(limit: Int = 30): Result<Channel> {
        return runChannelQuery(
            QueryChannelPaginationRequest(limit).apply {
                getLoadMoreBaseMessageId(Pagination.GREATER_THAN)?.let {
                    messageFilterDirection = Pagination.GREATER_THAN
                    messageFilterValue = it
                }
            }
        )
    }

    /**
     *  Loads a list of messages before the message with particular message id.
     */
    internal suspend fun loadOlderMessages(messageId: String, limit: Int): Result<Channel> {
        return runChannelQuery(
            QueryChannelPaginationRequest(limit).apply {
                messageFilterDirection = Pagination.LESS_THAN
                messageFilterValue = messageId
            }
        )
    }

    /**
     *  Loads a list of messages after the message with particular message id.
     */
    internal suspend fun loadNewerMessages(messageId: String, limit: Int): Result<Channel> {
        return runChannelQuery(
            QueryChannelPaginationRequest(limit).apply {
                messageFilterDirection = Pagination.GREATER_THAN
                messageFilterValue = messageId
            }
        )
    }

    internal suspend fun runChannelQuery(pagination: QueryChannelPaginationRequest): Result<Channel> {
        val loader = when (pagination.messageFilterDirection) {
            Pagination.GREATER_THAN,
            Pagination.GREATER_THAN_OR_EQUAL,
            -> _loadingNewerMessages
            Pagination.LESS_THAN,
            Pagination.LESS_THAN_OR_EQUAL,
            -> _loadingOlderMessages
            null -> _loading
        }
        if (loader.value) {
            logger.logI("Another request to load messages is in progress. Ignoring this request.")
            return Result(
                ChatError("Another request to load messages is in progress. Ignoring this request.")
            )
        }
        loader.value = true
        // first we load the data from room and update the messages and channel flow
        val queryOfflineJob = domainImpl.scope.async { runChannelQueryOffline(pagination) }

        // start the online query before queryOfflineJob.await
        val queryOnlineJob = domainImpl.scope.async { runChannelQueryOnline(pagination) }
        val localChannel = queryOfflineJob.await()?.also { channel ->
            if (pagination.messageFilterDirection == Pagination.LESS_THAN) {
                updateOldMessagesFromLocalChannel(channel)
            } else {
                updateDataFromLocalChannel(channel)
            }
            loader.value = false
        }

        val result: Result<Channel> = queryOnlineJob.await().let { onlineResult ->
            if (onlineResult.isSuccess) {
                onlineResult.also { updateDataFromChannel(onlineResult.data()) }
            } else {
                if (onlineResult.error().isPermanent()) {
                    logger.logW("Permanent failure calling channel.watch for channel $cid, with error ${onlineResult.error()}")
                } else {
                    logger.logW("Temporary failure calling channel.watch for channel $cid. Marking the channel as needing recovery. Error was ${onlineResult.error()}")
                    recoveryNeeded = true
                }
                localChannel?.let { Result(it) } ?: onlineResult
            }
        }
        loader.value = false
        return result
    }

    private suspend fun runChannelQueryOffline(pagination: QueryChannelPaginationRequest): Channel? =
        domainImpl.selectAndEnrichChannel(cid, pagination)?.also { channel ->
            logger.logI("Loaded channel ${channel.cid} from offline storage with ${channel.messages.size} messages")
        }

    internal suspend fun runChannelQueryOnline(pagination: QueryChannelPaginationRequest): Result<Channel> {
        val request = pagination.toQueryChannelRequest(domainImpl.userPresence)
        val response = channelClient.watch(request).await()

        if (response.isSuccess) {
            recoveryNeeded = false
            val channelResponse = response.data()
            if (pagination.messageLimit > channelResponse.messages.size) {
                if (request.isFilteringNewerMessages()) {
                    _endOfNewerMessages.value = true
                } else {
                    _endOfOlderMessages.value = true
                }
            }
            // first thing here needs to be updating configs otherwise we have a race with receiving events
            domainImpl.repos.insertChannelConfig(ChannelConfig(channelResponse.type, channelResponse.config))

            domainImpl.storeStateForChannel(channelResponse)
        } else {
            recoveryNeeded = true
            domainImpl.addError(response.error())
        }
        return response
    }

    /**
     * - Generate an ID
     * - Insert the message into offline storage with sync status set to Sync Needed
     * - If we're online do the send message request
     * - If the request fails we retry according to the retry policy set on the repo
     */
    @Deprecated(
        message = "Don't use sendMessage with attachmentTransformer. It's better to implement custom attachment uploading mechanism for additional transformation",
        replaceWith = ReplaceWith("sendMessage(message: Message)")
    )
    internal suspend fun sendMessage(
        message: Message,
        attachmentTransformer: ((at: Attachment, file: File) -> Attachment)? = null,
    ): Result<Message> {
        return if (attachmentTransformer != null) {
            sendMessage(message, attachmentTransformer)
        } else {
            sendMessage(message)
        }
    }

    internal suspend fun sendMessage(message: Message): Result<Message> = messageSendingService.sendNewMessage(message)

    internal suspend fun retrySendMessage(message: Message): Result<Message> =
        messageSendingService.sendMessage(message)

    @Deprecated(
        message = "Don't use sendMessage with attachmentTransformer. It's better to implement custom attachment uploading mechanism for additional transformation",
        replaceWith = ReplaceWith("sendMessage(message: Message)")
    )
    private suspend fun sendMessage(
        message: Message,
        attachmentTransformer: ((at: Attachment, file: File) -> Attachment),
    ): Result<Message> {
        val online = domainImpl.isOnline()
        val newMessage = message.copy()
        val hasAttachments = newMessage.attachments.isNotEmpty()

        // set defaults for id, cid and created at
        if (newMessage.id.isEmpty()) {
            newMessage.id = domainImpl.generateMessageId()
        }
        if (newMessage.cid.isEmpty()) {
            newMessage.enrichWithCid(cid)
        }

        newMessage.user = domainImpl.currentUser

        newMessage.attachments.forEach { attachment ->
            attachment.uploadId = generateUploadId()
            attachment.uploadState = Attachment.UploadState.InProgress
        }

        newMessage.type = getMessageType(message)
        newMessage.createdLocallyAt = newMessage.createdAt ?: newMessage.createdLocallyAt ?: Date()
        newMessage.syncStatus = if (online) SyncStatus.IN_PROGRESS else SyncStatus.SYNC_NEEDED

        var uploadStatusMessage: Message? = null

        if (hasAttachments) {
            uploadStatusMessage = newMessage
        }

        // Update flow in channel controller
        upsertMessage(newMessage)
        // TODO: an event broadcasting feature for LOCAL/offline events on the LLC would be a cleaner approach
        // Update flow for currently running queries
        for (query in domainImpl.getActiveQueries()) {
            query.refreshChannel(cid)
        }

        // we insert early to ensure we don't lose messages
        domainImpl.repos.insertMessage(newMessage)
        domainImpl.repos.updateLastMessageForChannel(newMessage.cid, newMessage)

        return if (online) {
            // upload attachments
            if (hasAttachments) {
                logger.logI("Uploading attachments for message with id ${newMessage.id} and text ${newMessage.text}")

                newMessage.attachments = uploadAttachments(newMessage, attachmentTransformer).toMutableList()

                uploadStatusMessage?.let { cancelEphemeralMessage(it) }
            }

            newMessage.type = "regular"

            logger.logI("Starting to send message with id ${newMessage.id} and text ${newMessage.text}")
            domainImpl.runAndRetry { channelClient.sendMessage(newMessage) }
                .mapSuspend(::handleSendMessageSuccess)
                .recoverSuspend { handleSendMessageFail(newMessage, it) }
        } else {
            logger.logI("Chat is offline, postponing send message with id ${newMessage.id} and text ${newMessage.text}")
            Result(newMessage)
        }
    }

    internal suspend fun uploadAttachments(
        message: Message,
        attachmentTransformer: ((at: Attachment, file: File) -> Attachment)? = null,
    ): List<Attachment> {
        return try {
            message.attachments.filter { it.uploadState != Attachment.UploadState.Success }
                .map { attachment ->
                    AttachmentUploader(client)
                        .uploadAttachment(channelType, channelId, attachment, attachmentTransformer)
                        .recover { error -> attachment.apply { uploadState = Attachment.UploadState.Failed(error) } }
                        .data()
                }.toMutableList()
        } catch (e: Exception) {
            message.attachments.map {
                if (it.uploadState != Attachment.UploadState.Success) {
                    it.uploadState = Attachment.UploadState.Failed(ChatError(e.message, e))
                }
                it
            }.toMutableList()
        }.also {
            message.attachments = it

            // RepositoryFacade::insertMessage is implemented as upsert, therefore we need to delete the message first
            domainImpl.repos.deleteChannelMessage(message)
            domainImpl.repos.insertMessage(message)
        }
    }

    internal suspend fun handleSendMessageSuccess(processedMessage: Message): Message {
        return processedMessage.apply { enrichWithCid(this.cid) }
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
        removeLocalMessage(message)
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

    private suspend fun sendFile(file: File, callback: ProgressCallback): Result<String> {
        return client.sendFile(channelType, channelId, file, callback).await()
    }

    /**
     * sendReaction posts the reaction on local storage
     * message reaction count should increase, latest reactions and own_reactions should be updated
     *
     * If you're online we make the API call to sync to the server
     * If the request fails we retry according to the retry policy set on the repo
     */
    internal suspend fun sendReaction(reaction: Reaction, enforceUnique: Boolean): Result<Reaction> {
        val currentUser = domainImpl.currentUser
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
        domainImpl.repos.insertReaction(reaction)
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
        val online = domainImpl.isOnline()
        val currentUser = domainImpl.currentUser
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

    private fun setWatcherCount(watcherCount: Int) {
        if (watcherCount != this._watcherCount.value) {
            this._watcherCount.value = watcherCount
        }
    }

    // This one needs to be public for flows such as running a message action

    internal fun upsertMessage(message: Message) {
        upsertMessages(listOf(message))
    }

    private fun upsertEventMessage(message: Message) {
        // make sure we don't lose ownReactions
        getMessage(message.id)?.let {
            message.ownReactions = it.ownReactions
        }
        upsertMessages(listOf(message))
    }

    public fun getMessage(messageId: String): Message? {
        val copy = _messages.value
        var message = copy[messageId]

        if (hideMessagesBefore != null) {
            if (message != null && message.wasCreatedBeforeOrAt(hideMessagesBefore)) {
                message = null
            }
        }

        return message
    }

    private fun parseMessages(messages: List<Message>): Map<String, Message> {
        val currentMessages = _messages.value
        return currentMessages + attachmentUrlValidator.updateValidAttachmentsUrl(messages, currentMessages)
            .filter { newMessage -> isMessageNewerThanCurrent(currentMessages[newMessage.id], newMessage) }
            .associateBy(Message::id)
    }

    private fun isMessageNewerThanCurrent(currentMessage: Message?, newMessage: Message): Boolean {
        return if (newMessage.syncStatus == SyncStatus.COMPLETED) {
            currentMessage?.lastUpdateTime() ?: NEVER.time <= newMessage.lastUpdateTime()
        } else {
            currentMessage?.lastLocalUpdateTime() ?: NEVER.time <= newMessage.lastLocalUpdateTime()
        }
    }

    internal fun upsertMessages(messages: List<Message>) {
        val newMessages = parseMessages(messages)
        updateLastMessageAtByNewMessages(newMessages.values)
        _messages.value = newMessages
    }

    private fun updateLastMessageAtByNewMessages(newMessages: Collection<Message>) {
        if (newMessages.isEmpty()) {
            return
        }
        val newLastMessageAt =
            newMessages.mapNotNull { it.createdAt ?: it.createdLocallyAt }.maxOfOrNull(Date::getTime) ?: return
        lastMessageAt.value = when (val currentLastMessageAt = lastMessageAt.value) {
            null -> Date(newLastMessageAt)
            else -> max(currentLastMessageAt.time, newLastMessageAt).let(::Date)
        }
    }

    private fun upsertOldMessages(messages: List<Message>) {
        _oldMessages.value = parseMessages(messages)
    }

    private fun removeLocalMessage(message: Message) {
        _messages.value = _messages.value - message.id
    }

    public fun clean() {
        domainImpl.scope.launch {
            // cleanup your own typing state
            val now = Date()
            if (lastStartTypingEvent != null && now.time - lastStartTypingEvent!!.time > 5000) {
                stopTyping(keystrokeParentMessageId)
            }

            // Cleanup typing events that are older than 15 seconds
            var copy = _typing.value
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
                _typing.value = copy
            }
        }
    }

    internal fun setTyping(userId: String, event: ChatEvent?) {
        val copy = _typing.value.toMutableMap()
        if (event == null) {
            copy.remove(userId)
        } else {
            copy[userId] = event
        }
        copy.remove(domainImpl.currentUser.id)
        _typing.value = copy.toMap()
    }

    private fun setHidden(hidden: Boolean) {
        _hidden.value = hidden
    }

    internal suspend fun handleEvents(events: List<ChatEvent>) {
        for (event in events) {
            handleEvent(event)
        }
    }

    internal fun isHidden(): Boolean {
        return _hidden.value
    }

    internal fun handleEvent(event: ChatEvent) {
        when (event) {
            is NewMessageEvent -> {
                upsertEventMessage(event.message)
                incrementUnreadCountIfNecessary(event.message)
                setHidden(false)
            }
            is MessageUpdatedEvent -> {
                event.message.apply {
                    replyTo = _messages.value[replyMessageId]
                }.let(::upsertEventMessage)

                setHidden(false)
            }
            is MessageDeletedEvent -> {
                upsertEventMessage(event.message)
                setHidden(false)
            }
            is NotificationMessageNewEvent -> {
                upsertEventMessage(event.message)
                incrementUnreadCountIfNecessary(event.message)
                setHidden(false)
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
                setWatcherCount(event.watcherCount)
            }
            is UserStopWatchingEvent -> {
                deleteWatcher(event.user)
                setWatcherCount(event.watcherCount)
            }
            is ChannelUpdatedEvent -> {
                updateChannelData(event.channel)
            }
            is ChannelUpdatedByUserEvent -> {
                updateChannelData(event.channel)
            }
            is ChannelHiddenEvent -> {
                setHidden(true)
            }
            is ChannelVisibleEvent -> {
                setHidden(false)
            }
            is ChannelDeletedEvent -> {
                removeMessagesBefore(event.createdAt)
                val channelData = _channelData.value
                channelData?.let {
                    it.deletedAt = event.createdAt
                    _channelData.value = it
                }
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
                updateChannelData(event.channel)
            }
            is NotificationInviteRejectedEvent -> {
                upsertMember(event.member)
                updateChannelData(event.channel)
            }
        }
    }

    private fun upsertUserPresence(user: User) {
        val userId = user.id
        // members and watchers have users
        val members = _members.value
        val watchers = _watchers.value
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
        val channelData = _channelData.value
        if (channelData != null) {
            if (channelData.createdBy.id == userId) {
                channelData.createdBy = user
            }
        }

        // updating messages is harder
        // user updates don't happen frequently, it's probably ok for this update to be sluggish
        // if it turns out to be slow we can do a simple reverse index from user -> message
        val messages = _messages.value
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
            upsertMessages(changedMessages)
        }
    }

    private fun deleteWatcher(user: User) {
        _watchers.value = _watchers.value - user.id
    }

    private fun upsertWatcher(user: User) {
        _watchers.value = _watchers.value + mapOf(user.id to user)
    }

    private fun deleteMember(userId: String) {
        _members.value = _members.value - userId
    }

    internal fun upsertMembers(members: List<Member>) {
        _members.value = _members.value + members.associateBy { it.user.id }
    }

    internal suspend fun removeMembers(vararg userIds: String): Result<Channel> {
        return channelClient.removeMembers(*userIds).await()
    }

    internal fun upsertMember(member: Member) {
        upsertMembers(listOf(member))
    }

    private fun updateReads(reads: List<ChannelUserRead>) {
        val currentUserId = domainImpl.currentUser.id
        val previousUserIdToReadMap = _reads.value
        val incomingUserIdToReadMap = reads.associateBy(ChannelUserRead::getUserId).toMutableMap()

        /**
         * It's possible that the data coming back from the online channel query has a last read date that's
         * before what we've last pushed to the UI. We want to ignore this, as it will cause an unread state
         * to show in the channel list.
         */
        incomingUserIdToReadMap[currentUserId]?.let { incomingUserRead ->

            // the previous last Read date that is most current
            val previousLastRead = _read.value?.lastRead ?: previousUserIdToReadMap[currentUserId]?.lastRead

            // Use AFTER to determine if the incoming read is more current.
            // This prevents updates if it's BEFORE or EQUAL TO the previous Read.
            val shouldUpdateByIncoming = previousLastRead == null || incomingUserRead.lastRead?.inOffsetWith(
                previousLastRead,
                OFFSET_EVENT_TIME
            ) == true

            if (shouldUpdateByIncoming) {
                _read.value = incomingUserRead
                _unreadCount.value = incomingUserRead.unreadMessages
            } else {
                // if the previous Read was more current, replace the item in the update map
                incomingUserIdToReadMap[currentUserId] = ChannelUserRead(domainImpl.currentUser, previousLastRead)
            }
        }

        // always post the newly updated map
        _reads.value = (previousUserIdToReadMap + incomingUserIdToReadMap)
    }

    private fun updateRead(
        read: ChannelUserRead,
    ) {
        updateReads(listOf(read))
    }

    private fun updateDataFromLocalChannel(localChannel: Channel) {
        localChannel.hidden?.let(::setHidden)
        hideMessagesBefore = localChannel.hiddenMessagesBefore
        updateDataFromChannel(localChannel)
    }

    private fun updateOldMessagesFromLocalChannel(localChannel: Channel) {
        localChannel.hidden?.let(::setHidden)
        hideMessagesBefore = localChannel.hiddenMessagesBefore
        updateOldMessagesFromChannel(localChannel)
    }

    internal fun updateDataFromChannel(c: Channel) {
        // Update all the flow objects based on the channel
        updateChannelData(c)
        setWatcherCount(c.watcherCount)
        updateReads(c.read)

        // there are some edge cases here, this code adds to the members, watchers and messages
        // this means that if the offline sync went out of sync things go wrong
        setMembers(c.members)
        setWatchers(c.watchers)
        upsertMessages(c.messages)
        lastMessageAt.value = c.lastMessageAt
    }

    private fun updateOldMessagesFromChannel(c: Channel) {
        // Update all the flow objects based on the channel
        updateChannelData(c)
        setWatcherCount(c.watcherCount)
        updateReads(c.read)

        // there are some edge cases here, this code adds to the members, watchers and messages
        // this means that if the offline sync went out of sync things go wrong
        setMembers(c.members)
        setWatchers(c.watchers)
        upsertOldMessages(c.messages)
    }

    private fun setMembers(members: List<Member>) {
        _members.value = (_members.value + members.associateBy(Member::getUserId))
    }

    private fun updateChannelData(channel: Channel) {
        _channelData.value = (ChannelData(channel))
    }

    private fun setWatchers(watchers: List<User>) {
        this._watchers.value = (this._watchers.value + watchers.associateBy { it.id })
    }

    private fun incrementUnreadCountIfNecessary(message: Message) {
        val currentUserId = domainImpl.currentUser.id
        if (message.shouldIncrementUnreadCount(currentUserId)) {
            val newUnreadCount = _unreadCount.value + 1
            _unreadCount.value = newUnreadCount
            _read.value = _read.value?.copy(unreadMessages = newUnreadCount)
            _reads.value = _reads.value.apply {
                this[currentUserId]?.unreadMessages = newUnreadCount
            }
        }
    }

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

    internal suspend fun deleteMessage(message: Message): Result<Message> {
        val online = domainImpl.isOnline()
        val messageToBeDeleted = message.copy(deletedAt = Date())
        messageToBeDeleted.syncStatus = if (!online) SyncStatus.SYNC_NEEDED else SyncStatus.IN_PROGRESS

        // Update flow
        upsertMessage(messageToBeDeleted)

        // Update Room State
        domainImpl.repos.insertMessage(messageToBeDeleted)

        if (online) {
            val runnable = {
                client.deleteMessage(messageToBeDeleted.id)
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
        val channelData = _channelData.value ?: ChannelData(channelType, channelId)

        val messages = sortedMessages()
        val members = _members.value.values.toList()
        val watchers = _watchers.value.values.toList()
        val reads = _reads.value.values.toList()
        val watcherCount = _watcherCount.value

        val channel = channelData.toChannel(messages, members, reads, watchers, watcherCount)
        channel.config = getConfig()
        channel.unreadCount = _unreadCount.value
        channel.lastMessageAt =
            lastMessageAt.value ?: messages.lastOrNull()?.let { it.createdAt ?: it.createdLocallyAt }
        channel.hidden = _hidden.value

        return channel
    }

    internal suspend fun loadOlderThreadMessages(
        threadId: String,
        limit: Int,
        firstMessage: Message? = null,
    ): Result<List<Message>> {
        val result = if (firstMessage != null) {
            client.getRepliesMore(threadId, firstMessage.id, limit).await()
        } else {
            client.getReplies(threadId, limit).await()
        }
        if (result.isSuccess) {
            val newMessages = result.data()
            upsertMessages(newMessages)
            // Note that we don't handle offline storage for threads at the moment.
        }
        return result
    }

    internal suspend fun loadMessageById(
        messageId: String,
        newerMessagesOffset: Int,
        olderMessagesOffset: Int,
    ): Result<Message> {
        val result = client.getMessage(messageId).await()
        if (result.isError) {
            return Result(ChatError("Error while fetching message from backend. Message id: $messageId"))
        }
        val message = result.data()
        upsertMessage(message)
        loadOlderMessages(messageId, newerMessagesOffset)
        loadNewerMessages(messageId, olderMessagesOffset)
        return Result(message)
    }

    internal fun replyMessage(repliedMessage: Message?) {
        _repliedMessage.value = repliedMessage
    }

    internal fun cancelJobs() = messageSendingService.cancelJobs()

    private fun Message.lastUpdateTime(): Long = listOfNotNull(
        createdAt,
        updatedAt,
        deletedAt,
    ).map { it.time }
        .maxOrNull()
        ?: NEVER.time

    private fun Message.lastLocalUpdateTime(): Long = listOfNotNull(
        createdLocallyAt,
        updatedLocallyAt,
        deletedAt,
    ).map { it.time }
        .maxOrNull()
        ?: NEVER.time

    public sealed class MessagesState {
        /** The ChannelController is initialized but no query is currently running.
         * If you know that a query will be started you typically want to display a loading icon.
         * */
        public object NoQueryActive : MessagesState()

        /** Indicates we are loading the first page of results.
         * We are in this state if ChannelController.loading is true
         * For seeing if we're loading more results have a look at loadingNewerMessages and loadingOlderMessages
         *
         * @see loading
         * @see loadingNewerMessages
         * @see loadingOlderMessages
         * */
        public object Loading : MessagesState()

        /** If we are offline and don't have channels stored in offline storage, typically displayed as an error condition. */
        public object OfflineNoResults : MessagesState()

        /** The list of messages, loaded either from offline storage or an API call.
         * Observe chatDomain.online to know if results are currently up to date
         * @see ChatDomainImpl.online
         * */
        public data class Result(val messages: List<Message>) : MessagesState()
    }

    internal companion object {
        private const val KEY_MESSAGE_ACTION = "image_action"
        private const val MESSAGE_ACTION_SHUFFLE = "shuffle"
        private const val MESSAGE_ACTION_SEND = "send"
        private const val TYPE_IMAGE = "image"
        private const val TYPE_VIDEO = "video"
        private const val TYPE_FILE = "file"
        private const val OFFSET_EVENT_TIME = 5L
    }
}
