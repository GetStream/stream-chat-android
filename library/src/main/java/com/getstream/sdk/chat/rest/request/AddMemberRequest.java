package com.getstream.sdk.chat.rest.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AddMemberRequest {
    @SerializedName("add_members")
    @Expose
    List<String> add_members;

    public AddMemberRequest(List<String> add_members) {
        this.add_members = add_members;
    }
}
