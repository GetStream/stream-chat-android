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

/**
 * Every event fixture parses through the v2 event parser, with and without the fast path, to its expected domain
 * event.
 */
internal class EventFixtureParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()
    private val fastParser = ParserFactory.createMoshiChatParser(fastEventParsing = true)

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.parser.EventArguments#eventAdapterArgumentsList")
    fun `An event fixture parses to its domain event`(eventData: String, expectedEvent: ChatEvent) {
        assertEquals(expectedEvent, parser.fromJson(eventData, ChatEvent::class.java))
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.parser.EventArguments#eventAdapterArgumentsList")
    fun `An event fixture parses to the same domain event with fast event parsing`(
        eventData: String,
        expectedEvent: ChatEvent,
    ) {
        assertEquals(expectedEvent, fastParser.fromJson(eventData, ChatEvent::class.java))
    }
}
