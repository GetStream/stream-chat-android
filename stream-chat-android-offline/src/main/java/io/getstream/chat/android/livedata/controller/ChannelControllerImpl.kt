package io.getstream.chat.android.livedata.controller

import NEVER
import android.webkit.MimeTypeMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.api.models.SendActionRequest
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.ChannelHiddenEvent
import io.getstream.chat.android.client.events.ChannelTruncatedEvent
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
import io.getstream.chat.android.client.events.NotificationMarkReadEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.ReactionDeletedEvent
import io.getstream.chat.android.client.events.ReactionNewEvent
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.TypingStopEvent
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.events.UserUpdatedEvent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.ChannelData
import io.getstream.chat.android.livedata.ChannelPreview
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.controller.helper.MessageHelper
import io.getstream.chat.android.livedata.entity.ChannelConfigEntity
import io.getstream.chat.android.livedata.entity.ReactionEntity
import io.getstream.chat.android.livedata.extensions.addReaction
import io.getstream.chat.android.livedata.extensions.isImageMimetype
import io.getstream.chat.android.livedata.extensions.isPermanent
import io.getstream.chat.android.livedata.extensions.removeReaction
import io.getstream.chat.android.livedata.repository.MessageRepository
import io.getstream.chat.android.livedata.request.QueryChannelPaginationRequest
import io.getstream.chat.android.livedata.utils.computeUnreadCount
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
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import wasCreatedAfter
import wasCreatedBeforeOrAt
import java.io.File
import java.util.Calendar
import java.util.Date
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set

private const val KEY_MESSAGE_ACTION = "image_action"
private const val MESSAGE_ACTION_SHUFFLE = "shuffle"
private const val MESSAGE_ACTION_SEND = "send"

internal class ChannelControllerImpl(
    override val channelType: String,
    override val channelId: String,
    val client: ChatClient,
    val domainImpl: ChatDomainImpl
) :
    ChannelController {
    private val editJobs = mutableMapOf<String, Job>()

    private val _messages = MutableStateFlow<Map<String, Message>>(emptyMap())
    private val _watcherCount = MutableStateFlow<Int>(0)
    private val _typing = MutableStateFlow<Map<String, ChatEvent>>(emptyMap())
    private val _reads = MutableStateFlow<Map<String, ChannelUserRead>>(emptyMap())
    private val _read = MutableStateFlow<ChannelUserRead?>(null)
    private val _endOfNewerMessages = MutableStateFlow<Boolean>(false)
    private val _endOfOlderMessages = MutableStateFlow<Boolean>(false)
    private val _loading = MutableStateFlow<Boolean>(false)
    private val _hidden = MutableStateFlow<Boolean>(false)
    private val _muted = MutableStateFlow<Boolean>(false)
    private val _watchers = MutableStateFlow<Map<String, User>>(emptyMap())
    private val _members = MutableStateFlow<Map<String, Member>>(emptyMap())
    private val _loadingOlderMessages = MutableStateFlow<Boolean>(false)
    private val _loadingNewerMessages = MutableStateFlow<Boolean>(false)
    private val _channelData = MutableStateFlow<ChannelData?>(null)
    private val _oldMessages = MutableStateFlow<Map<String, Message>>(emptyMap())
    private val lastMessageAt = MutableStateFlow<Date?>(null)

    internal var hideMessagesBefore: Date? = null
    val unfilteredMessages = _messages.map { it.values.toList() }

    /** a list of messages sorted by message.createdAt */
    override val messages: LiveData<List<Message>> = messagesTransformation(_messages)

    override val oldMessages: LiveData<List<Message>> = messagesTransformation(_oldMessages)

    private fun messagesTransformation(messages: MutableStateFlow<Map<String, Message>>): LiveData<List<Message>> {
        return messages.map { messageMap ->
            messageMap.values
                .asSequence()
                .filter { it.parentId == null || it.showInChannel }
                .filter { hideMessagesBefore == null || it.wasCreatedAfter(hideMessagesBefore) }
                .sortedBy { it.createdAt ?: it.createdLocallyAt }
                .toList()
        }.asLiveData()
    }

    /** the number of people currently watching the channel */
    override val watcherCount: LiveData<Int> = _watcherCount.asLiveData()

    /** the list of users currently watching this channel */
    override val watchers: LiveData<List<User>> = _watchers
        .map { it.values.sortedBy { user -> user.createdAt } }
        .asLiveData()

    val _typingUsers: StateFlow<List<User>> = _typing.map {
        it.values
            .sortedBy(ChatEvent::createdAt)
            .mapNotNull { event ->
                (event as? TypingStartEvent)?.user ?: (event as? TypingStopEvent)?.user
            }
    }.stateIn(domainImpl.scope, SharingStarted.Eagerly, emptyList())

    /** who is currently typing (current user is excluded from this) */
    override val typing: LiveData<List<User>> = _typingUsers.asLiveData()

    /** how far every user in this channel has read */
    override val reads: LiveData<List<ChannelUserRead>> = _reads
        .map { it.values.sortedBy(ChannelUserRead::lastRead) }
        .asLiveData()

    /** read status for the current user */
    override val read: LiveData<ChannelUserRead> = _read.filterNotNull().asLiveData()

    val _unreadCount = _read.combine(_messages) {
        channelUserRead: ChannelUserRead?, messagesMap: Map<String, Message> ->
        val messages = messagesMap.values.toList()
        if (messages.size <= 1 && channelUserRead != null) {
            channelUserRead.unreadMessages
        } else {
            computeUnreadCount(domainImpl.currentUser, channelUserRead, messages)
        }
    }.stateIn(domainImpl.scope, SharingStarted.Eagerly, 0)

    /**
     * unread count for this channel, calculated based on read state (this works even if you're offline)
     */
    override val unreadCount: LiveData<Int?> = _unreadCount.asLiveData()

    /** the list of members of this channel */
    override val members: LiveData<List<Member>> = _members
        .map { it.values.sortedBy(Member::createdAt) }
        .asLiveData()

    /** LiveData object with the channel data */
    override val channelData: LiveData<ChannelData> = _channelData.filterNotNull().asLiveData()

    /** if the channel is currently hidden */
    override val hidden: LiveData<Boolean> = _hidden.asLiveData()

    /** if the channel is currently muted */
    override val muted: LiveData<Boolean> = _muted.asLiveData()

    /** if we are currently loading */
    override val loading: LiveData<Boolean> = _loading.asLiveData()

    /** if we are currently loading older messages */
    override val loadingOlderMessages: LiveData<Boolean> = _loadingOlderMessages.asLiveData()

    /** if we are currently loading newer messages */
    override val loadingNewerMessages: LiveData<Boolean> = _loadingNewerMessages.asLiveData()

    /** set to true if there are no more older messages to load */
    override val endOfOlderMessages: LiveData<Boolean> = _endOfOlderMessages.asLiveData()

    /** set to true if there are no more newer messages to load */
    override val endOfNewerMessages: LiveData<Boolean> = _endOfNewerMessages.asLiveData()

    override var recoveryNeeded: Boolean = false
    private var lastMarkReadEvent: Date? = null
    private var lastKeystrokeAt: Date? = null
    private var lastStartTypingEvent: Date? = null
    private val channelClient = client.channel(channelType, channelId)
    override val cid = "%s:%s".format(channelType, channelId)

    private val logger = ChatLogger.get("ChatDomain ChannelController")

    private val threadControllerMap: ConcurrentHashMap<String, ThreadControllerImpl> =
        ConcurrentHashMap()
    private val messageHelper = MessageHelper()

    fun getThread(threadId: String): ThreadControllerImpl = threadControllerMap.getOrPut(threadId) {
        ThreadControllerImpl(threadId, this, client, domainImpl)
            .also { domainImpl.scope.launch { it.loadOlderMessages() } }
    }

    private fun getConfig(): Config {
        return domainImpl.getChannelConfig(channelType)
    }

    fun keystroke(): Result<Boolean> {
        if (!getConfig().isTypingEvents) return Result(false, null)
        lastKeystrokeAt = Date()
        if (lastStartTypingEvent == null || lastKeystrokeAt!!.time - lastStartTypingEvent!!.time > 3000) {
            lastStartTypingEvent = lastKeystrokeAt
            val result = client.sendEvent(EventType.TYPING_START, channelType, channelId).execute()
            return if (result.isSuccess) {
                Result(result.isSuccess, null)
            } else {
                Result(result.isSuccess, null)
            }
        }
        return Result(false, null)
    }

    fun stopTyping(): Result<Boolean> {
        if (!getConfig().isTypingEvents) return Result(false, null)
        if (lastStartTypingEvent != null) {
            lastStartTypingEvent = null
            lastKeystrokeAt = null
            val result = client.sendEvent(EventType.TYPING_STOP, channelType, channelId).execute()
            return if (result.isSuccess) {
                Result(result.isSuccess, null)
            } else {
                Result(null, result.error())
            }
        }
        return Result(false, null)
    }

    /**
     * Marks the channel as read by the current user
     *
     * @return whether the channel was marked as read or not
     */
    internal suspend fun markRead(): Boolean {
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

    private suspend fun removeMessagesBefore(date: Date) {
        val copy = _messages.value
        // start off empty
        _messages.value = mutableMapOf()
        // call upsert with the messages that are recent
        val recentMessages = copy.values.filter { it.wasCreatedAfter(date) }
        upsertMessages(recentMessages)
    }

    suspend fun hide(clearHistory: Boolean): Result<Unit> {
        setHidden(true)
        val result = channelClient.hide(clearHistory).execute()
        if (result.isSuccess) {
            val channelEntity = domainImpl.repos.channels.select(cid)
            channelEntity?.let {
                it.hidden = true
                if (clearHistory) {
                    val now = Date()
                    it.hideMessagesBefore = now
                    hideMessagesBefore = now
                    removeMessagesBefore(now)
                    domainImpl.repos.messages.deleteChannelMessagesBefore(cid, now)
                }
                domainImpl.repos.channels.insert(it)
            }
        }
        return result
    }

    suspend fun show(): Result<Unit> {
        setHidden(false)
        val result = channelClient.show().execute()
        if (result.isSuccess) {
            val channelEntity = domainImpl.repos.channels.select(cid)
            channelEntity?.let {
                it.hidden = false
                domainImpl.repos.channels.insert(it)
            }
        }
        return result
    }

    suspend fun watch(limit: Int = 30) {
        // Otherwise it's too easy for devs to create UI bugs which DDOS our API
        if (_loading.value == true) {
            logger.logI("Another request to watch this channel is in progress. Ignoring this request.")
            return
        }
        _loading.value = true
        val pagination = QueryChannelPaginationRequest(limit)
        runChannelQuery(pagination)

        _loading.value = false
    }

    fun loadMoreMessagesRequest(
        limit: Int = 30,
        direction: Pagination
    ): QueryChannelPaginationRequest {
        val messages = sortedMessages()
        val request = QueryChannelPaginationRequest(limit)
        if (messages.isNotEmpty()) {
            val messageId: String = when (direction) {
                Pagination.GREATER_THAN_OR_EQUAL, Pagination.GREATER_THAN -> {
                    messages.last().id
                }
                Pagination.LESS_THAN, Pagination.LESS_THAN_OR_EQUAL -> {
                    messages.first().id
                }
            }
            request.apply {
                messageFilterDirection = direction
                messageFilterValue = messageId
            }
        }

        return request
    }

    suspend fun loadOlderMessages(limit: Int = 30): Result<Channel> {
        if (_loadingOlderMessages.value) {
            logger.logI("Another request to load older messages is in progress. Ignoring this request.")
            return Result(
                null,
                ChatError("Another request to load older messages is in progress. Ignoring this request.")
            )
        }
        _loadingOlderMessages.value = true
        val pagination = loadMoreMessagesRequest(limit, Pagination.LESS_THAN)
        val result = runChannelQuery(pagination)
        _loadingOlderMessages.value = false
        return result
    }

    suspend fun loadNewerMessages(limit: Int = 30): Result<Channel> {
        if (_loadingNewerMessages.value) {
            logger.logI("Another request to load newer messages is in progress. Ignoring this request.")
            return Result(
                null,
                ChatError("Another request to load newer messages is in progress. Ignoring this request.")
            )
        }
        _loadingNewerMessages.value = true
        val pagination = loadMoreMessagesRequest(limit, Pagination.GREATER_THAN)
        val result = runChannelQuery(pagination)
        _loadingNewerMessages.value = false
        return result
    }

    suspend fun runChannelQuery(pagination: QueryChannelPaginationRequest): Result<Channel> {
        // first we load the data from room and update the messages and channel livedata
        val queryOfflineJob = domainImpl.scope.async { runChannelQueryOffline(pagination) }

        // start the online query before queryOfflineJob.await
        val queryOnlineJob = if (domainImpl.isOnline()) {
            domainImpl.scope.async { runChannelQueryOnline(pagination) }
        } else {
            null
        }
        val localChannel = queryOfflineJob.await()
        if (localChannel != null) {
            if (pagination.messageFilterDirection == Pagination.LESS_THAN) {
                updateOldMessagesFromLocalChannel(localChannel)
            } else {
                updateLiveDataFromLocalChannel(localChannel)
            }
        }

        // if we are online we we run the actual API call
        return if (queryOnlineJob != null) {
            val response = queryOnlineJob.await()
            if (response.isSuccess) {
                updateLiveDataFromChannel(response.data())
            }
            response
        } else {
            // if we are not offline we mark it as needing recovery
            recoveryNeeded = true
            Result(localChannel, null)
        }
    }

    suspend fun runChannelQueryOffline(pagination: QueryChannelPaginationRequest): Channel? {
        val selectedChannel = domainImpl.selectAndEnrichChannel(cid, pagination)

        selectedChannel?.also { channel ->
            channel.config = domainImpl.getChannelConfig(channel.type)
            _loading.value = false
            logger.logI("Loaded channel ${channel.cid} from offline storage with ${channel.messages.size} messages")
        }

        return selectedChannel
    }

    suspend fun runChannelQueryOnline(pagination: QueryChannelPaginationRequest): Result<Channel> {
        val request = pagination.toQueryChannelRequest(domainImpl.userPresence)
        val response = channelClient.watch(request).execute()

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
            val configEntities = ChannelConfigEntity(channelResponse.type, channelResponse.config)
            domainImpl.repos.configs.insert(listOf(configEntities))

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

    suspend fun sendMessage(
        message: Message,
        attachmentTransformer: ((at: Attachment, file: File) -> Attachment)? = null
    ): Result<Message> {
        val online = domainImpl.isOnline()
        val newMessage = message.copy()

        // set defaults for id, cid and created at
        if (newMessage.id.isEmpty()) {
            newMessage.id = domainImpl.generateMessageId()
        }
        if (newMessage.cid.isEmpty()) {
            newMessage.cid = cid
        }

        newMessage.user = domainImpl.currentUser
        // TODO: type should be a sealed/class or enum at the client level
        newMessage.type = if (newMessage.text.startsWith("/")) {
            "ephemeral"
        } else {
            "regular"
        }
        newMessage.createdLocallyAt = newMessage.createdAt ?: newMessage.createdLocallyAt ?: Date()
        newMessage.syncStatus = SyncStatus.IN_PROGRESS
        if (!online) {
            newMessage.syncStatus = SyncStatus.SYNC_NEEDED
        }

        // TODO remove usage of MessageEntity
        val messageEntity = MessageRepository.toEntity(newMessage)

        // Update livedata in channel controller
        upsertMessage(newMessage)
        // TODO: an event broadcasting feature for LOCAL/offline events on the LLC would be a cleaner approach
        // Update livedata for currently running queries
        for (query in domainImpl.getActiveQueries()) {
            query.refreshChannel(cid)
        }

        // we insert early to ensure we don't lose messages
        domainImpl.repos.messages.insert(newMessage)

        val channelStateEntity = domainImpl.repos.channels.select(newMessage.cid)
        channelStateEntity?.let {
            // update channel lastMessage at and lastMessageAt
            it.updateLastMessage(messageEntity)
            domainImpl.repos.channels.insert(it)
        }

        return if (online) {
            // upload attachments
            logger.logI("Uploading attachments for message with id ${newMessage.id} and text ${newMessage.text}")
            newMessage.attachments = newMessage.attachments.map {
                var attachment: Attachment = it
                if (it.upload != null) {
                    val result = uploadAttachment(it, attachmentTransformer)
                    if (result.isSuccess) {
                        attachment = result.data()
                    }
                }
                attachment
            }.toMutableList()

            logger.logI("Starting to send message with id ${newMessage.id} and text ${newMessage.text}")

            val result = domainImpl.runAndRetry { channelClient.sendMessage(newMessage) }
            if (result.isSuccess) {
                val processedMessage: Message = result.data()
                processedMessage.apply {
                    syncStatus = SyncStatus.COMPLETED
                    domainImpl.repos.messages.insert(this)
                }

                upsertMessage(processedMessage)
                Result(processedMessage, null)
            } else {

                logger.logE(
                    "Failed to send message with id ${newMessage.id} and text ${newMessage.text}: ${result.error()}",
                    result.error()
                )

                if (result.error().isPermanent()) {
                    newMessage.syncStatus = SyncStatus.FAILED_PERMANENTLY
                } else {
                    newMessage.syncStatus = SyncStatus.SYNC_NEEDED
                }
                upsertMessage(newMessage)
                domainImpl.repos.messages.insert(newMessage)
                Result(newMessage, result.error())
            }
        } else {
            logger.logI("Chat is offline, postponing send message with id ${newMessage.id} and text ${newMessage.text}")
            Result(newMessage, null)
        }
    }

    /**
     * Upload the attachment.upload file for the given attachment
     * Structure of the resulting attachment object can be adjusted using the attachmentTransformer
     */
    internal suspend fun uploadAttachment(
        attachment: Attachment,
        attachmentTransformer: ((at: Attachment, file: File) -> Attachment)? = null
    ): Result<Attachment> {
        val file =
            checkNotNull(attachment.upload) { "upload file shouldn't be called on attachment without a attachment.upload" }
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
        val attachmentType = if (mimeType.isImageMimetype()) {
            TYPE_IMAGE
        } else {
            TYPE_FILE
        }
        val pathResult = if (attachmentType == TYPE_IMAGE) {
            sendImage(file)
        } else {
            sendFile(file)
        }
        val url = if (pathResult.isError) null else pathResult.data()
        val uploadState =
            if (pathResult.isError) Attachment.UploadState.Failed(pathResult.error()) else Attachment.UploadState.Success

        var newAttachment = attachment.copy(
            name = file.name,
            fileSize = file.length().toInt(),
            mimeType = mimeType ?: "",
            url = url,
            uploadState = uploadState,
            type = attachmentType
        ).apply {
            url?.let {
                if (attachmentType == TYPE_IMAGE) {
                    imageUrl = it
                } else {
                    assetUrl = it
                }
            }
        }

        // allow the user to change the format of the attachment
        if (attachmentTransformer != null) {
            newAttachment = attachmentTransformer(newAttachment, file)
        }

        return Result(newAttachment, if (pathResult.isError) pathResult.error() else null)
    }

    /**
     * Cancels ephemeral Message.
     * Removes message from the offline storage and memory and notifies about update.
     */
    suspend fun cancelMessage(message: Message): Result<Boolean> {
        if ("ephemeral" != message.type) {
            throw IllegalArgumentException("Only ephemeral message can be canceled")
        }

        domainImpl.repos.messages.deleteChannelMessage(message)
        removeLocalMessage(message)
        return Result(true)
    }

    suspend fun sendGiphy(message: Message): Result<Message> {
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

    suspend fun shuffleGiphy(message: Message): Result<Message> {
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
                domainImpl.repos.messages.insert(this)
            }
            upsertMessage(processedMessage)
            Result(processedMessage)
        } else {
            Result(result.error())
        }
    }

    fun sendImage(file: File): Result<String> {
        return client.sendImage(channelType, channelId, file).execute()
    }

    fun sendFile(file: File): Result<String> {
        return client.sendFile(channelType, channelId, file).execute()
    }

    /**
     * sendReaction posts the reaction on local storage
     * message reaction count should increase, latest reactions and own_reactions should be updated
     *
     * If you're online we make the API call to sync to the server
     * If the request fails we retry according to the retry policy set on the repo
     */
    suspend fun sendReaction(reaction: Reaction): Result<Reaction> {
        reaction.user = domainImpl.currentUser
        val online = domainImpl.isOnline()
        // insert the message into local storage

        reaction.syncStatus = SyncStatus.IN_PROGRESS
        if (!online) {
            reaction.syncStatus = SyncStatus.SYNC_NEEDED
        }
        domainImpl.repos.reactions.insertReaction(reaction)
        // update livedata
        val currentMessage = getMessage(reaction.messageId)
        currentMessage?.let {
            it.addReaction(reaction, true)
            upsertMessage(it)
            domainImpl.repos.messages.insert(it)
        }

        if (online) {
            val runnable = {
                client.sendReaction(reaction)
            }
            val result = domainImpl.runAndRetry(runnable)
            return if (result.isSuccess) {
                reaction.syncStatus = SyncStatus.COMPLETED
                domainImpl.repos.reactions.insertReaction(reaction)
                Result(result.data(), null)
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
                domainImpl.repos.reactions.insertReaction(reaction)
                Result(null, result.error())
            }
        }
        return Result(reaction, null)
    }

    suspend fun deleteReaction(reaction: Reaction): Result<Message> {
        val online = domainImpl.isOnline()
        reaction.user = domainImpl.currentUser
        reaction.syncStatus = SyncStatus.IN_PROGRESS
        if (!online) {
            reaction.syncStatus = SyncStatus.SYNC_NEEDED
        }

        val reactionEntity = ReactionEntity(reaction)
        reactionEntity.deletedAt = Date()
        domainImpl.repos.reactions.insert(reactionEntity)

        // update livedata
        val currentMessage = getMessage(reaction.messageId)
        currentMessage?.let {
            it.removeReaction(reaction, true)
            upsertMessage(it)
            domainImpl.repos.messages.insert(it)
        }

        if (online) {
            val runnable = {
                client.deleteReaction(reaction.messageId, reaction.type)
            }
            val result = domainImpl.runAndRetry(runnable)
            return if (result.isSuccess) {
                reaction.syncStatus = SyncStatus.COMPLETED
                domainImpl.repos.reactions.insertReaction(reaction)
                Result(result.data(), null)
            } else {
                if (result.error().isPermanent()) {
                    reaction.syncStatus = SyncStatus.FAILED_PERMANENTLY
                } else {
                    reaction.syncStatus = SyncStatus.SYNC_NEEDED
                }
                domainImpl.repos.reactions.insertReaction(reaction)
                Result(null, result.error())
            }
        }
        return Result(currentMessage, null)
    }

    private fun setWatcherCount(watcherCount: Int) {
        if (watcherCount != _watcherCount.value) {
            _watcherCount.value = watcherCount
        }
    }

    // This one needs to be public for flows such as running a message action

    internal suspend fun upsertMessage(message: Message) {
        upsertMessages(listOf(message))
    }

    private suspend fun upsertEventMessage(message: Message) {
        // make sure we don't lose ownReactions
        getMessage(message.id)?.let {
            message.ownReactions = it.ownReactions
        }
        upsertMessages(listOf(message))
    }

    override fun getMessage(messageId: String): Message? {
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
        val copy = _messages.value
        val newMessages = messageHelper.updateValidAttachmentsUrl(messages, copy)
        // filter out old events
        val freshMessages = mutableListOf<Message>()
        for (message in newMessages) {
            val oldMessage = copy[message.id]
            var outdated = false
            if (oldMessage != null) {
                val oldTime =
                    oldMessage.updatedAt?.time ?: oldMessage.updatedLocallyAt?.time ?: NEVER.time
                val newTime =
                    message.updatedAt?.time ?: message.updatedLocallyAt?.time ?: NEVER.time
                outdated = oldTime > newTime
            }
            if (!outdated) {
                freshMessages.add(message)
            } else {
                val oldDate = oldMessage?.updatedAt
                logger.logW("Skipping outdated message update for message with text ${message.text}. Old message date is $oldDate new message date id ${message.updatedAt}")
            }
        }

        // return all the fresh messages
        return copy + messages.map { it.copy() }.associateBy(Message::id)
    }

    private fun upsertMessages(messages: List<Message>) {
        val newMessages = parseMessages(messages)
        _messages.value = newMessages
    }

    private fun upsertOldMessages(messages: List<Message>) {
        _oldMessages.value = parseMessages(messages)
    }

    private fun removeLocalMessage(message: Message) {
        _messages.value = _messages.value - message.id
    }

    override fun clean() {
        // cleanup your own typing state
        val now = Date()
        if (lastStartTypingEvent != null && now.time - lastStartTypingEvent!!.time > 5000) {
            stopTyping()
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

    fun setTyping(userId: String, event: ChatEvent?) {
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
        // livedata actually batches many frequent updates after each other
        // we might not need a more optimized handleEvents implementation.. TBD.
        for (event in events) {
            handleEvent(event)
        }
    }

    fun isHidden(): Boolean {
        return _hidden.value
    }

    internal suspend fun handleEvent(event: ChatEvent) {
        when (event) {
            is NewMessageEvent -> {
                upsertEventMessage(event.message)
                setHidden(false)
            }
            is MessageUpdatedEvent -> {
                upsertEventMessage(event.message)
                setHidden(false)
            }
            is MessageDeletedEvent -> {
                upsertEventMessage(event.message)
                setHidden(false)
            }
            is NotificationMessageNewEvent -> {
                upsertEventMessage(event.message)
                setHidden(false)
                event.watcherCount?.let { setWatcherCount(it) }
            }
            is ReactionNewEvent -> {
                upsertEventMessage(event.message)
            }
            is ReactionDeletedEvent -> {
                upsertEventMessage(event.message)
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
            is NotificationChannelTruncatedEvent -> {
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
                event.watcherCount?.let { setWatcherCount(it) }
            }

            is MarkAllReadEvent -> {
                updateRead(ChannelUserRead(event.user, event.createdAt))
            }
        }
    }

    private fun upsertUserPresence(user: User) {
        val userId = user.id
        // members and watchers have users
        val members = _members.value
        val watchers = _watchers.value
        val member = members[userId]
        val watcher = watchers[userId]
        if (member != null) {
            member.user = user
            upsertMember(member)
        }
        if (watcher != null) {
            upsertWatcher(user)
        }
    }

    private suspend fun upsertUser(user: User) {
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

    fun upsertMembers(members: List<Member>) {
        _members.value = _members.value + members.associateBy { it.user.id }
    }

    fun upsertMember(member: Member) = upsertMembers(listOf(member))

    private fun updateReads(reads: List<ChannelUserRead>) {
        val currentUserId = domainImpl.currentUser.id
        val previousUserIdToReadMap = _reads.value
        val incomingUserIdToReadMap = reads.associateBy(ChannelUserRead::getUserId).toMutableMap()

        /*
        It's possible that the data coming back from the online channel query has a last read date that's before
        what we've last pushed to the UI. We want to ignore this, as it will cause an unread state to show in the
        channel list.
         */

        incomingUserIdToReadMap[currentUserId]?.let { incomingRead ->
            // the previous last Read date that is most current
            val previousLastRead =
                _read.value?.lastRead ?: previousUserIdToReadMap[currentUserId]?.lastRead

            // Use AFTER to determine if the incoming read is more current.
            // This prevents updates if it's BEFORE or EQUAL TO the previous Read.
            val incomingReadMoreCurrent =
                previousLastRead == null || incomingRead.lastRead?.after(previousLastRead) == true

            if (!incomingReadMoreCurrent) {
                // if the previous Read was more current, replace the item in the update map
                incomingUserIdToReadMap[currentUserId] =
                    ChannelUserRead(domainImpl.currentUser, previousLastRead)
                return@let // no need to post the incoming read value to the UI if it isn't newer
            }

            _read.value = incomingRead
        }

        // always post the newly updated map
        _reads.value = (previousUserIdToReadMap + incomingUserIdToReadMap)
    }

    private fun updateRead(
        read: ChannelUserRead
    ) {
        updateReads(listOf(read))
    }

    internal suspend fun updateLiveDataFromLocalChannel(localChannel: Channel) {
        localChannel.hidden?.let(::setHidden)
        hideMessagesBefore = localChannel.hiddenMessagesBefore
        updateLiveDataFromChannel(localChannel)
    }

    internal suspend fun updateOldMessagesFromLocalChannel(localChannel: Channel) {
        localChannel.hidden?.let(::setHidden)
        hideMessagesBefore = localChannel.hiddenMessagesBefore
        updateOldMessagesFromChannel(localChannel)
    }

    fun updateLiveDataFromChannel(c: Channel) {
        // Update all the livedata objects based on the channel
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
        // Update all the livedata objects based on the channel
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
        _watchers.value = (_watchers.value + watchers.associateBy { it.id })
    }

    suspend fun editMessage(message: Message): Result<Message> {
        // TODO: should we rename edit message into update message to be similar to llc?
        val online = domainImpl.isOnline()
        var editedMessage = message.copy()

        // set message.updated at if it's null or older than now (prevents issues with incorrect clocks)
        editedMessage.apply {
            val now = Date()
            if (updatedAt == null || updatedAt!!.before(now)) {
                updatedAt = now
            }
        }

        editedMessage.syncStatus = if (!online) SyncStatus.SYNC_NEEDED else SyncStatus.IN_PROGRESS

        // Update livedata
        upsertMessage(editedMessage)

        // Update Room State
        domainImpl.repos.messages.insert(editedMessage)

        if (online) {
            val runnable = {
                client.updateMessage(editedMessage)
            }
            // updating a message should cancel prior runnables editing the same message...
            // cancel previous message jobs
            editJobs[message.id]?.cancelAndJoin()
            val job = domainImpl.scope.async { domainImpl.runAndRetry(runnable) }
            editJobs[message.id] = job
            val result = job.await()
            if (result.isSuccess) {
                editedMessage = result.data()
                editedMessage.syncStatus = SyncStatus.COMPLETED
                upsertMessage(editedMessage)
                domainImpl.repos.messages.insert(editedMessage)

                return Result(editedMessage, null)
            } else {
                editedMessage.syncStatus = if (result.error().isPermanent()) {
                    SyncStatus.FAILED_PERMANENTLY
                } else {
                    SyncStatus.SYNC_NEEDED
                }

                upsertMessage(editedMessage)
                domainImpl.repos.messages.insert(editedMessage)
                return Result(null, result.error())
            }
        }
        return Result(editedMessage, null)
    }

    suspend fun deleteMessage(message: Message): Result<Message> {
        val online = domainImpl.isOnline()
        message.deletedAt = Date()
        message.syncStatus = if (!online) SyncStatus.SYNC_NEEDED else SyncStatus.IN_PROGRESS

        // Update livedata
        upsertMessage(message)

        // Update Room State
        domainImpl.repos.messages.insert(message)

        if (online) {
            val runnable = {
                client.deleteMessage(message.id)
            }
            val result = domainImpl.runAndRetry(runnable)
            if (result.isSuccess) {
                message.syncStatus = SyncStatus.COMPLETED
                upsertMessage(message)
                domainImpl.repos.messages.insert(message)
                return Result(result.data(), null)
            } else {
                message.syncStatus = if (result.error().isPermanent()) {
                    SyncStatus.FAILED_PERMANENTLY
                } else {
                    SyncStatus.SYNC_NEEDED
                }

                upsertMessage(message)
                domainImpl.repos.messages.insert(message)
                return Result(null, result.error())
            }
        }
        return Result(message, null)
    }

    override fun toChannel(): Channel {
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

    fun toChannelPreview(): ChannelPreview {

        return ChannelPreview(channel = toChannel(), _typingUsers.value)
    }

    internal fun loadOlderThreadMessages(threadId: String, limit: Int, firstMessage: Message? = null): Result<List<Message>> {
        val result = if (firstMessage != null) {
            client.getRepliesMore(threadId, firstMessage.id, limit).execute()
        } else {
            client.getReplies(threadId, limit).execute()
        }
        if (result.isSuccess) {
            val newMessages = result.data()
            upsertMessages(newMessages)
            // Note that we don't handle offline storage for threads at the moment.
        }
        return result
    }

    companion object {
        private const val TYPE_IMAGE = "image"
        private const val TYPE_FILE = "file"
    }
}
