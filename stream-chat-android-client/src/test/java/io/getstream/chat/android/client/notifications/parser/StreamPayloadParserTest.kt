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

package io.getstream.chat.android.client.notifications.parser

import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

public class StreamPayloadParserTest {

    @Test
    public fun parseNullContent() {
        val parsedMap = StreamPayloadParser.parse(null)
        parsedMap shouldBeEqualTo emptyMap()
    }

    @Test
    public fun parseEmptyContent() {
        val jsonString = ""

        val parsedMap = StreamPayloadParser.parse(jsonString)
        parsedMap shouldBeEqualTo emptyMap()
    }

    @Test
    public fun parseExpectedContent() {
        val jsonString = """{
                "file_type": "file",
                "message": "Retro",
                "sender_name": "Lando Calrissian",
                "unread_message_count": "8",
                "nested_object": {
                    "key1": "value1",
                    "key2": "value2",
                    "key3": null,
                    "key4": "null"
                },
                "nested_array": [
                    "item1",
                    "item2",
                    "item3"
                ]
            }"""

        val parsedMap = StreamPayloadParser.parse(jsonString)
        parsedMap shouldBeEqualTo mapOf(
            "file_type" to "file",
            "message" to "Retro",
            "sender_name" to "Lando Calrissian",
            "unread_message_count" to "8",
            "nested_object" to mapOf(
                "key1" to "value1",
                "key2" to "value2",
                "key3" to null,
                "key4" to "null",
            ),
            "nested_array" to listOf(
                "item1",
                "item2",
                "item3",
            ),
        )
    }
}
