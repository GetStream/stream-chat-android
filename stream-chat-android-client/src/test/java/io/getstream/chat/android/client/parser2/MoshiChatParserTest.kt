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

package io.getstream.chat.android.client.parser2

import io.getstream.chat.android.client.events.ChatEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class MoshiChatParserTest {

    private val parser = ParserFactory.createMoshiChatParser()

    /** [io.getstream.chat.android.client.parser.EventArguments.eventAdapterArgumentsList] */
    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.parser.EventArguments#eventAdapterArgumentsList")
    fun `Should create proper event`(eventData: String, expectedEvent: ChatEvent) {
        val parsedEvent = parser.fromJson(eventData, ChatEvent::class.java)
        assertEquals(expectedEvent, parsedEvent)
    }
}
