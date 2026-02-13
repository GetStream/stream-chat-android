package io.getstream.chat.docs.java.client.docusaurus;

import android.content.Context;

import java.util.HashSet;
import java.util.Set;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.api.OfflineConfig;

/**
 * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/offline-support">Offline Support</a>
 */
public class OfflineSupport {

    private String apiKey = "api-key";

    public void configureOfflinePlugin(Context context) {
        boolean enabled = true;
        Set<String> ignoredChannelTypes = new HashSet<>();
        ignoredChannelTypes.add("livestream");
        OfflineConfig config = new OfflineConfig(enabled, ignoredChannelTypes);
        new ChatClient.Builder("apiKey", context)
                .offlineConfig(config)
                .build();
    }

    public void clearData(ChatClient chatClient) {
        boolean flushPersistence = true;
        chatClient.disconnect(flushPersistence).enqueue((result) -> {
            if (result.isSuccess()) {
                // Handle success
            } else {
                // Handle error
            }
        });
    }
}
