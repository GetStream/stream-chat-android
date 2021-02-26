package io.getstream.chat.android.client.token

internal class FakeTokenManager(val tkn: String) : TokenManager {

    override fun loadSync(): String {
        return tkn
    }

    override fun ensureTokenLoaded() {
        // empty
    }

    override fun setTokenProvider(provider: TokenProvider) {
        // empty
    }

    override fun hasTokenProvider(): Boolean {
        return true
    }

    override fun getToken(): String {
        return tkn
    }

    override fun hasToken(): Boolean {
        return true
    }

    override fun expireToken() {
        // empty
    }
}
