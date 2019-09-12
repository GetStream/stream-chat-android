package com.getstream.sdk.chat.storage.converter;

import androidx.room.TypeConverter;

import com.getstream.sdk.chat.enums.QuerySort;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class QuerySortConverter {
    static Gson gson = new Gson();

    @TypeConverter
    public static QuerySort stringToObject(String data) {
        if (data == null) {
            return new QuerySort();
        }

        Type listType = new TypeToken<QuerySort>() {
        }.getType();

        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String objectToString(QuerySort someObjects) {
        return gson.toJson(someObjects);
    }
}
