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

package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.parser2.V2EventParser
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.network.infrastructure.Serializer

/**
 * How the socket talks to the v2 endpoint: it authenticates with [authFrame] once open, parses events with
 * [eventParser], and keeps the connection alive with a minimal health check, which the server answers whatever the
 * frame carries.
 */
internal class V2SocketProtocol(
    val authFrame: String,
    val eventParser: V2EventParser,
) {
    /** The keepalive frame for the connection [connectedEvent] established. */
    fun keepAliveFrame(connectedEvent: ConnectedEvent): String = frameAdapter.toJson(
        listOf(mapOf("type" to EventType.HEALTH_CHECK, "client_id" to connectedEvent.connectionId)),
    )

    internal companion object {
        internal val frameAdapter = Serializer.moshi.adapter(Any::class.java)
    }
}
