package com.getstream.sdk.chat.rest.apimodel.response;

import com.getstream.sdk.chat.model.User;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ChannelUserRead {
    @SerializedName("user")
    @Expose
    private User user;

    @SerializedName("last_read")
    @Expose
    private String last_read;

    public User getUser() {
        return user;
    }

    public String getLast_read() {
        return last_read;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setLast_read(String last_read) {
        this.last_read = last_read;
    }
}
