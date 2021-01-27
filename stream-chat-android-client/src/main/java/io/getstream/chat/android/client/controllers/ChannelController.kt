package io.getstream.chat.android.client.controllers

import androidx.annotation.CheckResult
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

    @CheckResult
    public fun create(extraData: Map<String, Any> = emptyMap()): Call<Channel>

    @CheckResult
    public fun create(members: List<String>, extraData: Map<String, Any> = emptyMap()): Call<Channel>

    @CheckResult
    public fun query(request: QueryChannelRequest): Call<Channel>

    @CheckResult
    public fun watch(request: WatchChannelRequest): Call<Channel>

    @CheckResult
    public fun watch(): Call<Channel>

    @CheckResult
    public fun stopWatching(): Call<Unit>

    @CheckResult
    public fun sendMessage(message: Message): Call<Message>

    @CheckResult
    public fun updateMessage(message: Message): Call<Message>

    @CheckResult
    public fun deleteMessage(messageId: String): Call<Message>

    @CheckResult
    public fun getMessage(messageId: String): Call<Message>

    @CheckResult
    public fun banUser(targetId: String, reason: String?, timeout: Int?): Call<Unit>

    @CheckResult
    public fun unBanUser(targetId: String, reason: String?, timeout: Int?): Call<Unit>

    @CheckResult
    public fun shadowBanUser(targetId: String, reason: String?, timeout: Int?): Call<Unit>

    @CheckResult
    public fun removeShadowBan(targetId: String): Call<Unit>

    @CheckResult
    public fun markMessageRead(messageId: String): Call<Unit>

    @CheckResult
    public fun markRead(): Call<Unit>

    @CheckResult
    public fun delete(): Call<Channel>

    @CheckResult
    public fun show(): Call<Unit>

    @CheckResult
    public fun hide(clearHistory: Boolean = false): Call<Unit>

    @CheckResult
    public fun sendFile(file: File): Call<String>

    @CheckResult
    public fun sendImage(file: File): Call<String>

    @CheckResult
    public fun sendFile(file: File, callback: ProgressCallback): Call<String>

    @CheckResult
    public fun sendImage(file: File, callback: ProgressCallback): Call<String>

    @CheckResult
    public fun sendReaction(reaction: Reaction, enforceUnique: Boolean = false): Call<Reaction>

    @CheckResult
    public fun sendAction(request: SendActionRequest): Call<Message>

    @CheckResult
    public fun deleteReaction(messageId: String, reactionType: String): Call<Message>

    @CheckResult
    public fun getReactions(messageId: String, offset: Int, limit: Int): Call<List<Reaction>>

    @CheckResult
    public fun getReactions(messageId: String, firstReactionId: String, limit: Int): Call<List<Message>>

    @Deprecated(
        message = "Use subscribe() on the controller directly instead",
        level = DeprecationLevel.WARNING
    )
    public fun events(): ChatObservable

    public fun subscribe(listener: (ChatEvent) -> Unit): Disposable

    public fun subscribeFor(
        vararg eventTypes: String,
        listener: (ChatEvent) -> Unit,
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
        listener: (event: ChatEvent) -> Unit,
    ): Disposable

    /**
     * Subscribes to one or more event [eventTypes]. Does not respect any lifecycle.
     *
     * @param eventTypes a list of events we wish to observe
     * @param listener callback invoked when events are received
     * @return
     */
    public fun subscribeFor(
        vararg eventTypes: Class<out ChatEvent>,
        listener: (event: ChatEvent) -> Unit,
    ): Disposable

    /**
     * Subscribes to the specific [eventTypes] of the channel, in the lifecycle of [lifecycleOwner].
     *
     * Only receives events when the lifecycle is in a STARTED state, otherwise events are dropped.
     */
    public fun subscribeFor(
        lifecycleOwner: LifecycleOwner,
        vararg eventTypes: Class<out ChatEvent>,
        listener: (event: ChatEvent) -> Unit,
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
        listener: (event: T) -> Unit,
    ): Disposable

    @CheckResult
    public fun update(message: Message? = null, extraData: Map<String, Any> = emptyMap()): Call<Channel>

    @CheckResult
    public fun enableSlowMode(cooldownTimeInSeconds: Int): Call<Channel>

    @CheckResult
    public fun disableSlowMode(): Call<Channel>

    @CheckResult
    public fun addMembers(vararg userIds: String): Call<Channel>

    @CheckResult
    public fun removeMembers(vararg userIds: String): Call<Channel>

    @CheckResult
    public fun acceptInvite(message: String?): Call<Channel>

    @CheckResult
    public fun rejectInvite(): Call<Channel>

    @CheckResult
    public fun mute(): Call<Unit>

    @CheckResult
    public fun unmute(): Call<Unit>

    @CheckResult
    public fun muteCurrentUser(): Call<Mute>

    @CheckResult
    public fun muteUser(userId: String): Call<Mute>

    @CheckResult
    public fun unmuteUser(userId: String): Call<Unit>

    @CheckResult
    public fun unmuteCurrentUser(): Call<Unit>

    @CheckResult
    public fun watch(data: Map<String, Any>): Call<Channel>

    @CheckResult
    public fun stopTyping(): Call<ChatEvent>

    @CheckResult
    public fun keystroke(): Call<ChatEvent>

    @CheckResult
    public fun queryMembers(
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySort<Member> = QuerySort(),
        members: List<Member> = emptyList(),
    ): Call<List<Member>>
}
