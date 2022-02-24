package io.getstream.chat.android.client

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.annotation.CheckResult
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.api.ErrorCall
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.PinnedMessagesPagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.api.models.SendActionRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.call.doOnResult
import io.getstream.chat.android.client.call.doOnStart
import io.getstream.chat.android.client.call.map
import io.getstream.chat.android.client.call.toUnitCall
import io.getstream.chat.android.client.call.withPrecondition
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
import io.getstream.chat.android.client.events.HasOwnUser
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationChannelMutesUpdatedEvent
import io.getstream.chat.android.client.events.NotificationMutesUpdatedEvent
import io.getstream.chat.android.client.events.UserEvent
import io.getstream.chat.android.client.experimental.errorhandler.ErrorHandler
import io.getstream.chat.android.client.experimental.errorhandler.factory.ErrorHandlerFactory
import io.getstream.chat.android.client.experimental.errorhandler.listeners.DeleteReactionErrorHandler
import io.getstream.chat.android.client.experimental.errorhandler.listeners.SendReactionErrorHandler
import io.getstream.chat.android.client.experimental.errorhandler.listeners.onMessageError
import io.getstream.chat.android.client.experimental.errorhandler.listeners.onReactionError
import io.getstream.chat.android.client.experimental.interceptor.Interceptor
import io.getstream.chat.android.client.experimental.interceptor.SendMessageInterceptor
import io.getstream.chat.android.client.experimental.plugin.Plugin
import io.getstream.chat.android.client.experimental.plugin.factory.PluginFactory
import io.getstream.chat.android.client.experimental.plugin.listeners.ChannelMarkReadListener
import io.getstream.chat.android.client.experimental.plugin.listeners.DeleteMessageListener
import io.getstream.chat.android.client.experimental.plugin.listeners.DeleteReactionListener
import io.getstream.chat.android.client.experimental.plugin.listeners.EditMessageListener
import io.getstream.chat.android.client.experimental.plugin.listeners.HideChannelListener
import io.getstream.chat.android.client.experimental.plugin.listeners.MarkAllReadListener
import io.getstream.chat.android.client.experimental.plugin.listeners.QueryChannelListener
import io.getstream.chat.android.client.experimental.plugin.listeners.QueryChannelsListener
import io.getstream.chat.android.client.experimental.plugin.listeners.SendMessageListener
import io.getstream.chat.android.client.experimental.plugin.listeners.SendReactionListener
import io.getstream.chat.android.client.experimental.plugin.listeners.ThreadQueryListener
import io.getstream.chat.android.client.extensions.ATTACHMENT_TYPE_FILE
import io.getstream.chat.android.client.extensions.ATTACHMENT_TYPE_IMAGE
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.retry
import io.getstream.chat.android.client.header.VersionPrefixHeader
import io.getstream.chat.android.client.helpers.QueryChannelsPostponeHelper
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.logger.ChatLoggerHandler
import io.getstream.chat.android.client.models.AppSettings
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
import io.getstream.chat.android.client.models.PushMessage
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.SearchMessagesResult
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.notifications.ChatNotifications
import io.getstream.chat.android.client.notifications.PushNotificationReceivedListener
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.client.notifications.handler.NotificationHandler
import io.getstream.chat.android.client.notifications.handler.NotificationHandlerFactory
import io.getstream.chat.android.client.setup.InitializationCoordinator
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.socket.InitConnectionListener
import io.getstream.chat.android.client.socket.SocketListener
import io.getstream.chat.android.client.token.CacheableTokenProvider
import io.getstream.chat.android.client.token.ConstantTokenProvider
import io.getstream.chat.android.client.token.TokenManager
import io.getstream.chat.android.client.token.TokenManagerImpl
import io.getstream.chat.android.client.token.TokenProvider
import io.getstream.chat.android.client.uploader.FileUploader
import io.getstream.chat.android.client.uploader.StreamCdnImageMimeTypes
import io.getstream.chat.android.client.user.CredentialConfig
import io.getstream.chat.android.client.user.storage.SharedPreferencesCredentialStorage
import io.getstream.chat.android.client.user.storage.UserCredentialStorage
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.TokenUtils
import io.getstream.chat.android.client.utils.flatMapSuspend
import io.getstream.chat.android.client.utils.internal.toggle.ToggleService
import io.getstream.chat.android.client.utils.observable.ChatEventsObservable
import io.getstream.chat.android.client.utils.observable.Disposable
import io.getstream.chat.android.client.utils.retry.NoRetryPolicy
import io.getstream.chat.android.client.utils.retry.RetryPolicy
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Calendar
import java.util.Date
import java.util.concurrent.Executor

/**
 * The ChatClient is the main entry point for all low-level operations on chat
 */
@Suppress("NEWER_VERSION_IN_SINCE_KOTLIN")
@OptIn(ExperimentalStreamChatApi::class)
public class ChatClient internal constructor(
    public val config: ChatClientConfig,
    private val api: ChatApi,
    private val socket: ChatSocket,
    @property:InternalStreamChatApi public val notifications: ChatNotifications,
    private val tokenManager: TokenManager = TokenManagerImpl(),
    private val socketStateService: SocketStateService = SocketStateService(),
    private val queryChannelsPostponeHelper: QueryChannelsPostponeHelper,
    private val userCredentialStorage: UserCredentialStorage,
    private val userStateService: UserStateService = UserStateService(),
    private val tokenUtils: TokenUtils = TokenUtils,
    internal val scope: CoroutineScope,
    // TODO: Make private/internal after migrating ChatDomain
    public val retryPolicy: RetryPolicy,
    private val initializationCoordinator: InitializationCoordinator = InitializationCoordinator.getOrCreate(),
) {
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

    private var pushNotificationReceivedListener: PushNotificationReceivedListener =
        PushNotificationReceivedListener { _, _ -> }

    public lateinit var plugins: List<Plugin>

    private var interceptors: MutableList<Interceptor> = mutableListOf()

    /**
     * Error handlers for API calls.
     */
    private var errorHandlers: List<ErrorHandler> = emptyList()

    init {
        eventsObservable.subscribe { event ->
            when (event) {
                is ConnectedEvent -> {
                    val user = event.me
                    val connectionId = event.connectionId
                    socketStateService.onConnected(connectionId)
                    api.setConnection(user.id, connectionId)
                    lifecycleObserver.observe()
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
                    }
                }
                is NewMessageEvent -> {
                    notifications.onNewMessageEvent(event)
                }
                else -> Unit // Ignore other events
            }

            val currentUser = when {
                event is HasOwnUser -> event.me
                event is UserEvent && event.user.id == getCurrentUser()?.id ?: "" -> event.user
                else -> null
            }
            currentUser?.let { updatedCurrentUser ->
                userStateService.onUserUpdated(updatedCurrentUser)
                storePushNotificationsConfig(updatedCurrentUser.id, updatedCurrentUser.name)
            }
        }
        logger.logI("Initialised: " + getVersion())
    }

    internal fun addPlugins(plugins: List<Plugin>) {
        this.plugins = plugins
    }

    @InternalStreamChatApi
    public fun addInterceptor(interceptor: Interceptor) {
        this.interceptors.add(interceptor)
    }

    internal fun addErrorHandlers(errorHandlers: List<ErrorHandler>) {
        this.errorHandlers = errorHandlers.sorted()
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
     * @param user The user to set.
     * @param tokenProvider A [TokenProvider] implementation.
     * @param listener Socket connection listener.
     */
    private fun setUser(
        user: User,
        tokenProvider: TokenProvider,
        listener: InitConnectionListener? = null,
    ) {
        val cacheableTokenProvider = CacheableTokenProvider(tokenProvider)
        if (tokenUtils.getUserId(cacheableTokenProvider.loadToken()) != user.id) {
            logger.logE("The user_id provided on the JWT token doesn't match with the current user you try to connect")
            listener?.onError(ChatError("The user_id provided on the JWT token doesn't match with the current user you try to connect"))
            return
        }
        val userState = userStateService.state
        when {
            userState is UserState.UserSet && userState.user.id == user.id && socketStateService.state == SocketState.Idle -> {
                userStateService.onUserUpdated(user)
                tokenManager.setTokenProvider(cacheableTokenProvider)
                connectionListener = listener
                socketStateService.onConnectionRequested()
                socket.connect(user)
                initializationCoordinator.userSet(user)
                initializationCoordinator.userConnected(user)
            }
            userState is UserState.NotSet -> {
                initializeClientWithUser(user, cacheableTokenProvider)
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
        tokenProvider: CacheableTokenProvider,
    ) {
        initializationCoordinator.userSet(user)
        userStateService.onSetUser(user)
        // fire a handler here that the chatDomain and chatUI can use
        config.isAnonymous = false
        tokenManager.setTokenProvider(tokenProvider)
        warmUp()
    }

    /**
     * Get the current settings of the app. Check [AppSettings].
     *
     * @return [AppSettings] the settings of the app.
     */
    public fun appSettings(): Call<AppSettings> = api.appSettings()

    /**
     * Initializes [ChatClient] for a specific user. The [tokenProvider] implementation is used
     * for the initial token, and it's also invoked whenever the user's token has expired, to
     * fetch a new token.
     *
     * This method performs required operations before connecting with the Stream API.
     * Moreover, it warms up the connection, sets up notifications, and connects to the socket.
     * You can use [listener] to get updates about socket connection.
     *
     * @param user The user to set.
     * @param tokenProvider A [TokenProvider] implementation.
     */
    @CheckResult
    public fun connectUser(user: User, tokenProvider: TokenProvider): Call<ConnectionData> {
        return createInitListenerCall { initListener -> setUser(user, tokenProvider, initListener) }
    }

    /**
     * Initializes [ChatClient] with stored user data.
     * Caution: This method doesn't establish connection to the web socket, you should use [connectUser] instead.
     *
     * This method initializes [ChatClient] to allow the use of Stream REST API client.
     * Moreover, it warms up the connection, and sets up notifications.
     */
    @InternalStreamChatApi
    public fun setUserWithoutConnectingIfNeeded() {
        if (isUserSet()) {
            return
        }

        userCredentialStorage.get()?.let { config ->
            initializeClientWithUser(
                user = User(id = config.userId).apply { name = config.userName },
                tokenProvider = CacheableTokenProvider(ConstantTokenProvider(config.userToken)),
            )
        }
    }

    @InternalStreamChatApi
    public fun containsStoredCredentials(): Boolean {
        return userCredentialStorage.get() != null
    }

    private fun storePushNotificationsConfig(userId: String, userName: String) {
        userCredentialStorage.put(
            CredentialConfig(
                userToken = getCurrentToken() ?: "",
                userId = userId,
                userName = userName,
            ),
        )
    }

    private fun setAnonymousUser(listener: InitConnectionListener? = null) {
        if (userStateService.state is UserState.NotSet) {
            socketStateService.onConnectionRequested()
            userStateService.onSetAnonymous()
            connectionListener = object : InitConnectionListener() {
                override fun onSuccess(data: ConnectionData) {
                    initializationCoordinator.userSet(data.user)
                    listener?.onSuccess(data)
                }

                override fun onError(error: ChatError) {
                    listener?.onError(error)
                }
            }
            config.isAnonymous = true
            warmUp()
            socket.connectAnonymously()
        } else {
            logger.logE("Failed to connect user. Please check you don't have connected user already")
            listener?.onError(ChatError("User cannot be set until previous one is disconnected."))
        }
    }

    @CheckResult
    public fun connectAnonymousUser(): Call<ConnectionData> {
        return createInitListenerCall { initListener -> setAnonymousUser(initListener) }
    }

    private fun setGuestUser(userId: String, username: String, listener: InitConnectionListener? = null) {
        getGuestToken(userId, username).enqueue {
            if (it.isSuccess) {
                setUser(it.data().user, ConstantTokenProvider(it.data().token), listener)
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
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     * @param file The file that needs to be uploaded.
     * @param callback The callback to track progress.
     *
     * @return Executable async [Call] which completes with [Result] having data equal to the URL of the uploaded file
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
     * @param channelType The channel type. ie messaging.
     * @param channelId Еhe channel id. ie 123.
     * @param file The image file that needs to be uploaded.
     * @param callback The callback to track progress.
     *
     * @return Executable async [Call] which completes with [Result] having data equal to the URL of the uploaded image
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
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     * @param url The URL of the file to be deleted.
     *
     * @return Executable async [Call] responsible for deleting a file.
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
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     * @param url The URL of the image to be deleted.
     *
     * @return Executable async [Call] responsible for deleting an image.
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

    /**
     * Deletes the reaction associated with the message with the given message id.
     * [cid] parameter is being used in side effect functions executed by plugins.
     * You can skip it if plugins are not being used.
     *
     * The call will be retried accordingly to [retryPolicy].
     *
     * @see [Plugin]
     * @see [RetryPolicy]
     *
     * @param messageId The id of the message to which reaction belongs.
     * @param reactionType The type of reaction.
     * @param cid The full channel id, i.e. "messaging:123" to which the message with reaction belongs.
     *
     * @return Executable async [Call] responsible for deleting the reaction.
     */
    @CheckResult
    public fun deleteReaction(messageId: String, reactionType: String, cid: String? = null): Call<Message> {
        val relevantPlugins = plugins.filterIsInstance<DeleteReactionListener>()
        val relevantErrorHandlers = errorHandlers.filterIsInstance<DeleteReactionErrorHandler>()

        val currentUser = getCurrentUser()

        return api.deleteReaction(messageId = messageId, reactionType = reactionType)
            .retry(scope = scope, retryPolicy = retryPolicy)
            .onMessageError(relevantErrorHandlers, cid, messageId)
            .doOnStart(scope) {
                relevantPlugins
                    .forEach { plugin ->
                        plugin.onDeleteReactionRequest(
                            cid = cid,
                            messageId = messageId,
                            reactionType = reactionType,
                            currentUser = currentUser!!,
                        )
                    }
            }
            .doOnResult(scope) { result ->
                relevantPlugins.forEach { plugin ->
                    plugin.onDeleteReactionResult(
                        cid = cid,
                        messageId = messageId,
                        reactionType = reactionType,
                        currentUser = currentUser!!,
                        result = result,
                    )
                }
            }
            .precondition(relevantPlugins) { onDeleteReactionPrecondition(currentUser) }
    }

    /**
     * Sends the reaction.
     * Use [enforceUnique] parameter to specify whether the reaction should replace other reactions added by the current user.
     * [cid] parameter is being used in side effect functions executed by plugins.
     * You can skip it if plugins are not being used.
     *
     * The call will be retried accordingly to [retryPolicy].
     *
     * @see [Plugin]
     * @see [RetryPolicy]
     *
     * @param reaction The [Reaction] to send.
     * @param enforceUnique Flag to determine whether the reaction should replace other ones added by the current user.
     * @param cid The full channel id, i.e. "messaging:123" to which the message with reaction belongs.
     *
     * @return Executable async [Call] responsible for sending the reaction.
     */
    @CheckResult
    @JvmOverloads
    public fun sendReaction(reaction: Reaction, enforceUnique: Boolean, cid: String? = null): Call<Reaction> {
        val relevantPlugins = plugins.filterIsInstance<SendReactionListener>()
        val relevantErrorHandlers = errorHandlers.filterIsInstance<SendReactionErrorHandler>()
        val currentUser = getCurrentUser()

        return api.sendReaction(reaction, enforceUnique)
            .retry(scope = scope, retryPolicy = retryPolicy)
            .onReactionError(relevantErrorHandlers, reaction, enforceUnique, currentUser!!)
            .doOnStart(scope) {
                relevantPlugins
                    .forEach { plugin ->
                        plugin.onSendReactionRequest(
                            cid = cid,
                            reaction = reaction,
                            enforceUnique = enforceUnique,
                            currentUser = currentUser,
                        )
                    }
            }
            .doOnResult(scope) { result ->
                relevantPlugins.forEach { plugin ->
                    plugin.onSendReactionResult(
                        cid = cid,
                        reaction = reaction,
                        enforceUnique = enforceUnique,
                        currentUser = currentUser,
                        result = result,
                    )
                }
            }
            .precondition(relevantPlugins) { onSendReactionPrecondition(currentUser, reaction) }
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
        }
    }

    public fun addSocketListener(listener: SocketListener) {
        socket.addListener(listener)
    }

    public fun removeSocketListener(listener: SocketListener) {
        socket.removeListener(listener)
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
        notifications.onLogout()
        // fire a handler here that the chatDomain and chatUI can use
        getCurrentUser().let(initializationCoordinator::userDisconnected)
        connectionListener = null
        socketStateService.onDisconnectRequested()
        userStateService.onLogout()
        socket.disconnect()
        userCredentialStorage.clear()
        lifecycleObserver.dispose()
    }

    //region: api calls

    @CheckResult
    public fun getDevices(): Call<List<Device>> {
        return api.getDevices()
    }

    @CheckResult
    public fun deleteDevice(device: Device): Call<Unit> {
        return api.deleteDevice(device)
    }

    @CheckResult
    public fun addDevice(device: Device): Call<Unit> {
        return api.addDevice(device)
    }

    /**
     * Search messages across channels. There are two ways to paginate through search results:
     *
     * 1. Using [limit] and [offset] parameters
     * 1. Using [limit] and [next] parameters
     *
     * Limit and offset will allow you to access up to 1000 results matching your query.
     * You will not be able to sort using limit and offset. The results will instead be
     * sorted by relevance and message ID.
     *
     * Next pagination will allow you to access all search results that match your query,
     * and you will be able to sort using any filter-able fields and custom fields.
     * Pages of sort results will be returned with **next** and **previous** strings which
     * can be supplied as a next parameter when making a query to get a new page of results.
     *
     * @param channelFilter Channel filter conditions.
     * @param messageFilter Message filter conditions.
     * @param offset Pagination offset, cannot be used with sort or next.
     * @param limit The number of messages to return.
     * @param next Pagination parameter, cannot be used with non-zero offset.
     * @param sort The sort criteria applied to the result, cannot be used with non-zero offset.
     *
     * @return Executable async [Call] responsible for searching messages across channels.
     */
    @CheckResult
    public fun searchMessages(
        channelFilter: FilterObject,
        messageFilter: FilterObject,
        offset: Int? = null,
        limit: Int? = null,
        next: String? = null,
        sort: QuerySort<Message>? = null,
    ): Call<SearchMessagesResult> {
        if (offset != null && (sort != null || next != null)) {
            return ErrorCall(ChatError("Cannot specify offset with sort or next parameters"))
        }
        return api.searchMessages(
            channelFilter = channelFilter,
            messageFilter = messageFilter,
            offset = offset,
            limit = limit,
            next = next,
            sort = sort,
        )
    }

    /**
     * Returns a list of messages pinned in the channel.
     * You can sort the list by specifying [sort] parameter.
     * Keep in mind that for now we only support sorting by [Message.pinnedAt].
     * The list can be paginated in a few different ways using [limit] and [pagination].
     * @see [PinnedMessagesPagination]
     *
     * @param channelType The channel type. (e.g. messaging, livestream)
     * @param channelId The id of the channel we're querying.
     * @param limit Max limit of messages to be fetched.
     * @param sort Parameter by which we sort the messages.
     * @param pagination Provides different options for pagination.
     *
     * @return Executable async [Call] responsible for getting pinned messages.
     */
    @CheckResult
    public fun getPinnedMessages(
        channelType: String,
        channelId: String,
        limit: Int,
        sort: QuerySort<Message>,
        pagination: PinnedMessagesPagination,
    ): Call<List<Message>> {
        return api.getPinnedMessages(
            channelType = channelType,
            channelId = channelId,
            limit = limit,
            sort = sort,
            pagination = pagination,
        )
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
        getMessagesWithAttachments(channelType, channelId, offset, limit, listOf(type)).map { messages ->
            messages.flatMap { message -> message.attachments.filter { it.type == type } }
        }

    /**
     * Returns a [Call] with messages that contain at least one desired type attachment but
     * not necessarily all of them will have a specified type.
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     * @param offset The messages offset.
     * @param limit Max limit messages to be fetched.
     * @param types Desired attachment's types list.
     */
    @CheckResult
    public fun getMessagesWithAttachments(
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        types: List<String>,
    ): Call<List<Message>> {
        val channelFilter = Filters.`in`("cid", "$channelType:$channelId")
        val messageFilter = Filters.`in`("attachments.type", types)
        return searchMessages(
            channelFilter = channelFilter,
            messageFilter = messageFilter,
            offset = offset,
            limit = limit,
        ).map { it.messages }
    }

    @CheckResult
    @InternalStreamChatApi
    @ExperimentalStreamChatApi
    public fun getRepliesInternal(messageId: String, limit: Int): Call<List<Message>> = api.getReplies(messageId, limit)

    @CheckResult
    @InternalStreamChatApi
    @ExperimentalStreamChatApi
    public fun getRepliesMoreInternal(
        messageId: String,
        firstId: String,
        limit: Int,
    ): Call<List<Message>> = api.getRepliesMore(messageId, firstId, limit)

    @CheckResult
    public fun getReplies(messageId: String, limit: Int): Call<List<Message>> {
        val relevantPlugins = plugins.filterIsInstance<ThreadQueryListener>()

        return api.getReplies(messageId, limit)
            .doOnStart(scope) {
                relevantPlugins.forEach { plugin -> plugin.onGetRepliesRequest(messageId, limit) }
            }
            .doOnResult(scope) { result ->
                relevantPlugins.forEach { plugin -> plugin.onGetRepliesResult(result, messageId, limit) }
            }
            .precondition(relevantPlugins) { onGetRepliesPrecondition(messageId, limit) }
    }

    @CheckResult
    public fun getRepliesMore(
        messageId: String,
        firstId: String,
        limit: Int,
    ): Call<List<Message>> {
        val relevantPlugins = plugins.filterIsInstance<ThreadQueryListener>()

        return api.getRepliesMore(messageId, firstId, limit)
            .doOnStart(scope) { relevantPlugins.forEach { it.onGetRepliesMoreRequest(messageId, firstId, limit) } }
            .doOnResult(scope) { result ->
                relevantPlugins.forEach { it.onGetRepliesMoreResult(result, messageId, firstId, limit) }
            }
            .precondition(relevantPlugins) { onGetRepliesMorePrecondition(messageId, firstId, limit) }
    }

    @CheckResult
    public fun sendAction(request: SendActionRequest): Call<Message> {
        return api.sendAction(request)
    }

    @CheckResult
    @JvmOverloads
    public fun deleteMessage(messageId: String, hard: Boolean = false): Call<Message> {
        val relevantPlugins = plugins.filterIsInstance<DeleteMessageListener>()

        return api.deleteMessage(messageId, hard)
            .doOnStart(scope) {
                relevantPlugins.forEach { listener ->
                    listener.onMessageDeleteRequest(messageId)
                }
            }
            .doOnResult(scope) { result ->
                relevantPlugins.forEach { listener ->
                    listener.onMessageDeleteResult(messageId, result)
                }
            }
    }

    @CheckResult
    public fun getMessage(messageId: String): Call<Message> {
        return api.getMessage(messageId)
    }

    /**
     * Sends the message to the given channel without running any side effects.
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     * @param message Message to send.
     *
     * @return Executable async [Call] responsible for sending a message.
     */
    internal fun sendMessageInternal(
        channelType: String,
        channelId: String,
        message: Message,
    ): Call<Message> {
        return api.sendMessage(channelType, channelId, message)
    }

    /**
     * Sends the message to the given channel. If [isRetrying] is set to true, the message may not be prepared again.
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     * @param message Message object
     * @param isRetrying True if this message is being retried.
     *
     * @return Executable async [Call] responsible for sending a message.
     */
    @CheckResult
    @JvmOverloads
    public fun sendMessage(
        channelType: String,
        channelId: String,
        message: Message,
        isRetrying: Boolean = false,
    ): Call<Message> {
        val relevantPlugins = plugins.filterIsInstance<SendMessageListener>()
        val relevantInterceptors = interceptors.filterIsInstance<SendMessageInterceptor>()
        return CoroutineCall(scope) {

            // Message is first prepared i.e. all its attachments are uploaded and message is updated with these attachments.
            // TODO: An InterceptedCall wrapper can be created to avoid so much code here.
            relevantInterceptors.fold(Result.success(message)) { message, interceptor ->
                if (message.isSuccess) {
                    interceptor.interceptMessage(channelType, channelId, message.data(), isRetrying)
                } else message
            }.flatMapSuspend { newMessage ->
                api.sendMessage(channelType, channelId, newMessage)
                    .retry(scope, retryPolicy)
                    .doOnResult(scope) { result ->
                        relevantPlugins.forEach {
                            it.onMessageSendResult(
                                result,
                                channelType,
                                channelId,
                                newMessage
                            )
                        }
                    }.await()
            }
        }
    }

    /**
     * Updates the message in the API and calls the plugins that handle this request. [OfflinePlugin] can be used here to
     * store the updated message locally.
     *
     * @param message [Message] The message to be updated.
     */
    @CheckResult
    public fun updateMessage(message: Message): Call<Message> {
        val relevantPlugins = plugins.filterIsInstance<EditMessageListener>()

        return api.updateMessage(message)
            .doOnStart(scope) {
                relevantPlugins
                    .forEach { plugin -> plugin.onMessageEditRequest(message) }
            }
            .doOnResult(scope) { result ->
                relevantPlugins.forEach { plugin -> plugin.onMessageEditResult(message, result) }
            }
    }

    /**
     * Updates the message in the API without causing any side effect in the local data of the SDK.
     *
     * @param message [Message] The message to be updated in the API
     */
    public fun updateMessageInternal(message: Message): Call<Message> {
        return api.updateMessage(message)
    }

    /**
     * Partially updates specific [Message] fields retaining the fields which were set previously.
     *
     * @param messageId The message ID.
     * @param set The key-value data which will be added to the existing message object.
     * @param unset The list of fields which will be removed from the existing message object.
     *
     * @return Executable async [Call] responsible for partially updating the message.
     */
    @CheckResult
    public fun partialUpdateMessage(
        messageId: String,
        set: Map<String, Any> = emptyMap(),
        unset: List<String> = emptyList(),
    ): Call<Message> {
        return api.partialUpdateMessage(
            messageId = messageId,
            set = set,
            unset = unset,
        )
    }

    /**
     * Pins the message.
     *
     * @param message The message object containing the ID of the message to be pinned.
     * @param expirationDate The exact expiration date.
     *
     * @return Executable async [Call] responsible for pinning the message.
     */
    @CheckResult
    public fun pinMessage(message: Message, expirationDate: Date? = null): Call<Message> {
        val set: MutableMap<String, Any> = LinkedHashMap()
        set["pinned"] = true
        expirationDate?.let { set["pin_expires"] = it }
        return partialUpdateMessage(
            messageId = message.id,
            set = set
        )
    }

    /**
     * Pins the message.
     *
     * @param message The message object containing the ID of the message to be pinned.
     * @param timeout The expiration timeout in seconds.
     *
     * @return Executable async [Call] responsible for pinning the message.
     */
    @CheckResult
    public fun pinMessage(message: Message, timeout: Int): Call<Message> {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.SECOND, timeout)
        }
        return partialUpdateMessage(
            messageId = message.id,
            set = mapOf(
                "pinned" to true,
                "pin_expires" to calendar.time
            )
        )
    }

    /**
     * Unpins the message that was previously pinned
     *
     * @param message The message object containing the ID of the message to be unpinned.
     *
     * @return Executable async [Call] responsible for unpinning the message.
     */
    @CheckResult
    public fun unpinMessage(message: Message): Call<Message> {
        return partialUpdateMessage(
            messageId = message.id,
            set = mapOf("pinned" to false)
        )
    }

    @CheckResult
    @InternalStreamChatApi
    public fun queryChannelsInternal(request: QueryChannelsRequest): Call<List<Channel>> =
        queryChannelsPostponeHelper.queryChannels(request)

    @CheckResult
    @InternalStreamChatApi
    public fun queryChannelInternal(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ): Call<Channel> = api.queryChannel(channelType, channelId, request)

    @CheckResult
    public fun queryChannel(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ): Call<Channel> {
        val relevantPlugins = plugins.filterIsInstance<QueryChannelListener>()

        return api.queryChannel(channelType, channelId, request)
            .doOnStart(scope) {
                relevantPlugins.forEach { it.onQueryChannelRequest(channelType, channelId, request) }
            }
            .doOnResult(scope) { result ->
                relevantPlugins.forEach { it.onQueryChannelResult(result, channelType, channelId, request) }
            }
            .precondition(relevantPlugins) { onQueryChannelPrecondition(channelType, channelId, request) }
    }

    @CheckResult
    public fun queryChannels(request: QueryChannelsRequest): Call<List<Channel>> {
        val relevantPlugins = plugins.filterIsInstance<QueryChannelsListener>()

        return queryChannelsPostponeHelper.queryChannels(request)
            .doOnStart(scope) {
                relevantPlugins.forEach { it.onQueryChannelsRequest(request) }
            }
            .doOnResult(scope) { result ->
                relevantPlugins.forEach { it.onQueryChannelsResult(result, request) }
            }
            .precondition(relevantPlugins) { onQueryChannelsPrecondition(request) }
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

    /**
     * Hides the specified channel.
     *
     * @param channelType The type of the channel.
     * @param channelId Id of the channel.
     * @param clearHistory Boolean, if you want to clear the history of this channel or not.
     *
     * @return Executable async [Call] responsible for hiding a channel.
     *
     * @see <a href="https://getstream.io/chat/docs/channel_delete/?language=kotlin">Hiding a channel</a>
     */
    internal fun hideChannelInternal(
        channelType: String,
        channelId: String,
        clearHistory: Boolean = false,
    ): Call<Unit> = api.hideChannel(channelType, channelId, clearHistory)

    /**
     * Hides the specified channel with side effects.
     *
     * @param channelType The type of the channel.
     * @param channelId Id of the channel.
     * @param clearHistory Boolean, if you want to clear the history of this channel or not.
     *
     * @return Executable async [Call] responsible for hiding a channel.
     *
     * @see <a href="https://getstream.io/chat/docs/channel_delete/?language=kotlin">Hiding a channel</a>
     */
    @CheckResult
    public fun hideChannel(
        channelType: String,
        channelId: String,
        clearHistory: Boolean = false,
    ): Call<Unit> {
        val relevantPlugins = plugins.filterIsInstance<HideChannelListener>()
        return hideChannelInternal(channelType, channelId, clearHistory)
            .doOnStart(scope) {
                relevantPlugins.forEach { it.onHideChannelRequest(channelType, channelId, clearHistory) }
            }
            .doOnResult(scope) { result ->
                relevantPlugins.forEach { it.onHideChannelResult(result, channelType, channelId, clearHistory) }
            }
            .precondition(relevantPlugins) { onHideChannelPrecondition(channelType, channelId, clearHistory) }
    }

    /**
     * Removes all of the messages of the channel but doesn't affect the channel data or members.
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     * @param systemMessage The system message that will be shown in the channel.
     *
     * @return Executable async [Call] which completes with [Result] having data equal to the truncated channel
     * if the channel was successfully truncated.
     */
    @CheckResult
    public fun truncateChannel(
        channelType: String,
        channelId: String,
        systemMessage: Message? = null,
    ): Call<Channel> {
        return api.truncateChannel(
            channelType = channelType,
            channelId = channelId,
            systemMessage = systemMessage
        )
    }

    @CheckResult
    public fun stopWatching(channelType: String, channelId: String): Call<Unit> {
        return api.stopWatching(channelType, channelId)
    }

    /**
     * Updates all of the channel data. Any data that is present on the channel and not included in a full update
     * will be deleted.
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     * @param updateMessage The message object allowing you to show a system message in the channel.
     * @param channelExtraData The updated channel extra data.
     *
     * @return Executable async [Call] responsible for updating channel data.
     */
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
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     * @param set The key-value data which will be added to the existing channel data object.
     * @param unset The list of fields which will be removed from the existing channel data object.
     *
     * @return Executable async [Call] responsible for updating channel data.
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

    /**
     * Enables slow mode for the channel. When slow mode is enabled, users can only send a message every
     * [cooldownTimeInSeconds] time interval. The [cooldownTimeInSeconds] is specified in seconds, and should be
     * between 1-120.
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     * @param cooldownTimeInSeconds The duration of the time interval users have to wait between messages.
     *
     * @return Executable async [Call] responsible for enabling slow mode.
     */
    @CheckResult
    public fun enableSlowMode(
        channelType: String,
        channelId: String,
        cooldownTimeInSeconds: Int,
    ): Call<Channel> {
        return if (cooldownTimeInSeconds in 1..120) {
            api.enableSlowMode(channelType, channelId, cooldownTimeInSeconds)
        } else {
            ErrorCall(ChatError("You can't specify a value outside the range 1-120 for cooldown duration."))
        }
    }

    /**
     * Disables slow mode for the channel.
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     *
     * @return Executable async [Call] responsible for disabling slow mode.
     */
    @CheckResult
    public fun disableSlowMode(
        channelType: String,
        channelId: String,
    ): Call<Channel> {
        return api.disableSlowMode(channelType, channelId)
    }

    @CheckResult
    public fun rejectInvite(channelType: String, channelId: String): Call<Channel> {
        return api.rejectInvite(channelType, channelId)
    }

    /**
     * Sends an event to all users watching the channel.
     *
     * @param eventType The event name.
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     * @param extraData The event payload.
     *
     * @return Executable async [Call] responsible for sending an event.
     */
    @CheckResult
    public fun sendEvent(
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any> = emptyMap(),
    ): Call<ChatEvent> {
        return api.sendEvent(eventType, channelType, channelId, extraData)
    }

    public fun getVersion(): String = VERSION_PREFIX_HEADER.prefix + BuildConfig.STREAM_CHAT_VERSION

    @CheckResult
    public fun acceptInvite(
        channelType: String,
        channelId: String,
        message: String?,
    ): Call<Channel> {
        return api.acceptInvite(channelType, channelId, message)
    }

    /**
     * Marks all the channel as read.
     *
     * @return [Result] Empty unit result.
     */
    @InternalStreamChatApi
    public fun markAllReadInternal(): Call<Unit> = api.markAllRead()

    /**
     * Marks all the channel as read.
     *
     * @return [Result] Empty unit result.
     */
    @CheckResult
    public fun markAllRead(): Call<Unit> {
        val relevantPlugins = plugins.filterIsInstance<MarkAllReadListener>()
        return api.markAllRead()
            .doOnStart(scope) {
                relevantPlugins.forEach { it.onMarkAllReadRequest() }
            }
    }

    /**
     * Marks the specified channel as read.
     *
     * @param channelType Type of the channel.
     * @param channelId Id of the channel.
     */
    @CheckResult
    public fun markRead(channelType: String, channelId: String): Call<Unit> {
        val relevantPlugins = plugins.filterIsInstance<ChannelMarkReadListener>()

        return api.markRead(channelType, channelId)
            .precondition(relevantPlugins) { onChannelMarkReadPrecondition(channelType, channelId) }
    }

    /**
     * Marks the specified channel as read without running a precondition.
     *
     * @param channelType Type of the channel.
     * @param channelId Id of the channel.
     */
    @InternalStreamChatApi
    @CheckResult
    public fun markReadInternal(channelType: String, channelId: String): Call<Unit> {
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

    /**
     * Updates specific user fields retaining the custom data fields which were set previously.
     *
     * @param id User ids.
     * @param set The key-value data which will be added to the existing user object.
     * @param unset The list of fields which will be removed from the existing user object.
     *
     * @return Executable async [Call].
     */
    @CheckResult
    public fun partialUpdateUser(
        id: String,
        set: Map<String, Any> = emptyMap(),
        unset: List<String> = emptyList(),
    ): Call<User> {
        if (id != getCurrentUser()?.id) {
            logger.logE("The client-side partial update allows you to update only the current user. Make sure the user is set before updating it.")
            return ErrorCall(ChatError("The client-side partial update allows you to update only the current user. Make sure the user is set before updating it."))
        }

        return api.partialUpdateUser(
            id = id,
            set = set,
            unset = unset,
        )
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

    /**
     * Method to remove members of a given channel.
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     * @param members The list of the members to be removed.
     */
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

    /**
     * Mutes a channel for the current user. Messages added to the channel will not trigger
     * push notifications, and will not change the unread count for the users that muted it.
     * By default, mutes stay in place indefinitely until the user removes it. However, you
     * can optionally set an expiration time. Triggers `notification.channel_mutes_updated`
     * event.
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     * @param expiration The duration of mute in **millis**.
     *
     * @return Executable async [Call] responsible for muting a channel.
     *
     * @see [NotificationChannelMutesUpdatedEvent]
     */
    @JvmOverloads
    @CheckResult
    public fun muteChannel(
        channelType: String,
        channelId: String,
        expiration: Int? = null,
    ): Call<Unit> {
        return api.muteChannel(
            channelType = channelType,
            channelId = channelId,
            expiration = expiration
        )
    }

    /**
     * Unmutes a channel for the current user. Triggers `notification.channel_mutes_updated`
     * event.
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     *
     * @return Executable async [Call] responsible for unmuting a channel.
     *
     * @see [NotificationChannelMutesUpdatedEvent]
     */
    @CheckResult
    public fun unmuteChannel(
        channelType: String,
        channelId: String,
    ): Call<Unit> {
        return api.unmuteChannel(channelType, channelId)
    }

    /**
     * Mutes a user. Messages from muted users will not trigger push notifications. By default,
     * mutes stay in place indefinitely until the user removes it. However, you can optionally
     * set a mute timeout. Triggers `notification.mutes_updated` event.
     *
     * @param userId The user id to mute.
     * @param timeout The timeout in **minutes** until the mute is expired.
     *
     * @return Executable async [Call] responsible for muting a user.
     *
     * @see [NotificationMutesUpdatedEvent]
     */
    @JvmOverloads
    @CheckResult
    public fun muteUser(
        userId: String,
        timeout: Int? = null,
    ): Call<Mute> {
        return api.muteUser(userId, timeout)
    }

    /**
     * Unmutes a previously muted user. Triggers `notification.mutes_updated` event.
     *
     * @param userId The user id to unmute.
     *
     * @return Executable async [Call] responsible for unmuting a user.
     *
     * @see [NotificationMutesUpdatedEvent]
     */
    @CheckResult
    public fun unmuteUser(userId: String): Call<Unit> {
        return api.unmuteUser(userId)
    }

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
     * Returns a [ChannelClient] for given type and id.
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     */
    public fun channel(channelType: String, channelId: String): ChannelClient {
        return ChannelClient(channelType, channelId, this)
    }

    /**
     * Returns a [ChannelClient] for given cid.
     *
     * @param cid The full channel id. ie messaging:123.
     */
    public fun channel(cid: String): ChannelClient {
        val (type, id) = cid.cidToTypeAndId()
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

    /**
     * Returns all events that happened for a list of channels since last sync (while the user was not
     * connected to the web-socket).
     *
     * @param channelsIds The list of channel CIDs
     * @param lastSyncAt The last time the user was online and in sync
     *
     * @return Executable async [Call] responsible for obtaining missing events.
     */
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

    public fun devToken(userId: String): String {
        require(userId.isNotEmpty()) { "User id must not be empty" }
        val header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9" //  {"alg": "HS256", "typ": "JWT"}
        val devSignature = "devtoken"
        val payload: String =
            Base64.encodeToString("{\"user_id\":\"$userId\"}".toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
        return "$header.$payload.$devSignature"
    }

    @ExperimentalStreamChatApi
    internal fun <R, T : Any> Call<T>.precondition(
        pluginsList: List<R>,
        preconditionCheck: suspend R.() -> Result<Unit>,
    ): Call<T> =
        withPrecondition(scope) {
            pluginsList.fold(Result.success(Unit)) { result, plugin ->
                if (result.isError) {
                    result
                } else {
                    val preconditionResult = preconditionCheck(plugin)
                    if (preconditionResult.isError) {
                        preconditionResult
                    } else {
                        result
                    }
                }
            }
        }

    /**
     * Builder to initialize the singleton [ChatClient] instance and configure its parameters.
     *
     * @param apiKey The API key of your Stream Chat app obtained from the [Stream Dashboard](https://dashboard.getstream.io/).
     * @param appContext The application [Context].
     */
    public class Builder(private val apiKey: String, private val appContext: Context) : ChatClientBuilder() {

        private var baseUrl: String = "chat.stream-io-api.com"
        private var cdnUrl: String = baseUrl
        private var logLevel = ChatLogLevel.NOTHING
        private var warmUp: Boolean = true
        private var callbackExecutor: Executor? = null
        private var loggerHandler: ChatLoggerHandler? = null
        private var notificationsHandler: NotificationHandler? = null
        private var notificationConfig: NotificationConfig = NotificationConfig(pushNotificationsEnabled = false)
        private var fileUploader: FileUploader? = null
        private val tokenManager: TokenManager = TokenManagerImpl()
        private var customOkHttpClient: OkHttpClient? = null
        private var userCredentialStorage: UserCredentialStorage? = null
        private var retryPolicy: RetryPolicy = NoRetryPolicy()

        /**
         * Sets the log level to be used by the client.
         *
         * See [ChatLogLevel] for details about the available options.
         *
         * We strongly recommend using [ChatLogLevel.NOTHING] in production builds,
         * which produces no logs.
         *
         * @param level The log level to use.
         */
        public fun logLevel(level: ChatLogLevel): Builder {
            logLevel = level
            return this
        }

        /**
         * Sets a [ChatLoggerHandler] instance that will receive log events from the SDK.
         *
         * Use this to forward SDK events to your own logging solutions.
         *
         * See the FirebaseLogger class in the UI Components sample app for an example implementation.
         *
         * @param loggerHandler Your custom [ChatLoggerHandler] implementation.
         */
        public fun loggerHandler(loggerHandler: ChatLoggerHandler): Builder {
            this.loggerHandler = loggerHandler
            return this
        }

        /**
         * Sets a custom [NotificationHandler] that the SDK will use to handle everything
         * around push notifications. Create your own subclass and override methods to customize
         * notification appearance and behavior.
         *
         * See the [Push Notifications](https://staging.getstream.io/chat/docs/sdk/android/client/guides/push-notifications/)
         * documentation for more information.
         *
         *
         * @param notificationConfig Config push notification.
         * @param notificationsHandler Your custom class implementation of [NotificationHandler].
         */
        @JvmOverloads
        public fun notifications(
            notificationConfig: NotificationConfig,
            notificationsHandler: NotificationHandler = NotificationHandlerFactory.createNotificationHandler(context = appContext),
        ): Builder = apply {
            this.notificationConfig = notificationConfig
            this.notificationsHandler = notificationsHandler
        }

        /**
         * Sets a custom file uploader implementation that will be used by the client
         * to upload files and images.
         *
         * The default implementation uses Stream's own CDN to store these files,
         * which has a 20 MB upload size limit.
         *
         * For more info, see [the File Uploads documentation](https://getstream.io/chat/docs/android/file_uploads/?language=kotlin).
         *
         * @param fileUploader Your custom implementation of [FileUploader].
         */
        public fun fileUploader(fileUploader: FileUploader): Builder {
            this.fileUploader = fileUploader
            return this
        }

        /**
         * By default, ChatClient performs a dummy HTTP call to the Stream API
         * when a user is set to initialize the HTTP connection and make subsequent
         * requests reusing this connection execute faster.
         *
         * Calling this method disables this connection warm-up behavior.
         */
        public fun disableWarmUp(): Builder = apply {
            warmUp = false
        }

        /**
         * Sets a custom [OkHttpClient] that will be used by the client to
         * perform API calls to Stream.
         *
         * Use this to configure parameters like timeout values, or to
         * add interceptors to process all network requests.
         *
         * @param okHttpClient The client to use for API calls.
         */
        public fun okHttpClient(okHttpClient: OkHttpClient): Builder = apply {
            this.customOkHttpClient = okHttpClient
        }

        /**
         * Sets the base URL to be used by the client.
         *
         * By default, this is the URL of Stream's [Edge API Infrastructure](https://getstream.io/blog/chat-edge-infrastructure/),
         * which provides low latency regardless of which region your Stream
         * app is hosted in.
         *
         * You should only change this URL if you're on dedicated Stream
         * Chat infrastructure.
         *
         * @param value The base URL to use.
         */
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

        @InternalStreamChatApi
        @VisibleForTesting
        public fun callbackExecutor(callbackExecutor: Executor): Builder = apply {
            this.callbackExecutor = callbackExecutor
        }

        @InternalStreamChatApi
        @ExperimentalStreamChatApi
        public fun withPlugin(pluginFactory: PluginFactory): Builder = apply {
            pluginFactories.add(pluginFactory)
        }

        /**
         * Overrides the default no-op error handler factory.
         *
         * @see [ErrorHandlerFactory]
         */
        @InternalStreamChatApi
        @ExperimentalStreamChatApi
        public fun withErrorHandler(errorHandlerFactory: ErrorHandlerFactory): Builder = apply {
            this.errorHandlerFactories.add(errorHandlerFactory)
        }

        /**
         * Overrides a default, based on shared preferences implementation for [UserCredentialStorage].
         */
        public fun credentialStorage(credentialStorage: UserCredentialStorage): Builder = apply {
            userCredentialStorage = credentialStorage
        }

        /**
         * Sets a custom [RetryPolicy] used to determine whether a particular call should be retried.
         * By default, no calls are retried.
         * @see [NoRetryPolicy]
         *
         * @param retryPolicy Custom [RetryPolicy] implementation.
         */
        public fun retryPolicy(retryPolicy: RetryPolicy): Builder = apply {
            this.retryPolicy = retryPolicy
        }

        private fun configureInitializer(chatClient: ChatClient) {
            chatClient.initializationCoordinator.addUserSetListener { user ->
                chatClient.addPlugins(
                    pluginFactories.map { pluginFactory ->
                        pluginFactory.get(user)
                    }
                )

                chatClient.addErrorHandlers(
                    errorHandlerFactories.map { factory ->
                        factory.create()
                    }
                )
            }
        }

        @InternalStreamChatApi
        @Deprecated(
            message = "It shouldn't be used outside of SDK code. Created for testing purposes",
            replaceWith = ReplaceWith("this.build()"),
            level = DeprecationLevel.ERROR
        )
        override fun internalBuild(): ChatClient {

            if (apiKey.isEmpty()) {
                throw IllegalStateException("apiKey is not defined in " + this::class.java.simpleName)
            }

            instance?.run {
                Log.e(
                    "Chat",
                    "[ERROR] You have just re-initialized ChatClient, old configuration has been overridden [ERROR]"
                )
            }

            val config = ChatClientConfig(
                apiKey = apiKey,
                httpUrl = "https://$baseUrl/",
                cdnHttpUrl = "https://$cdnUrl/",
                wssUrl = "wss://$baseUrl/",
                warmUp = warmUp,
                loggerConfig = ChatLogger.Config(logLevel, loggerHandler),
            )

            if (ToggleService.isInitialized().not()) {
                ToggleService.init(appContext, emptyMap())
            }

            val module =
                ChatModule(
                    appContext,
                    config,
                    notificationsHandler ?: NotificationHandlerFactory.createNotificationHandler(appContext),
                    notificationConfig,
                    fileUploader,
                    tokenManager,
                    callbackExecutor,
                    customOkHttpClient,
                )

            return ChatClient(
                config,
                module.api(),
                module.socket(),
                module.notifications(),
                tokenManager,
                module.socketStateService,
                module.queryChannelsPostponeHelper,
                userCredentialStorage = userCredentialStorage ?: SharedPreferencesCredentialStorage(appContext),
                module.userStateService,
                scope = module.networkScope,
                retryPolicy = retryPolicy,
            ).also {
                configureInitializer(it)
            }
        }
    }

    public abstract class ChatClientBuilder @InternalStreamChatApi public constructor() {
        /**
         * Factories of plugins that will be added to the SDK.
         *
         * @see [Plugin]
         * @see [PluginFactory]
         */
        protected val pluginFactories: MutableList<PluginFactory> = mutableListOf()

        /**
         * Factories of error handlers that will be added to the SDK.
         *
         * @see [Plugin]
         * @see [PluginFactory]
         */
        protected val errorHandlerFactories: MutableList<ErrorHandlerFactory> = mutableListOf()

        /**
         * Create a [ChatClient] instance based on the current configuration
         * of the [Builder].
         */
        public fun build(): ChatClient = internalBuild()
            .also {
                instance = it
            }

        @InternalStreamChatApi
        public abstract fun internalBuild(): ChatClient
    }

    public companion object {
        @InternalStreamChatApi
        @JvmStatic
        public var VERSION_PREFIX_HEADER: VersionPrefixHeader = VersionPrefixHeader.DEFAULT

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
         * Handles push message.
         * If user is not connected - automatically restores last user credentials and sets user without connecting to the socket.
         * Push message will be handled internally unless user overrides [NotificationHandler.onPushMessage]
         * Be sure to initialize ChatClient before calling this method!
         *
         * @see [NotificationHandler.onPushMessage]
         * @throws IllegalStateException if called before initializing ChatClient
         */
        @Throws(IllegalStateException::class)
        @JvmStatic
        public fun handlePushMessage(pushMessage: PushMessage) {
            ensureClientInitialized().run {
                setUserWithoutConnectingIfNeeded()
                notifications.onPushMessage(pushMessage, pushNotificationReceivedListener)
            }
        }

        @Throws(IllegalStateException::class)
        internal fun displayNotification(
            channel: Channel,
            message: Message,
        ) {
            ensureClientInitialized().notifications.displayNotification(
                channel = channel,
                message = message,
            )
        }

        /**
         * Dismiss notifications from a given [channelType] and [channelId].
         * Be sure to initialize ChatClient before calling this method!
         *
         * @param channelType String that represent the channel type of the channel you want to dismiss notifications.
         * @param channelId String that represent the channel id of the channel you want to dismiss notifications.
         *
         * @throws IllegalStateException if called before initializing ChatClient
         */
        @Throws(IllegalStateException::class)
        public fun dismissChannelNotifications(channelType: String, channelId: String) {
            ensureClientInitialized().notifications.dismissChannelNotifications(channelType, channelId)
        }

        /**
         * Sets device.
         * Be sure to initialize ChatClient before calling this method!
         *
         * @throws IllegalStateException if called before initializing ChatClient
         */
        @Throws(IllegalStateException::class)
        @JvmStatic
        public fun setDevice(device: Device) {
            ensureClientInitialized().notifications.setDevice(device)
        }

        @Throws(IllegalStateException::class)
        private fun ensureClientInitialized(): ChatClient {
            check(isInitialized) { "ChatClient should be initialized first!" }
            return instance()
        }
    }
}
