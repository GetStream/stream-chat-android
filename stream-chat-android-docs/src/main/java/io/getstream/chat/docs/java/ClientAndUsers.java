package io.getstream.chat.docs.java;

import android.content.Context;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.api.models.QuerySort;
import io.getstream.chat.android.client.api.models.QueryUsersRequest;
import io.getstream.chat.android.client.models.Filters;
import io.getstream.chat.android.client.models.User;
import io.getstream.chat.android.client.token.TokenProvider;
import io.getstream.chat.android.client.utils.FilterObject;
import io.getstream.chat.docs.TokenService;

import static io.getstream.chat.docs.StaticInstances.TAG;

public class ClientAndUsers {
    private Context context;
    private ChatClient client;
    private TokenService yourTokenService;

    /**
     * @see <a href="https://getstream.io/chat/docs/init_and_users/?language=java">Initialization & Users</a>
     */
    class InitializationAndUsers {
        public void initialization() {
            // Typically done in your Application class using your API Key
            ChatClient client = new ChatClient.Builder("{{ api_key }}", context).build();

            // Static reference to initialised client
            ChatClient staticClientRef = ChatClient.instance();
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/init_and_users/?language=java#setting-the-user">Setting the User</a>
         */
        public void setUser() {
            User user = new User();
            user.setId("user-id");

            // ExtraData allows you to add any custom fields you want to store about your user
            HashMap<String, Object> extraData = new HashMap<>();
            extraData.put("name", "Bender");
            extraData.put("image", "https://bit.ly/321RmWb");
            user.setExtraData(extraData);

            // You can setup a user token in 2 ways.
            // 1. Setup the current user with a JWT token.
            String token = "{{ chat_user_token }}";
            client.connectUser(user, token).enqueue(result -> {
                if (result.isSuccess()) {
                    User userRes = result.data().getUser();
                    String connectionId = result.data().getConnectionId();

                    Log.i(TAG, String.format("Connection (%s) established for user %s", connectionId, userRes));
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
            });

            // 2. Setup the current user with a TokenProvider
            TokenProvider tokenProvider = new TokenProvider() {
                @NotNull
                @Override
                public String loadToken() {
                    return yourTokenService.getToken(user);
                }
            };

            client.connectUser(user, tokenProvider).enqueue(result -> {
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
         * @see <a href="https://getstream.io/chat/docs/init_and_users/?language=java#websocket-connections">Websocket Connections</a>
         */
        public void disconnect() {
            ChatClient.instance().disconnect();
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/tokens_and_authentication/?language=java">Tokens & Authentication</a>
     */
    class TokensAndAuthentication {
        /**
         * @see <a href="https://getstream.io/chat/docs/tokens_and_authentication/?language=java#development-tokens">Development Tokens</a>
         */
        public void developmentToken() {
            User user = new User();
            user.setId("user-id");
            String token = client.devToken(user.getId());

            client.connectUser(user, token).enqueue(result -> {
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
         * @see <a href="https://getstream.io/chat/docs/tokens_and_authentication/?language=java#token-expiration">Token Expiration</a>
         */
        public void tokenExpiration() {
            User user = new User();
            user.setId("user-id");
            TokenProvider tokenProvider = new TokenProvider() {
                @NotNull
                @Override
                public String loadToken() {
                    return yourTokenService.getToken(user);
                }
            };

            client.connectUser(user, tokenProvider).enqueue(result -> {
                if (result.isSuccess()) {
                    User userRes = result.data().getUser();
                    String connectionId = result.data().getConnectionId();

                    Log.i(TAG, String.format("Connection (%s) established for user %s", connectionId, userRes));
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/guest_users/?language=java">Guest Users</a>
     */
    class GuestUsers {
        public void guestUser() {
            client.connectGuestUser("user-id", "name").enqueue(result -> {
                if (result.isSuccess()) {
                    User userRes = result.data().getUser();
                    String connectionId = result.data().getConnectionId();

                    Log.i(TAG, String.format("Connection (%s) established for user %s", connectionId, userRes));
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/anon/?language=java">Anonymous Users</a>
     */
    class AnonymousUsers {
        public void anonymousUser() {
            client.connectAnonymousUser().enqueue(result -> {
                if (result.isSuccess()) {
                    User userRes = result.data().getUser();
                    String connectionId = result.data().getConnectionId();

                    Log.i(TAG, String.format("Connection (%s) established for user %s", connectionId, userRes));
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/query_users/?language=java">Query Users</a>
     */
    class QueryUsers {
        public void queryingUsersById() {
            // Search users with id "john", "jack", or "jessie"
            List<String> userIds = new ArrayList<>();
            userIds.add("john");
            userIds.add("jack");
            userIds.add("jessie");
            FilterObject filter = Filters.in("id", userIds);
            int offset = 0;
            int limit = 10;
            QuerySort<User> sort = new QuerySort<User>().desc("last_active");
            QueryUsersRequest request = new QueryUsersRequest(filter, offset, limit, sort, false);

            client.queryUsers(request).enqueue(result -> {
                if (result.isSuccess()) {
                    List<User> users = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
            });

        }

        public void queryingBannedUsers() {
            FilterObject filter = Filters.eq("banned", true);
            int offset = 0;
            int limit = 10;
            QueryUsersRequest request = new QueryUsersRequest(filter, offset, limit);

            client.queryUsers(request).enqueue(result -> {
                if (result.isSuccess()) {
                    List<User> users = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/query_users/?language=java#querying-using-the-autocomplete-operator">Autocomplete Operator</a>
         */
        public void queryingUsersByAutocompleteName() {
            // Search users with name contains "ro"
            FilterObject filter = Filters.autocomplete("name", "ro");
            int offset = 0;
            int limit = 10;
            QueryUsersRequest request = new QueryUsersRequest(filter, offset, limit);

            client.queryUsers(request).enqueue(result -> {
                if (result.isSuccess()) {
                    List<User> users = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/query_users/?language=java#querying-using-the-autocomplete-operator">Autocomplete Operator</a>
         */
        public void queryingUsersByAutocompleteId() {
            // Search users with id contains "ro"
            FilterObject filter = Filters.autocomplete("id", "ro");
            int offset = 0;
            int limit = 10;
            QueryUsersRequest request = new QueryUsersRequest(filter, offset, limit);

            client.queryUsers(request).enqueue(result -> {
                if (result.isSuccess()) {
                    List<User> users = result.data();
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/increasing_timeout/?language=java">Increasing Timeout</a>
     */
    class IncreasingTimeout {
        public void increasingTimeout() {
            new ChatClient.Builder("{{ api_key }}", context)
                    .baseTimeout(6000)
                    .cdnTimeout(6000)
                    .build();
        }
    }
}