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

package io.getstream.chat.android.client.utils.observable

import androidx.lifecycle.testing.TestLifecycleOwner
import io.getstream.chat.android.client.StreamLifecycleObserver
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.UnknownEvent
import io.getstream.chat.android.client.network.NetworkStateProvider
import io.getstream.chat.android.client.parser2.adapters.internal.StreamDateFormatter
import io.getstream.chat.android.client.scope.ClientTestScope
import io.getstream.chat.android.client.scope.UserTestScope
import io.getstream.chat.android.client.socket.FakeChatSocket
import io.getstream.chat.android.client.token.FakeTokenManager
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
internal class ChatEventsObservableTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private lateinit var observable: ChatEventsObservable
    private lateinit var result: MutableList<ChatEvent>
    private val streamDateFormatter = StreamDateFormatter()
    private lateinit var fakeChatSocket: FakeChatSocket

    @BeforeEach
    fun before() {
        result = mutableListOf()
        val clientScope = ClientTestScope(testCoroutines.scope)
        val userScope = UserTestScope(clientScope)
        val lifecycleOwner = TestLifecycleOwner(coroutineDispatcher = testCoroutines.dispatcher)
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
        userScope.launch { fakeChatSocket.prepareAliveConnection(randomUser(), randomString()) }
        observable = ChatEventsObservable(mock(), testCoroutines.scope, fakeChatSocket)
    }

    @Test
    fun filtering() {
        val createdAt = Date()
        val rawCreatedAt = streamDateFormatter.format(createdAt)

        val eventA = UnknownEvent("a", createdAt, rawCreatedAt, null, emptyMap<Any, Any>())
        val eventB = UnknownEvent("b", createdAt, rawCreatedAt, null, mapOf<Any, Any>("cid" to "myCid"))
        val eventC = UnknownEvent("c", createdAt, rawCreatedAt, null, emptyMap<Any, Any>())

        val filter: (ChatEvent) -> Boolean = {
            it.type == "b" && (it as? UnknownEvent)?.rawData?.get("cid") == "myCid"
        }
        observable.subscribe(filter) {
            result.add(it)
        }

        fakeChatSocket.mockEventReceived(eventA)
        fakeChatSocket.mockEventReceived(eventB)
        fakeChatSocket.mockEventReceived(eventC)

        result shouldBeEqualTo listOf(eventB)
    }

    @Test
    fun unsubscription() {
        val createdAt = Date()
        val rawCreatedAt = streamDateFormatter.format(createdAt)

        val eventA = UnknownEvent("a", createdAt, rawCreatedAt, null, emptyMap<Any, Any>())
        val eventB = UnknownEvent("b", createdAt, rawCreatedAt, null, emptyMap<Any, Any>())
        val eventC = UnknownEvent("c", createdAt, rawCreatedAt, null, emptyMap<Any, Any>())

        val subscription = observable.subscribe { result.add(it) }

        fakeChatSocket.mockEventReceived(eventA)
        fakeChatSocket.mockEventReceived(eventB)

        subscription.dispose()

        fakeChatSocket.mockEventReceived(eventC)

        result shouldBeEqualTo listOf(eventA, eventB)
    }
}
