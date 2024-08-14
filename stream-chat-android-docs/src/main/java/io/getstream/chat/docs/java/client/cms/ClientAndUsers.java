package io.getstream.chat.docs.java.client.cms;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.models.FilterObject;
import io.getstream.chat.android.client.api.models.QueryUsersRequest;
import io.getstream.chat.android.models.Filters;
import io.getstream.chat.android.models.User;
import io.getstream.chat.android.client.token.TokenProvider;
import io.getstream.chat.docs.TokenService;

public class ClientAndUsers {
    private Context context;
    private ChatClient client;
    private TokenService yourTokenService;

    /**
     * @see <a href="https://getstream.io/chat/docs/init_and_users/?language=java">Initialization & Users</a>
     */
    class InitializationAndUsers {
        public void initialization() {
            // Typically done in your Application class on startup
            ChatClient client = new ChatClient.Builder("{{ api_key }}", context).build();

            // Client singleton is also available via static reference
            ChatClient staticClientRef = ChatClient.instance();
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/android/init_and_users/?language=kotlin#connecting-the-user">Connecting the User</a>
         */
        @SuppressWarnings("Convert2Lambda")
        public void connectingUser() {
            User user = new User.Builder()
                    .withId("bender")
                    .withName("Bender")
                    .withImage("https://bit.ly/321RmWb")
                    .build();

            // You can setup a user token in two ways:

            // 1. Setup the current user with a JWT token
            String token = "{{ chat_user_token }}";
            client.connectUser(user, token).enqueue(result -> {
                if (result.isSuccess()) {
                    // Logged in
                    User userRes = result.getOrNull().getUser();
                    String connectionId = result.getOrNull().getConnectionId();
                } else {
                    // Handle error
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
            client.connectUser(user, tokenProvider).enqueue(result -> {/* ... */});
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/init_and_users/?language=java#websocket-connections">Websocket Connections</a>
         */
        public void disconnect() {
            ChatClient.instance().disconnect(true).enqueue();
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
            User user = new User.Builder()
                    .withId("bender")
                    .withName("Bender")
                    .withImage("https://bit.ly/321RmWb")
                    .build();

            String token = client.devToken(user.getId());

            client.connectUser(user, token).enqueue(result -> { /* ... */ });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/tokens_and_authentication/?language=java#token-expiration">Token Expiration</a>
         */
        @SuppressWarnings("Convert2Lambda")
        public void tokenExpiration() {
            User user = new User.Builder()
                    .withId("bender")
                    .withName("Bender")
                    .withImage("https://bit.ly/321RmWb")
                    .build();

            TokenProvider tokenProvider = new TokenProvider() {
                @NotNull
                @Override
                public String loadToken() {
                    return yourTokenService.getToken(user);
                }
            };
            client.connectUser(user, tokenProvider).enqueue(result -> { /* ... */ });
        }
    }

    class AuthlessUsers {
        /**
         * @see <a href="https://getstream.io/chat/docs/android/authless_users/?language=java#guest-users">Guest Users</a>
         */
        class GuestUsers {
            public void guestUser() {
                client.connectGuestUser("bender", "Bender").enqueue(result -> { /* ... */ });
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/android/authless_users/?language=java#anonymous-users">Anonymous Users</a>
         */
        class AnonymousUsers {
            public void anonymousUser() {
                client.connectAnonymousUser().enqueue(result -> { /* ... */ });
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/query_users/?language=java">Query Users</a>
     */
    class QueryUsers {
        public void queryingUsersById() {
            // Search users with id "john", "jack", or "jessie"
            FilterObject filter = Filters.in("id", Arrays.asList("john", "jack", "jessie"));
            int offset = 0;
            int limit = 3;
            QueryUsersRequest request = new QueryUsersRequest(filter, offset, limit);

            client.queryUsers(request).enqueue(result -> {
                if (result.isSuccess()) {
                    List<User> users = result.getOrNull();
                } else {
                    // Handle error
                }
            });
        }

        public void queryingBannedUsers() {
            FilterObject filter = Filters.eq("banned", true);
            int offset = 0;
            int limit = 10;
            QueryUsersRequest request = new QueryUsersRequest(filter, offset, limit);

            client.queryUsers(request).enqueue(result -> { /* ... */ });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/query_users/?language=java#querying-using-the-autocomplete-operator">Autocomplete Operator</a>
         */
        public void queryingUsersByAutocompleteName() {
            FilterObject filter = Filters.autocomplete("name", "ro");
            int offset = 0;
            int limit = 10;
            QueryUsersRequest request = new QueryUsersRequest(filter, offset, limit);

            client.queryUsers(request).enqueue(result -> { /* ... */ });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/query_users/?language=java#querying-using-the-autocomplete-operator">Autocomplete Operator</a>
         */
        public void queryingUsersByAutocompleteId() {
            FilterObject filter = Filters.autocomplete("id", "USER_ID");
            int offset = 0;
            int limit = 10;
            QueryUsersRequest request = new QueryUsersRequest(filter, offset, limit);

            client.queryUsers(request).enqueue(result -> { /* ... */ });
        }
    }

    class LoggingOut {
        /**
         * @see <a href="https://getstream.io/chat/docs/android/logout/?language=java">Disconnect</a>
         */
        public void disconnect() {
            User user = new User();
            String token = "token";

            client.disconnect(true).enqueue(disconnectResult -> {
                if (disconnectResult.isSuccess()) {
                    client.connectUser(user, token).enqueue(loginResult -> { /* ... */ });
                } else {
                    // Handle result error
                }
            });
        }
    }
}
