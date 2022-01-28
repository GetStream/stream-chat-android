package io.getstream.chat.android.client.token

/**
 * An implementation of [TokenProvider] that keeps previous values of the loaded token.
 * This implementation delegate the process to obtain a new token to another tokenProvider.
 *
 * @property tokenProvider The [TokenProvider] used to obtain new tokens
 */
internal class CacheableTokenProvider(private val tokenProvider: TokenProvider) : TokenProvider {
    private var cachedToken = ""
    override fun loadToken(): String = tokenProvider.loadToken().also { cachedToken = it }

    /**
     * Obtain the cached token.
     *
     * @return The cached token.
     */
    fun getCachedToken(): String = cachedToken
}
