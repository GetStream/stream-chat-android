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

package io.getstream.chat.android.common.messagelist

import com.getstream.sdk.chat.utils.extensions.getCreatedAtOrThrow
import io.getstream.chat.android.client.models.Message

/**
 * A SAM designed to evaluate if a date separator should be added between messages.
 */
public fun interface DateSeparatorHandler {
    public fun shouldAddDateSeparator(previousMessage: Message?, message: Message): Boolean

    public companion object {
        public fun getDefaultDateSeparator(separatorTimeMillis: Long = DateSeparatorDefaultHourThreshold):
            DateSeparatorHandler = DateSeparatorHandler { previousMessage, message ->
            if (previousMessage == null) {
                true
            } else {
                val timeDifference = message.getCreatedAtOrThrow().time - previousMessage.getCreatedAtOrThrow().time
                timeDifference > separatorTimeMillis
            }
        }

        public fun getDefaultThreadDateSeparator(separatorTimeMillis: Long = DateSeparatorDefaultHourThreshold):
            DateSeparatorHandler = DateSeparatorHandler { previousMessage, message ->
            if (previousMessage == null) {
                false
            } else {
                (message.getCreatedAtOrThrow().time - previousMessage.getCreatedAtOrThrow().time) >
                    separatorTimeMillis
            }
        }

        /**
         * The default threshold for showing date separators. If the message difference in millis is equal to this
         * number, then we show a separator, if it's enabled in the list.
         */
        private const val DateSeparatorDefaultHourThreshold: Long = 4 * 60 * 60 * 1000
    }
}
