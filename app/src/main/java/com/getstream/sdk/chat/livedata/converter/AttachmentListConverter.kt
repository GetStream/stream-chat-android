package com.getstream.sdk.chat.livedata.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.getstream.chat.android.client.models.Attachment

class AttachmentListConverter {
    var gson = Gson()

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
}