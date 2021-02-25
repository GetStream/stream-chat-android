@file:Suppress("DEPRECATION_ERROR")

package io.getstream.chat.android.client

import android.content.Context
import android.util.Base64
import androidx.annotation.CheckResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
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
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.call.map
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.clientstate.ClientState
import io.getstream.chat.android.client.clientstate.ClientStateService
import io.getstream.chat.android.client.di.ChatModule
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.extensions.ATTACHMENT_TYPE_FILE
import io.getstream.chat.android.client.extensions.ATTACHMENT_TYPE_IMAGE
import io.getstream.chat.android.client.extensions.isValid
import io.getstream.chat.android.client.helpers.QueryChannelsPostponeHelper
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.logger.ChatLoggerHandler
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ConnectionData
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.Filters
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
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
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
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.observable.ChatEventsObservable
import io.getstream.chat.android.client.utils.observable.ChatObservable
import io.getstream.chat.android.client.utils.observable.Disposable
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.internal.exhaustive
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Calendar
import java.util.Date

/**
 * The ChatClient is the main entry point for all low-level operations on chat
 */
@Suppress("NEWER_VERSION_IN_SINCE_KOTLIN")
public class ChatClient internal constructor(
    public val config: ChatClientConfig,
    private val api: ChatApi,
    private val socket: ChatSocket,
    private val notifications: ChatNotifications,
    private val tokenManager: TokenManager = TokenManagerImpl(),
    private val clientStateService: ClientStateService = ClientStateService(),
) {
    private val queryChannelsPostponeHelper = QueryChannelsPostponeHelper(api, clientStateService)

    @InternalStreamChatApi
    public val notificationHandler: ChatNotificationHandler = notifications.handler

    private var connectionListener: InitConnectionListener? = null
    private val logger = ChatLogger.get("Client")
    private val eventsObservable = ChatEventsObservable(socket, this)
    private val lifecycleObserver = StreamLifecycleObserver(
        object : LifecycleHandler {
            override fun resume() = reconnectSocket()
            override fun stopped() = disconnectSocket()
        }
    )

    public val disconnectListeners: MutableList<(User?) -> Unit> = mutableListOf()
    public val preSetUserListeners: MutableList<(User) -> Unit> = mutableListOf()

    init {
        eventsObservable.subscribe { event ->

            notifications.onChatEvent(event)

            when (event) {
                is ConnectedEvent -> {
                    val user = event.me
                    val connectionId = event.connectionId
                    clientStateService.onConnected(user, connectionId)
                    api.setConnection(user.id, connectionId)
                    lifecycleObserver.observe()
                    notifications.onSetUser()
                }
                is DisconnectedEvent -> {
                    clientStateService.onDisconnected()
                }
            }
        }
        logger.logI("Initialised: " + getVersion())
    }

    //region Set user

    /**
     * Creates a [Call] implementation that wraps a call that would otherwise be
     * asynchronous and provide results to an [InitConnectionListener].
     *
     * @param performCall This should perform the call, passing in the
     *                    [initListener] to it.
     */
    private fun createInitListenerCall(
        performCall: (initListener: InitConnectionListener) -> Unit,
    ): Call<ConnectionData> {
        return object : Call<ConnectionData> {
            override fun execute(): Result<ConnectionData> {
                // Uses coroutines to turn the async call into blocking
                return runBlocking { await() }
            }

            override fun enqueue(callback: Call.Callback<ConnectionData>) {
                // Converts InitConnectionListener to Call.Callback
                performCall(
                    object : InitConnectionListener() {
                        override fun onSuccess(data: ConnectionData) {
                            val connectionData =
                                io.getstream.chat.android.client.models.ConnectionData(data.user, data.connectionId)
                            callback.onResult(Result(connectionData))
                        }

                        override fun onError(error: ChatError) {
                            callback.onResult(Result(error))
                        }
                    }
                )
            }

            override fun cancel() {}
        }
    }

    /**
     * Initializes [ChatClient] for a specific user using the given user [token].
     *
     * @see ChatClient.setUser with [TokenProvider] for advanced use cases
     */
    @Deprecated(
        message = "Use connectUser instead",
        replaceWith = ReplaceWith("this.connectUser(user, token).enqueue { result -> TODO(\"Handle result\") })")
    )
    public fun setUser(user: User, token: String, listener: InitConnectionListener? = null) {
        setUser(user, ImmediateTokenProvider(token), listener)
    }

    /**
     * Initializes [ChatClient] for a specific user using the given user [token].
     *
     * @see ChatClient.connectUser with [TokenProvider] parameter for advanced use cases
     */
    @CheckResult
    public fun connectUser(user: User, token: String): Call<ConnectionData> {
        return connectUser(user, ImmediateTokenProvider(token))
    }

    /**
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
    @Deprecated(
        message = "Use connectUser instead",
        replaceWith = ReplaceWith("this.connectUser(user, tokenProvider).enqueue { result -> TODO(\"Handle result\") })")
    )
    public fun setUser(
        user: User,
        tokenProvider: TokenProvider,
        listener: InitConnectionListener? = null,
    ) {
        if (!ensureUserNotSet(listener)) {
            return
        }
        initializeClientWithUser(user, tokenProvider)
        connectionListener = listener
        getTokenAndConnect {
            socket.connect(user)
        }
    }

    private fun initializeClientWithUser(
        user: User,
        tokenProvider: TokenProvider,
    ) {
        clientStateService.onSetUser(user)
        // fire a handler here that the chatDomain and chatUI can use
        notifySetUser(user)
        config.isAnonymous = false
        tokenManager.setTokenProvider(tokenProvider)
        warmUp()
    }

    /**
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
     */
    @CheckResult
    public fun connectUser(user: User, tokenProvider: TokenProvider): Call<ConnectionData> {
        return createInitListenerCall { initListener -> setUser(user, tokenProvider, initListener) }
    }

    /**
     * Initializes [ChatClient] for a specific user and a given [userToken].
     * Caution: This method doesn't establish connection to the web socket, you should use [connectUser] instead.
     *
     * This method initializes [ChatClient] to allow the use of Stream REST API client.
     * Moreover, it warms up the connection, and sets up notifications.
     *
     * @param user the user to set
     * @param userToken the user token
     */
    @InternalStreamChatApi
    public fun setUserWithoutConnecting(user: User, userToken: String) {
        if (isUserSet()) {
            return
        }
        initializeClientWithUser(user, ImmediateTokenProvider(userToken))
    }

    private fun notifySetUser(user: User) {
        preSetUserListeners.forEach { it(user) }
    }

    @Deprecated(
        message = "Use connectAnonymousUser instead",
        replaceWith = ReplaceWith("this.connectAnonymousUser().enqueue { result -> TODO(\"Handle result\") })")
    )
    public fun setAnonymousUser(listener: InitConnectionListener? = null) {
        clientStateService.onSetAnonymousUser()
        connectionListener = object : InitConnectionListener() {
            override fun onSuccess(data: ConnectionData) {
                notifySetUser(data.user)
                listener?.onSuccess(data)
            }

            override fun onError(error: ChatError) {
                listener?.onError(error)
            }
        }
        config.isAnonymous = true
        warmUp()
        getTokenAndConnect {
            socket.connectAnonymously()
        }
    }

    @CheckResult
    public fun connectAnonymousUser(): Call<ConnectionData> {
        return createInitListenerCall { initListener -> setAnonymousUser(initListener) }
    }

    @Deprecated(
        message = "Use connectGuestUser instead",
        replaceWith = ReplaceWith("this.connectGuestUser(userId, username).enqueue { result -> TODO(\"Handle result\") })")
    )
    public fun setGuestUser(userId: String, username: String, listener: InitConnectionListener? = null) {
        getGuestToken(userId, username).enqueue {
            if (it.isSuccess) {
                setUser(it.data().user, it.data().token, listener)
            } else {
                listener?.onError(it.error())
            }
        }
    }

    @CheckResult
    public fun connectGuestUser(userId: String, username: String): Call<ConnectionData> {
        return createInitListenerCall { initListener -> setGuestUser(userId, username, initListener) }
    }

    @CheckResult
    public fun getGuestToken(userId: String, userName: String): Call<GuestUser> {
        return api.getGuestUser(userId, userName)
    }

    @CheckResult
    public fun sendFile(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback? = null,
    ): Call<String> {
        return api.sendFile(channelType, channelId, file, callback)
    }

    @CheckResult
    public fun sendImage(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback? = null,
    ): Call<String> {
        return api.sendImage(channelType, channelId, file, callback)
    }

    @CheckResult
    public fun queryMembers(
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySort<Member>,
        members: List<Member>,
    ): Call<List<Member>> {
        return api.queryMembers(channelType, channelId, offset, limit, filter, sort, members)
    }

    @CheckResult
    public fun deleteFile(channelType: String, channelId: String, url: String): Call<Unit> {
        return api.deleteFile(channelType, channelId, url)
    }

    @CheckResult
    public fun deleteImage(channelType: String, channelId: String, url: String): Call<Unit> {
        return api.deleteImage(channelType, channelId, url)
    }

    //region Reactions
    @CheckResult
    public fun getReactions(
        messageId: String,
        offset: Int,
        limit: Int,
    ): Call<List<Reaction>> {
        return api.getReactions(messageId, offset, limit)
    }

    @CheckResult
    @JvmOverloads
    public fun sendReaction(messageId: String, reactionType: String, enforceUnique: Boolean = false): Call<Reaction> {
        return api.sendReaction(messageId, reactionType, enforceUnique)
    }

    @CheckResult
    public fun deleteReaction(messageId: String, reactionType: String): Call<Message> {
        return api.deleteReaction(messageId, reactionType)
    }

    @CheckResult
    @JvmOverloads
    public fun sendReaction(reaction: Reaction, enforceUnique: Boolean = false): Call<Reaction> {
        return api.sendReaction(reaction, enforceUnique)
    }
    //endregion

    //endregion

    public fun disconnectSocket() {
        socket.disconnect()
    }

    public fun reconnectSocket() {
        when (val state = clientStateService.state) {
            is ClientState.Anonymous -> socket.connectAnonymously()
            is ClientState.User -> socket.connect(state.user)
            is ClientState.Idle -> {
            }
        }.exhaustive
    }

    public fun addSocketListener(listener: SocketListener) {
        socket.addListener(listener)
    }

    public fun removeSocketListener(listener: SocketListener) {
        socket.removeListener(listener)
    }

    @Deprecated(
        message = "Use subscribe() on the client directly instead",
        level = DeprecationLevel.ERROR,
    )
    @Suppress("DEPRECATION_ERROR")
    public fun events(): ChatObservable {
        return socket.events()
    }

    @Deprecated(message = "Use subscribe with ChatEventListener parameter")
    @SinceKotlin("99999.9")
    public fun subscribe(
        listener: (event: ChatEvent) -> Unit,
    ): Disposable {
        return eventsObservable.subscribe(listener = listener)
    }

    public fun subscribe(
        listener: ChatEventListener<ChatEvent>,
    ): Disposable {
        return eventsObservable.subscribe(listener = listener)
    }

    /**
     * Subscribes to the specific [eventTypes] of the client.
     *
     * @see [io.getstream.chat.android.client.models.EventType] for type constants
     */
    @Deprecated(message = "Use subscribeFor with ChatEventListener parameter")
    @SinceKotlin("99999.9")
    public fun subscribeFor(
        vararg eventTypes: String,
        listener: (event: ChatEvent) -> Unit,
    ): Disposable {
        val filter = { event: ChatEvent ->
            event.type in eventTypes
        }
        return eventsObservable.subscribe(filter, listener)
    }

    /**
     * Subscribes to the specific [eventTypes] of the client.
     *
     * @see [io.getstream.chat.android.client.models.EventType] for type constants
     */
    public fun subscribeFor(
        vararg eventTypes: String,
        listener: ChatEventListener<ChatEvent>,
    ): Disposable {
        val filter = { event: ChatEvent ->
            event.type in eventTypes
        }
        return eventsObservable.subscribe(filter, listener)
    }

    /**
     * Subscribes to the specific [eventTypes] of the client, in the lifecycle of [lifecycleOwner].
     *
     * Only receives events when the lifecycle is in a STARTED state, otherwise events are dropped.
     */
    @Deprecated(message = "Use subscribeFor with ChatEventListener parameter")
    @SinceKotlin("99999.9")
    public fun subscribeFor(
        lifecycleOwner: LifecycleOwner,
        vararg eventTypes: String,
        listener: (event: ChatEvent) -> Unit,
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

    /**
     * Subscribes to the specific [eventTypes] of the client, in the lifecycle of [lifecycleOwner].
     *
     * Only receives events when the lifecycle is in a STARTED state, otherwise events are dropped.
     */
    public fun subscribeFor(
        lifecycleOwner: LifecycleOwner,
        vararg eventTypes: String,
        listener: ChatEventListener<ChatEvent>,
    ): Disposable {
        val disposable = subscribeFor(
            *eventTypes,
            listener = { event ->
                if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    listener.onEvent(event)
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

    /**
     * Subscribes to the specific [eventTypes] of the client.
     */
    @Deprecated("Use subscribeFor with ChatEventListener parameter")
    @SinceKotlin("99999.9")
    public fun subscribeFor(
        vararg eventTypes: Class<out ChatEvent>,
        listener: (event: ChatEvent) -> Unit,
    ): Disposable {
        val filter = { event: ChatEvent ->
            eventTypes.any { type -> type.isInstance(event) }
        }
        return eventsObservable.subscribe(filter, listener)
    }

    /**
     * Subscribes to the specific [eventTypes] of the client.
     */
    public fun subscribeFor(
        vararg eventTypes: Class<out ChatEvent>,
        listener: ChatEventListener<ChatEvent>,
    ): Disposable {
        val filter = { event: ChatEvent ->
            eventTypes.any { type -> type.isInstance(event) }
        }
        return eventsObservable.subscribe(filter, listener)
    }

    /**
     * Subscribes to the specific [eventTypes] of the client, in the lifecycle of [lifecycleOwner].
     *
     * Only receives events when the lifecycle is in a STARTED state, otherwise events are dropped.
     */
    @Deprecated("Use subscribeFor with ChatEventListener parameter")
    @SinceKotlin("99999.9")
    public fun subscribeFor(
        lifecycleOwner: LifecycleOwner,
        vararg eventTypes: Class<out ChatEvent>,
        listener: (event: ChatEvent) -> Unit,
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

    /**
     * Subscribes to the specific [eventTypes] of the client, in the lifecycle of [lifecycleOwner].
     *
     * Only receives events when the lifecycle is in a STARTED state, otherwise events are dropped.
     */
    public fun subscribeFor(
        lifecycleOwner: LifecycleOwner,
        vararg eventTypes: Class<out ChatEvent>,
        listener: ChatEventListener<ChatEvent>,
    ): Disposable {
        val disposable = subscribeFor(
            *eventTypes,
            listener = { event ->
                if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    listener.onEvent(event)
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

    /**
     * Subscribes for the next event with the given [eventType].
     */
    @Deprecated("Use subscribeForSingle with ChatEventListener parameter")
    @SinceKotlin("99999.9")
    public fun subscribeForSingle(
        eventType: String,
        listener: (event: ChatEvent) -> Unit,
    ): Disposable {
        val filter = { event: ChatEvent ->
            event.type == eventType
        }
        return eventsObservable.subscribeSingle(filter, listener)
    }

    /**
     * Subscribes for the next event with the given [eventType].
     */
    public fun subscribeForSingle(
        eventType: String,
        listener: ChatEventListener<ChatEvent>,
    ): Disposable {
        val filter = { event: ChatEvent ->
            event.type == eventType
        }
        return eventsObservable.subscribeSingle(filter, listener)
    }

    /**
     * Subscribes for the next event with the given [eventType].
     */
    @Deprecated("Use subscribeForSingle with ChatEventListener parameter")
    @SinceKotlin("99999.9")
    public fun <T : ChatEvent> subscribeForSingle(
        eventType: Class<T>,
        listener: (event: T) -> Unit,
    ): Disposable {
        val filter = { event: ChatEvent ->
            eventType.isInstance(event)
        }
        return eventsObservable.subscribeSingle(filter) { event ->
            @Suppress("UNCHECKED_CAST")
            listener(event as T)
        }
    }

    /**
     * Subscribes for the next event with the given [eventType].
     */
    public fun <T : ChatEvent> subscribeForSingle(
        eventType: Class<T>,
        listener: ChatEventListener<T>,
    ): Disposable {
        val filter = { event: ChatEvent ->
            eventType.isInstance(event)
        }
        return eventsObservable.subscribeSingle(filter) { event ->
            @Suppress("UNCHECKED_CAST")
            listener.onEvent(event as T)
        }
    }

    public fun disconnect() {

        // fire a handler here that the chatDomain and chatUI can use
        runCatching {
            clientStateService.state.userOrError().let { user ->
                disconnectListeners.forEach { listener -> listener(user) }
            }
        }
        connectionListener = null
        clientStateService.onDisconnectRequested()
        socket.disconnect()
    }

    //region: api calls

    @CheckResult
    public fun getDevices(): Call<List<Device>> {
        return api.getDevices()
    }

    @CheckResult
    public fun deleteDevice(deviceId: String): Call<Unit> {
        return api.deleteDevice(deviceId)
    }

    @CheckResult
    public fun addDevice(deviceId: String): Call<Unit> {
        return api.addDevice(deviceId)
    }

    @CheckResult
    public fun searchMessages(request: SearchMessagesRequest): Call<List<Message>> {
        return api.searchMessages(request)
    }

    @CheckResult
    public fun getFileAttachments(
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
    ): Call<List<Attachment>> =
        getAttachments(channelType, channelId, offset, limit, ATTACHMENT_TYPE_FILE)

    @CheckResult
    public fun getImageAttachments(
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
    ): Call<List<Attachment>> =
        getAttachments(channelType, channelId, offset, limit, ATTACHMENT_TYPE_IMAGE)

    @CheckResult
    private fun getAttachments(
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        type: String,
    ): Call<List<Attachment>> =
        getMessagesWithAttachments(channelType, channelId, offset, limit, type).map { messages ->
            messages.flatMap { message -> message.attachments.filter { it.type == type } }
        }

    /**
     * Returns a [Call<List<Message>>] With messages which contain at least one desired type attachment but
     * not necessarily all of them will have a specified type
     *
     * @param channelType the channel type. ie messaging
     * @param channelId the channel id. ie 123
     * @param offset The messages offset
     * @param limit max limit messages to be fetched
     * @param type The desired type attachment
     */
    @CheckResult
    public fun getMessagesWithAttachments(
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        type: String,
    ): Call<List<Message>> {
        val channelFilter = Filters.`in`("cid", "$channelType:$channelId")
        val messageFilter = Filters.`in`("attachments.type", type)
        return searchMessages(SearchMessagesRequest(offset, limit, channelFilter, messageFilter))
    }

    @CheckResult
    public fun getReplies(messageId: String, limit: Int): Call<List<Message>> {
        return api.getReplies(messageId, limit)
    }

    @CheckResult
    public fun getRepliesMore(
        messageId: String,
        firstId: String,
        limit: Int,
    ): Call<List<Message>> {
        return api.getRepliesMore(messageId, firstId, limit)
    }

    @CheckResult
    public fun sendAction(request: SendActionRequest): Call<Message> {
        return api.sendAction(request)
    }

    @CheckResult
    public fun deleteMessage(messageId: String): Call<Message> {
        return api.deleteMessage(messageId)
    }

    @CheckResult
    public fun getMessage(messageId: String): Call<Message> {
        return api.getMessage(messageId)
    }

    @CheckResult
    public fun sendMessage(
        channelType: String,
        channelId: String,
        message: Message,
    ): Call<Message> {
        return api.sendMessage(channelType, channelId, message)
    }

    @CheckResult
    public fun updateMessage(
        message: Message,
    ): Call<Message> {
        return api.updateMessage(message)
    }

    @CheckResult
    public fun pinMessage(message: Message, expirationDate: Date?): Call<Message> {
        return updateMessage(
            message.apply {
                pinned = true
                pinExpires = expirationDate
            }
        )
    }

    @CheckResult
    public fun pinMessage(message: Message, timeout: Int): Call<Message> {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.SECOND, timeout)
        }
        return updateMessage(
            message.apply {
                pinned = true
                pinExpires = calendar.time
            }
        )
    }

    @CheckResult
    public fun unpinMessage(message: Message): Call<Message> {
        return updateMessage(
            message.apply {
                pinned = false
            }
        )
    }

    @CheckResult
    public fun queryChannel(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ): Call<Channel> {
        return queryChannelsPostponeHelper.queryChannel(channelType, channelId, request)
    }

    @CheckResult
    public fun queryChannels(request: QueryChannelsRequest): Call<List<Channel>> {
        return queryChannelsPostponeHelper.queryChannels(request)
    }

    @CheckResult
    public fun deleteChannel(channelType: String, channelId: String): Call<Channel> {
        return api.deleteChannel(channelType, channelId)
    }

    @CheckResult
    public fun markMessageRead(
        channelType: String,
        channelId: String,
        messageId: String,
    ): Call<Unit> {
        return api.markRead(channelType, channelId, messageId)
    }

    @CheckResult
    public fun showChannel(channelType: String, channelId: String): Call<Unit> {
        return api.showChannel(channelType, channelId)
    }

    @CheckResult
    public fun hideChannel(
        channelType: String,
        channelId: String,
        clearHistory: Boolean = false,
    ): Call<Unit> {
        return api.hideChannel(channelType, channelId, clearHistory)
    }

    @CheckResult
    public fun stopWatching(channelType: String, channelId: String): Call<Unit> {
        return api.stopWatching(channelType, channelId)
    }

    @CheckResult
    public fun updateChannel(
        channelType: String,
        channelId: String,
        updateMessage: Message?,
        channelExtraData: Map<String, Any> = emptyMap(),
    ): Call<Channel> =
        api.updateChannel(
            channelType,
            channelId,
            UpdateChannelRequest(channelExtraData, updateMessage)
        )

    @CheckResult
    public fun enableSlowMode(
        channelType: String,
        channelId: String,
        cooldownTimeInSeconds: Int,
    ): Call<Channel> =
        api.enableSlowMode(channelType, channelId, cooldownTimeInSeconds)

    @CheckResult
    public fun disableSlowMode(
        channelType: String,
        channelId: String,
    ): Call<Channel> =
        api.disableSlowMode(channelType, channelId)

    @CheckResult
    public fun rejectInvite(channelType: String, channelId: String): Call<Channel> {
        return api.rejectInvite(channelType, channelId)
    }

    @CheckResult
    public fun sendEvent(
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any> = emptyMap(),
    ): Call<ChatEvent> {
        return api.sendEvent(eventType, channelType, channelId, extraData)
    }

    public fun getVersion(): String = VERSION_PREFIX + BuildConfig.STREAM_CHAT_VERSION

    @CheckResult
    public fun acceptInvite(
        channelType: String,
        channelId: String,
        message: String?,
    ): Call<Channel> {
        return api.acceptInvite(channelType, channelId, message)
    }

    @CheckResult
    public fun markAllRead(): Call<Unit> {
        return api.markAllRead()
    }

    @CheckResult
    public fun markRead(channelType: String, channelId: String): Call<Unit> {
        return api.markRead(channelType, channelId)
    }

    @CheckResult
    public fun updateUsers(users: List<User>): Call<List<User>> {
        return api.updateUsers(users)
    }

    @CheckResult
    public fun updateUser(user: User): Call<User> {
        return updateUsers(listOf(user)).map { it.first() }
    }

    @CheckResult
    public fun queryUsers(query: QueryUsersRequest): Call<List<User>> {
        return api.queryUsers(query)
    }

    @CheckResult
    public fun addMembers(
        channelType: String,
        channelId: String,
        members: List<String>,
    ): Call<Channel> {
        return api.addMembers(
            channelType,
            channelId,
            members
        )
    }

    @CheckResult
    public fun removeMembers(
        channelType: String,
        channelId: String,
        members: List<String>,
    ): Call<Channel> = api.removeMembers(
        channelType,
        channelId,
        members
    )

    @CheckResult
    public fun muteUser(userId: String): Call<Mute> = api.muteUser(userId)

    @CheckResult
    public fun muteChannel(channelType: String, channelId: String): Call<Unit> {
        return api.muteChannel(channelType, channelId)
    }

    @CheckResult
    public fun unMuteChannel(channelType: String, channelId: String): Call<Unit> {
        return api.unMuteChannel(channelType, channelId)
    }

    @CheckResult
    public fun unmuteUser(userId: String): Call<Unit> = api.unmuteUser(userId)

    @CheckResult
    public fun unmuteCurrentUser(): Call<Unit> = api.unmuteCurrentUser()

    @CheckResult
    public fun muteCurrentUser(): Call<Mute> = api.muteCurrentUser()

    @CheckResult
    @Deprecated(
        message = "We are going to replace with flagUser()",
        replaceWith = ReplaceWith("this.flagUser(userId)"),
        level = DeprecationLevel.ERROR,
    )
    public fun flag(userId: String): Call<Flag> = flagUser(userId)

    @CheckResult
    public fun flagUser(userId: String): Call<Flag> = api.flagUser(userId)

    @CheckResult
    public fun flagMessage(messageId: String): Call<Flag> = api.flagMessage(messageId)

    @CheckResult
    public fun translate(messageId: String, language: String): Call<Message> =
        api.translate(messageId, language)

    @CheckResult
    public fun banUser(
        targetId: String,
        channelType: String,
        channelId: String,
        reason: String?,
        timeout: Int?,
    ): Call<Unit> = api.banUser(
        targetId = targetId,
        channelType = channelType,
        channelId = channelId,
        reason = reason,
        timeout = timeout,
        shadow = false
    ).map {
        Unit
    }

    @CheckResult
    public fun unBanUser(
        targetId: String,
        channelType: String,
        channelId: String,
    ): Call<Unit> = api.unBanUser(
        targetId = targetId,
        channelType = channelType,
        channelId = channelId,
        shadow = false
    ).map {
        Unit
    }

    @CheckResult
    public fun shadowBanUser(
        targetId: String,
        channelType: String,
        channelId: String,
        reason: String?,
        timeout: Int?,
    ): Call<Unit> = api.banUser(
        targetId = targetId,
        channelType = channelType,
        channelId = channelId,
        reason = reason,
        timeout = timeout,
        shadow = true
    ).map {
        Unit
    }

    @CheckResult
    public fun removeShadowBan(
        targetId: String,
        channelType: String,
        channelId: String,
    ): Call<Unit> = api.unBanUser(
        targetId = targetId,
        channelType = channelType,
        channelId = channelId,
        shadow = true
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
        return runCatching { clientStateService.state.connectionIdOrError() }.getOrNull()
    }

    public fun getCurrentUser(): User? {
        return runCatching { clientStateService.state.userOrError() }.getOrNull()
    }

    public fun getCurrentToken(): String? {
        return runCatching { clientStateService.state.tokenOrError() }.getOrNull()
    }

    public fun isSocketConnected(): Boolean {
        return clientStateService.state.let {
            it is ClientState.User.Authorized.Connected ||
                it is ClientState.Anonymous.Authorized.Connected
        }
    }

    /**
     * Returns a [ChannelClient] for given type and id
     *
     * @param channelType the channel type. ie messaging
     * @param channelId the channel id. ie 123
     */
    public fun channel(channelType: String, channelId: String): ChannelClient {
        return ChannelClient(channelType, channelId, this)
    }

    /**
     * Returns a [ChannelClient] for given cid
     *
     * @param cid the full channel id. ie messaging:123
     */
    public fun channel(cid: String): ChannelClient {
        val type = cid.split(":")[0]
        val id = cid.split(":")[1]
        return channel(type, id)
    }

    @CheckResult
    public fun createChannel(
        channelType: String,
        channelId: String,
        extraData: Map<String, Any>,
    ): Call<Channel> =
        createChannel(channelType, channelId, emptyList(), extraData)

    @CheckResult
    public fun createChannel(
        channelType: String,
        channelId: String,
        members: List<String>,
    ): Call<Channel> =
        createChannel(channelType, channelId, members, emptyMap())

    @CheckResult
    public fun createChannel(channelType: String, members: List<String>): Call<Channel> =
        createChannel(channelType, "", members, emptyMap())

    @CheckResult
    public fun createChannel(
        channelType: String,
        members: List<String>,
        extraData: Map<String, Any>,
    ): Call<Channel> =
        createChannel(channelType, "", members, extraData)

    @CheckResult
    public fun createChannel(
        channelType: String,
        channelId: String,
        members: List<String>,
        extraData: Map<String, Any>,
    ): Call<Channel> =
        queryChannel(
            channelType,
            channelId,
            QueryChannelRequest().withData(extraData + mapOf(ModelFields.MEMBERS to members))
        )

    @CheckResult
    public fun getSyncHistory(
        channelsIds: List<String>,
        lastSyncAt: Date,
    ): Call<List<ChatEvent>> {
        return api.getSyncHistory(channelsIds, lastSyncAt)
    }

    internal fun callConnectionListener(connectedEvent: ConnectedEvent?, error: ChatError?) {
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
                clientStateService.onTokenReceived(it.data())
            } else {
                clientStateService.onTokenReceived("")
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
        return if (isUserSet()) {
            logger.logE("Trying to set user without disconnecting the previous one - make sure that previously set user is disconnected.")
            listener?.onError(ChatError("User cannot be set until previous one is disconnected."))
            false
        } else {
            true
        }
    }

    private fun isUserSet() = clientStateService.state !is ClientState.Idle

    private fun isValidRemoteMessage(remoteMessage: RemoteMessage): Boolean =
        notifications.isValidRemoteMessage(remoteMessage)

    public fun devToken(userId: String): String {
        require(userId.isNotEmpty()) { "User id must not be empty" }
        val header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9" //  {"alg": "HS256", "typ": "JWT"}
        val devSignature = "devtoken"
        val payload: String =
            Base64.encodeToString("{\"user_id\":\"$userId\"}".toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
        return "$header.$payload.$devSignature"
    }

    public class Builder(private val apiKey: String, private val appContext: Context) {

        private var baseUrl: String = "chat-us-east-1.stream-io-api.com"
        private var cdnUrl: String = baseUrl
        private var baseTimeout = 10000L
        private var cdnTimeout = 10000L
        private var logLevel = ChatLogLevel.NOTHING
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

            // Should not be set by clients yet, only for development
            config.enableMoshi = false

            val module =
                ChatModule(appContext, config, notificationsHandler, fileUploader, tokenManager)

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
        private const val VERSION_PREFIX = "stream-chat-android-"
        private var instance: ChatClient? = null

        @JvmField
        public val DEFAULT_SORT: QuerySort<Member> = QuerySort.desc("last_updated")

        @JvmStatic
        public fun instance(): ChatClient {
            return instance
                ?: throw IllegalStateException("ChatClient.Builder::build() must be called before obtaining ChatClient instance")
        }

        public val isInitialized: Boolean
            get() = instance != null

        public fun isValidRemoteMessage(
            remoteMessage: RemoteMessage,
            defaultNotificationConfig: NotificationConfig = NotificationConfig(),
        ): Boolean {
            return instance?.isValidRemoteMessage(remoteMessage) ?: remoteMessage.isValid(
                defaultNotificationConfig
            )
        }
    }
}
