package com.getstream.sdk.chat.storage.converter;

import androidx.room.TypeConverter;

import com.getstream.sdk.chat.model.Member;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MemberListConverter {
    static Gson gson = new Gson();

    @TypeConverter
    public static List<Member> stringToSomeObjectList(String data) {
        if (data == null) {
            return new ArrayList<Member>();
        }

        Type listType = new TypeToken<List<Member>>() {
        }.getType();

        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String someObjectListToString(List<Member> someObjects) {
        return gson.toJson(someObjects);
    }
}
