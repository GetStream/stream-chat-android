package io.getstream.chat.docs.java;

import android.content.Context;

import io.getstream.chat.android.client.ChatClient;

public class AppSettings {
    private Context context;

    /**
     * @see <a href="https://getstream.io/chat/docs/multi_region/?language=java#client-location-settings">Client Location Settings</a>
     */
    public void clientLocationSettings() {
        // EU West - Dublin region
        ChatClient dublinClient = new ChatClient.Builder("{{ api_key }}", context)
                .baseUrl("https://chat-proxy-dublin.stream-io-api.com/")
                .build();

        // US East region
        ChatClient usEastClient = new ChatClient.Builder("{{ api_key }}", context)
                .baseUrl("https://chat-proxy-us-east.stream-io-api.com/")
                .build();

        // Singapore region
        ChatClient singaporeClient = new ChatClient.Builder("{{ api_key }}", context)
                .baseUrl("https://chat-proxy-singapore.stream-io-api.com/")
                .build();

        // Sydney region
        ChatClient sydneyClient = new ChatClient.Builder("{{ api_key }}", context)
                .baseUrl("https://chat-proxy-sydney.stream-io-api.com/")
                .build();
    }
}
