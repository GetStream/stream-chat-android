package com.getstream.sdk.chat.storage.converter;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ChannelIdListConverter {
    static Gson gson = new Gson();

    @TypeConverter
    public static List<String> stringToObject(String data) {
        if (data == null) {
            return new ArrayList<String>();
        }

        Type listType = new TypeToken<List<String>>() {
        }.getType();

        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String objectToString(List<String> someObjects) {
        return gson.toJson(someObjects);
    }
}
