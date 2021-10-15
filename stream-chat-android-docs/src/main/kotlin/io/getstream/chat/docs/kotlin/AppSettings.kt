package io.getstream.chat.docs.kotlin

import android.content.Context
import io.getstream.chat.android.client.ChatClient

class AppSettings(val context: Context) {

    /**
     * See: [Client Location Settings](https://getstream.io/chat/docs/android/multi_region/?language=kotlin#client-location-settings)
     */
    fun clientLocationSettings() {
        // Set the base URL
        val client = ChatClient.Builder("{{ api_key }}", context)
            .baseUrl("https://chat.stream-io-api.com/")
            .build()
    }
}
