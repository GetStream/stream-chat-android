package com.getstream.sdk.chat.rest.request;

import com.google.gson.annotations.SerializedName;

/*
 * Created by Anton Bevza on 2019-10-01.
 */
public class HideChannelRequest {
    @SerializedName("user_id")
    private String userId;

    public HideChannelRequest(String userId) {
        this.userId = userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
