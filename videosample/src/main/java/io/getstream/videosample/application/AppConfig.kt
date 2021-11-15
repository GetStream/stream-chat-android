package io.getstream.videosample.application

import io.getstream.videosample.data.user.SampleUser

object AppConfig {
    const val apiKey: String = "qx5us2v6xvmh"
    const val apiUrl: String = "chat-us-east-staging.stream-io-api.com"
    const val apiTimeout: Int = 6000
    const val cndTimeout: Int = 30000

    fun getUser(): SampleUser =
        SampleUser(
            apiKey = apiKey,
            id = "leandro",
            name = "Leandro Borges Ferreira",
            image = "https://ca.slack-edge.com/T02RM6X6B-U01AQ67NJ9Z-2f28d711cae9-128",
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoibGVhbmRybyJ9.CjlYUr79r4GopAhXIbqLBighl3meLsT4dQKzdKX7L3g"
        )
}
