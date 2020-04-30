package io.getstream.chat.android.client.utils

import io.getstream.chat.android.client.token.TokenProvider

class SuccessTokenProvider : TokenProvider {
    override fun loadToken(): String {
        return "test-token"
    }
}