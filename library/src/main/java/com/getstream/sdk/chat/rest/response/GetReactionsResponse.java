package com.getstream.sdk.chat.rest.response;

import com.getstream.sdk.chat.model.Reaction;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


public class GetReactionsResponse {

    @SerializedName("reactions")
    @Expose
    private List<Reaction> reactions;

    public List<Reaction> getReactions() {
        return reactions;
    }
}
