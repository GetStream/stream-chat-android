package com.getstream.sdk.chat.rest.response;

import com.getstream.sdk.chat.interfaces.UserEntity;
import com.getstream.sdk.chat.rest.User;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class ChannelUserRead implements UserEntity {
    @SerializedName("user")
    @Expose
    private User user;

    @SerializedName("last_read")
    @Expose
    private Date lastRead;

    public ChannelUserRead(User user, Date lastRead) {
        this.user = user;
        this.lastRead = lastRead;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getLastRead() {
        return lastRead;
    }

    public void setLastRead(Date last_read) {
        this.lastRead = last_read;
    }

    @Override
    public String getUserId() {
        return user.getId();
    }
}
