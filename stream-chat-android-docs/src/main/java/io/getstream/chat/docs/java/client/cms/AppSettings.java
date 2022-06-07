package io.getstream.chat.docs.java.client.cms;

import android.content.Context;

import io.getstream.chat.android.client.ChatClient;

public class AppSettings {
    private Context context;

    class MultiRegionSupport {
        /**
         * @see <a href="https://getstream.io/chat/docs/android/multi_region/?language=kotlin#migrating-to-edge">Migrating to Edge</a></></a>
         */
        public void migratingToEdge() {
            // Set the base URL
            ChatClient client = new ChatClient.Builder("{{ api_key }}", context)
                    .baseUrl("https://chat.stream-io-api.com/")
                    .build();
        }
    }
}
