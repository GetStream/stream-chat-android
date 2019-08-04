package com.getstream.sdk.chat.rest.request;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.enums.Pagination;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class PaginationRequest {
    @SerializedName("messages")
    Map<String, Object> messages;

    @SerializedName("data")
    Map<String, Object> channel;

    @SerializedName("state")
    boolean state;

    @SerializedName("watch")
    boolean watch;

    public PaginationRequest(int limit, String messageId, Channel channel) {
        Map<String, Object> map = new HashMap<>();
        map.put("limit", limit);
        map.put(Pagination.LESS_THAN.get(), messageId);
        this.messages = map;

        Gson gson = new Gson();
        String json = gson.toJson(channel);
        this.channel = (Map<String, Object>) gson.fromJson(json, Map.class);

        this.channel.remove("id");
        this.channel.remove("cid");
        this.channel.remove("type");
        this.channel.remove("last_message_at");
        this.channel.remove("created_by");
        this.channel.remove("frozen");
        this.channel.remove("config");
        this.channel.remove("example");
        this.channel.remove("extraData");


        this.state = true;
        this.watch = true;
    }
}
