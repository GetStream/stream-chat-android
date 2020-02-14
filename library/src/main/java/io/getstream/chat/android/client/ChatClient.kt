package io.getstream.chat.android.client

import android.content.Context
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.api.ChatConfig
import io.getstream.chat.android.client.api.models.*
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.*
import io.getstream.chat.android.client.socket.SocketListener
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.observable.ChatObservable
import java.io.File

interface ChatClient {

    fun setUser(user: User)

    fun setAnonymousUser()

    fun getGuestToken(userId: String, userName: String): Call<TokenResponse>

    fun disconnect()

    fun disconnectSocket()

    fun reconnectSocket()

    fun isSocketConnected(): Boolean

    fun getConnectionId(): String?

    fun getCurrentUser(): User?

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

    fun getUsers(query: QueryUsersRequest): Call<List<User>>

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


    companion object {

        private lateinit var instance: ChatClient

        fun init(config: ChatConfig): ChatClient {

            instance = ChatClientImpl(
                config,
                config.modules.api(),
                config.modules.socket(),
                config.modules.logger(),
                config.modules.notifications()
            )
            return instance
        }

        fun instance(): ChatClient {
            return instance
        }


    }

}