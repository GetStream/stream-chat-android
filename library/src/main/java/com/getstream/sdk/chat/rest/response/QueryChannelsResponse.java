package com.getstream.sdk.chat.rest.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

;import java.util.List;

public class QueryChannelsResponse {
    @SerializedName("channels")
    @Expose
    private List<ChannelResponse> channels;

    public List<ChannelResponse> getChannels() {
        return channels;
    }
}
