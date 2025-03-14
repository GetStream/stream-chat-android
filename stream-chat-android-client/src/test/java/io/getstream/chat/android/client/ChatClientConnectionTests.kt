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

import androidx.lifecycle.testing.TestLifecycleOwner
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.api2.mapping.DtoMapping
import io.getstream.chat.android.client.clientstate.UserStateService
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.ErrorEvent
import io.getstream.chat.android.client.network.NetworkStateProvider
import io.getstream.chat.android.client.parser2.adapters.internal.StreamDateFormatter
import io.getstream.chat.android.client.persistance.repository.noop.NoOpRepositoryFactory
import io.getstream.chat.android.client.scope.ClientTestScope
import io.getstream.chat.android.client.scope.UserTestScope
import io.getstream.chat.android.client.setup.state.internal.MutableClientState
import io.getstream.chat.android.client.socket.FakeChatSocket
import io.getstream.chat.android.client.token.FakeTokenManager
import io.getstream.chat.android.client.token.TokenManager
import io.getstream.chat.android.client.user.CredentialConfig
import io.getstream.chat.android.client.user.storage.UserCredentialStorage
import io.getstream.chat.android.client.utils.TokenUtils
import io.getstream.chat.android.models.ConnectionData
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.models.GuestUser
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
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
    private val config = mock<ChatClientConfig>()
    private val tokenManager = mock<TokenManager>()
    private val userCredentialStorage = mock<UserCredentialStorage>()

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
        val networkStateProvider: NetworkStateProvider = mock()
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
            pluginFactories = emptyList(),
            repositoryFactoryProvider = NoOpRepositoryFactory.Provider,
            mutableClientState = mutableClientState,
            currentUserFetcher = mock(),
            audioPlayer = mock(),
        ).apply {
            attachmentsSender = mock()
        }
    }

    @Test
    fun `Connect an user with a different userId than the one into the JWT should return an error`() = runTest {
        whenever(tokenUtils.getUserId(eq(jwt))) doReturn randomString()

        val result = client.connectUser(user, jwt).await()

        result.shouldBeInstanceOf(Result.Failure::class)
        (result as Result.Failure).value.message `should be equal to`
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
        val flushPersistence = randomBoolean()
        val result = client.disconnect(flushPersistence).await()

        result.shouldBeInstanceOf(Result.Failure::class)
        (result as Result.Failure).value.message `should be equal to`
            "ChatClient can't be disconnected because user wasn't connected previously"
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

    private suspend fun prepareAliveConnection(user: User, connectionId: String) {
        userStateService.onSetUser(user, false)
        fakeChatSocket.prepareAliveConnection(user, connectionId)
    }

    private fun runCancellableTest(testBody: suspend TestScope.() -> Unit) {
        runTest {
            testBody()
            clientScope.cancel()
        }
    }
}
