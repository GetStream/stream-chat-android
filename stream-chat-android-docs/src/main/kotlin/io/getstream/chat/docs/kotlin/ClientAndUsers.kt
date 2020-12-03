package io.getstream.chat.docs.kotlin

import android.content.Context
import android.util.Log
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.socket.InitConnectionListener
import io.getstream.chat.android.client.token.TokenProvider
import io.getstream.chat.android.client.utils.ChatUtils
import io.getstream.chat.docs.StaticInstances.TAG
import io.getstream.chat.docs.TokenService

object ClientAndUsers {
    val context: Context = TODO()
    val client: ChatClient = TODO()
    val yourTokenService = TokenService

    fun initialization() {
        // Typically done in your Application class using your API Key
        val client = ChatClient.Builder("{{ api_key }}", context).build()

        // Static reference to initialised client
        val staticClientRef = ChatClient.instance()
    }

    @Suppress("NAME_SHADOWING")
    fun setUser() {
        val user = User("user-id")

        // extraData allows you to add any custom fields you want to store about your user
        user.extraData["name"] = "Bender"
        user.extraData["image"] = "https://bit.ly/321RmWb"

        // You can setup a user token in 2 ways.
        // 1. Setup the current user with a JWT token.
        val token = "{{ chat_user_token }}"
        client.setUser(
            user,
            token,
            object : InitConnectionListener() {
                override fun onSuccess(data: ConnectionData) {
                    val user: User = data.user
                    val connectionId: String = data.connectionId

                    Log.i(TAG, "Connection ($connectionId) established for user $user")
                }

                override fun onError(error: ChatError) {
                    Log.e(TAG, "There was an error $error", error.cause)
                }
            }
        )

        // 2. Setup the current user with a TokenProvider
        val tokenProvider = object : TokenProvider {
            // Make a request here to your backend to generate a valid token for the user.
            override fun loadToken(): String = yourTokenService.getToken(user)
        }

        client.setUser(
            user,
            tokenProvider,
            object : InitConnectionListener() {
                override fun onSuccess(data: ConnectionData) {
                    val user: User = data.user
                    val connectionId: String = data.connectionId

                    Log.i(TAG, "Connection ($connectionId) established for user $user")
                }

                override fun onError(error: ChatError) {
                    Log.e(TAG, "There was an error $error", error.cause)
                }
            }
        )
    }

    fun disconnect() {
        ChatClient.instance().disconnect()
    }

    fun developmentToken() {
        val user: User = User("user-id")
        val token: String = ChatUtils.devToken(user.id)
        client.setUser(
            user,
            token,
            object : InitConnectionListener() {
                override fun onSuccess(data: ConnectionData) {
                    val user: User = data.user
                    val connectionId: String = data.connectionId

                    Log.i(TAG, "Connection ($connectionId) established for user $user")
                }

                override fun onError(error: ChatError) {
                    Log.e(TAG, "There was an error $error", error.cause)
                }
            }
        )
    }

    @Suppress("NAME_SHADOWING")
    fun tokenExpiration() {
        val user = User("user-id")
        val tokenProvider = object : TokenProvider {
            // Make a request here to your backend to generate a valid token for the user.
            override fun loadToken(): String = yourTokenService.getToken(user)
        }

        client.setUser(
            user,
            tokenProvider,
            object : InitConnectionListener() {
                override fun onSuccess(data: ConnectionData) {
                    val user: User = data.user
                    val connectionId: String = data.connectionId

                    Log.i(TAG, "Connection ($connectionId) established for user $user")
                }

                override fun onError(error: ChatError) {
                    Log.e(TAG, "There was an error $error", error.cause)
                }
            }
        )
    }

    fun guestUser() {
        client.setGuestUser(
            "user-id",
            "name",
            object : InitConnectionListener() {
                override fun onSuccess(data: ConnectionData) {
                    val user = data.user
                    val connectionId = data.connectionId
                    Log.i(TAG, String.format("Connection (%s) established for user %s", connectionId, user))
                }

                override fun onError(error: ChatError) {
                    Log.e(TAG, String.format("There was an error %s", error, error.cause))
                }
            }
        )
    }
}
