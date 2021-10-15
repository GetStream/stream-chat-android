package io.getstream.chat.docs.java;

import android.content.Context;

import io.getstream.chat.android.client.ChatClient;

public class AppSettings {
    private Context context;

    /**
     * @see <a href="https://getstream.io/chat/docs/android/multi_region/?language=java#client-location-settings">Client Location Settings</a>
     */
    public void clientLocationSettings() {
        // Set the base URL
        ChatClient client = new ChatClient.Builder("{{ api_key }}", context)
                .baseUrl("https://chat.stream-io-api.com/")
                .build();
    }
}
