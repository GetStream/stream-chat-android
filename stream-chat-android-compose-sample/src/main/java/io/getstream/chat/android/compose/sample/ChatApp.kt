package io.getstream.chat.android.compose.sample

import android.app.Application
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.ChatDomain

class ChatApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val user = User("tomi-vk")

        val chatClient = ChatClient.Builder(
            "kumvbr5ah5jg",
            this
        ).build()

        ChatDomain.Builder(chatClient, this)
            .userPresenceEnabled()
            .offlineEnabled()
            .build()

        chatClient.connectUser(
            user = user,
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoidG9taS12ayJ9.abdUJPrUWBufGAnXl4nuXD2fsLpFFKY96NJzrBtptoY"
        ).enqueue()
    }
}
