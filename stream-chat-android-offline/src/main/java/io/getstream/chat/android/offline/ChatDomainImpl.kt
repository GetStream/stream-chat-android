package io.getstream.chat.android.offline

import android.content.Context
import android.os.Handler
import androidx.annotation.CheckResult
import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.BuildConfig
import io.getstream.chat.android.client.BuildConfig.STREAM_CHAT_VERSION
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.call.map
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.enrichWithCid
import io.getstream.chat.android.client.extensions.isPermanent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.setup.InitializationCoordinator
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.client.utils.map
import io.getstream.chat.android.client.utils.observable.Disposable
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.event.EventHandlerImpl
import io.getstream.chat.android.offline.experimental.channel.state.toMutableState
import io.getstream.chat.android.offline.experimental.channel.thread.state.toMutableState
import io.getstream.chat.android.offline.experimental.global.GlobalMutableState
import io.getstream.chat.android.offline.experimental.global.GlobalState
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.experimental.plugin.state.StateRegistry
import io.getstream.chat.android.offline.experimental.querychannels.state.toMutableState
import io.getstream.chat.android.offline.extensions.applyPagination
import io.getstream.chat.android.offline.extensions.users
import io.getstream.chat.android.offline.message.attachment.UploadAttachmentsNetworkType
import io.getstream.chat.android.offline.message.users
import io.getstream.chat.android.offline.model.ChannelConfig
import io.getstream.chat.android.offline.model.ConnectionState
import io.getstream.chat.android.offline.model.SyncState
import io.getstream.chat.android.offline.querychannels.QueryChannelsController
import io.getstream.chat.android.offline.repository.RepositoryFacade
import io.getstream.chat.android.offline.request.AnyChannelPaginationRequest
import io.getstream.chat.android.offline.request.QueryChannelsPaginationRequest
import io.getstream.chat.android.offline.request.toAnyChannelPaginationRequest
import io.getstream.chat.android.offline.service.sync.OfflineSyncFirebaseMessagingHandler
import io.getstream.chat.android.offline.thread.ThreadController
import io.getstream.chat.android.offline.usecase.EditMessage
import io.getstream.chat.android.offline.usecase.GetChannelController
import io.getstream.chat.android.offline.usecase.LoadNewerMessages
import io.getstream.chat.android.offline.usecase.QueryChannels
import io.getstream.chat.android.offline.usecase.WatchChannel
import io.getstream.chat.android.offline.utils.Event
import io.getstream.chat.android.offline.utils.validateCid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch
import java.util.Date
import java.util.InputMismatchException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

private const val MESSAGE_LIMIT = 30
private const val MEMBER_LIMIT = 30
private const val INITIAL_CHANNEL_OFFSET = 0
private const val CHANNEL_LIMIT = 30

/**
 * The Chat Domain exposes StateFlow objects to make it easier to build your chat UI.
 * It intercepts the various low level events to ensure data stays in sync.
 * Offline storage is handled using Room
 *
 * A different Room database is used for different users. That's why it's mandatory to specify the user id when
 * initializing the ChatRepository
 *
 * chatDomain.channel(type, id) returns a controller object with channel specific state flow objects
 * chatDomain.queryChannels(query) returns a state flow object for the specific queryChannels query
 *
 * chatDomain.online state flow object indicates if you're online or not
 * chatDomain.totalUnreadCount state flow object returns the current unread count for this user
 * chatDomain.muted the list of muted users
 * chatDomain.banned if the current user is banned or not
 * chatDomain.channelUnreadCount state flow object returns the number of unread channels for this user
 * chatDomain.errorEvents events for errors that happen while interacting with the chat
 *
 */
internal class ChatDomainImpl internal constructor(
    internal var client: ChatClient,
    @VisibleForTesting
    private val mainHandler: Handler,
    internal var recoveryEnabled: Boolean = true,
    override var userPresence: Boolean = false,
    internal var backgroundSyncEnabled: Boolean = false,
    internal var appContext: Context,
    internal val uploadAttachmentsNetworkType: UploadAttachmentsNetworkType = UploadAttachmentsNetworkType.NOT_ROAMING,
    private val globalState: GlobalMutableState = GlobalMutableState.getOrCreate(),
) : ChatDomain, GlobalState by globalState {
    internal constructor(
        client: ChatClient,
        handler: Handler,
        recoveryEnabled: Boolean,
        userPresence: Boolean,
        backgroundSyncEnabled: Boolean,
        appContext: Context,
        globalState: GlobalMutableState = GlobalMutableState.getOrCreate(),
    ) : this(
        client = client,
        mainHandler = handler,
        recoveryEnabled = recoveryEnabled,
        userPresence = userPresence,
        backgroundSyncEnabled = backgroundSyncEnabled,
        appContext = appContext,
        globalState = globalState,
    )

    private val state: StateRegistry by lazy {
        StateRegistry.getOrCreate(job, scope, user, repos, repos.observeLatestUsers())
    }
    private val logic: LogicRegistry by lazy { LogicRegistry.getOrCreate(state) }

    internal val job = SupervisorJob()
    internal var scope = CoroutineScope(job + DispatcherProvider.IO)

    @VisibleForTesting
    val defaultConfig: Config = Config(connectEventsEnabled = true, muteEnabled = true)

    /*
     * It is necessary to initialize `RepositoryFacade` lazily to give time to the real RepositoryFacade to be set
     * instead of using `createNoOpRepos()`, otherwise the SDK will create a in memory Room's database which will be later
     * replaced with the real database. This creates a resource leak because, when the second database is created, the first one is
     * not closed by room.
     */

    internal lateinit var repos: RepositoryFacade
    /** stores the mapping from cid to ChannelController */
    private val activeChannelMapImpl: ConcurrentHashMap<String, ChannelController> = ConcurrentHashMap()
    private val activeQueryMapImpl: ConcurrentHashMap<String, QueryChannelsController> = ConcurrentHashMap()

    override val typingUpdates: StateFlow<TypingEvent> = globalState.typingUpdates

    private var _eventHandler: EventHandlerImpl? = null

    @VisibleForTesting
    // Todo: Move this dependency to constructor
    internal var eventHandler: EventHandlerImpl
        get() = _eventHandler ?: throw IllegalArgumentException("EventHandlerImpl was not initialized yet")
        set(value) {
            _eventHandler = value
        }

    private var logger = ChatLogger.get("Domain")

    private val cleanTask = object : Runnable {
        override fun run() {
            clean()
            mainHandler.postDelayed(this, 1000)
        }
    }
    private val syncStateFlow: MutableStateFlow<SyncState?> = MutableStateFlow(null)
    internal var initJob: Deferred<*>? = null

    private val offlineSyncFirebaseMessagingHandler = OfflineSyncFirebaseMessagingHandler()

    /** State flow of latest cached users. Usually it has size of 100 as max size of LRU cache in [UserRepository].*/
    internal var latestUsers: StateFlow<Map<String, User>> = MutableStateFlow(emptyMap())
        private set

    private fun clearUnreadCountState() {
        globalState._totalUnreadCount.value = 0
        globalState._channelUnreadCount.value = 0
    }

    private fun clearConnectionState() {
        globalState._initialized.value = false
        globalState._connectionState.value = ConnectionState.OFFLINE
        globalState._banned.value = false
        globalState._mutedUsers.value = emptyList()
        latestUsers = MutableStateFlow(emptyMap())
    }

    internal fun setUser(user: User) {
        globalState._user.value = user
        // load channel configs from Room into memory
    }

    internal fun userConnected(user: User) {
        clearConnectionState()
        clearUnreadCountState()

        if (client.isSocketConnected()) {
            globalState._connectionState.value = ConnectionState.CONNECTED
        }
        initClean()
    }

    init {
        logger.logI("Initializing ChatDomain with version " + getVersion())

        // if the user is already defined, just call setUser ourselves
        val current = client.getCurrentUser()
        if (current != null) {
            setUser(current)
        }

        if (backgroundSyncEnabled) {
            client.setPushNotificationReceivedListener { channelType, channelId ->
                offlineSyncFirebaseMessagingHandler.syncMessages(appContext, "$channelType:$channelId")
            }
        }

        InitializationCoordinator.getOrCreate().addUserDisconnectedListener {
            scope.launch {
                disconnect()
            }
        }
    }

    internal suspend fun updateCurrentUser(me: User) {
        if (me.id != user.value?.id) {
            throw InputMismatchException("received connect event for user with id ${me.id} while chat domain is configured for user with id ${user.value?.id}. create a new ChatDomain when connecting a different user.")
        }
        globalState._user.value = me
        repos.insertCurrentUser(me)
        globalState._mutedUsers.value = me.mutes
        globalState._channelMutes.value = me.channelMutes
        globalState._totalUnreadCount.value = me.totalUnreadCount
        setChannelUnreadCount(me.unreadChannels)
        setBanned(me.banned)
    }

    override suspend fun disconnect() {
        job.cancelChildren()
        stopClean()
        clearConnectionState()
        offlineSyncFirebaseMessagingHandler.cancel(appContext)
        logic.clear()
        state.clear()
    }

    override fun getVersion(): String {
        return STREAM_CHAT_VERSION + "-" + BuildConfig.BUILD_TYPE
    }

    private fun stopClean() {
        mainHandler.removeCallbacks(cleanTask)
    }

    private fun initClean() {
        mainHandler.postDelayed(cleanTask, 5000)
    }

    fun addError(error: ChatError) {
        globalState._errorEvent.value = Event(error)
    }

    @VisibleForTesting
    fun addActiveChannel(cid: String, channelController: ChannelController) {
        activeChannelMapImpl[cid] = channelController
    }

    fun setChannelUnreadCount(newCount: Int) {
        globalState._channelUnreadCount.value = newCount
    }

    fun setBanned(newBanned: Boolean) {
        globalState._banned.value = newBanned
    }

    fun setTotalUnreadCount(newCount: Int) {
        globalState._totalUnreadCount.value = newCount
    }

    internal fun channel(cid: String): ChannelController {
        val (channelType, channelId) = cid.cidToTypeAndId()
        return channel(channelType, channelId)
    }

    /**
     * @return [Channel] object from repository if exists, null otherwise.
     */
    internal suspend fun getCachedChannel(cid: String): Channel? {
        return repos.selectChannelWithoutMessages(cid)
    }

    internal fun channel(
        channelType: String,
        channelId: String,
    ): ChannelController {
        val cid = "%s:%s".format(channelType, channelId)
        if (!activeChannelMapImpl.containsKey(cid)) {
            val channelController = ChannelController(
                mutableState = state.channel(channelType, channelId).toMutableState(),
                channelLogic = logic.channel(channelType, channelId),
                client = client,
                userPresence = userPresence,
                repos = repos,
                scope = scope,
                globalState = globalState
            )
            activeChannelMapImpl[cid] = channelController
            addTypingChannel(channelController)
        }
        return activeChannelMapImpl.getValue(cid)
    }

    internal fun allActiveChannels(): List<ChannelController> =
        activeChannelMapImpl.values.toList()

    fun generateMessageId(): String {
        return user.value!!.id + "-" + UUID.randomUUID().toString()
    }

    private fun addTypingChannel(channelController: ChannelController) {
        scope.launch { globalState._typingChannels.emitAll(channelController.typing) }
    }

    override fun isOnline(): Boolean = globalState.isOnline()

    override fun isOffline(): Boolean = globalState.isOffline()

    override fun isConnecting(): Boolean = globalState.isConnecting()

    override fun isInitialized(): Boolean = globalState.isInitialized()

    override fun getActiveQueries(): List<QueryChannelsController> {
        return activeQueryMapImpl.values.toList()
    }

    /**
     * queryChannels
     * - first read the current results from Room
     * - if we are online make the API call to update results
     */
    fun queryChannels(
        filter: FilterObject,
        sort: QuerySort<Channel>,
    ): QueryChannelsController =
        activeQueryMapImpl.getOrPut("${filter.hashCode()}-${sort.hashCode()}") {
            val mutableState = state.queryChannels(filter, sort).toMutableState()
            val logic = logic.queryChannels(filter, sort)
            QueryChannelsController(domainImpl = this, mutableState = mutableState, queryChannelsLogic = logic)
        }

    private suspend fun markMessageAsFailed(message: Message) =
        repos.insertMessage(message.copy(syncStatus = SyncStatus.FAILED_PERMANENTLY, updatedLocallyAt = Date()))

    @VisibleForTesting
    internal suspend fun retryReactions(): List<Reaction> {
        return repos.selectReactionsBySyncStatus(SyncStatus.SYNC_NEEDED).onEach { reaction ->
            val result = if (reaction.deletedAt != null) {
                client.deleteReaction(reaction.messageId, reaction.type).await()
            } else {
                client.sendReaction(reaction, reaction.enforceUnique).await()
            }

            if (result.isSuccess) {
                reaction.syncStatus = SyncStatus.COMPLETED
                repos.insertReaction(reaction)
            } else if (result.error().isPermanent()) {
                reaction.syncStatus = SyncStatus.FAILED_PERMANENTLY
                repos.insertReaction(reaction)
            }
        }
    }

    suspend fun storeStateForChannel(channel: Channel) {
        return storeStateForChannels(listOf(channel))
    }

    suspend fun storeStateForChannels(channelsResponse: Collection<Channel>) {
        val users = mutableMapOf<String, User>()
        val configs: MutableCollection<ChannelConfig> = mutableSetOf()
        // start by gathering all the users
        val messages = mutableListOf<Message>()
        for (channel in channelsResponse) {
            users.putAll(channel.users().associateBy { it.id })
            configs += ChannelConfig(channel.type, channel.config)

            channel.messages.forEach { message ->
                message.enrichWithCid(channel.cid)
                users.putAll(message.users().associateBy { it.id })
            }

            messages.addAll(channel.messages)
        }

        repos.storeStateForChannels(
            configs = configs,
            users = users.values.toList(),
            channels = channelsResponse,
            messages = messages
        )

        logger.logI("storeStateForChannels stored ${channelsResponse.size} channels, ${configs.size} configs, ${users.size} users and ${messages.size} messages")
    }

    suspend fun selectAndEnrichChannel(
        channelId: String,
        pagination: QueryChannelRequest,
    ): Channel? = selectAndEnrichChannels(listOf(channelId), pagination.toAnyChannelPaginationRequest()).getOrNull(0)

    suspend fun selectAndEnrichChannels(
        channelIds: List<String>,
        pagination: QueryChannelsPaginationRequest,
    ): List<Channel> = selectAndEnrichChannels(channelIds, pagination.toAnyChannelPaginationRequest())

    private suspend fun selectAndEnrichChannels(
        channelIds: List<String>,
        pagination: AnyChannelPaginationRequest,
    ): List<Channel> = repos.selectChannels(channelIds, pagination).applyPagination(pagination)

    override fun clean() {
        for (channelController in activeChannelMapImpl.values.toList()) {
            channelController.clean()
        }
    }

    override fun getChannelConfig(channelType: String): Config =
        repos.selectChannelConfig(channelType)?.config ?: defaultConfig

// region use-case functions

    override fun getChannelController(cid: String): Call<ChannelController> = GetChannelController(this).invoke(cid)

    override fun watchChannel(cid: String, messageLimit: Int): Call<ChannelController> =
        WatchChannel(this).invoke(cid, messageLimit)

    override fun queryChannels(
        filter: FilterObject,
        sort: QuerySort<Channel>,
        limit: Int,
        messageLimit: Int,
        memberLimit: Int,
    ): Call<QueryChannelsController> = QueryChannels(this).invoke(filter, sort, limit, messageLimit, memberLimit)

    /**
     * Returns a thread controller for the given channel and message id.
     *
     * @param cid The full channel id. ie messaging:123.
     * @param parentId The message id for the parent of this thread.
     *
     * @see io.getstream.chat.android.offline.thread.ThreadController
     */
    @CheckResult
    override fun getThread(cid: String, parentId: String): Call<ThreadController> {
        validateCid(cid)
        return CoroutineCall(scope) {
            Result.success(
                channel(cid).getThread(
                    state.thread(parentId).toMutableState(),
                    logic.thread(parentId)
                )
            )
        }
    }

    override fun loadNewerMessages(cid: String, messageLimit: Int): Call<Channel> =
        LoadNewerMessages(this).invoke(cid, messageLimit)

    override fun loadMessageById(
        cid: String,
        messageId: String,
        olderMessagesOffset: Int,
        newerMessagesOffset: Int,
    ): Call<Message> {
        return CoroutineCall(scope) {
            try {
                validateCid(cid)
                val channelController = channel(cid)
                channelController.loadMessageById(messageId, newerMessagesOffset, olderMessagesOffset)
            } catch (e: IllegalArgumentException) {
                Result(ChatError(e.message))
            }
        }
    }

    override fun queryChannelsLoadMore(
        filter: FilterObject,
        sort: QuerySort<Channel>,
        limit: Int,
        messageLimit: Int,
        memberLimit: Int,
    ): Call<List<Channel>> {
        return CoroutineCall(scope) {
            val queryChannelsController = queryChannels(filter, sort)
            val oldChannels = queryChannelsController.channels.value
            val pagination = queryChannelsController.loadMoreRequest(
                channelLimit = limit,
                messageLimit = messageLimit,
                memberLimit = memberLimit,
            )
            queryChannelsController.runQuery(pagination).map { it - oldChannels.toSet() }
        }
    }

    override fun queryChannelsLoadMore(
        filter: FilterObject,
        sort: QuerySort<Channel>,
        messageLimit: Int,
    ): Call<List<Channel>> = queryChannelsLoadMore(
        filter = filter,
        sort = sort,
        limit = CHANNEL_LIMIT,
        messageLimit = messageLimit,
        memberLimit = MEMBER_LIMIT,
    )

    override fun queryChannelsLoadMore(
        filter: FilterObject,
        sort: QuerySort<Channel>,
    ): Call<List<Channel>> = queryChannelsLoadMore(
        filter = filter,
        sort = sort,
        limit = CHANNEL_LIMIT,
        messageLimit = MESSAGE_LIMIT,
        memberLimit = MEMBER_LIMIT,
    )

    /**
     * Loads more messages for the specified thread.
     *
     * @param cid The full channel id i. e. messaging:123.
     * @param parentId The parentId of the thread.
     * @param messageLimit How many new messages to load.
     */
    override fun threadLoadMore(cid: String, parentId: String, messageLimit: Int): Call<List<Message>> {
        validateCid(cid)
        require(parentId.isNotEmpty()) { "parentId can't be empty" }

        return CoroutineCall(scope) {
            val threadController = getThread(cid, parentId).execute().data()
            threadController.loadOlderMessages(messageLimit)
        }
    }

    /**
     * Performs giphy shuffle operation. Removes the original "ephemeral" message from local storage.
     * Returns new "ephemeral" message with new giphy url.
     * API call to remove the message is retried according to the retry policy specified on the chatDomain
     *
     * @param message The message to send.
     * @see io.getstream.chat.android.offline.utils.RetryPolicy
     */
    override fun shuffleGiphy(message: Message): Call<Message> = client.shuffleGiphy(message)

    /**
     * Sends selected giphy message to the channel. Removes the original "ephemeral" message from local storage.
     * Returns new "ephemeral" message with new giphy url.
     * API call to remove the message is retried according to the retry policy specified on the chatDomain.
     *
     * @param message The message to send.
     * @see io.getstream.chat.android.offline.utils.RetryPolicy
     */
    override fun sendGiphy(message: Message): Call<Message> = client.sendGiphy(message)

    @Deprecated(
        message = "ChatDomain.editMessage is deprecated. Use function ChatClient::updateMessage instead",
        replaceWith = ReplaceWith(
            expression = "ChatClient.instance().updateMessage(message)",
            imports = arrayOf("io.getstream.chat.android.client.ChatClient")
        ),
        level = DeprecationLevel.WARNING
    )
    override fun editMessage(message: Message): Call<Message> = EditMessage(this).invoke(message)

    override fun sendReaction(cid: String, reaction: Reaction, enforceUnique: Boolean): Call<Reaction> =
        client.sendReaction(reaction = reaction, enforceUnique = enforceUnique, cid = cid)

    override fun deleteReaction(cid: String, reaction: Reaction): Call<Message> =
        client.deleteReaction(cid = cid, messageId = reaction.messageId, reactionType = reaction.type)

    override fun markRead(cid: String): Call<Boolean> {
        val (channelType, channelId) = cid.cidToTypeAndId()
        return client.markRead(channelType = channelType, channelId = channelId).map {
            true
        }
    }

    override fun markAllRead(): Call<Boolean> = client.markAllRead().map { true }

    override fun hideChannel(cid: String, keepHistory: Boolean): Call<Unit> {
        val (channelType, channelId) = cid.cidToTypeAndId()
        return client.hideChannel(channelType = channelType, channelId = channelId, clearHistory = !keepHistory)
    }

// end region

    companion object {
        val EMPTY_DISPOSABLE = object : Disposable {
            override val isDisposed: Boolean = true
            override fun dispose() {}
        }
    }
}
