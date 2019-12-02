package com.getstream.sdk.chat.channels;

import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.response.ChannelUserRead;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GetLastMessageReads {
    public synchronized List<ChannelUserRead> getLastMessageReads() {
        Message lastMessage = this.getLastMessage();
        List<ChannelUserRead> readLastMessage = new ArrayList<>();
        if (reads == null || lastMessage == null) return readLastMessage;
        Client client = this.getChannel().getClient();
        String userID = client.getUserId();
        for (ChannelUserRead r : reads) {
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
