package io.getstream.chat.android.core.poc.library

interface CachedTokenProvider {
    fun getToken(listener: TokenProvider.TokenProviderListener)
    fun tokenExpired()
}