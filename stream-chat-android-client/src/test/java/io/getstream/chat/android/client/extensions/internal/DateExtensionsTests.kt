/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.extensions.internal

import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import java.util.Date
import java.util.concurrent.TimeUnit

internal class DateExtensionsTests {

    @Test
    fun `isLaterThanDays should return true when date is older than specified days`() {
        // given
        val fiveDaysInMillis = TimeUnit.DAYS.toMillis(5)
        val sixDaysAgo = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(6))

        // when
        val result = sixDaysAgo.isLaterThanDays(fiveDaysInMillis)

        // then
        result shouldBeEqualTo true
    }

    @Test
    fun `isLaterThanDays should return false when date is exactly as old as specified days`() {
        // given
        val fiveDaysInMillis = TimeUnit.DAYS.toMillis(5)
        val fiveDaysAgo = Date(System.currentTimeMillis() - fiveDaysInMillis)

        // when
        val result = fiveDaysAgo.isLaterThanDays(fiveDaysInMillis)

        // then
        result shouldBeEqualTo false
    }

    @Test
    fun `isLaterThanDays should return false when date is newer than specified days`() {
        // given
        val fiveDaysInMillis = TimeUnit.DAYS.toMillis(5)
        val threeDaysAgo = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3))

        // when
        val result = threeDaysAgo.isLaterThanDays(fiveDaysInMillis)

        // then
        result shouldBeEqualTo false
    }

    @Test
    fun `isLaterThanDays should return false when date is in the future`() {
        // given
        val fiveDaysInMillis = TimeUnit.DAYS.toMillis(5)
        val twoDaysInFuture = Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(2))

        // when
        val result = twoDaysInFuture.isLaterThanDays(fiveDaysInMillis)

        // then
        result shouldBeEqualTo false
    }

    @Test
    fun `isLaterThanDays should handle zero day threshold correctly`() {
        // given
        val zeroDaysInMillis = TimeUnit.DAYS.toMillis(0)
        val justNow = Date(System.currentTimeMillis() - 1) // 1 millisecond ago

        // when
        val result = justNow.isLaterThanDays(zeroDaysInMillis)

        // then
        result shouldBeEqualTo true
    }

    @Test
    fun `isLaterThanDays should handle large time differences correctly`() {
        // given
        val oneYearInMillis = TimeUnit.DAYS.toMillis(365)
        val twoYearsAgo = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365 * 2))

        // when
        val result = twoYearsAgo.isLaterThanDays(oneYearInMillis)

        // then
        result shouldBeEqualTo true
    }
}
