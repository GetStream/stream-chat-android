package com.getstream.sdk.chat.rest.codecs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonConverter {
    private static Gson gson;

    public static Gson Gson(){
        if (gson == null) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            gson = gsonBuilder.create();
        }
        return gson;
    }
}
