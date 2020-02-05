package io.getstream.chat.android.client.token

import io.getstream.chat.android.client.token.TokenProvider

interface CachedTokenProvider {
    fun getToken(listener: TokenProvider.TokenProviderListener)
    fun tokenExpired()
    fun setTokenProvider(provider: TokenProvider)
}