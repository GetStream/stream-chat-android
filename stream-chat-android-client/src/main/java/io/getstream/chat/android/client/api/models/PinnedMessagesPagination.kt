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

package io.getstream.chat.android.client.api.models

import java.util.Date

/**
 * Pagination options for getting pinned messages.
 */
public sealed class PinnedMessagesPagination {

    /**
     * Returns messages around the message with given id.
     *
     * @param messageId The id of the message used for generating result.
     */
    public data class AroundMessage(val messageId: String) : PinnedMessagesPagination()

    /**
     * Returns messages before the message with given id.
     *
     * @param messageId The id of the message used for generating result.
     * @param inclusive Whether the results should include the message with the given id.
     */
    public data class BeforeMessage(val messageId: String, val inclusive: Boolean) : PinnedMessagesPagination()

    /**
     * Returns messages after the message with given id.
     *
     * @param messageId The id of the message used for generating result.
     * @param inclusive Whether the results should include the message with the given id.
     */
    public data class AfterMessage(val messageId: String, val inclusive: Boolean) : PinnedMessagesPagination()

    /**
     * Returns messages around the date.
     *
     * @param date The date used for generating result.
     */
    public data class AroundDate(val date: Date) : PinnedMessagesPagination()

    /**
     * Returns messages before the date.
     *
     * @param date The date used for generating result.
     * @param inclusive Whether the results should include the message with the given id.
     */
    public data class BeforeDate(val date: Date, val inclusive: Boolean) : PinnedMessagesPagination()

    /**
     * Returns messages after the date.
     *
     * @param date The date used for generating result.
     * @param inclusive Whether the results should include the message with the given id.
     */
    public data class AfterDate(val date: Date, val inclusive: Boolean) : PinnedMessagesPagination()
}
