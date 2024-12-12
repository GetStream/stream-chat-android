package io.getstream.chat.docs.kotlin.client.docusaurus

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.User
import io.getstream.result.Result
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import io.getstream.chat.android.state.plugin.config.StatePluginConfig
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory

/**
 * @see <a href="https://getstream.io/chat/docs/sdk/android/client/overview/">Getting Started</a>
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

    fun addingAPlugin(apiKey: String, context: Context) {
        val client = ChatClient.Builder(apiKey, context)
            .withPlugins(
                //Add the desired plugin factories here
            )
            .build()
    }

    fun addingTheStatePlugin(apiKey: String, context: Context) {
        // Create a state plugin factory
        val statePluginFactory = StreamStatePluginFactory(
            config = StatePluginConfig(
                // Enables background sync which syncs user actions performed while offline
                backgroundSyncEnabled = true,
                // Enables tracking online states for users
                userPresence = true
            ),
            appContext = context
        )

        ChatClient.Builder(apiKey, context)
            // Add the state plugin to the chat client
            .withPlugins(statePluginFactory)
            .build()
    }

    fun addingTheOfflinePlugin(apiKey: String, context: Context) {
        // Create an offline plugin factory
        val offlinePluginFactory = StreamOfflinePluginFactory(appContext = context)

        // Create a state plugin factory
        val statePluginFactory = StreamStatePluginFactory(
            config = StatePluginConfig(
                // Enables background sync which syncs user actions performed while offline.
                backgroundSyncEnabled = true,
                // Enables tracking online states for users
                userPresence = true,
            ),
            appContext = context
        )

        ChatClient.Builder(apiKey, context)
            // Add both the state and offline plugin factories to the chat client
            .withPlugins(offlinePluginFactory, statePluginFactory)
            .build()
    }

    fun connectingAUser() {
        val user = User(
            id = "bender",
            name = "Bender",
            image = "https://bit.ly/321RmWb",
        )

        // Connect the user only if they aren't already connected
        if (ChatClient.instance().getCurrentUser() == null) {

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
}
