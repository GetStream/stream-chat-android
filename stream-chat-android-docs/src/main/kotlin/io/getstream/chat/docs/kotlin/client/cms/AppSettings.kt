package io.getstream.chat.docs.kotlin.client.cms

import android.content.Context
import io.getstream.chat.android.client.ChatClient

class AppSettings(val context: Context) {

    inner class MultiRegionSupport {
        /**
         * @see <a href="https://getstream.io/chat/docs/android/multi_region/?language=java#migrating-to-edge">Migrating to Edge</a></></a>
         */
        fun migratingToEdge() {
            // Set the base URL
            val client = ChatClient.Builder("{{ api_key }}", context)
                .baseUrl("https://chat.stream-io-api.com/")
                .build()
        }
    }
}
