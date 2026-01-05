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

package io.getstream.chat.android.ui.common.state.messages.list

/**
 * Intended to be used for regulating the visibility of a single message footer visibility.
 */
public sealed class MessageFooterVisibility {

    /**
     * The message footer will never be visible.
     */
    public object Never : MessageFooterVisibility() {
        override fun toString(): String = "Never"
    }

    /**
     * The message footer will only be visible to items that are last in group.
     */
    public object LastInGroup : MessageFooterVisibility() {
        override fun toString(): String = "LastInGroup"
    }

    /**
     * The message footer will be visible to items that are sent inside a specified time duration.
     *
     * @param timeDifferenceMillis Time duration after which we show the message footer.
     */
    public data class WithTimeDifference(
        val timeDifferenceMillis: Long = DEFAULT_FOOTER_TIME_DIFF_MILLIS,
    ) : MessageFooterVisibility()

    /**
     * The message footer will be visible for every message.
     */
    public object Always : MessageFooterVisibility() {
        override fun toString(): String = "Always"
    }
}

/**
 * The default time difference after which the footer is shown.
 */
private const val DEFAULT_FOOTER_TIME_DIFF_MILLIS: Long = 60 * 1000L
