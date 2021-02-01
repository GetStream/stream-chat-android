package io.getstream.chat.android.livedata.converter

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import io.getstream.chat.android.livedata.entity.ReactionEntity
import io.getstream.chat.android.livedata.gson

internal class ListConverter {
    @TypeConverter
    fun stringToStringList(data: String?): List<String>? {
        if (data.isNullOrEmpty() || data == "null") {
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
    fun stringListToString(someObjects: List<String>?): String? {
        return gson.toJson(someObjects)
    }

    @TypeConverter
    fun stringToReactionList(data: String?): List<ReactionEntity>? {
        if (data.isNullOrEmpty() || data == "null") {
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
