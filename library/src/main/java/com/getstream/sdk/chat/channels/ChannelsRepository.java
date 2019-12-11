package com.getstream.sdk.chat.channels;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.request.QueryChannelsRequest;
import com.getstream.sdk.chat.utils.Subscription;

import java.util.List;

public interface ChannelsRepository {
    Subscription<Channel> getChannel(String channelId);

    Subscription<List<Channel>> getChannels(QueryChannelsRequest query);

    Subscription<Channel> create(Channel channelName);
}