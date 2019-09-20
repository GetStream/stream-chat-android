package com.getstream.sdk.chat.storage.converter;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;

public class ExtraDataConverter {
    static Gson gson = new Gson();

    @TypeConverter
    public static HashMap<String, Object> stringToMap(String data) {
        if (data == null) {
            return new HashMap<>();
        }

        Type mapType = new TypeToken<HashMap<String, Object>>() {
        }.getType();

        return gson.fromJson(data, mapType);
    }

    @TypeConverter
    public static String mapToString(HashMap<String, Object> someObjects) {
        return gson.toJson(someObjects);
    }
}
