package io.getstream.chat.android.livedata.converter

import android.util.ArrayMap
import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.entity.ChannelUserReadEntity
import io.getstream.chat.android.livedata.entity.MemberEntity
import io.getstream.chat.android.livedata.entity.ReactionEntity
import io.getstream.chat.android.livedata.gson

class Converter {
    @TypeConverter
    fun stringToStringList(data: String?): List<String>? {
        if (data == null) {
            return emptyList()
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
    fun stringToSyncStatus(data: String): SyncStatus {
        return SyncStatus.valueOf(data)
    }

    @TypeConverter
    fun syncStatusToString(syncStatus: SyncStatus): String? {
        return syncStatus.toString()
    }

    @TypeConverter
    fun readListToString(someObjects: MutableMap<String, ChannelUserReadEntity>?): String? {
        return gson.toJson(someObjects)
    }

    @TypeConverter
    fun stringToReadList(data: String?): MutableMap<String, ChannelUserReadEntity>? {
        if (data == null) {
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
            return emptyList()
        }
        val listType = object :
            TypeToken<List<Attachment?>?>() {}.type
        return gson.fromJson(
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