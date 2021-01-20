package io.getstream.chat.android.client.controllers

import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.SendActionRequest
import io.getstream.chat.android.client.api.models.WatchChannelRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.observable.ChatObservable
import io.getstream.chat.android.client.utils.observable.Disposable
import java.io.File

@Deprecated(
    message = "The ChannelController has been replaced by ChannelClient",
    replaceWith = ReplaceWith("ChannelClient")
)
public interface ChannelController {

    public val channelType: String
    public val channelId: String
    public val cid: String

    public fun create(extraData: Map<String, Any> = emptyMap()): Call<Channel>
    public fun create(members: List<String>, extraData: Map<String, Any> = emptyMap()): Call<Channel>
    public fun query(request: QueryChannelRequest): Call<Channel>
    public fun watch(request: WatchChannelRequest): Call<Channel>
    public fun watch(): Call<Channel>
    public fun stopWatching(): Call<Unit>
    public fun sendMessage(message: Message): Call<Message>
    public fun updateMessage(message: Message): Call<Message>
    public fun deleteMessage(messageId: String): Call<Message>
    public fun getMessage(messageId: String): Call<Message>
    public fun banUser(targetId: String, reason: String?, timeout: Int?): Call<Unit>
    public fun unBanUser(targetId: String, reason: String?, timeout: Int?): Call<Unit>
    public fun shadowBanUser(targetId: String, reason: String?, timeout: Int?): Call<Unit>
    public fun removeShadowBan(targetId: String): Call<Unit>
    public fun markMessageRead(messageId: String): Call<Unit>
    public fun markRead(): Call<Unit>
    public fun delete(): Call<Channel>
    public fun show(): Call<Unit>
    public fun hide(clearHistory: Boolean = false): Call<Unit>
    public fun sendFile(file: File): Call<String>
    public fun sendImage(file: File): Call<String>
    public fun sendFile(file: File, callback: ProgressCallback): Call<String>
    public fun sendImage(file: File, callback: ProgressCallback): Call<String>
    public fun sendReaction(reaction: Reaction, enforceUnique: Boolean = false): Call<Reaction>
    public fun sendAction(request: SendActionRequest): Call<Message>
    public fun deleteReaction(messageId: String, reactionType: String): Call<Message>
    public fun getReactions(messageId: String, offset: Int, limit: Int): Call<List<Reaction>>
    public fun getReactions(messageId: String, firstReactionId: String, limit: Int): Call<List<Message>>

    @Deprecated(
        message = "Use subscribe() on the controller directly instead",
        level = DeprecationLevel.WARNING
    )
    public fun events(): ChatObservable

    public fun subscribe(listener: (event: ChatEvent) -> Unit): Disposable

    public fun subscribeFor(
        vararg eventTypes: String,
        listener: (event: ChatEvent) -> Unit
    ): Disposable

    /**
     * Subscribes to the specific [eventTypes] of the channel, in the lifecycle of [lifecycleOwner].
     *
     * Only receives events when the lifecycle is in a STARTED state, otherwise events are dropped.
     *
     * @see [io.getstream.chat.android.client.models.EventType] for type constants
     */
    public fun subscribeFor(
        lifecycleOwner: LifecycleOwner,
        vararg eventTypes: String,
        listener: (event: ChatEvent) -> Unit
    ): Disposable

    public fun subscribeFor(
        vararg eventTypes: Class<out ChatEvent>,
        listener: (event: ChatEvent) -> Unit
    ): Disposable

    /**
     * Subscribes to the specific [eventTypes] of the channel, in the lifecycle of [lifecycleOwner].
     *
     * Only receives events when the lifecycle is in a STARTED state, otherwise events are dropped.
     */
    public fun subscribeFor(
        lifecycleOwner: LifecycleOwner,
        vararg eventTypes: Class<out ChatEvent>,
        listener: (event: ChatEvent) -> Unit
    ): Disposable

    /**
     * Subscribes for the next channel event with the given [eventType].
     *
     * @see [io.getstream.chat.android.client.models.EventType] for type constants
     */
    public fun subscribeForSingle(eventType: String, listener: (event: ChatEvent) -> Unit): Disposable

    /**
     * Subscribes for the next channel event with the given [eventType].
     */
    public fun <T : ChatEvent> subscribeForSingle(
        eventType: Class<T>,
        listener: (event: T) -> Unit
    ): Disposable

    public fun update(message: Message? = null, extraData: Map<String, Any> = emptyMap()): Call<Channel>
    public fun enableSlowMode(cooldownTimeInSeconds: Int): Call<Channel>
    public fun disableSlowMode(): Call<Channel>
    public fun addMembers(vararg userIds: String): Call<Channel>
    public fun removeMembers(vararg userIds: String): Call<Channel>
    public fun acceptInvite(message: String?): Call<Channel>
    public fun rejectInvite(): Call<Channel>
    public fun mute(): Call<Unit>
    public fun unmute(): Call<Unit>
    public fun muteCurrentUser(): Call<Mute>
    public fun muteUser(userId: String): Call<Mute>
    public fun unmuteUser(userId: String): Call<Unit>
    public fun unmuteCurrentUser(): Call<Unit>
    public fun watch(data: Map<String, Any>): Call<Channel>
    public fun stopTyping(): Call<ChatEvent>
    public fun keystroke(): Call<ChatEvent>
    public fun queryMembers(
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySort<Member> = QuerySort(),
        members: List<Member> = emptyList()
    ): Call<List<Member>>
}
