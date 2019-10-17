package com.getstream.sdk.chat.rest.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/*
 * Created by Anton Bevza on 2019-10-17.
 */
public class SearchMessagesResponse {

    @SerializedName("results")
    @Expose
    private List<MessageResponse> results;

    public List<MessageResponse> getResults() {
        return results;
    }

}
