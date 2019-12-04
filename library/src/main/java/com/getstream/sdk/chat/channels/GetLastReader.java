package com.getstream.sdk.chat.channels;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.rest.response.ChannelUserRead;

public class GetLastReader {

    private final Client client;

    public GetLastReader(Client client) {

        this.client = client;
    }

    public User getLastReader(Channel channel) {

        ChannelState channelState = channel.getChannelState();

        if (channelState.reads == null || channelState.reads.isEmpty()) return null;
        User lastReadUser = null;
        for (int i = channelState.reads.size() - 1; i >= 0; i--) {
            ChannelUserRead channelUserRead = channelState.reads.get(i);
            if (!client.fromCurrentUser(channelUserRead)) {
                lastReadUser = channelUserRead.getUser();
                break;
            }
        }
        return lastReadUser;
    }
}
