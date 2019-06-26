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
}
