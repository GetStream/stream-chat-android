package com.getstream.sdk.chat.rest.apimodel.response;

import com.getstream.sdk.chat.model.channel.Event;;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EventResponse {
    @SerializedName("event")
    @Expose
    private Event event;

    public Event getEvent() {
        return event;
    }
}
