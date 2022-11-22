package io.getstream.chat.docs.kotlin.client.docusaurus

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.User
import io.getstream.chat.android.client.utils.Result

/**
 * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/channel-list-updates/">Channel List Updates</a>
 */
class HandlingUserConnection {

    fun connectingAUser() {
        val user = User(
            id = "bender",
            name = "Bender",
            image = "https://bit.ly/321RmWb",
        )

        // Check if the user is not already set
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

    fun disconnectTheUser() {
        ChatClient.instance().disconnect(flushPersistence = false).enqueue { result ->
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

    fun switchTheUser() {
        val user1 = User(
            id = "bender",
            name = "Bender",
            image = "https://bit.ly/321RmWb",
        )

        // Connect the first user
        ChatClient.instance().connectUser(user = user1, token = "userToken") // Replace with a real token
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

        val user2 = User(
            id = "bender2",
            name = "Bender2",
            image = "https://bit.ly/321RmWb",
        )

        ChatClient.instance().switchUser(user = user2, token = "userToken") // Replace with a real token
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
