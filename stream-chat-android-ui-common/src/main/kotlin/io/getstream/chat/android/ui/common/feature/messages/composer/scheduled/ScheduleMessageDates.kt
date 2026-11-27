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

package io.getstream.chat.android.ui.common.feature.messages.composer.scheduled

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.ScheduledMessage
import io.getstream.chat.android.models.ScheduledMessageStatus
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

/**
 * The quick dates to schedule a message: tomorrow at 9:00 and at 13:00, and the first Monday after tomorrow at 9:00,
 * in [timeZone].
 *
 * @param now The current date.
 * @param timeZone The time zone of the user.
 */
@InternalStreamChatApi
public fun scheduleMessageQuickDates(now: Date, timeZone: TimeZone = TimeZone.getDefault()): List<Date> {
    val tomorrowMorning = Calendar.getInstance(timeZone).apply {
        time = now
        add(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, MORNING_HOUR)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val tomorrowAfternoon = (tomorrowMorning.clone() as Calendar).apply { set(Calendar.HOUR_OF_DAY, AFTERNOON_HOUR) }
    val mondayMorning = (tomorrowMorning.clone() as Calendar).apply {
        do {
            add(Calendar.DAY_OF_YEAR, 1)
        } while (get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY)
    }
    return listOf(tomorrowMorning.time, tomorrowAfternoon.time, mondayMorning.time)
}

/**
 * Whether the scheduled message counts toward the scheduling limits: it is not failed, and its date has not passed,
 * as the server has most likely sent it.
 *
 * @param now The current date.
 */
@InternalStreamChatApi
public fun ScheduledMessage.isPending(now: Date): Boolean =
    status != ScheduledMessageStatus.Failed && scheduledAt.after(now)

private const val MORNING_HOUR = 9
private const val AFTERNOON_HOUR = 13
