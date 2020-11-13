package io.getstream.chat.android.client

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.api.models.SearchMessagesRequest
import io.getstream.chat.android.client.api.models.SendActionRequest
import io.getstream.chat.android.client.api.models.UpdateChannelRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.map
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.di.ChatModule
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.ErrorEvent
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.logger.ChatLoggerHandler
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.Flag
import io.getstream.chat.android.client.models.GuestUser
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.ModelFields
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.notifications.ChatNotifications
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.socket.InitConnectionListener
import io.getstream.chat.android.client.socket.SocketListener
import io.getstream.chat.android.client.token.TokenManager
import io.getstream.chat.android.client.token.TokenManagerImpl
import io.getstream.chat.android.client.token.TokenProvider
import io.getstream.chat.android.client.uploader.FileUploader
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.client.utils.ImmediateTokenProvider
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.observable.ChatEventsObservable
import io.getstream.chat.android.client.utils.observable.ChatObservable
import io.getstream.chat.android.client.utils.observable.Disposable
import java.io.File
import java.util.Date

/***
 * The ChatClient is the main entry point for all low-level operations on chat
 */
public class ChatClient internal constructor(
    public val config: ChatClientConfig,
    private val api: ChatApi,
    private val socket: ChatSocket,
    private val notifications: ChatNotifications,
    private val tokenManager: TokenManager = TokenManagerImpl()
) : LifecycleObserver {

    private val state = ClientState()
    private var connectionListener: InitConnectionListener? = null
    private val logger = ChatLogger.get("Client")
    private val eventsObservable = ChatEventsObservable(socket)

    public val disconnectListeners: MutableList<(User?) -> Unit> = mutableListOf<(User?) -> Unit>()
    public val preSetUserListeners: MutableList<(User) -> Unit> = mutableListOf<(User) -> Unit>()

    init {
        eventsObservable.subscribe { event ->

            notifications.onChatEvent(event)

            when (event) {
                is ConnectedEvent -> {

                    val user = event.me
                    val connectionId = event.connectionId

                    state.user = user
                    state.connectionId = connectionId
                    state.socketConnected = true
                    api.setConnection(user.id, connectionId)
                    callConnectionListener(event, null)
                }
                is ErrorEvent -> {
                    callConnectionListener(null, event.error)
                }
                is DisconnectedEvent -> {
                    state.socketConnected = false
                }
            }
        }

        // disconnect when the app is stopped
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        logger.logI("Initialised: " + getVersion())
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public fun onResume() {
        reconnectSocket()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public fun onStopped() {
        disconnectSocket()
    }

    //region Set user

    /***
     * Initializes [ChatClient] for a specific user using the given user [token].
     *
     * @see ChatClient.setUser with [TokenProvider] for advanced use cases
     */
    public fun setUser(user: User, token: String, listener: InitConnectionListener? = null) {
        state.token = token
        setUser(user, ImmediateTokenProvider(token), listener)
    }

    /***
     * Initializes [ChatClient] for a specific user. The [tokenProvider] implementation is used
     * for the initial token, and it's also invoked whenever the user's token has expired, to
     * fetch a new token.
     *
     * This method performs required operations before connecting with the Stream API.
     * Moreover, it warms up the connection, sets up notifications, and connects to the socket.
     * You can use [listener] to get updates about socket connection.
     *
     * @param user the user to set
     * @param tokenProvider a [TokenProvider] implementation
     * @param listener socket connection listener
     */
    public fun setUser(
        user: User,
        tokenProvider: TokenProvider,
        listener: InitConnectionListener? = null
    ) {
        if (!ensureUserNotSet(listener)) {
            return
        }
        state.user = user
        // fire a handler here that the chatDomain and chatUI can use
        for (preSetUserListener in preSetUserListeners) {
            preSetUserListener(user)
        }
        connectionListener = listener
        config.isAnonymous = false
        tokenManager.setTokenProvider(tokenProvider)

        warmUp()
        notifications.onSetUser()
        getTokenAndConnect {
            socket.connect(user)
        }
    }

    public fun setAnonymousUser(listener: InitConnectionListener? = null) {
        connectionListener = listener
        config.isAnonymous = true
        warmUp()
        getTokenAndConnect {
            socket.connectAnonymously()
        }
    }

    public fun getGuestToken(userId: String, userName: String): Call<GuestUser> {
        return api.getGuestUser(userId, userName)
    }

    public fun sendFile(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback
    ) {
        api.sendFile(channelType, channelId, file, callback)
    }

    public fun sendImage(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback
    ) {
        api.sendImage(channelType, channelId, file, callback)
    }

    public fun sendFile(
        channelType: String,
        channelId: String,
        file: File
    ): Call<String> {
        return api.sendFile(channelType, channelId, file)
    }

    public fun queryMembers(
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySort<Member> = DEFAULT_SORT,
        members: List<Member> = emptyList()
    ): Call<List<Member>> {
        return api.queryMembers(channelType, channelId, offset, limit, filter, sort, members)
    }

    public fun sendImage(
        channelType: String,
        channelId: String,
        file: File
    ): Call<String> {
        return api.sendImage(channelType, channelId, file)
    }

    public fun deleteFile(channelType: String, channelId: String, url: String): Call<Unit> {
        return api.deleteFile(channelType, channelId, url)
    }

    public fun deleteImage(channelType: String, channelId: String, url: String): Call<Unit> {
        return api.deleteImage(channelType, channelId, url)
    }

    public fun replayEvents(
        channelIds: List<String>,
        since: Date?,
        limit: Int,
        offset: Int
    ): Call<List<ChatEvent>> {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    //region Reactions
    public fun getReactions(
        messageId: String,
        offset: Int,
        limit: Int
    ): Call<List<Reaction>> {
        return api.getReactions(messageId, offset, limit)
    }

    public fun sendReaction(messageId: String, reactionType: String): Call<Reaction> {
        return api.sendReaction(messageId, reactionType)
    }

    public fun deleteReaction(messageId: String, reactionType: String): Call<Message> {
        return api.deleteReaction(messageId, reactionType)
    }

    public fun sendReaction(reaction: Reaction): Call<Reaction> {
        return api.sendReaction(reaction)
    }
    //endregion

    //endregion

    public fun disconnectSocket() {
        socket.disconnect()
    }

    public fun reconnectSocket() {
        val user = state.user
        if (user != null) socket.connect(user)
    }

    public fun addSocketListener(listener: SocketListener) {
        socket.addListener(listener)
    }

    public fun removeSocketListener(listener: SocketListener) {
        socket.removeListener(listener)
    }

    @Deprecated(
        message = "Use subscribe() on the client directly instead",
        level = DeprecationLevel.WARNING
    )
    public fun events(): ChatObservable {
        return socket.events()
    }

    public fun subscribe(
        listener: (event: ChatEvent) -> Unit
    ): Disposable {
        return eventsObservable.subscribe(listener = listener)
    }

    /***
     * Subscribes to the specific [eventTypes] of the client.
     *
     * @see [io.getstream.chat.android.client.models.EventType] for type constants
     */
    public fun subscribeFor(
        vararg eventTypes: String,
        listener: (event: ChatEvent) -> Unit
    ): Disposable {
        val filter = { event: ChatEvent ->
            event.type in eventTypes
        }
        return eventsObservable.subscribe(filter, listener)
    }

    /***
     * Subscribes to the specific [eventTypes] of the client, in the lifecycle of [lifecycleOwner].
     *
     * Only receives events when the lifecycle is in a STARTED state, otherwise events are dropped.
     */
    public fun subscribeFor(
        lifecycleOwner: LifecycleOwner,
        vararg eventTypes: String,
        listener: (event: ChatEvent) -> Unit
    ): Disposable {
        val disposable = subscribeFor(
            *eventTypes,
            listener = { event ->
                if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    listener(event)
                }
            }
        )

        lifecycleOwner.lifecycle.addObserver(
            object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                fun onDestroy() {
                    disposable.dispose()
                }
            }
        )

        return disposable
    }

    /***
     * Subscribes to the specific [eventTypes] of the client.
     */
    public fun subscribeFor(
        vararg eventTypes: Class<out ChatEvent>,
        listener: (event: ChatEvent) -> Unit
    ): Disposable {
        val filter = { event: ChatEvent ->
            eventTypes.any { type -> type.isInstance(event) }
        }
        return eventsObservable.subscribe(filter, listener)
    }

    /***
     * Subscribes to the specific [eventTypes] of the client, in the lifecycle of [lifecycleOwner].
     *
     * Only receives events when the lifecycle is in a STARTED state, otherwise events are dropped.
     */
    public fun subscribeFor(
        lifecycleOwner: LifecycleOwner,
        vararg eventTypes: Class<out ChatEvent>,
        listener: (event: ChatEvent) -> Unit
    ): Disposable {
        val disposable = subscribeFor(
            *eventTypes,
            listener = { event ->
                if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    listener(event)
                }
            }
        )

        lifecycleOwner.lifecycle.addObserver(
            object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                fun onDestroy() {
                    disposable.dispose()
                }
            }
        )

        return disposable
    }

    /***
     * Subscribes for the next event with the given [eventType].
     */
    public fun subscribeForSingle(
        eventType: String,
        listener: (event: ChatEvent) -> Unit
    ): Disposable {
        val filter = { event: ChatEvent ->
            event.type == eventType
        }
        return eventsObservable.subscribeSingle(filter, listener)
    }

    /***
     * Subscribes for the next event with the given [eventType].
     */
    public fun <T : ChatEvent> subscribeForSingle(
        eventType: Class<T>,
        listener: (event: T) -> Unit
    ): Disposable {
        val filter = { event: ChatEvent ->
            eventType.isInstance(event)
        }
        return eventsObservable.subscribeSingle(filter) { event: ChatEvent ->
            @Suppress("UNCHECKED_CAST")
            listener.invoke(event as T)
        }
    }

    public fun disconnect() {

        // fire a handler here that the chatDomain and chatUI can use
        for (listener in disconnectListeners) {
            listener(state.user)
        }
        connectionListener = null
        socket.disconnect()
        state.reset()
    }

    //region: api calls

    public fun getDevices(): Call<List<Device>> {
        return api.getDevices()
    }

    public fun deleteDevice(deviceId: String): Call<Unit> {
        return api.deleteDevice(deviceId)
    }

    public fun addDevice(deviceId: String): Call<Unit> {
        return api.addDevice(deviceId)
    }

    public fun searchMessages(request: SearchMessagesRequest): Call<List<Message>> {
        return api.searchMessages(request)
    }

    public fun getReplies(messageId: String, limit: Int): Call<List<Message>> {
        return api.getReplies(messageId, limit)
    }

    public fun getRepliesMore(
        messageId: String,
        firstId: String,
        limit: Int
    ): Call<List<Message>> {
        return api.getRepliesMore(messageId, firstId, limit)
    }

    public fun sendAction(request: SendActionRequest): Call<Message> {
        return api.sendAction(request)
    }

    public fun deleteMessage(messageId: String): Call<Message> {
        return api.deleteMessage(messageId)
    }

    public fun getMessage(messageId: String): Call<Message> {
        return api.getMessage(messageId)
    }

    public fun sendMessage(
        channelType: String,
        channelId: String,
        message: Message
    ): Call<Message> {
        return api.sendMessage(channelType, channelId, message)
    }

    public fun updateMessage(
        message: Message
    ): Call<Message> {
        return api.updateMessage(message)
    }

    public fun queryChannel(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest
    ): Call<Channel> {
        // for convenience we add the message.cid field
        return api.queryChannel(channelType, channelId, request)
            .map { channel ->
                channel.messages.forEach { message -> message.cid = channel.cid }
                channel
            }
    }

    public fun queryChannels(request: QueryChannelsRequest): Call<List<Channel>> {
        return api.queryChannels(request).map { channels ->
            channels.map { channel ->
                channel.messages.forEach { message ->
                    message.cid = channel.cid
                }
            }
            channels
        }
    }

    public fun deleteChannel(channelType: String, channelId: String): Call<Channel> {
        return api.deleteChannel(channelType, channelId)
    }

    public fun markMessageRead(
        channelType: String,
        channelId: String,
        messageId: String
    ): Call<Unit> {
        return api.markRead(channelType, channelId, messageId)
    }

    public fun showChannel(channelType: String, channelId: String): Call<Unit> {
        return api.showChannel(channelType, channelId)
    }

    public fun hideChannel(
        channelType: String,
        channelId: String,
        clearHistory: Boolean = false
    ): Call<Unit> {
        return api.hideChannel(channelType, channelId, clearHistory)
    }

    public fun stopWatching(channelType: String, channelId: String): Call<Unit> {
        return api.stopWatching(channelType, channelId)
    }

    public fun updateChannel(
        channelType: String,
        channelId: String,
        updateMessage: Message?,
        channelExtraData: Map<String, Any> = emptyMap()
    ): Call<Channel> =
        api.updateChannel(
            channelType,
            channelId,
            UpdateChannelRequest(channelExtraData, updateMessage)
        )

    public fun enableSlowMode(
        channelType: String,
        channelId: String,
        cooldownTimeInSeconds: Int
    ): Call<Channel> =
        api.enableSlowMode(channelType, channelId, cooldownTimeInSeconds)

    public fun disableSlowMode(
        channelType: String,
        channelId: String
    ): Call<Channel> =
        api.disableSlowMode(channelType, channelId)

    public fun rejectInvite(channelType: String, channelId: String): Call<Channel> {
        return api.rejectInvite(channelType, channelId)
    }

    public fun sendEvent(
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any> = emptyMap()
    ): Call<ChatEvent> {
        return api.sendEvent(eventType, channelType, channelId, extraData)
    }

    public fun getVersion(): String {
        return BuildConfig.STREAM_CHAT_CLIENT_VERSION + "-" + BuildConfig.BUILD_TYPE
    }

    public fun acceptInvite(
        channelType: String,
        channelId: String,
        message: String
    ): Call<Channel> {
        return api.acceptInvite(channelType, channelId, message)
    }

    public fun markAllRead(): Call<Unit> {
        return api.markAllRead()
    }

    public fun markRead(channelType: String, channelId: String): Call<Unit> {
        return api.markRead(channelType, channelId)
    }

    public fun updateUsers(users: List<User>): Call<List<User>> {
        return api.updateUsers(users)
    }

    public fun updateUser(user: User): Call<User> {
        return updateUsers(listOf(user)).map { it.first() }
    }

    public fun queryUsers(query: QueryUsersRequest): Call<List<User>> {
        return api.queryUsers(query)
    }

    public fun addMembers(
        channelType: String,
        channelId: String,
        members: List<String>
    ): Call<Channel> {
        return api.addMembers(
            channelType,
            channelId,
            members
        )
    }

    public fun removeMembers(
        channelType: String,
        channelId: String,
        members: List<String>
    ): Call<Channel> = api.removeMembers(
        channelType,
        channelId,
        members
    )

    public fun muteUser(userId: String): Call<Mute> = api.muteUser(userId)

    public fun muteChannel(channelType: String, channelId: String): Call<Unit> {
        return api.muteChannel(channelType, channelId)
    }

    public fun unMuteChannel(channelType: String, channelId: String): Call<Unit> {
        return api.unMuteChannel(channelType, channelId)
    }

    public fun unmuteUser(userId: String): Call<Mute> = api.unmuteUser(userId)

    public fun unmuteCurrentUser(): Call<Mute> = api.unmuteCurrentUser()

    public fun muteCurrentUser(): Call<Mute> = api.muteCurrentUser()

    @Deprecated(
        message = "We are going to replace with flagUser()",
        replaceWith = ReplaceWith("this.flagUser(userId)")
    )
    public fun flag(userId: String): Call<Flag> = flagUser(userId)

    public fun flagUser(userId: String): Call<Flag> = api.flagUser(userId)

    public fun flagMessage(messageId: String): Call<Flag> = api.flagMessage(messageId)

    public fun translate(messageId: String, language: String): Call<Message> =
        api.translate(messageId, language)

    public fun banUser(
        targetId: String,
        channelType: String,
        channelId: String,
        reason: String,
        timeout: Int
    ): Call<Unit> = api.banUser(
        targetId,
        timeout,
        reason,
        channelType,
        channelId
    ).map {
        Unit
    }

    public fun unBanUser(
        targetId: String,
        channelType: String,
        channelId: String
    ): Call<Unit> = api.unBanUser(
        targetId,
        channelType,
        channelId
    ).map {
        Unit
    }

    //endregion

    public fun onMessageReceived(remoteMessage: RemoteMessage) {
        notifications.onFirebaseMessage(remoteMessage)
    }

    public fun onNewTokenReceived(token: String) {
        notifications.setFirebaseToken(token)
    }

    public fun getConnectionId(): String? {
        return state.connectionId
    }

    public fun getCurrentUser(): User? {
        return state.user
    }

    public fun getCurrentToken(): String {
        return state.token
    }

    public fun isSocketConnected(): Boolean {
        return state.socketConnected
    }

    /***
     * Returns a [ChannelClient] for given type and id
     *
     * @param channelType the channel type. ie messaging
     * @param channelId the channel id. ie 123
     */
    public fun channel(channelType: String, channelId: String): ChannelClient {
        return ChannelClient(channelType, channelId, this)
    }

    /***
     * Returns a [ChannelClient] for given cid
     *
     * @param cid the full channel id. ie messaging:123
     */
    public fun channel(cid: String): ChannelClient {
        val type = cid.split(":")[0]
        val id = cid.split(":")[1]
        return channel(type, id)
    }

    public fun createChannel(
        channelType: String,
        channelId: String,
        extraData: Map<String, Any>
    ): Call<Channel> =
        createChannel(channelType, channelId, emptyList(), extraData)

    public fun createChannel(
        channelType: String,
        channelId: String,
        members: List<String>
    ): Call<Channel> =
        createChannel(channelType, channelId, members, emptyMap())

    public fun createChannel(channelType: String, members: List<String>): Call<Channel> =
        createChannel(channelType, "", members, emptyMap())

    public fun createChannel(
        channelType: String,
        members: List<String>,
        extraData: Map<String, Any>
    ): Call<Channel> =
        createChannel(channelType, "", members, extraData)

    public fun createChannel(
        channelType: String,
        channelId: String,
        members: List<String>,
        extraData: Map<String, Any>
    ): Call<Channel> =
        queryChannel(
            channelType,
            channelId,
            QueryChannelRequest().withData(extraData + mapOf(ModelFields.MEMBERS to members))
        )

    public fun getSyncHistory(
        channelsIds: List<String>,
        lastSyncAt: Date
    ): Call<List<ChatEvent>> {
        return api.getSyncHistory(channelsIds, lastSyncAt)
    }

    private fun callConnectionListener(connectedEvent: ConnectedEvent?, error: ChatError?) {
        if (connectedEvent != null) {
            val user = connectedEvent.me
            val connectionId = connectedEvent.connectionId
            connectionListener?.onSuccess(InitConnectionListener.ConnectionData(user, connectionId))
        } else if (error != null) {
            connectionListener?.onError(error)
        }
        connectionListener = null
    }

    private fun getTokenAndConnect(connect: () -> Unit) {
        tokenManager.loadAsync {
            if (it.isSuccess) {
                state.token = it.data()
            }
            connect()
        }
    }

    private fun warmUp() {
        if (config.warmUp) {
            api.warmUp()
        }
    }

    private fun ensureUserNotSet(listener: InitConnectionListener?): Boolean {
        return if (state.user != null) {
            logger.logE("Trying to set user without disconnecting the previous one - make sure that previously set user is disconnected.")
            listener?.onError(ChatError("User cannot be set until previous one is disconnected."))
            false
        } else {
            true
        }
    }

    public class Builder(private val apiKey: String, private val appContext: Context) {

        private var baseUrl: String = "chat-us-east-1.stream-io-api.com"
        private var cdnUrl: String = baseUrl
        private var baseTimeout = 10000L
        private var cdnTimeout = 10000L
        private var logLevel = ChatLogLevel.ALL
        private var warmUp: Boolean = true
        private var loggerHandler: ChatLoggerHandler? = null
        private var notificationsHandler: ChatNotificationHandler =
            ChatNotificationHandler(appContext)
        private var fileUploader: FileUploader? = null
        private val tokenManager: TokenManager = TokenManagerImpl()

        public fun logLevel(level: ChatLogLevel): Builder {
            logLevel = level
            return this
        }

        public fun logLevel(level: String): Builder {
            logLevel = ChatLogLevel.valueOf(level)
            return this
        }

        public fun loggerHandler(loggerHandler: ChatLoggerHandler): Builder {
            this.loggerHandler = loggerHandler
            return this
        }

        public fun notifications(notificationsHandler: ChatNotificationHandler): Builder {
            this.notificationsHandler = notificationsHandler
            return this
        }

        public fun fileUploader(fileUploader: FileUploader): Builder {
            this.fileUploader = fileUploader
            return this
        }

        public fun baseTimeout(timeout: Long): Builder {
            baseTimeout = timeout
            return this
        }

        public fun cdnTimeout(timeout: Long): Builder {
            cdnTimeout = timeout
            return this
        }

        public fun disableWarmUp(): Builder = apply {
            warmUp = false
        }

        public fun baseUrl(value: String): Builder {
            var baseUrl = value
            if (baseUrl.startsWith("https://")) {
                baseUrl = baseUrl.split("https://").toTypedArray()[1]
            }
            if (baseUrl.startsWith("http://")) {
                baseUrl = baseUrl.split("http://").toTypedArray()[1]
            }
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length - 1)
            }
            this.baseUrl = baseUrl
            return this
        }

        public fun cdnUrl(value: String): Builder {
            var cdnUrl = value
            if (cdnUrl.startsWith("https://")) {
                cdnUrl = cdnUrl.split("https://").toTypedArray()[1]
            }
            if (cdnUrl.startsWith("http://")) {
                cdnUrl = cdnUrl.split("http://").toTypedArray()[1]
            }
            if (cdnUrl.endsWith("/")) {
                cdnUrl = cdnUrl.substring(0, cdnUrl.length - 1)
            }
            this.cdnUrl = cdnUrl
            return this
        }

        public fun build(): ChatClient {

            if (apiKey.isEmpty()) {
                throw IllegalStateException("apiKey is not defined in " + this::class.java.simpleName)
            }

            val config = ChatClientConfig(
                apiKey,
                "https://$baseUrl/",
                "https://$cdnUrl/",
                "wss://$baseUrl/",
                baseTimeout,
                cdnTimeout,
                warmUp,
                ChatLogger.Config(logLevel, loggerHandler),

            )

            val module = ChatModule(appContext, config, notificationsHandler, fileUploader, tokenManager)

            val result = ChatClient(
                config,
                module.api(),
                module.socket(),
                module.notifications(),
                tokenManager
            )
            instance = result

            return result
        }
    }

    public companion object {
        private var instance: ChatClient? = null
        @JvmField
        public val DEFAULT_SORT: QuerySort<Member> = QuerySort.desc("last_updated")

        @JvmStatic
        public fun instance(): ChatClient {
            return instance ?: throw IllegalStateException("ChatClient.Builder::build() must be called before obtaining ChatClient instance")
        }

        public val isInitialized: Boolean
            get() = instance != null
    }
}
