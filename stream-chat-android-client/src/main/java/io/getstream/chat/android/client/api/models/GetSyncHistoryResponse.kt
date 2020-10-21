package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.events.ChatEvent

internal data class GetSyncHistoryResponse(
    val events: List<ChatEvent>
)
