/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Represents the amount of time one instant of time is away from another instant.
 */
public class TimeDuration private constructor(
    private val duration: Duration,
) : Comparable<TimeDuration> {

    /**
     * Returns the number of milliseconds in this duration.
     */
    public val millis: Long get() = duration.inWholeMilliseconds

    /**
     * Returns the number of seconds in this duration.
     */
    public val seconds: Long get() = duration.inWholeSeconds

    /**
     * Returns the number of minutes in this duration.
     */
    public val minutes: Long get() = duration.inWholeMinutes

    /**
     * Returns the number of hours in this duration.
     */
    public val hours: Long get() = duration.inWholeHours

    /**
     * Returns the number of days in this duration.
     */
    public val days: Long get() = duration.inWholeDays

    override fun compareTo(other: TimeDuration): Int = duration.compareTo(other.duration)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TimeDuration) return false
        return duration == other.duration
    }

    override fun hashCode(): Int = duration.hashCode()

    override fun toString(): String = duration.toString()

    public companion object {
        /**
         * Creates a [TimeDuration] from the specified number of milliseconds.
         */
        public fun millis(millis: Long): TimeDuration = TimeDuration(millis.milliseconds)

        /**
         * Creates a [TimeDuration] from the specified number of seconds.
         */
        public fun seconds(seconds: Int): TimeDuration = TimeDuration(seconds.seconds)

        /**
         * Creates a [TimeDuration] from the specified number of minutes.
         */
        public fun minutes(minutes: Int): TimeDuration = TimeDuration(minutes.minutes)

        /**
         * Creates a [TimeDuration] from the specified number of hours.
         */
        public fun hours(hours: Int): TimeDuration = TimeDuration(hours.hours)

        /**
         * Creates a [TimeDuration] from the specified number of days.
         */
        public fun days(days: Int): TimeDuration = TimeDuration(days.days)
    }
}
