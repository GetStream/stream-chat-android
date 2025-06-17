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

package io.getstream.chat.android.client

import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomLong
import io.getstream.chat.android.randomString
import org.amshove.kluent.`should be`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class PayloadValidatorTest {

    /** [isFromStreamServerArguments] */
    @ParameterizedTest
    @MethodSource("isFromStreamServerArguments")
    fun `PayloadValidator should verify if payload comes from Stream Server or not`(
        payload: Map<String, Any?>,
        expectedResult: Boolean,
    ) {
        PayloadValidator.isFromStreamServer(payload) `should be` expectedResult
    }

    /** [isValidNewMessageArguments] */
    @ParameterizedTest
    @MethodSource("isValidNewMessageArguments")
    fun `PayloadValidator should verify if payload contains a valid New Message Payload`(
        payload: Map<String, Any?>,
        expectedResult: Boolean,
    ) {
        PayloadValidator.isValidNewMessage(payload) `should be` expectedResult
    }

    /** [isValidPayloadArguments] **/
    @ParameterizedTest
    @MethodSource("isValidPayloadArguments")
    fun `PayloadValidator should verify if payload contains a valid Payload`(
        payload: Map<String, Any?>,
        expectedResult: Boolean,
    ) {
        PayloadValidator.isValidPayload(payload) `should be` expectedResult
    }

    companion object {

        @JvmStatic
        fun isFromStreamServerArguments() =
            isFromStreamServerV1Arguments() +
                isFromStreamServerV2Arguments() +
                isFromStreamServerUnknownVersionArguments()

        private fun isFromStreamServerV1Arguments() = listOf(
            Arguments.of(mapOf("version" to "v1"), false),
            Arguments.of(mapOf("version" to "v1", "sender_server" to ""), false),
            Arguments.of(mapOf("version" to "v1", "sender_server" to null), false),
            Arguments.of(mapOf("version" to "v1", "sender_server" to randomString()), false),
            Arguments.of(mapOf("version" to "v1", "sender_server" to randomInt()), false),
            Arguments.of(mapOf("version" to "v1", "sender_server" to randomLong()), false),
            Arguments.of(mapOf("version" to "v1", "sender_server" to "stream.chat"), true),
            Arguments.of(
                mapOf(
                    "version" to "v1",
                    "sender_server" to "stream.chat",
                    randomString() to randomString(),
                ),
                true,
            ),
        )

        private fun isFromStreamServerV2Arguments() = listOf(
            Arguments.of(mapOf("version" to "v2"), false),
            Arguments.of(mapOf("version" to "v2", "sender" to ""), false),
            Arguments.of(mapOf("version" to "v2", "sender" to null), false),
            Arguments.of(mapOf("version" to "v2", "sender" to randomString()), false),
            Arguments.of(mapOf("version" to "v2", "sender" to randomInt()), false),
            Arguments.of(mapOf("version" to "v2", "sender" to randomLong()), false),
            Arguments.of(mapOf("version" to "v2", "sender" to "stream.chat"), true),
            Arguments.of(mapOf("version" to "v2", "sender" to "stream.chat", randomString() to randomString()), true),
        )

        private fun isFromStreamServerUnknownVersionArguments() = listOf(
            Arguments.of(emptyMap<String, Any?>(), false),
            Arguments.of(mapOf("version" to ""), false),
            Arguments.of(mapOf("version" to randomString()), false),
            Arguments.of(mapOf("sender" to ""), false),
            Arguments.of(mapOf("version" to randomString(), "sender" to ""), false),
            Arguments.of(mapOf("sender" to null), false),
            Arguments.of(mapOf("version" to randomString(), "sender" to null), false),
            Arguments.of(mapOf("sender" to randomString()), false),
            Arguments.of(mapOf("version" to randomString(), "sender" to randomString()), false),
            Arguments.of(mapOf("sender" to randomInt()), false),
            Arguments.of(mapOf("version" to randomString(), "sender" to randomInt()), false),
            Arguments.of(mapOf("sender" to randomLong()), false),
            Arguments.of(mapOf("version" to randomString(), "sender" to randomLong()), false),
            Arguments.of(mapOf("sender" to "stream.chat"), false),
            Arguments.of(mapOf("version" to randomString(), "sender" to "stream.chat"), false),
            Arguments.of(mapOf("sender" to "stream.chat", randomString() to randomString()), false),
            Arguments.of(
                mapOf(
                    "version" to randomString(),
                    "sender" to "stream.chat",
                    randomString() to randomString(),
                ),
                false,
            ),
        )

        @JvmStatic
        fun isValidNewMessageArguments() =
            isValidNewMessageV1Arguments() +
                isValidNewMessageV2Arguments()

        @Suppress("LongMethod")
        private fun isValidNewMessageV1Arguments() = listOf(
            Arguments.of(emptyMap<String, Any?>(), false),
            Arguments.of(mapOf("version" to "v1"), false),
            Arguments.of(
                mapOf(
                    "version" to "v1",
                    "channel_id" to randomString(),
                ),
                false,
            ),
            Arguments.of(
                mapOf(
                    "version" to "v1",
                    "channel_id" to randomString(),
                ),
                false,
            ),
            Arguments.of(
                mapOf(
                    "version" to "v1",
                    "channel_id" to randomString(),
                    "message_id" to randomString(),
                ),
                false,
            ),
            Arguments.of(
                mapOf(
                    "version" to "v1",
                    "channel_id" to randomString(),
                    "message_id" to randomString(),
                ),
                false,
            ),
            Arguments.of(
                mapOf(
                    "version" to "v1",
                    "channel_id" to null,
                    "message_id" to randomString(),
                    "channel_type" to randomString(),
                ),
                false,
            ),
            Arguments.of(
                mapOf(
                    "version" to "v1",
                    "channel_id" to randomString(),
                    "message_id" to null,
                    "channel_type" to randomString(),
                ),
                false,
            ),
            Arguments.of(
                mapOf(
                    "version" to "v1",
                    "channel_id" to randomString(),
                    "message_id" to randomString(),
                    "channel_type" to null,
                ),
                false,
            ),
            Arguments.of(
                mapOf(
                    "version" to "v1",
                    "channel_id" to "",
                    "message_id" to randomString(),
                    "channel_type" to randomString(),
                ),
                false,
            ),
            Arguments.of(
                mapOf(
                    "version" to "v1",
                    "channel_id" to randomString(),
                    "message_id" to "",
                    "channel_type" to randomString(),
                ),
                false,
            ),
            Arguments.of(
                mapOf(
                    "version" to "v1",
                    "channel_id" to randomString(),
                    "message_id" to randomString(),
                    "channel_type" to "",
                ),
                false,
            ),
            Arguments.of(
                mapOf(
                    "version" to "v1",
                    "channel_id" to randomString(),
                    "message_id" to randomString(),
                    "channel_type" to randomString(),
                ),
                true,
            ),
        )

        @Suppress("LongMethod")
        private fun isValidNewMessageV2Arguments() = listOf(
            Arguments.of(emptyMap<String, Any?>(), false),
            Arguments.of(mapOf("version" to "v2", "type" to ""), false),
            Arguments.of(mapOf("version" to "v2", "type" to "message.new"), false),
            Arguments.of(
                mapOf(
                    "version" to "v2",
                    "type" to "",
                    "channel_id" to randomString(),
                ),
                false,
            ),
            Arguments.of(
                mapOf(
                    "version" to "v2",
                    "type" to "message.new",
                    "channel_id" to randomString(),
                ),
                false,
            ),
            Arguments.of(
                mapOf(
                    "version" to "v2",
                    "type" to "",
                    "channel_id" to randomString(),
                    "message_id" to randomString(),
                ),
                false,
            ),
            Arguments.of(
                mapOf(
                    "version" to "v2",
                    "type" to "message.new",
                    "channel_id" to randomString(),
                    "message_id" to randomString(),
                ),
                false,
            ),
            Arguments.of(
                mapOf(
                    "version" to "v2",
                    "type" to "",
                    "channel_id" to randomString(),
                    "message_id" to randomString(),
                    "channel_type" to randomString(),
                ),
                false,
            ),
            Arguments.of(
                mapOf(
                    "version" to "v2",
                    "type" to "message.new",
                    "channel_id" to null,
                    "message_id" to randomString(),
                    "channel_type" to randomString(),
                ),
                false,
            ),
            Arguments.of(
                mapOf(
                    "version" to "v2",
                    "type" to "message.new",
                    "channel_id" to randomString(),
                    "message_id" to null,
                    "channel_type" to randomString(),
                ),
                false,
            ),
            Arguments.of(
                mapOf(
                    "version" to "v2",
                    "type" to "message.new",
                    "channel_id" to randomString(),
                    "message_id" to randomString(),
                    "channel_type" to null,
                ),
                false,
            ),
            Arguments.of(
                mapOf(
                    "version" to "v2",
                    "type" to "message.new",
                    "channel_id" to "",
                    "message_id" to randomString(),
                    "channel_type" to randomString(),
                ),
                false,
            ),
            Arguments.of(
                mapOf(
                    "version" to "v2",
                    "type" to "message.new",
                    "channel_id" to randomString(),
                    "message_id" to "",
                    "channel_type" to randomString(),
                ),
                false,
            ),
            Arguments.of(
                mapOf(
                    "version" to "v2",
                    "type" to "message.new",
                    "channel_id" to randomString(),
                    "message_id" to randomString(),
                    "channel_type" to "",
                ),
                false,
            ),
            Arguments.of(
                mapOf(
                    "version" to "v2",
                    "channel_id" to randomString(),
                    "message_id" to randomString(),
                    "channel_type" to randomString(),
                ),
                false,
            ),
            Arguments.of(
                mapOf(
                    "version" to "v2",
                    "type" to "message.new",
                    "channel_id" to randomString(),
                    "message_id" to randomString(),
                    "channel_type" to randomString(),
                ),
                true,
            ),
        )

        @JvmStatic
        private fun isValidPayloadArguments() =
            isValidNewMessageV2Arguments() +
                Arguments.of(
                    mapOf(
                        "version" to "v2",
                        "type" to "notification.reminder_due",
                        "channel_id" to randomString(),
                        "message_id" to randomString(),
                        "channel_type" to randomString(),
                    ),
                    true,
                )
    }
}
