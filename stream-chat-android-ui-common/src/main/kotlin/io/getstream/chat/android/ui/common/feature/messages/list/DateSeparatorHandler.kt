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

import io.getstream.chat.android.client.extensions.internal.NEVER
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.utils.extensions.getCreatedAtOrThrow

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

        /**
         * @param separatorTimeMillis Time difference between two message after which we add the date separator.
         *
         * @return The default normal list date separator handler.
         */
        public fun getDefaultDateSeparatorHandler(separatorTimeMillis: Long = DateSeparatorDefaultHourThreshold):
            DateSeparatorHandler = DateSeparatorHandler { previousMessage, message ->
            if (previousMessage == null) {
                true
            } else {
                shouldAddDateSeparator(previousMessage, message, separatorTimeMillis)
            }
        }

        /**
         * @param separatorTimeMillis Time difference between two message after which we add the date separator.
         *
         * @return The default thread date separator handler.
         */
        public fun getDefaultThreadDateSeparatorHandler(separatorTimeMillis: Long = DateSeparatorDefaultHourThreshold):
            DateSeparatorHandler = DateSeparatorHandler { previousMessage, message ->
            if (previousMessage == null) {
                false
            } else {
                shouldAddDateSeparator(previousMessage, message, separatorTimeMillis)
            }
        }

        /**
         * @param previousMessage The [Message] before the one we are currently evaluating.
         * @param message The [Message] before which we want to add a date separator or not.
         *
         * @return Whether to add the date separator or not depending on the time difference.
         */
        private fun shouldAddDateSeparator(
            previousMessage: Message?,
            message: Message,
            separatorTimeMillis: Long,
        ): Boolean {
            return (message.getCreatedAtOrThrow().time - (previousMessage?.getCreatedAtOrThrow()?.time ?: NEVER.time)) >
                separatorTimeMillis
        }

        /**
         * The default threshold for showing date separators. If the message difference in millis is equal to this
         * number, then we show a separator, if it's enabled in the list.
         */
        private const val DateSeparatorDefaultHourThreshold: Long = 4 * 60 * 60 * 1000
    }
}
