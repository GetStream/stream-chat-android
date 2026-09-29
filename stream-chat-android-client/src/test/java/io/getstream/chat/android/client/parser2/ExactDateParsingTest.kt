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

import io.getstream.chat.android.client.parser2.adapters.internal.StreamDateFormatter
import io.getstream.chat.android.network.infrastructure.ExactDate
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

/**
 * The generated [ExactDate] against the SDK's [StreamDateFormatter] on the path unit tests take, the
 * `SimpleDateFormat` fallback on the desktop JVM. There `S` counts milliseconds, so the two only agree up to
 * millisecond fractions; Android documents `S` as fractional seconds, where both read longer fractions alike.
 */
internal class ExactDateParsingTest {

    @ParameterizedTest
    @MethodSource("timestamps")
    fun `The generated ExactDate keeps the raw string and parses the instant to the millisecond`(
        raw: String,
        millis: Long,
        sdkAgrees: Boolean,
    ) {
        val parsed = ExactDate.parseOrNull(raw)!!

        parsed.raw shouldBeEqualTo raw
        parsed.date.time shouldBeEqualTo millis
        (StreamDateFormatter().parse(raw)?.time == millis) shouldBeEqualTo sdkAgrees
    }

    companion object {
        private const val SECOND = 1593411268000L

        @JvmStatic
        fun timestamps(): List<Arguments> = listOf(
            Arguments.of("2020-06-29T06:14:28Z", SECOND, true),
            Arguments.of("2020-06-29T06:14:28.000Z", SECOND, true),
            Arguments.of("2020-06-29T06:14:28.123Z", SECOND + 123, true),
            // The desktop JVM reads every fraction digit as milliseconds; ExactDate normalizes to three first.
            Arguments.of("2020-06-29T06:14:28.123456Z", SECOND + 123, false),
            Arguments.of("2020-06-29T06:14:28.123456789Z", SECOND + 123, false),
        )
    }
}
