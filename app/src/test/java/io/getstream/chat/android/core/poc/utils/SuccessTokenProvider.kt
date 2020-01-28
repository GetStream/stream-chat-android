package io.getstream.chat.android.core.poc.utils

import io.getstream.chat.android.core.poc.library.TokenProvider

class SuccessTokenProvider: TokenProvider {
    override fun getToken(listener: TokenProvider.TokenProviderListener) {
        listener.onSuccess("test-token")
    }
}