package io.getstream.chat.docs.kotlin.cookbook.utils

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.models.User
import kotlinx.coroutines.flow.transformWhile

suspend fun connectUser() {
    ChatClient.instance().run {
        clientState.initializationState
            .transformWhile {
                emit(it)
                it != InitializationState.COMPLETE
            }
            .collect {
                if (it == InitializationState.NOT_INITIALIZED) {
                    connectUser(userCredentials.user, userCredentials.token)
                        .enqueue { result ->
                            result.onSuccess { }
                            result.onError { }
                        }
                }
            }
    }
}

val userCredentials = UserCredentials(
    apiKey = "qx5us2v6xvmh",
    user = User(
        id = "filip",
        name = "Filip Babić",
        image = "https://ca.slack-edge.com/T02RM6X6B-U022AFX9D2S-f7bcb3d56180-128",
    ),
    token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiZmlsaXAifQ.WKqTjU6fHHjtFej-sUqS2ml3Rvdqn4Ptrf7jfKqzFgU",
)

data class UserCredentials(
    val apiKey: String,
    val user: User,
    val token: String,
)
