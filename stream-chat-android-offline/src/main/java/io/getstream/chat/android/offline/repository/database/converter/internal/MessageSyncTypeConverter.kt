package io.getstream.chat.android.offline.repository.database.converter.internal

import androidx.room.TypeConverter
import io.getstream.chat.android.client.models.MessageSyncType

internal class MessageSyncTypeConverter {
    @TypeConverter
    fun stringToMessageSyncType(data: Int): MessageSyncType {
        return MessageSyncType.fromInt(data) ?: error("unexpected MessageSyncType: $data")
    }

    @TypeConverter
    fun messageSyncTypeToString(type: MessageSyncType): Int {
        return type.type
    }
}