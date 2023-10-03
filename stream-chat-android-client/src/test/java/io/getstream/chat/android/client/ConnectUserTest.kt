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
import io.getstream.chat.android.client.clientstate.SocketStateService
import io.getstream.chat.android.client.clientstate.UserStateService
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.ErrorEvent
import io.getstream.chat.android.client.models.ConnectionData
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.models.GuestUser
import io.getstream.chat.android.client.models.InitializationState
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.parser2.adapters.internal.StreamDateFormatter
import io.getstream.chat.android.client.persistance.repository.noop.NoOpRepositoryFactory
import io.getstream.chat.android.client.scope.ClientTestScope
import io.getstream.chat.android.client.scope.UserTestScope
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.setup.state.internal.ClientStateImpl
import io.getstream.chat.android.client.utils.TokenUtils
import io.getstream.chat.android.client.utils.observable.FakeSocket
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
internal class ConnectUserTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private lateinit var socket: FakeSocket
    private lateinit var chatApi: ChatApi
    private lateinit var userStateService: UserStateService
    private lateinit var socketStateService: SocketStateService
    private lateinit var client: ChatClient
    private val tokenUtils: TokenUtils = mock()
    private val userId = randomString()
    private val jwt = randomString()
    private val anonjwt = randomString()
    private val user = User(id = userId)
    private val anonId = "!anon"
    private val anonUser = User(id = anonId)
    private val clientState: ClientState = ClientStateImpl(mock())
    private val streamDateFormatter = StreamDateFormatter()

    @BeforeEach
    fun setup() {
        whenever(tokenUtils.devToken(eq(anonId))) doReturn anonjwt
        whenever(tokenUtils.getUserId(jwt)) doReturn userId
        whenever(tokenUtils.getUserId(anonjwt)) doReturn anonId
        val lifecycleOwner = TestLifecycleOwner(coroutineDispatcher = testCoroutines.dispatcher)
        socket = FakeSocket()
        chatApi = mock()
        userStateService = UserStateService()
        socketStateService = SocketStateService()
        val clientScope = ClientTestScope(testCoroutines.scope)
        val userScope = UserTestScope(clientScope)
        client = ChatClient(
            config = mock(),
            api = chatApi,
            socket = socket,
            notifications = mock(),
            tokenManager = mock(),
            socketStateService = socketStateService,
            userCredentialStorage = mock(),
            userStateService = userStateService,
            tokenUtils = tokenUtils,
            clientScope = clientScope,
            userScope = userScope,
            retryPolicy = mock(),
            appSettingsManager = mock(),
            chatSocketExperimental = mock(),
            lifecycleObserver = StreamLifecycleObserver(userScope, lifecycleOwner.lifecycle),
            pluginFactories = emptyList(),
            repositoryFactoryProvider = NoOpRepositoryFactory.Provider,
            clientState = clientState,
            currentUserFetcher = mock(),
        )
    }

    @Test
    fun `Connect an user with a different userId than the one into the JWT should return an error`() = runTest {
        whenever(tokenUtils.getUserId(eq(jwt))) doReturn randomString()

        val result = client.connectUser(user, jwt).await()

        result.isError `should be equal to` true
        result.error().message `should be equal to`
            "The user_id provided on the JWT token doesn't match with the current user you try to connect"
    }

    @Test
    fun `When connection is successful, initialisation state should be updated`() = runTest {
        val connectionId = randomString()
        val createdAt = Date()
        val rawCreatedAt = streamDateFormatter.format(createdAt)

        val event = ConnectedEvent(EventType.HEALTH_CHECK, createdAt, rawCreatedAt, user, connectionId)

        val deferred = testCoroutines.scope.async { client.connectUser(user, jwt).await() }
        socket.sendEvent(event)
        val result = deferred.await()

        result.isSuccess `should be equal to` true
        clientState.initializationState.value `should be equal to` InitializationState.COMPLETE
    }

    @Test
    fun `When connection is running, initialisation state should be updated`() = runTest {
        client.connectUser(user, jwt).enqueue()

        clientState.initializationState.value `should be equal to` InitializationState.RUNNING
    }

    @Test
    fun `Connect an user when alive connection exists with the same user should return a success`() = runTest {
        val connectionId = randomString()
        prepareAliveConnection(user, connectionId)

        val result = client.connectUser(user, jwt).await()

        result.isSuccess `should be equal to` true
        result.data() `should be equal to` ConnectionData(user, connectionId)
    }

    @Test
    fun `Connect an user when no previous connection was performed should return a success`() = runTest {
        val connectionId = randomString()
        val createdAt = Date()
        val rawCreatedAt = streamDateFormatter.format(createdAt)
        val event = ConnectedEvent(EventType.HEALTH_CHECK, createdAt, rawCreatedAt, user, connectionId)

        val deferred = testCoroutines.scope.async { client.connectUser(user, jwt).await() }
        socket.sendEvent(event)
        val result = deferred.await()

        socket.verifyUserToConnect(user)
        result.isSuccess `should be equal to` true
        result.data() `should be equal to` ConnectionData(user, connectionId)
    }

    @Test
    fun `Where there is a connection error connecting an user, it should be propagated`() = runTest {
        val messageError = randomString()
        val createdAt = Date()
        val rawCreatedAt = streamDateFormatter.format(createdAt)
        val event = ErrorEvent(EventType.HEALTH_CHECK, createdAt, rawCreatedAt, ChatError(message = messageError))

        val localScope = testCoroutines.scope + Job()
        val deferred = localScope.async {
            client.connectUser(user, jwt).await()
        }
        socket.sendEvent(event)
        val result = deferred.await()

        result.isError `should be equal to` true
        result.error().message `should be equal to` messageError
    }

    @Test
    fun `When there is an ongoing connection with the same user, an error should be propagated`() = runTest {
        userStateService.onSetUser(user, false)

        val result = client.connectUser(user, jwt).await()

        result.isError `should be equal to` true
        result.error().message `should be equal to` "Failed to connect user. Please check you haven't connected a user already."
    }

    @Test
    fun `When connection take more time than expected an error should be propagated`() = runTest {
        val result = client.connectUser(user, jwt, 1).await()

        result.isError `should be equal to` true
        result.error().message `should be equal to` "Connection wasn't established in 1ms"
    }

    @Test
    fun `When there is an user connected and try to connect a different user, an error should be propagated`() =
        runTest {
            userStateService.onSetUser(user, false)

            val result = client.connectUser(user, jwt).await()

            result.isError `should be equal to` true
            result.error().message `should be equal to` "Failed to connect user. Please check you haven't connected a user already."
        }

    @Test
    fun `Connect a guest user when no previous connection was performed should return a success`() = runTest {
        val connectionId = randomString()
        val createdAt = Date()
        val rawCreatedAt = streamDateFormatter.format(createdAt)
        val event = ConnectedEvent(EventType.HEALTH_CHECK, createdAt, rawCreatedAt, user, connectionId)

        whenever(chatApi.getGuestUser(user.id, user.name)) doReturn GuestUser(user, jwt).asCall()
        val deferred = testCoroutines.scope.async { client.connectGuestUser(user.id, user.name).await() }
        socket.sendEvent(event)
        val result = deferred.await()

        socket.verifyUserToConnect(user)
        result.isSuccess `should be equal to` true
        result.data() `should be equal to` ConnectionData(user, connectionId)
    }

    @Test
    fun `Where there is a connection error connecting a guest user, it should be propagated`() = runTest {
        val messageError = randomString()
        val createdAt = Date()
        val rawCreatedAt = streamDateFormatter.format(createdAt)
        val event = ErrorEvent(EventType.HEALTH_CHECK, createdAt, rawCreatedAt, ChatError(message = messageError))

        whenever(chatApi.getGuestUser(user.id, user.name)) doReturn GuestUser(user, jwt).asCall()
        val localScope = testCoroutines.scope + Job()
        val deferred = localScope.async { client.connectGuestUser(user.id, user.name).await() }
        socket.sendEvent(event)
        val result = deferred.await()

        result.isError `should be equal to` true
        result.error().message `should be equal to` messageError
    }

    @Test
    fun `Connect an anonymous user when no previous connection was performed should return a success`() = runTest {
        val connectionId = randomString()
        val createdAt = Date()
        val rawCreatedAt = streamDateFormatter.format(createdAt)
        val event = ConnectedEvent(EventType.HEALTH_CHECK, createdAt, rawCreatedAt, anonUser, connectionId)

        val deferred = testCoroutines.scope.async { client.connectAnonymousUser().await() }
        socket.sendEvent(event)
        val result = deferred.await()

        socket.verifyUserToConnect(anonUser)
        result.isSuccess `should be equal to` true
        result.data() `should be equal to` ConnectionData(anonUser, connectionId)
        userStateService.state.userOrError() `should be equal to` anonUser
    }

    @Test
    fun `Where there is a connection error connecting an anonymous user, it should be propagated`() = runTest {
        val messageError = randomString()
        val createdAt = Date()
        val rawCreatedAt = streamDateFormatter.format(createdAt)
        val event = ErrorEvent(EventType.HEALTH_CHECK, createdAt, rawCreatedAt, ChatError(message = messageError))

        val localScope = testCoroutines.scope + Job()
        val deferred = localScope.async { client.connectAnonymousUser().await() }
        socket.sendEvent(event)
        val result = deferred.await()

        result.isError `should be equal to` true
        result.error().message `should be equal to` messageError
    }

    private suspend fun prepareAliveConnection(user: User, connectionId: String) {
        userStateService.onSetUser(user, false)
        socketStateService.onConnectionRequested()
        socketStateService.onConnected(connectionId)
    }
}
