package io.getstream.chat.ui.sample.application

import io.getstream.chat.ui.sample.data.user.SampleUser

object AppConfig {
    const val apiKey: String = "gxdnf9h3dx8n"
    const val apiUrl: String = "chat-us-east-staging.stream-io-api.com"
    const val apiTimeout: Int = 6000
    const val cndTimeout: Int = 30000

    private val user1 = SampleUser(
        id = "Daanyaal",
        name = "Daanyaal",
        image = "https://randomuser.me/api/portraits/thumb/men/22.jpg",
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiRGFhbnlhYWwifQ.nYWcva2EshXhB-dGxWzAUaKTQU-fGYKXEypdToWnAR8"
    )

    private val user2 = SampleUser(
        id = "Fionn",
        name = "Fionn",
        image = "https://randomuser.me/api/portraits/thumb/men/42.jpg",
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiRmlvbm4ifQ.kKmy8uaO88nxWYb8gp_RsrMvap1rZDTj6-rmBMzMrKA"
    )

    private val user3 = SampleUser(
        id = "Mckay",
        name = "Mckay",
        image = "https://randomuser.me/api/portraits/thumb/men/6.jpg",
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiTWNrYXkifQ.ohglmIK3FrbTf0S8lZvOlFy129Pe1EKT756F-AMHEXY"
    )

    private val user4 = SampleUser(
        id = "Barnard",
        name = "Barnard",
        image = "https://randomuser.me/api/portraits/thumb/men/26.jpg",
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiQmFybmFyZCJ9.77_w0OIfuE-QSstqXetdeqRHZFtoRLrn41z5SKFPaY4"
    )

    val availableUsers: List<SampleUser> = listOf(user1, user2, user3, user4)
}
