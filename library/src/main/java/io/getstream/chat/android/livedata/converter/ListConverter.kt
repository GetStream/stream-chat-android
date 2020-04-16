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
import java.util.*

class ListConverter {
    @TypeConverter
    fun stringToStringList(data: String?): List<String>? {
        if (data.isNullOrEmpty() || data=="null") {
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
    fun memberListToString(someObjects: Map<String, MemberEntity>?): String? {
        return gson.toJson(someObjects)
    }

    @TypeConverter
    fun stringToMemberList(data: String?): Map<String, MemberEntity>? {
        if (data.isNullOrEmpty() || data=="null") {
            return emptyMap()
        }
        val listType = object :
                TypeToken<Map<String, MemberEntity>>() {}.type
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
        if (data.isNullOrEmpty() || data=="null") {
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
    fun reactionListFromString(data: String?): List<ReactionEntity>? {
        if (data.isNullOrEmpty() || data=="null") {
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