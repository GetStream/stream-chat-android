package com.getstream.sdk.chat.channels;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.rest.response.ChannelUserRead;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GetLastMessageReads {

    private final Client client;

    public GetLastMessageReads(Client client) {

        this.client = client;
    }

    public synchronized List<ChannelUserRead> getLastMessageReads(Channel channel) {

        ChannelState channelState = channel.getChannelState();
        Message lastMessage = channelState.getLastMessage();

        List<ChannelUserRead> readLastMessage = new ArrayList<>();
        if (channelState.reads == null || lastMessage == null) return readLastMessage;
        String userID = client.getUserId();
        for (ChannelUserRead r : channelState.reads) {
            if (r.getUserId().equals(userID))
                continue;
            if (r.getLastRead().compareTo(lastMessage.getCreatedAt()) > -1) {
                readLastMessage.add(r);
            }
        }

        // sort the reads
        Collections.sort(readLastMessage, (ChannelUserRead o1, ChannelUserRead o2) -> o1.getLastRead().compareTo(o2.getLastRead()));
        return readLastMessage;
    }
}
