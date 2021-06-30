package io.getstream.chat.android.client.offline.model

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import java.util.Date

@InternalStreamChatApi
public data class SyncState(
    val userId: String,
    val activeChannelIds: List<String> = emptyList(),
    val activeQueryIds: List<String> = emptyList(),
    val lastSyncedAt: Date? = null,
    val markedAllReadAt: Date? = null,
)
