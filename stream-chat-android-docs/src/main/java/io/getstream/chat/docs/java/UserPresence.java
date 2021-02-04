package io.getstream.chat.docs.java;

import android.util.Log;

import java.util.Arrays;
import java.util.List;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.api.models.QueryChannelsRequest;
import io.getstream.chat.android.client.api.models.QuerySort;
import io.getstream.chat.android.client.api.models.QueryUsersRequest;
import io.getstream.chat.android.client.api.models.WatchChannelRequest;
import io.getstream.chat.android.client.channel.ChannelClient;
import io.getstream.chat.android.client.events.UserPresenceChangedEvent;
import io.getstream.chat.android.client.models.Channel;
import io.getstream.chat.android.client.models.Filters;
import io.getstream.chat.android.client.models.User;
import io.getstream.chat.android.client.utils.FilterObject;

import static io.getstream.chat.docs.StaticInstances.TAG;

public class UserPresence {
    private ChatClient client;
    private ChannelClient channelClient;

    /**
     * @see <a href="https://getstream.io/chat/docs/presence_format/?language=java#invisible">Invisible</a>
     */
    public void invisible() {
        User user = new User();
        user.setId("user-id");
        user.setInvisible(true);
        client.connectUser(user, "{{ chat_user_token }}").enqueue(result -> {
            if (result.isSuccess()) {
                User userRes = result.data().getUser();
                String connectionId = result.data().getConnectionId();

                Log.i(TAG, String.format("Connection (%s) established for user %s", connectionId, userRes));
            } else {
                Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
            }
        });
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/presence_format/?language=java#listening-to-presence-changes">Listening to Presence Changes</a>
     */
    public void listeningPresenceChanges() {
        // You need to be watching some channels/queries to be able to get presence events.
        // There are multiple ways of doing so:
        // 1. Watch a channel for presence event
        WatchChannelRequest watchRequest = new WatchChannelRequest();
        watchRequest.setPresence(true);
        watchRequest.getData().put("members", Arrays.asList("john", "jack"));
        channelClient.watch(watchRequest).enqueue(result -> {
            if (result.isSuccess()) {
                Channel channel = result.data();
            } else {
                Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
            }
        });

        // 2. Query some channels with presence events
        int channelsOffset = 0;
        int channelsLimit = 10;
        FilterObject channelsFilter = Filters.in("members", "john", "jack").put("type", "messaging");
        QuerySort channelsSort = new QuerySort();
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
                List<Channel> channels = result.data();
            } else {
                Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
            }
        });

        // 3. Query some users for presence event
        int usersOffset = 0;
        int usersLimit = 2;
        FilterObject usersFilter = Filters.in("id", Arrays.asList("john", "jack"));
        QueryUsersRequest usersQuery = new QueryUsersRequest(usersFilter, usersOffset, usersLimit);
        usersQuery.setPresence(true);
        client.queryUsers(usersQuery).enqueue(result -> {
            if (result.isSuccess()) {
                List<User> users = result.data();
            } else {
                Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
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
