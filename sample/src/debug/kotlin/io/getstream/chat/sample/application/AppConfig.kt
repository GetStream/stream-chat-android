package io.getstream.chat.sample.application

import io.getstream.chat.sample.data.user.User

class AppConfig {
    val apiKey: String = "tpamn43dmgtc"
    val apiUrl: String = "chat-us-east-staging.stream-io-api.com"
    val apiTimeout: Int = 6000
    val cndTimeout: Int = 30000
    val availableUsers: List<User> = listOf(sam, frodo)
    companion object {
        private val sam = User("user_id_1", "Sam", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoidXNlcl9pZF8xIn0.Y42IJLmQGDJm8GXOo3KH9nMMnHg_3Gnt4iSTVwFEwuY")
        private val frodo = User("user_id_2", "Frodo", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoidXNlcl9pZF8yIn0.4mQPEg1v4xlzB9lw1ECUXPMe1VnvRkR4gIAyXzEGWuM")
    }
}
