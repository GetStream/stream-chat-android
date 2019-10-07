package com.getstream.sdk.chat.rest.response;

import com.getstream.sdk.chat.model.Channel;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/*
 * Created by Anton Bevza on 2019-10-03.
 */
public class ChannelResponse {

    @SerializedName("duration")
    @Expose
    private String duration;

    @SerializedName("channel")
    @Expose
    private Channel channel;

    public ChannelResponse(String duration, Channel channel) {
        this.duration = duration;
        this.channel = channel;
    }

    public String getDuration() {
        return duration;
    }

    public Channel getChannel() {
        return channel;
    }
}
