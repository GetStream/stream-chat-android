package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.models.Message

public data class SearchMessagesRequest @JvmOverloads constructor(
    /**
     * Pagination offset. Cannot be used with sort or next.
     */
    val offset: Int?,

    /**
     * Number of messages to return
     */
    val limit: Int?,

    /**
     * Channel filter conditions
     */
    val channelFilter: FilterObject,

    /**
     * Message filter conditions
     */
    val messageFilter: FilterObject,

    /**
     * Pagination parameter. Cannot be used with non-zero offset.
     */
    val next: String? = null,

    /**
     * Sort parameters. Cannot be used with non-zero offset
     */
    val querySort: QuerySort<Message>? = null,
) {
    val sort: List<Map<String, Any>>? = querySort?.toDto()
}
