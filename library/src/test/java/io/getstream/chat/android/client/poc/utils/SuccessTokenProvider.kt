package io.getstream.chat.android.client.poc.utils

import io.getstream.chat.android.client.TokenProvider

class SuccessTokenProvider: TokenProvider {
    override fun getToken(listener: TokenProvider.TokenProviderListener) {
        listener.onSuccess("test-token")
    }
}