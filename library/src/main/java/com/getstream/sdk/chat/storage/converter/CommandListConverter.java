package com.getstream.sdk.chat.storage.converter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import androidx.room.TypeConverter;
import io.getstream.chat.android.client.models.Command;

public class CommandListConverter {

    static Gson gson = new Gson();

    @TypeConverter
    public static List<Command> stringToSomeObjectList(String data) {
        if (data == null) {
            return Collections.emptyList();
        }

        Type listType = new TypeToken<List<Command>>() {
        }.getType();

        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String someObjectListToString(List<Command> someObjects) {
        return gson.toJson(someObjects);
    }
}