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

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeGreaterThan
import org.amshove.kluent.shouldBeLessThan
import org.junit.jupiter.api.Test

internal class TimeDurationTest {

    @Test
    fun `test equivalence`() {
        TimeDuration.hours(24) shouldBeEqualTo TimeDuration.days(1)
        TimeDuration.minutes(60) shouldBeEqualTo TimeDuration.hours(1)
        TimeDuration.seconds(60) shouldBeEqualTo TimeDuration.minutes(1)
        TimeDuration.millis(1000) shouldBeEqualTo TimeDuration.seconds(1)
    }

    @Test
    fun `test comparability`() {
        TimeDuration.hours(25) shouldBeGreaterThan TimeDuration.days(1)
        TimeDuration.minutes(61) shouldBeGreaterThan TimeDuration.hours(1)
        TimeDuration.seconds(61) shouldBeGreaterThan TimeDuration.minutes(1)
        TimeDuration.millis(1001) shouldBeGreaterThan TimeDuration.seconds(1)

        TimeDuration.hours(23) shouldBeLessThan TimeDuration.days(1)
        TimeDuration.minutes(59) shouldBeLessThan TimeDuration.hours(1)
        TimeDuration.seconds(59) shouldBeLessThan TimeDuration.minutes(1)
        TimeDuration.millis(999) shouldBeLessThan TimeDuration.seconds(1)
    }

    @Test
    fun `test transformation`() {
        val oneDay = TimeDuration.days(1)
        oneDay.days shouldBeEqualTo 1
        oneDay.hours shouldBeEqualTo 24
        oneDay.minutes shouldBeEqualTo 24 * 60
        oneDay.seconds shouldBeEqualTo 24 * 60 * 60
        oneDay.millis shouldBeEqualTo 24 * 60 * 60 * 1000

        val oneHour = TimeDuration.hours(1)
        oneHour.days shouldBeEqualTo 0
        oneHour.hours shouldBeEqualTo 1
        oneHour.minutes shouldBeEqualTo 60
        oneHour.seconds shouldBeEqualTo 60 * 60
        oneHour.millis shouldBeEqualTo 60 * 60 * 1000

        val oneMinute = TimeDuration.minutes(1)
        oneMinute.days shouldBeEqualTo 0
        oneMinute.hours shouldBeEqualTo 0
        oneMinute.minutes shouldBeEqualTo 1
        oneMinute.seconds shouldBeEqualTo 60
        oneMinute.millis shouldBeEqualTo 60 * 1000

        val oneSecond = TimeDuration.seconds(1)
        oneSecond.days shouldBeEqualTo 0
        oneSecond.hours shouldBeEqualTo 0
        oneSecond.minutes shouldBeEqualTo 0
        oneSecond.seconds shouldBeEqualTo 1
        oneSecond.millis shouldBeEqualTo 1000

        val oneMillisecond = TimeDuration.millis(1)
        oneMillisecond.days shouldBeEqualTo 0
        oneMillisecond.hours shouldBeEqualTo 0
        oneMillisecond.minutes shouldBeEqualTo 0
        oneMillisecond.seconds shouldBeEqualTo 0
        oneMillisecond.millis shouldBeEqualTo 1
    }
}
