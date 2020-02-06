package io.getstream.chat.android.client.token

class CachedTokenProviderImpl :
    CachedTokenProvider {

    private var cacheUserToken: String = ""
    private var fetchingToken = false
    private val listeners = mutableListOf<TokenProvider.TokenProviderListener>()
    private var provider: TokenProvider? = null

    override fun getToken(listener: TokenProvider.TokenProviderListener) {
        if (cacheUserToken.isNotEmpty()) {
            listener.onSuccess(cacheUserToken)
            return
        }

        if (fetchingToken) {
            listeners.add(listener)
            return
        } else {
            // token is not in cache and there are no in-flight requests, go fetch it
            fetchingToken = true
        }

        provider?.getToken(object : TokenProvider.TokenProviderListener {
            override fun onSuccess(token: String) {
                fetchingToken = false
                listener.onSuccess(token)
                for (l in listeners) l.onSuccess(token)
                listeners.clear()
            }
        })
    }

    override fun tokenExpired() {
        cacheUserToken = ""
    }

    override fun setTokenProvider(provider: TokenProvider) {
        this.provider = provider
    }
}