package io.getstream.chat.android.client.cache.hash

internal data class GetRepliesHash(
    val messageId: String,
    val firstId: String?,
    val limit: Int,
)
