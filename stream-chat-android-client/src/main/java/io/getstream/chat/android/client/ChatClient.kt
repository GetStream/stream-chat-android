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
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.api.ErrorCall
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
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationChannelMutesUpdatedEvent
import io.getstream.chat.android.client.events.NotificationMutesUpdatedEvent
import io.getstream.chat.android.client.extensions.ATTACHMENT_TYPE_FILE
import io.getstream.chat.android.client.extensions.ATTACHMENT_TYPE_IMAGE
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.header.VersionPrefixHeader
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
import io.getstream.chat.android.client.models.PushMessage
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.SearchMessagesResult
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.notifications.ChatNotifications
import io.getstream.chat.android.client.notifications.PushNotificationReceivedListener
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.client.notifications.handler.NotificationHandler
import io.getstream.chat.android.client.notifications.handler.NotificationHandlerFactory
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
public class ChatClient internal constructor(
    public val config: ChatClientConfig,
    private val api: ChatApi,
    private val socket: ChatSocket,
    @property:InternalStreamChatApi public val notifications: ChatNotifications,
    private val tokenManager: TokenManager = TokenManagerImpl(),
    private val socketStateService: SocketStateService = SocketStateService(),
    private val queryChannelsPostponeHelper: QueryChannelsPostponeHelper,
    private val encryptedUserConfigStorage: EncryptedPushNotificationsConfigStore,
    private val userStateService: UserStateService = UserStateService(),
    private val tokenUtils: TokenUtils = TokenUtils,
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

    public val disconnectListeners: MutableList<(User?) -> Unit> = mutableListOf()
    public val preSetUserListeners: MutableList<(User) -> Unit> = mutableListOf()

    private var pushNotificationReceivedListener: PushNotificationReceivedListener =
        PushNotificationReceivedListener { _, _ -> }

    init {
        eventsObservable.subscribe { event ->
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
                is NewMessageEvent -> {
                    notifications.onNewMessageEvent(event)
                }
                else -> Unit // Ignore other events
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
     * @param user The user to set.
     * @param tokenProvider A [TokenProvider] implementation.
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
     */
    @InternalStreamChatApi
    public fun setUserWithoutConnectingIfNeeded() {
        if (isUserSet()) {
            return
        }

        encryptedUserConfigStorage.get()?.let { config ->
            initializeClientWithUser(
                user = User(id = config.userId),
                tokenProvider = ConstantTokenProvider(config.userToken),
            )
        }
    }

    @InternalStreamChatApi
    public fun containsStoredCredentials(): Boolean {
        return encryptedUserConfigStorage.get() != null
    }

    private fun notifySetUser(user: User) {
        preSetUserListeners.forEach { it(user) }
    }

    private fun storePushNotificationsConfig(userId: String) {
        encryptedUserConfigStorage.put(
            PushNotificationsConfig(
                userToken = getCurrentToken() ?: "",
                userId = userId,
            ),
        )
    }

    private fun setAnonymousUser(listener: InitConnectionListener? = null) {
        if (userStateService.state is UserState.NotSet) {
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

    @CheckResult
    @JvmOverloads
    public fun sendReaction(
        messageId: String,
        reactionType: String,
        enforceUnique: Boolean = false,
    ): Call<Reaction> {
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
        runCatching {
            userStateService.state.userOrError().let { user ->
                disconnectListeners.forEach { listener -> listener(user) }
            }
        }
        connectionListener = null
        socketStateService.onDisconnectRequested()
        userStateService.onLogout()
        socket.disconnect()
        encryptedUserConfigStorage.clear()
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

    @Deprecated(
        message = "Use the searchMessages method with unwrapped parameters instead",
        replaceWith = ReplaceWith("searchMessages(channelFilter, messageFilter, offset, limit)"),
        level = DeprecationLevel.WARNING,
    )
    @CheckResult
    public fun searchMessages(request: SearchMessagesRequest): Call<List<Message>> {
        return api.searchMessages(request)
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
     * Returns a [Call<List<Message>>] With messages which contain at least one desired type attachment but
     * not necessarily all of them will have a specified type.
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     * @param offset The messages offset.
     * @param limit Max limit messages to be fetched.
     * @param type The desired type attachment.
     */
    @Deprecated(
        message = "Use getMessagesWithAttachments function with types list instead",
        level = DeprecationLevel.ERROR,
    )
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
    @JvmOverloads
    public fun deleteMessage(messageId: String, hard: Boolean = false): Call<Message> {
        return api.deleteMessage(messageId, hard)
    }

    @CheckResult
    public fun getMessage(messageId: String): Call<Message> {
        return api.getMessage(messageId)
    }

    /**
     * Sends the message to the given channel.
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     * @param message Message object
     *
     * @return Executable async [Call] responsible for sending a message.
     */
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
    public fun queryChannel(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ): Call<Channel> {
        return api.queryChannel(channelType, channelId, request)
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
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     *
     * @return Executable async [Call] which completes with [Result] having data equal to the truncated channel
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

    /**
     * Builder to initialize the singleton [ChatClient] instance and configure its parameters.
     *
     * @param apiKey The API key of your Stream Chat app obtained from the [Stream Dashboard](https://dashboard.getstream.io/).
     * @param appContext The application [Context].
     */
    public class Builder(private val apiKey: String, private val appContext: Context) : ChatClientBuilder() {

        private var baseUrl: String = "chat.stream-io-api.com"
        private var cdnUrl: String = baseUrl
        private var baseTimeout = 30_000L
        private var cdnTimeout = 30_000L
        private var logLevel = ChatLogLevel.NOTHING
        private var warmUp: Boolean = true
        private var callbackExecutor: Executor? = null
        private var loggerHandler: ChatLoggerHandler? = null
        private var notificationsHandler: NotificationHandler? = null
        private var notificationConfig: NotificationConfig = NotificationConfig()
        private var fileUploader: FileUploader? = null
        private val tokenManager: TokenManager = TokenManagerImpl()
        private var customOkHttpClient: OkHttpClient? = null

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
         * Sets a custom [ChatNotificationHandler] that the SDK will use to handle everything
         * around push notifications. Create your own subclass and override methods to customize
         * notification appearance and behavior.
         *
         * See the [Push Notifications](https://staging.getstream.io/chat/docs/sdk/android/client/guides/push-notifications/)
         * documentation for more information.
         *
         * @param notificationsHandler Your custom subclass of [ChatNotificationHandler].
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

        @Deprecated("Use okHttpClient() to set the timeouts")
        public fun baseTimeout(timeout: Long): Builder {
            baseTimeout = timeout
            return this
        }

        @Deprecated("Use okHttpClient() to set the timeouts")
        public fun cdnTimeout(timeout: Long): Builder {
            cdnTimeout = timeout
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

        @Deprecated(
            message = "Do not use this method for file upload customization. Instead, implement the FileUploader interface and use the fileUploader method of this builder.",
            level = DeprecationLevel.ERROR,
            replaceWith = ReplaceWith("this.fileUploader(CustomFileUploader())")
        )
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

        public override fun buildChatClient(): ChatClient {

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
            return result
        }
    }

    public abstract class ChatClientBuilder @InternalStreamChatApi public constructor() {
        /**
         * Create a [ChatClient] instance based on the current configuration
         * of the [Builder].
         */
        public fun build(): ChatClient = buildChatClient().also {
            instance = it
        }

        public abstract fun buildChatClient(): ChatClient
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
         * Push message will be handled internally unless user overrides [ChatNotificationHandler.onPushMessage]
         * Be sure to initialize ChatClient before calling this method!
         *
         * @see [ChatNotificationHandler.onPushMessage]
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
