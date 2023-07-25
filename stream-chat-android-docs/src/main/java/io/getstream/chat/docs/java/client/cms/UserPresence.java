package io.getstream.chat.docs.java.client.cms;

import java.util.Arrays;
import java.util.List;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.models.FilterObject;
import io.getstream.chat.android.client.api.models.QueryChannelsRequest;
import io.getstream.chat.android.client.api.models.QueryUsersRequest;
import io.getstream.chat.android.client.api.models.WatchChannelRequest;
import io.getstream.chat.android.models.querysort.QuerySortByField;
import io.getstream.chat.android.models.querysort.QuerySorter;
import io.getstream.chat.android.client.channel.ChannelClient;
import io.getstream.chat.android.client.events.UserPresenceChangedEvent;
import io.getstream.chat.android.models.Channel;
import io.getstream.chat.android.models.Filters;
import io.getstream.chat.android.models.User;

public class UserPresence {
    private ChatClient client;
    private ChannelClient channelClient;

    /**
     * @see <a href="https://getstream.io/chat/docs/presence_format/?language=java#invisible">Invisible</a>
     */
    public void invisible() {
        User user = new User.Builder()
                .withId("user-id")
                .withInvisible(true)
                .build();
        client.connectUser(user, "{{ chat_user_token }}").enqueue(result -> {
            if (result.isSuccess()) {
                User userRes = result.getOrNull().getUser();
            } else {
                // Handle error
            }
        });
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/presence_format/?language=java#listening-to-presence-changes">Listening to Presence Changes</a>
     */
    public void listeningPresenceChanges() {
        // You need to be watching some channels/queries to be able to get presence events.
        // Here are three different ways of doing that:

        // 1. Watch a single channel with presence = true set
        WatchChannelRequest watchRequest = new WatchChannelRequest();
        watchRequest.setPresence(true);
        watchRequest.getData().put("members", Arrays.asList("john", "jack"));
        channelClient.watch(watchRequest).enqueue(result -> {
            if (result.isSuccess()) {
                Channel channel = result.getOrNull();
            } else {
                // Handle error
            }
        });

        // 2. Query some channels with presence events
        int channelsOffset = 0;
        int channelsLimit = 10;
        FilterObject channelsFilter = Filters.and(
                Filters.eq("type", "messaging"),
                Filters.in("members", Arrays.asList("john", "jack"))
        );
        QuerySorter<Channel> channelsSort = new QuerySortByField<>();
        int messageLimit = 0;
        int memberLimit = 0;
        QueryChannelsRequest channelsRequest = new QueryChannelsRequest(
                channelsFilter,
                channelsOffset,
                channelsLimit,
                channelsSort,
                messageLimit,
                memberLimit
        );
        client.queryChannels(channelsRequest).enqueue(result -> {
            if (result.isSuccess()) {
                List<Channel> channels = result.getOrNull();
            } else {
                // Handle error
            }
        });

        // 3. Query some users with presence = true set
        int usersOffset = 0;
        int usersLimit = 2;
        FilterObject usersFilter = Filters.in("id", Arrays.asList("john", "jack"));
        QueryUsersRequest usersQuery = new QueryUsersRequest(usersFilter, usersOffset, usersLimit);
        usersQuery.setPresence(true);
        client.queryUsers(usersQuery).enqueue(result -> {
            if (result.isSuccess()) {
                List<User> users = result.getOrNull();
            } else {
                // Handle error
            }
        });

        // Finally, Subscribe to events
        client.subscribeFor(
                new Class[]{UserPresenceChangedEvent.class},
                event -> {
                    // Handle change
                }
        );
    }
}
