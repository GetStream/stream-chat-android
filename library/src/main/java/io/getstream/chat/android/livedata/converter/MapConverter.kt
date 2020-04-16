package io.getstream.chat.android.livedata.converter

import android.util.ArrayMap
import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import io.getstream.chat.android.livedata.entity.ChannelUserReadEntity
import io.getstream.chat.android.livedata.gson

class MapConverter {
    @TypeConverter
    fun readMapToString(someObjects: MutableMap<String, ChannelUserReadEntity>?): String {
        return gson.toJson(someObjects)
    }

    @TypeConverter
    fun stringToReadMap(data: String?): MutableMap<String, ChannelUserReadEntity> {
        if (data.isNullOrEmpty() || data=="null") {
            return mutableMapOf()
        }
        val listType = object :
            TypeToken<MutableMap<String, ChannelUserReadEntity>>() {}.type
        return gson.fromJson(
            data,
            listType
        )
    }


    @TypeConverter
    fun stringToMap(data: String?): Map<String, Int> {
        if (data.isNullOrEmpty() || data=="null") {
            return mutableMapOf()
        }
        val mapType = object :
            TypeToken<Map<String, Int>?>() {}.type
        return gson.fromJson(
            data,
            mapType
        )
    }

    @TypeConverter
    fun mapToString(someObjects: Map<String, Int>?): String? {
        return gson.toJson(
            someObjects
        )
    }

    @TypeConverter
    fun stringToStringMap(data: String?): Map<String, String>? {
        if (data.isNullOrEmpty() || data=="null") {
            return mutableMapOf()
        }
        val mapType = object :
            TypeToken<Map<String, String>?>() {}.type
        return gson.fromJson(
            data,
            mapType
        )
    }

    @TypeConverter
    fun stringMapToString(someObjects: Map<String, String>?): String? {
        return gson.toJson(
            someObjects
        )
    }

}