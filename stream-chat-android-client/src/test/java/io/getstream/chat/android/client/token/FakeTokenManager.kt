package io.getstream.chat.android.client.token

import io.getstream.chat.android.client.utils.Result

internal class FakeTokenManager(val tkn: String) : TokenManager {
    override fun loadAsync(listener: (Result<String>) -> Unit) {
        listener(Result(tkn))
    }

    override fun loadAsync() {
    }

    override fun loadSync() {
    }

    override fun expireToken() {
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
}
