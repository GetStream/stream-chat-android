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

package io.getstream.chat.android.client.api2.model.requests

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.api.models.PinnedMessagesPagination
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.querysort.QuerySorter
import java.util.Date

/**
 * Request data class for the pinned messages.
 * You should provide only one of the optional parameters.
 * You can use [create] for creating the request based on [PinnedMessagesPagination].
 *
 * @param limit Max limit of messages to be fetched.
 * @param sort Parameter by which we sort the messages.
 * @param id_around Id of the message used to fetch messages around.
 * @param id_gt Id of the message used to fetch messages greater than provided, based on [sort].
 * @param id_gte Same as [id_gt] but the response will also include the message with provided id.
 * @param id_lt Id of the message used to fetch messages lower than provided, based on [sort].
 * @param id_lte Same as [id_lt] but the response will also include the message with provided id.
 * @param pinned_at_around Date of the message used to fetch messages around.
 * @param pinned_at_after Date of the message used to fetch messages sent after the date, based on [sort].
 * @param pinned_at_after_or_equal Same as [pinned_at_after] but the response will also include the message with
 * provided id.
 * @param pinned_at_before Date of the message used to fetch messages sent before the date, based on [sort].
 * @param pinned_at_before_or_equal Same as [pinned_at_before] but the response will also include the message with
 * provided id.
 */
@JsonClass(generateAdapter = true)
internal data class PinnedMessagesRequest(
    val limit: Int,
    val sort: List<Map<String, Any>>,
    val id_around: String? = null,
    val id_gt: String? = null,
    val id_gte: String? = null,
    val id_lt: String? = null,
    val id_lte: String? = null,
    val pinned_at_around: Date? = null,
    val pinned_at_after: Date? = null,
    val pinned_at_after_or_equal: Date? = null,
    val pinned_at_before: Date? = null,
    val pinned_at_before_or_equal: Date? = null,
) {

    companion object {
        /**
         * Creates [PinnedMessagesPagination] based on provided arguments.
         *
         * @param limit Max limit of messages to be fetched.
         * @param sort Parameter by which we sort the messages.
         * @param pagination Provides different options for pagination.
         *
         * @return Request data class for the pinned messages.
         */
        fun create(
            limit: Int,
            sort: QuerySorter<Message>,
            pagination: PinnedMessagesPagination,
        ): PinnedMessagesRequest {
            return when (pagination) {
                is PinnedMessagesPagination.AroundDate -> PinnedMessagesRequest(
                    limit = limit,
                    pinned_at_around = pagination.date,
                    sort = sort.toDto(),
                )
                is PinnedMessagesPagination.BeforeDate -> if (pagination.inclusive) {
                    PinnedMessagesRequest(
                        limit = limit,
                        pinned_at_before_or_equal = pagination.date,
                        sort = sort.toDto(),
                    )
                } else {
                    PinnedMessagesRequest(
                        limit = limit,
                        pinned_at_before = pagination.date,
                        sort = sort.toDto(),
                    )
                }
                is PinnedMessagesPagination.AfterDate -> if (pagination.inclusive) {
                    PinnedMessagesRequest(
                        limit = limit,
                        pinned_at_after_or_equal = pagination.date,
                        sort = sort.toDto(),
                    )
                } else {
                    PinnedMessagesRequest(
                        limit = limit,
                        pinned_at_after = pagination.date,
                        sort = sort.toDto(),
                    )
                }
                is PinnedMessagesPagination.AroundMessage -> PinnedMessagesRequest(
                    limit = limit,
                    id_around = pagination.messageId,
                    sort = sort.toDto(),
                )
                is PinnedMessagesPagination.BeforeMessage -> if (pagination.inclusive) {
                    PinnedMessagesRequest(
                        limit = limit,
                        id_lte = pagination.messageId,
                        sort = sort.toDto(),
                    )
                } else {
                    PinnedMessagesRequest(
                        limit = limit,
                        id_lt = pagination.messageId,
                        sort = sort.toDto(),
                    )
                }
                is PinnedMessagesPagination.AfterMessage -> if (pagination.inclusive) {
                    PinnedMessagesRequest(
                        limit = limit,
                        id_gte = pagination.messageId,
                        sort = sort.toDto(),
                    )
                } else {
                    PinnedMessagesRequest(
                        limit = limit,
                        id_gt = pagination.messageId,
                        sort = sort.toDto(),
                    )
                }
            }
        }
    }
}
