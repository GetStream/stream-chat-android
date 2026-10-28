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

import androidx.compose.runtime.Immutable

/**
 * A recurring quiet-hours schedule. While any of its windows is active, the server drops (does not
 * delay) the user's push notifications for chat messages, calls (including ringing) and feeds.
 * Reminder pushes are still delivered.
 *
 * @param timezone IANA time zone name the windows are evaluated in, for example `Europe/Amsterdam`.
 * Offsets such as `+02:00` are rejected. Use `TimeZone.getDefault().id` for the device zone.
 * @param quietHours The quiet windows, 1 to 7 rules.
 */
@Immutable
public data class PushSchedule(
    public val timezone: String,
    public val quietHours: List<QuietHoursRule>,
)

/**
 * One recurring quiet window, in local wall-clock time of the [PushSchedule.timezone].
 *
 * A window belongs to the day it opens on: [QuietHoursDay.friday] with 22:00 to 07:00 is quiet from
 * Friday 22:00 until Saturday 07:00.
 *
 * @param start When the window opens, as `HH:MM` from `00:00` to `23:59`. Must differ from [end].
 * @param end When the window closes, as `HH:MM` from `00:00` to `24:00`, exclusive. An [end]
 * earlier than [start] wraps past midnight. `00:00` to `24:00` is a whole day.
 * @param days The days the window opens on. Empty means every day.
 */
@Immutable
public data class QuietHoursRule(
    public val start: String,
    public val end: String,
    public val days: List<QuietHoursDay> = emptyList(),
)

/**
 * A day of the week in a [QuietHoursRule]. Wraps the raw wire string so unknown values round-trip
 * without being lost.
 */
@Immutable
public data class QuietHoursDay(public val value: String) {

    public companion object {
        public val monday: QuietHoursDay = QuietHoursDay(value = "mon")
        public val tuesday: QuietHoursDay = QuietHoursDay(value = "tue")
        public val wednesday: QuietHoursDay = QuietHoursDay(value = "wed")
        public val thursday: QuietHoursDay = QuietHoursDay(value = "thu")
        public val friday: QuietHoursDay = QuietHoursDay(value = "fri")
        public val saturday: QuietHoursDay = QuietHoursDay(value = "sat")
        public val sunday: QuietHoursDay = QuietHoursDay(value = "sun")

        /** Monday to Friday. */
        public val weekdays: List<QuietHoursDay> =
            listOf(monday, tuesday, wednesday, thursday, friday)

        /** Saturday and Sunday. */
        public val weekend: List<QuietHoursDay> = listOf(saturday, sunday)
    }
}
