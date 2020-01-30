package io.getstream.chat.android.core.poc.library

import io.getstream.chat.android.core.poc.app.App
import io.getstream.chat.android.core.poc.library.api.ApiClientOptions
import io.getstream.chat.android.core.poc.library.api.RetrofitClient
import io.getstream.chat.android.core.poc.library.gson.JsonParserImpl
import io.getstream.chat.android.core.poc.library.socket.ChatSocket
import io.getstream.chat.android.core.poc.library.socket.ChatSocketImpl

class ChatClientBuilder(
    application: App,
    apiKey: String,
    apiOptions: ApiClientOptions,
    anonymousAuth: () -> Boolean
) {

    private val jsonParser = JsonParserImpl()
    private val tokenProvider: CachedTokenProvider = CachedTokenProviderImpl()
    private val socket: ChatSocket =
        ChatSocketImpl(apiKey, apiOptions.wssURL, tokenProvider, jsonParser)

    private val api: ChatApi = ChatApiImpl(
        apiKey,
        RetrofitClient.buildClient(
            apiOptions,
            { tokenProvider },
            anonymousAuth,
            jsonParser
        ).create(
            RetrofitApi::class.java
        ),
        jsonParser,
        application = application
    )

    fun build(): ChatClient {
        return ChatClientImpl(api, socket)
    }
}