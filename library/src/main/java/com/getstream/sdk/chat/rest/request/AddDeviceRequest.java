package com.getstream.sdk.chat.rest.request;

import com.getstream.sdk.chat.utils.Global;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AddDeviceRequest {
    @SerializedName("id")
    @Expose
    String id;

    @SerializedName("user_id")
    @Expose
    String user_id;

    @SerializedName("push_provider")
    @Expose
    String push_provider;

    public AddDeviceRequest(String deviceId){
        this.id = deviceId;
        this.user_id = Global.client.user.getId();
        this.push_provider = "firebase";
    }
}
