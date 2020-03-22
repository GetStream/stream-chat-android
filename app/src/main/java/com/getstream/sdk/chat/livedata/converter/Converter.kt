package com.getstream.sdk.chat.livedata.converter

import android.util.ArrayMap
import androidx.room.TypeConverter
import com.getstream.sdk.chat.livedata.SyncStatus
import com.getstream.sdk.chat.livedata.entity.ChannelUserReadEntity
import com.getstream.sdk.chat.livedata.entity.MemberEntity
import com.getstream.sdk.chat.livedata.entity.ReactionEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Config

class Converter {
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
    fun memberListToString(someObjects: List<MemberEntity>?): String? {
        return gson.toJson(someObjects)
    }

    @TypeConverter
    fun stringToMemberList(data: String?): List<MemberEntity>? {
        if (data == null) {
            return emptyList()
        }
        val listType = object :
            TypeToken<List<MemberEntity>?>() {}.type
        return gson.fromJson(
            data,
            listType
        )
    }

    @TypeConverter
    fun readListToString(someObjects: List<ChannelUserReadEntity>?): String? {
        return gson.toJson(someObjects)
    }

    @TypeConverter
    fun stringToSyncStatus(data: String): SyncStatus {
        return SyncStatus.valueOf(data)
    }

    @TypeConverter
    fun syncStatusToString(syncStatus: SyncStatus): String? {
        return syncStatus.toString()
    }


    @TypeConverter
    fun stringToReadList(data: String?): List<ChannelUserReadEntity>? {
        if (data == null) {
            return emptyList()
        }
        val listType = object :
            TypeToken<List<ChannelUserReadEntity>?>() {}.type
        return gson.fromJson(
            data,
            listType
        )
    }

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
    fun reactionListFromString(data: String?): List<ReactionEntity>? {
        if (data == null) {
            return emptyList()
        }
        val listType = object :
            TypeToken<List<ReactionEntity>?>() {}.type
        return gson.fromJson(
            data,
            listType
        )
    }

    @TypeConverter
    fun reactionListToString(someObjects: List<ReactionEntity>?): String? {
        return gson.toJson(
            someObjects
        )
    }
}