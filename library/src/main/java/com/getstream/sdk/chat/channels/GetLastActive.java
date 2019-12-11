package com.getstream.sdk.chat.channels;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Watcher;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.users.UsersCache;
import com.getstream.sdk.chat.utils.UseCase;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class GetLastActive extends UseCase {

    private final UsersCache usersCache;

    public GetLastActive(UsersCache usersCache) {
        this.usersCache = usersCache;
    }

    public Date getLastActive(Channel channel) {

        ChannelState state = channel.getChannelState();

        Date lastActive = channel.getCreatedAt();
        if (lastActive == null) lastActive = new Date();
        if (state.getLastKnownActiveWatcher().after(lastActive)) {
            lastActive = state.getLastKnownActiveWatcher();
        }
        Message message = getLastMessageFromOtherUser(state);
        if (message != null) {
            if (message.getCreatedAt().after(lastActive)) {
                lastActive = message.getCreatedAt();
            }
        }
        for (Watcher watcher : state.getWatchers()) {
            if (watcher.getUser() == null || watcher.getUser().getLastActive() == null)
                continue;
            if (lastActive.before(watcher.getUser().getLastActive())) {
                String userId = watcher.getUserId();
                if (usersCache.isCurrentUser(userId)) continue;
                lastActive = watcher.getUser().getLastActive();
            }
        }
        return lastActive;
    }

    private Message getLastMessageFromOtherUser(ChannelState channelState) {

        Message lastMessage = null;
        try {
            List<Message> messages = channelState.getMessages();
            for (int i = messages.size() - 1; i >= 0; i--) {
                Message message = messages.get(i);
                String userId = message.getUserId();
                if (message.getDeletedAt() == null && !usersCache.isCurrentUser(userId)) {
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
