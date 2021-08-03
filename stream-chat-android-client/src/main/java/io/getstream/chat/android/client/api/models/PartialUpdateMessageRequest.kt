package io.getstream.chat.android.client.api.models

internal data class PartialUpdateMessageRequest(
    val set: Map<String, Any>,
    val unset: List<String>,
)
