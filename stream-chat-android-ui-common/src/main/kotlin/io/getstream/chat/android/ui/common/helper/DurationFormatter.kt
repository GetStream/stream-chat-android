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

package io.getstream.chat.android.ui.common.helper

import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Interface for formatting durations into a string representation.
 */
public interface DurationFormatter {
    /**
     * Formats the given duration in milliseconds into a string representation.
     */
    public fun format(durationInMillis: Int): String

    public companion object {
        /**
         * Creates the default [DurationFormatter] which formats durations as "mm:ss".
         */
        public fun defaultFormatter(): DurationFormatter = DefaultDurationFormatter()
    }
}

private const val TIME_DIVIDER = 60

private class DefaultDurationFormatter : DurationFormatter {

    override fun format(durationInMillis: Int): String {
        val duration = durationInMillis.toDuration(DurationUnit.MILLISECONDS)
        val minutes = duration.inWholeMinutes.toString().padStart(2, '0')
        val seconds = duration.inWholeSeconds.rem(TIME_DIVIDER).toString().padStart(2, '0')
        return "$minutes:$seconds"
    }
}
