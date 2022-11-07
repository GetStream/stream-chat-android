package io.getstream.chat.docs.kotlin.client.docusaurus

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.UploadAttachmentsNetworkType
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.plugin.configuration.Config
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import io.getstream.chat.android.state.plugin.configuration.StatePluginConfig
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory

/**
 * @see <a href="https://getstream.io/chat/docs/sdk/android/basics/getting-started/#getting-started">Getting Started</a>
 */
class GettingStarted {

    fun creatingAChatClient() {
        class App : Application() {
            override fun onCreate() {
                super.onCreate()
                val chatClient = ChatClient.Builder("apiKey", applicationContext).build()
            }
        }

        class MainActivity : AppCompatActivity() {
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                val chatClient = ChatClient.instance() // Returns the singleton instance
            }
        }
    }

    fun addingTheOfflinePlugin(apiKey: String, context: Context) {
        val offlinePluginFactory = StreamOfflinePluginFactory(
            config = Config(
                // Enables background sync which syncs user actions performed while offline.
                backgroundSyncEnabled = true,
                // Enables the ability to receive information about user activity such as last active date and if they are online right now.
                userPresence = true,
                // Enables using the database as an internal caching mechanism.
                persistenceEnabled = true,
                // An enumeration of various network types used as a constraint inside upload attachments worker.
            ),
            appContext = context,
        )

        val statePluginFactory = StreamStatePluginFactory(
            config = StatePluginConfig(
                backgroundSyncEnabled = true,
                userPresence = true,
            ),
            appContext = context
        )

        ChatClient.Builder(apiKey, context)
            .withPlugins(offlinePluginFactory, statePluginFactory)
            .uploadAttachmentsNetworkType(UploadAttachmentsNetworkType.NOT_ROAMING)
            .build()
    }

    fun connectingAUser() {
        val user = User(
            id = "bender",
            name = "Bender",
            image = "https://bit.ly/321RmWb",
        )

        ChatClient.instance().connectUser(user = user, token = "userToken") // Replace with a real token
            .enqueue { result ->
                when (result) {
                    is Result.Success -> {
                        // Handle success
                    }
                    is Result.Failure -> {
                        // Handler error
                    }
                }
            }
    }
}
