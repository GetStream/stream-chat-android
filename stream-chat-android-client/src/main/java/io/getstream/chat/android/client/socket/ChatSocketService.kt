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

package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.models.User

internal interface ChatSocketService {
    fun anonymousConnect(endpoint: String, apiKey: String)
    fun userConnect(endpoint: String, apiKey: String, user: User)
    fun disconnect()
    fun releaseConnection()
    fun addListener(listener: SocketListener)
    fun removeListener(listener: SocketListener)
    fun onSocketError(error: ChatError)
    fun onConnectionResolved(event: ConnectedEvent)
    fun onEvent(event: ChatEvent)
}
