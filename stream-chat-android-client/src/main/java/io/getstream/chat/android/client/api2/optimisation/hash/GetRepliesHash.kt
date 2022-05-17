package io.getstream.chat.android.client.api2.optimisation.hash

internal data class GetRepliesHash(
    val messageId: String,
    val firstId: String?,
    val limit: Int,
)
