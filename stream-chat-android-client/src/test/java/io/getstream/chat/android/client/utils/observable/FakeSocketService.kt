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

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.socket.ChatSocketService
import io.getstream.chat.android.client.socket.SocketListener
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull

internal class FakeSocketService(
    val eventsCollector: MutableList<ChatEvent> = mutableListOf()
) : ChatSocketService {

    private var connectionUserId: String? = null

    override fun anonymousConnect(endpoint: String, apiKey: String) { }

    override fun userConnect(endpoint: String, apiKey: String, user: User) { }

    private val listeners = mutableListOf<SocketListener>()

    fun sendEvent(event: ChatEvent) {
        listeners.forEach {
            it.onEvent(event)
        }
    }

    override fun disconnect() {
    }

    override fun releaseConnection() {
    }

    override fun addListener(listener: SocketListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: SocketListener) {
        listeners.remove(listener)
    }

    override fun onSocketError(error: ChatError) {
    }

    override fun onConnectionResolved(event: ConnectedEvent) {
        connectionUserId = event.me.id
    }

    override fun onEvent(event: ChatEvent) {
        eventsCollector.add(event)
    }

    fun verifyConnectionUserId(userId: String) {
        connectionUserId shouldBeEqualTo userId
    }

    fun verifyNoConnectionUserId() {
        connectionUserId.shouldBeNull()
    }
}
