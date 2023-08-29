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
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import androidx.annotation.CheckResult
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.api.ErrorCall
import io.getstream.chat.android.client.api.models.PinnedMessagesPagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.api.models.SendActionRequest
import io.getstream.chat.android.client.api.models.identifier.DeleteMessageIdentifier
import io.getstream.chat.android.client.api.models.identifier.DeleteReactionIdentifier
import io.getstream.chat.android.client.api.models.identifier.GetMessageIdentifier
import io.getstream.chat.android.client.api.models.identifier.GetRepliesIdentifier
import io.getstream.chat.android.client.api.models.identifier.GetRepliesMoreIdentifier
import io.getstream.chat.android.client.api.models.identifier.HideChannelIdentifier
import io.getstream.chat.android.client.api.models.identifier.MarkAllReadIdentifier
import io.getstream.chat.android.client.api.models.identifier.QueryChannelIdentifier
import io.getstream.chat.android.client.api.models.identifier.QueryChannelsIdentifier
import io.getstream.chat.android.client.api.models.identifier.QueryMembersIdentifier
import io.getstream.chat.android.client.api.models.identifier.SendEventIdentifier
import io.getstream.chat.android.client.api.models.identifier.SendGiphyIdentifier
import io.getstream.chat.android.client.api.models.identifier.SendReactionIdentifier
import io.getstream.chat.android.client.api.models.identifier.ShuffleGiphyIdentifier
import io.getstream.chat.android.client.api.models.identifier.UpdateMessageIdentifier
import io.getstream.chat.android.client.attachment.AttachmentsSender
import io.getstream.chat.android.client.audio.AudioPlayer
import io.getstream.chat.android.client.audio.StreamMediaPlayer
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.channel.state.ChannelStateLogicProvider
import io.getstream.chat.android.client.clientstate.DisconnectCause
import io.getstream.chat.android.client.clientstate.UserState
import io.getstream.chat.android.client.clientstate.UserStateService
import io.getstream.chat.android.client.debugger.ChatClientDebugger
import io.getstream.chat.android.client.debugger.SendMessageDebugger
import io.getstream.chat.android.client.debugger.StubChatClientDebugger
import io.getstream.chat.android.client.di.ChatModule
import io.getstream.chat.android.client.errorhandler.ErrorHandler
import io.getstream.chat.android.client.errorhandler.onCreateChannelError
import io.getstream.chat.android.client.errorhandler.onMessageError
import io.getstream.chat.android.client.errorhandler.onQueryMembersError
import io.getstream.chat.android.client.errorhandler.onReactionError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.ConnectingEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.HasOwnUser
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationChannelMutesUpdatedEvent
import io.getstream.chat.android.client.events.NotificationMutesUpdatedEvent
import io.getstream.chat.android.client.events.UserEvent
import io.getstream.chat.android.client.events.UserUpdatedEvent
import io.getstream.chat.android.client.extensions.ATTACHMENT_TYPE_FILE
import io.getstream.chat.android.client.extensions.ATTACHMENT_TYPE_IMAGE
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.internal.isLaterThanDays
import io.getstream.chat.android.client.header.VersionPrefixHeader
import io.getstream.chat.android.client.helpers.AppSettingManager
import io.getstream.chat.android.client.helpers.CallPostponeHelper
import io.getstream.chat.android.client.interceptor.message.internal.PrepareMessageLogicImpl
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.logger.ChatLoggerConfigImpl
import io.getstream.chat.android.client.logger.ChatLoggerHandler
import io.getstream.chat.android.client.logger.StreamLogLevelValidator
import io.getstream.chat.android.client.logger.StreamLoggerHandler
import io.getstream.chat.android.client.notifications.ChatNotifications
import io.getstream.chat.android.client.notifications.PushNotificationReceivedListener
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.client.notifications.handler.NotificationHandler
import io.getstream.chat.android.client.notifications.handler.NotificationHandlerFactory
import io.getstream.chat.android.client.parser2.adapters.internal.StreamDateFormatter
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.persistance.repository.factory.RepositoryFactory
import io.getstream.chat.android.client.persistance.repository.noop.NoOpRepositoryFactory
import io.getstream.chat.android.client.plugin.DependencyResolver
import io.getstream.chat.android.client.plugin.Plugin
import io.getstream.chat.android.client.plugin.factory.PluginFactory
import io.getstream.chat.android.client.scope.ClientScope
import io.getstream.chat.android.client.scope.UserScope
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.setup.state.internal.MutableClientState
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
import io.getstream.chat.android.client.user.CurrentUserFetcher
import io.getstream.chat.android.client.user.storage.SharedPreferencesCredentialStorage
import io.getstream.chat.android.client.user.storage.UserCredentialStorage
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.TokenUtils
import io.getstream.chat.android.client.utils.internal.toggle.ToggleService
import io.getstream.chat.android.client.utils.mergePartially
import io.getstream.chat.android.client.utils.observable.ChatEventsObservable
import io.getstream.chat.android.client.utils.observable.Disposable
import io.getstream.chat.android.client.utils.retry.NoRetryPolicy
import io.getstream.chat.android.client.utils.stringify
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.AppSettings
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.BannedUser
import io.getstream.chat.android.models.BannedUsersSort
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ConnectionData
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Flag
import io.getstream.chat.android.models.GuestUser
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Mute
import io.getstream.chat.android.models.PushMessage
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.SearchMessagesResult
import io.getstream.chat.android.models.UploadAttachmentsNetworkType
import io.getstream.chat.android.models.UploadedFile
import io.getstream.chat.android.models.UploadedImage
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.VideoCallInfo
import io.getstream.chat.android.models.VideoCallToken
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.log.CompositeStreamLogger
import io.getstream.log.StreamLog
import io.getstream.log.android.AndroidStreamLogger
import io.getstream.log.taggedLogger
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.call.Call
import io.getstream.result.call.CoroutineCall
import io.getstream.result.call.doOnResult
import io.getstream.result.call.doOnStart
import io.getstream.result.call.map
import io.getstream.result.call.retry
import io.getstream.result.call.retry.RetryPolicy
import io.getstream.result.call.share
import io.getstream.result.call.toUnitCall
import io.getstream.result.call.withPrecondition
import io.getstream.result.flatMapSuspend
import io.getstream.result.onErrorSuspend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import java.io.File
import java.util.Calendar
import java.util.Date
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.days

/**
 * The ChatClient is the main entry point for all low-level operations on chat
 */
@Suppress("NEWER_VERSION_IN_SINCE_KOTLIN", "TooManyFunctions", "LargeClass")
public class ChatClient
@Suppress("LongParameterList")
internal constructor(
    public val config: ChatClientConfig,
    private val api: ChatApi,
    private val notifications: ChatNotifications,
    private val tokenManager: TokenManager = TokenManagerImpl(),
    private val userCredentialStorage: UserCredentialStorage,
    private val userStateService: UserStateService = UserStateService(),
    private val clientDebugger: ChatClientDebugger = StubChatClientDebugger,
    private val tokenUtils: TokenUtils = TokenUtils,
    private val clientScope: ClientScope,
    private val userScope: UserScope,
    internal val retryPolicy: RetryPolicy,
    private val appSettingsManager: AppSettingManager,
    private val chatSocket: ChatSocket,
    private val pluginFactories: List<PluginFactory>,
    private val mutableClientState: MutableClientState,
    private val currentUserFetcher: CurrentUserFetcher,
    private val repositoryFactoryProvider: RepositoryFactory.Provider,
    @InternalStreamChatApi
    public val audioPlayer: AudioPlayer,
) {
    private val logger by taggedLogger("Chat:Client")
    private val waitConnection = MutableSharedFlow<Result<ConnectionData>>()
    public val clientState: ClientState = mutableClientState

    private val streamDateFormatter: StreamDateFormatter = StreamDateFormatter()
    private val eventsObservable = ChatEventsObservable(waitConnection, userScope, chatSocket)
    private val eventMutex = Mutex()

    /**
     * The user's id for which the client is initialized.
     * Used in [initializeClientWithUser] to prevent recreating objects like repository, plugins, etc.
     */
    private val initializedUserId = AtomicReference<String?>(null)

    /**
     * Launches a new coroutine in the [UserScope] without blocking the current thread
     * and returns a reference to the coroutine as a [Job].
     */
    internal fun launch(
        block: suspend CoroutineScope.() -> Unit,
    ) = userScope.launch(block = block)

    /**
     * Inherits the [UserScope] and provides its [Job] as an anchor for children.
     */
    @InternalStreamChatApi
    public fun inheritScope(block: (Job) -> CoroutineContext): CoroutineScope {
        return userScope + block(userScope.coroutineContext.job)
    }

    @InternalStreamChatApi
    public val repositoryFacade: RepositoryFacade
        get() = _repositoryFacade
            ?: (getCurrentUser() ?: getStoredUser())
                ?.let { user ->
                    createRepositoryFacade(userScope, createRepositoryFactory(user))
                        .also { _repositoryFacade = it }
                }
            ?: createRepositoryFacade(userScope)

    private var _repositoryFacade: RepositoryFacade? = null

    private var pushNotificationReceivedListener: PushNotificationReceivedListener =
        PushNotificationReceivedListener { _, _ -> }

    /**
     * The list of plugins added once user is connected.
     *
     * @see [Plugin]
     */
    @InternalStreamChatApi
    public var plugins: List<Plugin> = emptyList()

    /**
     * Resolves dependency [T] within the provided plugin [P].
     * This method can't be called before user is connected because plugins are added only after user
     * connection is completed.
     *
     * @see [Plugin]
     * @throws IllegalStateException if plugin was not added or dependency is not found.
     */
    @InternalStreamChatApi
    @Throws(IllegalStateException::class)
    @Suppress("ThrowsCount")
    public inline fun <reified P : Plugin, reified T : Any> resolveDependency(): T {
        if (clientState.initializationState.value != InitializationState.COMPLETE) {
            throw IllegalStateException("ChatClient::connectUser() must be called before resolving any dependency")
        }
        val resolver = plugins.find { plugin ->
            plugin is P && plugin is DependencyResolver
        } as? DependencyResolver
            ?: throw IllegalStateException(
                "Plugin '${P::class.qualifiedName}' was not found. Did you init it within ChatClient?",
            )
        return resolver.resolveDependency(T::class)
            ?: throw IllegalStateException(
                "Dependency '${T::class.qualifiedName}' was not resolved from plugin '${P::class.qualifiedName}'",
            )
    }

    /**
     * Error handlers for API calls.
     */
    private val errorHandlers: List<ErrorHandler>
        get() = plugins.mapNotNull { it.errorHandler }.sorted()

    public var logicRegistry: ChannelStateLogicProvider? = null

    internal lateinit var attachmentsSender: AttachmentsSender

    init {
        eventsObservable.subscribeSuspend { event ->
            eventMutex.withLock {
                handleEvent(event)
            }
        }
        logger.i { "Initialised: ${buildSdkTrackingHeaders()}" }
    }

    private suspend fun handleEvent(event: ChatEvent) {
        when (event) {
            is ConnectedEvent -> {
                logger.i { "[handleEvent] event: ConnectedEvent(userId='${event.me.id}')" }
                val user = event.me
                val connectionId = event.connectionId
                api.setConnection(user.id, connectionId)
                notifications.onSetUser()

                mutableClientState.setConnectionState(ConnectionState.Connected)
                mutableClientState.setUser(user)
            }

            is NewMessageEvent -> {
                notifications.onNewMessageEvent(event)
            }

            is ConnectingEvent -> {
                logger.i { "[handleEvent] event: ConnectingEvent" }
                mutableClientState.setConnectionState(ConnectionState.Connecting)
            }

            is UserUpdatedEvent -> {
                val eventUser = event.user
                val currentUser = clientState.user.value
                if (currentUser?.id == eventUser.id) {
                    val mergedUser = currentUser.mergePartially(eventUser)
                    mutableClientState.setUser(mergedUser)
                }
            }

            is NotificationMutesUpdatedEvent -> {
                mutableClientState.setUser(event.me)
            }

            is NotificationChannelMutesUpdatedEvent -> {
                mutableClientState.setUser(event.me)
            }

            is DisconnectedEvent -> {
                logger.i { "[handleEvent] event: DisconnectedEvent(disconnectCause=${event.disconnectCause})" }
                api.releseConnection()
                when (event.disconnectCause) {
                    is DisconnectCause.ConnectionReleased,
                    is DisconnectCause.NetworkNotAvailable,
                    is DisconnectCause.WebSocketNotAvailable,
                    is DisconnectCause.Error,
                    -> {
                    }

                    is DisconnectCause.UnrecoverableError -> {
                        userStateService.onSocketUnrecoverableError()
                    }
                }
                mutableClientState.setConnectionState(ConnectionState.Offline)
            }

            else -> Unit // Ignore other events
        }

        event.extractCurrentUser()?.let { currentUser ->
            userStateService.onUserUpdated(currentUser)
            mutableClientState.setUser(currentUser)
            storePushNotificationsConfig(
                currentUser.id,
                currentUser.name,
                userStateService.state !is UserState.UserSet,
            )
        }
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

        return when {
            tokenUtils.getUserId(cacheableTokenProvider.loadToken()) != user.id -> {
                logger.e {
                    "The user_id provided on the JWT token doesn't match with the current user you try to connect"
                }
                Result.Failure(
                    Error.GenericError(
                        "The user_id provided on the JWT token doesn't match with the current user you try to connect",
                    ),
                )
            }

            userState is UserState.NotSet -> {
                logger.v { "[setUser] user is NotSet" }
                mutableClientState.setUser(user)
                initializeClientWithUser(user, cacheableTokenProvider, isAnonymous)
                userStateService.onSetUser(user, isAnonymous)
                chatSocket.connectUser(user, isAnonymous)
                mutableClientState.setInitializationState(InitializationState.COMPLETE)
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
                        Result.Failure(
                            Error.GenericError(
                                "User cannot be set until the previous one is disconnected.",
                            ),
                        )
                    }

                    else -> {
                        getConnectionId()?.let {
                            mutableClientState.setInitializationState(InitializationState.COMPLETE)
                            Result.Success(ConnectionData(userState.user, it))
                        }
                            ?: run {
                                logger.e {
                                    "[setUser] Trying to connect the same user twice without a previously completed " +
                                        "connection."
                                }
                                Result.Failure(
                                    Error.GenericError(
                                        "Failed to connect user. Please check you haven't connected a user already.",
                                    ),
                                )
                            }
                    }
                }
            }

            else -> {
                logger.e { "[setUser] Failed to connect user. Please check you don't have connected user already." }
                Result.Failure(
                    Error.GenericError(
                        "Failed to connect user. Please check you don't have connected user already.",
                    ),
                )
            }
        }.onErrorSuspend {
            disconnectSuspend(flushPersistence = true)
        }
    }

    @Synchronized
    private fun initializeClientWithUser(
        user: User,
        tokenProvider: CacheableTokenProvider,
        isAnonymous: Boolean,
    ) {
        logger.i { "[initializeClientWithUser] user.id: '${user.id}'" }
        val clientJobCount = clientScope.coroutineContext[Job]?.children?.count() ?: -1
        val userJobCount = userScope.coroutineContext[Job]?.children?.count() ?: -1
        logger.v { "[initializeClientWithUser] clientJobCount: $clientJobCount, userJobCount: $userJobCount" }
        if (initializedUserId.get() != user.id) {
            _repositoryFacade = createRepositoryFacade(userScope, createRepositoryFactory(user))
            plugins = pluginFactories.map { it.get(user) }
            initializedUserId.set(user.id)
        } else {
            logger.i {
                "[initializeClientWithUser] initializing client with the same user id." +
                    " Skipping repository and plugins recreation"
            }
        }
        plugins.forEach { it.onUserSet(user) }
        // fire a handler here that the chatDomain and chatUI can use
        config.isAnonymous = isAnonymous
        tokenManager.setTokenProvider(tokenProvider)
        appSettingsManager.loadAppSettings()
        warmUp()
        logger.i { "[initializeClientWithUser] user.id: '${user.id}'completed" }
    }

    private fun createRepositoryFactory(user: User): RepositoryFactory =
        repositoryFactoryProvider.createRepositoryFactory(user)

    private fun createRepositoryFacade(
        scope: CoroutineScope,
        repositoryFactory: RepositoryFactory = NoOpRepositoryFactory,
    ): RepositoryFacade =
        RepositoryFacade.create(repositoryFactory, scope)

    /**
     * Get the current settings of the app. Check [AppSettings].
     *
     * @return [AppSettings] the settings of the app.
     */
    @CheckResult
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
        return CoroutineCall(clientScope) {
            userScope.userId.value = user.id
            connectUserSuspend(user, tokenProvider, timeoutMilliseconds)
        }
    }

    private suspend fun connectUserSuspend(
        user: User,
        tokenProvider: TokenProvider,
        timeoutMilliseconds: Long?,
    ): Result<ConnectionData> {
        mutableClientState.setInitializationState(InitializationState.INITIALIZING)
        logger.d { "[connectUserSuspend] userId: '${user.id}', username: '${user.name}'" }
        return setUser(user, tokenProvider, timeoutMilliseconds).also { result ->
            logger.v {
                "[connectUserSuspend] " +
                    "completed: ${result.stringify { "ConnectionData(connectionId=${it.connectionId})" }}"
            }
        }
    }

    /**
     * Changes the user. Disconnects the current user and connects to a new one.
     * The [tokenProvider] implementation is used for the initial token,
     * and it's also invoked whenever the user's token has expired, to fetch a new token.
     *
     * This method disconnects from the SDK and right after connects to it with the new User.
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
    public fun switchUser(
        user: User,
        tokenProvider: TokenProvider,
        timeoutMilliseconds: Long? = null,
        onDisconnectionComplete: () -> Unit = {},
    ): Call<ConnectionData> {
        return CoroutineCall(clientScope) {
            logger.d { "[switchUser] user.id: '${user.id}'" }
            userScope.userId.value = user.id
            disconnectUserSuspend(flushPersistence = true)
            onDisconnectionComplete()
            connectUserSuspend(user, tokenProvider, timeoutMilliseconds).also {
                logger.v { "[switchUser] completed('${user.id}')" }
            }
        }
    }

    /**
     * Changes the user. Disconnects the current user and connects to a new one.
     * The [tokenProvider] implementation is used for the initial token,
     * and it's also invoked whenever the user's token has expired, to fetch a new token.
     *
     * This method disconnects from the SDK and right after connects to it with the new User.
     *
     * @see TokenProvider
     *
     * @param user The user to set.
     * @param token Instance of JWT token.
     * @param timeoutMilliseconds The timeout in milliseconds to be waiting until the connection is established.
     *
     * @return Executable [Call] responsible for connecting the user.
     */
    @CheckResult
    @JvmOverloads
    public fun switchUser(
        user: User,
        token: String,
        timeoutMilliseconds: Long? = null,
        onDisconnectionComplete: () -> Unit = {},
    ): Call<ConnectionData> {
        return switchUser(user, ConstantTokenProvider(token), timeoutMilliseconds, onDisconnectionComplete)
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
    internal suspend fun setUserWithoutConnectingIfNeeded() {
        if (clientState.initializationState.value == InitializationState.INITIALIZING) {
            delay(INITIALIZATION_DELAY)
            return setUserWithoutConnectingIfNeeded()
        } else if (isUserSet() || clientState.initializationState.value == InitializationState.COMPLETE) {
            logger.d {
                "[setUserWithoutConnectingIfNeeded] User is already set: ${isUserSet()}" +
                    " Initialization state: ${clientState.initializationState.value}"
            }
            return
        }

        userCredentialStorage.get()?.let { config ->
            initializeClientWithUser(
                User(
                    id = config.userId,
                    name = config.userName,
                ),
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
        return CoroutineCall(clientScope) {
            logger.d { "[connectAnonymousUser] no args" }
            userScope.userId.value = ANONYMOUS_USER_ID
            setUser(
                anonUser,
                ConstantTokenProvider(devToken(ANONYMOUS_USER_ID)),
                timeoutMilliseconds,
            ).also { result ->
                logger.v {
                    "[connectAnonymousUser] " +
                        "completed: ${result.stringify { "ConnectionData(connectionId=${it.connectionId})" }}"
                }
            }
        }
    }

    private suspend fun waitFirstConnection(timeoutMilliseconds: Long?): Result<ConnectionData> =
        timeoutMilliseconds?.let {
            withTimeoutOrNull(timeoutMilliseconds) { waitConnection.first() }
                ?: Result.Failure(
                    Error.GenericError("Connection wasn't established in ${timeoutMilliseconds}ms"),
                )
        } ?: waitConnection.first()

    @CheckResult
    @JvmOverloads
    public fun connectGuestUser(
        userId: String,
        username: String,
        timeoutMilliseconds: Long? = null,
    ): Call<ConnectionData> {
        return CoroutineCall(clientScope) {
            logger.d { "[connectGuestUser] userId: '$userId', username: '$username'" }
            userScope.userId.value = userId

            getGuestToken(userId, username).await()
                .flatMapSuspend { setUser(it.user, ConstantTokenProvider(it.token), timeoutMilliseconds) }
                .onSuccess { connectionData ->
                    logger.v {
                        "[connectGuestUser] completed: ConnectionData(connectionId=${connectionData.connectionId})"
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
        logger.d { "[queryMembers] cid: $channelType:$channelId, offset: $offset, limit: $limit" }
        return api.queryMembers(channelType, channelId, offset, limit, filter, sort, members)
            .doOnResult(userScope) { result ->
                plugins.forEach { plugin ->
                    logger.v { "[queryMembers] #doOnResult; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onQueryMembersResult(
                        result,
                        channelType,
                        channelId,
                        offset,
                        limit,
                        filter,
                        sort,
                        members,
                    )
                    logger.v { "[queryMembers] result: ${result.stringify { "Members(count=${it.size})" }}" }
                }
            }
            .onQueryMembersError(errorHandlers, channelType, channelId, offset, limit, filter, sort, members)
            .share(userScope) { QueryMembersIdentifier(channelType, channelId, offset, limit, filter, sort, members) }
    }

    /**
     * Uploads a file for the given channel. Progress can be accessed via [callback].
     *
     * The Stream CDN imposes the following restrictions on file uploads:
     * - The maximum file size is 100 MB
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     * @param file The file that needs to be uploaded.
     * @param callback The callback to track progress.
     *
     * @return Executable async [Call] which completes with [Result] containing an instance of [UploadedFile]
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
    ): Call<UploadedFile> {
        return api.sendFile(channelType, channelId, file, callback)
    }

    /**
     * Uploads an image for the given channel. Progress can be accessed via [callback].
     *
     * The Stream CDN imposes the following restrictions on image uploads:
     * - The maximum image size is 100 MB
     * - Supported MIME types are listed in [StreamCdnImageMimeTypes.SUPPORTED_IMAGE_MIME_TYPES]
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId Еhe channel id. ie 123.
     * @param file The image file that needs to be uploaded.
     * @param callback The callback to track progress.
     *
     * @return Executable async [Call] which completes with [Result] containing an instance of [UploadedImage]
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
    ): Call<UploadedImage> {
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
        val currentUser = getCurrentUser()

        return api.deleteReaction(messageId = messageId, reactionType = reactionType)
            .retry(scope = userScope, retryPolicy = retryPolicy)
            .doOnStart(userScope) {
                plugins.forEach { plugin ->
                    logger.v { "[deleteReaction] #doOnStart; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onDeleteReactionRequest(
                        cid = cid,
                        messageId = messageId,
                        reactionType = reactionType,
                        currentUser = currentUser!!,
                    )
                }
            }
            .doOnResult(userScope) { result ->
                plugins.forEach { plugin ->
                    logger.v { "[deleteReaction] #doOnResult; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onDeleteReactionResult(
                        cid = cid,
                        messageId = messageId,
                        reactionType = reactionType,
                        currentUser = currentUser!!,
                        result = result,
                    )
                }
            }
            .precondition(plugins) { onDeleteReactionPrecondition(currentUser) }
            .onMessageError(errorHandlers, cid, messageId)
            .share(userScope) { DeleteReactionIdentifier(messageId, reactionType, cid) }
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
        val currentUser = getCurrentUser()

        return api.sendReaction(reaction, enforceUnique)
            .retry(scope = userScope, retryPolicy = retryPolicy)
            .doOnStart(userScope) {
                plugins
                    .forEach { plugin ->
                        logger.v { "[sendReaction] #doOnStart; plugin: ${plugin::class.qualifiedName}" }
                        plugin.onSendReactionRequest(
                            cid = cid,
                            reaction = reaction,
                            enforceUnique = enforceUnique,
                            currentUser = currentUser!!,
                        )
                    }
            }
            .doOnResult(userScope) { result ->
                plugins.forEach { plugin ->
                    logger.v { "[sendReaction] #doOnResult; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onSendReactionResult(
                        cid = cid,
                        reaction = reaction,
                        enforceUnique = enforceUnique,
                        currentUser = currentUser!!,
                        result = result,
                    )
                }
            }
            .onReactionError(errorHandlers, reaction, enforceUnique, currentUser!!)
            .precondition(plugins) { onSendReactionPrecondition(currentUser, reaction) }
            .share(userScope) { SendReactionIdentifier(reaction, enforceUnique, cid) }
    }
    //endregion

    //endregion

    @CheckResult
    public fun disconnectSocket(): Call<Unit> =
        CoroutineCall(userScope) {
            Result.Success(chatSocket.disconnect())
        }

    /**
     * Fetches the current user.
     * Works only if the user was previously set and the WS connections is closed.
     */
    public fun fetchCurrentUser(): Call<User> {
        return CoroutineCall(userScope) {
            logger.d { "[fetchCurrentUser] isUserSet: ${isUserSet()}, isSocketConnected: ${isSocketConnected()}" }
            when {
                !isUserSet() -> Result.Failure(Error.GenericError("User is not set, can't fetch current user"))
                isSocketConnected() -> Result.Failure(
                    Error.GenericError(
                        "Socket is connected, can't fetch current user",
                    ),
                )
                else -> currentUserFetcher.fetch(getCurrentUser()!!)
            }
        }.doOnResult(userScope) { result ->
            logger.v { "[fetchCurrentUser] completed: $result" }
            result.getOrNull()?.also { currentUser ->
                mutableClientState.setUser(currentUser)
            }
            plugins.forEach { plugin ->
                logger.v { "[fetchCurrentUser] #doOnResult; plugin: ${plugin::class.qualifiedName}" }
                plugin.onFetchCurrentUserResult(result)
            }
        }
    }

    /**
     * Reconnects the socket.
     * Works only if the user was previously set and the WS connections is closed.
     */
    @CheckResult
    public fun reconnectSocket(): Call<Unit> =
        CoroutineCall(userScope) {
            when (val userState = userStateService.state) {
                is UserState.UserSet, is UserState.AnonymousUserSet -> Result.Success(
                    chatSocket.reconnectUser(
                        userState.userOrError(),
                        userState is UserState.AnonymousUserSet,
                        true,
                    ),
                )

                else -> Result.Failure(Error.GenericError("Invalid user state $userState without user being set!"))
            }
        }

    public fun addSocketListener(listener: SocketListener) {
        chatSocket.addListener(listener)
    }

    public fun removeSocketListener(listener: SocketListener) {
        chatSocket.removeListener(listener)
    }

    public fun subscribe(
        listener: ChatEventListener<ChatEvent>,
    ): Disposable {
        return eventsObservable.subscribe(listener = listener)
    }

    /**
     * Subscribes to the specific [eventTypes] of the client.
     *
     * @see [EventType] for type constants
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
            },
        )

        lifecycleOwner.lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    disposable.dispose()
                }
            },
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
            },
        )

        lifecycleOwner.lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    disposable.dispose()
                }
            },
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
     * Clear local data stored on the device from the current user.
     *
     * This method can be called even if the user is not connected, on that case the stored credentials
     * will be used.
     *
     * If there is already a connection alive for the current user, it will be disconnected.
     *
     * @return Executable async [Call] which performs the cleanup.
     */
    @CheckResult
    public fun clearPersistence(): Call<Unit> =
        CoroutineCall(clientScope) {
            disconnectSuspend(true)
            Result.Success(Unit)
        }.doOnStart(clientScope) { setUserWithoutConnectingIfNeeded() }

    /**
     * Disconnect the current user, stop all observers and clear user data.
     * This method should only be used whenever the user logouts from the main app.
     * You shouldn't call this method, if the user will continue using the Chat in the future.
     *
     * @param flushPersistence if true will clear user data.
     *
     * @return Executable async [Call] which performs the disconnection.
     */
    @CheckResult
    public fun disconnect(flushPersistence: Boolean): Call<Unit> =
        CoroutineCall(clientScope) {
            logger.d { "[disconnect] flushPersistence: $flushPersistence" }
            when (isUserSet()) {
                true -> {
                    disconnectSuspend(flushPersistence)
                    Result.Success(Unit)
                }

                false -> {
                    logger.i { "[disconnect] cannot disconnect as the user wasn't connected" }
                    Result.Failure(
                        Error.GenericError(
                            message = "ChatClient can't be disconnected because user wasn't connected previously",
                        ),
                    )
                }
            }
        }

    private suspend fun disconnectSuspend(flushPersistence: Boolean) {
        disconnectUserSuspend(flushPersistence)
        userScope.userId.value = null
    }

    private suspend fun disconnectUserSuspend(flushPersistence: Boolean) {
        val userId = getCurrentUser()?.id
        initializedUserId.set(null)
        logger.d { "[disconnectUserSuspend] userId: '$userId', flushPersistence: $flushPersistence" }

        notifications.onLogout(flushPersistence)
        plugins.forEach { it.onUserDisconnected() }
        plugins = emptyList()
        userStateService.onLogout()
        chatSocket.disconnect()
        clientState.awaitConnectionState(ConnectionState.Offline)
        userScope.cancelChildren(userId)

        if (flushPersistence) {
            repositoryFacade.clear()
            userCredentialStorage.clear()
        }

        _repositoryFacade = null
        appSettingsManager.clear()

        mutableClientState.clearState()

        audioPlayer.dispose()
        logger.v { "[disconnectUserSuspend] completed('$userId')" }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun ClientState.awaitConnectionState(
        state: ConnectionState,
        timeoutInMillis: Long = DEFAULT_CONNECTION_STATE_TIMEOUT,
    ) = try {
        withTimeout(timeoutInMillis) {
            connectionState.first {
                it == state
            }
        }
    } catch (e: Throwable) {
        logger.e { "[awaitConnectionState] failed: $e" }
    }

    //region: api calls

    @CheckResult
    public fun getDevices(): Call<List<Device>> {
        return api.getDevices()
    }

    @CheckResult
    internal fun deleteDevice(device: Device): Call<Unit> {
        return api.deleteDevice(device)
    }

    @CheckResult
    internal fun addDevice(device: Device): Call<Unit> {
        return api.addDevice(device)
    }

    /**
     * Dismiss notifications from a given [channelType] and [channelId].
     * Be sure to initialize ChatClient before calling this method!
     *
     * @param channelType String that represent the channel type of the channel you want to dismiss notifications.
     * @param channelId String that represent the channel id of the channel you want to dismiss notifications.
     *
     */
    public fun dismissChannelNotifications(channelType: String, channelId: String) {
        notifications.dismissChannelNotifications(channelType, channelId)
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
            return ErrorCall(userScope, Error.GenericError("Cannot specify offset with sort or next parameters"))
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
        logger.d { "[getReplies] messageId: $messageId, limit: $limit" }

        return api.getReplies(messageId, limit)
            .doOnStart(userScope) {
                plugins.forEach { plugin ->
                    logger.v { "[getReplies] #doOnStart; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onGetRepliesRequest(messageId, limit)
                }
            }
            .doOnResult(userScope) { result ->
                plugins.forEach { plugin ->
                    logger.v { "[getReplies] #doOnResult; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onGetRepliesResult(result, messageId, limit)
                }
            }
            .precondition(plugins) { onGetRepliesPrecondition(messageId, limit) }
            .share(userScope) { GetRepliesIdentifier(messageId, limit) }
    }

    @CheckResult
    public fun getRepliesMore(
        messageId: String,
        firstId: String,
        limit: Int,
    ): Call<List<Message>> {
        logger.d { "[getRepliesMore] messageId: $messageId, firstId: $firstId, limit: $limit" }

        return api.getRepliesMore(messageId, firstId, limit)
            .doOnStart(userScope) {
                plugins.forEach { plugin ->
                    logger.v { "[getRepliesMore] #doOnStart; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onGetRepliesMoreRequest(messageId, firstId, limit)
                }
            }
            .doOnResult(userScope) { result ->
                plugins.forEach { plugin ->
                    logger.v { "[getRepliesMore] #doOnResult; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onGetRepliesMoreResult(result, messageId, firstId, limit)
                }
            }
            .precondition(plugins) { onGetRepliesMorePrecondition(messageId, firstId, limit) }
            .share(userScope) { GetRepliesMoreIdentifier(messageId, firstId, limit) }
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
    @CheckResult
    public fun sendGiphy(message: Message): Call<Message> {
        val request = message.run {
            SendActionRequest(cid, id, type, mapOf(KEY_MESSAGE_ACTION to MESSAGE_ACTION_SEND))
        }

        return sendAction(request)
            .retry(scope = userScope, retryPolicy = retryPolicy)
            .doOnResult(userScope) { result ->
                plugins.forEach { listener ->
                    logger.v { "[sendGiphy] #doOnResult; plugin: ${listener::class.qualifiedName}" }
                    listener.onGiphySendResult(cid = message.cid, result = result)
                }
            }
            .share(userScope) { SendGiphyIdentifier(request) }
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
    @CheckResult
    public fun shuffleGiphy(message: Message): Call<Message> {
        val request = message.run {
            SendActionRequest(cid, id, type, mapOf(KEY_MESSAGE_ACTION to MESSAGE_ACTION_SHUFFLE))
        }

        return sendAction(request)
            .retry(scope = userScope, retryPolicy = retryPolicy)
            .doOnResult(userScope) { result ->
                plugins.forEach { listener ->
                    logger.v { "[shuffleGiphy] #doOnResult; plugin: ${listener::class.qualifiedName}" }
                    listener.onShuffleGiphyResult(cid = message.cid, result = result)
                }
            }
            .share(userScope) { ShuffleGiphyIdentifier(request) }
    }

    @CheckResult
    @JvmOverloads
    public fun deleteMessage(messageId: String, hard: Boolean = false): Call<Message> {
        logger.d { "[deleteMessage] messageId: $messageId, hard: $hard" }

        return api.deleteMessage(messageId, hard)
            .doOnStart(userScope) {
                plugins.forEach { listener ->
                    logger.v { "[deleteMessage] #doOnStart; plugin: ${listener::class.qualifiedName}" }
                    listener.onMessageDeleteRequest(messageId)
                }
            }
            .doOnResult(userScope) { result ->
                plugins.forEach { listener ->
                    logger.v { "[deleteMessage] #doOnResult; plugin: ${listener::class.qualifiedName}" }
                    listener.onMessageDeleteResult(messageId, result)
                }
            }
            .precondition(plugins) {
                onMessageDeletePrecondition(messageId)
            }
            .share(userScope) { DeleteMessageIdentifier(messageId, hard) }
    }

    /**
     * Fetches a single message from the backend.
     *
     * @param messageId The ID of the message we are fetching from the backend.
     *
     * @return The message wrapped inside [Result] if the call was successful,
     * otherwise returns a [Error] instance wrapped inside [Result].
     */
    @CheckResult
    public fun getMessage(messageId: String): Call<Message> {
        logger.d { "[getMessage] messageId: $messageId" }

        return api.getMessage(messageId)
            .doOnResult(
                userScope,
            ) { result ->
                plugins.forEach { listener ->
                    logger.v { "[getMessage] #doOnResult; plugin: ${listener::class.qualifiedName}" }
                    listener.onGetMessageResult(messageId, result)
                }
            }
            .share(userScope) { GetMessageIdentifier(messageId) }
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
    public fun sendMessage(
        channelType: String,
        channelId: String,
        message: Message,
        isRetrying: Boolean = false,
    ): Call<Message> {
        val debugger = clientDebugger.debugSendMessage(channelType, channelId, message, isRetrying)
        return CoroutineCall(userScope) {
            debugger.onStart(message)
            sendAttachments(channelType, channelId, message, isRetrying, debugger)
                .flatMapSuspend { newMessage ->
                    doSendMessage(channelType, channelId, newMessage, debugger)
                }
                .also { result ->
                    debugger.onStop(result)
                }
        }
    }

    private suspend fun doSendMessage(
        channelType: String,
        channelId: String,
        message: Message,
        debugger: SendMessageDebugger,
    ): Result<Message> {
        debugger.onSendStart(message)
        return api.sendMessage(channelType, channelId, message)
            .retry(userScope, retryPolicy)
            .doOnResult(userScope) { result ->
                logger.i { "[sendMessage] result: ${result.stringify { it.toString() }}" }
                plugins.forEach { listener ->
                    logger.v { "[sendMessage] #doOnResult; plugin: ${listener::class.qualifiedName}" }
                    listener.onMessageSendResult(result, channelType, channelId, message)
                }
            }.await()
            .also { result ->
                debugger.onSendStop(result)
            }
    }

    private suspend fun sendAttachments(
        channelType: String,
        channelId: String,
        message: Message,
        isRetrying: Boolean = false,
        debugger: SendMessageDebugger,
    ): Result<Message> {
        debugger.onInterceptionStart(message)
        val prepareMessageLogic = PrepareMessageLogicImpl(clientState, logicRegistry)

        val preparedMessage = getCurrentUser()?.let { user ->
            prepareMessageLogic.prepareMessage(message, channelId, channelType, user)
        } ?: message
        debugger.onInterceptionUpdate(preparedMessage)

        plugins.forEach { listener -> listener.onAttachmentSendRequest(channelType, channelId, preparedMessage) }

        return attachmentsSender
            .sendAttachments(preparedMessage, channelType, channelId, isRetrying, repositoryFacade)
            .also { result ->
                debugger.onInterceptionStop(result)
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
        return api.updateMessage(message)
            .doOnStart(userScope) {
                plugins.forEach { plugin ->
                    logger.v { "[updateMessage] #doOnStart; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onMessageEditRequest(message)
                }
            }
            .doOnResult(userScope) { result ->
                plugins.forEach { plugin ->
                    logger.v { "[updateMessage] #doOnResult; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onMessageEditResult(message, result)
                }
            }
            .share(userScope) { UpdateMessageIdentifier(message) }
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
            set = set,
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
                "pin_expires" to calendar.time,
            ),
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
            set = mapOf("pinned" to false),
        )
    }

    /**
     * Gets the channels without running any side effects.
     *
     * @param request The request's parameters combined into [QueryChannelsRequest] class.
     *
     * @see [queryChannels]
     *
     * @return Executable async [Call] responsible for querying channels.
     */
    @CheckResult
    @InternalStreamChatApi
    public fun queryChannelsInternal(request: QueryChannelsRequest): Call<List<Channel>> {
        return api.queryChannels(request)
    }

    /**
     * Runs [queryChannel] without applying side effects.
     *
     * @see [queryChannel]
     */
    @CheckResult
    private fun queryChannelInternal(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ): Call<Channel> = api.queryChannel(channelType, channelId, request)

    /**
     * Gets the channel from the server based on [channelType], [channelId] and parameters from [QueryChannelRequest].
     * The call requires active socket connection if [QueryChannelRequest.watch] or [QueryChannelRequest.presence] is
     * enabled, and will be automatically postponed and retried until the connection is established or the maximum
     * number of attempts is reached.
     *
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
        skipOnRequest: Boolean = false,
    ): Call<Channel> {
        logger.d { "[queryChannel] cid: $channelType:$channelId" }
        return queryChannelInternal(channelType = channelType, channelId = channelId, request = request)
            .doOnStart(userScope) {
                if (!skipOnRequest) {
                    plugins.forEach { plugin ->
                        logger.v { "[queryChannel] #doOnStart; plugin: ${plugin::class.qualifiedName}" }
                        plugin.onQueryChannelRequest(channelType, channelId, request)
                    }
                }
            }.doOnResult(userScope) { result ->
                plugins.forEach { plugin ->
                    logger.v { "[queryChannel] #doOnResult; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onQueryChannelResult(result, channelType, channelId, request)
                }
            }.precondition(plugins) {
                onQueryChannelPrecondition(channelType, channelId, request)
            }.share(userScope) {
                QueryChannelIdentifier(channelType, channelId, request)
            }
    }

    /**
     * Gets the channels from the server based on parameters from [QueryChannelsRequest].
     * The call requires active socket connection if [QueryChannelsRequest.watch] or [QueryChannelsRequest.presence] is
     * enabled, and will be automatically postponed and retried until the connection is established or
     * the maximum number of attempts is reached.
     * @see [CallPostponeHelper]
     *
     * @param request The request's parameters combined into [QueryChannelsRequest] class.
     *
     * @return Executable async [Call] responsible for querying channels.
     */
    @CheckResult
    public fun queryChannels(request: QueryChannelsRequest): Call<List<Channel>> {
        logger.d { "[queryChannels] offset: ${request.offset}, limit: ${request.limit}" }
        return queryChannelsInternal(request = request).doOnStart(userScope) {
            plugins.forEach { listener ->
                logger.v { "[queryChannels] #doOnStart; plugin: ${listener::class.qualifiedName}" }
                listener.onQueryChannelsRequest(request)
            }
        }.doOnResult(userScope) { result ->
            plugins.forEach { listener ->
                logger.v { "[queryChannels] #doOnResult; plugin: ${listener::class.qualifiedName}" }
                listener.onQueryChannelsResult(result, request)
            }
        }.precondition(plugins) {
            onQueryChannelsPrecondition(request)
        }.share(userScope) {
            QueryChannelsIdentifier(request)
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
        logger.d { "[hideChannel] cid: $channelType:$channelId, clearHistory: $clearHistory" }
        return api.hideChannel(channelType, channelId, clearHistory)
            .doOnStart(userScope) {
                plugins.forEach { plugin ->
                    logger.v { "[hideChannel] #doOnStart; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onHideChannelRequest(channelType, channelId, clearHistory)
                }
            }
            .doOnResult(userScope) { result ->
                plugins.forEach { plugin ->
                    logger.v { "[hideChannel] #doOnResult; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onHideChannelResult(result, channelType, channelId, clearHistory)
                }
            }
            .precondition(plugins) { onHideChannelPrecondition(channelType, channelId, clearHistory) }
            .share(userScope) { HideChannelIdentifier(channelType, channelId, clearHistory) }
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
            systemMessage = systemMessage,
        )
    }

    /**
     * Stops watching the channel which means you won't receive more events for the channel.
     * The call requires active socket connection and will be automatically postponed and
     * retried until the connection is established.
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     *
     * @return Executable async [Call] responsible for stop watching the channel.
     */
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
            unset = unset,
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
                userScope,
                Error.GenericError(
                    "You can't specify a value outside the range 1-$MAX_COOLDOWN_TIME_SECONDS for cooldown duration.",
                ),
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
        return api.markAllRead()
            .doOnStart(userScope) {
                plugins.forEach { plugin ->
                    logger.v { "[markAllRead] #doOnStart; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onMarkAllReadRequest()
                }
            }
            .share(userScope) { MarkAllReadIdentifier() }
    }

    /**
     * Marks the specified channel as read.
     *
     * @param channelType Type of the channel.
     * @param channelId Id of the channel.
     */
    @CheckResult
    public fun markRead(channelType: String, channelId: String): Call<Unit> {
        return api.markRead(channelType, channelId)
            .precondition(plugins) { onChannelMarkReadPrecondition(channelType, channelId) }
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
            return ErrorCall(userScope, Error.GenericError(errorMessage))
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
     * The call requires active socket connection if [QueryUsersRequest.presence] is enabled, and will be
     * automatically postponed and retried until the connection is established.
     *
     * @param query [QueryUsersRequest] with query parameters like filters, sort to get matching users.
     *
     * @return [Call] with a list of [User].
     */
    @CheckResult
    public fun queryUsers(query: QueryUsersRequest): Call<List<User>> {
        return api.queryUsers(query)
    }

    /**
     * Adds members to a given channel.
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     * @param memberIds The list of the member ids to be added.
     * @param systemMessage The system message that will be shown in the channel.
     * @param hideHistory Hides the history of the channel to the added member.
     *
     * @return Executable async [Call] responsible for adding the members.
     */
    @CheckResult
    public fun addMembers(
        channelType: String,
        channelId: String,
        memberIds: List<String>,
        systemMessage: Message? = null,
        hideHistory: Boolean? = null,
    ): Call<Channel> {
        return api.addMembers(
            channelType,
            channelId,
            memberIds,
            systemMessage,
            hideHistory,
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
        systemMessage,
    )

    /**
     * Invites members to a given channel.
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     * @param memberIds The list of the member ids to be invited.
     * @param systemMessage The system message that will be shown in the channel.
     *
     * @return Executable async [Call] responsible for inviting the members.
     */
    @CheckResult
    public fun inviteMembers(
        channelType: String,
        channelId: String,
        memberIds: List<String>,
        systemMessage: Message? = null,
    ): Call<Channel> = api.inviteMembers(
        channelType,
        channelId,
        memberIds,
        systemMessage,
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
            expiration = expiration,
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
        shadow = false,
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
        shadow = false,
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
        shadow = true,
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
        shadow = true,
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
        return runCatching { chatSocket.connectionIdOrError() }.getOrNull()
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

    public fun isSocketConnected(): Boolean = chatSocket.isConnected()

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
        val currentUser = getCurrentUser()

        val request = QueryChannelRequest()
            .withData(extraData + mapOf(QueryChannelRequest.KEY_MEMBERS to memberIds))
        return queryChannelInternal(
            channelType = channelType,
            channelId = channelId,
            request = request,
        )
            .retry(scope = userScope, retryPolicy = retryPolicy)
            .doOnStart(userScope) {
                plugins.forEach { plugin ->
                    logger.v { "[createChannel] #doOnStart; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onCreateChannelRequest(
                        channelType = channelType,
                        channelId = channelId,
                        memberIds = memberIds,
                        extraData = extraData,
                        currentUser = currentUser!!,
                    )
                }
            }
            .doOnResult(userScope) { result ->
                plugins.forEach { plugin ->
                    logger.v { "[createChannel] #doOnResult; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onCreateChannelResult(
                        channelType = channelType,
                        channelId = channelId,
                        memberIds = memberIds,
                        result = result,
                    )
                }
            }
            .onCreateChannelError(
                errorHandlers = errorHandlers,
                channelType = channelType,
                channelId = channelId,
                memberIds = memberIds,
                extraData = extraData,
            )
            .precondition(plugins) {
                onCreateChannelPrecondition(
                    currentUser = currentUser,
                    channelId = channelId,
                    memberIds = memberIds,
                )
            }
            .share(userScope) { QueryChannelIdentifier(channelType, channelId, request) }
    }

    /**
     * Returns all events that happened for a list of channels since last sync (while the user was not
     * connected to the web-socket).
     *
     * @param channelsIds The list of channel CIDs. Cannot be empty.
     * @param lastSyncAt The last time the user was online and in sync. Shouldn't be later than 30 days.
     *
     * @return Executable async [Call] responsible for obtaining missing events.
     */
    @CheckResult
    public fun getSyncHistory(
        channelsIds: List<String>,
        lastSyncAt: Date,
    ): Call<List<ChatEvent>> {
        val stringDate = streamDateFormatter.format(lastSyncAt)

        return api.getSyncHistory(channelsIds, stringDate)
            .withPrecondition(userScope) {
                checkSyncHistoryPreconditions(channelsIds, lastSyncAt)
            }
    }

    /**
     * Returns all events that happened for a list of channels since last sync (while the user was not
     * connected to the web socket). [lastSyncAt] is in _yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'_ format.  Use this version of
     * getSyncHistory when high precision is necessary.
     *
     * @param channelsIds The list of channel CIDs. Cannot be empty.
     * @param lastSyncAt The last time the user was online and in sync. Shouldn't be later than 30 days.
     *
     * @return Executable async [Call] responsible for obtaining missing events.
     */
    @CheckResult
    public fun getSyncHistory(
        channelsIds: List<String>,
        lastSyncAt: String,
    ): Call<List<ChatEvent>> {
        val parsedDate = streamDateFormatter.parse(lastSyncAt) ?: return ErrorCall(
            userScope,
            Error.GenericError(
                "The string for data: $lastSyncAt could not be parsed for format: ${streamDateFormatter.datePattern}",
            ),
        )

        return api.getSyncHistory(channelsIds, lastSyncAt)
            .withPrecondition(userScope) {
                checkSyncHistoryPreconditions(channelsIds, parsedDate)
            }
    }

    /**
     * Checks if sync history request parameters meet preconditions:
     * 1. If [channelsIds] is not empty.
     * 2. If [lastSyncAt] is no later than 30 days
     */
    private fun checkSyncHistoryPreconditions(channelsIds: List<String>, lastSyncAt: Date): Result<Unit> {
        return when {
            channelsIds.isEmpty() -> {
                Result.Failure(Error.GenericError("channelsIds must contain at least 1 id."))
            }

            lastSyncAt.isLaterThanDays(THIRTY_DAYS_IN_MILLISECONDS) -> {
                Result.Failure(Error.GenericError("lastSyncAt cannot by later than 30 days."))
            }

            else -> {
                Result.Success(Unit)
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
     * [Error] if fails.
     */
    @CheckResult
    public fun keystroke(channelType: String, channelId: String, parentId: String? = null): Call<ChatEvent> {
        val extraData: Map<Any, Any> = parentId?.let {
            mapOf(ARG_TYPING_PARENT_ID to parentId)
        } ?: emptyMap()
        val eventTime = Date()
        val eventType = EventType.TYPING_START
        return api.sendEvent(
            eventType = eventType,
            channelType = channelType,
            channelId = channelId,
            extraData = extraData,
        )
            .doOnStart(userScope) {
                plugins.forEach { plugin ->
                    logger.v { "[keystroke] #doOnStart; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onTypingEventRequest(eventType, channelType, channelId, extraData, eventTime)
                }
            }
            .doOnResult(userScope) { result ->
                plugins.forEach { plugin ->
                    logger.v { "[keystroke] #doOnResult; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onTypingEventResult(result, eventType, channelType, channelId, extraData, eventTime)
                }
            }
            .precondition(plugins) {
                this.onTypingEventPrecondition(eventType, channelType, channelId, extraData, eventTime)
            }
            .share(userScope) { SendEventIdentifier(eventType, channelType, channelId, parentId) }
    }

    /**
     * Sends a [EventType.TYPING_STOP] event to the backend.
     *
     * @param channelType The type of this channel i.e. messaging etc.
     * @param channelId The id of this channel.
     * @param parentId Set this field to `message.id` to indicate that typing event is happening in a thread.
     *
     * @return Executable async [Call] which completes with [Result] having [ChatEvent] data if successful or
     * [Error] if fails.
     */
    @CheckResult
    public fun stopTyping(channelType: String, channelId: String, parentId: String? = null): Call<ChatEvent> {
        val extraData: Map<Any, Any> = parentId?.let {
            mapOf(ARG_TYPING_PARENT_ID to parentId)
        } ?: emptyMap()
        val eventTime = Date()
        val eventType = EventType.TYPING_STOP
        return api.sendEvent(
            eventType = eventType,
            channelType = channelType,
            channelId = channelId,
            extraData = extraData,
        )
            .doOnStart(userScope) {
                plugins.forEach { plugin ->
                    logger.v { "[stopTyping] #doOnStart; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onTypingEventRequest(eventType, channelType, channelId, extraData, eventTime)
                }
            }
            .doOnResult(userScope) { result ->
                plugins.forEach { plugin ->
                    logger.v { "[stopTyping] #doOnResult; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onTypingEventResult(result, eventType, channelType, channelId, extraData, eventTime)
                }
            }
            .precondition(plugins) {
                this.onTypingEventPrecondition(eventType, channelType, channelId, extraData, eventTime)
            }
            .share(userScope) { SendEventIdentifier(eventType, channelType, channelId, parentId) }
    }

    /**
     * Creates a newly available video call, which belongs to a channel.
     * The video call will be created based on the third-party video integration (Agora and 100ms) on your
     * [Stream Dashboard](https://dashboard.getstream.io/).
     *
     * You can set the call type by passing [callType] like `video` or `audio`.
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The id of the channel.
     * @param callType Represents call type such as `video` or `audio`.
     * @param callId A unique identifier to assign to the call. The id is case-insensitive.
     */
    @CheckResult
    public fun createVideoCall(
        channelType: String,
        channelId: String,
        callType: String,
        callId: String,
    ): Call<VideoCallInfo> {
        return api.createVideoCall(
            channelType = channelType,
            channelId = channelId,
            callType = callType,
            callId = callId,
        )
    }

    /**
     * Returns the currently available video call token.
     *
     * @param callId The call id, which indicates a dedicated video call id on the channel.
     */
    @CheckResult
    public fun getVideoCallToken(callId: String): Call<VideoCallToken> {
        return api.getVideoCallToken(callId = callId)
    }

    /**
     * Downloads the given file which can be fetched through the response body.
     *
     * @param fileUrl The URL of the file that we are downloading.
     *
     * @return A Retrofit [ResponseBody] wrapped inside a [Call].
     */
    @InternalStreamChatApi
    @CheckResult
    public fun downloadFile(fileUrl: String): Call<ResponseBody> {
        return api.downloadFile(fileUrl)
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

    @CheckResult
    internal fun <R, T : Any> Call<T>.precondition(
        pluginsList: List<R>,
        preconditionCheck: suspend R.() -> Result<Unit>,
    ): Call<T> =
        withPrecondition(userScope) {
            pluginsList.fold(Result.Success(Unit) as Result<Unit>) { result, plugin ->
                when (result) {
                    is Result.Failure -> result
                    is Result.Success -> {
                        when (val preconditionResult = preconditionCheck(plugin)) {
                            is Result.Failure -> preconditionResult
                            is Result.Success -> result
                        }
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
        private var clientDebugger: ChatClientDebugger? = null
        private var notificationsHandler: NotificationHandler? = null
        private var notificationConfig: NotificationConfig = NotificationConfig(pushNotificationsEnabled = false)
        private var fileUploader: FileUploader? = null
        private val tokenManager: TokenManager = TokenManagerImpl()
        private var customOkHttpClient: OkHttpClient? = null
        private var userCredentialStorage: UserCredentialStorage? = null
        private var retryPolicy: RetryPolicy = NoRetryPolicy()
        private var distinctApiCalls: Boolean = true
        private var debugRequests: Boolean = false
        private var repositoryFactoryProvider: RepositoryFactory.Provider? = null
        private var uploadAttachmentsNetworkType = UploadAttachmentsNetworkType.CONNECTED

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
         * Sets a [ChatClientDebugger] instance that will be invoked accordingly through various flows within SDK.
         *
         * Use this to debug SDK inner processes like [Message] sending.
         *
         * @param clientDebugger Your custom [ChatClientDebugger] implementation.
         */
        public fun clientDebugger(clientDebugger: ChatClientDebugger): Builder {
            this.clientDebugger = clientDebugger
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
         * which has a 100 MB upload size limit.
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
         * Inject a [RepositoryFactory.Provider] to use your own DB Persistence mechanism.
         */
        public fun withRepositoryFactoryProvider(provider: RepositoryFactory.Provider): Builder = apply {
            repositoryFactoryProvider = provider
        }

        /**
         * Adds plugins factory to be used by the client.
         * @see [PluginFactory]
         *
         * @param pluginFactories The factories to be added.
         */
        public fun withPlugins(vararg pluginFactories: PluginFactory): Builder = apply {
            this.pluginFactories.addAll(pluginFactories)
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

        /**
         * An enumeration of various network types used as a constraint inside upload attachments worker.
         */
        public fun uploadAttachmentsNetworkType(type: UploadAttachmentsNetworkType): Builder = apply {
            this.uploadAttachmentsNetworkType = type
        }

        public override fun build(): ChatClient {
            return super.build()
        }

        @InternalStreamChatApi
        @SuppressWarnings("LongMethod")
        override fun internalBuild(): ChatClient {
            if (apiKey.isEmpty()) {
                throw IllegalStateException("apiKey is not defined in " + this::class.java.simpleName)
            }

            instance?.run {
                Log.e(
                    "Chat",
                    "[ERROR] You have just re-initialized ChatClient, old configuration has been overridden [ERROR]",
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
                debugRequests,
            )
            setupStreamLog()

            if (ToggleService.isInitialized().not()) {
                ToggleService.init(appContext, emptyMap())
            }
            val clientScope = ClientScope()
            val userScope = UserScope(clientScope)
            val module =
                ChatModule(
                    appContext,
                    clientScope,
                    userScope,
                    config,
                    notificationsHandler ?: NotificationHandlerFactory.createNotificationHandler(appContext),
                    notificationConfig,
                    fileUploader,
                    tokenManager,
                    customOkHttpClient,
                    lifecycle,
                )

            val appSettingsManager = AppSettingManager(module.api())

            val audioPlayer: AudioPlayer = StreamMediaPlayer(
                mediaPlayer = MediaPlayer().apply {
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                        .let(this::setAudioAttributes)
                },
                userScope = userScope,
            )

            return ChatClient(
                config,
                module.api(),
                module.notifications(),
                tokenManager,
                userCredentialStorage = userCredentialStorage ?: SharedPreferencesCredentialStorage(appContext),
                userStateService = module.userStateService,
                clientDebugger = clientDebugger ?: StubChatClientDebugger,
                clientScope = clientScope,
                userScope = userScope,
                retryPolicy = retryPolicy,
                appSettingsManager = appSettingsManager,
                chatSocket = module.chatSocket,
                pluginFactories = pluginFactories,
                repositoryFactoryProvider = repositoryFactoryProvider
                    ?: pluginFactories
                        .filterIsInstance<RepositoryFactory.Provider>()
                        .firstOrNull()
                    ?: NoOpRepositoryFactory.Provider,
                mutableClientState = MutableClientState(module.networkStateProvider),
                currentUserFetcher = module.currentUserFetcher,
                audioPlayer = audioPlayer,
            ).apply {
                attachmentsSender = AttachmentsSender(
                    context = appContext,
                    networkType = uploadAttachmentsNetworkType,
                    clientState = clientState,
                    scope = clientScope,
                )
            }
        }

        private fun setupStreamLog() {
            if (!StreamLog.isInstalled && logLevel != ChatLogLevel.NOTHING) {
                StreamLog.setValidator(StreamLogLevelValidator(logLevel))
                StreamLog.install(
                    CompositeStreamLogger(
                        AndroidStreamLogger(),
                        StreamLoggerHandler(loggerHandler),
                    ),
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
        private const val DEFAULT_CONNECTION_STATE_TIMEOUT = 10_000L
        private const val KEY_MESSAGE_ACTION = "image_action"
        private const val MESSAGE_ACTION_SEND = "send"
        private const val MESSAGE_ACTION_SHUFFLE = "shuffle"
        private val THIRTY_DAYS_IN_MILLISECONDS = 30.days.inWholeMilliseconds
        private const val INITIALIZATION_DELAY = 100L

        private const val ARG_TYPING_PARENT_ID = "parent_id"

        private var instance: ChatClient? = null

        @JvmField
        public val DEFAULT_SORT: QuerySorter<Member> = QuerySortByField.descByName("last_updated")

        internal const val ANONYMOUS_USER_ID = "!anon"
        private val anonUser by lazy { User(id = ANONYMOUS_USER_ID) }

        @JvmStatic
        public fun instance(): ChatClient {
            return instance
                ?: throw IllegalStateException(
                    "ChatClient.Builder::build() must be called before obtaining ChatClient instance",
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
                clientScope.launch {
                    setUserWithoutConnectingIfNeeded()
                    notifications.onPushMessage(pushMessage, pushNotificationReceivedListener)
                }
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
         * Sets device.
         * Be sure to initialize ChatClient before calling this method!
         *
         * @throws IllegalStateException if called before initializing ChatClient
         */
        @Throws(IllegalStateException::class)
        internal fun setDevice(device: Device) {
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
