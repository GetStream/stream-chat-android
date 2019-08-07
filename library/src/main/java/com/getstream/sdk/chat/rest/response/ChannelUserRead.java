package com.getstream.sdk.chat.rest.response;

import com.getstream.sdk.chat.rest.User;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class ChannelUserRead {
    @SerializedName("user")
    @Expose
    private User user;

    @SerializedName("last_read")
    @Expose
    private Date last_read;

    public User getUser() {
        return user;
    }

    public Date getLast_read() {
        return last_read;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setLast_read(Date last_read) {
        this.last_read = last_read;
    }
}
