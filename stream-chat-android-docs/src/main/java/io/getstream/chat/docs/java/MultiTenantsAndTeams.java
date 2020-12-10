package io.getstream.chat.docs.java;

import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.api.models.QueryUsersRequest;
import io.getstream.chat.android.client.models.Channel;
import io.getstream.chat.android.client.models.Filters;
import io.getstream.chat.android.client.models.User;
import io.getstream.chat.android.client.utils.FilterObject;

import static io.getstream.chat.docs.StaticInstances.TAG;

public class MultiTenantsAndTeams {
    private ChatClient client;

    /**
     * @see <a href="https://getstream.io/chat/docs/multi_tenant_chat/?language=java#channel-team">Multi Tenants & Teams</a>
     */
    public void createTeamChannel() {
        // Create channel with id red-general for red team
        Map<String, Object> extraData = new HashMap<>();
        extraData.put("team", "red");
        client.createChannel("messaging", "red-general", extraData).enqueue(result -> {
            if (result.isSuccess()) {
                Channel channel = result.data();
            } else {
                Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
            }
        });
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/multi_tenant_chat/?language=java#user-search">User Search</a>
     */
    public void searchUserWithSpecificTeam() {
        // Search for users with name Jordan that are part of the red team
        FilterObject filter = Filters.and(
                Filters.eq("name", "Jordan"),
                Filters.eq("teams", Filters.contains("red"))
        );

        int offset = 0;
        int limit = 1;
        client.queryUsers(new QueryUsersRequest(filter, offset, limit)).enqueue(result -> {
            if (result.isSuccess()) {
                List<User> users = result.data();
            } else {
                Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
            }
        });
    }
}
