package io.getstream.chat.android.client.api2.model.requests

import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
internal data class SyncHistoryRequest(
    val channel_cids: List<String>,
    val last_sync_at: Date,
)
