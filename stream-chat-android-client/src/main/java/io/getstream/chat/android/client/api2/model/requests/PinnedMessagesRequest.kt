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

package io.getstream.chat.android.client.api2.model.requests

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.api.models.PinnedMessagesPagination
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Message
import java.util.Date

/**
 * Request data class for the pinned messages.
 * You should provide only one of the optional parameters.
 * You can use [create] for creating the request based on [PinnedMessagesPagination].
 *
 * @param limit Max limit of messages to be fetched.
 * @param sort Parameter by which we sort the messages.
 * @param idAround Id of the message used to fetch messages around.
 * @param idGt Id of the message used to fetch messages greater than provided, based on [sort].
 * @param idGte Same as [idGt] but the response will also include the message with provided id.
 * @param idLt Id of the message used to fetch messages lower than provided, based on [sort].
 * @param idLte Same as [idLt] but the response will also include the message with provided id.
 * @param pinnedAtAround Date of the message used to fetch messages around.
 * @param pinnedAtAfter Date of the message used to fetch messages sent after the date, based on [sort].
 * @param pinnedAtAfterOrEqual Same as [pinnedAtAfter] but the response will also include the message with
 * provided id.
 * @param pinnedAtBefore Date of the message used to fetch messages sent before the date, based on [sort].
 * @param pinnedAtBeforeOrEqual Same as [pinnedAtBefore] but the response will also include the message with
 * provided id.
 */
@JsonClass(generateAdapter = true)
internal data class PinnedMessagesRequest(
    @Json(name = "limit") val limit: Int,
    @Json(name = "sort") val sort: List<Map<String, Any>>,
    @Json(name = "id_around") val idAround: String? = null,
    @Json(name = "id_gt") val idGt: String? = null,
    @Json(name = "id_gte") val idGte: String? = null,
    @Json(name = "id_lt") val idLt: String? = null,
    @Json(name = "id_lte") val idLte: String? = null,
    @Json(name = "pinned_at_around") val pinnedAtAround: Date? = null,
    @Json(name = "pinned_at_after") val pinnedAtAfter: Date? = null,
    @Json(name = "pinned_at_after_or_equal") val pinnedAtAfterOrEqual: Date? = null,
    @Json(name = "pinned_at_before") val pinnedAtBefore: Date? = null,
    @Json(name = "pinned_at_before_or_equal") val pinnedAtBeforeOrEqual: Date? = null,
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
        fun create(limit: Int, sort: QuerySort<Message>, pagination: PinnedMessagesPagination): PinnedMessagesRequest {
            return when (pagination) {
                is PinnedMessagesPagination.AroundDate -> PinnedMessagesRequest(
                    limit = limit,
                    pinnedAtAround = pagination.date,
                    sort = sort.toDto(),
                )
                is PinnedMessagesPagination.BeforeDate -> if (pagination.inclusive) PinnedMessagesRequest(
                    limit = limit,
                    pinnedAtBeforeOrEqual = pagination.date,
                    sort = sort.toDto(),
                ) else PinnedMessagesRequest(
                    limit = limit,
                    pinnedAtBefore = pagination.date,
                    sort = sort.toDto(),
                )
                is PinnedMessagesPagination.AfterDate -> if (pagination.inclusive) PinnedMessagesRequest(
                    limit = limit,
                    pinnedAtAfterOrEqual = pagination.date,
                    sort = sort.toDto(),
                ) else PinnedMessagesRequest(
                    limit = limit,
                    pinnedAtAfter = pagination.date,
                    sort = sort.toDto(),
                )
                is PinnedMessagesPagination.AroundMessage -> PinnedMessagesRequest(
                    limit = limit,
                    idAround = pagination.messageId,
                    sort = sort.toDto(),
                )
                is PinnedMessagesPagination.BeforeMessage -> if (pagination.inclusive) PinnedMessagesRequest(
                    limit = limit,
                    idLte = pagination.messageId,
                    sort = sort.toDto(),
                ) else PinnedMessagesRequest(
                    limit = limit,
                    idLt = pagination.messageId,
                    sort = sort.toDto(),
                )
                is PinnedMessagesPagination.AfterMessage -> if (pagination.inclusive) PinnedMessagesRequest(
                    limit = limit,
                    idGte = pagination.messageId,
                    sort = sort.toDto(),
                ) else PinnedMessagesRequest(
                    limit = limit,
                    idGt = pagination.messageId,
                    sort = sort.toDto(),
                )
            }
        }
    }
}
