package io.getstream.chat.android.offline.repository.database.converter.internal

import androidx.room.TypeConverter
import com.squareup.moshi.adapter
import io.getstream.chat.android.offline.repository.domain.message.internal.MessageSyncContentEntity

internal class MessageSyncDescriptionConverter {

    @OptIn(ExperimentalStdlibApi::class)
    private val entityMapAdapter = moshi.adapter<MessageSyncContentEntity>()

    @TypeConverter
    fun stringToSyncDescription(data: String?): MessageSyncContentEntity? {
        return data?.let {
            entityMapAdapter.fromJson(it)
        }
    }

    @TypeConverter
    fun messageSyncDescriptionToString(syncStatus: MessageSyncContentEntity?): String? {
        return syncStatus?.let {
            entityMapAdapter.toJson(it)
        }
    }
}