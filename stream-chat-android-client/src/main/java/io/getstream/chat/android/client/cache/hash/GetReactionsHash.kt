package io.getstream.chat.android.client.cache.hash

internal data class GetReactionsHash(
    val messageId: String,
    val offset: Int,
    val limit: Int,
)
