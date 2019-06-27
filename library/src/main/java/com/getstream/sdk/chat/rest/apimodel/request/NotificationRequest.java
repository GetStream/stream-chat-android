package com.getstream.sdk.chat.rest.apimodel.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class NotificationRequest {
    @SerializedName("notification")
    @Expose
    Map<String,Object> notification;

    @SerializedName("to")
    @Expose
    String to;

    @SerializedName("data")
    @Expose
    Map<String,Object> data;

    @SerializedName("watch")
    @Expose
    boolean watch;

    public NotificationRequest(String title, String body, String to, Map<String,Object>data){
        notification = new HashMap<>();
        notification.put("title",title);
        notification.put("body",body);
        notification.put("badge","1");
        notification.put("sound","default");
        this.to = to;
        this.data = data;
    }
}
