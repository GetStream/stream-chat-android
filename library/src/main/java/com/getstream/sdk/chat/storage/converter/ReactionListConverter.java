package com.getstream.sdk.chat.storage.converter;

import androidx.room.TypeConverter;

import com.getstream.sdk.chat.model.Reaction;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class ReactionListConverter {

    static Gson gson = new Gson();

    @TypeConverter
    public static List<Reaction> stringToSomeObjectList(String data) {
        if (data == null) {
            return Collections.emptyList();
        }

        Type listType = new TypeToken<List<Reaction>>() {
        }.getType();

        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String someObjectListToString(List<Reaction> someObjects) {
        if (someObjects != null) {
            for (Reaction r : someObjects) {
                if (r.getUser() != null) {
                    r.setUserID(r.getUser().getId());
                    // dont serialize the full user object
                    r.setUser(null);
                }
            }
        }

        return gson.toJson(someObjects);
    }
}