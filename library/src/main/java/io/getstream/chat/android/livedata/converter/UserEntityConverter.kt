package io.getstream.chat.android.livedata.converter

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import io.getstream.chat.android.livedata.entity.UserEntity
import io.getstream.chat.android.livedata.gson

class UserEntityConverter {
    @TypeConverter
    fun stringToUser(data: String?): UserEntity? {
        if (data.isNullOrEmpty() || data == "null") {
            return null
        }
        val t = object : TypeToken<UserEntity>() {}.type

        return gson.fromJson(data, t)
    }

    @TypeConverter
    fun userToString(someObjects: UserEntity): String {

        return gson.toJson(someObjects)
    }
}
