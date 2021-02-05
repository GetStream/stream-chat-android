package io.getstream.chat.android.client.channel

import androidx.annotation.CheckResult
import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.ChatEventListener
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.SendActionRequest
import io.getstream.chat.android.client.api.models.WatchChannelRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.controllers.ChannelController
import io.getstream.chat.android.client.events.ChannelCreatedEvent
import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.ChannelHiddenEvent
import io.getstream.chat.android.client.events.ChannelMuteEvent
import io.getstream.chat.android.client.events.ChannelTruncatedEvent
import io.getstream.chat.android.client.events.ChannelUnmuteEvent
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.ChannelUserBannedEvent
import io.getstream.chat.android.client.events.ChannelUserUnbannedEvent
import io.getstream.chat.android.client.events.ChannelVisibleEvent
import io.getstream.chat.android.client.events.ChannelsMuteEvent
import io.getstream.chat.android.client.events.ChannelsUnmuteEvent
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.ConnectingEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.ErrorEvent
import io.getstream.chat.android.client.events.GlobalUserBannedEvent
import io.getstream.chat.android.client.events.GlobalUserUnbannedEvent
import io.getstream.chat.android.client.events.HealthEvent
import io.getstream.chat.android.client.events.MarkAllReadEvent
import io.getstream.chat.android.client.events.MemberAddedEvent
import io.getstream.chat.android.client.events.MemberRemovedEvent
import io.getstream.chat.android.client.events.MemberUpdatedEvent
import io.getstream.chat.android.client.events.MessageDeletedEvent
import io.getstream.chat.android.client.events.MessageReadEvent
import io.getstream.chat.android.client.events.MessageUpdatedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationChannelDeletedEvent
import io.getstream.chat.android.client.events.NotificationChannelMutesUpdatedEvent
import io.getstream.chat.android.client.events.NotificationChannelTruncatedEvent
import io.getstream.chat.android.client.events.NotificationInviteAcceptedEvent
import io.getstream.chat.android.client.events.NotificationInvitedEvent
import io.getstream.chat.android.client.events.NotificationMarkReadEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.NotificationMutesUpdatedEvent
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.events.ReactionDeletedEvent
import io.getstream.chat.android.client.events.ReactionNewEvent
import io.getstream.chat.android.client.events.ReactionUpdateEvent
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.TypingStopEvent
import io.getstream.chat.android.client.events.UnknownEvent
import io.getstream.chat.android.client.events.UserDeletedEvent
import io.getstream.chat.android.client.events.UserMutedEvent
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.events.UserUnmutedEvent
import io.getstream.chat.android.client.events.UserUpdatedEvent
import io.getstream.chat.android.client.events.UsersMutedEvent
import io.getstream.chat.android.client.events.UsersUnmutedEvent
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.observable.ChatObservable
import io.getstream.chat.android.client.utils.observable.Disposable
import java.io.File
import java.util.Date

public class ChannelClient internal constructor(
    override val channelType: String,
    override val channelId: String,
    private val client: ChatClient,
) : ChannelController {

    override val cid: String = "$channelType:$channelId"

    @CheckResult
    override fun create(members: List<String>, extraData: Map<String, Any>): Call<Channel> {
        return client.createChannel(channelType, channelId, members, extraData)
    }

    @CheckResult
    override fun create(extraData: Map<String, Any>): Call<Channel> {
        return client.createChannel(channelType, channelId, emptyList())
    }

    override fun events(): ChatObservable {
        return client.events().filter(this::isRelevantForChannel)
    }

    @Suppress("NEWER_VERSION_IN_SINCE_KOTLIN")
    @Deprecated("User subscribe with ChatEventListener")
    @SinceKotlin("99999.9")
    override fun subscribe(listener: (event: ChatEvent) -> Unit): Disposable {
        return client.subscribe(filterRelevantEvents(listener))
    }

    public fun subscribe(listener: ChatEventListener<ChatEvent>): Disposable {
        return client.subscribe(filterRelevantEvents(listener))
    }

    @Suppress("NEWER_VERSION_IN_SINCE_KOTLIN")
    @Deprecated("User subscribeFor with ChatEventListener")
    @SinceKotlin("99999.9")
    override fun subscribeFor(
        vararg eventTypes: String,
        listener: (event: ChatEvent) -> Unit,
    ): Disposable {
        return client.subscribeFor(*eventTypes, listener = filterRelevantEvents(listener))
    }

    public fun subscribeFor(
        vararg eventTypes: String,
        listener: ChatEventListener<ChatEvent>,
    ): Disposable {
        return client.subscribeFor(*eventTypes, listener = filterRelevantEvents(listener))
    }

    @Suppress("NEWER_VERSION_IN_SINCE_KOTLIN")
    @Deprecated("User subscribeFor with ChatEventListener")
    @SinceKotlin("99999.9")
    override fun subscribeFor(
        lifecycleOwner: LifecycleOwner,
        vararg eventTypes: String,
        listener: (event: ChatEvent) -> Unit,
    ): Disposable {
        return client.subscribeFor(
            lifecycleOwner,
            *eventTypes,
            listener = filterRelevantEvents(listener)
        )
    }

    public fun subscribeFor(
        lifecycleOwner: LifecycleOwner,
        vararg eventTypes: String,
        listener: ChatEventListener<ChatEvent>,
    ): Disposable {
        return client.subscribeFor(
            lifecycleOwner,
            *eventTypes,
            listener = filterRelevantEvents(listener)
        )
    }

    @Suppress("NEWER_VERSION_IN_SINCE_KOTLIN")
    @Deprecated("User subscribeFor with ChatEventListener")
    @SinceKotlin("99999.9")
    override fun subscribeFor(
        vararg eventTypes: Class<out ChatEvent>,
        listener: (event: ChatEvent) -> Unit,
    ): Disposable {
        return client.subscribeFor(*eventTypes, listener = filterRelevantEvents(listener))
    }

    public fun subscribeFor(
        vararg eventTypes: Class<out ChatEvent>,
        listener: ChatEventListener<ChatEvent>,
    ): Disposable {
        return client.subscribeFor(*eventTypes, listener = filterRelevantEvents(listener))
    }

    @Suppress("NEWER_VERSION_IN_SINCE_KOTLIN")
    @Deprecated("User subscribeFor with ChatEventListener")
    @SinceKotlin("99999.9")
    override fun subscribeFor(
        lifecycleOwner: LifecycleOwner,
        vararg eventTypes: Class<out ChatEvent>,
        listener: (event: ChatEvent) -> Unit,
    ): Disposable {
        return client.subscribeFor(
            lifecycleOwner,
            *eventTypes,
            listener = filterRelevantEvents(listener)
        )
    }

    public fun subscribeFor(
        lifecycleOwner: LifecycleOwner,
        vararg eventTypes: Class<out ChatEvent>,
        listener: ChatEventListener<ChatEvent>,
    ): Disposable {
        return client.subscribeFor(
            lifecycleOwner,
            *eventTypes,
            listener = filterRelevantEvents(listener)
        )
    }

    @Suppress("NEWER_VERSION_IN_SINCE_KOTLIN")
    @Deprecated("User subscribeForSingle with ChatEventListener")
    @SinceKotlin("99999.9")
    override fun subscribeForSingle(
        eventType: String,
        listener: (event: ChatEvent) -> Unit,
    ): Disposable {
        return client.subscribeForSingle(eventType, listener = filterRelevantEvents(listener))
    }

    public fun subscribeForSingle(
        eventType: String,
        listener: ChatEventListener<ChatEvent>,
    ): Disposable {
        return client.subscribeForSingle(eventType, listener = filterRelevantEvents(listener))
    }

    @Suppress("NEWER_VERSION_IN_SINCE_KOTLIN")
    @Deprecated("User subscribeForSingle with ChatEventListener")
    @SinceKotlin("99999.9")
    override fun <T : ChatEvent> subscribeForSingle(
        eventType: Class<T>,
        listener: (event: T) -> Unit,
    ): Disposable {
        return client.subscribeForSingle(eventType, listener = filterRelevantEvents(listener))
    }

    public fun <T : ChatEvent> subscribeForSingle(
        eventType: Class<T>,
        listener: ChatEventListener<T>,
    ): Disposable {
        return client.subscribeForSingle(eventType, listener = filterRelevantEvents(listener))
    }

    private fun <T : ChatEvent> filterRelevantEvents(
        listener: ChatEventListener<T>,
    ): ChatEventListener<T> {
        return ChatEventListener { event: T ->
            if (isRelevantForChannel(event)) {
                listener.onEvent(event)
            }
        }
    }

    private fun isRelevantForChannel(event: ChatEvent): Boolean {
        return when (event) {
            is ChannelCreatedEvent -> event.cid == cid
            is ChannelDeletedEvent -> event.cid == cid
            is ChannelHiddenEvent -> event.cid == cid
            is ChannelMuteEvent -> event.channelMute.channel.cid == cid
            is ChannelsMuteEvent -> event.channelsMute.any { it.channel.cid == cid }
            is ChannelTruncatedEvent -> event.cid == cid
            is ChannelUnmuteEvent -> event.channelMute.channel.cid == cid
            is ChannelsUnmuteEvent -> event.channelsMute.any { it.channel.cid == cid }
            is ChannelUpdatedEvent -> event.cid == cid
            is ChannelUpdatedByUserEvent -> event.cid == cid
            is ChannelVisibleEvent -> event.cid == cid
            is MemberAddedEvent -> event.cid == cid
            is MemberRemovedEvent -> event.cid == cid
            is MemberUpdatedEvent -> event.cid == cid
            is MessageDeletedEvent -> event.cid == cid
            is MessageReadEvent -> event.cid == cid
            is MessageUpdatedEvent -> event.cid == cid
            is NewMessageEvent -> event.cid == cid
            is NotificationAddedToChannelEvent -> event.cid == cid
            is NotificationChannelDeletedEvent -> event.cid == cid
            is NotificationChannelTruncatedEvent -> event.cid == cid
            is NotificationInviteAcceptedEvent -> event.cid == cid
            is NotificationInvitedEvent -> event.cid == cid
            is NotificationMarkReadEvent -> event.cid == cid
            is NotificationMessageNewEvent -> event.cid == cid
            is NotificationRemovedFromChannelEvent -> event.cid == cid
            is ReactionDeletedEvent -> event.cid == cid
            is ReactionNewEvent -> event.cid == cid
            is ReactionUpdateEvent -> event.cid == cid
            is TypingStartEvent -> event.cid == cid
            is TypingStopEvent -> event.cid == cid
            is ChannelUserBannedEvent -> event.cid == cid
            is UserStartWatchingEvent -> event.cid == cid
            is UserStopWatchingEvent -> event.cid == cid
            is ChannelUserUnbannedEvent -> event.cid == cid
            is UnknownEvent -> event.rawData["cid"] == cid
            is HealthEvent,
            is NotificationChannelMutesUpdatedEvent,
            is NotificationMutesUpdatedEvent,
            is GlobalUserBannedEvent,
            is UserDeletedEvent,
            is UserMutedEvent,
            is UsersMutedEvent,
            is UserPresenceChangedEvent,
            is GlobalUserUnbannedEvent,
            is UserUnmutedEvent,
            is UsersUnmutedEvent,
            is UserUpdatedEvent,
            is ConnectedEvent,
            is ConnectingEvent,
            is DisconnectedEvent,
            is ErrorEvent,
            is MarkAllReadEvent,
            -> false
        }
    }

    @CheckResult
    override fun query(request: QueryChannelRequest): Call<Channel> {
        return client.queryChannel(channelType, channelId, request)
    }

    @CheckResult
    override fun watch(request: WatchChannelRequest): Call<Channel> {
        return client.queryChannel(channelType, channelId, request)
    }

    @CheckResult
    override fun watch(data: Map<String, Any>): Call<Channel> {
        val request = WatchChannelRequest()
        request.data.putAll(data)
        return watch(request)
    }

    @CheckResult
    override fun watch(): Call<Channel> {
        return client.queryChannel(channelType, channelId, WatchChannelRequest())
    }

    @CheckResult
    override fun stopWatching(): Call<Unit> {
        return client.stopWatching(channelType, channelId)
    }

    @CheckResult
    override fun getMessage(messageId: String): Call<Message> {
        return client.getMessage(messageId)
    }

    @CheckResult
    override fun updateMessage(message: Message): Call<Message> {
        return client.updateMessage(message)
    }

    @CheckResult
    override fun deleteMessage(messageId: String): Call<Message> {
        return client.deleteMessage(messageId)
    }

    @CheckResult
    override fun sendMessage(message: Message): Call<Message> {
        return client.sendMessage(channelType, channelId, message)
    }

    @CheckResult
    override fun banUser(targetId: String, reason: String?, timeout: Int?): Call<Unit> {
        return client.banUser(
            targetId = targetId,
            channelType = channelType,
            channelId = channelId,
            reason = reason,
            timeout = timeout,
        )
    }

    @CheckResult
    override fun unBanUser(targetId: String, reason: String?, timeout: Int?): Call<Unit> {
        return client.unBanUser(
            targetId = targetId,
            channelType = channelType,
            channelId = channelId,
        )
    }

    @CheckResult
    override fun shadowBanUser(targetId: String, reason: String?, timeout: Int?): Call<Unit> {
        return client.shadowBanUser(
            targetId = targetId,
            channelType = channelType,
            channelId = channelId,
            reason = reason,
            timeout = timeout,
        )
    }

    @CheckResult
    override fun removeShadowBan(targetId: String): Call<Unit> {
        return client.removeShadowBan(
            targetId = targetId,
            channelType = channelType,
            channelId = channelId,
        )
    }

    @CheckResult
    override fun markMessageRead(messageId: String): Call<Unit> {
        return client.markMessageRead(channelType, channelId, messageId)
    }

    @CheckResult
    override fun markRead(): Call<Unit> {
        return client.markRead(channelType, channelId)
    }

    @CheckResult
    override fun delete(): Call<Channel> {
        return client.deleteChannel(channelType, channelId)
    }

    @CheckResult
    override fun show(): Call<Unit> {
        return client.showChannel(channelType, channelId)
    }

    @CheckResult
    override fun hide(clearHistory: Boolean): Call<Unit> {
        return client.hideChannel(channelType, channelId, clearHistory)
    }

    @CheckResult
    override fun sendFile(file: File): Call<String> {
        return client.sendFile(channelType, channelId, file)
    }

    @CheckResult
    override fun sendImage(file: File): Call<String> {
        return client.sendImage(channelType, channelId, file)
    }

    @CheckResult
    override fun sendFile(file: File, callback: ProgressCallback): Call<String> {
        return client.sendFile(channelType, channelId, file)
    }

    @CheckResult
    override fun sendImage(file: File, callback: ProgressCallback): Call<String> {
        return client.sendImage(channelType, channelId, file)
    }

    @CheckResult
    override fun sendReaction(reaction: Reaction, enforceUnique: Boolean): Call<Reaction> {
        return client.sendReaction(reaction, enforceUnique)
    }

    @CheckResult
    override fun sendAction(request: SendActionRequest): Call<Message> {
        return client.sendAction(request)
    }

    @CheckResult
    override fun deleteReaction(messageId: String, reactionType: String): Call<Message> {
        return client.deleteReaction(messageId, reactionType)
    }

    @CheckResult
    override fun getReactions(messageId: String, offset: Int, limit: Int): Call<List<Reaction>> {
        return client.getReactions(messageId, offset, limit)
    }

    @CheckResult
    override fun getReactions(
        messageId: String,
        firstReactionId: String,
        limit: Int,
    ): Call<List<Message>> {
        return client.getRepliesMore(messageId, firstReactionId, limit)
    }

    @CheckResult
    override fun update(message: Message?, extraData: Map<String, Any>): Call<Channel> {
        return client.updateChannel(channelType, channelId, message, extraData)
    }

    @CheckResult
    override fun enableSlowMode(cooldownTimeInSeconds: Int): Call<Channel> =
        client.enableSlowMode(channelType, channelId, cooldownTimeInSeconds)

    @CheckResult
    override fun disableSlowMode(): Call<Channel> =
        client.disableSlowMode(channelType, channelId)

    @CheckResult
    override fun addMembers(vararg userIds: String): Call<Channel> {
        return client.addMembers(channelType, channelId, userIds.toList())
    }

    @CheckResult
    override fun removeMembers(vararg userIds: String): Call<Channel> {
        return client.removeMembers(channelType, channelId, userIds.toList())
    }

    @CheckResult
    override fun acceptInvite(message: String?): Call<Channel> {
        return client.acceptInvite(channelType, channelId, message)
    }

    @CheckResult
    override fun rejectInvite(): Call<Channel> {
        return client.rejectInvite(channelType, channelId)
    }

    @CheckResult
    override fun muteCurrentUser(): Call<Mute> {
        return client.muteCurrentUser()
    }

    @CheckResult
    override fun mute(): Call<Unit> {
        return client.muteChannel(channelType, channelId)
    }

    @CheckResult
    override fun unmute(): Call<Unit> {
        return client.unMuteChannel(channelType, channelId)
    }

    @CheckResult
    override fun muteUser(userId: String): Call<Mute> {
        return client.muteUser(userId)
    }

    @CheckResult
    override fun unmuteUser(userId: String): Call<Unit> {
        return client.unmuteUser(userId)
    }

    @CheckResult
    override fun unmuteCurrentUser(): Call<Unit> {
        return client.unmuteCurrentUser()
    }

    @CheckResult
    override fun keystroke(): Call<ChatEvent> {
        return client.sendEvent(EventType.TYPING_START, channelType, channelId)
    }

    public fun keystroke(parentId: String): Call<ChatEvent> {
        return client.sendEvent(
            eventType = EventType.TYPING_START,
            channelType = channelType,
            channelId = channelId,
            extraData = mapOf(ARG_TYPING_PARENT_ID to parentId),
        )
    }

    @CheckResult
    override fun stopTyping(): Call<ChatEvent> {
        return client.sendEvent(EventType.TYPING_STOP, channelType, channelId)
    }

    public fun stopTyping(parentId: String): Call<ChatEvent> {
        return client.sendEvent(
            eventType = EventType.TYPING_STOP,
            channelType = channelType,
            channelId = channelId,
            extraData = mapOf(ARG_TYPING_PARENT_ID to parentId),
        )
    }

    @CheckResult
    override fun queryMembers(
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySort<Member>,
        members: List<Member>,
    ): Call<List<Member>> {
        return client.queryMembers(channelType, channelId, offset, limit, filter, sort, members)
    }

    @CheckResult
    public fun getFileAttachments(offset: Int, limit: Int): Call<List<Attachment>> =
        client.getFileAttachments(channelType, channelId, offset, limit)

    @CheckResult
    public fun getImageAttachments(offset: Int, limit: Int): Call<List<Attachment>> =
        client.getImageAttachments(channelType, channelId, offset, limit)

    @CheckResult
    public fun getMessagesWithAttachments(offset: Int, limit: Int, type: String): Call<List<Message>> =
        client.getMessagesWithAttachments(channelType, channelId, offset, limit, type)

    @CheckResult
    public fun pinMessage(message: Message, expirationDate: Date?): Call<Message> {
        return client.pinMessage(message, expirationDate)
    }

    @CheckResult
    public fun pinMessage(message: Message, timeout: Int): Call<Message> {
        return client.pinMessage(message, timeout)
    }

    @CheckResult
    public fun unpinMessage(message: Message): Call<Message> = client.unpinMessage(message)

    private companion object {
        private const val ARG_TYPING_PARENT_ID = "parent_id"
    }
}
