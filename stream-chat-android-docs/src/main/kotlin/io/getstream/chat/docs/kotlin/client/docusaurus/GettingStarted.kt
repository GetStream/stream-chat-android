package io.getstream.chat.docs.kotlin.client.docusaurus

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.User
import io.getstream.result.Result

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
