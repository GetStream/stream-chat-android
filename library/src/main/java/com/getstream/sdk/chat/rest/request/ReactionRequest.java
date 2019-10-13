package com.getstream.sdk.chat.rest.request;

import com.getstream.sdk.chat.model.Reaction;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class ReactionRequest {

    @SerializedName("reaction")
    @Expose
    private
    Map<String, Object> data;

    public ReactionRequest(Reaction reaction) {
        HashMap<String, Object> data;
        if (reaction.getExtraData() != null) {
            data = new HashMap<>(reaction.getExtraData());
        } else {
            data = new HashMap<>();
        }
        data.put("type", reaction.getType());
        this.data = data;
    }
}
