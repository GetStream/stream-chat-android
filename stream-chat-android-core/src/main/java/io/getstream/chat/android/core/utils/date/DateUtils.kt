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

@file:JvmName("DateUtils")

package io.getstream.chat.android.core.utils.date

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.TimeDuration
import java.util.Date
import kotlin.math.abs

/**
 * Tests if [this] date is after [that] date.
 */
@InternalStreamChatApi
public infix fun Date?.after(that: Date?): Boolean {
    return when {
        this == null -> false
        that == null -> true
        else -> this.after(that)
    }
}

/**
 * Returns the greater of two dates.
 */
@InternalStreamChatApi
public fun max(dateA: Date?, dateB: Date?): Date? = when (dateA after dateB) {
    true -> dateA
    else -> dateB
}

/**
 * Returns the minimum of two dates.
 */
@InternalStreamChatApi
public fun min(dateA: Date?, dateB: Date?): Date? = when (dateA after dateB) {
    true -> dateB
    else -> dateA
}

/**
 * Returns the maximum of the given dates.
 */
@InternalStreamChatApi
public fun maxOf(vararg dates: Date?): Date? = dates.reduceOrNull { acc, date -> max(acc, date) }

/**
 * Returns the minimum of the given dates.
 */
@InternalStreamChatApi
public fun minOf(vararg dates: Date?): Date? = dates.reduceOrNull { acc, date -> min(acc, date) }

/**
 * Returns difference between [this] date and the [otherTime] in [TimeDuration].
 */
@InternalStreamChatApi
public fun Date.diff(otherTime: Long): TimeDuration {
    val diff = abs(time - otherTime)
    return TimeDuration.millis(diff)
}

/**
 * Returns difference between [this] date and [that] date in [TimeDuration].
 */
@InternalStreamChatApi
public fun Date.diff(that: Date): TimeDuration {
    val diff = abs(time - that.time)
    return TimeDuration.millis(diff)
}

/**
 * Returns true if the date is within the specified duration from now.
 */
@InternalStreamChatApi
public inline fun Date?.isWithinDurationFromNow(
    duration: TimeDuration,
    now: () -> Long = { System.currentTimeMillis() },
): Boolean {
    return this != null && (now() - this.time) < duration.millis
}

/**
 * Truncates the date to the current time if it's in the future.
 */
@InternalStreamChatApi
public inline fun Date?.truncateFuture(
    now: () -> Long = { System.currentTimeMillis() },
): Date? = this?.apply {
    if (time > now()) {
        time = now()
    }
}
