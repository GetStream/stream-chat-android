package com.getstream.sdk.chat.rest.response;

import io.getstream.chat.android.client.models.User;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class QueryUserListResponse {
    @SerializedName("users")
    @Expose
    private List<User> users;

    public List<User> getUsers() {
        return users;
    }
}
