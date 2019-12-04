package com.getstream.sdk.chat.channels;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.response.ChannelState;

import java.util.Date;
import java.util.List;

public class CountUnreadMentions {

    private final Client client;

    public CountUnreadMentions(Client client) {

        this.client = client;
    }

    /**
     * countUnread - Count the number of unread messages mentioning the current user
     *
     * @return {int} Unread mentions count
     */
    public int countUnreadMentions(Channel channel) {

        String currentUserId = client.getUserId();

        if (currentUserId == null) return -1;

        ChannelState channelState = channel.getChannelState();


        Date lastRead = channelState.getReadDateOfChannelLastMessage(currentUserId);
        int count = 0;
        for (Message m : channelState.getMessages()) {
            if (currentUserId.equals(m.getUserId())) {
                continue;
            }
            if (lastRead == null) {
                count++;
                continue;
            }
            if (m.getCreatedAt().getTime() > lastRead.getTime()) {
                List<User> mentionedUsers = m.getMentionedUsers();
                for (User mentionedUser : mentionedUsers) {
                    if (!mentionedUser.getId().equals(currentUserId)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
}
