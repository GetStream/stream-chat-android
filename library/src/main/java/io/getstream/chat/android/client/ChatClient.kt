package io.getstream.chat.android.client

import com.facebook.stetho.okhttp3.StethoInterceptor
import io.getstream.chat.android.client.api.*
import android.content.Context
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.api.ChatConfig
import io.getstream.chat.android.client.call.ChatCall
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.parser.JsonParser
import io.getstream.chat.android.client.parser.JsonParserImpl
import io.getstream.chat.android.client.logger.StreamChatSilentLogger
import io.getstream.chat.android.client.logger.StreamLogger
import io.getstream.chat.android.client.models.*
import io.getstream.chat.android.client.api.models.QueryUsers
import io.getstream.chat.android.client.api.models.*
import io.getstream.chat.android.client.notifications.DeviceRegisteredListener
import io.getstream.chat.android.client.notifications.NotificationConfig
import io.getstream.chat.android.client.notifications.ChatNotificationsManager
import io.getstream.chat.android.client.notifications.ChatNotificationsManagerImpl
import io.getstream.chat.android.client.notifications.options.NotificationOptions
import io.getstream.chat.android.client.notifications.options.StreamNotificationOptions
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.socket.ChatSocketImpl
import io.getstream.chat.android.client.socket.SocketListener
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.observable.ChatObservable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.io.File
import java.util.concurrent.TimeUnit

interface ChatClient {

    fun setUser(user: User, token: String)

    fun setUser(user: User)

    fun setGuestUser(user: User): ChatCall<TokenResponse>

    fun setAnonymousUser()

    fun disconnect()

    //region CDN

    fun sendFile(
        channelType: String,
        channelId: String,
        file: File,
        mimeType: String,
        callback: ProgressCallback
    )

    fun sendFile(
        channelType: String,
        channelId: String,
        file: File,
        mimeType: String
    ): ChatCall<String>

    fun deleteFile(channelType: String, channelId: String, url: String): ChatCall<Unit>

    fun deleteImage(channelType: String, channelId: String, url: String): ChatCall<Unit>

    //endregion

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
    fun addDevice(firebaseToken: String): ChatCall<Unit>
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

    // region messages
    fun onMessageReceived(remoteMessage: RemoteMessage, context: Context)
    fun onNewTokenReceived(token: String, context: Context)
    //endregion

    class Builder {

        private val parser =
            JsonParserImpl()
        private var logger: StreamLogger = StreamChatSilentLogger()
        private var notificationConfig: NotificationConfig = NotificationConfig(
            notificationOptions = StreamNotificationOptions(),
            deviceRegisteredListener = null,
            messageListener = null
        )

        private lateinit var config: ChatConfig

        fun notification(notificationConfig: NotificationConfig): Builder {
            this.notificationConfig = notificationConfig
            return this
        }

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
            val notificationsManager = buildNotificationManager(
                notificationOptions = notificationConfig.notificationOptions,
                registeredListener = notificationConfig.deviceRegisteredListener,
                client = api
            )
            return ChatClientImpl(api, socket, config, logger, notificationsManager)
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
                buildRetrofitApi(),
                buildRetrofitCdnApi(),
                chatConfig,
                parser,
                logger
            )
        }

        private fun buildNotificationManager(
            notificationOptions: NotificationOptions,
            registeredListener: DeviceRegisteredListener?,
            client: ChatApi
        ): ChatNotificationsManager {
            return ChatNotificationsManagerImpl(
                notificationOptions = notificationOptions,
                registerListener = registeredListener,
                client = client,
                logger = logger
            )
        }

        private fun buildRetrofit(
            endpoint: String,
            connectTimeout: Long,
            writeTimeout: Long,
            readTimeout: Long,
            config: ChatConfig,
            parser: JsonParser
        ): Retrofit {

            val clientBuilder = OkHttpClient.Builder()
                .followRedirects(false)
                // timeouts
                .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(writeTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
                // interceptors
                .addInterceptor(HeadersInterceptor(config))
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .addInterceptor(TokenAuthInterceptor(config, parser))
                .addNetworkInterceptor(StethoInterceptor())

            val builder = Retrofit.Builder()
                .baseUrl(endpoint)
                .client(clientBuilder.build())

            return parser.configRetrofit(builder).build()
        }

        private fun buildRetrofitApi(): RetrofitApi {
            return buildRetrofit(
                config.httpURL,
                config.baseTimeout.toLong(),
                config.baseTimeout.toLong(),
                config.baseTimeout.toLong(),
                config,
                parser
            ).create(RetrofitApi::class.java)
        }

        private fun buildRetrofitCdnApi(): RetrofitCdnApi {
            return buildRetrofit(
                config.cdnHttpURL,
                config.cdnTimeout.toLong(),
                config.cdnTimeout.toLong(),
                config.cdnTimeout.toLong(),
                config,
                parser
            ).create(RetrofitCdnApi::class.java)
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