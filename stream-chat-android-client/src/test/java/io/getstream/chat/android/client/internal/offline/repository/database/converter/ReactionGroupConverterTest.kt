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

package io.getstream.chat.android.client.internal.offline.repository.database.converter

import io.getstream.chat.android.client.internal.offline.repository.database.converter.ReactionGroupConverterTest.Companion.decodeArguments
import io.getstream.chat.android.client.internal.offline.repository.database.converter.ReactionGroupConverterTest.Companion.decodeMapArguments
import io.getstream.chat.android.client.internal.offline.repository.database.converter.ReactionGroupConverterTest.Companion.encodeArguments
import io.getstream.chat.android.client.internal.offline.repository.database.converter.ReactionGroupConverterTest.Companion.encodeMapArguments
import io.getstream.chat.android.client.internal.offline.repository.database.database.converter.internal.ReactionGroupConverter
import io.getstream.chat.android.client.internal.offline.repository.domain.message.internal.ReactionGroupEntity
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomString
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Date

internal class ReactionGroupConverterTest {
    private val reactionGroupConverter = ReactionGroupConverter()

    /** This method tests the [encodeArguments] method */
    @ParameterizedTest
    @MethodSource("encodeArguments")
    fun testEncodeEntity(
        reactionGroupEntity: ReactionGroupEntity?,
        expectedResult: String?,
    ) {
        val result = reactionGroupConverter.reactionGroupEntityToString(reactionGroupEntity)

        result `should be equal to` expectedResult
    }

    /** This method tests the [decodeArguments] method */
    @ParameterizedTest
    @MethodSource("decodeArguments")
    fun testDecodeEntity(
        source: String?,
        expectedResult: ReactionGroupEntity?,
    ) {
        val result = reactionGroupConverter.stringToReactionGroupEntity(source)

        result `should be equal to` expectedResult
    }

    /** This method tests the [encodeMapArguments] method */
    @ParameterizedTest
    @MethodSource("encodeMapArguments")
    fun testEncodeEntityMap(
        reactionGroupEntityMap: Map<String, ReactionGroupEntity>?,
        expectedResult: String?,
    ) {
        val result = reactionGroupConverter.reactionGroupEntityMapToString(reactionGroupEntityMap)

        result `should be equal to` expectedResult
    }

    /** This method tests the [decodeMapArguments] method */
    @ParameterizedTest
    @MethodSource("decodeMapArguments")
    fun testDecodeEntityMap(
        source: String?,
        expectedResult: Map<String, ReactionGroupEntity>?,
    ) {
        val result = reactionGroupConverter.stringToReactionGroupEntityMap(source)

        result `should be equal to` expectedResult
    }

    companion object {
        private val count = randomInt()
        private val sumScore = randomInt()
        private val type = randomString()

        @JvmStatic
        fun encodeArguments(): List<Arguments> =
            listOf(
                Arguments.of(
                    null,
                    null,
                ),
                Arguments.of(
                    ReactionGroupEntity(
                        type = type,
                        count = count,
                        sumScore = sumScore,
                        firstReactionAt = Date(0),
                        lastReactionAt = Date(0),
                    ),
                    "{" +
                        "\"type\":\"$type\"," +
                        "\"count\":$count," +
                        "\"sumScore\":$sumScore," +
                        "\"firstReactionAt\":\"1970-01-01T00:00:00.000Z\"," +
                        "\"lastReactionAt\":\"1970-01-01T00:00:00.000Z" +
                        "\"}",
                ),
            )

        @JvmStatic
        fun decodeArguments(): List<Arguments> =
            listOf(
                Arguments.of(
                    null,
                    null,
                ),
                Arguments.of(
                    "{" +
                        "\"type\":\"$type\"," +
                        "\"count\":$count," +
                        "\"sumScore\":$sumScore," +
                        "\"firstReactionAt\":\"1970-01-01T00:00:00.000Z\"," +
                        "\"lastReactionAt\":\"1970-01-01T00:00:00.000Z" +
                        "\"}",
                    ReactionGroupEntity(
                        type = type,
                        count = count,
                        sumScore = sumScore,
                        firstReactionAt = Date(0),
                        lastReactionAt = Date(0),
                    ),
                ),
            )

        @JvmStatic
        fun encodeMapArguments(): List<Arguments> =
            listOf(
                Arguments.of(
                    null,
                    null,
                ),
                Arguments.of(
                    mapOf(
                        type to ReactionGroupEntity(
                            type = type,
                            count = count,
                            sumScore = sumScore,
                            firstReactionAt = Date(0),
                            lastReactionAt = Date(0),
                        ),
                    ),
                    "{\"$type\":" +
                        "{" +
                        "\"type\":\"$type\"," +
                        "\"count\":$count," +
                        "\"sumScore\":$sumScore," +
                        "\"firstReactionAt\":\"1970-01-01T00:00:00.000Z\"," +
                        "\"lastReactionAt\":\"1970-01-01T00:00:00.000Z" +
                        "\"}" +
                        "}",
                ),
            )

        @JvmStatic
        fun decodeMapArguments(): List<Arguments> =
            listOf(
                Arguments.of(
                    null,
                    mutableMapOf<String, ReactionGroupEntity>(),
                ),
                Arguments.of(
                    "{\"$type\":" +
                        "{" +
                        "\"type\":\"$type\"," +
                        "\"count\":$count," +
                        "\"sumScore\":$sumScore," +
                        "\"firstReactionAt\":\"1970-01-01T00:00:00.000Z\"," +
                        "\"lastReactionAt\":\"1970-01-01T00:00:00.000Z" +
                        "\"}" +
                        "}",
                    mapOf(
                        type to ReactionGroupEntity(
                            type = type,
                            count = count,
                            sumScore = sumScore,
                            firstReactionAt = Date(0),
                            lastReactionAt = Date(0),
                        ),
                    ),
                ),
            )
    }
}
