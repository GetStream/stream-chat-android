package io.getstream.chat.sample.application

import io.getstream.chat.sample.data.user.User

class AppConfig {
    val apiKey: String = "qk4nn7rpcn75"
    val apiUrl: String = "chat-us-east-staging.stream-io-api.com"
    val apiTimeout: Int = 6000
    val cndTimeout: Int = 30000
    val availableUsers: List<User> = listOf(user1, user2, user3)
    companion object {
        private val user1 = User("bender", "Bender", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYmVuZGVyIn0.3KYJIoYvSPgTURznP8nWvsA2Yj2-vLqrm-ubqAeOlcQ")
        private val user2 = User("broken-waterfall-5", "Jon Snow", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYnJva2VuLXdhdGVyZmFsbC01In0.d1xKTlD_D0G-VsBoDBNbaLjO-2XWNA8rlTm4ru4sMHg")
        private val user3 = User("steep-moon-9", "Steep moon", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoic3RlZXAtbW9vbi05In0.K7uZEqKmiVb5_Y7XFCmlz64SzOV34hoMpeqRSz7g4YI")
    }
}
