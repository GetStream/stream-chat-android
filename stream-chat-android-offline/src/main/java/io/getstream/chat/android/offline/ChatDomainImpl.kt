package io.getstream.chat.android.offline

import android.content.Context
import android.os.Handler
import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.BuildConfig
import io.getstream.chat.android.client.BuildConfig.STREAM_CHAT_VERSION
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.call.map
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.enrichWithCid
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.setup.InitializationCoordinator
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.event.EventHandlerImpl
import io.getstream.chat.android.offline.experimental.channel.state.toMutableState
import io.getstream.chat.android.offline.experimental.global.GlobalMutableState
import io.getstream.chat.android.offline.experimental.global.GlobalState
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.experimental.plugin.state.StateRegistry
import io.getstream.chat.android.offline.extensions.users
import io.getstream.chat.android.offline.message.users
import io.getstream.chat.android.offline.model.ChannelConfig
import io.getstream.chat.android.offline.repository.RepositoryFacade
import io.getstream.chat.android.offline.service.sync.OfflineSyncFirebaseMessagingHandler
import io.getstream.chat.android.offline.usecase.EditMessage
import io.getstream.chat.android.offline.usecase.GetChannelController
import io.getstream.chat.android.offline.usecase.LoadNewerMessages
import io.getstream.chat.android.offline.usecase.WatchChannel
import io.getstream.chat.android.offline.utils.validateCid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch
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
    private val globalState: GlobalMutableState = GlobalMutableState.getOrCreate(),
) : ChatDomain, GlobalState by globalState {

    private val state: StateRegistry by lazy {
        StateRegistry.getOrCreate(job, scope, user, repos, repos.observeLatestUsers())
    }
    private val logic: LogicRegistry by lazy { LogicRegistry.getOrCreate(state, globalState, userPresence, repos, client) }

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

    override val typingUpdates: StateFlow<TypingEvent> = globalState.typingUpdates

    private var _eventHandler: EventHandlerImpl? = null

    @VisibleForTesting
    // Todo: Move this dependency to constructor
    internal var eventHandler: EventHandlerImpl
        get() = _eventHandler ?: throw IllegalStateException("You need to provide eventHandler!!")
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

    private val offlineSyncFirebaseMessagingHandler = OfflineSyncFirebaseMessagingHandler()

    /** State flow of latest cached users. Usually it has size of 100 as max size of LRU cache in [UserRepository].*/
    internal var latestUsers: StateFlow<Map<String, User>> = MutableStateFlow(emptyMap())
        private set

    private fun clearConnectionState() {
        activeChannelMapImpl.clear()
        latestUsers = MutableStateFlow(emptyMap())
    }

    internal fun userConnected(user: User) {
        clearConnectionState()

        initClean()
    }

    init {
        logger.logI("Initializing ChatDomain with version " + getVersion())

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

    override suspend fun disconnect() {
        job.cancelChildren()
        stopClean()
        clearConnectionState()
        offlineSyncFirebaseMessagingHandler.cancel(appContext)
        activeChannelMapImpl.clear()
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

    internal fun channel(c: Channel): ChannelController {
        return channel(c.type, c.id)
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
                domainImpl = this,
            )
            activeChannelMapImpl[cid] = channelController
            addTypingChannel(channelController)
        }
        return activeChannelMapImpl.getValue(cid)
    }

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
}
