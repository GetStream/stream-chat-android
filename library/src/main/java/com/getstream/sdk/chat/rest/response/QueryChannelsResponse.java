package com.getstream.sdk.chat.rest.response;

import com.getstream.sdk.chat.model.Channel;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class QueryChannelsResponse {
    @SerializedName("channels")
    @Expose
    private List<ChannelState> channels;

    public List<ChannelState> getChannelStates() {
        return channels;
    }

    public List<Channel> getChannels() {
        List<Channel> channels = new ArrayList<Channel>();
        for (ChannelState cs : getChannelStates()) {
            channels.add(cs.getChannel());
        }
        return channels;
    }

    public void setChannelStates(List<ChannelState> channels) {
        this.channels = channels;
    }
}
