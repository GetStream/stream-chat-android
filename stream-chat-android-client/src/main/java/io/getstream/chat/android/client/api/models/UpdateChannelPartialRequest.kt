package io.getstream.chat.android.client.api.models

internal data class UpdateChannelPartialRequest(
    val set: Map<String, Any>,
    val unset: List<String>,
)
