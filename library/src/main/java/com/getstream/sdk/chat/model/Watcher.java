package com.getstream.sdk.chat.model;

import androidx.annotation.Nullable;

import com.getstream.sdk.chat.interfaces.UserEntity;
import com.getstream.sdk.chat.rest.User;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Watcher implements UserEntity {
    @SerializedName("user")
    @Expose
    private User user;
    private Date createdAt;

    public Watcher(User user, Date createdAt) {
        this.user = user;
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (getClass() != obj.getClass()) {
            return false;
        }
        Watcher other = (Watcher) obj;
        return other.user.equals(user);
    }

    @Override
    public String getUserId() {
        return user.getId();
    }
}