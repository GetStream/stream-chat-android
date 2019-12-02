package com.getstream.sdk.chat.channels;

import android.text.TextUtils;
import android.util.Log;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.users.GetOtherUsers;

import java.util.ArrayList;
import java.util.List;

public class GetChannelNameOrMembers {

    private final GetOtherUsers getOtherUsers;

    public GetChannelNameOrMembers(GetOtherUsers getOtherUsers) {

        this.getOtherUsers = getOtherUsers;
    }

    public String getChannelNameOrMembers(Channel channel) {
        String channelName;

        //Log.i(TAG, "Channel name is" + channel.getName() + channel.getCid());

        if (!TextUtils.isEmpty(channel.getName())) {
            channelName = channel.getName();
        } else {
            List<User> users = this.getOtherUsers.getOtherUsers(channel);
            List<User> top3 = users.subList(0, Math.min(3, users.size()));
            List<String> usernames = new ArrayList<>();
            for (User u : top3) {
                if (u == null) continue;
                usernames.add(u.getName());
            }

            channelName = TextUtils.join(", ", usernames);
            if (users.size() > 3) {
                channelName += "...";
            }
        }
        return channelName;
    }
}
