package com.getstream.sdk.chat.model.channel;

import com.getstream.sdk.chat.model.User;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Member {
    @SerializedName("user")
    @Expose
    private User user;

    @SerializedName("role")
    @Expose
    private String role;

    @SerializedName("created_at")
    @Expose
    private String created_at;

    @SerializedName("updated_at")
    @Expose
    private String updated_at;

    public User getUser() {
        return user;
    }

    public String getRole() {
        return role;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }
}
