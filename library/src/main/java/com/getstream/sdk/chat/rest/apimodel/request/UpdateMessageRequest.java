package com.getstream.sdk.chat.rest.apimodel.request;

import com.getstream.sdk.chat.model.Message;
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class UpdateMessageRequest {
    @SerializedName("message")
    @Expose
    Map<String,Object> message;

    public UpdateMessageRequest(Message message){
        Gson gson = new Gson();
        String json = gson.toJson(message);
        Map<String, Object> map = new HashMap<>();
        this.message = (Map<String, Object>)gson.fromJson(json, map.getClass());
        this.message.remove("id");
        this.message.remove("latest_reactions");
        this.message.remove("own_reactions");
        this.message.remove("reaction_counts");
        this.message.remove("reply_count");
        this.message.remove("type");
        this.message.remove("user");
        this.message.remove("created_at");
        this.message.remove("updated_at");
        this.message.remove("html");
        this.message.remove("command");
        // Custom Keys
        this.message.remove("costomFields");
        this.message.remove("isStartDay");
        this.message.remove("isYesterday");
        this.message.remove("isToday");
        this.message.remove("created");
        this.message.remove("edited");
        this.message.remove("deleted");
        this.message.remove("date");
        this.message.remove("time");
        this.message.remove("isIncoming");
        this.message.remove("isDelivered");
    }
}
