package io.getstream.chat.android.client.api.models

data class GetSyncHistoryResponse(
    val channels:Map<String, ChannelResponse>
)