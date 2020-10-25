package io.getstream.chat.android.livedata.controller

import NEVER
import android.webkit.MimeTypeMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
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
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.controller.helper.MessageHelper
import io.getstream.chat.android.livedata.entity.ChannelConfigEntity
import io.getstream.chat.android.livedata.entity.ReactionEntity
import io.getstream.chat.android.livedata.extensions.addReaction
import io.getstream.chat.android.livedata.extensions.isImageMimetype
import io.getstream.chat.android.livedata.extensions.removeReaction
import io.getstream.chat.android.livedata.repository.MessageRepository
import io.getstream.chat.android.livedata.request.QueryChannelPaginationRequest
import io.getstream.chat.android.livedata.utils.ChannelUnreadCountLiveData
import io.getstream.chat.android.livedata.utils.computeUnreadCount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private val _messages = MutableLiveData<Map<String, Message>>()
    private val _watcherCount = MutableLiveData<Int>()
    private val _typing = MutableLiveData<Map<String, ChatEvent>>()
    private val _reads = MutableLiveData<Map<String, ChannelUserRead>>()
    private val _read = MutableLiveData<ChannelUserRead>()
    private val _endOfNewerMessages = MutableLiveData(false)
    private val _endOfOlderMessages = MutableLiveData(false)
    private val _loading = MutableLiveData(false)
    private val _hidden = MutableLiveData(false)
    private val _muted = MutableLiveData(false)
    private val _watchers = MutableLiveData<Map<String, User>>(mapOf())
    private val _members = MutableLiveData<Map<String, Member>>()
    private val _loadingOlderMessages = MutableLiveData(false)
    private val _loadingNewerMessages = MutableLiveData(false)
    private val _channelData = MutableLiveData<ChannelData>()
    internal var hideMessagesBefore: Date? = null
    val unfilteredMessages: LiveData<List<Message>> =
        Transformations.map(_messages) { it.values.toList() }

    /** a list of messages sorted by message.createdAt */
    override val messages: LiveData<List<Message>> = Transformations.map(_messages) { messageMap ->
        // TODO: consider removing this check
        messageMap.values
            .asSequence()
            .filter { it.parentId == null || it.showInChannel }
            .filter { hideMessagesBefore == null || it.wasCreatedAfter(hideMessagesBefore) }
            .sortedBy { it.createdAt ?: it.createdLocallyAt }
            .toList()
            .map { it.copy() }
    }

    /** the number of people currently watching the channel */
    override val watcherCount: LiveData<Int> = _watcherCount

    /** the list of users currently watching this channel */
    override val watchers: LiveData<List<User>> = Transformations.map(_watchers) {
        it.values.sortedBy { user -> user.createdAt }
    }

    /** who is currently typing (current user is excluded from this) */
    override val typing: LiveData<List<User>> = Transformations.map(_typing) {
        it.values
            .sortedBy { event -> event.createdAt }
            .mapNotNull { event ->
                (event as? TypingStartEvent)?.user
                    ?: (event as? TypingStopEvent)?.user
            }
    }

    /** how far every user in this channel has read */
    override val reads: LiveData<List<ChannelUserRead>> = Transformations.map(_reads) {
        it.values.sortedBy { userRead -> userRead.lastRead }
    }

    /** read status for the current user */
    override val read: LiveData<ChannelUserRead> = _read

    /**
     * unread count for this channel, calculated based on read state (this works even if you're offline)
     */
    override val unreadCount: LiveData<Int> =
        ChannelUnreadCountLiveData(
            domainImpl.currentUser,
            read,
            messages
        )

    /** the list of members of this channel */
    override val members: LiveData<List<Member>> = Transformations.map(_members) {
        it.values.sortedBy { member -> member.createdAt }
    }

    /** LiveData object with the channel data */
    override val channelData: LiveData<ChannelData> = _channelData

    /** if the channel is currently hidden */
    override val hidden: LiveData<Boolean> = _hidden

    /** if the channel is currently muted */
    override val muted: LiveData<Boolean> = _muted

    /** if we are currently loading */
    override val loading: LiveData<Boolean> = _loading

    /** if we are currently loading older messages */
    override val loadingOlderMessages: LiveData<Boolean> = _loadingOlderMessages

    /** if we are currently loading newer messages */
    override val loadingNewerMessages: LiveData<Boolean> = _loadingNewerMessages

    /** set to true if there are no more older messages to load */
    override val endOfOlderMessages: LiveData<Boolean> = _endOfOlderMessages

    /** set to true if there are no more newer messages to load */
    override val endOfNewerMessages: LiveData<Boolean> = _endOfNewerMessages

    override var recoveryNeeded: Boolean = false
    private var lastMarkReadEvent: Date? = null
    private var lastKeystrokeAt: Date? = null
    private var lastStartTypingEvent: Date? = null
    val channelController = client.channel(channelType, channelId)
    override val cid = "%s:%s".format(channelType, channelId)

    private val job = SupervisorJob()
    val scope = CoroutineScope(Dispatchers.IO + domainImpl.job + job)

    private val logger = ChatLogger.get("ChatDomain ChannelController")

    private val threadControllerMap: ConcurrentHashMap<String, ThreadControllerImpl> =
        ConcurrentHashMap()
    private val messageHelper = MessageHelper()

    fun getThread(threadId: String): ThreadControllerImpl = threadControllerMap.getOrPut(threadId) {
        ThreadControllerImpl(threadId, this, client)
            .also { scope.launch { it.watch() } }
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

    fun markRead(): Result<Boolean> {
        if (!getConfig().isReadEvents) return Result(false, null)
        // throttle the mark read
        val messages = sortedMessages()
        if (messages.isNotEmpty()) {
            val last = messages.last()
            val lastMessageDate = last.createdAt ?: last.createdLocallyAt

            if (lastMarkReadEvent == null || lastMessageDate!!.after(lastMarkReadEvent)) {
                lastMarkReadEvent = lastMessageDate
                val userRead = ChannelUserRead(domainImpl.currentUser).apply {
                    lastRead = last.createdAt ?: last.createdLocallyAt
                }
                _read.postValue(userRead)
                client.markMessageRead(channelType, channelId, last.id).execute()
                return Result(true, null)
            }
        }
        return Result(false, null)
    }

    private fun sortedMessages(): List<Message> {
        // sorted ascending order, so the oldest messages are at the beginning of the list
        var messages = emptyList<Message>()
        _messages.value?.let { mapOfMessages ->
            messages = mapOfMessages.values
                .sortedBy { it.createdAt ?: it.createdLocallyAt }
                .filter { hideMessagesBefore == null || it.wasCreatedAfter(hideMessagesBefore) }
        }
        return messages
    }

    private fun removeMessagesBefore(date: Date) {
        val copy = _messages.value ?: mutableMapOf()
        // start off empty
        _messages.postValue(mutableMapOf())
        // call upsert with the messages that are recent
        val recentMessages = copy.values.filter { it.wasCreatedAfter(date) }
        upsertMessages(recentMessages)
    }

    suspend fun hide(clearHistory: Boolean): Result<Unit> {
        setHidden(true)
        val result = channelController.hide(clearHistory).execute()
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
        val result = channelController.show().execute()
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
        _loading.postValue(true)
        val pagination = QueryChannelPaginationRequest(limit)
        runChannelQuery(pagination)

        _loading.postValue(false)
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
        if (_loadingOlderMessages.value == true) {
            logger.logI("Another request to load older messages is in progress. Ignoring this request.")
            return Result(
                null,
                ChatError("Another request to load older messages is in progress. Ignoring this request.")
            )
        }
        _loadingOlderMessages.postValue(true)
        val pagination = loadMoreMessagesRequest(limit, Pagination.LESS_THAN)
        val result = runChannelQuery(pagination)
        _loadingOlderMessages.postValue(false)
        return result
    }

    suspend fun loadNewerMessages(limit: Int = 30): Result<Channel> {
        if (_loadingNewerMessages.value == true) {
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
        val queryOfflineJob = scope.async { runChannelQueryOffline(pagination) }
        // start the online query before queryOfflineJob.await
        val queryOnlineJob = if (domainImpl.isOnline()) { scope.async { runChannelQueryOnline(pagination) } } else { null }
        val localChannel = queryOfflineJob.await()
        if (localChannel != null) {
            updateLiveDataFromLocalChannel(localChannel)
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
            _loading.postValue(false)

            logger.logI("Loaded channel ${channel.cid} from offline storage with ${channel.messages.size} messages")
        }

        return selectedChannel
    }

    suspend fun runChannelQueryOnline(pagination: QueryChannelPaginationRequest): Result<Channel> {
        val request = pagination.toQueryChannelRequest(domainImpl.userPresence)
        val response = channelController.watch(request).execute()

        if (response.isSuccess) {
            recoveryNeeded = false
            val channelResponse = response.data()
            if (pagination.messageLimit > channelResponse.messages.size) {
                if (request.isFilteringNewerMessages()) {
                    _endOfNewerMessages.postValue(true)
                } else {
                    _endOfOlderMessages.postValue(true)
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
    ): Result<Message> = withContext(scope.coroutineContext) {
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
        newMessage.type = if (newMessage.text.startsWith("/")) { "ephemeral" } else { "regular" }
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

        return@withContext if (online) {
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

            val result = domainImpl.runAndRetry { channelController.sendMessage(newMessage) }
            if (result.isSuccess) {
                val processedMessage: Message = result.data()
                processedMessage.apply {
                    syncStatus = SyncStatus.COMPLETED
                    domainImpl.repos.messages.insert(this)
                }

                upsertMessage(processedMessage)
                Result(processedMessage, null)
            } else {

                logger.logE("Failed to send message with id ${newMessage.id} and text ${newMessage.text}: ${result.error()}", result.error())

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
        val result = domainImpl.runAndRetry { channelController.sendAction(request) }
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
        val result = domainImpl.runAndRetry { channelController.sendAction(request) }
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

    suspend fun sendImage(file: File): Result<String> = withContext(scope.coroutineContext) {
        client.sendImage(channelType, channelId, file).execute()
    }

    suspend fun sendFile(file: File): Result<String> = withContext(scope.coroutineContext) {
        client.sendFile(channelType, channelId, file).execute()
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
                logger.logE("Failed to send reaction of type ${reaction.type} on messge ${reaction.messageId}", result.error())

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

    fun setWatcherCount(watcherCount: Int) {
        if (watcherCount != _watcherCount.value) {
            _watcherCount.postValue(watcherCount)
        }
    }

    // This one needs to be public for flows such as running a message action
    override fun upsertMessage(message: Message) {
        upsertMessages(listOf(message))
    }

    private fun upsertEventMessage(message: Message) {
        // make sure we don't lose ownReactions
        getMessage(message.id)?.let {
            message.ownReactions = it.ownReactions
        }
        upsertMessages(listOf(message))
    }

    override fun getMessage(messageId: String): Message? {
        val copy = _messages.value ?: mutableMapOf()
        var message = copy[messageId]

        if (hideMessagesBefore != null) {
            if (message != null && message.wasCreatedBeforeOrAt(hideMessagesBefore)) {
                message = null
            }
        }

        return message
    }

    private fun upsertMessages(messages: List<Message>) {
        var copy = _messages.value ?: mapOf()
        val newMessages = messageHelper.updateValidAttachmentsUrl(messages, copy)
        // filter out old events
        val freshMessages = mutableListOf<Message>()
        for (message in newMessages) {
            val oldMessage = copy[message.id]
            var outdated = false
            if (oldMessage != null) {
                val oldTime = oldMessage.updatedAt?.time ?: oldMessage.updatedLocallyAt?.time ?: NEVER.time
                val newTime = message.updatedAt?.time ?: message.updatedLocallyAt?.time ?: NEVER.time
                outdated = oldTime > newTime
            }
            if (!outdated) {
                freshMessages.add(message)
            } else {
                val oldDate = oldMessage?.updatedAt
                logger.logW("Skipping outdated message update for message with text ${message.text}. Old message date is $oldDate new message date id ${message.updatedAt}")
            }
        }

        // update all the fresh messages
        copy = copy + messages.map { it.copy() }.associateBy(Message::id)
        _messages.postValue(copy)
    }

    private fun removeLocalMessage(message: Message) {
        val messages = _messages.value ?: mapOf()
        _messages.postValue(messages - message.id)
    }

    override fun clean() {
        // cleanup your own typing state
        val now = Date()
        if (lastStartTypingEvent != null && now.time - lastStartTypingEvent!!.time > 5000) {
            stopTyping()
        }

        // Cleanup typing events that are older than 15 seconds
        var copy = _typing.value ?: mapOf()
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
            _typing.postValue(copy)
        }
    }

    fun setTyping(userId: String, event: ChatEvent?) {
        val copy = _typing.value?.toMutableMap() ?: mutableMapOf()
        if (event == null) {
            copy.remove(userId)
        } else {
            copy[userId] = event
        }
        copy.remove(domainImpl.currentUser.id)
        _typing.postValue(copy.toMap())
    }

    private fun setHidden(hidden: Boolean) {
        if (_hidden.value != hidden) {
            _hidden.postValue(hidden)
        }
    }

    fun handleEvents(events: List<ChatEvent>) {
        // livedata actually batches many frequent updates after each other
        // we might not need a more optimized handleEvents implementation.. TBD.
        for (event in events) {
            handleEvent(event)
        }
    }

    fun isHidden(): Boolean {
        return _hidden.value ?: false
    }

    fun handleEvent(event: ChatEvent) {
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
                    _channelData.postValue(it)
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
        }
    }

    private fun upsertUserPresence(user: User) {
        val userId = user.id
        // members and watchers have users
        val members = _members.value ?: mutableMapOf()
        val watchers = _watchers.value ?: mutableMapOf()
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
        val messages = _messages.value ?: mutableMapOf()
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
        _watchers.postValue((_watchers.value ?: mapOf()) - user.id)
    }

    private fun upsertWatcher(user: User) {
        _watchers.postValue((_watchers.value ?: mapOf()) + mapOf(user.id to user))
    }

    private fun deleteMember(userId: String) {
        val copy = _members.value ?: mapOf()
        _members.postValue(copy - userId)
    }

    fun upsertMembers(members: List<Member>) {
        val channelMembers = _members.value ?: mapOf()
        _members.postValue(channelMembers + members.associateBy { it.user.id })
    }

    fun upsertMember(member: Member) = upsertMembers(listOf(member))

    fun updateReads(
        reads: List<ChannelUserRead>
    ) {
        val currentUserId = domainImpl.currentUser.id
        val copy = _reads.value ?: mutableMapOf()
        val readMap = reads.associateBy { it.getUserId() }

        // handle the current user
        val currentUserRead = readMap[currentUserId]
        currentUserRead?.let {
            _read.postValue(it)
            println("updateReads for current user to ${it.lastRead} on channel $cid")
        }
        _reads.postValue(copy + reads.associateBy(ChannelUserRead::getUserId))
    }

    fun updateRead(
        read: ChannelUserRead
    ) {
        updateReads(listOf(read))
    }

    internal fun updateLiveDataFromLocalChannel(localChannel: Channel) {
        localChannel.hidden?.let(::setHidden)
        hideMessagesBefore = localChannel.hiddenMessagesBefore
        updateLiveDataFromChannel(localChannel)
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
    }

    private fun setMembers(members: List<Member>) {
        val copy = _members.value ?: mapOf()
        _members.postValue(copy + members.associateBy(Member::getUserId))
    }

    fun updateChannelData(channel: Channel) {
        _channelData.postValue(ChannelData(channel))
    }

    fun setWatchers(watchers: List<User>) {
        _watchers.postValue((_watchers.value ?: mapOf()) + watchers.associateBy { it.id })
    }

    suspend fun editMessage(message: Message): Result<Message> {
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
            val job = scope.async { domainImpl.runAndRetry(runnable) }
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
        val members = (_members.value ?: mutableMapOf()).values.toList()
        val watchers = (_watchers.value ?: mutableMapOf()).values.toList()
        val reads = (_reads.value ?: mutableMapOf()).values.toList()
        val watcherCount = _watcherCount.value ?: 0

        val channel = channelData.toChannel(messages, members, reads, watchers, watcherCount)
        channel.config = getConfig()
        channel.unreadCount = computeUnreadCount(domainImpl.currentUser, _read.value, messages)
        channel.lastMessageAt = messages.lastOrNull()?.let { it.createdAt ?: it.createdLocallyAt }
        channel.hidden = _hidden.value

        return channel
    }

    companion object {
        private const val TYPE_IMAGE = "image"
        private const val TYPE_FILE = "file"
    }
}
