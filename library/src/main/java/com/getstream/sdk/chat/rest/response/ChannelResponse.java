package com.getstream.sdk.chat.rest.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.getstream.chat.android.client.models.Channel;

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

    public String getDuration() {
        return duration;
    }

    public Channel getChannel() {
        return channel;
    }
}
