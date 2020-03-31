package com.getstream.sdk.chat.rest.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import io.getstream.chat.android.client.models.User;

public class QueryUserListResponse {
    @SerializedName("users")
    @Expose
    private List<User> users;

    public List<User> getUsers() {
        return users;
    }
}
