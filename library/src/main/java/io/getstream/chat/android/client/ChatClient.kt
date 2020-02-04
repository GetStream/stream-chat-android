package io.getstream.chat.android.client

import io.getstream.chat.android.client.api.ChatConfig
import io.getstream.chat.android.client.api.RetrofitClient
import io.getstream.chat.android.client.call.ChatCall
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.gson.JsonParser
import io.getstream.chat.android.client.gson.JsonParserImpl
import io.getstream.chat.android.client.logger.StreamChatSilentLogger
import io.getstream.chat.android.client.logger.StreamLogger
import io.getstream.chat.android.client.observable.ChatObservable
import io.getstream.chat.android.client.requests.QueryUsers
import io.getstream.chat.android.client.rest.*
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.socket.ChatSocketImpl
import io.getstream.chat.android.client.socket.SocketListener

interface ChatClient {

    fun setUser(user: User, token: String)

    fun setUser(user: User)

    fun setGuestUser(user: User): ChatCall<TokenResponse>

    fun setAnonymousUser()

    fun disconnect()

    //region Events

    fun addSocketListener(listener: SocketListener)

    fun removeSocketListener(listener: SocketListener)

    fun events(): ChatObservable

    //endregion

    //region Users

    fun getUsers(query: QueryUsers): ChatCall<List<User>>

    fun addMembers(
        channelType: String,
        channelId: String,
        members: List<String>
    ): ChatCall<ChannelResponse>

    fun removeMembers(
        channelType: String,
        channelId: String,
        members: List<String>
    ): ChatCall<Channel>

    fun muteUser(targetId: String): ChatCall<MuteUserResponse>
    fun unMuteUser(targetId: String): ChatCall<MuteUserResponse>
    fun flag(targetId: String): ChatCall<FlagResponse>
    fun banUser(
        targetId: String,
        channelType: String,
        channelId: String,
        reason: String,
        timeout: Int
    ): ChatCall<CompletableResponse>

    fun unBanUser(
        targetId: String,
        channelType: String,
        channelId: String
    ): ChatCall<CompletableResponse>

    //endregion

    //region Api calls

    fun getState(): ClientState
    fun fromCurrentUser(entity: UserEntity): Boolean
    fun getUserId(): String
    fun getClientId(): String
    fun getDevices(): ChatCall<List<Device>>
    fun deleteDevice(deviceId: String): ChatCall<Unit>
    fun addDevice(request: AddDeviceRequest): ChatCall<Unit>
    fun searchMessages(request: SearchMessagesRequest): ChatCall<List<Message>>
    fun getReplies(messageId: String, limit: Int): ChatCall<List<Message>>
    fun getRepliesMore(messageId: String, firstId: String, limit: Int): ChatCall<List<Message>>
    fun getReactions(messageId: String, offset: Int, limit: Int): ChatCall<List<Reaction>>
    fun deleteReaction(messageId: String, reactionType: String): ChatCall<Message>
    fun sendAction(request: SendActionRequest): ChatCall<Message>
    fun deleteMessage(messageId: String): ChatCall<Message>
    fun getMessage(messageId: String): ChatCall<Message>
    fun sendMessage(channelType: String, channelId: String, message: Message): ChatCall<Message>
    fun updateMessage(message: Message): ChatCall<Message>

    fun queryChannel(
        channelType: String,
        channelId: String,
        request: ChannelQueryRequest
    ): ChatCall<Channel>

    fun markRead(channelType: String, channelId: String, messageId: String): ChatCall<Unit>
    fun showChannel(channelType: String, channelId: String): ChatCall<Unit>
    fun hideChannel(
        channelType: String,
        channelId: String,
        clearHistory: Boolean = false
    ): ChatCall<Unit>

    fun stopWatching(channelType: String, channelId: String): ChatCall<Unit>
    fun queryChannels(request: QueryChannelsRequest): ChatCall<List<Channel>>

    fun updateChannel(
        channelType: String,
        channelId: String,
        updateMessage: Message,
        channelExtraData: Map<String, Any> = emptyMap()
    ): ChatCall<Channel>

    fun rejectInvite(channelType: String, channelId: String): ChatCall<Channel>
    fun acceptInvite(channelType: String, channelId: String, message: String): ChatCall<Channel>
    fun markAllRead(): ChatCall<ChatEvent>
    fun deleteChannel(channelType: String, channelId: String): ChatCall<Channel>
    //endregion

    class Builder {

        private val parser = JsonParserImpl()
        private var logger: StreamLogger = StreamChatSilentLogger()

        private lateinit var config: ChatConfig

        fun logger(logger: StreamLogger): Builder {
            this.logger = logger
            return this
        }

        fun config(config: ChatConfig): Builder {
            this.config = config
            return this
        }

        internal fun build(): ChatClient {
            val socket = buildSocket(config, parser, logger)
            val api = buildApi(config, parser, logger)
            return ChatClientImpl(api, socket, config, logger)
        }

        private fun buildSocket(
            chatConfig: ChatConfig,
            parser: JsonParser,
            logger: StreamLogger
        ): ChatSocket {
            return ChatSocketImpl(
                chatConfig.apiKey,
                chatConfig.wssURL,
                chatConfig.tokenProvider,
                parser,
                logger
            )
        }

        private fun buildApi(
            chatConfig: ChatConfig,
            parser: JsonParser,
            logger: StreamLogger
        ): ChatApi {
            return ChatApiImpl(
                chatConfig,
                RetrofitClient.buildClient(
                    chatConfig,
                    parser,
                    chatConfig
                ).create(RetrofitApi::class.java),
                parser,
                logger
            )
        }
    }

    companion object {

        private lateinit var instance: ChatClient

        fun init(builder: Builder): ChatClient {
            instance = builder.build()
            return instance
        }

        fun instance(): ChatClient {
            return instance
        }
    }

}