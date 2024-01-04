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

@file:JvmName("DateUtils")

package io.getstream.chat.android.core.utils.date

import io.getstream.chat.android.core.internal.InternalStreamChatApi
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
 * Check if current date has difference with [other] no more that [offset].
 */
@InternalStreamChatApi
public fun Date.inOffsetWith(other: Date, offset: Long): Boolean = (time + offset) >= other.time

/**
 * Returns difference between [this] date and [that] date in [TimeDuration].
 */
@InternalStreamChatApi
public fun Date.diff(that: Date): Long {
    return abs(time - that.time)
}
