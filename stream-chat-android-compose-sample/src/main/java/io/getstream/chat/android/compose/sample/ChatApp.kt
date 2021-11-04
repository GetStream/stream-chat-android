package io.getstream.chat.android.compose.sample

import android.app.Application
import com.getstream.sdk.chat.utils.DateFormatter
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.ChatDomain

class ChatApp : Application() {

    companion object {
        lateinit var dateFormatter: DateFormatter
            private set
    }

    override fun onCreate() {
        super.onCreate()
        dateFormatter = DateFormatter.from(this)

        val client = ChatClient.Builder("qx5us2v6xvmh", applicationContext)
            .logLevel(ChatLogLevel.ALL)
            .build()
        ChatDomain.Builder(client, applicationContext)
            .userPresenceEnabled()
            .build()

        val user = User(
            id = "jc",
            extraData = mutableMapOf(
                "name" to "Jc Miñarro",
                "image" to "https://ca.slack-edge.com/T02RM6X6B-U011KEXDPB2-891dbb8df64f-128",
            ),
        )

        client.connectUser(
            user = user,
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiamMifQ.2_5Hae3LKjVSfA0gQxXlZn54Bq6xDlhjPx2J7azUNB4"
        ).enqueue()
    }
}
