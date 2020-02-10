package io.getstream.chat.android.client

import android.content.Context
import android.text.TextUtils
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.ChatConfig
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.notifications.ChatNotificationsManager
import io.getstream.chat.android.client.models.*
import io.getstream.chat.android.client.api.models.QueryUsers
import io.getstream.chat.android.client.api.models.*
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.socket.SocketListener
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.observable.ChatObservable
import java.io.File


internal class ChatClientImpl constructor(
    private val api: ChatApi,
    private val socket: ChatSocket,
    private val config: ChatConfig,
    private val logger: ChatLogger,
    private val notificationsManager: ChatNotificationsManager
) : ChatClient {

    private val state = ClientState()

    init {
        val events = socket.events()
        events.subscribe {
            if (it is ConnectedEvent) {
                state.user = it.me
                state.connectionId = it.connectionId
                api.setConnection(it.me.id, it.connectionId)
            } else if (it is DisconnectedEvent) {
                state.user = null
                state.connectionId = null
            }
        }
    }

    //region Set user

    override fun setUser(user: User) {
        config.isAnonymous = false
        socket.connect(user)
    }

    override fun setAnonymousUser() {
        config.isAnonymous = true
        socket.connectAnonymously()
    }

    override fun setGuestUser(user: User): Call<TokenResponse> {
        config.isAnonymous = true
        return api.setGuestUser(user.id, user.name)
    }

    override fun sendFile(
        channelType: String,
        channelId: String,
        file: File,
        mimeType: String,
        callback: ProgressCallback
    ) {
        api.sendFile(channelType, channelId, file, mimeType, callback)
    }

    override fun sendFile(
        channelType: String,
        channelId: String,
        file: File,
        mimeType: String
    ): Call<String> {
        return api.sendFile(channelType, channelId, file, mimeType)
    }

    override fun deleteFile(channelType: String, channelId: String, url: String): Call<Unit> {
        return api.deleteFile(channelType, channelId, url)
    }

    override fun deleteImage(channelType: String, channelId: String, url: String): Call<Unit> {
        return api.deleteImage(channelType, channelId, url)
    }

    //endregion

    override fun addSocketListener(listener: SocketListener) {
        socket.addListener(listener)
    }

    override fun removeSocketListener(listener: SocketListener) {
        socket.removeListener(listener)
    }

    override fun events(): ChatObservable {
        return socket.events()
    }

    override fun getState(): ClientState {
        return state
    }

    override fun fromCurrentUser(entity: UserEntity): Boolean {
        val otherUserId = entity.getUserId()
        return if (getUser() == null) false else TextUtils.equals(getUserId(), otherUserId)
    }

    override fun getUserId(): String {
        return state.user?.id!!
    }

    override fun getClientId(): String {
        return state.connectionId!!
    }

    override fun disconnect() {
        socket.disconnect()
        state.reset()
    }

    //region: api calls

    override fun getDevices(): Call<List<Device>> {
        return api.getDevices()
    }

    override fun deleteDevice(firebaseToken: String): Call<Unit> {
        return api.deleteDevice(firebaseToken)
    }

    override fun addDevice(firebaseToken: String): Call<Unit> {
        return api.addDevice(firebaseToken)
    }

    override fun searchMessages(request: SearchMessagesRequest): Call<List<Message>> {
        return api.searchMessages(request)
    }

    override fun getReplies(messageId: String, limit: Int): Call<List<Message>> {
        return api.getReplies(messageId, limit)
    }

    override fun getRepliesMore(
        messageId: String,
        firstId: String,
        limit: Int
    ): Call<List<Message>> {
        return api.getRepliesMore(messageId, firstId, limit)
    }

    override fun getReactions(
        messageId: String,
        offset: Int,
        limit: Int
    ): Call<List<Reaction>> {
        return api.getReactions(messageId, offset, limit)
    }

    override fun deleteReaction(messageId: String, reactionType: String): Call<Message> {
        return api.deleteReaction(messageId, reactionType)
    }

    override fun sendAction(request: SendActionRequest): Call<Message> {
        return api.sendAction(request)
    }

    override fun deleteMessage(messageId: String): Call<Message> {
        return api.deleteMessage(messageId)
    }

    override fun getMessage(messageId: String): Call<Message> {
        return api.getMessage(messageId)
    }

    override fun sendMessage(
        channelType: String,
        channelId: String,
        message: Message
    ): Call<Message> {
        return api.sendMessage(channelType, channelId, message)
    }

    override fun updateMessage(
        message: Message
    ): Call<Message> {
        return api.updateMessage(message)
    }

    override fun queryChannel(
        channelType: String,
        channelId: String,
        request: ChannelQueryRequest
    ): Call<Channel> {
        return api.queryChannel(channelType, channelId, request).map { attachClient(it) }
    }

    override fun deleteChannel(channelType: String, channelId: String): Call<Channel> {
        return api.deleteChannel(channelType, channelId)
    }

    override fun markRead(
        channelType: String,
        channelId: String,
        messageId: String
    ): Call<Unit> {
        return api.markRead(channelType, channelId, messageId)
    }

    override fun showChannel(channelType: String, channelId: String): Call<Unit> {
        return api.showChannel(channelType, channelId)
    }

    override fun hideChannel(
        channelType: String,
        channelId: String,
        clearHistory: Boolean
    ): Call<Unit> {
        return api.hideChannel(channelType, channelId, clearHistory)
    }

    override fun stopWatching(channelType: String, channelId: String): Call<Unit> {
        return api.stopWatching(channelType, channelId)
    }

    override fun queryChannels(
        request: QueryChannelsRequest
    ): Call<List<Channel>> {
        return api.queryChannels(request)
            .map { attachClient(it) }
    }

    override fun updateChannel(
        channelType: String,
        channelId: String,
        updateMessage: Message,
        channelExtraData: Map<String, Any>
    ): Call<Channel> {

        val toMutableMap = channelExtraData.toMutableMap()
        toMutableMap.remove("members")

        val request = UpdateChannelRequest(channelExtraData, updateMessage)
        return api.updateChannel(channelType, channelId, request)
            .map { attachClient(it) }
    }

    override fun rejectInvite(channelType: String, channelId: String): Call<Channel> {
        return api.rejectInvite(channelType, channelId).map { attachClient(it) }
    }

    override fun acceptInvite(
        channelType: String,
        channelId: String,
        message: String
    ): Call<Channel> {
        return api.acceptInvite(channelType, channelId, message).map { attachClient(it) }
    }

    override fun markAllRead(): Call<ChatEvent> {
        return api.markAllRead().map {
            it.event
        }
    }

    override fun getUsers(query: QueryUsers): Call<List<User>> {
        return api.getUsers(query).map { it.users }
    }

    override fun addMembers(
        channelType: String,
        channelId: String,
        members: List<String>
    ): Call<ChannelResponse> {
        return api.addMembers(
            channelType = channelType,
            channelId = channelId,
            members = members
        )
    }

    override fun removeMembers(
        channelType: String,
        channelId: String,
        members: List<String>
    ) = api.removeMembers(
        channelType,
        channelId,
        members
    ).map {
        it.channel
    }

    override fun muteUser(targetId: String) = api.muteUser(
        targetId = targetId
    )

    override fun unMuteUser(targetId: String) = api.unMuteUser(
        targetId = targetId
    )

    override fun flag(targetId: String) = api.flag(
        targetId = targetId
    )

    override fun banUser(
        targetId: String,
        channelType: String,
        channelId: String,
        reason: String,
        timeout: Int
    ): Call<CompletableResponse> = api.banUser(
        targetId, timeout, reason, channelType, channelId
    )

    override fun unBanUser(
        targetId: String,
        channelType: String,
        channelId: String
    ) = api.unBanUser(
        targetId = targetId,
        channelType = channelType,
        channelId = channelId
    )

    //endregion

    override fun onMessageReceived(remoteMessage: RemoteMessage, context: Context) {
        notificationsManager.onReceiveFirebaseMessage(remoteMessage, context)
    }

    override fun onNewTokenReceived(token: String, context: Context) {
        notificationsManager.setFirebaseToken(token, context)
    }

    private fun attachClient(channels: List<Channel>): List<Channel> {
        channels.forEach { attachClient(it) }
        return channels
    }

    private fun attachClient(channel: Channel): Channel {
        channel.client = this
        return channel
    }

    private fun getUser(): User? {
        return state.user
    }
}