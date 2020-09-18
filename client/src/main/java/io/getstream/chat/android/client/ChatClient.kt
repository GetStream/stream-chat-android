package io.getstream.chat.android.client

import android.content.Context
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.api.models.SearchMessagesRequest
import io.getstream.chat.android.client.api.models.SendActionRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.controllers.ChannelController
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.logger.ChatLoggerHandler
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.Flag
import io.getstream.chat.android.client.models.GuestUser
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler
import io.getstream.chat.android.client.socket.InitConnectionListener
import io.getstream.chat.android.client.socket.SocketListener
import io.getstream.chat.android.client.token.TokenProvider
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.observable.ChatObservable
import java.io.File
import java.util.Date

interface ChatClient {

    fun setUser(user: User, token: String, listener: InitConnectionListener? = null)

    fun setUser(user: User, tokenProvider: TokenProvider, listener: InitConnectionListener? = null)

    fun setAnonymousUser(listener: InitConnectionListener? = null)

    fun getGuestToken(userId: String, userName: String): Call<GuestUser>

    fun disconnect()

    fun disconnectSocket()

    fun reconnectSocket()

    fun isSocketConnected(): Boolean

    fun getConnectionId(): String?

    fun getCurrentUser(): User?

    fun channel(cid: String): ChannelController

    fun channel(channelType: String, channelId: String): ChannelController

    fun createChannel(channelType: String, channelId: String, members: List<String>): Call<Channel>

    fun createChannel(channelType: String, members: List<String>): Call<Channel>

    fun createChannel(channelType: String, members: List<String>, extraData: Map<String, Any>): Call<Channel>

    fun createChannel(
        channelType: String,
        channelId: String,
        members: List<String>,
        extraData: Map<String, Any>
    ): Call<Channel>

    fun createChannel(channelType: String, channelId: String, extraData: Map<String, Any>): Call<Channel>

    fun muteChannel(channelType: String, channelId: String): Call<Unit>

    fun unMuteChannel(channelType: String, channelId: String): Call<Unit>

    //region CDN

    fun sendFile(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback
    )

    fun sendImage(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback
    )

    fun sendFile(
        channelType: String,
        channelId: String,
        file: File
    ): Call<String>

    fun sendImage(
        channelType: String,
        channelId: String,
        file: File
    ): Call<String>

    fun deleteFile(channelType: String, channelId: String, url: String): Call<Unit>

    fun deleteImage(channelType: String, channelId: String, url: String): Call<Unit>

    fun replayEvents(channelIds: List<String>, since: Date?, limit: Int = 100, offset: Int = 0): Call<List<ChatEvent>>

    //endregion

    //region Events

    fun addSocketListener(listener: SocketListener)

    fun removeSocketListener(listener: SocketListener)

    fun events(): ChatObservable

    //endregion

    //region Users

    fun updateUser(user: User): Call<User>

    fun updateUsers(users: List<User>): Call<List<User>>

    fun queryUsers(query: QueryUsersRequest): Call<List<User>>

    fun addMembers(
        channelType: String,
        channelId: String,
        members: List<String>
    ): Call<Channel>

    fun removeMembers(
        channelType: String,
        channelId: String,
        members: List<String>
    ): Call<Channel>

    fun queryMembers(
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySort = QuerySort(),
        members: List<Member> = emptyList()
    ): Call<List<Member>>

    fun muteUser(userId: String): Call<Mute>
    fun muteCurrentUser(): Call<Mute>
    fun unmuteUser(userId: String): Call<Mute>
    fun unmuteCurrentUser(): Call<Mute>

    @Deprecated(
        message = "We are going to replace with flagUser()",
        replaceWith = ReplaceWith("this.flagUser(userId)")
    )
    fun flag(userId: String): Call<Flag>
    fun flagUser(userId: String): Call<Flag>
    fun flagMessage(messageId: String): Call<Flag>
    fun banUser(
        targetId: String,
        channelType: String,
        channelId: String,
        reason: String,
        timeout: Int
    ): Call<Unit>

    fun unBanUser(
        targetId: String,
        channelType: String,
        channelId: String
    ): Call<Unit>

    //region Reactions
    fun getReactions(messageId: String, offset: Int, limit: Int): Call<List<Reaction>>

    fun sendReaction(messageId: String, reactionType: String): Call<Reaction>
    fun sendReaction(reaction: Reaction): Call<Reaction>
    fun deleteReaction(messageId: String, reactionType: String): Call<Message>
    //endregion

    //endregion

    //region Api calls

    fun getDevices(): Call<List<Device>>
    fun deleteDevice(deviceId: String): Call<Unit>
    fun addDevice(deviceId: String): Call<Unit>
    fun searchMessages(request: SearchMessagesRequest): Call<List<Message>>
    fun getReplies(messageId: String, limit: Int): Call<List<Message>>
    fun getRepliesMore(messageId: String, firstId: String, limit: Int): Call<List<Message>>
    fun sendAction(request: SendActionRequest): Call<Message>
    fun deleteMessage(messageId: String): Call<Message>
    fun getMessage(messageId: String): Call<Message>
    fun sendMessage(channelType: String, channelId: String, message: Message): Call<Message>
    fun updateMessage(message: Message): Call<Message>

    fun queryChannel(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest
    ): Call<Channel>

    fun markMessageRead(channelType: String, channelId: String, messageId: String): Call<Unit>
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
        updateMessage: Message? = null,
        channelExtraData: Map<String, Any> = emptyMap()
    ): Call<Channel>

    fun rejectInvite(channelType: String, channelId: String): Call<Channel>
    fun acceptInvite(channelType: String, channelId: String, message: String): Call<Channel>
    fun markRead(channelType: String, channelId: String): Call<Unit>
    fun markAllRead(): Call<Unit>
    fun deleteChannel(channelType: String, channelId: String): Call<Channel>
    //endregion

    // region messages
    fun onMessageReceived(remoteMessage: RemoteMessage, context: Context)
    fun onNewTokenReceived(token: String, context: Context)
    //endregion

    fun sendEvent(
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any> = emptyMap()
    ): Call<ChatEvent>

    fun translate(messageId: String, language: String): Call<Message>

    fun getSyncHistory(channelsIds: List<String>, lastSyncAt: Date): Call<List<ChatEvent>>

    fun getVersion(): String

    class Builder {
        private val apiKey: String
        private val appContext: Context

        private var baseUrl: String = "chat-us-east-1.stream-io-api.com"
        private var cdnUrl: String = baseUrl
        private var baseTimeout = 10000L
        private var cdnTimeout = 10000L
        private var logLevel = ChatLogLevel.ALL
        private var warmUp: Boolean = true
        private var loggerHandler: ChatLoggerHandler? = null
        private var notificationsHandler: ChatNotificationHandler

        constructor(apiKey: String, appContext: Context) {
            this.apiKey = apiKey
            this.appContext = appContext
            this.notificationsHandler = ChatNotificationHandler(appContext)
        }

        fun logLevel(level: ChatLogLevel): Builder {
            logLevel = level
            return this
        }

        fun logLevel(level: String): Builder {
            logLevel = ChatLogLevel.valueOf(level)
            return this
        }

        fun loggerHandler(loggerHandler: ChatLoggerHandler): Builder {
            this.loggerHandler = loggerHandler
            return this
        }

        fun notifications(notificationsHandler: ChatNotificationHandler): Builder {
            this.notificationsHandler = notificationsHandler
            return this
        }

        fun baseTimeout(timeout: Long): Builder {
            baseTimeout = timeout
            return this
        }

        fun cdnTimeout(timeout: Long): Builder {
            cdnTimeout = timeout
            return this
        }

        fun disableWarmUp(): Builder = apply {
            warmUp = false
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

        fun build(): ChatClient {

            if (apiKey.isEmpty()) {
                throw IllegalArgumentException("apiKey is not defined in " + this::class.java.simpleName)
            }

            val config = ChatClientConfig(
                apiKey,
                "https://$baseUrl/",
                "https://$cdnUrl/",
                "wss://$baseUrl/",
                baseTimeout,
                cdnTimeout,
                warmUp,
                ChatLogger.Config(logLevel, loggerHandler),
                notificationsHandler
            )

            val modules = ChatModules(config)

            val result = ChatClientImpl(
                config,
                modules.api(),
                modules.socket(),
                modules.notifications()
            )
            instance = result

            return result
        }
    }

    companion object {

        private lateinit var instance: ChatClient

        @JvmStatic
        fun instance(): ChatClient {
            return instance
        }
    }
}
