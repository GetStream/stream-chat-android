package com.getstream.sdk.chat.rest.response;

import com.getstream.sdk.chat.model.Event;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EventResponse {
    @SerializedName("event")
    @Expose
    private Event event;

    @SerializedName("duration")
    @Expose
    private String duration;

    public Event getEvent() {
        return event;
    }
}
