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
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.socket.SocketFactory
import io.getstream.chat.android.client.socket.SocketListener
import io.getstream.chat.android.test.randomString
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.mockito.kotlin.mock

internal class FakeSocket(
    val eventsCollector: MutableList<ChatEvent> = mutableListOf(),
) : ChatSocket(
    randomString(),
    randomString(),
    mock(),
    mock(),
    mock(),
    mock(),
    mock()
) {

    private var connectionUserId: String? = null
    private var connectionConf: SocketFactory.ConnectionConf? = null

    private val listeners = mutableSetOf<SocketListener>()

    override fun sendEvent(event: ChatEvent) {
        listeners.forEach {
            it.onEvent(event)
        }
    }

    override fun connect(connectionConf: SocketFactory.ConnectionConf) {
        super.connect(connectionConf)
        this.connectionConf = connectionConf
    }

    override fun disconnect() {
        // no-op
    }

    override fun releaseConnection(requested: Boolean) {
        // no-op
    }

    override fun addListener(listener: SocketListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: SocketListener) {
        listeners.remove(listener)
    }

    override fun onSocketError(error: ChatError) {
        // no-op
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

    fun verifyUserToConnect(connectUser: User) {
        this.connectionConf?.user shouldBeEqualTo connectUser
    }
}
