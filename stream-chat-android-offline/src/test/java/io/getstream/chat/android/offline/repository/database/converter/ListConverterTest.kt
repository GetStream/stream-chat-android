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

package io.getstream.chat.android.offline.repository.database.converter

import io.getstream.chat.android.offline.repository.database.converter.internal.ListConverter
import io.getstream.chat.android.offline.repository.domain.channel.userread.internal.ChannelUserReadEntity
import io.getstream.chat.android.offline.repository.domain.user.internal.UserMuteEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.Date

internal class ListConverterTest {
    private val sut = ListConverter()

    @ParameterizedTest
    @MethodSource("toStringListArgs")
    fun toStringList(input: String?, expected: List<String>?) {
        val result = sut.toStringList(input)

        assertEquals(expected, result)
    }

    @ParameterizedTest
    @MethodSource("fromStringListArgs")
    fun fromStringList(input: List<String>?, expected: String?) {
        val result = sut.fromStringList(input)

        assertEquals(expected, result)
    }

    @ParameterizedTest
    @MethodSource("toReadListArgs")
    fun toReadList(input: String?, expected: List<ChannelUserReadEntity>?) {
        val result = sut.toReadList(input)

        assertEquals(expected, result)
    }

    @ParameterizedTest
    @MethodSource("fromReadListArgs")
    fun fromReadList(input: List<ChannelUserReadEntity>?, expected: String?) {
        val result = sut.fromReadList(input)

        assertEquals(expected, result)
    }

    @ParameterizedTest
    @MethodSource("toUserMuteListArgs")
    fun toUserMuteList(input: String?, expected: List<UserMuteEntity>?) {
        val result = sut.toUserMuteList(input)

        assertEquals(expected, result)
    }

    @ParameterizedTest
    @MethodSource("fromUserMuteListArgs")
    fun fromUserMuteList(input: List<UserMuteEntity>?, expected: String?) {
        val result = sut.fromUserMuteList(input)

        assertEquals(expected, result)
    }

    companion object {
        @JvmStatic
        fun toStringListArgs() = listOf(
            arrayOf<Any?>(null, emptyList<String>()),
            arrayOf<Any?>("", emptyList<String>()),
            arrayOf<Any?>("[]", emptyList<String>()),
            arrayOf<Any?>("null", emptyList<String>()),
            arrayOf(
                """["a","b","c"]""",
                listOf("a", "b", "c"),
            ),
        )

        @JvmStatic
        fun fromStringListArgs() = listOf(
            arrayOf<Any?>(null, "null"),
            arrayOf<Any?>(emptyList<String>(), "[]"),
            arrayOf(
                listOf("a", "b", "c"),
                """["a","b","c"]""",
            ),
        )

        @JvmStatic
        fun toReadListArgs() = listOf(
            arrayOf<Any?>(null, emptyList<ChannelUserReadEntity>()),
            arrayOf<Any?>("", emptyList<ChannelUserReadEntity>()),
            arrayOf<Any?>("[]", emptyList<ChannelUserReadEntity>()),
            arrayOf<Any?>("null", emptyList<ChannelUserReadEntity>()),
            arrayOf(
                """[{"userId":"user-id",
                    "lastReceivedEventDate":"1970-01-01T00:00:00.001Z",
                    "unreadMessages":5,"lastRead":"1970-01-01T00:00:00.001Z",
                    "lastReadMessageId":"message-id",
                    "lastDeliveredAt":"1970-01-01T00:00:00.001Z",
                    "lastDeliveredMessageId":"delivered-message-id"}]"""
                    .replace("\\s+".toRegex(), ""),
                listOf(
                    ChannelUserReadEntity(
                        userId = "user-id",
                        lastReceivedEventDate = Date(1),
                        unreadMessages = 5,
                        lastRead = Date(1),
                        lastReadMessageId = "message-id",
                        lastDeliveredAt = Date(1),
                        lastDeliveredMessageId = "delivered-message-id",
                    ),
                ),
            ),
        )

        @JvmStatic
        fun fromReadListArgs() = listOf(
            arrayOf<Any?>(null, "null"),
            arrayOf<Any?>(emptyList<ChannelUserReadEntity>(), "[]"),
            arrayOf(
                listOf(
                    ChannelUserReadEntity(
                        userId = "user-id",
                        lastReceivedEventDate = Date(1),
                        unreadMessages = 5,
                        lastRead = Date(1),
                        lastReadMessageId = "message-id",
                        lastDeliveredAt = Date(1),
                        lastDeliveredMessageId = "delivered-message-id",
                    ),
                ),
                """[{"userId":"user-id",
                    "lastReceivedEventDate":"1970-01-01T00:00:00.001Z",
                    "unreadMessages":5,"lastRead":"1970-01-01T00:00:00.001Z",
                    "lastReadMessageId":"message-id",
                    "lastDeliveredAt":"1970-01-01T00:00:00.001Z",
                    "lastDeliveredMessageId":"delivered-message-id"}]"""
                    .replace("\\s+".toRegex(), ""),
            ),
        )

        @JvmStatic
        fun toUserMuteListArgs() = listOf(
            arrayOf<Any?>(null, emptyList<UserMuteEntity>()),
            arrayOf<Any?>("", emptyList<UserMuteEntity>()),
            arrayOf<Any?>("[]", emptyList<UserMuteEntity>()),
            arrayOf<Any?>("null", emptyList<UserMuteEntity>()),
            arrayOf(
                """[{"userId":"user-id",
                    "targetId":"target-id",
                    "createdAt":"1970-01-01T00:00:00.001Z",
                    "updatedAt":"1970-01-01T00:00:00.001Z",
                    "expires":"1970-01-01T00:00:00.001Z"}]"""
                    .replace("\\s+".toRegex(), ""),
                listOf(
                    UserMuteEntity(
                        userId = "user-id",
                        targetId = "target-id",
                        createdAt = Date(1),
                        updatedAt = Date(1),
                        expires = Date(1),
                    ),
                ),
            ),
        )

        @JvmStatic
        fun fromUserMuteListArgs() = listOf(
            arrayOf<Any?>(null, "null"),
            arrayOf<Any?>(emptyList<UserMuteEntity>(), "[]"),
            arrayOf(
                listOf(
                    UserMuteEntity(
                        userId = "user-id",
                        targetId = "target-id",
                        createdAt = Date(1),
                        updatedAt = Date(1),
                        expires = Date(1),
                    ),
                ),
                """[{"userId":"user-id",
                    "targetId":"target-id",
                    "createdAt":"1970-01-01T00:00:00.001Z",
                    "updatedAt":"1970-01-01T00:00:00.001Z",
                    "expires":"1970-01-01T00:00:00.001Z"}]"""
                    .replace("\\s+".toRegex(), ""),
            ),
        )
    }
}
