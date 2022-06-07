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

package io.getstream.chat.android.client.utils.observable

import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.UnknownEvent
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.test.TestCoroutineRule
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import java.util.Date

internal class ChatEventsObservableTest {

    @get:Rule
    val testCoroutines: TestCoroutineRule = TestCoroutineRule()

    private lateinit var socket: FakeSocket
    private lateinit var observable: ChatEventsObservable
    private lateinit var result: MutableList<ChatEvent>

    @Before
    fun before() {
        socket = FakeSocket()
        observable = ChatEventsObservable(socket, mock(), testCoroutines.scope)
        result = mutableListOf()
    }

    @Test
    fun oneEventDelivery() {
        val event = ConnectedEvent(EventType.HEALTH_CHECK, Date(), User(), "")
        observable.subscribe { result.add(it) }

        socket.sendEvent(event)

        result shouldBeEqualTo listOf(event)
    }

    @Test
    fun multipleEventsDelivery() {
        val eventA = ConnectedEvent(EventType.HEALTH_CHECK, Date(), User(), "")
        val eventB = NewMessageEvent(
            EventType.MESSAGE_NEW,
            Date(),
            User(),
            "type:id",
            "type",
            "id",
            Message(),
            0,
            0,
            0
        )
        val eventC = DisconnectedEvent(EventType.CONNECTION_DISCONNECTED, Date())
        observable.subscribe { result.add(it) }

        socket.sendEvent(eventA)
        socket.sendEvent(eventB)
        socket.sendEvent(eventC)

        result shouldBeEqualTo listOf(eventA, eventB, eventC)
    }

    @Test
    fun filtering() {
        val eventA = UnknownEvent("a", Date(), null, emptyMap<Any, Any>())
        val eventB = UnknownEvent("b", Date(), null, mapOf<Any, Any>("cid" to "myCid"))
        val eventC = UnknownEvent("c", Date(), null, emptyMap<Any, Any>())

        val filter: (ChatEvent) -> Boolean = {
            it.type == "b" && (it as? UnknownEvent)?.rawData?.get("cid") == "myCid"
        }
        observable.subscribe(filter) {
            result.add(it)
        }

        socket.sendEvent(eventA)
        socket.sendEvent(eventB)
        socket.sendEvent(eventC)

        result shouldBeEqualTo listOf(eventB)
    }

    @Test
    fun unsubscription() {
        val eventA = UnknownEvent("a", Date(), null, emptyMap<Any, Any>())
        val eventB = UnknownEvent("b", Date(), null, emptyMap<Any, Any>())
        val eventC = UnknownEvent("c", Date(), null, emptyMap<Any, Any>())

        val subscription = observable.subscribe { result.add(it) }

        socket.sendEvent(eventA)
        socket.sendEvent(eventB)

        subscription.dispose()

        socket.sendEvent(eventC)

        result shouldBeEqualTo listOf(eventA, eventB)
    }
}
