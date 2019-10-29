package com.getstream.sdk.chat.rest.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/*
 * Created by Anton Bevza on 2019-10-08.
 */
@SuppressWarnings("FieldCanBeLocal")
public class AddMembersRequest {

    @SerializedName("add_members")
    @NotNull
    @Expose
    private List<String> members;

    public AddMembersRequest(@NotNull List<String> members) {
        this.members = members;
    }

    @NotNull
    public List<String> getMembers() {
        return members;
    }
}
