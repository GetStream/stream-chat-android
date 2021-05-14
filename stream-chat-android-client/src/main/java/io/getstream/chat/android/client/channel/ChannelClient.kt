@file:Suppress("DEPRECATION_ERROR")

package io.getstream.chat.android.client.channel

import androidx.annotation.CheckResult
import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.ChatEventListener
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.SendActionRequest
import io.getstream.chat.android.client.api.models.WatchChannelRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.ChannelHiddenEvent
import io.getstream.chat.android.client.events.ChannelTruncatedEvent
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.ChannelUserBannedEvent
import io.getstream.chat.android.client.events.ChannelUserUnbannedEvent
import io.getstream.chat.android.client.events.ChannelVisibleEvent
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
import io.getstream.chat.android.client.events.NotificationInviteRejectedEvent
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
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.events.UserUpdatedEvent
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.BannedUser
import io.getstream.chat.android.client.models.BannedUsersSort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.uploader.FileUploader
import io.getstream.chat.android.client.uploader.StreamCdnImageMimeTypes
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.observable.Disposable
import java.io.File
import java.util.Date

public class ChannelClient internal constructor(
    public val channelType: String,
    public val channelId: String,
    private val client: ChatClient,
) {

    public val cid: String = "$channelType:$channelId"

    @CheckResult
    public fun create(members: List<String>, extraData: Map<String, Any> = emptyMap()): Call<Channel> {
        return client.createChannel(channelType, channelId, members, extraData)
    }

    @CheckResult
    public fun create(extraData: Map<String, Any> = emptyMap()): Call<Channel> {
        return client.createChannel(channelType, channelId, extraData)
    }

    @Suppress("NEWER_VERSION_IN_SINCE_KOTLIN")
    @Deprecated("Use subscribe with ChatEventListener")
    @SinceKotlin("99999.9")
    public fun subscribe(listener: (event: ChatEvent) -> Unit): Disposable {
        return client.subscribe(filterRelevantEvents(listener))
    }

    public fun subscribe(listener: ChatEventListener<ChatEvent>): Disposable {
        return client.subscribe(filterRelevantEvents(listener))
    }

    @Suppress("NEWER_VERSION_IN_SINCE_KOTLIN")
    @Deprecated("Use subscribeFor with ChatEventListener")
    @SinceKotlin("99999.9")
    public fun subscribeFor(
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
    @Deprecated("Use subscribeFor with ChatEventListener")
    @SinceKotlin("99999.9")
    public fun subscribeFor(
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
    @Deprecated("Use subscribeFor with ChatEventListener")
    @SinceKotlin("99999.9")
    public fun subscribeFor(
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
    @Deprecated("Use subscribeFor with ChatEventListener")
    @SinceKotlin("99999.9")
    public fun subscribeFor(
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
    @Deprecated("Use subscribeForSingle with ChatEventListener")
    @SinceKotlin("99999.9")
    public fun subscribeForSingle(
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
    @Deprecated("Use subscribeForSingle with ChatEventListener")
    @SinceKotlin("99999.9")
    public fun <T : ChatEvent> subscribeForSingle(
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
            is ChannelDeletedEvent -> event.cid == cid
            is ChannelHiddenEvent -> event.cid == cid
            is ChannelTruncatedEvent -> event.cid == cid
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
            is NotificationInviteRejectedEvent -> event.cid == cid
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
            is UserPresenceChangedEvent,
            is GlobalUserUnbannedEvent,
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
    public fun query(request: QueryChannelRequest): Call<Channel> {
        return client.queryChannel(channelType, channelId, request)
    }

    @CheckResult
    public fun watch(request: WatchChannelRequest): Call<Channel> {
        return client.queryChannel(channelType, channelId, request)
    }

    @CheckResult
    public fun watch(data: Map<String, Any>): Call<Channel> {
        val request = WatchChannelRequest()
        request.data.putAll(data)
        return watch(request)
    }

    @CheckResult
    public fun watch(): Call<Channel> {
        return client.queryChannel(channelType, channelId, WatchChannelRequest())
    }

    @CheckResult
    public fun stopWatching(): Call<Unit> {
        return client.stopWatching(channelType, channelId)
    }

    @CheckResult
    public fun getMessage(messageId: String): Call<Message> {
        return client.getMessage(messageId)
    }

    @CheckResult
    public fun updateMessage(message: Message): Call<Message> {
        return client.updateMessage(message)
    }

    @CheckResult
    public fun deleteMessage(messageId: String): Call<Message> {
        return client.deleteMessage(messageId)
    }

    @CheckResult
    public fun sendMessage(message: Message): Call<Message> {
        return client.sendMessage(channelType, channelId, message)
    }

    @CheckResult
    public fun banUser(targetId: String, reason: String?, timeout: Int?): Call<Unit> {
        return client.banUser(
            targetId = targetId,
            channelType = channelType,
            channelId = channelId,
            reason = reason,
            timeout = timeout,
        )
    }

    @CheckResult
    @Deprecated(
        message = "Use the unbanUser(targetId) method instead",
        replaceWith = ReplaceWith("this.unbanUser(targetId)"),
        level = DeprecationLevel.ERROR,
    )
    public fun unBanUser(targetId: String): Call<Unit> {
        return client.unbanUser(
            targetId = targetId,
            channelType = channelType,
            channelId = channelId,
        )
    }

    @CheckResult
    public fun unbanUser(targetId: String): Call<Unit> {
        return client.unbanUser(
            targetId = targetId,
            channelType = channelType,
            channelId = channelId,
        )
    }

    @CheckResult
    public fun shadowBanUser(targetId: String, reason: String?, timeout: Int?): Call<Unit> {
        return client.shadowBanUser(
            targetId = targetId,
            channelType = channelType,
            channelId = channelId,
            reason = reason,
            timeout = timeout,
        )
    }

    @CheckResult
    public fun removeShadowBan(targetId: String): Call<Unit> {
        return client.removeShadowBan(
            targetId = targetId,
            channelType = channelType,
            channelId = channelId,
        )
    }

    @CheckResult
    @JvmOverloads
    public fun queryBannedUsers(
        filter: FilterObject? = null,
        sort: QuerySort<BannedUsersSort> = QuerySort.asc(BannedUsersSort::createdAt),
        offset: Int? = null,
        limit: Int? = null,
        createdAtAfter: Date? = null,
        createdAtAfterOrEqual: Date? = null,
        createdAtBefore: Date? = null,
        createdAtBeforeOrEqual: Date? = null,
    ): Call<List<BannedUser>> {
        val channelCidFilter = Filters.eq("channel_cid", cid)
        return client.queryBannedUsers(
            filter = filter?.let { Filters.and(channelCidFilter, it) } ?: channelCidFilter,
            sort = sort,
            offset = offset,
            limit = limit,
            createdAtAfter = createdAtAfter,
            createdAtAfterOrEqual = createdAtAfterOrEqual,
            createdAtBefore = createdAtBefore,
            createdAtBeforeOrEqual = createdAtBeforeOrEqual,
        )
    }

    @CheckResult
    public fun markMessageRead(messageId: String): Call<Unit> {
        return client.markMessageRead(channelType, channelId, messageId)
    }

    @CheckResult
    public fun markRead(): Call<Unit> {
        return client.markRead(channelType, channelId)
    }

    @CheckResult
    public fun delete(): Call<Channel> {
        return client.deleteChannel(channelType, channelId)
    }

    @CheckResult
    public fun show(): Call<Unit> {
        return client.showChannel(channelType, channelId)
    }

    @CheckResult
    public fun hide(clearHistory: Boolean = false): Call<Unit> {
        return client.hideChannel(channelType, channelId, clearHistory)
    }

    /**
     * Removes all of the messages of the channel but doesn't affect the channel data or members.
     *
     * @return executable async [Call] which completes with [Result] having data equal to the truncated channel
     * if the channel was successfully truncated.
     */
    @CheckResult
    public fun truncate(): Call<Channel> {
        return client.truncateChannel(channelType, channelId)
    }

    /**
     * Uploads a file for the given channel. Progress can be accessed via [callback].
     *
     * The Stream CDN imposes the following restrictions on file uploads:
     * - The maximum file size is 20 MB
     *
     * @param file the file that needs to be uploaded
     * @param callback the callback to track progress
     *
     * @return executable async [Call] which completes with [Result] having data equal to the URL of the uploaded file
     * if the file was successfully uploaded.
     *
     * @see FileUploader
     * @see <a href="https://getstream.io/chat/docs/android/file_uploads/?language=kotlin">File Uploads</a>
     */
    @CheckResult
    @JvmOverloads
    public fun sendFile(file: File, callback: ProgressCallback? = null): Call<String> {
        return client.sendFile(channelType, channelId, file, callback)
    }

    /**
     * Uploads an image for the given channel. Progress can be accessed via [callback].
     *
     * The Stream CDN imposes the following restrictions on image uploads:
     * - The maximum image size is 20 MB
     * - Supported MIME types are listed in [StreamCdnImageMimeTypes.SUPPORTED_IMAGE_MIME_TYPES]
     *
     * @param file the image file that needs to be uploaded
     * @param callback the callback to track progress
     *
     * @return executable async [Call] which completes with [Result] having data equal to the URL of the uploaded image
     * if the image was successfully uploaded.
     *
     * @see FileUploader
     * @see StreamCdnImageMimeTypes.SUPPORTED_IMAGE_MIME_TYPES
     * @see <a href="https://getstream.io/chat/docs/android/file_uploads/?language=kotlin">File Uploads</a>
     */
    @CheckResult
    @JvmOverloads
    public fun sendImage(file: File, callback: ProgressCallback? = null): Call<String> {
        return client.sendImage(channelType, channelId, file, callback)
    }

    /**
     * Deletes the file represented by [url] from the given channel.
     *
     * @param url the URL of the file to be deleted
     *
     * @return executable async [Call] responsible for deleting a file
     *
     * @see FileUploader
     * @see <a href="https://getstream.io/chat/docs/android/file_uploads/?language=kotlin">File Uploads</a>
     */
    @CheckResult
    public fun deleteFile(url: String): Call<Unit> {
        return client.deleteFile(channelType, channelId, url)
    }

    /**
     * Deletes the image represented by [url] from the given channel.
     *
     * @param url the URL of the image to be deleted
     *
     * @return executable async [Call] responsible for deleting an image
     *
     * @see FileUploader
     * @see <a href="https://getstream.io/chat/docs/android/file_uploads/?language=kotlin">File Uploads</a>
     */
    @CheckResult
    public fun deleteImage(url: String): Call<Unit> {
        return client.deleteImage(channelType, channelId, url)
    }

    @CheckResult
    public fun sendReaction(reaction: Reaction, enforceUnique: Boolean = false): Call<Reaction> {
        return client.sendReaction(reaction, enforceUnique)
    }

    @CheckResult
    public fun sendAction(request: SendActionRequest): Call<Message> {
        return client.sendAction(request)
    }

    @CheckResult
    public fun deleteReaction(messageId: String, reactionType: String): Call<Message> {
        return client.deleteReaction(messageId, reactionType)
    }

    @CheckResult
    public fun getReactions(messageId: String, offset: Int, limit: Int): Call<List<Reaction>> {
        return client.getReactions(messageId, offset, limit)
    }

    @CheckResult
    public fun getReactions(
        messageId: String,
        firstReactionId: String,
        limit: Int,
    ): Call<List<Message>> {
        return client.getRepliesMore(messageId, firstReactionId, limit)
    }

    @CheckResult
    public fun update(message: Message? = null, extraData: Map<String, Any> = emptyMap()): Call<Channel> {
        return client.updateChannel(channelType, channelId, message, extraData)
    }

    @CheckResult
    public fun updatePartial(set: Map<String, Any> = emptyMap(), unset: List<String> = emptyList()): Call<Channel> {
        return client.updateChannelPartial(channelType, channelId, set, unset)
    }

    @CheckResult
    public fun enableSlowMode(cooldownTimeInSeconds: Int): Call<Channel> =
        client.enableSlowMode(channelType, channelId, cooldownTimeInSeconds)

    @CheckResult
    public fun disableSlowMode(): Call<Channel> =
        client.disableSlowMode(channelType, channelId)

    @CheckResult
    public fun addMembers(vararg userIds: String): Call<Channel> {
        return client.addMembers(channelType, channelId, userIds.toList())
    }

    @CheckResult
    public fun removeMembers(vararg userIds: String): Call<Channel> {
        return client.removeMembers(channelType, channelId, userIds.toList())
    }

    @CheckResult
    public fun acceptInvite(message: String?): Call<Channel> {
        return client.acceptInvite(channelType, channelId, message)
    }

    @CheckResult
    public fun rejectInvite(): Call<Channel> {
        return client.rejectInvite(channelType, channelId)
    }

    @CheckResult
    public fun muteCurrentUser(): Call<Mute> {
        return client.muteCurrentUser()
    }

    @CheckResult
    public fun mute(): Call<Unit> {
        return client.muteChannel(channelType, channelId)
    }

    @CheckResult
    public fun unmute(): Call<Unit> {
        return client.unmuteChannel(channelType, channelId)
    }

    @CheckResult
    public fun muteUser(userId: String): Call<Mute> {
        return client.muteUser(userId)
    }

    @CheckResult
    public fun unmuteUser(userId: String): Call<Unit> {
        return client.unmuteUser(userId)
    }

    @CheckResult
    public fun unmuteCurrentUser(): Call<Unit> {
        return client.unmuteCurrentUser()
    }

    @CheckResult
    public fun keystroke(): Call<ChatEvent> {
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
    public fun stopTyping(): Call<ChatEvent> {
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
    public fun queryMembers(
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySort<Member> = QuerySort(),
        members: List<Member> = emptyList(),
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
