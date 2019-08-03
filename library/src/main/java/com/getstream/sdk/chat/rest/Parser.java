package com.getstream.sdk.chat.rest;

import com.getstream.sdk.chat.model.Event;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.json.JSONObject;

public class Parser {

    public static Event parseEvent(JSONObject json){
        JsonParser parser = new JsonParser();
        JsonElement mJson =  parser.parse(json.toString());
        Gson gson = new Gson();

        return gson.fromJson(mJson, Event.class);
    }
}
