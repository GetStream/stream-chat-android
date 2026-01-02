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

package io.getstream.chat.android.models

import androidx.compose.runtime.Immutable

/**
 * Represents possible states of the WebSocket connection.
 */
@Immutable
public sealed class ConnectionState {
    /**
     * The client is connected to the WebSocket.
     */
    @Immutable
    public data object Connected : ConnectionState() { override fun toString(): String = "Connected" }

    /**
     * The client is trying to connect to the WebSocket.
     */
    @Immutable
    public data object Connecting : ConnectionState() { override fun toString(): String = "Connecting" }

    /**
     * The client is permanently disconnected from the WebSocket.
     */
    @Immutable
    public data object Offline : ConnectionState() { override fun toString(): String = "Offline" }

    public companion object {
        /**
         * A list of all possible [ConnectionState]s.
         */
        public val values: List<ConnectionState> = listOf(Connected, Connecting, Offline)
    }
}
