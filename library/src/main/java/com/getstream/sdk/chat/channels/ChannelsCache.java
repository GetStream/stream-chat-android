package com.getstream.sdk.chat.channels;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.request.QueryChannelsRequest;
import com.getstream.sdk.chat.utils.Subscription;

import java.util.List;

public interface ChannelsCache {
    Channel getChannel(String channelId);

    List<Channel> getChannels(QueryChannelsRequest query);

    void store(Channel channel);

    void store(QueryChannelsRequest query, List<Channel> channels);
}
