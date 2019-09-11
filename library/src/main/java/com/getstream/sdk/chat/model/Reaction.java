package com.getstream.sdk.chat.model;

import androidx.room.ColumnInfo;

import com.getstream.sdk.chat.rest.User;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Reaction {
    @SerializedName("message_id")
    @Expose
    private String message_id;

    public void setUser(User user) {
        this.user = user;
    }

    @SerializedName("user")
    @Expose
    private User user;

    @ColumnInfo(name="user_id")
    private String userID;

    @SerializedName("type")
    @Expose
    private String type;

    public String getMessage_id() {
        return message_id;
    }

    public User getUser() {
        return user;
    }

    public String getType() {
        return type;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
