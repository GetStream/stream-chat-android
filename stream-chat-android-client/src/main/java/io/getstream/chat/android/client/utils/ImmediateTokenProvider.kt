package io.getstream.chat.android.client.utils

import io.getstream.chat.android.client.token.TokenProvider

internal class ImmediateTokenProvider(private val token: String) : TokenProvider {
    override fun loadToken(): String = token
}
