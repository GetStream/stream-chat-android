/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.clientstate

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

internal class SocketStateTest {

    @Test
    fun `connectionIdOrError throws error for idle`() {
        val socketState = SocketState.Idle
        assertThrows(IllegalStateException::class.java) {
            socketState.connectionIdOrError()
        }
    }

    @Test
    fun `connectionIdOrError throws error for pending`() {
        val socketState = SocketState.Pending
        assertThrows(IllegalStateException::class.java) {
            socketState.connectionIdOrError()
        }
    }

    @Test
    fun `connectionIdOrError returns connection id for connected`() {
        val connectionId = "connectionId"
        val socketState = SocketState.Connected(connectionId)
        assertEquals(connectionId, socketState.connectionIdOrError())
    }

    @Test
    fun `connectionIdOrError throws error for disconnected`() {
        val socketState = SocketState.Disconnected
        assertThrows(IllegalStateException::class.java) {
            socketState.connectionIdOrError()
        }
    }
}
