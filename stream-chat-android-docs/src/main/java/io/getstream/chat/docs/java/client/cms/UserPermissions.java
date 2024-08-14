package io.getstream.chat.docs.java.client.cms;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.models.FilterObject;
import io.getstream.chat.android.client.api.models.QueryUsersRequest;
import io.getstream.chat.android.models.Channel;
import io.getstream.chat.android.models.Filters;
import io.getstream.chat.android.models.User;

public class UserPermissions {
    private ChatClient client;

    class MultiTenantAndTeams {
        /**
         * @see <a href="https://getstream.io/chat/docs/multi_tenant_chat/?language=java#channel-team">Channel Team</a>
         */
        public void createTeamChannel() {
            // Creates the red-general channel for the red team
            Map<String, Object> extraData = new HashMap<>();
            List<String> memberIds = new LinkedList<>();
            extraData.put("team", "red");
            client.createChannel("messaging", "red-general", memberIds, extraData).enqueue(result -> {
                if (result.isSuccess()) {
                    Channel channel = result.getOrNull();
                } else {
                    // Handle error
                }
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/multi_tenant_chat/?language=java#user-search">User Search</a>
         */
        public void searchUserWithSpecificTeam() {
            // Search for users with the name Jordan that are part of the red team
            FilterObject filter = Filters.and(
                    Filters.eq("name", "Jordan"),
                    Filters.contains("teams", "red")
            );

            int offset = 0;
            int limit = 1;
            client.queryUsers(new QueryUsersRequest(filter, offset, limit)).enqueue(result -> {
                if (result.isSuccess()) {
                    List<User> users = result.getOrNull();
                } else {
                    // Handle error
                }
            });
        }
    }

}
