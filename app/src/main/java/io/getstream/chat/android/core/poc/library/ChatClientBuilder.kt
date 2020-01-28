package io.getstream.chat.android.core.poc.library

import io.getstream.chat.android.core.poc.library.api.ApiClientOptions
import io.getstream.chat.android.core.poc.library.api.RetrofitClient
import io.getstream.chat.android.core.poc.library.gson.JsonParserImpl
import io.getstream.chat.android.core.poc.library.socket.ChatSocket
import io.getstream.chat.android.core.poc.library.socket.ChatSocketImpl

class ChatClientBuilder(
    apiKey: String,
    apiOptions: ApiClientOptions
) {

    private var isAnonymous = false

    private val jsonParser = JsonParserImpl()
    private val tokenProvider: CachedTokenProvider = CachedTokenProviderImpl()
    private val socket: ChatSocket =
        ChatSocketImpl(apiKey, apiOptions.wssURL, tokenProvider, jsonParser)

    private val api: ChatApi = ChatApiImpl(
        apiKey,
        RetrofitClient.buildClient(
            apiOptions,
            { tokenProvider },
            { isAnonymous },
            jsonParser
        ).create(
            RetrofitApi::class.java
        ),
        jsonParser
    )


    fun build(): ChatClient {
        return ChatClientImpl(api, socket)
    }
}