package io.getstream.chat.android.client.token

internal interface TokenManager {
    fun ensureTokenLoaded()
    fun loadSync(): String
    fun expireToken()
    fun hasTokenProvider(): Boolean
    fun setTokenProvider(provider: TokenProvider)
    fun getToken(): String
    fun hasToken(): Boolean
}
