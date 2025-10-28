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

package io.getstream.chat.android.client.utils

import io.getstream.chat.android.client.events.ChatEvent

internal class EventsConsumer(val expected: List<Class<out ChatEvent>>) {

    var received = mutableListOf<ChatEvent>()

    fun onEvent(event: ChatEvent) {
        received.add(event)
    }

    fun isReceived(): Boolean {
        expected.forEach { expectedType ->
            received.forEach { event ->
                if (expectedType.isInstance(event)) {
                    return true
                }
            }
        }

        return false
    }

    fun isReceivedExactly(check: List<Class<out ChatEvent>>): Boolean = check == expected
}
