package com.getstream.sdk.chat.livedata.converter

import android.util.ArrayMap
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Reaction

class AttachmentListConverter {
    var gson = Gson()

    // TODO find a nicer way to handle these conversions

    @TypeConverter
    fun stringToStringList(data: String?): List<String>? {
        if (data == null) {
            return emptyList<String>()
        }
        val listType = object :
            TypeToken<List<String?>?>() {}.type
        return gson.fromJson(
            data,
            listType
        )
    }

    @TypeConverter
    fun memberListToString(someObjects: List<Member>?): String? {
        return gson.toJson(someObjects)
    }

    @TypeConverter
    fun stringToMemberList(data: String?): List<Member>? {
        if (data == null) {
            return emptyList<Member>()
        }
        val listType = object :
            TypeToken<List<Member?>?>() {}.type
        return gson.fromJson(
            data,
            listType
        )
    }

    @TypeConverter
    fun readListToString(someObjects: List<ChannelUserRead>?): String? {
        return gson.toJson(someObjects)
    }

    @TypeConverter
    fun stringToReadList(data: String?): List<ChannelUserRead>? {
        if (data == null) {
            return emptyList<ChannelUserRead>()
        }
        val listType = object :
            TypeToken<List<ChannelUserRead?>?>() {}.type
        return gson.fromJson(
            data,
            listType
        )
    }

    @TypeConverter
    fun stringListToString(someObjects: List<String>?): String? {
        return gson.toJson(someObjects)
    }

    @TypeConverter
    fun stringToSomeObjectList(data: String?): List<Attachment> {
        if (data == null) {
            return emptyList<Attachment>()
        }
        val listType = object :
            TypeToken<List<Attachment?>?>() {}.type
        return gson.fromJson<List<Attachment>>(
            data,
            listType
        )
    }

    @TypeConverter
    fun someObjectListToString(someObjects: List<Attachment?>?): String {
        return gson.toJson(someObjects)
    }

    @TypeConverter
    fun stringToMap(data: String?): Map<String?, Int?>? {
        if (data == null) {
            return ArrayMap()
        }
        val mapType = object :
            TypeToken<Map<String?, Int?>?>() {}.type
        return gson.fromJson(
            data,
            mapType
        )
    }

    @TypeConverter
    fun mapToString(someObjects: Map<String?, Int?>?): String? {
        return gson.toJson(
            someObjects
        )
    }

    @TypeConverter
    fun stringToStringMap(data: String?): Map<String, String>? {
        if (data == null) {
            return ArrayMap()
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

    @TypeConverter
    fun reactionListFromString(data: String?): List<Reaction?>? {
        if (data == null) {
            return emptyList<Reaction>()
        }
        val listType = object :
            TypeToken<List<Reaction?>?>() {}.type
        return gson.fromJson(
            data,
            listType
        )
    }

    @TypeConverter
    fun reactionListToString(someObjects: List<Reaction>?): String? {
        return gson.toJson(
            someObjects
        )
    }
}