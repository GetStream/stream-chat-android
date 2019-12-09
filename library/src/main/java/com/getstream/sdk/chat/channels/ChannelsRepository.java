package com.getstream.sdk.chat.channels;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.utils.Subscription;

public interface ChannelsRepository {
    Subscription<Channel> getChannel(String channelId);
}