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

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.clientstate.SocketStateService
import io.getstream.chat.android.client.clientstate.UserStateService
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.ErrorEvent
import io.getstream.chat.android.client.models.ConnectionData
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.models.GuestUser
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.observable.FakeSocket
import io.getstream.chat.android.test.TestCoroutineRule
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.test.randomString
import io.getstream.chat.android.test.runTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import org.amshove.kluent.`should be equal to`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
internal class ConnectUserTest {

    @get:Rule
    val testCoroutines: TestCoroutineRule = TestCoroutineRule()

    private lateinit var socket: FakeSocket
    private lateinit var chatApi: ChatApi
    private lateinit var userStateService: UserStateService
    private lateinit var socketStateService: SocketStateService
    private lateinit var client: ChatClient

    @Before
    fun setup() {
        socket = FakeSocket()
        chatApi = mock()
        userStateService = UserStateService()
        socketStateService = SocketStateService()
        client = ChatClient(
            config = mock(),
            api = chatApi,
            socket = socket,
            notifications = mock(),
            tokenManager = mock(),
            socketStateService = socketStateService,
            queryChannelsPostponeHelper = mock(),
            userCredentialStorage = mock(),
            userStateService = userStateService,
            scope = testCoroutines.scope,
            retryPolicy = mock(),
            appSettingsManager = mock(),
        )
    }

    @Test
    fun `Connect an user with a different userId than the one into the JWT should return an error`() {
        val user = User(id = "asdf")
        val jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiamMifQ==.devtoken"

        val result = client.connectUser(user, jwt).execute()

        result.isError `should be equal to` true
        result.error().message `should be equal to`
            "The user_id provided on the JWT token doesn't match with the current user you try to connect"
    }

    @Test
    fun `Connect an user when alive connection exists with the same user should return a success`() = testCoroutines.runTest {
        /* Given */
        val user = User(id = "jc")
        val jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiamMifQ==.devtoken"
        val connectionId = randomString()
        prepareAliveConnection(user, connectionId)

        /* When */
        val result = client.connectUser(user, jwt).await()

        /* Then */
        result.isSuccess `should be equal to` true
        result.data() `should be equal to` ConnectionData(user, connectionId)
    }

    @Test
    fun `Connect an user when no previous connection was performed should return a success`() = testCoroutines.runTest {
        /* Given */
        val user = User(id = "jc")
        val jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiamMifQ==.devtoken"
        val connectionId = randomString()
        val event = ConnectedEvent(EventType.HEALTH_CHECK, Date(), user, connectionId)

        /* When */
        val deferred = async { client.connectUser(user, jwt).await() }
        socket.sendEvent(event)
        val result = deferred.await()

        /* Then */
        socket.verifyUserToConnect(user)
        result.isSuccess `should be equal to` true
        result.data() `should be equal to` ConnectionData(user, connectionId)
    }

    @Test
    fun `Where there is a connection error connecting an user, it should be propagated`() = testCoroutines.runTest {
        /* Given */
        val user = User(id = "jc")
        val jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiamMifQ==.devtoken"
        val messageError = randomString()
        val event = ErrorEvent(EventType.HEALTH_CHECK, Date(), ChatError(message = messageError))

        /* When */
        val deferred = async { client.connectUser(user, jwt).await() }
        socket.sendEvent(event)
        val result = deferred.await()

        /* Then */
        result.isError `should be equal to` true
        result.error().message `should be equal to` messageError
    }

    @Test
    fun `Where there is an ongoing connection with the same user, an error should be propagated`() = testCoroutines.runTest {
        /* Given */
        val user = User(id = "jc")
        userStateService.onSetUser(user)
        val jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiamMifQ==.devtoken"

        /* When */
        val result = client.connectUser(user, jwt).await()

        /* Then */
        result.isError `should be equal to` true
        result.error().message `should be equal to` "Failed to connect user. Please check you don't have connected user already."
    }

    @Test
    fun `When connection take more time than expected an error should be propagated`() = testCoroutines.runTest {
        /* Given */
        val user = User(id = "jc")
        val jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiamMifQ==.devtoken"

        /* When */
        val result = client.connectUser(user, jwt, 1).await()

        /* Then */
        result.isError `should be equal to` true
        result.error().message `should be equal to` "Connection wasn't established in 1ms"
    }

    @Test
    fun `When there is an user connected and try to connect a different user, an error should be propagated`() = testCoroutines.runTest {
        /* Given */
        val user = User(id = "jc")
        userStateService.onSetUser(user)
        val jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiamMifQ==.devtoken"

        /* When */
        val result = client.connectUser(user, jwt).await()

        /* Then */
        result.isError `should be equal to` true
        result.error().message `should be equal to` "Failed to connect user. Please check you don't have connected user already."
    }

    @Test
    fun `Connect a guest user when no previous connection was performed should return a success`() = testCoroutines.runTest {
        /* Given */
        val user = User(id = "jc", name = "Jc M")
        val jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiamMifQ==.devtoken"
        val connectionId = randomString()
        val event = ConnectedEvent(EventType.HEALTH_CHECK, Date(), user, connectionId)

        /* When */
        whenever(chatApi.getGuestUser(user.id, user.name)) doReturn GuestUser(user, jwt).asCall()
        val deferred = async { client.connectGuestUser(user.id, user.name).await() }
        socket.sendEvent(event)
        val result = deferred.await()

        /* Then */
        socket.verifyUserToConnect(user)
        result.isSuccess `should be equal to` true
        result.data() `should be equal to` ConnectionData(user, connectionId)
    }

    @Test
    fun `Where there is a connection error connecting a guest user, it should be propagated`() = testCoroutines.runTest {
        /* Given */
        val user = User(id = "jc")
        val jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiamMifQ==.devtoken"
        val messageError = randomString()
        val event = ErrorEvent(EventType.HEALTH_CHECK, Date(), ChatError(message = messageError))

        /* When */
        whenever(chatApi.getGuestUser(user.id, user.name)) doReturn GuestUser(user, jwt).asCall()
        val deferred = async { client.connectGuestUser(user.id, user.name).await() }
        socket.sendEvent(event)
        val result = deferred.await()

        /* Then */
        result.isError `should be equal to` true
        result.error().message `should be equal to` messageError
    }

    @Test
    fun `Connect an anonymous user when no previous connection was performed should return a success`() = testCoroutines.runTest {
        /* Given */
        val user = User(id = "!anon")
        val connectionId = randomString()
        val event = ConnectedEvent(EventType.HEALTH_CHECK, Date(), user, connectionId)

        /* When */
        val deferred = async { client.connectAnonymousUser().await() }
        socket.sendEvent(event)
        val result = deferred.await()

        /* Then */
        socket.verifyUserToConnect(user)
        result.isSuccess `should be equal to` true
        result.data() `should be equal to` ConnectionData(user, connectionId)
        userStateService.state.userOrError() `should be equal to` user
    }

    @Test
    fun `Where there is a connection error connecting an anonymous user, it should be propagated`() = testCoroutines.runTest {
        /* Given */
        val messageError = randomString()
        val event = ErrorEvent(EventType.HEALTH_CHECK, Date(), ChatError(message = messageError))

        /* When */
        val deferred = async { client.connectAnonymousUser().await() }
        socket.sendEvent(event)
        val result = deferred.await()

        /* Then */
        result.isError `should be equal to` true
        result.error().message `should be equal to` messageError
    }

    private fun prepareAliveConnection(user: User, connectionId: String) {
        userStateService.onSetUser(user)
        socketStateService.onConnectionRequested()
        socketStateService.onConnected(connectionId)
    }
}
