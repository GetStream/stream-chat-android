package com.getstream.sdk.chat.model;

import com.getstream.sdk.chat.rest.User;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * A member
 */
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

    @SerializedName("invited")
    @Expose
    private boolean invited;

    @SerializedName("invite_accepted_at")
    @Expose
    private String invite_accepted_at;


    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public boolean isInvited() {
        return invited;
    }

    public void setInvited(boolean invited) {
        this.invited = invited;
    }

    public String getInvite_accepted_at() {
        return invite_accepted_at;
    }

    public void setInvite_accepted_at(String invite_accepted_at) {
        this.invite_accepted_at = invite_accepted_at;
    }
}
