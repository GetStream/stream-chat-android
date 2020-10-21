package io.getstream.chat.android.client.api.models

import java.util.Date

internal data class GetSyncHistory(
    val channel_cids: List<String>,
    val last_sync_at: Date
)
