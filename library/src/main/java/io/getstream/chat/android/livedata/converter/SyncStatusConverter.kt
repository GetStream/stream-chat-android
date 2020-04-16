package io.getstream.chat.android.livedata.converter

import androidx.room.TypeConverter
import io.getstream.chat.android.client.utils.SyncStatus

class SyncStatusConverter {
    @TypeConverter
    fun stringToSyncStatus(data: Int): SyncStatus {
        val st = SyncStatus.fromInt(data)!!
        return st
    }

    @TypeConverter
    fun syncStatusToString(syncStatus: SyncStatus): Int {
        return syncStatus.status
    }
}

