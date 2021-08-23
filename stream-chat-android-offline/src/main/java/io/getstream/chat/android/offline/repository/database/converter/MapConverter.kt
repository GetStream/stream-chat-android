package io.getstream.chat.android.offline.repository.database.converter

import androidx.room.TypeConverter
import com.squareup.moshi.adapter
import io.getstream.chat.android.offline.repository.domain.channel.member.MemberEntity
import io.getstream.chat.android.offline.repository.domain.channel.userread.ChannelUserReadEntity

internal class MapConverter {
    @OptIn(ExperimentalStdlibApi::class)
    private val stringMapAdapter = moshi.adapter<Map<String, String>>()
    @OptIn(ExperimentalStdlibApi::class)
    private val intMapAdapter = moshi.adapter<Map<String, Int>>()
    @OptIn(ExperimentalStdlibApi::class)
    private val channelUserReadMapAdapter = moshi.adapter<Map<String, ChannelUserReadEntity>>()
    @OptIn(ExperimentalStdlibApi::class)
    private val memberEntityMapAdapter = moshi.adapter<Map<String, MemberEntity>>()

    @TypeConverter
    fun readMapToString(someObjects: Map<String, ChannelUserReadEntity>?): String {
        return channelUserReadMapAdapter.toJson(someObjects)
    }

    @TypeConverter
    fun stringToReadMap(data: String?): Map<String, ChannelUserReadEntity>? {
        if (data.isNullOrEmpty() || data == "null") {
            return mutableMapOf()
        }
        return channelUserReadMapAdapter.fromJson(data)
    }

    @TypeConverter
    fun memberMapToString(someObjects: Map<String, MemberEntity>?): String? {
        return memberEntityMapAdapter.toJson(someObjects)
    }

    @TypeConverter
    fun stringToMemberMap(data: String?): Map<String, MemberEntity>? {
        if (data.isNullOrEmpty() || data == "null") {
            return emptyMap()
        }
        return memberEntityMapAdapter.fromJson(data)
    }

    @TypeConverter
    fun stringToMap(data: String?): Map<String, Int>? {
        if (data.isNullOrEmpty() || data == "null") {
            return mutableMapOf()
        }
        return intMapAdapter.fromJson(data)
    }

    @TypeConverter
    fun mapToString(someObjects: Map<String, Int>?): String? {
        return intMapAdapter.toJson(someObjects)
    }

    @TypeConverter
    fun stringToStringMap(data: String?): Map<String, String>? {
        if (data.isNullOrEmpty() || data == "null") {
            return mutableMapOf()
        }
        return stringMapAdapter.fromJson(data)
    }

    @TypeConverter
    fun stringMapToString(someObjects: Map<String, String>?): String? {
        return stringMapAdapter.toJson(someObjects)
    }
}
