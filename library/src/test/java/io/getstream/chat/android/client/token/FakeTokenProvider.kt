package io.getstream.chat.android.client.token

class FakeTokenProvider(vararg val tokens: String) : TokenProvider {

    var tokenId = 0

    override fun loadToken(): String {
        return tokens[tokenId++]
    }
}