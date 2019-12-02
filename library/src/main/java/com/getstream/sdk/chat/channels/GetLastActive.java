package com.getstream.sdk.chat.channels;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Watcher;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.users.UsersRepository;

import java.util.Date;

public class GetLastActive {

    private final UsersRepository usersRepository;

    public GetLastActive(UsersRepository usersRepository) {

        this.usersRepository = usersRepository;
    }

    public Date getLastActive(Channel channel) {

        ChannelState state = channel.getChannelState();

        Date lastActive = channel.getCreatedAt();
        if (lastActive == null) lastActive = new Date();
        if (state.getLastKnownActiveWatcher().after(lastActive)) {
            lastActive = state.getLastKnownActiveWatcher();
        }
        Message message = state.getLastMessageFromOtherUser();
        if (message != null) {
            if (message.getCreatedAt().after(lastActive)) {
                lastActive = message.getCreatedAt();
            }
        }
        for (Watcher watcher : state.getWatchers()) {
            if (watcher.getUser() == null || watcher.getUser().getLastActive() == null)
                continue;
            if (lastActive.before(watcher.getUser().getLastActive())) {
                if (usersRepository.fromCurrentUser(watcher)) continue;
                lastActive = watcher.getUser().getLastActive();
            }
        }
        return lastActive;
    }
}
