package io.getstream.chat.android.client

import com.facebook.stetho.okhttp3.StethoInterceptor
import io.getstream.chat.android.client.api.*
import android.content.Context
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.api.ChatConfig
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.parser.JsonParser
import io.getstream.chat.android.client.parser.JsonParserImpl
import io.getstream.chat.android.client.logger.ChatSilentLogger
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.*
import io.getstream.chat.android.client.api.models.QueryUsers
import io.getstream.chat.android.client.api.models.*
import io.getstream.chat.android.client.notifications.DeviceRegisteredListener
import io.getstream.chat.android.client.notifications.NotificationConfig
import io.getstream.chat.android.client.notifications.ChatNotificationsManager
import io.getstream.chat.android.client.notifications.ChatNotificationsManagerImpl
import io.getstream.chat.android.client.notifications.options.NotificationOptions
import io.getstream.chat.android.client.notifications.options.ChatNotificationOptions
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

    fun setGuestUser(user: User): Call<TokenResponse>

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
    ): Call<String>

    fun deleteFile(channelType: String, channelId: String, url: String): Call<Unit>

    fun deleteImage(channelType: String, channelId: String, url: String): Call<Unit>

    //endregion

    //region Events

    fun addSocketListener(listener: SocketListener)

    fun removeSocketListener(listener: SocketListener)

    fun events(): ChatObservable

    //endregion

    //region Users

    fun getUsers(query: QueryUsers): Call<List<User>>

    fun addMembers(
        channelType: String,
        channelId: String,
        members: List<String>
    ): Call<ChannelResponse>

    fun removeMembers(
        channelType: String,
        channelId: String,
        members: List<String>
    ): Call<Channel>

    fun muteUser(targetId: String): Call<MuteUserResponse>
    fun unMuteUser(targetId: String): Call<MuteUserResponse>
    fun flag(targetId: String): Call<FlagResponse>
    fun banUser(
        targetId: String,
        channelType: String,
        channelId: String,
        reason: String,
        timeout: Int
    ): Call<CompletableResponse>

    fun unBanUser(
        targetId: String,
        channelType: String,
        channelId: String
    ): Call<CompletableResponse>

    //endregion

    //region Api calls

    fun getState(): ClientState
    fun fromCurrentUser(entity: UserEntity): Boolean
    fun getUserId(): String
    fun getClientId(): String
    fun getDevices(): Call<List<Device>>
    fun deleteDevice(deviceId: String): Call<Unit>
    fun addDevice(firebaseToken: String): Call<Unit>
    fun searchMessages(request: SearchMessagesRequest): Call<List<Message>>
    fun getReplies(messageId: String, limit: Int): Call<List<Message>>
    fun getRepliesMore(messageId: String, firstId: String, limit: Int): Call<List<Message>>
    fun getReactions(messageId: String, offset: Int, limit: Int): Call<List<Reaction>>
    fun deleteReaction(messageId: String, reactionType: String): Call<Message>
    fun sendAction(request: SendActionRequest): Call<Message>
    fun deleteMessage(messageId: String): Call<Message>
    fun getMessage(messageId: String): Call<Message>
    fun sendMessage(channelType: String, channelId: String, message: Message): Call<Message>
    fun updateMessage(message: Message): Call<Message>

    fun queryChannel(
        channelType: String,
        channelId: String,
        request: ChannelQueryRequest
    ): Call<Channel>

    fun markRead(channelType: String, channelId: String, messageId: String): Call<Unit>
    fun showChannel(channelType: String, channelId: String): Call<Unit>
    fun hideChannel(
        channelType: String,
        channelId: String,
        clearHistory: Boolean = false
    ): Call<Unit>

    fun stopWatching(channelType: String, channelId: String): Call<Unit>
    fun queryChannels(request: QueryChannelsRequest): Call<List<Channel>>

    fun updateChannel(
        channelType: String,
        channelId: String,
        updateMessage: Message,
        channelExtraData: Map<String, Any> = emptyMap()
    ): Call<Channel>

    fun rejectInvite(channelType: String, channelId: String): Call<Channel>
    fun acceptInvite(channelType: String, channelId: String, message: String): Call<Channel>
    fun markAllRead(): Call<ChatEvent>
    fun deleteChannel(channelType: String, channelId: String): Call<Channel>
    //endregion

    // region messages
    fun onMessageReceived(remoteMessage: RemoteMessage, context: Context)
    fun onNewTokenReceived(token: String, context: Context)
    //endregion

    class Builder {

        private val parser =
            JsonParserImpl()
        private var logger: ChatLogger = ChatSilentLogger()
        private var notificationConfig: NotificationConfig = NotificationConfig(
            notificationOptions = ChatNotificationOptions(),
            deviceRegisteredListener = null,
            messageListener = null
        )

        private lateinit var config: ChatConfig

        fun notification(notificationConfig: NotificationConfig): Builder {
            this.notificationConfig = notificationConfig
            return this
        }

        fun logger(logger: ChatLogger): Builder {
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
            logger: ChatLogger
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
            logger: ChatLogger
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