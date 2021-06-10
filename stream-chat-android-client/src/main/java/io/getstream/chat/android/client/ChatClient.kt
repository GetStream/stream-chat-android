@file:Suppress("DEPRECATION_ERROR")

package io.getstream.chat.android.client

import android.content.Context
import android.util.Base64
import androidx.annotation.CheckResult
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.api.models.SearchMessagesRequest
import io.getstream.chat.android.client.api.models.SendActionRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.call.map
import io.getstream.chat.android.client.call.toUnitCall
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.clientstate.DisconnectCause
import io.getstream.chat.android.client.clientstate.SocketState
import io.getstream.chat.android.client.clientstate.SocketStateService
import io.getstream.chat.android.client.clientstate.UserState
import io.getstream.chat.android.client.clientstate.UserStateService
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
import io.getstream.chat.android.client.models.BannedUser
import io.getstream.chat.android.client.models.BannedUsersSort
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
import io.getstream.chat.android.client.notifications.PushNotificationReceivedListener
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.client.notifications.storage.EncryptedPushNotificationsConfigStore
import io.getstream.chat.android.client.notifications.storage.PushNotificationsConfig
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.socket.InitConnectionListener
import io.getstream.chat.android.client.socket.SocketListener
import io.getstream.chat.android.client.token.ConstantTokenProvider
import io.getstream.chat.android.client.token.TokenManager
import io.getstream.chat.android.client.token.TokenManagerImpl
import io.getstream.chat.android.client.token.TokenProvider
import io.getstream.chat.android.client.uploader.FileUploader
import io.getstream.chat.android.client.uploader.StreamCdnImageMimeTypes
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.TokenUtils
import io.getstream.chat.android.client.utils.observable.ChatEventsObservable
import io.getstream.chat.android.client.utils.observable.Disposable
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.internal.exhaustive
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Calendar
import java.util.Date
import java.util.concurrent.Executor
import kotlin.jvm.Throws

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
    private val socketStateService: SocketStateService = SocketStateService(),
    private val queryChannelsPostponeHelper: QueryChannelsPostponeHelper,
    private val encryptedPushNotificationsConfigStore: EncryptedPushNotificationsConfigStore,
    private val userStateService: UserStateService = UserStateService(),
    private val tokenUtils: TokenUtils = TokenUtils
) {

    @InternalStreamChatApi
    public val notificationHandler: ChatNotificationHandler = notifications.handler

    private var connectionListener: InitConnectionListener? = null
    private val logger = ChatLogger.get("Client")
    private val eventsObservable = ChatEventsObservable(socket, this)
    private val lifecycleObserver = StreamLifecycleObserver(
        object : LifecycleHandler {
            override fun resume() = reconnectSocket()
            override fun stopped() {
                socket.releaseConnection()
            }
        }
    )

    public val disconnectListeners: MutableList<(User?) -> Unit> = mutableListOf()
    public val preSetUserListeners: MutableList<(User) -> Unit> = mutableListOf()

    private var pushNotificationReceivedListener: PushNotificationReceivedListener =
        PushNotificationReceivedListener { _, _ -> }

    init {
        eventsObservable.subscribe { event ->

            notifications.onChatEvent(event)

            when (event) {
                is ConnectedEvent -> {
                    val user = event.me
                    val connectionId = event.connectionId
                    socketStateService.onConnected(connectionId)
                    userStateService.onUserUpdated(user)
                    api.setConnection(user.id, connectionId)
                    lifecycleObserver.observe()
                    storePushNotificationsConfig(user.id)
                    notifications.onSetUser()
                }
                is DisconnectedEvent -> {
                    when (event.disconnectCause) {
                        DisconnectCause.ConnectionReleased,
                        DisconnectCause.NetworkNotAvailable,
                        is DisconnectCause.Error,
                        -> socketStateService.onDisconnected()
                        is DisconnectCause.UnrecoverableError -> {
                            userStateService.onSocketUnrecoverableError()
                            socketStateService.onSocketUnrecoverableError()
                        }
                    }.exhaustive
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
        replaceWith = ReplaceWith("this.connectUser(user, token).enqueue { result -> TODO(\"Handle result\") })"),
        level = DeprecationLevel.ERROR,
    )
    public fun setUser(user: User, token: String, listener: InitConnectionListener? = null) {
        setUser(user, ConstantTokenProvider(token), listener)
    }

    /**
     * Initializes [ChatClient] for a specific user using the given user [token].
     *
     * @param user Instance of [User] type.
     * @param token Instance of JWT token. It must be unique for each user.
     * Check out [docs](https://getstream.io/chat/docs/android/init_and_users/) for more info about tokens.
     * Also visit [this site](https://jwt.io) to find more about Json Web Token standard.
     * You can generate the JWT token on using one of the available libraries or use our manual
     * [tool](https://getstream.io/chat/docs/react/token_generator/) for token generation.
     *
     * @see ChatClient.connectUser with [TokenProvider] parameter for advanced use cases.
     */
    @CheckResult
    public fun connectUser(user: User, token: String): Call<ConnectionData> {
        return connectUser(user, ConstantTokenProvider(token))
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
        replaceWith = ReplaceWith("this.connectUser(user, tokenProvider).enqueue { result -> TODO(\"Handle result\") })"),
        level = DeprecationLevel.ERROR,
    )
    public fun setUser(
        user: User,
        tokenProvider: TokenProvider,
        listener: InitConnectionListener? = null,
    ) {
        if (tokenUtils.getUserId(tokenProvider.loadToken()) != user.id) {
            logger.logE("The user_id provided on the JWT token doesn't match with the current user you try to connect")
            listener?.onError(ChatError("The user_id provided on the JWT token doesn't match with the current user you try to connect"))
            return
        }
        val userState = userStateService.state
        when {
            userState is UserState.UserSet && userState.user.id == user.id && socketStateService.state == SocketState.Idle -> {
                userStateService.onUserUpdated(user)
                tokenManager.setTokenProvider(tokenProvider)
                connectionListener = listener
                socketStateService.onConnectionRequested()
                socket.connect(user)
                notifySetUser(user)
            }
            userState is UserState.NotSet -> {
                initializeClientWithUser(user, tokenProvider)
                connectionListener = listener
                socketStateService.onConnectionRequested()
                socket.connect(user)
            }
            userState is UserState.UserSet && userState.user.id != user.id -> {
                logger.logE("Trying to set user without disconnecting the previous one - make sure that previously set user is disconnected.")
                listener?.onError(ChatError("User cannot be set until previous one is disconnected."))
            }
            else -> {
                logger.logE("Failed to connect user. Please check you don't have connected user already")
                listener?.onError(ChatError("User cannot be set until previous one is disconnected."))
            }
        }
    }

    private fun initializeClientWithUser(
        user: User,
        tokenProvider: TokenProvider,
    ) {
        userStateService.onSetUser(user)
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
        @Suppress("DEPRECATION_ERROR")
        return createInitListenerCall { initListener -> setUser(user, tokenProvider, initListener) }
    }

    /**
     * Initializes [ChatClient] with stored user data.
     * Caution: This method doesn't establish connection to the web socket, you should use [connectUser] instead.
     *
     * This method initializes [ChatClient] to allow the use of Stream REST API client.
     * Moreover, it warms up the connection, and sets up notifications.
     *
     */
    private fun setUserWithoutConnectingIfNeeded() {
        if (isUserSet()) {
            return
        }
        encryptedPushNotificationsConfigStore.get()?.let { config ->
            initializeClientWithUser(
                user = User(id = config.userId),
                tokenProvider = ConstantTokenProvider(config.userToken),
            )
        }
    }

    private fun notifySetUser(user: User) {
        preSetUserListeners.forEach { it(user) }
    }

    private fun storePushNotificationsConfig(userId: String) {
        encryptedPushNotificationsConfigStore.put(
            PushNotificationsConfig(
                userToken = getCurrentToken() ?: "",
                userId = userId,
            ),
        )
    }

    @Deprecated(
        message = "Use connectAnonymousUser instead",
        replaceWith = ReplaceWith("this.connectAnonymousUser().enqueue { result -> TODO(\"Handle result\") })"),
        level = DeprecationLevel.ERROR,
    )
    public fun setAnonymousUser(listener: InitConnectionListener? = null) {
        socketStateService.onConnectionRequested()
        userStateService.onSetAnonymous()
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
        socket.connectAnonymously()
    }

    @CheckResult
    public fun connectAnonymousUser(): Call<ConnectionData> {
        return createInitListenerCall { initListener -> setAnonymousUser(initListener) }
    }

    @Deprecated(
        message = "Use connectGuestUser instead",
        replaceWith = ReplaceWith("this.connectGuestUser(userId, username).enqueue { result -> TODO(\"Handle result\") })"),
        level = DeprecationLevel.ERROR,
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

    /**
     * Uploads a file for the given channel. Progress can be accessed via [callback].
     *
     * The Stream CDN imposes the following restrictions on file uploads:
     * - The maximum file size is 20 MB
     *
     * @param channelType the channel type. ie messaging
     * @param channelId the channel id. ie 123
     * @param file the file that needs to be uploaded
     * @param callback the callback to track progress
     *
     * @return executable async [Call] which completes with [Result] having data equal to the URL of the uploaded file
     * if the file was successfully uploaded.
     *
     * @see FileUploader
     * @see <a href="https://getstream.io/chat/docs/android/file_uploads/?language=kotlin">File Uploads</a>
     */
    @CheckResult
    @JvmOverloads
    public fun sendFile(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback? = null,
    ): Call<String> {
        return api.sendFile(channelType, channelId, file, callback)
    }

    /**
     * Uploads an image for the given channel. Progress can be accessed via [callback].
     *
     * The Stream CDN imposes the following restrictions on image uploads:
     * - The maximum image size is 20 MB
     * - Supported MIME types are listed in [StreamCdnImageMimeTypes.SUPPORTED_IMAGE_MIME_TYPES]
     *
     * @param channelType the channel type. ie messaging
     * @param channelId the channel id. ie 123
     * @param file the image file that needs to be uploaded
     * @param callback the callback to track progress
     *
     * @return executable async [Call] which completes with [Result] having data equal to the URL of the uploaded image
     * if the image was successfully uploaded.
     *
     * @see FileUploader
     * @see StreamCdnImageMimeTypes.SUPPORTED_IMAGE_MIME_TYPES
     * @see <a href="https://getstream.io/chat/docs/android/file_uploads/?language=kotlin">File Uploads</a>
     */
    @CheckResult
    @JvmOverloads
    public fun sendImage(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback? = null,
    ): Call<String> {
        return api.sendImage(channelType, channelId, file, callback)
    }

    /**
     * Deletes the file represented by [url] from the given channel.
     *
     * @param channelType the channel type. ie messaging
     * @param channelId the channel id. ie 123
     * @param url the URL of the file to be deleted
     *
     * @return executable async [Call] responsible for deleting a file
     *
     * @see FileUploader
     * @see <a href="https://getstream.io/chat/docs/android/file_uploads/?language=kotlin">File Uploads</a>
     */
    @CheckResult
    public fun deleteFile(channelType: String, channelId: String, url: String): Call<Unit> {
        return api.deleteFile(channelType, channelId, url)
    }

    /**
     * Deletes the image represented by [url] from the given channel.
     *
     * @param channelType the channel type. ie messaging
     * @param channelId the channel id. ie 123
     * @param url the URL of the image to be deleted
     *
     * @return executable async [Call] responsible for deleting an image
     *
     * @see FileUploader
     * @see <a href="https://getstream.io/chat/docs/android/file_uploads/?language=kotlin">File Uploads</a>
     */
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
        when (socketStateService.state) {
            is SocketState.Disconnected -> when (val userState = userStateService.state) {
                is UserState.UserSet -> socket.connect(userState.user)
                is UserState.Anonymous.AnonymousUserSet -> socket.connectAnonymously()
                else -> error("Invalid user state $userState without user being set!")
            }
            else -> Unit
        }.exhaustive
    }

    public fun addSocketListener(listener: SocketListener) {
        socket.addListener(listener)
    }

    public fun removeSocketListener(listener: SocketListener) {
        socket.removeListener(listener)
    }

    @Deprecated(
        message = "Use subscribe with ChatEventListener parameter",
        level = DeprecationLevel.ERROR,
    )
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
    @Deprecated(
        message = "Use subscribeFor with ChatEventListener parameter",
        level = DeprecationLevel.ERROR,
    )
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
    @Deprecated(
        message = "Use subscribeFor with ChatEventListener parameter",
        level = DeprecationLevel.ERROR,
    )
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
    @Deprecated(
        message = "Use subscribeFor with ChatEventListener parameter",
        level = DeprecationLevel.ERROR,
    )
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
    @Deprecated(
        message = "Use subscribeFor with ChatEventListener parameter",
        level = DeprecationLevel.ERROR,
    )
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
    @Deprecated(
        message = "Use subscribeForSingle with ChatEventListener parameter",
        level = DeprecationLevel.ERROR,
    )
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
    @Deprecated(
        message = "Use subscribeForSingle with ChatEventListener parameter",
        level = DeprecationLevel.ERROR,
    )
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
            userStateService.state.userOrError().let { user ->
                disconnectListeners.forEach { listener -> listener(user) }
            }
        }
        connectionListener = null
        socketStateService.onDisconnectRequested()
        userStateService.onLogout()
        socket.disconnect()
        notifications.cancelLoadDataWork()
        encryptedPushNotificationsConfigStore.clear()
        lifecycleObserver.dispose()
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

    /**
     * Removes all of the messages of the channel but doesn't affect the channel data or members.
     *
     * @param channelType the channel type. ie messaging
     * @param channelId the channel id. ie 123
     *
     * @return executable async [Call] which completes with [Result] having data equal to the truncated channel
     * if the channel was successfully truncated.
     */
    @CheckResult
    public fun truncateChannel(channelType: String, channelId: String): Call<Channel> {
        return api.truncateChannel(channelType, channelId)
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
            channelExtraData,
            updateMessage,
        )

    /**
     * Updates specific fields of channel data retaining the custom data fields which were set previously.
     *
     * @param channelType the channel type. ie messaging
     * @param channelId the channel id. ie 123
     * @param set the key-value data which will be added to the existing channel data object
     * @param unset the list of fields which will be removed from the existing channel data object
     */
    @CheckResult
    public fun updateChannelPartial(
        channelType: String,
        channelId: String,
        set: Map<String, Any> = emptyMap(),
        unset: List<String> = emptyList(),
    ): Call<Channel> {
        return api.updateChannelPartial(
            channelType = channelType,
            channelId = channelId,
            set = set,
            unset = unset
        )
    }

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
    @Deprecated(
        message = "Use the unmuteChannel(channelType, channelId) method instead",
        replaceWith = ReplaceWith("this.unmuteChannel(channelType, channelId)"),
        level = DeprecationLevel.ERROR,
    )
    public fun unMuteChannel(channelType: String, channelId: String): Call<Unit> {
        return api.unmuteChannel(channelType, channelId)
    }

    @CheckResult
    public fun unmuteChannel(channelType: String, channelId: String): Call<Unit> {
        return api.unmuteChannel(channelType, channelId)
    }

    @CheckResult
    public fun unmuteUser(userId: String): Call<Unit> = api.unmuteUser(userId)

    @CheckResult
    public fun unmuteCurrentUser(): Call<Unit> = api.unmuteCurrentUser()

    @CheckResult
    public fun muteCurrentUser(): Call<Mute> = api.muteCurrentUser()

    @CheckResult
    public fun flagUser(userId: String): Call<Flag> = api.flagUser(userId)

    @CheckResult
    public fun unflagUser(userId: String): Call<Flag> = api.unflagUser(userId)

    @CheckResult
    public fun flagMessage(messageId: String): Call<Flag> = api.flagMessage(messageId)

    @CheckResult
    public fun unflagMessage(messageId: String): Call<Flag> = api.unflagMessage(messageId)

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
    ).toUnitCall()

    @CheckResult
    @Deprecated(
        message = "Use the unbanUser(targetId, channelType, channelId) method instead",
        replaceWith = ReplaceWith("this.unbanUser(targetId, channelType, channelId)"),
        level = DeprecationLevel.ERROR,
    )
    public fun unBanUser(
        targetId: String,
        channelType: String,
        channelId: String,
    ): Call<Unit> = api.unbanUser(
        targetId = targetId,
        channelType = channelType,
        channelId = channelId,
        shadow = false
    ).toUnitCall()

    @CheckResult
    public fun unbanUser(
        targetId: String,
        channelType: String,
        channelId: String,
    ): Call<Unit> = api.unbanUser(
        targetId = targetId,
        channelType = channelType,
        channelId = channelId,
        shadow = false
    ).toUnitCall()

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
    ).toUnitCall()

    @CheckResult
    public fun removeShadowBan(
        targetId: String,
        channelType: String,
        channelId: String,
    ): Call<Unit> = api.unbanUser(
        targetId = targetId,
        channelType = channelType,
        channelId = channelId,
        shadow = true
    ).toUnitCall()

    @CheckResult
    @JvmOverloads
    public fun queryBannedUsers(
        filter: FilterObject,
        sort: QuerySort<BannedUsersSort> = QuerySort.asc(BannedUsersSort::createdAt),
        offset: Int? = null,
        limit: Int? = null,
        createdAtAfter: Date? = null,
        createdAtAfterOrEqual: Date? = null,
        createdAtBefore: Date? = null,
        createdAtBeforeOrEqual: Date? = null,
    ): Call<List<BannedUser>> {
        return api.queryBannedUsers(
            filter = filter,
            sort = sort,
            offset = offset,
            limit = limit,
            createdAtAfter = createdAtAfter,
            createdAtAfterOrEqual = createdAtAfterOrEqual,
            createdAtBefore = createdAtBefore,
            createdAtBeforeOrEqual = createdAtBeforeOrEqual,
        )
    }

    //endregion

    @Deprecated(
        message = "Use ChatClient.handleRemoteMessage instead",
        replaceWith = ReplaceWith("handleRemoteMessage(remoteMessage)"),
        level = DeprecationLevel.WARNING,
    )
    public fun onMessageReceived(remoteMessage: RemoteMessage) {
        setUserWithoutConnectingIfNeeded()
        notifications.onFirebaseMessage(remoteMessage, pushNotificationReceivedListener)
    }

    @Deprecated(
        message = "Use ChatClient.setFirebaseToken instead",
        replaceWith = ReplaceWith("setFirebaseToken(token)"),
        level = DeprecationLevel.WARNING,
    )
    public fun onNewTokenReceived(token: String) {
        notifications.setFirebaseToken(token)
    }

    @InternalStreamChatApi
    public fun setPushNotificationReceivedListener(pushNotificationReceivedListener: PushNotificationReceivedListener) {
        this.pushNotificationReceivedListener = pushNotificationReceivedListener
    }

    public fun getConnectionId(): String? {
        return runCatching { socketStateService.state.connectionIdOrError() }.getOrNull()
    }

    public fun getCurrentUser(): User? {
        return runCatching { userStateService.state.userOrError() }.getOrNull()
    }

    public fun getCurrentToken(): String? {
        return runCatching {
            when (userStateService.state) {
                is UserState.UserSet -> if (tokenManager.hasToken()) tokenManager.getToken() else null
                else -> null
            }
        }.getOrNull()
    }

    public fun isSocketConnected(): Boolean {
        return socketStateService.state is SocketState.Connected
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

    private fun warmUp() {
        if (config.warmUp) {
            api.warmUp()
        }
    }

    private fun isUserSet() = userStateService.state !is UserState.NotSet

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
        private var baseTimeout = 30_000L
        private var cdnTimeout = 30_000L
        private var enableMoshi = true
        private var logLevel = ChatLogLevel.NOTHING
        private var warmUp: Boolean = true
        private var callbackExecutor: Executor? = null
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

        /**
         * A new serialization implementation is now used by default by the SDK.
         *
         * If you experience any issues with the new implementation, call this builder method with `false`
         * as the parameter to revert to the old implementation. Note that the old implementation will be
         * removed soon.
         *
         * To check for issues caused by new serialization, enable error logs using the [logLevel]
         * method and look for the NEW_SERIALIZATION_ERROR tag in your logs.
         */
        @Deprecated(
            message = "Old serialization will be removed soon",
            level = DeprecationLevel.WARNING,
        )
        public fun useNewSerialization(enabled: Boolean): Builder = apply {
            this.enableMoshi = enabled
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

        @InternalStreamChatApi
        @VisibleForTesting
        public fun callbackExecutor(callbackExecutor: Executor): Builder = apply {
            this.callbackExecutor = callbackExecutor
        }

        public fun build(): ChatClient {

            if (apiKey.isEmpty()) {
                throw IllegalStateException("apiKey is not defined in " + this::class.java.simpleName)
            }

            val config = ChatClientConfig(
                apiKey = apiKey,
                httpUrl = "https://$baseUrl/",
                cdnHttpUrl = "https://$cdnUrl/",
                wssUrl = "wss://$baseUrl/",
                baseTimeout = baseTimeout,
                cdnTimeout = cdnTimeout,
                warmUp = warmUp,
                loggerConfig = ChatLogger.Config(logLevel, loggerHandler),
            )

            config.enableMoshi = enableMoshi

            val module =
                ChatModule(appContext, config, notificationsHandler, fileUploader, tokenManager, callbackExecutor)

            val result = ChatClient(
                config,
                module.api(),
                module.socket(),
                module.notifications(),
                tokenManager,
                module.socketStateService,
                module.queryChannelsPostponeHelper,
                EncryptedPushNotificationsConfigStore(appContext),
                module.userStateService,
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

        /**
         * Checks if remote message can be handled
         *
         * @return true if message can be handled
         */
        @JvmStatic
        public fun isValidRemoteMessage(
            remoteMessage: RemoteMessage,
            defaultNotificationConfig: NotificationConfig = NotificationConfig(),
        ): Boolean {
            return instance?.isValidRemoteMessage(remoteMessage) ?: remoteMessage.isValid(
                defaultNotificationConfig
            )
        }

        /**
         * Handles remote message.
         * If user is not connected - automatically restores last user credentials and sets user without connecting to the socket.
         * Remote message will be handled internally unless user overrides [ChatNotificationHandler.onFirebaseMessage]
         * Be sure to initialize ChatClient before calling this method!
         *
         * @see [ChatNotificationHandler.onFirebaseMessage]
         * @throws IllegalStateException if called before initializing ChatClient
         */
        @Throws(IllegalStateException::class)
        @JvmStatic
        public fun handleRemoteMessage(remoteMessage: RemoteMessage) {
            ensureClientInitialized().run {
                setUserWithoutConnectingIfNeeded()
                notifications.onFirebaseMessage(remoteMessage, pushNotificationReceivedListener)
            }
        }

        @Throws(IllegalStateException::class)
        internal suspend fun displayNotificationWithData(channelType: String, channelId: String, messageId: String) {
            ensureClientInitialized().notifications.displayNotificationWithData(
                channelId = channelId,
                channelType = channelType,
                messageId = messageId,
            )
        }

        /**
         * Sets Firebase token.
         * Be sure to initialize ChatClient before calling this method!
         *
         * @throws IllegalStateException if called before initializing ChatClient
         */
        @Throws(IllegalStateException::class)
        @JvmStatic
        public fun setFirebaseToken(token: String) {
            ensureClientInitialized().notifications.setFirebaseToken(token)
        }

        @Throws(IllegalStateException::class)
        private fun ensureClientInitialized(): ChatClient {
            check(isInitialized) { "ChatClient should be initialized first!" }
            return instance()
        }
    }
}
