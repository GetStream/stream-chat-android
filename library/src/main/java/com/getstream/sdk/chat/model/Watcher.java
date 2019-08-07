package com.getstream.sdk.chat.model;

import com.getstream.sdk.chat.rest.User;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Watcher {
    public User getUser() {
        return user;
    }

    @SerializedName("user")
    @Expose
    private User user;

}