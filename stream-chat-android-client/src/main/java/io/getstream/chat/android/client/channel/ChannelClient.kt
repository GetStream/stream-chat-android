/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.channel

import androidx.annotation.CheckResult
import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.ChatEventListener
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.PinnedMessagesPagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.SendActionRequest
import io.getstream.chat.android.client.api.models.WatchChannelRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.errors.ChatError
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

@Suppress("TooManyFunctions")
public class ChannelClient internal constructor(
    public val channelType: String,
    public val channelId: String,
    private val client: ChatClient,
) {

    public val cid: String = "$channelType:$channelId"

    /**
     * Creates the id-based channel.
     * @see [ChatClient.createChannel]
     *
     * @param memberIds The list of members' ids.
     * @param extraData Map of key-value pairs that let you store extra data
     *
     * @return Executable async [Call] responsible for creating the channel.
     */
    @CheckResult
    public fun create(memberIds: List<String>, extraData: Map<String, Any>): Call<Channel> {
        return client.createChannel(
            channelType = channelType,
            channelId = channelId,
            memberIds = memberIds,
            extraData = extraData,
        )
    }

    public fun subscribe(listener: ChatEventListener<ChatEvent>): Disposable {
        return client.subscribe(filterRelevantEvents(listener))
    }

    public fun subscribeFor(
        vararg eventTypes: String,
        listener: ChatEventListener<ChatEvent>,
    ): Disposable {
        return client.subscribeFor(*eventTypes, listener = filterRelevantEvents(listener))
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

    public fun subscribeFor(
        vararg eventTypes: Class<out ChatEvent>,
        listener: ChatEventListener<ChatEvent>,
    ): Disposable {
        return client.subscribeFor(*eventTypes, listener = filterRelevantEvents(listener))
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

    public fun subscribeForSingle(
        eventType: String,
        listener: ChatEventListener<ChatEvent>,
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

    @Suppress("ComplexMethod")
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
    @JvmOverloads
    public fun deleteMessage(messageId: String, hard: Boolean = false): Call<Message> {
        return client.deleteMessage(messageId, hard)
    }

    /**
     * Sends the message to the given channel with side effects if there is any plugin added in the client.
     *
     * @param message Message to send.
     * @param isRetrying True if this message is being retried.
     *
     * @return Executable async [Call] responsible for sending a message.
     */
    @CheckResult
    @JvmOverloads
    public fun sendMessage(message: Message, isRetrying: Boolean = false): Call<Message> {
        return client.sendMessage(channelType, channelId, message, isRetrying)
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

    /**
     * Hides the channel.
     * @see [ChatClient.hideChannel]
     *
     * @param clearHistory Boolean, if you want to clear the history of this channel or not.
     *
     * @return Executable async [Call] responsible for hiding a channel.
     */
    @CheckResult
    public fun hide(clearHistory: Boolean = false): Call<Unit> {
        return client.hideChannel(channelType, channelId, clearHistory)
    }

    /**
     * Removes all of the messages of the channel but doesn't affect the channel data or members.
     *
     * @param systemMessage The system message object that will be shown in the channel.
     *
     * @return Executable async [Call] which completes with [Result] having data equal to the truncated channel
     * if the channel was successfully truncated.
     */
    @CheckResult
    @JvmOverloads
    public fun truncate(systemMessage: Message? = null): Call<Channel> {
        return client.truncateChannel(channelType, channelId, systemMessage)
    }

    /**
     * Uploads a file for the given channel. Progress can be accessed via [callback].
     *
     * The Stream CDN imposes the following restrictions on file uploads:
     * - The maximum file size is 20 MB
     *
     * @param file The file that needs to be uploaded.
     * @param callback The callback to track progress.
     *
     * @return Executable async [Call] which completes with [Result] having data equal to the URL of the uploaded file
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
     * @param file The image file that needs to be uploaded.
     * @param callback The callback to track progress.
     *
     * @return Executable async [Call] which completes with [Result] having data equal to the URL of the uploaded image
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
     * @param url The URL of the file to be deleted.
     *
     * @return Executable async [Call] responsible for deleting a file.
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
     * @param url The URL of the image to be deleted.
     *
     * @return Executable async [Call] responsible for deleting an image.
     *
     * @see FileUploader
     * @see <a href="https://getstream.io/chat/docs/android/file_uploads/?language=kotlin">File Uploads</a>
     */
    @CheckResult
    public fun deleteImage(url: String): Call<Unit> {
        return client.deleteImage(channelType, channelId, url)
    }

    /**
     * Sends the reaction.
     * Use [enforceUnique] parameter to specify whether the reaction should replace other reactions added by the
     * current user.
     *
     * @see [ChatClient.sendReaction]
     *
     * @param reaction The [Reaction] to send.
     * @param enforceUnique Flag to determine whether the reaction should replace other ones added by the current user.
     *
     * @return Executable async [Call] responsible for sending the reaction.
     */
    @CheckResult
    public fun sendReaction(reaction: Reaction, enforceUnique: Boolean = false): Call<Reaction> {
        return client.sendReaction(reaction, enforceUnique)
    }

    @CheckResult
    public fun sendAction(request: SendActionRequest): Call<Message> {
        return client.sendAction(request)
    }

    /**
     * Deletes the reaction associated with the message with the given message id.
     *
     * @see [ChatClient.deleteReaction]
     *
     * @param messageId The id of the message to which reaction belongs.
     * @param reactionType The type of reaction.
     *
     * @return Executable async [Call] responsible for deleting the reaction.
     */
    @CheckResult
    public fun deleteReaction(messageId: String, reactionType: String): Call<Message> {
        return client.deleteReaction(messageId = messageId, reactionType = reactionType, cid = cid)
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

    /**
     * Updates all of the channel data. Any data that is present on the channel and not included in a full update
     * will be deleted.
     *
     * @param message The message object allowing you to show a system message in the channel.
     * @param extraData The updated channel extra data.
     *
     * @return Executable async [Call] responsible for updating channel data.
     */
    @CheckResult
    public fun update(message: Message? = null, extraData: Map<String, Any> = emptyMap()): Call<Channel> {
        return client.updateChannel(channelType, channelId, message, extraData)
    }

    /**
     * Updates specific fields of channel data retaining the custom data fields which were set previously.
     *
     * @param set The key-value data which will be added to the existing channel data object.
     * @param unset The list of fields which will be removed from the existing channel data object.
     */
    @CheckResult
    public fun updatePartial(set: Map<String, Any> = emptyMap(), unset: List<String> = emptyList()): Call<Channel> {
        return client.updateChannelPartial(channelType, channelId, set, unset)
    }

    /**
     * Enables slow mode for the channel. When slow mode is enabled, users can only send a message every
     * [cooldownTimeInSeconds] time interval. The [cooldownTimeInSeconds] is specified in seconds, and should be
     * between 1-120.
     *
     * @param cooldownTimeInSeconds The duration of the time interval users have to wait between messages.
     *
     * @return Executable async [Call] responsible for enabling slow mode.
     */
    @CheckResult
    public fun enableSlowMode(cooldownTimeInSeconds: Int): Call<Channel> =
        client.enableSlowMode(channelType, channelId, cooldownTimeInSeconds)

    /**
     * Disables slow mode for the channel.
     *
     * @return Executable async [Call] responsible for disabling slow mode.
     */
    @CheckResult
    public fun disableSlowMode(): Call<Channel> =
        client.disableSlowMode(channelType, channelId)

    /**
     * Adds members to a given channel.
     *
     * @see [ChatClient.addMembers]
     *
     * @param memberIds The list of the member ids to be added.
     * @param systemMessage The system message object that will be shown in the channel.
     *
     * @return Executable async [Call] responsible for adding the members.
     */
    @CheckResult
    public fun addMembers(memberIds: List<String>, systemMessage: Message? = null): Call<Channel> {
        return client.addMembers(
            channelType = channelType,
            channelId = channelId,
            memberIds = memberIds,
            systemMessage = systemMessage,
        )
    }

    /**
     * Removes members from a given channel.
     *
     * @see [ChatClient.removeMembers]
     *
     * @param memberIds The list of the member ids to be removed.
     * @param systemMessage The system message object that will be shown in the channel.
     *
     * @return Executable async [Call] responsible for removing the members.
     */
    @CheckResult
    public fun removeMembers(memberIds: List<String>, systemMessage: Message? = null): Call<Channel> {
        return client.removeMembers(
            channelType = channelType,
            channelId = channelId,
            memberIds = memberIds,
            systemMessage = systemMessage,
        )
    }

    @CheckResult
    public fun acceptInvite(message: String?): Call<Channel> {
        return client.acceptInvite(channelType, channelId, message)
    }

    @CheckResult
    public fun rejectInvite(): Call<Channel> {
        return client.rejectInvite(channelType, channelId)
    }

    /**
     * Mutes a channel for the current user. Messages added to the channel will not trigger
     * push notifications, and will not change the unread count for the users that muted it.
     * By default, mutes stay in place indefinitely until the user removes it. However, you
     * can optionally set an expiration time. Triggers `notification.channel_mutes_updated`
     * event.
     *
     * @param expiration The duration of mute in **millis**.
     *
     * @return Executable async [Call] responsible for muting a channel.
     *
     * @see [NotificationChannelMutesUpdatedEvent]
     */
    @JvmOverloads
    @CheckResult
    public fun mute(expiration: Int? = null): Call<Unit> {
        return client.muteChannel(channelType, channelId, expiration)
    }

    /**
     * Unmutes a channel for the current user. Triggers `notification.channel_mutes_updated`
     * event.
     *
     * @return Executable async [Call] responsible for unmuting a channel.
     *
     * @see [NotificationChannelMutesUpdatedEvent]
     */
    @CheckResult
    public fun unmute(): Call<Unit> {
        return client.unmuteChannel(channelType, channelId)
    }

    /**
     * Mutes a user. Messages from muted users will not trigger push notifications. By default,
     * mutes stay in place indefinitely until the user removes it. However, you can optionally
     * set a mute timeout. Triggers `notification.mutes_updated` event.
     *
     * @param userId The user id to mute.
     * @param timeout The timeout in **minutes** until the mute is expired.
     *
     * @return Executable async [Call] responsible for muting a user.
     *
     * @see [NotificationMutesUpdatedEvent]
     */
    @JvmOverloads
    @CheckResult
    public fun muteUser(userId: String, timeout: Int? = null): Call<Mute> {
        return client.muteUser(userId, timeout)
    }

    /**
     * Unmutes a previously muted user. Triggers `notification.mutes_updated` event.
     *
     * @param userId The user id to unmute.
     *
     * @return Executable async [Call] responsible for unmuting a user.
     *
     * @see [NotificationMutesUpdatedEvent]
     */
    @CheckResult
    public fun unmuteUser(userId: String): Call<Unit> {
        return client.unmuteUser(userId)
    }

    @CheckResult
    public fun muteCurrentUser(): Call<Mute> {
        return client.muteCurrentUser()
    }

    @CheckResult
    public fun unmuteCurrentUser(): Call<Unit> {
        return client.unmuteCurrentUser()
    }

    /**
     * Sends a start typing event [EventType.TYPING_START] in this channel to the server.
     *
     * @param parentId Set this field to `message.id` to indicate that typing event is happening in a thread.
     *
     * @return Executable async [Call] which completes with [Result] having [ChatEvent] data if successful or
     * [ChatError] if fails.
     */
    @CheckResult
    @JvmOverloads
    public fun keystroke(parentId: String? = null): Call<ChatEvent> {
        return client.keystroke(channelType, channelId, parentId)
    }

    /**
     * Sends a stop typing event [EventType.TYPING_STOP] in this channel to the server.
     *
     * @param parentId Set this field to `message.id` to indicate that typing event is happening in a thread.
     *
     * @return Executable async [Call] which completes with [Result] having [ChatEvent] data if successful or
     * [ChatError] if fails.
     */
    @CheckResult
    @JvmOverloads
    public fun stopTyping(parentId: String? = null): Call<ChatEvent> {
        return client.stopTyping(channelType, channelId, parentId)
    }

    /**
     * Sends an event to all users watching the channel.
     *
     * @param eventType The event name.
     * @param extraData The event payload.
     *
     * @return Executable async [Call] responsible for sending an event.
     */
    @CheckResult
    public fun sendEvent(
        eventType: String,
        extraData: Map<Any, Any> = emptyMap(),
    ): Call<ChatEvent> {
        return client.sendEvent(eventType, channelType, channelId, extraData)
    }

    /**
     * Queries members for this channel.
     *
     * @param offset Offset limit.
     * @param limit Number of members to fetch.
     * @param filter [FilterObject] to filter members of certain type.
     * @param sort Sort the list of members.
     * @param members List of members to search in distinct channels.
     *
     * @return [Call] with a list of members or an error.
     */
    @CheckResult
    public fun queryMembers(
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySort<Member>,
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

    /**
     * Returns a [Call] with messages that contain at least one desired type attachment but
     * not necessarily all of them will have a specified type.
     *
     * @param offset The messages offset.
     * @param limit Max limit messages to be fetched.
     * @param types Desired attachment's types list.
     */
    @CheckResult
    public fun getMessagesWithAttachments(offset: Int, limit: Int, types: List<String>): Call<List<Message>> {
        return client.getMessagesWithAttachments(
            channelType = channelType,
            channelId = channelId,
            offset = offset,
            limit = limit,
            types = types,
        )
    }

    /**
     * Returns a list of messages pinned in the channel.
     * You can sort the list by specifying [sort] parameter.
     * Keep in mind that for now we only support sorting by [Message.pinnedAt].
     * The list can be paginated in a few different ways using [limit] and [pagination].
     * @see [PinnedMessagesPagination]
     *
     * @param limit Max limit of messages to be fetched.
     * @param sort Parameter by which we sort the messages.
     * @param pagination Provides different options for pagination.
     *
     * @return Executable async [Call] responsible for getting pinned messages.
     */
    @CheckResult
    public fun getPinnedMessages(
        limit: Int,
        sort: QuerySort<Message>,
        pagination: PinnedMessagesPagination,
    ): Call<List<Message>> {
        return client.getPinnedMessages(
            channelType = channelType,
            channelId = channelId,
            limit = limit,
            sort = sort,
            pagination = pagination,
        )
    }

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
}
