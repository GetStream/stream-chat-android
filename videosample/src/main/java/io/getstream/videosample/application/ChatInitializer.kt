package io.getstream.videosample.application

import android.content.Context
import android.util.Log
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.offline.experimental.plugin.Config
import io.getstream.chat.android.offline.experimental.plugin.OfflinePlugin
import io.getstream.videosample.BuildConfig
import io.getstream.videosample.data.user.SampleUser

@OptIn(InternalStreamChatApi::class)
class ChatInitializer(private val context: Context) {

    @OptIn(ExperimentalStreamChatApi::class)
    @Suppress("UNUSED_VARIABLE")
    fun init(apiKey: String) {
        val logLevel = if (BuildConfig.DEBUG) ChatLogLevel.ALL else ChatLogLevel.NOTHING

        val offlinePlugin = OfflinePlugin(Config(userPresence = true, persistenceEnabled = true))

        val client = ChatClient.Builder(apiKey, context)
            .logLevel(logLevel)
            .withPlugin(offlinePlugin)
            .build()

        val domain = ChatDomain.Builder(client, context)
            .userPresenceEnabled()
            .offlineEnabled()
            .build()

        val sampleUser = AppConfig.getUser()

        val user = User(
            id = sampleUser.id,
            extraData = mutableMapOf(
                "name" to "Leandro",
                "image" to "https://bit.ly/321RmWb",
            ),
        )

        ChatClient.instance().connectUser(user = user, token = sampleUser.token).enqueue { result ->
            if (result.isSuccess) {
                Log.d("ChatInitializer", "Success!!")
            } else {
                Log.d("ChatInitializer", "Failure!!")
            }
        }
    }
}
