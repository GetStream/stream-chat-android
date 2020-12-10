package io.getstream.chat.docs.kotlin

import android.content.Context
import io.getstream.chat.android.client.ChatClient

class AppSettings(val context: Context) {

    /**
     * @see <a href="https://getstream.io/chat/docs/multi_region/?language=kotlin#client-location-settings">Client Location Settings</a>
     */
    fun clientLocationSettings() {
        // EU West - Dublin region
        val dublinClient = ChatClient.Builder("{{ api_key }}", context)
            .baseUrl("https://chat-proxy-dublin.stream-io-api.com/")
            .build()

        // US East region
        val usEastClient = ChatClient.Builder("{{ api_key }}", context)
            .baseUrl("https://chat-proxy-us-east.stream-io-api.com/")
            .build()

        // Singapore region
        val singaporeClient = ChatClient.Builder("{{ api_key }}", context)
            .baseUrl("https://chat-proxy-singapore.stream-io-api.com/")
            .build()

        // Sydney region
        val sydneyClient = ChatClient.Builder("{{ api_key }}", context)
            .baseUrl("https://chat-proxy-sydney.stream-io-api.com/")
            .build()
    }
}
