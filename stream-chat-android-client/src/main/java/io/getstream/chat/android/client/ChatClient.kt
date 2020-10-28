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
import io.getstream.chat.android.client.di.ChatModule
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
import io.getstream.chat.android.client.uploader.FileUploader
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.observable.ChatObservable
import io.getstream.chat.android.client.utils.observable.Disposable
import java.io.File
import java.util.Date

public interface ChatClient {

    public fun setUser(user: User, token: String, listener: InitConnectionListener? = null)

    public fun setUser(
        user: User,
        tokenProvider: TokenProvider,
        listener: InitConnectionListener? = null
    )

    public fun setAnonymousUser(listener: InitConnectionListener? = null)

    public fun getGuestToken(userId: String, userName: String): Call<GuestUser>

    public fun disconnect()

    public fun disconnectSocket()

    public fun reconnectSocket()

    public fun isSocketConnected(): Boolean

    public fun getConnectionId(): String?

    public fun getCurrentUser(): User?

    public fun channel(cid: String): ChannelController

    public fun channel(channelType: String, channelId: String): ChannelController

    public fun createChannel(
        channelType: String,
        channelId: String,
        members: List<String>
    ): Call<Channel>

    public fun createChannel(channelType: String, members: List<String>): Call<Channel>

    public fun createChannel(
        channelType: String,
        members: List<String>,
        extraData: Map<String, Any>
    ): Call<Channel>

    public fun createChannel(
        channelType: String,
        channelId: String,
        members: List<String>,
        extraData: Map<String, Any>
    ): Call<Channel>

    public fun createChannel(
        channelType: String,
        channelId: String,
        extraData: Map<String, Any>
    ): Call<Channel>

    public fun muteChannel(channelType: String, channelId: String): Call<Unit>

    public fun unMuteChannel(channelType: String, channelId: String): Call<Unit>

    //region CDN

    public fun sendFile(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback
    )

    public fun sendImage(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback
    )

    public fun sendFile(
        channelType: String,
        channelId: String,
        file: File
    ): Call<String>

    public fun sendImage(
        channelType: String,
        channelId: String,
        file: File
    ): Call<String>

    public fun deleteFile(channelType: String, channelId: String, url: String): Call<Unit>

    public fun deleteImage(channelType: String, channelId: String, url: String): Call<Unit>

    public fun replayEvents(
        channelIds: List<String>,
        since: Date?,
        limit: Int = 100,
        offset: Int = 0
    ): Call<List<ChatEvent>>

    //endregion

    //region Events

    public fun addSocketListener(listener: SocketListener)

    public fun removeSocketListener(listener: SocketListener)

    @Deprecated(
        message = "Use subscribe() on the client directly instead",
        level = DeprecationLevel.WARNING
    )
    public fun events(): ChatObservable

    public fun subscribe(listener: (event: ChatEvent) -> Unit): Disposable

    public fun subscribeFor(
        vararg eventTypes: String,
        listener: (event: ChatEvent) -> Unit
    ): Disposable

    public fun subscribeFor(
        vararg eventTypes: Class<out ChatEvent>,
        listener: (event: ChatEvent) -> Unit
    ): Disposable

    public fun subscribeForSingle(
        eventType: String,
        listener: (event: ChatEvent) -> Unit
    ): Disposable

    public fun <T : ChatEvent> subscribeForSingle(
        eventType: Class<T>,
        listener: (event: T) -> Unit
    ): Disposable

    //endregion

    //region Users

    public fun updateUser(user: User): Call<User>

    public fun updateUsers(users: List<User>): Call<List<User>>

    public fun queryUsers(query: QueryUsersRequest): Call<List<User>>

    public fun addMembers(
        channelType: String,
        channelId: String,
        members: List<String>
    ): Call<Channel>

    public fun removeMembers(
        channelType: String,
        channelId: String,
        members: List<String>
    ): Call<Channel>

    public fun queryMembers(
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySort = QuerySort(),
        members: List<Member> = emptyList()
    ): Call<List<Member>>

    public fun muteUser(userId: String): Call<Mute>
    public fun muteCurrentUser(): Call<Mute>
    public fun unmuteUser(userId: String): Call<Mute>
    public fun unmuteCurrentUser(): Call<Mute>

    @Deprecated(
        message = "We are going to replace with flagUser()",
        replaceWith = ReplaceWith("this.flagUser(userId)")
    )
    public fun flag(userId: String): Call<Flag>
    public fun flagUser(userId: String): Call<Flag>
    public fun flagMessage(messageId: String): Call<Flag>
    public fun banUser(
        targetId: String,
        channelType: String,
        channelId: String,
        reason: String,
        timeout: Int
    ): Call<Unit>

    public fun unBanUser(
        targetId: String,
        channelType: String,
        channelId: String
    ): Call<Unit>

    //region Reactions
    public fun getReactions(messageId: String, offset: Int, limit: Int): Call<List<Reaction>>

    public fun sendReaction(messageId: String, reactionType: String): Call<Reaction>
    public fun sendReaction(reaction: Reaction): Call<Reaction>
    public fun deleteReaction(messageId: String, reactionType: String): Call<Message>
    //endregion

    //endregion

    //region Api calls

    public fun getDevices(): Call<List<Device>>
    public fun deleteDevice(deviceId: String): Call<Unit>
    public fun addDevice(deviceId: String): Call<Unit>
    public fun searchMessages(request: SearchMessagesRequest): Call<List<Message>>
    public fun getReplies(messageId: String, limit: Int): Call<List<Message>>
    public fun getRepliesMore(messageId: String, firstId: String, limit: Int): Call<List<Message>>
    public fun sendAction(request: SendActionRequest): Call<Message>
    public fun deleteMessage(messageId: String): Call<Message>
    public fun getMessage(messageId: String): Call<Message>
    public fun sendMessage(channelType: String, channelId: String, message: Message): Call<Message>
    public fun updateMessage(message: Message): Call<Message>

    public fun queryChannel(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest
    ): Call<Channel>

    public fun markMessageRead(
        channelType: String,
        channelId: String,
        messageId: String
    ): Call<Unit>

    public fun showChannel(channelType: String, channelId: String): Call<Unit>
    public fun hideChannel(
        channelType: String,
        channelId: String,
        clearHistory: Boolean = false
    ): Call<Unit>

    public fun stopWatching(channelType: String, channelId: String): Call<Unit>
    public fun queryChannels(request: QueryChannelsRequest): Call<List<Channel>>

    public fun updateChannel(
        channelType: String,
        channelId: String,
        updateMessage: Message? = null,
        channelExtraData: Map<String, Any> = emptyMap()
    ): Call<Channel>

    public fun enableSlowMode(
        channelType: String,
        channelId: String,
        cooldownTimeInSeconds: Int
    ): Call<Channel>

    public fun disableSlowMode(
        channelType: String,
        channelId: String
    ): Call<Channel>

    public fun rejectInvite(channelType: String, channelId: String): Call<Channel>
    public fun acceptInvite(channelType: String, channelId: String, message: String): Call<Channel>
    public fun markRead(channelType: String, channelId: String): Call<Unit>
    public fun markAllRead(): Call<Unit>
    public fun deleteChannel(channelType: String, channelId: String): Call<Channel>
    //endregion

    // region messages
    public fun onMessageReceived(remoteMessage: RemoteMessage, context: Context)
    public fun onNewTokenReceived(token: String, context: Context)
    //endregion

    public fun sendEvent(
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any> = emptyMap()
    ): Call<ChatEvent>

    public fun translate(messageId: String, language: String): Call<Message>

    public fun getSyncHistory(channelsIds: List<String>, lastSyncAt: Date): Call<List<ChatEvent>>

    public fun getVersion(): String

    public class Builder(private val apiKey: String, private val appContext: Context) {

        private var baseUrl: String = "chat-us-east-1.stream-io-api.com"
        private var cdnUrl: String = baseUrl
        private var baseTimeout = 10000L
        private var cdnTimeout = 10000L
        private var logLevel = ChatLogLevel.ALL
        private var warmUp: Boolean = true
        private var loggerHandler: ChatLoggerHandler? = null
        private var notificationsHandler: ChatNotificationHandler =
            ChatNotificationHandler(appContext)
        private var fileUploader: FileUploader? = null

        public fun logLevel(level: ChatLogLevel): Builder {
            logLevel = level
            return this
        }

        public fun logLevel(level: String): Builder {
            logLevel = ChatLogLevel.valueOf(level)
            return this
        }

        public fun loggerHandler(loggerHandler: ChatLoggerHandler): Builder {
            this.loggerHandler = loggerHandler
            return this
        }

        public fun notifications(notificationsHandler: ChatNotificationHandler): Builder {
            this.notificationsHandler = notificationsHandler
            return this
        }

        public fun fileUploader(fileUploader: FileUploader): Builder {
            this.fileUploader = fileUploader
            return this
        }

        public fun baseTimeout(timeout: Long): Builder {
            baseTimeout = timeout
            return this
        }

        public fun cdnTimeout(timeout: Long): Builder {
            cdnTimeout = timeout
            return this
        }

        public fun disableWarmUp(): Builder = apply {
            warmUp = false
        }

        public fun baseUrl(value: String): Builder {
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

        public fun cdnUrl(value: String): Builder {
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

        public fun build(): ChatClient {

            if (apiKey.isEmpty()) {
                throw IllegalStateException("apiKey is not defined in " + this::class.java.simpleName)
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
                notificationsHandler,
                fileUploader
            )

            val module = ChatModule(appContext, config)

            val result = ChatClientImpl(
                config,
                module.api(),
                module.socket(),
                module.notifications()
            )
            instance = result

            return result
        }
    }

    public companion object {
        private var instance: ChatClient? = null

        @JvmStatic
        public fun instance(): ChatClient = instance
            ?: throw IllegalStateException("ChatClient.Builder::build() must be called before obtaining ChatClient instance")

        public val isInitialized: Boolean
            get() = instance != null
    }
}
