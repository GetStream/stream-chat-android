package io.getstream.chat.android.client.token

internal interface TokenManager {
    /**
     * Ensure a token has been loaded.
     */
    fun ensureTokenLoaded()

    /**
     * Load a new token.
     */
    fun loadSync(): String

    /**
     * Expire the current token.
     */
    fun expireToken()

    /**
     * Check if a [TokenProvider] has been provided.
     *
     * @return true if a token provider has been provided, false on another case.
     */
    fun hasTokenProvider(): Boolean

    /**
     * Inject a new [CacheableTokenProvider]
     *
     * @param provider A [CacheableTokenProvider]
     */
    fun setTokenProvider(provider: CacheableTokenProvider)

    /**
     * Obtain last token loaded.
     *
     * @return the last token loaded. If the token was expired an empty [String] will be returned.
     */
    fun getToken(): String

    /**
     * Check if a token was loaded.
     *
     * @return true if a token was loaded and it is not expired, false on another case.
     */
    fun hasToken(): Boolean
}
