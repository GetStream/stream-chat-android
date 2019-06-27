package com.getstream.sdk.chat.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Device {
    @SerializedName("id")
    @Expose
    String id;

    @SerializedName("push_provider")
    @Expose
    String push_provider;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPush_provider() {
        return push_provider;
    }

    public void setPush_provider(String push_provider) {
        this.push_provider = push_provider;
    }
}
