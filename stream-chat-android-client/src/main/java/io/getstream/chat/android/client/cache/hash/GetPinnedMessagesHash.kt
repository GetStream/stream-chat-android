package io.getstream.chat.android.client.cache.hash

import io.getstream.chat.android.client.api.models.PinnedMessagesPagination
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Message

internal data class GetPinnedMessagesHash(
    val channelType: String,
    val channelId: String,
    val limit: Int,
    val sort: QuerySort<Message>,
    val pagination: PinnedMessagesPagination,
)
