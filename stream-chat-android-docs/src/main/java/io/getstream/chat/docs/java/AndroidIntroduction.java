package io.getstream.chat.docs.java;

import android.content.Context;

import com.getstream.sdk.chat.ChatUI;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.logger.ChatLogLevel;
import io.getstream.chat.android.client.models.User;
import io.getstream.chat.android.livedata.ChatDomain;

public class AndroidIntroduction {

    /**
     * @see <a href="https://getstream.io/chat/docs/android/?language=java#chat-client">Chat Client</a>
     */
    public void chatClient(Context applicationContext) {
        String apiKey = "{{ api_key }}";
        String userToken = "{{ chat_user_token }}";
        // Step 1 - Set up the client for API calls
        ChatClient client = new ChatClient.Builder(apiKey, applicationContext)
                // Change log level
                .logLevel(ChatLogLevel.ALL)
                .build();
        // Step 2 - Set up the domain for offline storage
        ChatDomain domain = new ChatDomain.Builder(client, applicationContext)
                // Enable offline support
                .offlineEnabled()
                .build();
        // Step 3 - Set up UI components
        ChatUI ui = new ChatUI.Builder(applicationContext).build();

        // Step 2 - Authenticate and connect the user
        User user = new User();
        user.setId("summer-brook-2");
        user.getExtraData().put("name", "Paranoid Android");
        user.getExtraData().put("image", "https://bit.ly/2TIt8NR");

        client.connectUser(
                user,
                userToken // or client.devToken(userId); if auth is disabled for your app
        ).enqueue((result) -> {
            if (result.isSuccess()) {
                // Handle success
            } else {
                // Handler error
            }
        });
    }
}
