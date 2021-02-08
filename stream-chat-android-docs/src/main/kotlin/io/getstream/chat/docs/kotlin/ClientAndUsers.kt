package io.getstream.chat.docs.kotlin

import android.content.Context
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.token.TokenProvider
import io.getstream.chat.docs.TokenService

class ClientAndUsers(val context: Context, val client: ChatClient, val yourTokenService: TokenService) {

    /**
     * @see <a href="https://getstream.io/chat/docs/init_and_users/?language=kotlin">Initialization & Users</a>
     */
    inner class InitializationAndUsers {
        fun initialization() {
            // Typically done in your Application class on startup
            val client = ChatClient.Builder("{{ api_key }}", context).build()

            // Client singleton is also available via static reference
            val staticClientRef = ChatClient.instance()
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/init_and_users/?language=kotlin#setting-the-user">Setting the User</a>
         */
        @Suppress("NAME_SHADOWING")
        fun setUser() {
            val user = User(
                id = "bender",
                extraData = mutableMapOf(
                    "name" to "Bender",
                    "image" to "https://bit.ly/321RmWb",
                ),
            )

            // You can setup a user token in two ways:

            // 1. Setup the current user with a JWT token
            val token = "{{ chat_user_token }}"
            client.connectUser(user, token).enqueue { result ->
                if (result.isSuccess) {
                    // Logged in
                    val user: User = result.data().user
                    val connectionId: String = result.data().connectionId
                } else {
                    // Handle result.error()
                }
            }

            // 2. Setup the current user with a TokenProvider
            val tokenProvider = object : TokenProvider {
                // Make a request to your backend to generate a valid token for the user
                override fun loadToken(): String = yourTokenService.getToken(user)
            }
            client.connectUser(user, tokenProvider).enqueue { /* ... */ }
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
            val user = User(
                id = "bender",
                extraData = mutableMapOf(
                    "name" to "Bender",
                    "image" to "https://bit.ly/321RmWb",
                ),
            )
            val token = client.devToken(user.id)

            client.connectUser(user, token).enqueue { /* ... */ }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/tokens_and_authentication/?language=kotlin#token-expiration">Token Expiration</a>
         */
        @Suppress("NAME_SHADOWING")
        fun tokenExpiration() {
            val user = User(
                id = "bender",
                extraData = mutableMapOf(
                    "name" to "Bender",
                    "image" to "https://bit.ly/321RmWb",
                ),
            )

            val tokenProvider = object : TokenProvider {
                // Make a request to your backend to generate a valid token for the user
                override fun loadToken(): String = yourTokenService.getToken(user)
            }
            client.connectUser(user, tokenProvider).enqueue { /* ... */ }
        }

        fun loggingOutAndSwitchingUsers(user: User, token: String) {
            client.disconnect()
            client.connectUser(user, token).enqueue { /* ... */ }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/guest_users/?language=kotlin">Guest Users</a>
     */
    inner class GuestUsers {
        fun guestUser() {
            client.connectGuestUser(userId = "bender", username = "Bender").enqueue { /*... */ }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/anon/?language=kotlin">Anonymous Users</a>
     */
    inner class AnonymousUsers {
        fun anonymousUser() {
            client.connectAnonymousUser().enqueue { /*... */ }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/query_users/?language=kotlin">Query Users</a>
     */
    inner class QueryUsers {
        fun queryingUsersById() {
            // Search for users with id "john", "jack", or "jessie"
            val request = QueryUsersRequest(
                filter = Filters.`in`("id", listOf("john", "jack", "jessie")),
                offset = 0,
                limit = 3,
            )

            client.queryUsers(request).enqueue { result ->
                if (result.isSuccess) {
                    val users: List<User> = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }

        fun queryingBannedUsers() {
            val request = QueryUsersRequest(
                filter = Filters.eq("banned", true),
                offset = 0,
                limit = 10,
            )

            client.queryUsers(request).enqueue { /* ... */ }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/query_users/?language=kotlin#querying-using-the-autocomplete-operator">Autocomplete Operator</a>
         */
        fun queryingUsersByAutocompleteName() {
            val request = QueryUsersRequest(
                filter = Filters.autocomplete("name", "ro"),
                offset = 0,
                limit = 10,
            )

            client.queryUsers(request).enqueue { /* ... */ }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/query_users/?language=kotlin#querying-using-the-autocomplete-operator">Autocomplete Operator</a>
         */
        fun queryingUsersByAutocompleteId() {
            val request = QueryUsersRequest(
                filter = Filters.autocomplete("id", "USER_ID"),
                offset = 0,
                limit = 10,
            )

            client.queryUsers(request).enqueue { /* ... */ }
        }
    }
}
