package com.getstream.sdk.chat.channels;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Watcher;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.users.UsersRepository;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class GetLastActive {

    private final Client client;
    private final UsersRepository usersRepository;

    public GetLastActive(Client client, UsersRepository usersRepository) {
        this.client = client;

        this.usersRepository = usersRepository;
    }

    public Date getLastActive(Channel channel) {

        ChannelState state = channel.getChannelState();

        Date lastActive = channel.getCreatedAt();
        if (lastActive == null) lastActive = new Date();
        if (state.getLastKnownActiveWatcher().after(lastActive)) {
            lastActive = state.getLastKnownActiveWatcher();
        }
        Message message = getLastMessageFromOtherUser(channel);
        if (message != null) {
            if (message.getCreatedAt().after(lastActive)) {
                lastActive = message.getCreatedAt();
            }
        }
        for (Watcher watcher : state.getWatchers()) {
            if (watcher.getUser() == null || watcher.getUser().getLastActive() == null)
                continue;
            if (lastActive.before(watcher.getUser().getLastActive())) {
                if (client.fromCurrentUser(watcher)) continue;
                lastActive = watcher.getUser().getLastActive();
            }
        }
        return lastActive;
    }

    private Message getLastMessageFromOtherUser(Channel channel) {

        Message lastMessage = null;
        try {
            List<Message> messages = channel.getChannelState().getMessages();
            for (int i = messages.size() - 1; i >= 0; i--) {
                Message message = messages.get(i);
                if (message.getDeletedAt() == null && !client.fromCurrentUser(message)) {
                    lastMessage = message;
                    break;
                }
            }
            Message.setStartDay(Collections.singletonList(lastMessage), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lastMessage;
    }
}
