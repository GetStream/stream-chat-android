package io.getstream.chat.android.client.token

interface TokenProvider {
    fun getToken(listener: TokenProviderListener)

    interface TokenProviderListener {
        fun onSuccess(token: String)
    }
}