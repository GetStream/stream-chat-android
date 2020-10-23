package io.getstream.chat.android.client

import android.content.Context
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.api.models.SearchMessagesRequest
import io.getstream.chat.android.client.api.models.SendActionRequest
import io.getstream.chat.android.client.api.models.UpdateChannelRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.controllers.ChannelController
import io.getstream.chat.android.client.controllers.ChannelControllerImpl
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.ErrorEvent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.GuestUser
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.ModelFields
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.notifications.ChatNotifications
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.socket.InitConnectionListener
import io.getstream.chat.android.client.socket.SocketListener
import io.getstream.chat.android.client.token.TokenProvider
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.client.utils.ImmediateTokenProvider
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.observable.ChatEventsObservable
import io.getstream.chat.android.client.utils.observable.ChatObservable
import io.getstream.chat.android.client.utils.observable.Disposable
import java.io.File
import java.util.Date

internal class ChatClientImpl(
    private val config: ChatClientConfig,
    private val api: ChatApi,
    private val socket: ChatSocket,
    private val notifications: ChatNotifications
) : ChatClient {

    private val state = ClientState()
    private var connectionListener: InitConnectionListener? = null
    private val logger = ChatLogger.get("Client")
    private val eventsObservable = ChatEventsObservable(socket)

    init {
        eventsObservable.subscribe { event ->

            notifications.onChatEvent(event)

            when (event) {
                is ConnectedEvent -> {

                    val user = event.me
                    val connectionId = event.connectionId

                    state.user = user
                    state.connectionId = connectionId
                    state.socketConnected = true
                    api.setConnection(user.id, connectionId)
                    callConnectionListener(event, null)
                }
                is ErrorEvent -> {
                    callConnectionListener(null, event.error)
                }
                is DisconnectedEvent -> {
                    state.socketConnected = false
                }
            }
        }

        logger.logI("Initialised: " + getVersion())
    }

    //region Set user

    override fun setUser(user: User, token: String, listener: InitConnectionListener?) {
        setUser(user, ImmediateTokenProvider(token), listener)
    }

    override fun setUser(
        user: User,
        tokenProvider: TokenProvider,
        listener: InitConnectionListener?
    ) {
        if (!ensureUserNotSet(listener)) {
            return
        }
        connectionListener = listener
        config.isAnonymous = false
        config.tokenManager.setTokenProvider(tokenProvider)
        warmUp()
        notifications.onSetUser()
        getTokenAndConnect {
            socket.connect(user)
        }
    }

    override fun setAnonymousUser(listener: InitConnectionListener?) {
        connectionListener = listener
        config.isAnonymous = true
        warmUp()
        getTokenAndConnect {
            socket.connectAnonymously()
        }
    }

    override fun getGuestToken(userId: String, userName: String): Call<GuestUser> {
        config.isAnonymous = true
        return api.getGuestUser(userId, userName)
    }

    override fun sendFile(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback
    ) {
        api.sendFile(channelType, channelId, file, callback)
    }

    override fun sendImage(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback
    ) {
        api.sendImage(channelType, channelId, file, callback)
    }

    override fun sendFile(
        channelType: String,
        channelId: String,
        file: File
    ): Call<String> {
        return api.sendFile(channelType, channelId, file)
    }

    override fun queryMembers(
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySort,
        members: List<Member>
    ): Call<List<Member>> {
        return api.queryMembers(channelType, channelId, offset, limit, filter, sort, members)
    }

    override fun sendImage(
        channelType: String,
        channelId: String,
        file: File
    ): Call<String> {
        return api.sendImage(channelType, channelId, file)
    }

    override fun deleteFile(channelType: String, channelId: String, url: String): Call<Unit> {
        return api.deleteFile(channelType, channelId, url)
    }

    override fun deleteImage(channelType: String, channelId: String, url: String): Call<Unit> {
        return api.deleteImage(channelType, channelId, url)
    }

    override fun replayEvents(
        channelIds: List<String>,
        since: Date?,
        limit: Int,
        offset: Int
    ): Call<List<ChatEvent>> {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    //region Reactions
    override fun getReactions(
        messageId: String,
        offset: Int,
        limit: Int
    ): Call<List<Reaction>> {
        return api.getReactions(messageId, offset, limit)
    }

    override fun sendReaction(messageId: String, reactionType: String): Call<Reaction> {
        return api.sendReaction(messageId, reactionType)
    }

    override fun deleteReaction(messageId: String, reactionType: String): Call<Message> {
        return api.deleteReaction(messageId, reactionType)
    }

    override fun sendReaction(reaction: Reaction): Call<Reaction> {
        return api.sendReaction(reaction)
    }
    //endregion

    //endregion

    override fun disconnectSocket() {
        socket.disconnect()
    }

    override fun reconnectSocket() {
        val user = state.user
        if (user != null) socket.connect(user)
    }

    override fun addSocketListener(listener: SocketListener) {
        socket.addListener(listener)
    }

    override fun removeSocketListener(listener: SocketListener) {
        socket.removeListener(listener)
    }

    override fun events(): ChatObservable {
        return socket.events()
    }

    override fun subscribe(
        listener: (event: ChatEvent) -> Unit
    ): Disposable {
        return eventsObservable.subscribe(listener = listener)
    }

    override fun subscribeFor(
        vararg eventTypes: String,
        listener: (event: ChatEvent) -> Unit
    ): Disposable {
        val filter = { event: ChatEvent ->
            event.type in eventTypes
        }
        return eventsObservable.subscribe(filter, listener)
    }

    override fun subscribeFor(
        vararg eventTypes: Class<out ChatEvent>,
        listener: (event: ChatEvent) -> Unit
    ): Disposable {
        val filter = { event: ChatEvent ->
            eventTypes.any { type -> type.isInstance(event) }
        }
        return eventsObservable.subscribe(filter, listener)
    }

    override fun subscribeForSingle(
        eventType: String,
        listener: (event: ChatEvent) -> Unit
    ): Disposable {
        val filter = { event: ChatEvent ->
            event.type == eventType
        }
        return eventsObservable.subscribeSingle(filter, listener)
    }

    override fun <T : ChatEvent> subscribeForSingle(
        eventType: Class<T>,
        listener: (event: T) -> Unit
    ): Disposable {
        val filter = { event: ChatEvent ->
            eventType.isInstance(event)
        }
        return eventsObservable.subscribeSingle(filter) { event: ChatEvent ->
            @Suppress("UNCHECKED_CAST")
            listener.invoke(event as T)
        }
    }

    override fun disconnect() {
        connectionListener = null
        socket.disconnect()
        state.reset()
    }

    //region: api calls

    override fun getDevices(): Call<List<Device>> {
        return api.getDevices()
    }

    override fun deleteDevice(deviceId: String): Call<Unit> {
        return api.deleteDevice(deviceId)
    }

    override fun addDevice(deviceId: String): Call<Unit> {
        return api.addDevice(deviceId)
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
        request: QueryChannelRequest
    ): Call<Channel> {
        // for convenience we add the message.cid field
        return api.queryChannel(channelType, channelId, request)
            .map { channel ->
                channel.messages.forEach { message -> message.cid = channel.cid }
                channel
            }
    }

    override fun queryChannels(request: QueryChannelsRequest): Call<List<Channel>> {
        return api.queryChannels(request).map { channels ->
            channels.map { channel -> channel.messages.forEach { message -> message.cid = channel.cid } }
            channels
        }
    }

    override fun deleteChannel(channelType: String, channelId: String): Call<Channel> {
        return api.deleteChannel(channelType, channelId)
    }

    override fun markMessageRead(
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

    override fun updateChannel(
        channelType: String,
        channelId: String,
        updateMessage: Message?,
        channelExtraData: Map<String, Any>
    ): Call<Channel> =
        api.updateChannel(
            channelType,
            channelId,
            UpdateChannelRequest(channelExtraData, updateMessage)
        )

    override fun enableSlowMode(
        channelType: String,
        channelId: String,
        cooldownTimeInSeconds: Int
    ): Call<Channel> =
        api.enableSlowMode(channelType, channelId, cooldownTimeInSeconds)

    override fun disableSlowMode(
        channelType: String,
        channelId: String
    ): Call<Channel> =
        api.disableSlowMode(channelType, channelId)

    override fun rejectInvite(channelType: String, channelId: String): Call<Channel> {
        return api.rejectInvite(channelType, channelId)
    }

    override fun sendEvent(
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>
    ): Call<ChatEvent> {
        return api.sendEvent(eventType, channelType, channelId, extraData)
    }

    override fun getVersion(): String {
        return BuildConfig.VERSION_NAME + "-" + BuildConfig.BUILD_TYPE
    }

    override fun acceptInvite(
        channelType: String,
        channelId: String,
        message: String
    ): Call<Channel> {
        return api.acceptInvite(channelType, channelId, message)
    }

    override fun markAllRead(): Call<Unit> {
        return api.markAllRead()
    }

    override fun markRead(channelType: String, channelId: String): Call<Unit> {
        return api.markRead(channelType, channelId)
    }

    override fun updateUsers(users: List<User>): Call<List<User>> {
        return api.updateUsers(users)
    }

    override fun updateUser(user: User): Call<User> {
        return updateUsers(listOf(user)).map { it.first() }
    }

    override fun queryUsers(query: QueryUsersRequest): Call<List<User>> {
        return api.queryUsers(query)
    }

    override fun addMembers(
        channelType: String,
        channelId: String,
        members: List<String>
    ): Call<Channel> {
        return api.addMembers(
            channelType,
            channelId,
            members
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
    )

    override fun muteUser(userId: String) = api.muteUser(userId)

    override fun muteChannel(channelType: String, channelId: String): Call<Unit> {
        return api.muteChannel(channelType, channelId)
    }

    override fun unMuteChannel(channelType: String, channelId: String): Call<Unit> {
        return api.unMuteChannel(channelType, channelId)
    }

    override fun unmuteUser(userId: String) = api.unmuteUser(userId)

    override fun unmuteCurrentUser(): Call<Mute> = api.unmuteCurrentUser()

    override fun muteCurrentUser(): Call<Mute> = api.muteCurrentUser()

    override fun flag(userId: String) = flagUser(userId)

    override fun flagUser(userId: String) = api.flagUser(userId)

    override fun flagMessage(messageId: String) = api.flagMessage(messageId)

    override fun translate(messageId: String, language: String) = api.translate(messageId, language)

    override fun banUser(
        targetId: String,
        channelType: String,
        channelId: String,
        reason: String,
        timeout: Int
    ): Call<Unit> = api.banUser(
        targetId,
        timeout,
        reason,
        channelType,
        channelId
    ).map {
        Unit
    }

    override fun unBanUser(
        targetId: String,
        channelType: String,
        channelId: String
    ) = api.unBanUser(
        targetId,
        channelType,
        channelId
    ).map {
        Unit
    }

    //endregion

    override fun onMessageReceived(remoteMessage: RemoteMessage, context: Context) {
        notifications.onFirebaseMessage(remoteMessage)
    }

    override fun onNewTokenReceived(token: String, context: Context) {
        notifications.setFirebaseToken(token)
    }

    override fun getConnectionId(): String? {
        return state.connectionId
    }

    override fun getCurrentUser(): User? {
        return state.user
    }

    override fun isSocketConnected(): Boolean {
        return state.socketConnected
    }

    override fun channel(channelType: String, channelId: String): ChannelController {
        return ChannelControllerImpl(channelType, channelId, this)
    }

    override fun channel(cid: String): ChannelController {
        val type = cid.split(":")[0]
        val id = cid.split(":")[1]
        return channel(type, id)
    }

    override fun createChannel(
        channelType: String,
        channelId: String,
        extraData: Map<String, Any>
    ): Call<Channel> =
        createChannel(channelType, channelId, emptyList(), extraData)

    override fun createChannel(
        channelType: String,
        channelId: String,
        members: List<String>
    ): Call<Channel> =
        createChannel(channelType, channelId, members, emptyMap())

    override fun createChannel(channelType: String, members: List<String>): Call<Channel> =
        createChannel(channelType, "", members, emptyMap())

    override fun createChannel(
        channelType: String,
        members: List<String>,
        extraData: Map<String, Any>
    ): Call<Channel> =
        createChannel(channelType, "", members, extraData)

    override fun createChannel(
        channelType: String,
        channelId: String,
        members: List<String>,
        extraData: Map<String, Any>
    ): Call<Channel> =
        queryChannel(
            channelType,
            channelId,
            QueryChannelRequest().withData(extraData + mapOf(ModelFields.MEMBERS to members))
        )

    override fun getSyncHistory(
        channelsIds: List<String>,
        lastSyncAt: Date
    ): Call<List<ChatEvent>> {
        return api.getSyncHistory(channelsIds, lastSyncAt)
    }

    private fun callConnectionListener(connectedEvent: ConnectedEvent?, error: ChatError?) {
        if (connectedEvent != null) {
            val user = connectedEvent.me
            val connectionId = connectedEvent.connectionId
            connectionListener?.onSuccess(InitConnectionListener.ConnectionData(user, connectionId))
        } else if (error != null) {
            connectionListener?.onError(error)
        }
        connectionListener = null
    }

    private fun getTokenAndConnect(connect: () -> Unit) {
        config.tokenManager.loadAsync {
            connect()
        }
    }

    private fun warmUp() {
        if (config.warmUp) {
            api.warmUp()
        }
    }

    private fun ensureUserNotSet(listener: InitConnectionListener?): Boolean {
        return if (state.user != null) {
            logger.logE("Trying to set user without disconnecting the previous one - make sure that previously set user is disconnected.")
            listener?.onError(ChatError("User cannot be set until previous one is disconnected."))
            false
        } else {
            true
        }
    }
}
