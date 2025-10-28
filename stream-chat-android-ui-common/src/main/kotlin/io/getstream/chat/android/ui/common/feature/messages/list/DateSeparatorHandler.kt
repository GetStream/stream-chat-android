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

package io.getstream.chat.android.ui.common.feature.messages.list

import io.getstream.chat.android.client.extensions.getCreatedAtOrDefault
import io.getstream.chat.android.client.extensions.getCreatedAtOrNull
import io.getstream.chat.android.client.extensions.internal.NEVER
import io.getstream.chat.android.models.Message
import java.util.Calendar
import java.util.Date

/**
 * A SAM designed to evaluate if a date separator should be added between messages.
 */
public fun interface DateSeparatorHandler {

    /**
     * Determines whether we should add the date separator or not.
     *
     * @param previousMessage The [Message] before the one we are currently evaluating.
     * @param message The [Message] before which we want to add a date separator or not.
     *
     * @return Whether to add the date separator or not.
     */
    public fun shouldAddDateSeparator(previousMessage: Message?, message: Message): Boolean

    public companion object {

        private val defaultDateSeparatorHandler = DateSeparatorHandler { previousMessage, message ->
            !message.getCreatedAtOrDefault(NEVER).isInTheSameDay(
                previousMessage?.getCreatedAtOrNull() ?: NEVER,
            )
        }

        /**
         * Creates a [DateSeparatorHandler] returning true if the messages are not in the same day.
         *
         * @return The default normal list date separator handler.
         */
        public fun getDefaultDateSeparatorHandler(): DateSeparatorHandler = defaultDateSeparatorHandler

        /**
         * Creates a [DateSeparatorHandler] returning true if the messages are not in the same day.
         *
         * @return The default thread date separator handler.
         */
        public fun getDefaultThreadDateSeparatorHandler(): DateSeparatorHandler = DateSeparatorHandler { previousMessage, message ->
            previousMessage?.let { defaultDateSeparatorHandler.shouldAddDateSeparator(it, message) } ?: false
        }

        /**
         * Checks if the two dates are in the same day.
         *
         * @param that The date to compare with.
         * @return True if the two dates are in the same day, false otherwise.
         */
        private fun Date.isInTheSameDay(that: Date): Boolean {
            val thisCalendar = Calendar.getInstance().apply { time = this@isInTheSameDay }
            val thatCalendar = Calendar.getInstance().apply { time = that }
            return thisCalendar.get(Calendar.DAY_OF_YEAR) == thatCalendar.get(Calendar.DAY_OF_YEAR) &&
                thisCalendar.get(Calendar.YEAR) == thatCalendar.get(Calendar.YEAR)
        }
    }
}
