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

package io.getstream.chat.android.client.extensions.internal

import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import java.util.Date
import java.util.concurrent.TimeUnit

internal class DateExtensionsTests {

    private val now = Date()
    private val fiveDaysInMillis = TimeUnit.DAYS.toMillis(5)

    @Test
    fun `isOlderThanDays should return true when date is older than specified days`() {
        // given
        val sixDaysAgo = Date(now.time - TimeUnit.DAYS.toMillis(6))

        // when
        val result = sixDaysAgo.isOlderThanDays(fiveDaysInMillis, now)

        // then
        result shouldBeEqualTo true
    }

    @Test
    fun `isOlderThanDays should return false when date is exactly as old as specified days`() {
        // given
        val fiveDaysAgo = Date(now.time - fiveDaysInMillis)

        // when
        val result = fiveDaysAgo.isOlderThanDays(fiveDaysInMillis, now)

        // then
        result shouldBeEqualTo false
    }

    @Test
    fun `isOlderThanDays should return false when date is newer than specified days`() {
        // given
        val threeDaysAgo = Date(now.time - TimeUnit.DAYS.toMillis(3))

        // when
        val result = threeDaysAgo.isOlderThanDays(fiveDaysInMillis, now)

        // then
        result shouldBeEqualTo false
    }

    @Test
    fun `isOlderThanDays should return false when date is in the future`() {
        // given
        val twoDaysInFuture = Date(now.time + TimeUnit.DAYS.toMillis(2))

        // when
        val result = twoDaysInFuture.isOlderThanDays(fiveDaysInMillis, now)

        // then
        result shouldBeEqualTo false
    }

    @Test
    fun `isOlderThanDays should handle zero day threshold correctly`() {
        // given
        val zeroDaysInMillis = TimeUnit.DAYS.toMillis(0)
        val oneMillisAgo = Date(now.time - 1)

        // when
        val result = oneMillisAgo.isOlderThanDays(zeroDaysInMillis, now)

        // then
        result shouldBeEqualTo true
    }

    @Test
    fun `isOlderThanDays should handle large time differences correctly`() {
        // given
        val oneYearInMillis = TimeUnit.DAYS.toMillis(365)
        val twoYearsAgo = Date(now.time - TimeUnit.DAYS.toMillis(365 * 2))

        // when
        val result = twoYearsAgo.isOlderThanDays(oneYearInMillis, now)

        // then
        result shouldBeEqualTo true
    }
}
