package com.getstream.sdk.chat.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Reaction {
    @SerializedName("message_id")
    @Expose
    private String message_id;

    @SerializedName("user")
    @Expose
    private User user;

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

}
