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

package io.getstream.chat.android.ui.common.utils

import kotlin.time.DurationUnit
import kotlin.time.toDuration

private const val TIME_DIVIDER = 60

public object DurationFormatter {

    /**
     * Formats duration in millis into string of mm:ss.
     */
    public fun formatDurationInMillis(durationInMillis: Int): String {
        val duration = durationInMillis.toDuration(DurationUnit.MILLISECONDS)
        val seconds = duration.inWholeSeconds.rem(TIME_DIVIDER).toString().padStart(2, '0')
        val minutes = duration.inWholeMinutes.rem(TIME_DIVIDER).toString().padStart(2, '0')

        return "$minutes:$seconds"
    }

    /**
     * Formats duration in seconds into string of mm:ss.
     */
    public fun formatDurationInSeconds(durationInSeconds: Float): String {
        val millis = durationInSeconds.times(1000).toInt()
        val duration = millis.toDuration(DurationUnit.MILLISECONDS)
        val seconds = duration.inWholeSeconds.rem(TIME_DIVIDER).toString().padStart(2, '0')
        val minutes = duration.inWholeMinutes.rem(TIME_DIVIDER).toString().padStart(2, '0')

        return "$minutes:$seconds"
    }
}
