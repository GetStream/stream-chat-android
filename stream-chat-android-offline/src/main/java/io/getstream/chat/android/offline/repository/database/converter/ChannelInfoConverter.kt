package io.getstream.chat.android.offline.repository.database.converter

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import io.getstream.chat.android.client.models.ChannelInfo
import io.getstream.chat.android.offline.gson

internal class ChannelInfoConverter {
    @TypeConverter
    fun stringToChannelInfo(data: String): ChannelInfo {
        return gson.fromJson(data, object : TypeToken<ChannelInfo>() {}.type)
    }

    @TypeConverter
    fun channelInfoToString(channelInfo: ChannelInfo): String {
        return gson.toJson(channelInfo)
    }
}
