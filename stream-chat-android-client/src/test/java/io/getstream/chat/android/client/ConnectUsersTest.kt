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
import io.getstream.chat.android.test.runTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import org.amshove.kluent.`should be equal to`
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
internal class ConnectUsersTest {

    @get:Rule
    val testCoroutines: TestCoroutineRule = TestCoroutineRule()

    private val socket: FakeSocket = FakeSocket()
    private val chatApi: ChatApi = mock()

    val client = ChatClient(
        config = mock(),
        api = chatApi,
        socket = socket,
        notifications = mock(),
        tokenManager = mock(),
        socketStateService = mock(),
        queryChannelsPostponeHelper = mock(),
        userCredentialStorage = mock(),
        scope = testCoroutines.scope,
        retryPolicy = mock(),
        appSettingsManager = mock(),
    )

    @Test
    fun connectUserSuccess() = testCoroutines.runTest {
        /* Given */
        val user = User(id = "jc")
        val jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiamMifQ==.devtoken"
        val connectionId = "123"
        val event = ConnectedEvent(EventType.HEALTH_CHECK, Date(), user, connectionId)

        /* When */
        val deferred = async { client.connectUser(user, jwt).await() }
        socket.sendEvent(event)
        val result = deferred.await()

        /* Then */
        result.isSuccess `should be equal to` true
        result.data() `should be equal to` ConnectionData(user, connectionId)
    }

    @Test
    fun connectUserFailure() = testCoroutines.runTest {
        /* Given */
        val user = User(id = "jc")
        val jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiamMifQ==.devtoken"
        val event = ErrorEvent(EventType.HEALTH_CHECK, Date(), ChatError(message = "test error"))

        /* When */
        val deferred = async { client.connectUser(user, jwt).await() }
        socket.sendEvent(event)
        val result = deferred.await()

        /* Then */
        result.isError `should be equal to` true
        result.error().message `should be equal to` "test error"
    }

    @Test
    fun connectGuestUserSuccess() = testCoroutines.runTest {
        /* Given */
        val user = User(id = "jc", name = "Jc M")
        val jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiamMifQ==.devtoken"
        val connectionId = "123"
        val event = ConnectedEvent(EventType.HEALTH_CHECK, Date(), user, connectionId)

        /* When */
        whenever(chatApi.getGuestUser(user.id, user.name)) doReturn GuestUser(user, jwt).asCall()
        val deferred = async { client.connectGuestUser(user.id, user.name).await() }
        socket.sendEvent(event)
        val result = deferred.await()

        /* Then */
        result.isSuccess `should be equal to` true
        result.data() `should be equal to` ConnectionData(user, connectionId)
    }

    @Test
    fun connectGuestUserFailure() = testCoroutines.runTest {
        /* Given */
        val user = User(id = "jc")
        val jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiamMifQ==.devtoken"
        val event = ErrorEvent(EventType.HEALTH_CHECK, Date(), ChatError(message = "test error"))

        /* When */
        whenever(chatApi.getGuestUser(user.id, user.name)) doReturn GuestUser(user, jwt).asCall()
        val deferred = async { client.connectGuestUser(user.id, user.name).await() }
        socket.sendEvent(event)
        val result = deferred.await()

        /* Then */
        result.isError `should be equal to` true
        result.error().message `should be equal to` "test error"
    }

    @Test
    fun connectAnonymousUserSuccess() = testCoroutines.runTest {
        /* Given */
        val user = User(id = "anonymous")
        val connectionId = "123"
        val event = ConnectedEvent(EventType.HEALTH_CHECK, Date(), user, connectionId)

        /* When */
        val deferred = async { client.connectAnonymousUser().await() }
        socket.sendEvent(event)
        val result = deferred.await()

        /* Then */
        result.isSuccess `should be equal to` true
        result.data() `should be equal to` ConnectionData(user, connectionId)
    }

    @Test
    fun connectAnonymousUserFailure() = testCoroutines.runTest {
        /* Given */
        val event = ErrorEvent(EventType.HEALTH_CHECK, Date(), ChatError(message = "test error"))

        /* When */
        val deferred = async { client.connectAnonymousUser().await() }
        socket.sendEvent(event)
        val result = deferred.await()

        /* Then */
        result.isError `should be equal to` true
        result.error().message `should be equal to` "test error"
    }
}
