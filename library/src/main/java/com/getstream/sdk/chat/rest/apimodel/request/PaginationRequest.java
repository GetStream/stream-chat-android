package com.getstream.sdk.chat.rest.apimodel.request;

import com.getstream.sdk.chat.model.channel.Channel;
import com.getstream.sdk.chat.model.enums.Pagination;
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class PaginationRequest {
    @SerializedName("messages")
    @Expose
    Map<String, Object> messages;

    @SerializedName("data")
    @Expose
    Map<String, Object> channel;

    @SerializedName("state")
    @Expose
    boolean state;

    @SerializedName("watch")
    @Expose
    boolean watch;

    public PaginationRequest(int limit, String messageId, Channel channel) {
        Map<String, Object> map = new HashMap<>();
        map.put("limit", limit);
        map.put(Pagination.lessThan.get(), messageId);
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
        this.channel.remove("additionalFields");

        this.state = true;
        this.watch = true;
    }
}
