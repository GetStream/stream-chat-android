package com.getstream.sdk.chat.rest.apimodel.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

;import java.util.List;

public class GetChannelsResponse {
    @SerializedName("channels")
    @Expose
    private List<ChannelResponse> channels;

    public List<ChannelResponse> getChannels() {
        return channels;
    }
}
