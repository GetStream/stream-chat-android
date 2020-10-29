package io.getstream.chat.android.client.controllers

import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.SendActionRequest
import io.getstream.chat.android.client.api.models.WatchChannelRequest
import io.getstream.chat.android.client.call.Call
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

internal class ChannelControllerImpl(
    override val channelType: String,
    override val channelId: String,
    private val client: ChatClient
) : ChannelController {

    override val cid = "$channelType:$channelId"

    override fun create(members: List<String>, extraData: Map<String, Any>): Call<Channel> {
        return client.createChannel(channelType, channelId, members, extraData)
    }

    override fun create(extraData: Map<String, Any>): Call<Channel> {
        return client.createChannel(channelType, channelId, emptyList())
    }

    override fun events(): ChatObservable {
        return client.events().filter(this::isRelevantForChannel)
    }

    override fun subscribe(listener: (event: ChatEvent) -> Unit): Disposable {
        return client.subscribe(filterRelevantEvents(listener))
    }

    override fun subscribeFor(
        vararg eventTypes: String,
        listener: (event: ChatEvent) -> Unit
    ): Disposable {
        return client.subscribeFor(*eventTypes, listener = filterRelevantEvents(listener))
    }

    override fun subscribeFor(
        lifecycleOwner: LifecycleOwner,
        vararg eventTypes: String,
        listener: (event: ChatEvent) -> Unit
    ): Disposable {
        return client.subscribeFor(
            lifecycleOwner,
            *eventTypes,
            listener = filterRelevantEvents(listener)
        )
    }

    override fun subscribeFor(
        vararg eventTypes: Class<out ChatEvent>,
        listener: (event: ChatEvent) -> Unit
    ): Disposable {
        return client.subscribeFor(*eventTypes, listener = filterRelevantEvents(listener))
    }

    override fun subscribeFor(
        lifecycleOwner: LifecycleOwner,
        vararg eventTypes: Class<out ChatEvent>,
        listener: (event: ChatEvent) -> Unit
    ): Disposable {
        return client.subscribeFor(
            lifecycleOwner,
            *eventTypes,
            listener = filterRelevantEvents(listener)
        )
    }

    override fun subscribeForSingle(
        eventType: String,
        listener: (event: ChatEvent) -> Unit
    ): Disposable {
        return client.subscribeForSingle(eventType, listener = filterRelevantEvents(listener))
    }

    override fun <T : ChatEvent> subscribeForSingle(
        eventType: Class<T>,
        listener: (event: T) -> Unit
    ): Disposable {
        return client.subscribeForSingle(eventType, listener = filterRelevantEvents(listener))
    }

    private fun <T : ChatEvent> filterRelevantEvents(
        listener: (event: T) -> Unit
    ): (T) -> Unit {
        return { event: T ->
            if (isRelevantForChannel(event)) {
                listener.invoke(event)
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
            is ErrorEvent -> false
        }
    }

    override fun query(request: QueryChannelRequest): Call<Channel> {
        return client.queryChannel(channelType, channelId, request)
    }

    override fun watch(request: WatchChannelRequest): Call<Channel> {
        return client.queryChannel(channelType, channelId, request)
    }

    override fun watch(data: Map<String, Any>): Call<Channel> {
        val request = WatchChannelRequest()
        request.data.putAll(data)
        return watch(request)
    }

    override fun watch(): Call<Channel> {
        return client.queryChannel(channelType, channelId, WatchChannelRequest())
    }

    override fun stopWatching(): Call<Unit> {
        return client.stopWatching(channelType, channelId)
    }

    override fun getMessage(messageId: String): Call<Message> {
        return client.getMessage(messageId)
    }

    override fun updateMessage(message: Message): Call<Message> {
        return client.updateMessage(message)
    }

    override fun deleteMessage(messageId: String): Call<Message> {
        return client.deleteMessage(messageId)
    }

    override fun sendMessage(message: Message): Call<Message> {
        return client.sendMessage(channelType, channelId, message)
    }

    override fun banUser(targetId: String, reason: String, timout: Int): Call<Unit> {
        return client.banUser(targetId, channelType, channelId, reason, timout)
    }

    override fun unBanUser(targetId: String, reason: String, timout: Int): Call<Unit> {
        return client.unBanUser(targetId, channelType, channelId)
    }

    override fun markMessageRead(messageId: String): Call<Unit> {
        return client.markMessageRead(channelType, channelId, messageId)
    }

    override fun markRead(): Call<Unit> {
        return client.markRead(channelType, channelId)
    }

    override fun delete(): Call<Channel> {
        return client.deleteChannel(channelType, channelId)
    }

    override fun show(): Call<Unit> {
        return client.showChannel(channelType, channelId)
    }

    override fun hide(clearHistory: Boolean): Call<Unit> {
        return client.hideChannel(channelType, channelId, clearHistory)
    }

    override fun sendFile(file: File): Call<String> {
        return client.sendFile(channelType, channelId, file)
    }

    override fun sendImage(file: File): Call<String> {
        return client.sendImage(channelType, channelId, file)
    }

    override fun sendFile(file: File, callback: ProgressCallback): Call<String> {
        return client.sendFile(channelType, channelId, file)
    }

    override fun sendImage(file: File, callback: ProgressCallback): Call<String> {
        return client.sendImage(channelType, channelId, file)
    }

    override fun sendReaction(reaction: Reaction): Call<Reaction> {
        return client.sendReaction(reaction)
    }

    override fun sendAction(request: SendActionRequest): Call<Message> {
        return client.sendAction(request)
    }

    override fun deleteReaction(messageId: String, reactionType: String): Call<Message> {
        return client.deleteReaction(messageId, reactionType)
    }

    override fun getReactions(messageId: String, offset: Int, limit: Int): Call<List<Reaction>> {
        return client.getReactions(messageId, offset, limit)
    }

    override fun getReactions(
        messageId: String,
        firstReactionId: String,
        limit: Int
    ): Call<List<Message>> {
        return client.getRepliesMore(messageId, firstReactionId, limit)
    }

    override fun update(message: Message?, extraData: Map<String, Any>): Call<Channel> {
        return client.updateChannel(channelType, channelId, message, extraData)
    }

    override fun enableSlowMode(cooldownTimeInSeconds: Int): Call<Channel> =
        client.enableSlowMode(channelType, channelId, cooldownTimeInSeconds)

    override fun disableSlowMode(): Call<Channel> =
        client.disableSlowMode(channelType, channelId)

    override fun addMembers(vararg userIds: String): Call<Channel> {
        return client.addMembers(channelType, channelId, userIds.toList())
    }

    override fun removeMembers(vararg userIds: String): Call<Channel> {
        return client.removeMembers(channelType, channelId, userIds.toList())
    }

    override fun acceptInvite(message: String): Call<Channel> {
        return client.acceptInvite(channelType, channelId, message)
    }

    override fun rejectInvite(): Call<Channel> {
        return client.rejectInvite(channelType, channelId)
    }

    override fun muteCurrentUser(): Call<Mute> {
        return client.muteCurrentUser()
    }

    override fun mute(): Call<Unit> {
        return client.muteChannel(channelType, channelId)
    }

    override fun unmute(): Call<Unit> {
        return client.unMuteChannel(channelType, channelId)
    }

    override fun muteUser(userId: String): Call<Mute> {
        return client.muteUser(userId)
    }

    override fun unmuteUser(userId: String): Call<Mute> {
        return client.unmuteUser(userId)
    }

    override fun unmuteCurrentUser(): Call<Mute> {
        return client.unmuteCurrentUser()
    }

    override fun keystroke(): Call<ChatEvent> {
        return client.sendEvent(EventType.TYPING_START, channelType, channelId)
    }

    override fun stopTyping(): Call<ChatEvent> {
        return client.sendEvent(EventType.TYPING_STOP, channelType, channelId)
    }

    override fun queryMembers(
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySort,
        members: List<Member>
    ): Call<List<Member>> {
        return client.queryMembers(channelType, channelId, offset, limit, filter, sort, members)
    }
}
