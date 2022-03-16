package io.getstream.chat.android.offline.sync.internal

import java.util.Date

internal data class SyncState(
    val userId: String,
    val activeChannelIds: List<String> = emptyList(),
    val lastSyncedAt: Date? = null,
    val markedAllReadAt: Date? = null,
)
