package com.getstream.sdk.chat.model;

import com.getstream.sdk.chat.rest.User;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Flag {
    @SerializedName("user")
    @Expose
    User user;

    @SerializedName("target_user")
    @Expose
    User target_user;

    @SerializedName("target_message_id")
    @Expose
    String target_message_id;

    @SerializedName("created_at")
    @Expose
    String created_at;

    @SerializedName("updated_at")
    @Expose
    String updated_at;

    @SerializedName("reviewed_at")
    @Expose
    String reviewed_at;

    @SerializedName("reviewed_by")
    @Expose
    String reviewed_by;

    @SerializedName("approved_at")
    @Expose
    String approved_at;

    @SerializedName("rejected_at")
    @Expose
    String rejected_at;

    @SerializedName("created_by_automod")
    @Expose
    boolean created_by_automod;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getTarget_user() {
        return target_user;
    }

    public void setTarget_user(User target_user) {
        this.target_user = target_user;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getReviewed_at() {
        return reviewed_at;
    }

    public void setReviewed_at(String reviewed_at) {
        this.reviewed_at = reviewed_at;
    }

    public String getReviewed_by() {
        return reviewed_by;
    }

    public void setReviewed_by(String reviewed_by) {
        this.reviewed_by = reviewed_by;
    }

    public String getApproved_at() {
        return approved_at;
    }

    public void setApproved_at(String approved_at) {
        this.approved_at = approved_at;
    }

    public String getRejected_at() {
        return rejected_at;
    }

    public void setRejected_at(String rejected_at) {
        this.rejected_at = rejected_at;
    }

    public boolean isCreated_by_automod() {
        return created_by_automod;
    }

    public void setCreated_by_automod(boolean created_by_automod) {
        this.created_by_automod = created_by_automod;
    }
}
