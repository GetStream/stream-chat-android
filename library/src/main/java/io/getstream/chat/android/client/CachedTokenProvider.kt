package io.getstream.chat.android.client

interface CachedTokenProvider {
    fun getToken(listener: TokenProvider.TokenProviderListener)
    fun tokenExpired()
    fun setTokenProvider(provider: TokenProvider)
}