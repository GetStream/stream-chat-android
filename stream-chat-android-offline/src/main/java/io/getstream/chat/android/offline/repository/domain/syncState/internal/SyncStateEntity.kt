package io.getstream.chat.android.offline.repository.domain.syncState.internal

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "stream_sync_state")
internal data class SyncStateEntity(
    @PrimaryKey var userId: String,
    var activeChannelIds: List<String> = mutableListOf(),
    var lastSyncedAt: Date? = null,
    var markedAllReadAt: Date? = null,
)
