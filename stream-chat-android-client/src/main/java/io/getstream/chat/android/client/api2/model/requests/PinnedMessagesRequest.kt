package io.getstream.chat.android.client.api2.model.requests

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.api.models.PinnedMessagesPagination
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Message
import java.util.Date

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
        fun create(limit: Int, sort: QuerySort<Message>, pagination: PinnedMessagesPagination): PinnedMessagesRequest {
            return when (pagination) {
                is PinnedMessagesPagination.AroundDate -> PinnedMessagesRequest(
                    limit = limit,
                    pinned_at_around = pagination.date,
                    sort = sort.toDto(),
                )
                is PinnedMessagesPagination.BeforeDate -> if (pagination.inclusive) PinnedMessagesRequest(
                    limit = limit,
                    pinned_at_before_or_equal = pagination.date,
                    sort = sort.toDto(),
                ) else PinnedMessagesRequest(
                    limit = limit,
                    pinned_at_before = pagination.date,
                    sort = sort.toDto(),
                )
                is PinnedMessagesPagination.AfterDate -> if (pagination.inclusive) PinnedMessagesRequest(
                    limit = limit,
                    pinned_at_after_or_equal = pagination.date,
                    sort = sort.toDto(),
                ) else PinnedMessagesRequest(
                    limit = limit,
                    pinned_at_after = pagination.date,
                    sort = sort.toDto(),
                )
                is PinnedMessagesPagination.AroundMessage -> PinnedMessagesRequest(
                    limit = limit,
                    id_around = pagination.messageId,
                    sort = sort.toDto(),
                )
                is PinnedMessagesPagination.BeforeMessage -> if (pagination.inclusive) PinnedMessagesRequest(
                    limit = limit,
                    id_lte = pagination.messageId,
                    sort = sort.toDto(),
                ) else PinnedMessagesRequest(
                    limit = limit,
                    id_lt = pagination.messageId,
                    sort = sort.toDto(),
                )
                is PinnedMessagesPagination.AfterMessage -> if (pagination.inclusive) PinnedMessagesRequest(
                    limit = limit,
                    id_gte = pagination.messageId,
                    sort = sort.toDto(),
                ) else PinnedMessagesRequest(
                    limit = limit,
                    id_gt = pagination.messageId,
                    sort = sort.toDto(),
                )
            }
        }
    }
}
