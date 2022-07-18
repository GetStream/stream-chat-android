/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.CheckResult
import androidx.annotation.WorkerThread
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.api.ErrorCall
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.PinnedMessagesPagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.api.models.SendActionRequest
import io.getstream.chat.android.client.api.models.querysort.QuerySortByField
import io.getstream.chat.android.client.api.models.querysort.QuerySorter
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
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
import io.getstream.chat.android.client.errorhandler.CreateChannelErrorHandler
import io.getstream.chat.android.client.errorhandler.DeleteReactionErrorHandler
import io.getstream.chat.android.client.errorhandler.ErrorHandler
import io.getstream.chat.android.client.errorhandler.QueryMembersErrorHandler
import io.getstream.chat.android.client.errorhandler.SendReactionErrorHandler
import io.getstream.chat.android.client.errorhandler.onCreateChannelError
import io.getstream.chat.android.client.errorhandler.onMessageError
import io.getstream.chat.android.client.errorhandler.onQueryMembersError
import io.getstream.chat.android.client.errorhandler.onReactionError
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.ConnectingEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.HasOwnUser
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationChannelMutesUpdatedEvent
import io.getstream.chat.android.client.events.NotificationMutesUpdatedEvent
import io.getstream.chat.android.client.events.UserEvent
import io.getstream.chat.android.client.extensions.ATTACHMENT_TYPE_FILE
import io.getstream.chat.android.client.extensions.ATTACHMENT_TYPE_IMAGE
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.retry
import io.getstream.chat.android.client.header.VersionPrefixHeader
import io.getstream.chat.android.client.helpers.AppSettingManager
import io.getstream.chat.android.client.helpers.CallPostponeHelper
import io.getstream.chat.android.client.interceptor.Interceptor
import io.getstream.chat.android.client.interceptor.SendMessageInterceptor
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.logger.ChatLoggerConfigImpl
import io.getstream.chat.android.client.logger.ChatLoggerHandler
import io.getstream.chat.android.client.logger.StreamLogLevelValidator
import io.getstream.chat.android.client.logger.StreamLoggerHandler
import io.getstream.chat.android.client.models.AppSettings
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.BannedUser
import io.getstream.chat.android.client.models.BannedUsersSort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ConnectionData
import io.getstream.chat.android.client.models.ConnectionState
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.EventType
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
import io.getstream.chat.android.client.plugin.Plugin
import io.getstream.chat.android.client.plugin.factory.PluginFactory
import io.getstream.chat.android.client.plugin.listeners.ChannelMarkReadListener
import io.getstream.chat.android.client.plugin.listeners.CreateChannelListener
import io.getstream.chat.android.client.plugin.listeners.DeleteMessageListener
import io.getstream.chat.android.client.plugin.listeners.DeleteReactionListener
import io.getstream.chat.android.client.plugin.listeners.EditMessageListener
import io.getstream.chat.android.client.plugin.listeners.HideChannelListener
import io.getstream.chat.android.client.plugin.listeners.MarkAllReadListener
import io.getstream.chat.android.client.plugin.listeners.QueryChannelListener
import io.getstream.chat.android.client.plugin.listeners.QueryChannelsListener
import io.getstream.chat.android.client.plugin.listeners.QueryMembersListener
import io.getstream.chat.android.client.plugin.listeners.SendGiphyListener
import io.getstream.chat.android.client.plugin.listeners.SendMessageListener
import io.getstream.chat.android.client.plugin.listeners.SendReactionListener
import io.getstream.chat.android.client.plugin.listeners.ShuffleGiphyListener
import io.getstream.chat.android.client.plugin.listeners.ThreadQueryListener
import io.getstream.chat.android.client.plugin.listeners.TypingEventListener
import io.getstream.chat.android.client.setup.InitializationCoordinator
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.setup.state.internal.toMutableState
import io.getstream.chat.android.client.socket.ChatSocket
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
import io.getstream.chat.android.client.utils.mapSuspend
import io.getstream.chat.android.client.utils.mergePartially
import io.getstream.chat.android.client.utils.observable.ChatEventsObservable
import io.getstream.chat.android.client.utils.observable.Disposable
import io.getstream.chat.android.client.utils.onError
import io.getstream.chat.android.client.utils.retry.NoRetryPolicy
import io.getstream.chat.android.client.utils.retry.RetryPolicy
import io.getstream.chat.android.client.utils.stringify
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.logging.CompositeStreamLogger
import io.getstream.logging.SilentStreamLogger
import io.getstream.logging.StreamLog
import io.getstream.logging.android.AndroidStreamLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import java.io.File
import java.util.Calendar
import java.util.Date
import io.getstream.chat.android.client.experimental.socket.ChatSocket as ChatSocketExperimental

/**
 * The ChatClient is the main entry point for all low-level operations on chat
 */
@Suppress("NEWER_VERSION_IN_SINCE_KOTLIN", "TooManyFunctions", "LargeClass")
public class ChatClient
@Suppress("LongParameterList")
internal constructor(
    public val config: ChatClientConfig,
    private val api: ChatApi,
    private val socket: ChatSocket,
    @property:InternalStreamChatApi public val notifications: ChatNotifications,
    private val tokenManager: TokenManager = TokenManagerImpl(),
    private val socketStateService: SocketStateService = SocketStateService(),
    private val callPostponeHelper: CallPostponeHelper,
    private val userCredentialStorage: UserCredentialStorage,
    private val userStateService: UserStateService = UserStateService(),
    private val tokenUtils: TokenUtils = TokenUtils,
    internal val scope: CoroutineScope,
    internal val retryPolicy: RetryPolicy,
    private val initializationCoordinator: InitializationCoordinator = InitializationCoordinator.getOrCreate(),
    private val appSettingsManager: AppSettingManager,
    private val chatSocketExperimental: ChatSocketExperimental,
    lifecycle: Lifecycle,
) {
    private val logger = StreamLog.getLogger("Client")
    private val waitConnection = MutableSharedFlow<Result<ConnectionData>>()
    private val eventsObservable = ChatEventsObservable(socket, waitConnection, scope, chatSocketExperimental)
    private val lifecycleObserver = StreamLifecycleObserver(
        lifecycle,
        object : LifecycleHandler {
            override fun resume() = reconnectSocket()
            override fun stopped() {
                socket.releaseConnection()
            }
        }
    )

    /**
     * With clientState allows the user to get the state of the SDK like connection, initialization...
     */
    public val clientState: ClientState
        get() = ClientState.get()

    private var pushNotificationReceivedListener: PushNotificationReceivedListener =
        PushNotificationReceivedListener { _, _ -> }

    /**
     * The list of plugins added once user is connected.
     *
     * @see [Plugin]
     */
    internal var plugins: List<Plugin> = emptyList()

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
                    if (ToggleService.isSocketExperimental().not()) {
                        socketStateService.onConnected(connectionId)
                        runBlocking(context = scope.coroutineContext) { lifecycleObserver.observe() }
                    }
                    api.setConnection(user.id, connectionId)
                    notifications.onSetUser()

                    clientState.toMutableState()?.run {
                        setConnectionState(ConnectionState.CONNECTED)
                        setInitialized(true)
                        setUser(user)
                    }
                }
                is DisconnectedEvent -> {
                    when (event.disconnectCause) {
                        DisconnectCause.ConnectionReleased,
                        DisconnectCause.NetworkNotAvailable,
                        is DisconnectCause.Error,
                        -> if (ToggleService.isSocketExperimental().not()) socketStateService.onDisconnected()
                        is DisconnectCause.UnrecoverableError -> {
                            userStateService.onSocketUnrecoverableError()
                            if (ToggleService.isSocketExperimental().not()) {
                                socketStateService.onSocketUnrecoverableError()
                            }
                            clientState.toMutableState()?.setConnectionState(ConnectionState.OFFLINE)
                        }
                    }
                }
                is NewMessageEvent -> {
                    notifications.onNewMessageEvent(event)
                }
                is ConnectingEvent -> {
                    clientState.toMutableState()?.setConnectionState(ConnectionState.CONNECTING)
                }
                else -> Unit // Ignore other events
            }

            event.extractCurrentUser()?.let { currentUser ->
                userStateService.onUserUpdated(currentUser)
                storePushNotificationsConfig(
                    currentUser.id,
                    currentUser.name,
                    userStateService.state !is UserState.UserSet,
                )
            }
        }
        logger.i { "Initialised: ${buildSdkTrackingHeaders()}" }
    }

    /**
     * Either entirely extracts current user from the event
     * or merges the one from the event into the existing current user.
     */
    private fun ChatEvent.extractCurrentUser(): User? {
        return when (this) {
            is HasOwnUser -> me
            is UserEvent -> getCurrentUser()
                ?.takeIf { it.id == user.id }
                ?.mergePartially(user)
            else -> null
        }
    }

    internal fun addPlugins(plugins: List<Plugin>) {
        this.plugins = plugins
    }

    @InternalStreamChatApi
    public fun addInterceptor(interceptor: Interceptor) {
        this.interceptors.add(interceptor)
    }

    @InternalStreamChatApi
    public fun removeAllInterceptors() {
        this.interceptors.clear()
    }

    /**
     * Adds a list of error handlers.
     * @see [ErrorHandler]
     *
     * @param errorHandlers A list of handlers to add.
     */
    @InternalStreamChatApi
    public fun addErrorHandlers(errorHandlers: List<ErrorHandler>) {
        this.errorHandlers = errorHandlers.sorted()
    }

    private fun logPlugins(plugins: List<Any>) {
        if (plugins.isNotEmpty()) {
            val jointString = plugins.joinToString { plugin ->
                plugin::class.qualifiedName ?: "plugin without qualified name"
            }
            logger.d { "Plugins found: $jointString" }
        } else {
            logger.d { "No plugins found for this request." }
        }
    }

    //region Set user

    /**
     * Initializes [ChatClient] for a specific user. The [tokenProvider] implementation is used
     * for the initial token, and it's also invoked whenever the user's token has expired, to
     * fetch a new token.
     *
     * @param user The user to set.
     * @param tokenProvider A [TokenProvider] implementation.
     * @param timeoutMilliseconds A timeout in milliseconds when the process will be aborted.
     *
     * @return [Result] of [ConnectionData] with the info of the established connection or a detailed error.
     */
    @Suppress("LongMethod")
    private suspend fun setUser(
        user: User,
        tokenProvider: TokenProvider,
        timeoutMilliseconds: Long?,
    ): Result<ConnectionData> {
        val isAnonymous = user == anonUser
        val cacheableTokenProvider = CacheableTokenProvider(tokenProvider)
        val userState = userStateService.state

        ClientState.get().toMutableState()?.setUser(user)
        initializationCoordinator.userConnectionRequest(user)

        return when {
            tokenUtils.getUserId(cacheableTokenProvider.loadToken()) != user.id -> {
                logger.e {
                    "The user_id provided on the JWT token doesn't match with the current user you try to connect"
                }
                Result.error(
                    ChatError(
                        "The user_id provided on the JWT token doesn't match with the current user you try to connect"
                    )
                )
            }
            userState is UserState.NotSet -> {
                logger.v { "[setUser] user is NotSet" }
                initializeClientWithUser(cacheableTokenProvider, isAnonymous)
                userStateService.onSetUser(user, isAnonymous)
                if (ToggleService.isSocketExperimental()) {
                    chatSocketExperimental.connectUser(user, isAnonymous)
                } else {
                    socketStateService.onConnectionRequested()
                    socket.connectUser(user, isAnonymous)
                }
                waitFirstConnection(timeoutMilliseconds)
            }
            userState is UserState.UserSet -> {
                logger.w {
                    "[setUser] Trying to set user without disconnecting the previous one - " +
                        "make sure that previously set user is disconnected."
                }
                when {
                    userState.user.id != user.id -> {
                        logger.e { "[setUser] Trying to set different user without disconnect previous one." }
                        Result.error(
                            ChatError(
                                "User cannot be set until the previous one is disconnected."
                            )
                        )
                    }
                    else -> {
                        getConnectionId()?.let { Result.success(ConnectionData(userState.user, it)) }
                            ?: run {
                                logger.e {
                                    "[setUser] Trying to connect the same user twice without a previously completed " +
                                        "connection."
                                }
                                Result.error(
                                    ChatError(
                                        "Failed to connect user. Please check you haven't connected a user already."
                                    )
                                )
                            }
                    }
                }
            }
            else -> {
                logger.e { "[setUser] Failed to connect user. Please check you don't have connected user already." }
                Result.error(ChatError("Failed to connect user. Please check you don't have connected user already."))
            }
        }.onError { disconnect() }
    }

    private fun initializeClientWithUser(
        tokenProvider: CacheableTokenProvider,
        isAnonymous: Boolean,
    ) {
        // fire a handler here that the chatDomain and chatUI can use
        config.isAnonymous = isAnonymous
        tokenManager.setTokenProvider(tokenProvider)
        appSettingsManager.loadAppSettings()
        warmUp()
    }

    /**
     * Get the current settings of the app. Check [AppSettings].
     *
     * @return [AppSettings] the settings of the app.
     */
    public fun appSettings(): Call<AppSettings> = api.appSettings()

    /**
     * Initializes [ChatClient] for a specific user.
     * The [tokenProvider] implementation is used for the initial token,
     * and it's also invoked whenever the user's token has expired, to fetch a new token.
     *
     * This method performs required operations before connecting with the Stream API.
     * Moreover, it warms up the connection, sets up notifications, and connects to the socket.
     *
     * Check out [docs](https://getstream.io/chat/docs/android/init_and_users/) for more info about tokens.
     * Also visit [this site](https://jwt.io) to find more about Json Web Token standard.
     * You can generate the JWT token on using one of the available libraries or use our manual
     * [tool](https://getstream.io/chat/docs/react/token_generator/) for token generation.
     *
     * @see TokenProvider
     *
     * @param user The user to set.
     * @param tokenProvider A [TokenProvider] implementation.
     * @param timeoutMilliseconds The timeout in milliseconds to be waiting until the connection is established.
     *
     * @return Executable [Call] responsible for connecting the user.
     */
    @CheckResult
    @JvmOverloads
    public fun connectUser(
        user: User,
        tokenProvider: TokenProvider,
        timeoutMilliseconds: Long? = null,
    ): Call<ConnectionData> {
        return CoroutineCall(scope) {
            logger.d { "[connectUser] userId: '${user.id}', username: '${user.name}'" }
            setUser(user, tokenProvider, timeoutMilliseconds).also { result ->
                logger.v {
                    "[connectUser] completed: ${
                    result.stringify { "ConnectionData(connectionId=${it.connectionId})" }
                    }"
                }
            }
        }
    }

    /**
     * Initializes [ChatClient] for a specific user using the given user [token].
     * Check [ChatClient.connectUser] with [TokenProvider] parameter for advanced use cases.
     *
     * @param user Instance of [User] type.
     * @param token Instance of JWT token.
     *
     * @return Executable [Call] responsible for connecting the user.
     */
    @CheckResult
    @JvmOverloads
    public fun connectUser(
        user: User,
        token: String,
        timeoutMilliseconds: Long? = null,
    ): Call<ConnectionData> {
        return connectUser(user, ConstantTokenProvider(token), timeoutMilliseconds)
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
            initializationCoordinator.userConnectionRequest(User(id = config.userId).apply { name = config.userName })

            initializeClientWithUser(
                tokenProvider = CacheableTokenProvider(ConstantTokenProvider(config.userToken)),
                isAnonymous = config.isAnonymous,
            )
        }
    }

    @InternalStreamChatApi
    public fun containsStoredCredentials(): Boolean {
        return userCredentialStorage.get() != null
    }

    private fun storePushNotificationsConfig(userId: String, userName: String, isAnonymous: Boolean) {
        userCredentialStorage.put(
            CredentialConfig(
                userToken = getCurrentToken() ?: "",
                userId = userId,
                userName = userName,
                isAnonymous = isAnonymous,
            ),
        )
    }

    @CheckResult
    @JvmOverloads
    public fun connectAnonymousUser(timeoutMilliseconds: Long? = null): Call<ConnectionData> {
        return CoroutineCall(scope) {
            logger.d { "[connectAnonymousUser] no args" }
            setUser(
                anonUser,
                ConstantTokenProvider(devToken(ANONYMOUS_USER_ID)),
                timeoutMilliseconds,
            ).also { result ->
                logger.v {
                    "[connectAnonymousUser] completed: ${
                    result.stringify { "ConnectionData(connectionId=${it.connectionId})" }
                    }"
                }
            }
        }
    }

    private suspend fun waitFirstConnection(timeoutMilliseconds: Long?): Result<ConnectionData> =
        timeoutMilliseconds?.let {
            withTimeoutOrNull(timeoutMilliseconds) { waitConnection.first() }
                ?: Result.error(ChatError("Connection wasn't established in ${timeoutMilliseconds}ms"))
        } ?: waitConnection.first()

    @CheckResult
    @JvmOverloads
    public fun connectGuestUser(
        userId: String,
        username: String,
        timeoutMilliseconds: Long? = null,
    ): Call<ConnectionData> {
        return CoroutineCall(scope) {
            logger.d { "[connectGuestUser] userId: '$userId', username: '$username'" }
            getGuestToken(userId, username).await()
                .mapSuspend { setUser(it.user, ConstantTokenProvider(it.token), timeoutMilliseconds) }
                .data()
                .also { result ->
                    logger.v {
                        "[connectAnonymousUser] completed: ${
                        result.stringify { "ConnectionData(connectionId=${it.connectionId})" }
                        }"
                    }
                }
        }
    }

    @CheckResult
    public fun getGuestToken(userId: String, userName: String): Call<GuestUser> {
        return api.getGuestUser(userId, userName)
    }

    /**
     * Query members and apply side effects if there are any.
     *
     * @param channelType The type of channel.
     * @param channelId The id of the channel.
     * @param offset Offset limit.
     * @param limit Number of members to fetch.
     * @param filter [FilterObject] to filter members of certain type.
     * @param sort Sort the list of members.
     * @param members List of members to search in distinct channels.
     *
     * @return [Call] with a list of members or an error.
     */
    @Suppress("LongParameterList")
    @CheckResult
    public fun queryMembers(
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySorter<Member>,
        members: List<Member> = emptyList(),
    ): Call<List<Member>> {
        val relevantPlugins = plugins.filterIsInstance<QueryMembersListener>().also(::logPlugins)
        val errorHandlers = errorHandlers.filterIsInstance<QueryMembersErrorHandler>()
        return api.queryMembers(channelType, channelId, offset, limit, filter, sort, members)
            .doOnResult(scope) { result ->
                relevantPlugins.forEach { plugin ->
                    logger.d { "Applying ${plugin::class.qualifiedName}.onQueryMembersResult" }
                    plugin.onQueryMembersResult(
                        result,
                        channelType,
                        channelId,
                        offset,
                        limit,
                        filter,
                        sort,
                        members
                    )
                }
            }
            .onQueryMembersError(errorHandlers, channelType, channelId, offset, limit, filter, sort, members)
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
        val relevantPlugins = plugins.filterIsInstance<DeleteReactionListener>().also(::logPlugins)
        val relevantErrorHandlers = errorHandlers.filterIsInstance<DeleteReactionErrorHandler>()

        val currentUser = getCurrentUser()

        return api.deleteReaction(messageId = messageId, reactionType = reactionType)
            .retry(scope = scope, retryPolicy = retryPolicy)
            .doOnStart(scope) {
                relevantPlugins.forEach { plugin ->
                    logger.d { "Applying ${plugin::class.qualifiedName}.onDeleteReactionRequest" }
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
                    logger.d { "Applying ${plugin::class.qualifiedName}.onDeleteReactionResult" }
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
            .onMessageError(relevantErrorHandlers, cid, messageId)
    }

    /**
     * Sends the reaction.
     * Use [enforceUnique] parameter to specify whether the reaction should replace other reactions added by the
     * current user.
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
        val relevantPlugins = plugins.filterIsInstance<SendReactionListener>().also(::logPlugins)
        val relevantErrorHandlers = errorHandlers.filterIsInstance<SendReactionErrorHandler>()
        val currentUser = getCurrentUser()

        return api.sendReaction(reaction, enforceUnique)
            .retry(scope = scope, retryPolicy = retryPolicy)
            .doOnStart(scope) {
                relevantPlugins
                    .forEach { plugin ->
                        logger.d { "Applying ${plugin::class.qualifiedName}.onSendReactionRequest" }
                        plugin.onSendReactionRequest(
                            cid = cid,
                            reaction = reaction,
                            enforceUnique = enforceUnique,
                            currentUser = currentUser!!,
                        )
                    }
            }
            .doOnResult(scope) { result ->
                relevantPlugins.forEach { plugin ->
                    logger.d { "Applying ${plugin::class.qualifiedName}.onSendReactionResult" }
                    plugin.onSendReactionResult(
                        cid = cid,
                        reaction = reaction,
                        enforceUnique = enforceUnique,
                        currentUser = currentUser!!,
                        result = result,
                    )
                }
            }
            .onReactionError(relevantErrorHandlers, reaction, enforceUnique, currentUser!!)
            .precondition(relevantPlugins) { onSendReactionPrecondition(currentUser, reaction) }
    }
    //endregion

    //endregion

    public fun disconnectSocket() {
        if (ToggleService.isSocketExperimental()) {
            chatSocketExperimental.disconnect(DisconnectCause.ConnectionReleased)
        } else {
            socket.disconnect()
        }
    }

    public fun reconnectSocket() {
        if (ToggleService.isSocketExperimental().not()) {
            when (socketStateService.state is SocketState.Disconnected) {
                true -> when (val userState = userStateService.state) {
                    is UserState.UserSet, is UserState.AnonymousUserSet -> socket.reconnectUser(
                        userState.userOrError(),
                        userState is UserState.AnonymousUserSet
                    )
                    else -> error("Invalid user state $userState without user being set!")
                }
                false -> Unit
            }
        } else {
            when (chatSocketExperimental.isDisconnected()) {
                true -> when (val userState = userStateService.state) {
                    is UserState.UserSet, is UserState.AnonymousUserSet -> chatSocketExperimental.reconnectUser(
                        userState.userOrError(),
                        userState is UserState.AnonymousUserSet
                    )
                    else -> error("Invalid user state $userState without user being set!")
                }
                false -> Unit
            }
        }
    }

    public fun addSocketListener(listener: SocketListener) {
        if (ToggleService.isSocketExperimental().not()) {
            socket.addListener(listener)
        } else {
            chatSocketExperimental.addListener(listener)
        }
    }

    public fun removeSocketListener(listener: SocketListener) {
        if (ToggleService.isSocketExperimental().not()) {
            socket.removeListener(listener)
        } else {
            chatSocketExperimental.removeListener(listener)
        }
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
            object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
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
            object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
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

    /**
     * Disconnect the current user, stop all observers and clear user data.
     *
     * @param flushPersistence if true will clear user data.
     *
     * @return Executable async [Call] which performs the disconnection.
     */
    @CheckResult
    public fun disconnect(flushPersistence: Boolean): Call<Unit> =
        CoroutineCall(scope) {
            logger.d { "[disconnect] flushPersistence: $flushPersistence" }
            notifications.onLogout()
            clientState.toMutableState()?.clearState()
            getCurrentUser().let(initializationCoordinator::userDisconnected)
            if (ToggleService.isSocketExperimental().not()) {
                socketStateService.onDisconnectRequested()
                userStateService.onLogout()
                socket.disconnect()
            } else {
                userStateService.onLogout()
                chatSocketExperimental.disconnect(DisconnectCause.ConnectionReleased)
            }
            if (flushPersistence) {
                userCredentialStorage.clear()
            }
            lifecycleObserver.dispose()
            appSettingsManager.clear()
            Result.success(Unit)
        }

    /**
     * Disconnect the current user, stop all observers and clear user data
     */
    @Deprecated(
        message = "It is a blocking method that shouldn't be used from the Main Thread.\n" +
            "Instead of that, you can use `ChatClient.disconnect(true)` that return a `Call` " +
            "and run it safe using coroutines.",
        replaceWith = ReplaceWith("this.disconnect(true).await()"),
        level = DeprecationLevel.WARNING
    )
    @WorkerThread
    public fun disconnect() {
        disconnect(true).execute()
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
        sort: QuerySorter<Message>? = null,
    ): Call<SearchMessagesResult> {
        if (offset != null && (sort != null || next != null)) {
            return ErrorCall(scope, ChatError("Cannot specify offset with sort or next parameters"))
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
        sort: QuerySorter<Message>,
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
    public fun getReplies(messageId: String, limit: Int): Call<List<Message>> {
        val relevantPlugins = plugins.filterIsInstance<ThreadQueryListener>().also(::logPlugins)

        return api.getReplies(messageId, limit)
            .doOnStart(scope) {
                relevantPlugins.forEach { plugin ->
                    logger.d { "Applying ${plugin::class.qualifiedName}.onGetRepliesRequest" }
                    plugin.onGetRepliesRequest(messageId, limit)
                }
            }
            .doOnResult(scope) { result ->
                relevantPlugins.forEach { plugin ->
                    logger.d { "Applying ${plugin::class.qualifiedName}.onGetRepliesResult" }
                    plugin.onGetRepliesResult(result, messageId, limit)
                }
            }
            .precondition(relevantPlugins) { onGetRepliesPrecondition(messageId, limit) }
    }

    @CheckResult
    public fun getRepliesMore(
        messageId: String,
        firstId: String,
        limit: Int,
    ): Call<List<Message>> {
        val relevantPlugins = plugins.filterIsInstance<ThreadQueryListener>().also(::logPlugins)

        return api.getRepliesMore(messageId, firstId, limit)
            .doOnStart(scope) {
                relevantPlugins.forEach { plugin ->
                    logger.d { "Applying ${plugin::class.qualifiedName}.onGetRepliesMoreRequest" }
                    plugin.onGetRepliesMoreRequest(messageId, firstId, limit)
                }
            }
            .doOnResult(scope) { result ->
                relevantPlugins.forEach { plugin ->
                    logger.d { "Applying ${plugin::class.qualifiedName}.onGetRepliesMoreResults" }
                    plugin.onGetRepliesMoreResult(result, messageId, firstId, limit)
                }
            }
            .precondition(relevantPlugins) { onGetRepliesMorePrecondition(messageId, firstId, limit) }
    }

    @CheckResult
    public fun sendAction(request: SendActionRequest): Call<Message> {
        return api.sendAction(request)
    }

    /**
     * Sends selected giphy message to the channel specified by [Message.cid].
     * The call will be retried accordingly to [retryPolicy].
     * @see [RetryPolicy]
     *
     * @param message The message to send.
     *
     * @return Executable async [Call] responsible for sending the Giphy.
     */
    public fun sendGiphy(message: Message): Call<Message> {
        val relevantPlugins = plugins.filterIsInstance<SendGiphyListener>().also(::logPlugins)
        val request = message.run {
            SendActionRequest(cid, id, type, mapOf(KEY_MESSAGE_ACTION to MESSAGE_ACTION_SEND))
        }

        return sendAction(request)
            .retry(scope = scope, retryPolicy = retryPolicy)
            .doOnResult(scope) { result ->
                relevantPlugins.forEach { listener ->
                    logger.d { "Applying ${listener::class.qualifiedName}.onGiphySendResult" }
                    listener.onGiphySendResult(cid = message.cid, result = result)
                }
            }
    }

    /**
     * Performs Giphy shuffle operation in the channel specified by [Message.cid].
     * Returns new "ephemeral" message with new giphy url.
     * The call will be retried accordingly to [retryPolicy].
     * @see [RetryPolicy]
     *
     * @param message The message to send.
     *
     * @return Executable async [Call] responsible for shuffling the Giphy.
     */
    public fun shuffleGiphy(message: Message): Call<Message> {
        val relevantPlugins = plugins.filterIsInstance<ShuffleGiphyListener>().also(::logPlugins)
        val request = message.run {
            SendActionRequest(cid, id, type, mapOf(KEY_MESSAGE_ACTION to MESSAGE_ACTION_SHUFFLE))
        }

        return sendAction(request)
            .retry(scope = scope, retryPolicy = retryPolicy)
            .doOnResult(scope) { result ->
                relevantPlugins.forEach { listener ->
                    logger.d { "Applying ${listener::class.qualifiedName}.onShuffleGiphyResult" }
                    listener.onShuffleGiphyResult(cid = message.cid, result = result)
                }
            }
    }

    @CheckResult
    @JvmOverloads
    public fun deleteMessage(messageId: String, hard: Boolean = false): Call<Message> {
        val relevantPlugins = plugins.filterIsInstance<DeleteMessageListener>().also(::logPlugins)

        return api.deleteMessage(messageId, hard)
            .doOnStart(scope) {
                relevantPlugins.forEach { listener ->
                    logger.d { "Applying ${listener::class.qualifiedName}.onMessageDeleteRequest" }
                    listener.onMessageDeleteRequest(messageId)
                }
            }
            .doOnResult(scope) { result ->
                relevantPlugins.forEach { listener ->
                    logger.d { "Applying ${listener::class.qualifiedName}.onMessageDeleteResult" }
                    listener.onMessageDeleteResult(messageId, result)
                }
            }
            .precondition(relevantPlugins) {
                onMessageDeletePrecondition(messageId)
            }
    }

    @CheckResult
    public fun getMessage(messageId: String): Call<Message> {
        return api.getMessage(messageId)
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
        val relevantPlugins = plugins.filterIsInstance<SendMessageListener>().also(::logPlugins)
        val relevantInterceptors = interceptors.filterIsInstance<SendMessageInterceptor>()
        return CoroutineCall(scope) {

            // Message is first prepared i.e. all its attachments are uploaded and message is updated with
            // these attachments.
            relevantInterceptors.fold(Result.success(message)) { message, interceptor ->
                if (message.isSuccess) {
                    interceptor.interceptMessage(channelType, channelId, message.data(), isRetrying)
                } else message
            }.flatMapSuspend { newMessage ->
                api.sendMessage(channelType, channelId, newMessage)
                    .retry(scope, retryPolicy)
                    .doOnResult(scope) { result ->
                        logger.i { "[sendMessage] result: ${result.stringify { it.toString() }}" }
                        relevantPlugins.forEach { listener ->
                            logger.d { "Applying ${listener::class.qualifiedName}.onMessageSendResult" }
                            listener.onMessageSendResult(
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
     * Updates the message in the API and calls the plugins that handle this request. [OfflinePlugin] can be used here
     * to store the updated message locally.
     *
     * @param message [Message] The message to be updated.
     */
    @CheckResult
    public fun updateMessage(message: Message): Call<Message> {
        val relevantPlugins = plugins.filterIsInstance<EditMessageListener>().also(::logPlugins)

        return api.updateMessage(message)
            .doOnStart(scope) {
                relevantPlugins.forEach { plugin ->
                    logger.d { "Applying ${plugin::class.qualifiedName}.onMessageEditRequest" }
                    plugin.onMessageEditRequest(message)
                }
            }
            .doOnResult(scope) { result ->
                relevantPlugins.forEach { plugin ->
                    logger.d { "Applying ${plugin::class.qualifiedName}.onMessageEditResult" }
                    plugin.onMessageEditResult(message, result)
                }
            }
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

    /**
     * Gets the channels without running any side effects.
     *
     * @param request The request's parameters combined into [QueryChannelsRequest] class.
     *
     * @return Executable async [Call] responsible for querying channels.
     */
    @CheckResult
    @InternalStreamChatApi
    public fun queryChannelsInternal(request: QueryChannelsRequest): Call<List<Channel>> {
        logger.d { "[queryChannelsInternal] request: $request" }
        return callPostponeHelper.postponeCall { api.queryChannels(request) }
    }

    @CheckResult
    @InternalStreamChatApi
    public fun queryChannelInternal(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ): Call<Channel> {
        logger.d { "[queryChannelInternal] cid: $channelType:$channelId, request: $request" }
        return api.queryChannel(channelType, channelId, request)
    }

    /**
     * Gets the channel from the server based on [channelType], [channelId] and parameters from [QueryChannelRequest].
     * The call requires active socket connection and will be automatically postponed and retried until
     * the connection is established or the maximum number of attempts is reached.
     * @see [CallPostponeHelper]
     *
     * @param request The request's parameters combined into [QueryChannelRequest] class.
     *
     * @return Executable async [Call] responsible for querying channels.
     */
    @CheckResult
    public fun queryChannel(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ): Call<Channel> {
        val isConnectionRequired = request.watch || request.presence
        logger.d {
            "[queryChannel] channelType: $channelType, channelId: $channelId, " +
                "isConnectionRequired: $isConnectionRequired"
        }

        val relevantPlugins = plugins.filterIsInstance<QueryChannelListener>().also(::logPlugins)

        val callBuilder = { api.queryChannel(channelType, channelId, request) }
        val queryChannelCall = when (isConnectionRequired) {
            true -> callPostponeHelper.postponeCall(callBuilder)
            else -> callBuilder.invoke()
        }
        return queryChannelCall.doOnStart(scope) {
            relevantPlugins.forEach { plugin ->
                logger.v { "[queryChannel] #doOnStart; plugin: ${plugin::class.qualifiedName}" }
                plugin.onQueryChannelRequest(channelType, channelId, request)
            }
        }.doOnResult(scope) { result ->
            relevantPlugins.forEach { plugin ->
                logger.v { "[queryChannel] #doOnResult; plugin: ${plugin::class.qualifiedName}" }
                plugin.onQueryChannelResult(result, channelType, channelId, request)
            }
        }.precondition(relevantPlugins) {
            onQueryChannelPrecondition(channelType, channelId, request)
        }
    }

    /**
     * Gets the channels from the server based on parameters from [QueryChannelsRequest].
     * The call requires active socket connection and will be automatically postponed and retried until
     * the connection is established or the maximum number of attempts is reached.
     * @see [CallPostponeHelper]
     *
     * @param request The request's parameters combined into [QueryChannelsRequest] class.
     *
     * @return Executable async [Call] responsible for querying channels.
     */
    @CheckResult
    public fun queryChannels(request: QueryChannelsRequest): Call<List<Channel>> {
        logger.d { "[queryChannels] request: $request" }

        val relevantPluginsLazy = { plugins.filterIsInstance<QueryChannelsListener>() }
        logPlugins(relevantPluginsLazy())

        return callPostponeHelper.postponeCall {
            api.queryChannels(request)
        }.doOnStart(scope) {
            relevantPluginsLazy().forEach { listener ->
                logger.v { "[queryChannels] #doOnStart; plugin: ${listener::class.qualifiedName}" }
                listener.onQueryChannelsRequest(request)
            }
        }.doOnResult(scope) { result ->
            relevantPluginsLazy().forEach { listener ->
                logger.v { "[queryChannels] #doOnResult; plugin: ${listener::class.qualifiedName}" }
                listener.onQueryChannelsResult(result, request)
            }
        }.precondition(relevantPluginsLazy()) {
            onQueryChannelsPrecondition(request)
        }
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
        val relevantPlugins = plugins.filterIsInstance<HideChannelListener>().also(::logPlugins)
        return api.hideChannel(channelType, channelId, clearHistory)
            .doOnStart(scope) {
                relevantPlugins.forEach { plugin ->
                    logger.d { "Applying ${plugin::class.qualifiedName}.onHideChannelRequest" }
                    plugin.onHideChannelRequest(channelType, channelId, clearHistory)
                }
            }
            .doOnResult(scope) { result ->
                relevantPlugins.forEach { plugin ->
                    logger.d { "Applying ${plugin::class.qualifiedName}.onHideChannelResult" }
                    plugin.onHideChannelResult(result, channelType, channelId, clearHistory)
                }
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
     * between 1-[MAX_COOLDOWN_TIME_SECONDS].
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
        return if (cooldownTimeInSeconds in 1..MAX_COOLDOWN_TIME_SECONDS) {
            api.enableSlowMode(channelType, channelId, cooldownTimeInSeconds)
        } else {
            ErrorCall(
                scope,
                ChatError(
                    "You can't specify a value outside the range 1-$MAX_COOLDOWN_TIME_SECONDS for cooldown duration."
                )
            )
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
    ): Call<ChatEvent> = api.sendEvent(eventType, channelType, channelId, extraData)

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
    @CheckResult
    public fun markAllRead(): Call<Unit> {
        val relevantPlugins = plugins.filterIsInstance<MarkAllReadListener>().also(::logPlugins)
        return api.markAllRead()
            .doOnStart(scope) {
                relevantPlugins.forEach { plugin ->
                    logger.d { "Applying ${plugin::class.qualifiedName}.onMarkAllReadRequest" }
                    plugin.onMarkAllReadRequest()
                }
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
        val relevantPlugins = plugins.filterIsInstance<ChannelMarkReadListener>().also(::logPlugins)

        return api.markRead(channelType, channelId)
            .precondition(relevantPlugins) { onChannelMarkReadPrecondition(channelType, channelId) }
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
            val errorMessage = "The client-side partial update allows you to update only the current user. " +
                "Make sure the user is set before updating it."
            logger.e { errorMessage }
            return ErrorCall(scope, ChatError(errorMessage))
        }

        return api.partialUpdateUser(
            id = id,
            set = set,
            unset = unset,
        )
    }

    /**
     * Query users matching [query] request.
     *
     * @param query [QueryUsersRequest] with query parameters like filters, sort to get matching users.
     *
     * @return [Call] with a list of [User].
     */
    @CheckResult
    public fun queryUsers(query: QueryUsersRequest): Call<List<User>> = api.queryUsers(query)

    /**
     * Adds members to a given channel.
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     * @param memberIds The list of the member ids to be added.
     * @param systemMessage The system message that will be shown in the channel.
     *
     * @return Executable async [Call] responsible for adding the members.
     */
    @CheckResult
    public fun addMembers(
        channelType: String,
        channelId: String,
        memberIds: List<String>,
        systemMessage: Message? = null,
    ): Call<Channel> {
        return api.addMembers(
            channelType,
            channelId,
            memberIds,
            systemMessage,
        )
    }

    /**
     * Removes members from a given channel.
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     * @param memberIds The list of the member ids to be removed.
     * @param systemMessage The system message that will be shown in the channel.
     *
     * @return Executable async [Call] responsible for removing the members.
     */
    @CheckResult
    public fun removeMembers(
        channelType: String,
        channelId: String,
        memberIds: List<String>,
        systemMessage: Message? = null,
    ): Call<Channel> = api.removeMembers(
        channelType,
        channelId,
        memberIds,
        systemMessage
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
        sort: QuerySorter<BannedUsersSort> = QuerySortByField.ascByName("created_at"),
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

    /**
     * Return the [User] stored on the credential storage
     *
     * @return The stored user or null if it was logged out
     */
    internal fun getStoredUser(): User? = userCredentialStorage.get()?.let {
        User(id = it.userId, name = it.userName)
    }

    @InternalStreamChatApi
    public fun setPushNotificationReceivedListener(pushNotificationReceivedListener: PushNotificationReceivedListener) {
        this.pushNotificationReceivedListener = pushNotificationReceivedListener
    }

    public fun getConnectionId(): String? {
        return runCatching {
            if (ToggleService.isSocketExperimental().not()) {
                socketStateService.state.connectionIdOrError()
            } else {
                chatSocketExperimental.connectionIdOrError()
            }
        }.getOrNull()
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

    /**
     * Returns application settings from the server or the default ones as a fallback.
     *
     * @return The application settings.
     */
    public fun getAppSettings(): AppSettings {
        return appSettingsManager.getAppSettings()
    }

    public fun isSocketConnected(): Boolean {
        return if (ToggleService.isSocketExperimental().not()) socketStateService.state is SocketState.Connected
        else chatSocketExperimental.isConnected()
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

    /**
     * Creates the channel.
     * You can either create an id-based channel by passing not blank [channelId] or
     * member-based (distinct) channel by leaving [channelId] empty.
     * Use [memberIds] list to create a channel together with members. Make sure the list is not empty in case of
     * creating member-based channel!
     * Extra channel's information, for example name, can be passed in the [extraData] map.
     *
     * The call will be retried accordingly to [retryPolicy].
     *
     * @see [Plugin]
     * @see [RetryPolicy]
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     * @param memberIds The list of members' ids.
     * @param extraData Map of key-value pairs that let you store extra data.
     *
     * @return Executable async [Call] responsible for creating the channel.
     */
    @CheckResult
    public fun createChannel(
        channelType: String,
        channelId: String,
        memberIds: List<String>,
        extraData: Map<String, Any>,
    ): Call<Channel> {
        val relevantPlugins = plugins.filterIsInstance<CreateChannelListener>().also(::logPlugins)
        val relevantErrorHandlers = errorHandlers.filterIsInstance<CreateChannelErrorHandler>()
        val currentUser = getCurrentUser()

        return queryChannelInternal(
            channelType = channelType,
            channelId = channelId,
            request = QueryChannelRequest().withData(extraData + mapOf(ModelFields.MEMBERS to memberIds)),
        )
            .retry(scope = scope, retryPolicy = retryPolicy)
            .doOnStart(scope) {
                relevantPlugins.forEach { plugin ->
                    logger.d { "Applying ${plugin::class.qualifiedName}.onCreateChannelRequest" }
                    plugin.onCreateChannelRequest(
                        channelType = channelType,
                        channelId = channelId,
                        memberIds = memberIds,
                        extraData = extraData,
                        currentUser = currentUser!!,
                    )
                }
            }
            .doOnResult(scope) { result ->
                relevantPlugins.forEach { plugin ->
                    logger.d { "Applying ${plugin::class.qualifiedName}.onCreateChannelResult" }
                    plugin.onCreateChannelResult(
                        channelType = channelType,
                        channelId = channelId,
                        memberIds = memberIds,
                        result = result,
                    )
                }
            }
            .onCreateChannelError(
                errorHandlers = relevantErrorHandlers,
                channelType = channelType,
                channelId = channelId,
                memberIds = memberIds,
                extraData = extraData,
            )
            .precondition(relevantPlugins) {
                onCreateChannelPrecondition(
                    currentUser = currentUser,
                    channelId = channelId,
                    memberIds = memberIds,
                )
            }
    }

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
            .withPrecondition(scope) {
                when (channelsIds.isEmpty()) {
                    true -> Result.error(ChatError("channelsIds must contain at least 1 id."))
                    else -> Result.success(Unit)
                }
            }
    }

    /**
     * Sends a [EventType.TYPING_START] event to the backend.
     *
     * @param channelType The type of this channel i.e. messaging etc.
     * @param channelId The id of this channel.
     * @param parentId Set this field to `message.id` to indicate that typing event is happening in a thread.
     *
     * @return Executable async [Call] which completes with [Result] having [ChatEvent] data if successful or
     * [ChatError] if fails.
     */
    @CheckResult
    public fun keystroke(channelType: String, channelId: String, parentId: String? = null): Call<ChatEvent> {
        val extraData: Map<Any, Any> = parentId?.let {
            mapOf(ARG_TYPING_PARENT_ID to parentId)
        } ?: emptyMap()
        val relevantPlugins = plugins.filterIsInstance<TypingEventListener>().also(::logPlugins)
        val eventTime = Date()
        val eventType = EventType.TYPING_START
        return api.sendEvent(
            eventType = eventType,
            channelType = channelType,
            channelId = channelId,
            extraData = extraData,
        )
            .doOnStart(scope) {
                relevantPlugins.forEach { plugin ->
                    logger.d { "Applying ${plugin::class.qualifiedName}.onTypingEventRequest" }
                    plugin.onTypingEventRequest(eventType, channelType, channelId, extraData, eventTime)
                }
            }
            .doOnResult(scope) { result ->
                relevantPlugins.forEach { plugin ->
                    logger.d { "Applying ${plugin::class.qualifiedName}.onTypingEventResult" }
                    plugin.onTypingEventResult(result, eventType, channelType, channelId, extraData, eventTime)
                }
            }
            .precondition(relevantPlugins) {
                this.onTypingEventPrecondition(eventType, channelType, channelId, extraData, eventTime)
            }
    }

    /**
     * Sends a [EventType.TYPING_STOP] event to the backend.
     *
     * @param channelType The type of this channel i.e. messaging etc.
     * @param channelId The id of this channel.
     * @param parentId Set this field to `message.id` to indicate that typing event is happening in a thread.
     *
     * @return Executable async [Call] which completes with [Result] having [ChatEvent] data if successful or
     * [ChatError] if fails.
     */
    @CheckResult
    public fun stopTyping(channelType: String, channelId: String, parentId: String? = null): Call<ChatEvent> {
        val extraData: Map<Any, Any> = parentId?.let {
            mapOf(ARG_TYPING_PARENT_ID to parentId)
        } ?: emptyMap()
        val relevantPlugins = plugins.filterIsInstance<TypingEventListener>().also(::logPlugins)
        val eventTime = Date()
        val eventType = EventType.TYPING_STOP
        return api.sendEvent(
            eventType = eventType,
            channelType = channelType,
            channelId = channelId,
            extraData = extraData,
        )
            .doOnStart(scope) {
                relevantPlugins.forEach { plugin ->
                    logger.d { "Applying ${plugin::class.qualifiedName}.onTypingEventRequest" }
                    plugin.onTypingEventRequest(eventType, channelType, channelId, extraData, eventTime)
                }
            }
            .doOnResult(scope) { result ->
                relevantPlugins.forEach { plugin ->
                    logger.d { "Applying ${plugin::class.qualifiedName}.onTypingEventResult" }
                    plugin.onTypingEventResult(result, eventType, channelType, channelId, extraData, eventTime)
                }
            }
            .precondition(relevantPlugins) {
                this.onTypingEventPrecondition(eventType, channelType, channelId, extraData, eventTime)
            }
    }

    private fun warmUp() {
        if (config.warmUp) {
            api.warmUp()
        }
    }

    private fun isUserSet() = userStateService.state !is UserState.NotSet

    /**
     * Generate a developer token that can be used to connect users while the app is using a development environment.
     *
     * @param userId the desired id of the user to be connected.
     */
    public fun devToken(userId: String): String = tokenUtils.devToken(userId)

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
     * @param apiKey The API key of your Stream Chat app obtained from the
     * [Stream Dashboard](https://dashboard.getstream.io/).
     * @param appContext The application [Context].
     */
    @Suppress("TooManyFunctions")
    public class Builder(private val apiKey: String, private val appContext: Context) : ChatClientBuilder() {

        private var baseUrl: String = "chat.stream-io-api.com"
        private var cdnUrl: String = baseUrl
        private var logLevel = ChatLogLevel.NOTHING
        private var warmUp: Boolean = true
        private var loggerHandler: ChatLoggerHandler? = null
        private var notificationsHandler: NotificationHandler? = null
        private var notificationConfig: NotificationConfig = NotificationConfig(pushNotificationsEnabled = false)
        private var fileUploader: FileUploader? = null
        private val tokenManager: TokenManager = TokenManagerImpl()
        private var customOkHttpClient: OkHttpClient? = null
        private var userCredentialStorage: UserCredentialStorage? = null
        private var retryPolicy: RetryPolicy = NoRetryPolicy()
        private var distinctApiCalls: Boolean = true
        private var debugRequests: Boolean = false

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
         * See the
         * [Push Notifications](https://staging.getstream.io/chat/docs/sdk/android/client/guides/push-notifications/)
         * documentation for more information.
         *
         *
         * @param notificationConfig Config push notification.
         * @param notificationsHandler Your custom class implementation of [NotificationHandler].
         */
        @JvmOverloads
        public fun notifications(
            notificationConfig: NotificationConfig,
            notificationsHandler: NotificationHandler =
                NotificationHandlerFactory.createNotificationHandler(context = appContext),
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
         * For more info, see
         * [the File Uploads documentation](https://getstream.io/chat/docs/android/file_uploads/?language=kotlin).
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
         * By default, this is the URL of Stream's
         * [Edge API Infrastructure](https://getstream.io/blog/chat-edge-infrastructure/),
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

        /**
         * Adds a plugin factory to be used by the client.
         * @see [PluginFactory]
         *
         * @param pluginFactory The factory to be added.
         */
        public fun withPlugin(pluginFactory: PluginFactory): Builder = apply {
            pluginFactories.add(pluginFactory)
        }

        /**
         * Overrides a default, based on shared preferences implementation for [UserCredentialStorage].
         */
        public fun credentialStorage(credentialStorage: UserCredentialStorage): Builder = apply {
            userCredentialStorage = credentialStorage
        }

        /**
         * Debug requests using [ApiRequestsAnalyser]. Use this to debug your requests. This shouldn't be enabled in
         * release builds as it uses a memory cache.
         */
        public fun debugRequests(shouldDebug: Boolean): Builder = apply {
            this.debugRequests = shouldDebug
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

        /**
         * Allows simultaneous network calls of the same request, avoiding combining them into one.
         * By default [distinctApiCalls] is enabled.
         */
        public fun disableDistinctApiCalls(): Builder = apply {
            this.distinctApiCalls = false
        }

        private fun configureInitializer(chatClient: ChatClient) {
            chatClient.initializationCoordinator.addUserConnectionRequestListener { user ->
                chatClient.addPlugins(
                    pluginFactories.map { pluginFactory ->
                        pluginFactory.get(user)
                    }
                )
            }
        }

        public override fun build(): ChatClient {
            return super.build()
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

            // Use clear text traffic for instrumented tests
            val isLocalHost = baseUrl.contains("localhost")
            val httpProtocol = if (isLocalHost) "http" else "https"
            val wsProtocol = if (isLocalHost) "ws" else "wss"
            val lifecycle = ProcessLifecycleOwner.get().lifecycle

            val config = ChatClientConfig(
                apiKey = apiKey,
                httpUrl = "$httpProtocol://$baseUrl/",
                cdnHttpUrl = "$httpProtocol://$cdnUrl/",
                wssUrl = "$wsProtocol://$baseUrl/",
                warmUp = warmUp,
                loggerConfig = ChatLoggerConfigImpl(logLevel, loggerHandler),
                distinctApiCalls = distinctApiCalls,
                debugRequests
            )
            setupStreamLog()

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
                    customOkHttpClient,
                    lifecycle,
                )

            val appSettingsManager = AppSettingManager(module.api())

            return ChatClient(
                config,
                module.api(),
                module.socket(),
                module.notifications(),
                tokenManager,
                module.socketStateService,
                module.callPostponeHelper,
                userCredentialStorage = userCredentialStorage ?: SharedPreferencesCredentialStorage(appContext),
                module.userStateService,
                scope = module.networkScope,
                retryPolicy = retryPolicy,
                appSettingsManager = appSettingsManager,
                chatSocketExperimental = module.experimentalSocket(),
                lifecycle = lifecycle
            ).also {
                configureInitializer(it)
            }
        }

        private fun setupStreamLog() {
            val noLoggerSet = StreamLog.inspect { it is SilentStreamLogger }
            if (noLoggerSet && logLevel != ChatLogLevel.NOTHING) {
                StreamLog.setValidator(StreamLogLevelValidator(logLevel))
                StreamLog.setLogger(
                    CompositeStreamLogger(
                        AndroidStreamLogger(),
                        StreamLoggerHandler(loggerHandler)
                    )
                )
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
         * Create a [ChatClient] instance based on the current configuration
         * of the [Builder].
         */
        public open fun build(): ChatClient = internalBuild()
            .also {
                instance = it
            }

        @InternalStreamChatApi
        public abstract fun internalBuild(): ChatClient
    }

    public companion object {
        /**
         * Header used to track which SDK is being used.
         */
        @InternalStreamChatApi
        @JvmStatic
        public var VERSION_PREFIX_HEADER: VersionPrefixHeader = VersionPrefixHeader.DEFAULT

        /**
         * Flag used to track whether offline support is enabled.
         */
        @InternalStreamChatApi
        @JvmStatic
        public var OFFLINE_SUPPORT_ENABLED: Boolean = false

        private const val MAX_COOLDOWN_TIME_SECONDS = 120
        private const val KEY_MESSAGE_ACTION = "image_action"
        private const val MESSAGE_ACTION_SEND = "send"
        private const val MESSAGE_ACTION_SHUFFLE = "shuffle"

        private const val ARG_TYPING_PARENT_ID = "parent_id"

        private var instance: ChatClient? = null

        @JvmField
        public val DEFAULT_SORT: QuerySorter<Member> = QuerySortByField.descByName("last_updated")

        private const val ANONYMOUS_USER_ID = "!anon"
        private val anonUser by lazy { User(id = ANONYMOUS_USER_ID) }

        @JvmStatic
        public fun instance(): ChatClient {
            return instance
                ?: throw IllegalStateException(
                    "ChatClient.Builder::build() must be called before obtaining ChatClient instance"
                )
        }

        public val isInitialized: Boolean
            get() = instance != null

        /**
         * Handles push message.
         * If user is not connected - automatically restores last user credentials and sets user without
         * connecting to the socket.
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

        /**
         * Builds a detailed header of information we track around the SDK, Android OS, API Level, device name and
         * vendor and more.
         *
         * @return String formatted header that contains all the information.
         */
        internal fun buildSdkTrackingHeaders(): String {
            val clientInformation = VERSION_PREFIX_HEADER.prefix + BuildConfig.STREAM_CHAT_VERSION
            val buildModel = Build.MODEL
            val deviceManufacturer = Build.MANUFACTURER
            val apiLevel = Build.VERSION.SDK_INT
            val osName = "Android ${Build.VERSION.RELEASE}"

            return clientInformation +
                "|os=$osName" +
                "|api_version=$apiLevel" +
                "|device_vendor=$deviceManufacturer" +
                "|device_model=$buildModel" +
                "|offline_enabled=$OFFLINE_SUPPORT_ENABLED"
        }
    }
}
