package com.getstream.sdk.chat.storage.converter;

import androidx.room.TypeConverter;

import com.getstream.sdk.chat.enums.FilterObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class FilterObjectConverter {
    static Gson gson = new Gson();

    @TypeConverter
    public static FilterObject stringToObject(String data) {
        if (data == null) {
            return new FilterObject();
        }

        Type listType = new TypeToken<FilterObject>() {
        }.getType();

        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String objectToString(FilterObject someObjects) {
        return gson.toJson(someObjects);
    }
}


