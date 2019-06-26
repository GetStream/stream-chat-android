package com.getstream.sdk.chat.rest.apimodel.response;

import com.getstream.sdk.chat.model.User;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetUsersResponse {
    @SerializedName("users")
    @Expose
    private List<User> users;

    public List<User> getUsers() {
        return users;
    }
}
