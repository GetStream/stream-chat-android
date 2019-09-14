package com.getstream.sdk.chat.storage.converter;

import android.util.ArrayMap;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

public class CommandInfoConverter {
    static Gson gson = new Gson();

    @TypeConverter
    public static Map<String, String> stringToMap(String data) {
        if (data == null) {
            return new ArrayMap<>();
        }

        Type mapType = new TypeToken<Map<String, String>>() {
        }.getType();

        return gson.fromJson(data, mapType);
    }

    @TypeConverter
    public static String mapToString(Map<String, String> someObjects) {
        return gson.toJson(someObjects);
    }
}
