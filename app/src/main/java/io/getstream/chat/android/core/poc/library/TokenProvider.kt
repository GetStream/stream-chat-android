package io.getstream.chat.android.core.poc.library

interface TokenProvider {
    fun getToken(listener: TokenProviderListener)

    interface TokenProviderListener {
        fun onSuccess(token: String)
    }
}