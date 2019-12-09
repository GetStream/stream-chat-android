package com.getstream.sdk.chat.channels;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.utils.Subscription;

public class GetChannel {

    private final ChannelsRepository repository;

    public GetChannel(ChannelsRepository repository) {

        this.repository = repository;
    }

    public Subscription<Channel> get(String channelId) {
        return repository.getChannel(channelId);
    }
}
