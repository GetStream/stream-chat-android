package io.getstream.chat.android.core.poc.library

import io.getstream.chat.android.core.poc.library.api.ApiClientOptions
import io.getstream.chat.android.core.poc.library.api.RetrofitClient
import io.getstream.chat.android.core.poc.library.socket.ChatSocket
import io.getstream.chat.android.core.poc.library.socket.ChatSocketImpl

class ChatClientBuilder(
    apiKey: String,
    apiOptions: ApiClientOptions
) {

    private var isAnonymous = false
    private var tokenProvider: CachedTokenProvider = CachedTokenProviderImpl()
    private val socket: ChatSocket = ChatSocketImpl(apiKey, apiOptions.wssURL, tokenProvider)

    private val api: ChatApi = ChatApiImpl(
        apiKey,
        RetrofitClient.getClient(
            apiOptions,
            { tokenProvider },
            { isAnonymous }
        ).create(
            RetrofitApi::class.java
        )
    )

    private val anonymousApi: ChatApi = ChatApiImpl(
        apiKey,
        RetrofitClient.getClient(
            apiOptions,
            { null },
            { true }
        ).create(
            RetrofitApi::class.java
        )
    )


    fun build(): ChatClient {
        return ChatClientImpl(api, anonymousApi, socket)
    }
}