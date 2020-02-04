package io.getstream.chat.android.client.api

import io.getstream.chat.android.client.CachedTokenProvider
import io.getstream.chat.android.client.CachedTokenProviderImpl
import io.getstream.chat.android.client.TokenProvider
import io.getstream.chat.android.client.utils.ImmediateTokenProvider


class ChatConfig(
    val apiKey: String,
    private val baseURL: String,
    private val cdnURL: String,
    val baseTimeout: Int,
    val cdnTimeout: Int
) {

    val tokenProvider: CachedTokenProvider = CachedTokenProviderImpl()
    var isAnonymous: Boolean = false

    val httpURL: String
        get() = "https://$baseURL/"

    val cdnHttpURL: String
        get() = "https://$cdnURL/"

    val wssURL: String
        get() = "wss://$baseURL/"

    class Builder {

        private var apiKey: String = ""
        private var baseURL: String = ""
        private var cdnURL: String = ""
        private var baseTimeout: Int = 0
        private var cdnTimeout: Int = 0
        private lateinit var tokenProvider: TokenProvider

        fun apiKey(apiKey: String): Builder {
            this.apiKey = apiKey
            return this
        }

        fun token(token: String): Builder {
            this.tokenProvider = ImmediateTokenProvider(token)
            return this
        }

        fun tokenProvider(tokenProvider: TokenProvider): Builder {
            this.tokenProvider = tokenProvider
            return this
        }

        fun baseTimeout(timeout: Int): Builder {
            baseTimeout = timeout
            return this
        }

        fun cdnTimeout(timeout: Int): Builder {
            cdnTimeout = timeout
            return this
        }

        fun baseURL(baseURL: String): Builder {
            var baseURL = baseURL
            if (baseURL.startsWith("https://")) {
                baseURL = baseURL.split("https://").toTypedArray()[1]
            }
            if (baseURL.startsWith("http://")) {
                baseURL = baseURL.split("http://").toTypedArray()[1]
            }
            if (baseURL.endsWith("/")) {
                baseURL = baseURL.substring(0, baseURL.length - 1)
            }
            this.baseURL = baseURL
            return this
        }

        fun cdnUrl(cdnURL: String): Builder {
            var cdnURL = cdnURL
            if (cdnURL.startsWith("https://")) {
                cdnURL = cdnURL.split("https://").toTypedArray()[1]
            }
            if (cdnURL.startsWith("http://")) {
                cdnURL = cdnURL.split("http://").toTypedArray()[1]
            }
            if (cdnURL.endsWith("/")) {
                cdnURL = cdnURL.substring(0, cdnURL.length - 1)
            }
            this.cdnURL = cdnURL
            return this
        }

        fun build(): ChatConfig {
            val result = ChatConfig(
                apiKey,
                baseURL,
                cdnURL,
                baseTimeout,
                cdnTimeout
            )
            result.tokenProvider.setTokenProvider(tokenProvider)
            return result
        }
    }
}