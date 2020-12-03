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
import io.getstream.chat.android.client.errors.ChatError;
import io.getstream.chat.android.client.models.Filters;
import io.getstream.chat.android.client.models.User;
import io.getstream.chat.android.client.socket.InitConnectionListener;
import io.getstream.chat.android.client.token.TokenProvider;
import io.getstream.chat.android.client.utils.ChatUtils;
import io.getstream.chat.android.client.utils.FilterObject;
import io.getstream.chat.docs.TokenService;
import kotlin.Unit;

import static io.getstream.chat.docs.StaticInstances.TAG;

public class ClientAndUsers {
    private Context context;
    private ChatClient client;
    private TokenService yourTokenService;

    public void initialization() {
        // Typically done in your Application class using your API Key
        ChatClient client = new ChatClient.Builder("{{ api_key }}", context).build();

        // Static reference to initialised client
        ChatClient staticClientRef = ChatClient.instance();
    }

    public void setUser() {
        User user = new User();
        user.setId("user-id");

        // extraData allows you to add any custom fields you want to store about your user
        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("name", "Bender");
        extraData.put("image", "https://bit.ly/321RmWb");
        user.setExtraData(extraData);

        // You can setup a user token in 2 ways.
        // 1. Setup the current user with a JWT token.
        String token = "{{ chat_user_token }}";
        client.setUser(user, token, new InitConnectionListener() {
            @Override
            public void onSuccess(@NotNull ConnectionData data) {
                User user = data.getUser();
                String connectionId = data.getConnectionId();

                Log.i(TAG, String.format("Connection (%s) established for user %s", connectionId, user));
            }

            @Override
            public void onError(@NotNull ChatError error) {
                Log.e(TAG, String.format("There was an error %s", error), error.getCause());
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

        client.setUser(user, tokenProvider, new InitConnectionListener() {
            @Override
            public void onSuccess(@NotNull ConnectionData data) {
                User user = data.getUser();
                String connectionId = data.getConnectionId();

                Log.i(TAG, String.format("Connection (%s) established for user %s", connectionId, user));
            }

            @Override
            public void onError(@NotNull ChatError error) {
                Log.e(TAG, String.format("There was an error %s", error), error.getCause());
            }
        });
    }

    public void disconnect() {
        ChatClient.instance().disconnect();
    }

    public void developmentToken() {
        User user = new User();
        user.setId("user-id");
        String token = ChatUtils.devToken(user.getId());

        client.setUser(user, token, new InitConnectionListener() {
            @Override
            public void onSuccess(@NotNull ConnectionData data) {
                User user = data.getUser();
                String connectionId = data.getConnectionId();

                Log.i(TAG, String.format("Connection (%s) established for user %s", connectionId, user));
            }

            @Override
            public void onError(@NotNull ChatError error) {
                Log.e(TAG, String.format("There was an error %s", error), error.getCause());
            }
        });
    }

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

        client.setUser(user, tokenProvider, new InitConnectionListener() {
            @Override
            public void onSuccess(@NotNull ConnectionData data) {
                User user = data.getUser();
                String connectionId = data.getConnectionId();

                Log.i(TAG, String.format("Connection (%s) established for user %s", connectionId, user));
            }

            @Override
            public void onError(@NotNull ChatError error) {
                Log.e(TAG, String.format("There was an error %s", error, error.getCause()));
            }
        });
    }

    public void guestUser() {
        client.setGuestUser("user-id", "name", new InitConnectionListener() {
            @Override
            public void onSuccess(@NotNull ConnectionData data) {
                User user = data.getUser();
                String connectionId = data.getConnectionId();

                Log.i(TAG, String.format("Connection (%s) established for user %s", connectionId, user));
            }

            @Override
            public void onError(@NotNull ChatError error) {
                Log.e(TAG, String.format("There was an error %s", error), error.getCause());
            }
        });
    }

    public void queryingUsersById() {
        // search users with id "john", "jack", or "jessie"
        List<String> userIds = new ArrayList<>();
        userIds.add("john");
        userIds.add("jack");
        userIds.add("jessie");
        FilterObject filter = Filters.in("id", userIds);
        int offset = 0;
        int limit = 10;
        QuerySort<User> sort = new QuerySort<User>().desc("last_active");
        QueryUsersRequest request = new QueryUsersRequest(filter, offset, limit, sort, false);

        client.queryUsers(request).enqueue(result -> Unit.INSTANCE);

    }

    public void queryingBannedUsers() {
        FilterObject filter = Filters.eq("banned", true);
        int offset = 0;
        int limit = 10;
        QueryUsersRequest request = new QueryUsersRequest(filter, offset, limit);

        client.queryUsers(request).enqueue(result -> Unit.INSTANCE);
    }

    public void queryingUsersByAutocompleteName() {
        // search users with name contains "ro"
        FilterObject filter = Filters.autocomplete("name", "ro");
        int offset = 0;
        int limit = 10;
        QueryUsersRequest request = new QueryUsersRequest(filter, offset, limit);

        client.queryUsers(request).enqueue(result -> Unit.INSTANCE);
    }

    public void queryingUsersByAutocompleteId() {
        // search users with id contains "ro"
        FilterObject filter = Filters.autocomplete("id", "ro");
        int offset = 0;
        int limit = 10;
        QueryUsersRequest request = new QueryUsersRequest(filter, offset, limit);

        client.queryUsers(request).enqueue(result -> Unit.INSTANCE);
    }

    public void anonymousUser() {
        client.setAnonymousUser(new InitConnectionListener() {
            @Override
            public void onSuccess(@NotNull ConnectionData data) {
                User user = data.getUser();
                String connectionId = data.getConnectionId();

                Log.i(TAG, String.format("Connection (%s) established for user %s", connectionId, user));
            }

            @Override
            public void onError(@NotNull ChatError error) {
                Log.e(TAG, String.format("There was an error %s", error), error.getCause());
            }
        });
    }

    public void increasingTimeout() {
        new ChatClient.Builder("{{ api_key }}", context)
                .baseTimeout(6000)
                .cdnTimeout(6000)
                .build();
    }
}