package io.getstream.chat.docs.java.client.docusaurus;

import android.content.Context;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory;

/**
 * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/offline-support">Offline Support</a>
 */
public class OfflineSupport {

    private String apiKey = "api-key";

    public void configureOfflinePlugin(Context context) {
        StreamOfflinePluginFactory offlinePluginFactory = new StreamOfflinePluginFactory(context);
        new ChatClient.Builder("apiKey", context).withPlugins(offlinePluginFactory).build();
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
