package io.getstream.chat.android.client.utils

import io.getstream.chat.android.client.token.TokenProvider

data class ImmediateTokenProvider(val token: String) :
    TokenProvider {
    override fun getToken(listener: TokenProvider.TokenProviderListener) {
        listener.onSuccess(token)
    }
}