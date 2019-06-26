package com.getstream.sdk.chat.rest;

import com.getstream.sdk.chat.model.User;
import com.getstream.sdk.chat.model.channel.Event;
import com.getstream.sdk.chat.model.message.Message;
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

    public static Message parseMessage(JSONObject json){
        JSONObject json1 = json.optJSONObject("message");
        JsonParser parser = new JsonParser();
        JsonElement mJson =  parser.parse(json1.toString());
        Gson gson = new Gson();

        return gson.fromJson(mJson, Message.class);
    }

    public static User parseUser(JSONObject json){
        JSONObject json1 = json.optJSONObject("user");
        JsonParser parser = new JsonParser();
        JsonElement mJson =  parser.parse(json1.toString());
        Gson gson = new Gson();
        return gson.fromJson(mJson, User.class);
    }
}
