package io.getstream.chat.android.client

interface TokenProvider {
    fun getToken(listener: TokenProviderListener)

    interface TokenProviderListener {
        fun onSuccess(token: String)
    }
}