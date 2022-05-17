package io.getstream.chat.android.client.api2.optimisation.hash

internal data class GetReactionsHash(
    val messageId: String,
    val offset: Int,
    val limit: Int,
)
