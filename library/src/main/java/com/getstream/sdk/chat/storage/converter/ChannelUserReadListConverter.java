package com.getstream.sdk.chat.storage.converter;

import androidx.room.TypeConverter;

import com.getstream.sdk.chat.rest.response.ChannelUserRead;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ChannelUserReadListConverter {
    static Gson gson = new Gson();

    @TypeConverter
    public static List<ChannelUserRead> stringToSomeObjectList(String data) {
        if (data == null) {
            return new ArrayList<ChannelUserRead>();
        }

        Type listType = new TypeToken<List<ChannelUserRead>>() {
        }.getType();

        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String someObjectListToString(List<ChannelUserRead> someObjects) {
        return gson.toJson(someObjects);
    }
}
