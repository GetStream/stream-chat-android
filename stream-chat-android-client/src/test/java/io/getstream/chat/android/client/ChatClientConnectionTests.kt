/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

import androidx.lifecycle.testing.TestLifecycleOwner
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.ChatApiConfig
import io.getstream.chat.android.client.api2.mapping.DtoMapping
import io.getstream.chat.android.client.audio.AudioPlayer
import io.getstream.chat.android.client.clientstate.DisconnectCause
import io.getstream.chat.android.client.clientstate.UserStateService
import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.ErrorEvent
import io.getstream.chat.android.client.internal.offline.repository.factory.internal.DatabaseRepositoryFactory
import io.getstream.chat.android.client.network.NetworkStateProvider
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.client.parser2.adapters.internal.StreamDateFormatter
import io.getstream.chat.android.client.persistance.repository.ScheduledMessageRepository
import io.getstream.chat.android.client.persistance.repository.factory.RepositoryFactory
import io.getstream.chat.android.client.persistance.repository.noop.NoOpRepositoryFactory
import io.getstream.chat.android.client.persistence.repository.ChatClientRepository
import io.getstream.chat.android.client.plugin.Plugin
import io.getstream.chat.android.client.plugin.factory.PluginFactory
import io.getstream.chat.android.client.scheduled.InMemoryScheduledMessageRepository
import io.getstream.chat.android.client.scheduled.LocalScheduledMessage
import io.getstream.chat.android.client.scheduled.randomLocalScheduledMessage
import io.getstream.chat.android.client.scope.ClientTestScope
import io.getstream.chat.android.client.scope.UserTestScope
import io.getstream.chat.android.client.setup.state.internal.MutableClientState
import io.getstream.chat.android.client.socket.FakeChatSocket
import io.getstream.chat.android.client.token.FakeTokenManager
import io.getstream.chat.android.client.user.CredentialConfig
import io.getstream.chat.android.client.user.storage.UserCredentialStorage
import io.getstream.chat.android.client.utils.TokenUtils
import io.getstream.chat.android.client.utils.internal.ServerClockOffset
import io.getstream.chat.android.models.ConnectionData
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.models.GuestUser
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.ScheduledMessage
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should not contain`
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.Mockito.RETURNS_MOCKS
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

internal class ChatClientConnectionTests {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private lateinit var chatApi: ChatApi
    private lateinit var userStateService: UserStateService
    private lateinit var clientScope: ClientTestScope
    private lateinit var client: ChatClient
    lateinit var fakeChatSocket: FakeChatSocket
    private val tokenUtils: TokenUtils = mock()
    private val userId = randomString()
    private val jwt = randomString()
    private val anonjwt = randomString()
    private val user = User(id = userId)
    private val anonId = "!anon"
    private val anonUser = User(id = anonId)
    private val mutableClientState: MutableClientState = MutableClientState(mock())
    private val streamDateFormatter = StreamDateFormatter()
    private val config = mock<ChatApiConfig>()
    private val userCredentialStorage = mock<UserCredentialStorage>()
    private val pluginFactories = mutableListOf<PluginFactory>()
    private val repositoryFactories = mutableMapOf<String, RepositoryFactory>()
    private val audioPlayer = mock<AudioPlayer>()
    private val repository = mock<ChatClientRepository>()
    private lateinit var networkStateProvider: NetworkStateProvider

    @BeforeEach
    fun setup() {
        whenever(tokenUtils.devToken(eq(anonId))) doReturn anonjwt
        whenever(tokenUtils.getUserId(jwt)) doReturn userId
        whenever(tokenUtils.getUserId(anonjwt)) doReturn anonId
        val lifecycleOwner = TestLifecycleOwner(coroutineDispatcher = testCoroutines.dispatcher)
        chatApi = mock()
        userStateService = UserStateService()
        clientScope = ClientTestScope(testCoroutines.scope)
        val userScope = UserTestScope(clientScope)
        val lifecycleObserver = StreamLifecycleObserver(userScope, lifecycleOwner.lifecycle)
        val tokenManager = FakeTokenManager("")
        networkStateProvider = mock()
        whenever(networkStateProvider.isConnected()) doReturn true
        fakeChatSocket = FakeChatSocket(
            userScope = userScope,
            lifecycleObserver = lifecycleObserver,
            tokenManager = tokenManager,
            networkStateProvider = networkStateProvider,
        )
        client = ChatClient(
            config = config,
            api = chatApi,
            dtoMapping = DtoMapping(NoOpMessageTransformer, NoOpUserTransformer),
            notifications = mock(),
            tokenManager = tokenManager,
            userCredentialStorage = userCredentialStorage,
            userStateService = userStateService,
            tokenUtils = tokenUtils,
            clientScope = clientScope,
            userScope = userScope,
            retryPolicy = mock(),
            appSettingsManager = mock(),
            chatSocket = fakeChatSocket,
            serverClockOffset = ServerClockOffset(),
            pluginFactories = pluginFactories,
            repositoryFactoryProvider = object : RepositoryFactory.Provider {
                override fun createRepositoryFactory(user: User): RepositoryFactory =
                    repositoryFactories[user.id] ?: NoOpRepositoryFactory
            },
            mutableClientState = mutableClientState,
            currentUserFetcher = mock(),
            audioPlayer = audioPlayer,
            repository = repository,
            messageReceiptReporter = mock(),
            messageReceiptManager = mock(),
        ).apply {
            attachmentsSender = mock()
        }
    }

    @Test
    fun `Connect an user with a different userId than the one into the JWT should return an error`() = runTest {
        whenever(tokenUtils.getUserId(eq(jwt))) doReturn randomString()

        val result = client.connectUser(user, jwt).await()

        result.shouldBeInstanceOf(Result.Failure::class)
        val error = (result as Result.Failure).value
        error.shouldBeInstanceOf(Error.GenericError::class)
        error.message `should be equal to`
            "The user_id provided on the JWT token doesn't match with the current user you try to connect"
        (error as Error.GenericError).code `should be equal to` ChatErrorCode.INVALID_TOKEN.code
    }

    @Test
    fun `Connect an user with a blank token should return an undefined-token error`() = runTest {
        val result = client.connectUser(user, "").await()

        result.shouldBeInstanceOf(Result.Failure::class)
        val error = (result as Result.Failure).value
        error.shouldBeInstanceOf(Error.GenericError::class)
        // Distinguished by code; message stays unchanged from the pre-existing behaviour.
        (error as Error.GenericError).code `should be equal to` ChatErrorCode.UNDEFINED_TOKEN.code
        error.message `should be equal to`
            "The user_id provided on the JWT token doesn't match with the current user you try to connect"
    }

    @Test
    fun `When connection is successful, initialisation state should be updated`() = runCancellableTest {
        val connectionId = randomString()
        val createdAt = Date()
        val rawCreatedAt = streamDateFormatter.format(createdAt)

        val event = ConnectedEvent(EventType.HEALTH_CHECK, createdAt, rawCreatedAt, user, connectionId)

        val deferred = testCoroutines.scope.async { client.connectUser(user, jwt).await() }
        fakeChatSocket.mockEventReceived(event)
        val result = deferred.await()

        result.shouldBeInstanceOf(Result.Success::class)
        mutableClientState.initializationState.value `should be equal to` InitializationState.COMPLETE
    }

    @Test
    fun `Connect an user when alive connection exists with a different user should return error`() = runCancellableTest {
        val connectionId = randomString()
        prepareAliveConnection(user, connectionId)

        val differentJwt = randomString()
        val differentUser = randomUser()
        whenever(tokenUtils.getUserId(differentJwt)).thenReturn(differentUser.id)
        val result = client.connectUser(differentUser, differentJwt).await()

        result.shouldBeInstanceOf(Result.Failure::class)
        (result as Result.Failure).value.message`should be equal to`
            "User cannot be set until the previous one is disconnected."
    }

    @Test
    fun `Connect an user when alive connection exists with the same user should return a success`() = runCancellableTest {
        val connectionId = randomString()
        prepareAliveConnection(user, connectionId)

        val result = client.connectUser(user, jwt).await()

        result.shouldBeInstanceOf(Result.Success::class)
        (result as Result.Success).value `should be equal to` ConnectionData(user, connectionId)
    }

    @Test
    fun `Connect an user when no previous connection was performed should return a success`() = runCancellableTest {
        val connectionId = randomString()
        val createdAt = Date()
        val rawCreatedAt = streamDateFormatter.format(createdAt)
        val event = ConnectedEvent(EventType.HEALTH_CHECK, createdAt, rawCreatedAt, user, connectionId)

        val deferred = testCoroutines.scope.async { client.connectUser(user, jwt).await() }
        fakeChatSocket.mockEventReceived(event)
        val result = deferred.await()

        result.shouldBeInstanceOf(Result.Success::class)
        (result as Result.Success).value `should be equal to` ConnectionData(user, connectionId)
    }

    @Test
    fun `Where there is a connection error connecting an user, it should be propagated`() = runCancellableTest {
        val messageError = randomString()
        val createdAt = Date()
        val rawCreatedAt = streamDateFormatter.format(createdAt)
        val event = ErrorEvent(EventType.HEALTH_CHECK, createdAt, rawCreatedAt, Error.GenericError(message = messageError))

        val localScope = testCoroutines.scope + Job()
        val deferred = localScope.async {
            client.connectUser(user, jwt).await()
        }
        fakeChatSocket.mockEventReceived(event)
        val result = deferred.await()

        result.shouldBeInstanceOf(Result.Failure::class)
        (result as Result.Failure).value.message `should be equal to` messageError
    }

    @Test
    fun `When the server rejects the connection, local data should be kept`() = runCancellableTest {
        val error = Error.NetworkError(
            message = randomString(),
            serverErrorCode = ChatErrorCode.API_KEY_NOT_FOUND.code,
        )
        val event = ErrorEvent(EventType.CONNECTION_ERROR, Date(), streamDateFormatter.format(Date()), error)

        val deferred = (testCoroutines.scope + Job()).async { client.connectUser(user, jwt).await() }
        fakeChatSocket.mockEventReceived(event)
        val result = deferred.await()

        result.shouldBeInstanceOf(Result.Failure::class)
        verify(userCredentialStorage, never()).clear()
    }

    @Test
    fun `When the socket fails with an unrecoverable error during connectUser, the client disconnects once`() =
        runCancellableTest {
            val error = Error.NetworkError(
                message = randomString(),
                serverErrorCode = ChatErrorCode.API_KEY_NOT_FOUND.code,
            )
            val event = DisconnectedEvent(
                EventType.CONNECTION_DISCONNECTED,
                Date(),
                rawCreatedAt = null,
                disconnectCause = DisconnectCause.UnrecoverableError(error),
            )

            val deferred = (testCoroutines.scope + Job()).async { client.connectUser(user, jwt).await() }
            fakeChatSocket.mockEventReceived(event)
            val result = deferred.await()
            delay(10L)

            result.shouldBeInstanceOf(Result.Failure::class)
            verify(audioPlayer, times(1)).dispose()
        }

    @Test
    fun `When there is an ongoing connection with the same user, an error should be propagated`() = runTest {
        userStateService.onSetUser(user, false)

        val result = client.connectUser(user, jwt).await()

        result.shouldBeInstanceOf(Result.Failure::class)
        (result as Result.Failure).value.message `should be equal to` "Failed to connect user. Please check you haven't connected a user already."
    }

    @Test
    fun `When connection take more time than expected an error should be propagated`() = runTest {
        val result = client.connectUser(user, jwt, 1).await()

        result.shouldBeInstanceOf(Result.Failure::class)
        (result as Result.Failure).value.message `should be equal to` "Connection wasn't established in 1ms"
    }

    @Test
    fun `When connection times out, local data should be kept`() = runTest {
        val result = client.connectUser(user, jwt, 1).await()

        result.shouldBeInstanceOf(Result.Failure::class)
        verify(userCredentialStorage, never()).clear()
    }

    @Test
    fun `When the token is blank, local data should be kept`() = runTest {
        val result = client.connectUser(user, "").await()

        result.shouldBeInstanceOf(Result.Failure::class)
        verify(userCredentialStorage, never()).clear()
    }

    @Test
    fun `When offline without a timeout, connecting should wait for the network and keep local data`() = runCancellableTest {
        whenever(networkStateProvider.isConnected()) doReturn false

        val deferred = (testCoroutines.scope + Job()).async { client.connectUser(user, jwt).await() }
        testScheduler.advanceUntilIdle()

        deferred.isCompleted `should be equal to` false
        verify(userCredentialStorage, never()).clear()
        mutableClientState.initializationState.value `should be equal to` InitializationState.COMPLETE
        client.getCurrentUser() `should be equal to` user
        deferred.cancel()
    }

    @Test
    fun `When there is an user connected and try to connect a different user, an error should be propagated`() =
        runTest {
            userStateService.onSetUser(user, false)

            val result = client.connectUser(user, jwt).await()

            result.shouldBeInstanceOf(Result.Failure::class)
            (result as Result.Failure).value.message `should be equal to` "Failed to connect user. Please check you haven't connected a user already."
        }

    @Test
    fun `Connect a guest user when no previous connection was performed should return a success`() = runCancellableTest {
        val connectionId = randomString()
        val createdAt = Date()
        val rawCreatedAt = streamDateFormatter.format(createdAt)
        val event = ConnectedEvent(EventType.HEALTH_CHECK, createdAt, rawCreatedAt, user, connectionId)

        whenever(chatApi.getGuestUser(user.id, user.name)) doReturn GuestUser(user, jwt).asCall()
        val deferred = testCoroutines.scope.async { client.connectGuestUser(user.id, user.name).await() }
        fakeChatSocket.mockEventReceived(event)
        val result = deferred.await()

        result.shouldBeInstanceOf(Result.Success::class)
        (result as Result.Success).value `should be equal to` ConnectionData(user, connectionId)
    }

    @Test
    fun `Where there is a connection error connecting a guest user, it should be propagated`() = runTest {
        val messageError = randomString()
        val createdAt = Date()
        val rawCreatedAt = streamDateFormatter.format(createdAt)
        val event = ErrorEvent(EventType.HEALTH_CHECK, createdAt, rawCreatedAt, Error.GenericError(message = messageError))

        whenever(chatApi.getGuestUser(user.id, user.name)) doReturn GuestUser(user, jwt).asCall()
        val localScope = testCoroutines.scope + Job()
        val deferred = localScope.async { client.connectGuestUser(user.id, user.name).await() }
        fakeChatSocket.mockEventReceived(event)
        val result = deferred.await()

        result.shouldBeInstanceOf(Result.Failure::class)
        (result as Result.Failure).value.message `should be equal to` messageError
    }

    @Test
    fun `Connect an anonymous user when no previous connection was performed should return a success`() = runCancellableTest {
        val connectionId = randomString()
        val createdAt = Date()
        val rawCreatedAt = streamDateFormatter.format(createdAt)
        val event = ConnectedEvent(EventType.HEALTH_CHECK, createdAt, rawCreatedAt, anonUser, connectionId)

        val localScope = testCoroutines.scope + Job()
        val deferred = localScope.async { client.connectAnonymousUser().await() }
        fakeChatSocket.mockEventReceived(event)

        val result = deferred.await()

        result.shouldBeInstanceOf(Result.Success::class)
        (result as Result.Success).value `should be equal to` ConnectionData(anonUser, connectionId)
        userStateService.state.userOrError() `should be equal to` anonUser
    }

    @Test
    fun `Where there is a connection error connecting an anonymous user, it should be propagated`() = runCancellableTest {
        val messageError = randomString()
        val createdAt = Date()
        val rawCreatedAt = streamDateFormatter.format(createdAt)
        val event = ErrorEvent(EventType.HEALTH_CHECK, createdAt, rawCreatedAt, Error.GenericError(message = messageError))

        val localScope = testCoroutines.scope + Job()
        val deferred = localScope.async { client.connectAnonymousUser().await() }
        fakeChatSocket.mockEventReceived(event)
        val result = deferred.await()

        result.shouldBeInstanceOf(Result.Failure::class)
        (result as Result.Failure).value.message `should be equal to` messageError
    }

    @Test
    fun `Given no connected user, calling disconnect should return error`() = runCancellableTest {
        val result = client.disconnect(flushPersistence = false).await()

        result.shouldBeInstanceOf(Result.Failure::class)
        (result as Result.Failure).value.message `should be equal to`
            "ChatClient can't be disconnected because user wasn't connected previously"
    }

    @Test
    fun `Given no connected user, calling disconnect with flushPersistence should clear local data`() = runCancellableTest {
        val result = client.disconnect(flushPersistence = true).await()

        result.shouldBeInstanceOf(Result.Success::class)
        verify(userCredentialStorage).clear()
    }

    @Test
    fun `Given a failed connection, calling disconnect with flushPersistence should clear local data`() = runCancellableTest {
        client.connectUser(user, jwt, 1).await()
        verify(userCredentialStorage, never()).clear()

        val result = client.disconnect(flushPersistence = true).await()

        result.shouldBeInstanceOf(Result.Success::class)
        verify(userCredentialStorage).clear()
    }

    @Test
    fun `Given connected user, calling disconnect with flushPersistence should return success and clear local data`() = runCancellableTest {
        val connectionId = randomString()
        prepareAliveConnection(user, connectionId)

        val flushPersistence = true
        val result = client.disconnect(flushPersistence).await()

        result.shouldBeInstanceOf(Result.Success::class)
        verify(userCredentialStorage).clear()
    }

    @Test
    fun `Given connected user, calling disconnect without flushPersistence should return success and not clear local data`() = runCancellableTest {
        val connectionId = randomString()
        prepareAliveConnection(user, connectionId)

        val flushPersistence = false
        val result = client.disconnect(flushPersistence).await()

        result.shouldBeInstanceOf(Result.Success::class)
        verify(userCredentialStorage, never()).clear()
    }

    @Test
    fun `Given stored credentials, containsStoredCredentials returns true`() {
        val credentials = CredentialConfig(
            userId = randomString(),
            userToken = randomString(),
            userName = randomString(),
            isAnonymous = randomBoolean(),
        )
        whenever(userCredentialStorage.get()).doReturn(credentials)
        client.containsStoredCredentials() `should be equal to` true
    }

    @Test
    fun `Given no stored credentials, containsStoredCredentials returns false`() {
        whenever(userCredentialStorage.get()).doReturn(null)
        client.containsStoredCredentials() `should be equal to` false
    }

    @Test
    fun `Given stored credentials, getStoredUser returns the user`() {
        val credentials = CredentialConfig(
            userId = randomString(),
            userToken = randomString(),
            userName = randomString(),
            isAnonymous = randomBoolean(),
        )
        whenever(userCredentialStorage.get()).doReturn(credentials)

        val expectedUser = User(id = credentials.userId, name = credentials.userName)
        client.getStoredUser() `should be equal to` expectedUser
    }

    @Test
    fun `Given no stored credentials, getStoredUser returns null`() {
        whenever(userCredentialStorage.get()).doReturn(null)
        client.getStoredUser() `should be equal to` null
    }

    @Test
    fun `Given connected user, getConnectionId returns the active connectionId`() = runCancellableTest {
        val connectionId = randomString()
        prepareAliveConnection(user, connectionId)

        client.getConnectionId() `should be equal to` connectionId
    }

    @Test
    fun `Given no connected user, getConnectionId returns null`() {
        client.getConnectionId() `should be equal to` null
    }

    @Test
    fun `Given no connected user and no stored user, getCurrentOrStoredUserId returns null`() = runCancellableTest {
        userStateService.onLogout() // disconnect any user
        whenever(userCredentialStorage.get()).thenReturn(null)

        client.getCurrentOrStoredUserId() `should be equal to` null
    }

    @Test
    fun `Given connected user and no stored user, getCurrentOrStoredUserId returns the connected user id`() = runCancellableTest {
        val connectionId = randomString()
        prepareAliveConnection(user, connectionId)
        whenever(userCredentialStorage.get()).thenReturn(null)

        client.getCurrentOrStoredUserId() `should be equal to` user.id
    }

    @Test
    fun `Given no connected user and stored user, getCurrentOrStoredUserId returns the stored user id`() = runCancellableTest {
        userStateService.onLogout() // disconnect any user
        val storedUserId = randomString()
        whenever(userCredentialStorage.get())
            .thenReturn(CredentialConfig(storedUserId, randomString(), randomString(), randomBoolean()))

        client.getCurrentOrStoredUserId() `should be equal to` storedUserId
    }

    @Test
    fun `Given connected user and stored user, getCurrentOrStoredUserId returns the connected user id`() = runCancellableTest {
        val connectionId = randomString()
        prepareAliveConnection(user, connectionId)
        val storedUserId = randomString()
        whenever(userCredentialStorage.get())
            .thenReturn(CredentialConfig(storedUserId, randomString(), randomString(), randomBoolean()))

        client.getCurrentOrStoredUserId() `should be equal to` user.id
    }

    @Test
    fun `Given a user set without connecting, connecting another user stops the plugins of the first`() =
        runCancellableTest {
            val storedUserPlugin = mock<Plugin>()
            val connectedUserPlugin = mock<Plugin>()
            val storedUserId = randomString()
            pluginFactories += mock<PluginFactory> {
                on { get(any()) } doAnswer { invocation ->
                    if (invocation.getArgument<User>(0).id == storedUserId) storedUserPlugin else connectedUserPlugin
                }
            }
            whenever(config.notificationConfig) doReturn NotificationConfig()
            whenever(userCredentialStorage.get())
                .thenReturn(CredentialConfig(storedUserId, randomString(), randomString(), isAnonymous = false))
            client.setUserWithoutConnectingIfNeeded()
            verify(storedUserPlugin).onUserSet(any())

            val createdAt = Date()
            val rawCreatedAt = streamDateFormatter.format(createdAt)
            val event = ConnectedEvent(EventType.HEALTH_CHECK, createdAt, rawCreatedAt, user, randomString())
            val deferred = testCoroutines.scope.async { client.connectUser(user, jwt).await() }
            fakeChatSocket.mockEventReceived(event)
            deferred.await()

            verify(storedUserPlugin).onUserDisconnected()
            verify(connectedUserPlugin).onUserSet(any())
            verify(connectedUserPlugin, never()).onUserDisconnected()
        }

    @Test
    fun `Given a user set without connecting, connecting another user clears the queued receipts`() =
        runCancellableTest {
            connectAfterStoredUserIsSet(storedUserId = randomString())

            verify(repository).clear()
        }

    @Test
    fun `Given a user set without connecting, connecting the same user keeps the queued receipts`() =
        runCancellableTest {
            connectAfterStoredUserIsSet(storedUserId = user.id)

            verify(repository, never()).clear()
        }

    @Test
    fun `Given no user set, connecting a user keeps the queued receipts`() =
        runCancellableTest {
            connectAfterStoredUserIsSet(storedUserId = null)

            verify(repository, never()).clear()
        }

    private suspend fun connectAfterStoredUserIsSet(storedUserId: String?) {
        if (storedUserId != null) {
            whenever(config.notificationConfig) doReturn NotificationConfig()
            whenever(userCredentialStorage.get())
                .thenReturn(CredentialConfig(storedUserId, randomString(), randomString(), isAnonymous = false))
            client.setUserWithoutConnectingIfNeeded()
        }

        val createdAt = Date()
        val rawCreatedAt = streamDateFormatter.format(createdAt)
        val event = ConnectedEvent(EventType.HEALTH_CHECK, createdAt, rawCreatedAt, user, randomString())
        val deferred = testCoroutines.scope.async { client.connectUser(user, jwt).await() }
        fakeChatSocket.mockEventReceived(event)
        deferred.await()
    }

    @Test
    fun `Given a user set without connecting, connecting another user replaces the scheduled messages of the first`() =
        runCancellableTest { connectAnotherUserThanTheStoredOne(isStoredUserLoadedFirst = true) }

    @Test
    fun `Given the scheduled messages of a stored user still loading, another user connecting gets only its own`() =
        runCancellableTest { connectAnotherUserThanTheStoredOne(isStoredUserLoadedFirst = false) }

    private suspend fun TestScope.connectAnotherUserThanTheStoredOne(isStoredUserLoadedFirst: Boolean) {
        val storedUserId = randomString()
        val storedUserMessage = randomLocalScheduledMessage()
        val connectedUserMessage = randomLocalScheduledMessage()
        val storedUserLoad = CompletableDeferred<Unit>()
        val storedUserRepository = HeldScheduledMessageRepository(storedUserLoad, storedUserMessage)
        val connectedUserRepository = InMemoryScheduledMessageRepository(connectedUserMessage)
        repositoryFactories[storedUserId] = repositoryFactoryWith(storedUserRepository)
        repositoryFactories[user.id] = repositoryFactoryWith(connectedUserRepository)
        // What a scheduled message change made as soon as the client is ready would find.
        var scheduledMessagesWhenReady: List<ScheduledMessage>? = null
        client.clientState.initializationState
            .filter { it == InitializationState.COMPLETE }
            .onEach { scheduledMessagesWhenReady = client.scheduledMessages.value }
            .launchIn(backgroundScope + UnconfinedTestDispatcher(testScheduler))
        whenever(config.notificationConfig) doReturn NotificationConfig()
        whenever(userCredentialStorage.get())
            .thenReturn(CredentialConfig(storedUserId, randomString(), randomString(), isAnonymous = false))
        client.setUserWithoutConnectingIfNeeded()
        if (isStoredUserLoadedFirst) {
            storedUserLoad.complete(Unit)
            client.scheduledMessages.value.map { it.id } `should be equal to` listOf(storedUserMessage.id)
        }

        val createdAt = Date()
        val rawCreatedAt = streamDateFormatter.format(createdAt)
        val event = ConnectedEvent(EventType.HEALTH_CHECK, createdAt, rawCreatedAt, user, randomString())
        val deferred = testCoroutines.scope.async { client.connectUser(user, jwt).await() }
        // The start of the stored user ends only now, while the other user is connecting.
        if (!isStoredUserLoadedFirst) storedUserLoad.complete(Unit)
        fakeChatSocket.mockEventReceived(event)
        deferred.await()

        scheduledMessagesWhenReady.shouldNotBeNull().map { it.id } `should not contain` storedUserMessage.id
        client.scheduledMessages.value.map { it.id } `should be equal to` listOf(connectedUserMessage.id)

        val scheduled = Message(id = randomString(), text = randomString())
        client.scheduleMessage("messaging", randomString(), scheduled, scheduledAt = Date()).await()
            .shouldBeInstanceOf<Result.Success<ScheduledMessage>>()
        storedUserRepository.messages.keys `should be equal to` setOf(storedUserMessage.id)
        connectedUserRepository.messages.keys `should be equal to` setOf(connectedUserMessage.id, scheduled.id)
    }

    private fun repositoryFactoryWith(repository: ScheduledMessageRepository): RepositoryFactory =
        mock<DatabaseRepositoryFactory>(defaultAnswer = RETURNS_MOCKS) {
            on { createScheduledMessageRepository(any(), any()) } doReturn repository
            // No channel config, so none of its limits applies to scheduling.
            on { createChannelConfigRepository() } doReturn mock()
            // No stored channel, so the channel summary of a scheduled message comes from the server.
            on { createChannelRepository(any(), any()) } doReturn mock()
        }

    /** An [InMemoryScheduledMessageRepository] that returns its scheduled messages once [load] completes. */
    private class HeldScheduledMessageRepository(
        private val load: CompletableDeferred<Unit>,
        message: LocalScheduledMessage,
        private val repository: InMemoryScheduledMessageRepository = InMemoryScheduledMessageRepository(message),
    ) : ScheduledMessageRepository by repository {

        val messages get() = repository.messages

        override suspend fun selectScheduledMessages(): List<LocalScheduledMessage> {
            load.await()
            return repository.selectScheduledMessages()
        }
    }

    private suspend fun prepareAliveConnection(user: User, connectionId: String) {
        userStateService.onSetUser(user, false)
        fakeChatSocket.prepareAliveConnection(user, connectionId)
    }

    private fun runCancellableTest(testBody: suspend TestScope.() -> Unit) {
        runTest {
            try {
                testBody()
            } finally {
                // Also after a failure: a job left running would keep the test from ending.
                clientScope.cancel()
            }
        }
    }
}
