package io.getstream.chat.android.client

import io.getstream.chat.android.client.api.ApiClientOptions
import io.getstream.chat.android.client.api.RetrofitClient
import io.getstream.chat.android.client.gson.JsonParserImpl
import io.getstream.chat.android.client.logger.StreamChatSilentLogger
import io.getstream.chat.android.client.logger.StreamLogger
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.socket.ChatSocketImpl

class ChatClientBuilder(
    apiKey: String,
    apiOptions: ApiClientOptions,
    logger: StreamLogger? = StreamChatSilentLogger()
) {

    private val config = ChatConfig()
    private val jsonParser = JsonParserImpl()
    private val socket: ChatSocket = ChatSocketImpl(apiKey, apiOptions.wssURL, config.tokenProvider, jsonParser, logger)

    private val api: ChatApi = ChatApiImpl(
        apiKey,
        RetrofitClient.buildClient(
            apiOptions,
            jsonParser,
            config
        ).create(
            RetrofitApi::class.java
        ),
        jsonParser,
        logger
    )

    fun build(): ChatClient {
        return ChatClientImpl(api, socket, config)
    }

    class ChatConfig {
        var isAnonimous: Boolean = false
        val tokenProvider: CachedTokenProvider = CachedTokenProviderImpl()
    }
}