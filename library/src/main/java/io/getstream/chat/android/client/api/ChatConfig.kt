package io.getstream.chat.android.client.api

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.token.CachedTokenProvider
import io.getstream.chat.android.client.token.CachedTokenProviderImpl
import io.getstream.chat.android.client.token.TokenProvider
import io.getstream.chat.android.client.utils.ImmediateTokenProvider


class ChatConfig(
    val apiKey: String,
    private val baseURL: String,
    private val cdnURL: String,
    val baseTimeout: Int,
    val cdnTimeout: Int
) {

    val tokenProvider: CachedTokenProvider =
        CachedTokenProviderImpl()
    var isAnonymous: Boolean = false

    val httpURL: String
        get() = "https://$baseURL/"

    val cdnHttpURL: String
        get() = "https://$cdnURL/"

    val wssURL: String
        get() = "wss://$baseURL/"

    class Builder {

        private var apiKey: String = ""
        private var baseUrl: String = ""
        private var cdnUrl: String = ""
        private var baseTimeout: Int = 10000
        private var cdnTimeout: Int = 10000
        private lateinit var tokenProviderInstance: TokenProvider

        fun apiKey(apiKey: String): Builder {
            this.apiKey = apiKey
            return this
        }

        fun token(token: String): Builder {
            this.tokenProviderInstance = ImmediateTokenProvider(token)
            return this
        }

        fun tokenProvider(tokenProvider: TokenProvider): Builder {
            this.tokenProviderInstance = tokenProvider
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

        fun baseUrl(value: String): Builder {
            var baseUrl = value
            if (baseUrl.startsWith("https://")) {
                baseUrl = baseUrl.split("https://").toTypedArray()[1]
            }
            if (baseUrl.startsWith("http://")) {
                baseUrl = baseUrl.split("http://").toTypedArray()[1]
            }
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length - 1)
            }
            this.baseUrl = baseUrl
            return this
        }

        fun cdnUrl(value: String): Builder {
            var cdnUrl = value
            if (cdnUrl.startsWith("https://")) {
                cdnUrl = cdnUrl.split("https://").toTypedArray()[1]
            }
            if (cdnUrl.startsWith("http://")) {
                cdnUrl = cdnUrl.split("http://").toTypedArray()[1]
            }
            if (cdnUrl.endsWith("/")) {
                cdnUrl = cdnUrl.substring(0, cdnUrl.length - 1)
            }
            this.cdnUrl = cdnUrl
            return this
        }

        fun build(): ChatConfig {
            val result = ChatConfig(
                apiKey,
                baseUrl,
                cdnUrl,
                baseTimeout,
                cdnTimeout
            )

            if (!this::tokenProviderInstance.isInitialized) {
                throw ChatError("token or token provider is not defined in ChatConfig.Builder")
            } else if (apiKey.isEmpty()) {
                throw ChatError("apiKey is not defined in ChatConfig.Builder")
            } else if (baseUrl.isEmpty()) {
                throw ChatError("baseUrl is not defined in ChatConfig.Builder")
            }

            result.tokenProvider.setTokenProvider(tokenProviderInstance)
            return result
        }
    }
}