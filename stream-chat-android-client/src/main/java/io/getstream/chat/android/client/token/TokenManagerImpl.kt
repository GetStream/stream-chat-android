package io.getstream.chat.android.client.token

internal class TokenManagerImpl : TokenManager {
    @Volatile
    private var token: String = EMPTY_TOKEN
    private lateinit var provider: TokenProvider

    override fun ensureTokenLoaded() {
        if (!hasToken()) {
            loadSync()
        }
    }

    override fun loadSync(): String {
        return try {
            provider.loadToken()
        } catch (t: Throwable) {
            EMPTY_TOKEN
        }.also {
            this.token = it
        }
    }

    override fun setTokenProvider(provider: TokenProvider) {
        this.token = EMPTY_TOKEN
        this.provider = provider
    }

    override fun hasTokenProvider(): Boolean {
        return this::provider.isInitialized
    }

    override fun getToken(): String = token

    override fun hasToken(): Boolean {
        return token != EMPTY_TOKEN
    }

    override fun expireToken() {
        token = EMPTY_TOKEN
    }

    companion object {
        const val EMPTY_TOKEN = ""
    }
}
