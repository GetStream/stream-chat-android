package io.getstream.chat.android.core.poc.library

import io.getstream.chat.android.core.poc.library.api.ApiClientOptions
import io.getstream.chat.android.core.poc.library.api.RetrofitClient
import io.getstream.chat.android.core.poc.library.gson.JsonParserImpl
import io.getstream.chat.android.core.poc.library.logger.StreamChatSilentLogger
import io.getstream.chat.android.core.poc.library.logger.StreamLogger
import io.getstream.chat.android.core.poc.library.socket.ChatSocket
import io.getstream.chat.android.core.poc.library.socket.ChatSocketImpl

class ChatClientBuilder(
    apiKey: String,
    apiOptions: ApiClientOptions,
    logger: StreamLogger? = StreamChatSilentLogger()
) {

    private var isAnonymous = false

    private val jsonParser = JsonParserImpl()
    private val tokenProvider: CachedTokenProvider = CachedTokenProviderImpl()
    private val socket: ChatSocket =
        ChatSocketImpl(apiKey, apiOptions.wssURL, tokenProvider, jsonParser, logger)

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
        jsonParser,
        logger
    )


    fun build(): ChatClient {
        return ChatClientImpl(api, socket)
    }
}