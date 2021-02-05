package io.getstream.chat.docs.kotlin

import android.content.Context
import android.util.Log
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.token.TokenProvider
import io.getstream.chat.docs.StaticInstances.TAG
import io.getstream.chat.docs.TokenService

class ClientAndUsers(val context: Context, val client: ChatClient, val yourTokenService: TokenService) {

    /**
     * @see <a href="https://getstream.io/chat/docs/init_and_users/?language=kotlin">Initialization & Users</a>
     */
    inner class InitializationAndUsers {
        fun initialization() {
            // Typically done in your Application class using your API Key
            val client = ChatClient.Builder("{{ api_key }}", context).build()

            // Static reference to initialised client
            val staticClientRef = ChatClient.instance()
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/init_and_users/?language=kotlin#setting-the-user">Setting the User</a>
         */
        @Suppress("NAME_SHADOWING")
        fun setUser() {
            val user = User("user-id")

            // ExtraData allows you to add any custom fields you want to store about your user
            user.extraData["name"] = "Bender"
            user.extraData["image"] = "https://bit.ly/321RmWb"

            // You can setup a user token in 2 ways.
            // 1. Setup the current user with a JWT token.
            val token = "{{ chat_user_token }}"
            client.connectUser(user, token).enqueue { result ->
                if (result.isSuccess) {
                    val user: User = result.data().user
                    val connectionId: String = result.data().connectionId

                    Log.i(TAG, "Connection ($connectionId) established for user $user")
                } else {
                    Log.e(TAG, "There was an error ${result.error()}", result.error().cause)
                }
            }

            // 2. Setup the current user with a TokenProvider
            val tokenProvider = object : TokenProvider {
                // Make a request here to your backend to generate a valid token for the user.
                override fun loadToken(): String = yourTokenService.getToken(user)
            }

            client.connectUser(user, tokenProvider).enqueue { result ->
                if (result.isSuccess) {
                    val user: User = result.data().user
                    val connectionId: String = result.data().connectionId

                    Log.i(TAG, "Connection ($connectionId) established for user $user")
                } else {
                    Log.e(TAG, "There was an error ${result.error()}", result.error().cause)
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/init_and_users/?language=java#websocket-connections">Websocket Connections</a>
         */
        fun disconnect() {
            ChatClient.instance().disconnect()
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/tokens_and_authentication/?language=kotlin">Tokens & Authentication</a>
     */
    inner class TokensAndAuthentication {

        /**
         * @see <a href="https://getstream.io/chat/docs/tokens_and_authentication/?language=kotlin#development-tokens">Development Tokens</a>
         */
        fun developmentToken() {
            val user: User = User("user-id")
            val token: String = client.devToken(user.id)
            client.connectUser(user, token).enqueue { result ->
                if (result.isSuccess) {
                    val user: User = result.data().user
                    val connectionId: String = result.data().connectionId

                    Log.i(TAG, "Connection ($connectionId) established for user $user")
                } else {
                    Log.e(TAG, "There was an error ${result.error()}", result.error().cause)
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/tokens_and_authentication/?language=kotlin#token-expiration">Token Expiration</a>
         */
        @Suppress("NAME_SHADOWING")
        fun tokenExpiration() {
            val user = User("user-id")
            val tokenProvider = object : TokenProvider {
                // Make a request here to your backend to generate a valid token for the user.
                override fun loadToken(): String = yourTokenService.getToken(user)
            }

            client.connectUser(user, tokenProvider).enqueue { result ->
                if (result.isSuccess) {
                    val user: User = result.data().user
                    val connectionId: String = result.data().connectionId

                    Log.i(TAG, "Connection ($connectionId) established for user $user")
                } else {
                    Log.e(TAG, "There was an error ${result.error()}", result.error().cause)
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/guest_users/?language=kotlin">Guest Users</a>
     */
    inner class GuestUsers {
        fun guestUser() {
            client.connectGuestUser("user-id", "name").enqueue { result ->
                if (result.isSuccess) {
                    val user = result.data().user
                    val connectionId = result.data().connectionId
                    Log.i(TAG, String.format("Connection (%s) established for user %s", connectionId, user))
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error(), result.error().cause))
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/anon/?language=kotlin">Anonymous Users</a>
     */
    inner class AnonymousUsers {
        fun anonymousUser() {
            client.connectAnonymousUser().enqueue { result ->
                if (result.isSuccess) {
                    val user = result.data().user
                    val connectionId = result.data().connectionId
                    Log.i(TAG, String.format("Connection (%s) established for user %s", connectionId, user))
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error(), result.error().cause))
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/query_users/?language=kotlin">Query Users</a>
     */
    inner class QueryUsers {
        fun queryingUsersById() {
            // Search users with id "john", "jack", or "jessie"
            val filter = Filters.`in`("id", listOf("john", "jack", "jessie"))
            val offset = 0
            val limit = 10
            val sort = QuerySort.desc<User>("last_active")
            val request = QueryUsersRequest(filter, offset, limit, sort)

            client.queryUsers(request).enqueue {
                if (it.isSuccess) {
                    val users = it.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", it.error(), it.error().cause))
                }
            }
        }

        fun queryingBannedUsers() {
            val filter = Filters.eq("banned", true)
            val offset = 0
            val limit = 10
            val request = QueryUsersRequest(filter, offset, limit)

            client.queryUsers(request).enqueue {
                if (it.isSuccess) {
                    val users = it.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", it.error(), it.error().cause))
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/query_users/?language=kotlin#querying-using-the-autocomplete-operator">Autocomplete Operator</a>
         */
        fun queryingUsersByAutocompleteName() {
            // Search users with name contains "ro"
            val filter = Filters.autocomplete("name", "ro")
            val offset = 0
            val limit = 10

            client.queryUsers(QueryUsersRequest(filter, offset, limit)).enqueue {
                if (it.isSuccess) {
                    val users = it.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", it.error(), it.error().cause))
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/query_users/?language=kotlin#querying-using-the-autocomplete-operator">Autocomplete Operator</a>
         */
        fun queryingUsersByAutocompleteId() {
            // Search users with id contains "ro"
            val filter = Filters.autocomplete("id", "ro")
            val offset = 0
            val limit = 10

            client.queryUsers(QueryUsersRequest(filter, offset, limit)).enqueue {
                if (it.isSuccess) {
                    val users = it.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", it.error(), it.error().cause))
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/increasing_timeout/?language=kotlin">Increasing Timeout</a>
     */
    inner class IncreasingTimeout {
        fun increasingTimeout() {
            ChatClient
                .Builder("{{ api_key }}", context)
                .baseTimeout(6000)
                .cdnTimeout(6000)
                .build()
        }
    }
}
