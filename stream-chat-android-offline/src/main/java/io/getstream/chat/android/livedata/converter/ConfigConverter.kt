package io.getstream.chat.android.livedata.converter

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.livedata.gson

internal class ConfigConverter {
    @TypeConverter
    fun channelConfigToString(channelConfig: Config): String? {
        return gson.toJson(channelConfig)
    }

    @TypeConverter
    fun stringToChannelConfig(data: String?): Config? {
        val listType = object :
            TypeToken<Config>() {}.type
        return gson.fromJson<Config>(
            data,
            listType
        )
    }
}
