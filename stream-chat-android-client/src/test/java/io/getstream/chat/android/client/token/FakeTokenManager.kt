package io.getstream.chat.android.client.token

import io.getstream.chat.android.client.utils.Result

internal class FakeTokenManager(val tkn: String) : TokenManager {
    override fun loadAsyncAndRetry(listener: (Result<String>) -> Unit) {
        listener(Result(tkn))
    }

    override fun loadSync(): String {
        return tkn
    }

    override fun expireToken() {
        // empty
    }

    override fun setTokenProvider(provider: TokenProvider) {
    }

    override fun getToken(): String {
        return tkn
    }

    override fun hasToken(): Boolean {
        return true
    }

    override fun hasTokenProvider(): Boolean {
        return true
    }

    override fun shutdown() {
        // empty
    }
}
