package com.getstream.sdk.chat.rest.codecs;

import com.getstream.sdk.chat.enums.FilterObject;
import com.getstream.sdk.chat.enums.FilterObjectAdapter;
import com.getstream.sdk.chat.enums.QuerySort;
import com.getstream.sdk.chat.enums.QuerySortAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonConverter {
    private static Gson gson;

    public static Gson Gson(){
        if (gson == null) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            gsonBuilder.registerTypeAdapter(FilterObject.class, new FilterObjectAdapter());
            gsonBuilder.registerTypeAdapter(QuerySort.class, new QuerySortAdapter());
            gson = gsonBuilder.create();
        }
        return gson;
    }
}
