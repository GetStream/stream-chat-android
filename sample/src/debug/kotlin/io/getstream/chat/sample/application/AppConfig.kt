package io.getstream.chat.sample.application

import io.getstream.chat.sample.data.user.User

class AppConfig {
    val apiKey: String = "qk4nn7rpcn75"
    val apiUrl: String = "chat-us-east-staging.stream-io-api.com"
    val apiTimeout: Int = 6000
    val cndTimeout: Int = 30000
    val availableUsers: List<User> = listOf(user1, user2, user3, user4)

    companion object {
        private val user1 = User(
            "80c26629-bc25-4ee5-a8ae-4824f8097b53",
            "paul",
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiODBjMjY2MjktYmMyNS00ZWU1LWE4YWUtNDgyNGY4MDk3YjUzIn0.ca55fO4jAbexSSZKqBMT0OaxCZaPtYmo1HWgvjBnOY4",
            "https://api.adorable.io/avatars/face/eyes10/nose8/mouth9/5530"
        )
        private val user2 = User(
            "a97a21dd-d993-42e0-8e52-31d6b3cea82c",
            "chani",
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYTk3YTIxZGQtZDk5My00MmUwLThlNTItMzFkNmIzY2VhODJjIn0.7Hv38I8xq328_nyKdMA8m1ehz3jrNdnaH1GzSj6yuzk",
            "https://api.adorable.io/avatars/face/eyes4/nose3/mouth1/5527"
        )
        private val user3 = User(
            "d39f2878-2e97-49bd-9148-6e3fd85bbce5",
            "duncan",
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiZDM5ZjI4NzgtMmU5Ny00OWJkLTkxNDgtNmUzZmQ4NWJiY2U1In0.Do_LACAzy5pxApqJx0kpztMm_vXNRLkAVE79LEafXZg",
            "https://api.adorable.io/avatars/face/eyes5/nose6/mouth3/6584"
        )
        private val user4 = User(
            "3761c83e-2a2e-4d6f-94ff-69c452386f8a",
            "leto",
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMzc2MWM4M2UtMmEyZS00ZDZmLTk0ZmYtNjljNDUyMzg2ZjhhIn0.SrnLIH0bthY0yc-QwbsImI6mXTmQTz9-a7_95MPFwsA",
            "https://api.adorable.io/avatars/face/eyes5/nose5/mouth8/2799"
        )
    }
}
