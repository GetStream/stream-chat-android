package io.getstream.chat.ui.sample.application

import io.getstream.chat.ui.sample.data.user.SampleUser

object AppConfig {
    const val apiKey: String = "qk4nn7rpcn75"
    const val apiUrl: String = "chat-us-east-staging.stream-io-api.com"
    const val apiTimeout: Int = 6000
    const val cndTimeout: Int = 30000

    private val user1 = SampleUser(
        "80c26629-bc25-4ee5-a8ae-4824f8097b53",
        "paul",
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiODBjMjY2MjktYmMyNS00ZWU1LWE4YWUtNDgyNGY4MDk3YjUzIn0.ca55fO4jAbexSSZKqBMT0OaxCZaPtYmo1HWgvjBnOY4",
    )
    private val user2 = SampleUser(
        "a97a21dd-d993-42e0-8e52-31d6b3cea82c",
        "chani",
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYTk3YTIxZGQtZDk5My00MmUwLThlNTItMzFkNmIzY2VhODJjIn0.7Hv38I8xq328_nyKdMA8m1ehz3jrNdnaH1GzSj6yuzk",
    )
    private val user3 = SampleUser(
        "d39f2878-2e97-49bd-9148-6e3fd85bbce5",
        "duncan",
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiZDM5ZjI4NzgtMmU5Ny00OWJkLTkxNDgtNmUzZmQ4NWJiY2U1In0.Do_LACAzy5pxApqJx0kpztMm_vXNRLkAVE79LEafXZg",
    )
    private val user4 = SampleUser(
        "3761c83e-2a2e-4d6f-94ff-69c452386f8a",
        "leto",
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMzc2MWM4M2UtMmEyZS00ZDZmLTk0ZmYtNjljNDUyMzg2ZjhhIn0.SrnLIH0bthY0yc-QwbsImI6mXTmQTz9-a7_95MPFwsA",
    )
    val availableUsers: List<SampleUser> = listOf(user1, user2, user3, user4)
}
