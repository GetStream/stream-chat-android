package com.getstream.sdk.chat.rest.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class ReactionRequest {
    @SerializedName("reaction")
    @Expose
    Map<String, String> reaction;

    public ReactionRequest(String reactionType) {
        Map<String, String> map = new HashMap<>();
        map.put("type", reactionType);
        this.reaction = map;
    }
}
