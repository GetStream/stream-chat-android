package com.getstream.sdk.chat.users;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Member;
import com.getstream.sdk.chat.model.Watcher;
import com.getstream.sdk.chat.rest.User;

import java.util.ArrayList;
import java.util.List;

public class GetOtherUsers {

    private final UsersCache usersCache;

    public GetOtherUsers(UsersCache usersCache) {

        this.usersCache = usersCache;
    }

    public List<User> getOtherUsers(Channel channel) {

        List<Member> members = channel.getChannelState().getMembers();
        List<Watcher> watchers = channel.getChannelState().getWatchers();

        //Log.d(TAG, "getOtherUsers");

        List<User> users = new ArrayList<>();
        String currentUserId = usersCache.getCurrentId();

        if (members != null) {
            for (Member m : members) {
                String userId = m.getUserId();
                if (!userId.equals(currentUserId)) {
                    User user = usersCache.getUser(m.getUser().getId());
                    //Log.d(TAG, "getOtherUsers: member: " + user);
                    users.add(user);
                }
            }
        }

        if (watchers != null) {
            for (Watcher w : watchers) {
                String userId = w.getUserId();
                if (!userId.equals(currentUserId)) {
                    User user = usersCache.getUser(w.getUser().getId());
                    //Log.d(TAG, "getOtherUsers: watcher: " + user);
                    if (!users.contains(user))
                        users.add(user);
                }
            }
        }

        return users;
    }
}
