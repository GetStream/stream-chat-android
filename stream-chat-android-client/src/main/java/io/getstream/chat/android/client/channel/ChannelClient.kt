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
import io.getstream.chat.android.client.api.models.PinnedMessagesPagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.SendActionRequest
import io.getstream.chat.android.client.api.models.WatchChannelRequest
import io.getstream.chat.android.client.events.AIIndicatorClearEvent
import io.getstream.chat.android.client.events.AIIndicatorStopEvent
import io.getstream.chat.android.client.events.AIIndicatorUpdatedEvent
import io.getstream.chat.android.client.events.AnswerCastedEvent
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
import io.getstream.chat.android.client.events.ConnectionErrorEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.DraftMessageDeletedEvent
import io.getstream.chat.android.client.events.DraftMessageUpdatedEvent
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
import io.getstream.chat.android.client.events.NotificationMarkUnreadEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.NotificationMutesUpdatedEvent
import io.getstream.chat.android.client.events.NotificationReminderDueEvent
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.events.NotificationThreadMessageNewEvent
import io.getstream.chat.android.client.events.PollClosedEvent
import io.getstream.chat.android.client.events.PollDeletedEvent
import io.getstream.chat.android.client.events.PollUpdatedEvent
import io.getstream.chat.android.client.events.ReactionDeletedEvent
import io.getstream.chat.android.client.events.ReactionNewEvent
import io.getstream.chat.android.client.events.ReactionUpdateEvent
import io.getstream.chat.android.client.events.ReminderCreatedEvent
import io.getstream.chat.android.client.events.ReminderDeletedEvent
import io.getstream.chat.android.client.events.ReminderUpdatedEvent
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.TypingStopEvent
import io.getstream.chat.android.client.events.UnknownEvent
import io.getstream.chat.android.client.events.UserDeletedEvent
import io.getstream.chat.android.client.events.UserMessagesDeletedEvent
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.events.UserUpdatedEvent
import io.getstream.chat.android.client.events.VoteCastedEvent
import io.getstream.chat.android.client.events.VoteChangedEvent
import io.getstream.chat.android.client.events.VoteRemovedEvent
import io.getstream.chat.android.client.query.AddMembersParams
import io.getstream.chat.android.client.query.CreateChannelParams
import io.getstream.chat.android.client.uploader.FileUploader
import io.getstream.chat.android.client.uploader.StreamCdnImageMimeTypes
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.observable.Disposable
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.BannedUser
import io.getstream.chat.android.models.BannedUsersSort
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Mute
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.UploadedFile
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.call.Call
import java.io.File
import java.util.Date

/**
 * Client for performing actions related to a specific channel.
 *
 * @param channelType The type of the channel.
 * @param channelId The id of the channel.
 * @param client The ChatClient instance.
 */
@Suppress("TooManyFunctions")
public class ChannelClient internal constructor(
    public val channelType: String,
    public val channelId: String,
    private val client: ChatClient,
) {

    /**
     * The channel id in the format of `channelType:channelId`.
     */
    public val cid: String = "$channelType:$channelId"

    /**
     * Gets the channel.
     *
     * @param messageLimit The number of messages to retrieve.
     * @param memberLimit The number of members to retrieve.
     * @param state Whether to retrieve the channel state or not.
     */
    public fun get(
        messageLimit: Int = 0,
        memberLimit: Int = 0,
        state: Boolean = false,
    ): Call<Channel> = client.getChannel(
        cid = cid,
        messageLimit = messageLimit,
        memberLimit = memberLimit,
        state = state,
    )

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
    public fun create(memberIds: List<String>, extraData: Map<String, Any>): Call<Channel> = client.createChannel(
        channelType = channelType,
        channelId = channelId,
        memberIds = memberIds,
        extraData = extraData,
    )

    /**
     * Creates the id-based channel.
     * @see [ChatClient.createChannel]
     *
     * @param params The [CreateChannelParams] holding the data required for creating a channel.
     *
     * @return Executable async [Call] responsible for creating the channel.
     */
    @CheckResult
    public fun create(params: CreateChannelParams): Call<Channel> = client.createChannel(
        channelType = channelType,
        channelId = channelId,
        params = params,
    )

    public fun subscribe(listener: ChatEventListener<ChatEvent>): Disposable = client.subscribe(filterRelevantEvents(listener))

    public fun subscribeFor(
        vararg eventTypes: String,
        listener: ChatEventListener<ChatEvent>,
    ): Disposable = client.subscribeFor(*eventTypes, listener = filterRelevantEvents(listener))

    public fun subscribeFor(
        lifecycleOwner: LifecycleOwner,
        vararg eventTypes: String,
        listener: ChatEventListener<ChatEvent>,
    ): Disposable = client.subscribeFor(
        lifecycleOwner,
        *eventTypes,
        listener = filterRelevantEvents(listener),
    )

    public fun subscribeFor(
        vararg eventTypes: Class<out ChatEvent>,
        listener: ChatEventListener<ChatEvent>,
    ): Disposable = client.subscribeFor(*eventTypes, listener = filterRelevantEvents(listener))

    public fun subscribeFor(
        lifecycleOwner: LifecycleOwner,
        vararg eventTypes: Class<out ChatEvent>,
        listener: ChatEventListener<ChatEvent>,
    ): Disposable = client.subscribeFor(
        lifecycleOwner,
        *eventTypes,
        listener = filterRelevantEvents(listener),
    )

    public fun subscribeForSingle(
        eventType: String,
        listener: ChatEventListener<ChatEvent>,
    ): Disposable = client.subscribeForSingle(eventType, listener = filterRelevantEvents(listener))

    public fun <T : ChatEvent> subscribeForSingle(
        eventType: Class<T>,
        listener: ChatEventListener<T>,
    ): Disposable = client.subscribeForSingle(eventType, listener = filterRelevantEvents(listener))

    private fun <T : ChatEvent> filterRelevantEvents(
        listener: ChatEventListener<T>,
    ): ChatEventListener<T> = ChatEventListener { event: T ->
        if (isRelevantForChannel(event)) {
            listener.onEvent(event)
        }
    }

    @Suppress("ComplexMethod", "LongMethod")
    private fun isRelevantForChannel(event: ChatEvent): Boolean = when (event) {
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
        is NotificationMarkUnreadEvent -> event.cid == cid
        is NotificationMessageNewEvent -> event.cid == cid
        is NotificationThreadMessageNewEvent -> event.cid == cid
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
        is PollClosedEvent -> event.cid == cid
        is PollDeletedEvent -> event.cid == cid
        is PollUpdatedEvent -> event.cid == cid
        is VoteCastedEvent -> event.cid == cid
        is VoteChangedEvent -> event.cid == cid
        is VoteRemovedEvent -> event.cid == cid
        is AnswerCastedEvent -> event.cid == cid
        is UnknownEvent -> event.rawData["cid"] == cid
        is AIIndicatorUpdatedEvent -> event.cid == cid
        is AIIndicatorClearEvent -> event.cid == cid
        is AIIndicatorStopEvent -> event.cid == cid
        is DraftMessageUpdatedEvent -> event.draftMessage.cid == cid
        is DraftMessageDeletedEvent -> event.draftMessage.cid == cid
        is ReminderCreatedEvent -> event.cid == cid
        is ReminderUpdatedEvent -> event.cid == cid
        is ReminderDeletedEvent -> event.cid == cid
        is NotificationReminderDueEvent -> event.cid == cid
        is UserMessagesDeletedEvent -> event.cid == cid
        is HealthEvent,
        is NotificationChannelMutesUpdatedEvent,
        is NotificationMutesUpdatedEvent,
        is GlobalUserBannedEvent,
        is UserDeletedEvent,
        is UserPresenceChangedEvent,
        is GlobalUserUnbannedEvent,
        is UserUpdatedEvent,
        is ConnectedEvent,
        is ConnectionErrorEvent,
        is ConnectingEvent,
        is DisconnectedEvent,
        is ErrorEvent,
        is MarkAllReadEvent,
        -> false
    }

    @CheckResult
    public fun query(request: QueryChannelRequest): Call<Channel> = client.queryChannel(channelType, channelId, request)

    @CheckResult
    public fun watch(request: WatchChannelRequest): Call<Channel> = client.queryChannel(channelType, channelId, request)

    @CheckResult
    public fun watch(data: Map<String, Any>): Call<Channel> {
        val request = WatchChannelRequest()
        request.data.putAll(data)
        return watch(request)
    }

    @CheckResult
    public fun watch(): Call<Channel> = client.queryChannel(channelType, channelId, WatchChannelRequest())

    @CheckResult
    public fun stopWatching(): Call<Unit> = client.stopWatching(channelType, channelId)

    @CheckResult
    public fun getMessage(messageId: String): Call<Message> = client.getMessage(messageId)

    @CheckResult
    public fun updateMessage(message: Message): Call<Message> = client.updateMessage(message)

    @CheckResult
    @JvmOverloads
    public fun deleteMessage(messageId: String, hard: Boolean = false): Call<Message> = client.deleteMessage(messageId, hard)

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
    public fun sendMessage(message: Message, isRetrying: Boolean = false): Call<Message> = client.sendMessage(channelType, channelId, message, isRetrying)

    @CheckResult
    public fun banUser(targetId: String, reason: String?, timeout: Int?): Call<Unit> = client.banUser(
        targetId = targetId,
        channelType = channelType,
        channelId = channelId,
        reason = reason,
        timeout = timeout,
    )

    @CheckResult
    public fun unbanUser(targetId: String): Call<Unit> = client.unbanUser(
        targetId = targetId,
        channelType = channelType,
        channelId = channelId,
    )

    @CheckResult
    public fun shadowBanUser(targetId: String, reason: String?, timeout: Int?): Call<Unit> = client.shadowBanUser(
        targetId = targetId,
        channelType = channelType,
        channelId = channelId,
        reason = reason,
        timeout = timeout,
    )

    @CheckResult
    public fun removeShadowBan(targetId: String): Call<Unit> = client.removeShadowBan(
        targetId = targetId,
        channelType = channelType,
        channelId = channelId,
    )

    @CheckResult
    @JvmOverloads
    public fun queryBannedUsers(
        filter: FilterObject? = null,
        sort: QuerySorter<BannedUsersSort> = QuerySortByField.ascByName("created_at"),
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
    public fun markMessageRead(messageId: String): Call<Unit> = client.markMessageRead(channelType, channelId, messageId)

    /**
     * Marks a given thread in the channel as read.
     *
     * @param threadId The ID of the thread to mark as read.
     */
    @CheckResult
    public fun markThreadRead(threadId: String): Call<Unit> = client.markThreadRead(channelType, channelId, threadId)

    @CheckResult
    public fun markUnread(messageId: String): Call<Unit> = client.markUnread(channelType, channelId, messageId)

    /**
     * Marks a given thread in the channel starting from the given message as unread.
     *
     * @param messageId Id of the message from where the thread should be marked as unread.
     * @param threadId Id of the thread to mark as unread.
     */
    @CheckResult
    public fun markThreadUnread(threadId: String, messageId: String): Call<Unit> = client.markThreadUnread(channelType, channelId, threadId = threadId, messageId = messageId)

    @CheckResult
    public fun markRead(): Call<Unit> = client.markRead(channelType, channelId)

    @CheckResult
    public fun delete(): Call<Channel> = client.deleteChannel(channelType, channelId)

    @CheckResult
    public fun show(): Call<Unit> = client.showChannel(channelType, channelId)

    /**
     * Hides the channel.
     * @see [ChatClient.hideChannel]
     *
     * @param clearHistory Boolean, if you want to clear the history of this channel or not.
     *
     * @return Executable async [Call] responsible for hiding a channel.
     */
    @CheckResult
    public fun hide(clearHistory: Boolean = false): Call<Unit> = client.hideChannel(channelType, channelId, clearHistory)

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
    public fun truncate(systemMessage: Message? = null): Call<Channel> = client.truncateChannel(channelType, channelId, systemMessage)

    /**
     * Uploads a file for the given channel. Progress can be accessed via [callback].
     *
     * The Stream CDN imposes the following restrictions on file uploads:
     * - The maximum file size is 100 MB
     *
     * @param file The file that needs to be uploaded.
     * @param callback The callback to track progress.
     *
     * @return Executable async [Call] which completes with [Result] containing an instance of [UploadedFile]
     * if the file was successfully uploaded.
     *
     * @see FileUploader
     * @see <a href="https://getstream.io/chat/docs/android/file_uploads/?language=kotlin">File Uploads</a>
     */
    @CheckResult
    @JvmOverloads
    public fun sendFile(file: File, callback: ProgressCallback? = null): Call<UploadedFile> = client.sendFile(channelType, channelId, file, callback)

    /**
     * Uploads an image for the given channel. Progress can be accessed via [callback].
     *
     * The Stream CDN imposes the following restrictions on image uploads:
     * - The maximum image size is 100 MB
     * - Supported MIME types are listed in [StreamCdnImageMimeTypes.SUPPORTED_IMAGE_MIME_TYPES]
     *
     * @param file The image file that needs to be uploaded.
     * @param callback The callback to track progress.
     *
     * @return Executable async [Call] which completes with [Result] containing an instance of [UploadedFile]
     * if the image was successfully uploaded.
     *
     * @see FileUploader
     * @see StreamCdnImageMimeTypes.SUPPORTED_IMAGE_MIME_TYPES
     * @see <a href="https://getstream.io/chat/docs/android/file_uploads/?language=kotlin">File Uploads</a>
     */
    @CheckResult
    @JvmOverloads
    public fun sendImage(file: File, callback: ProgressCallback? = null): Call<UploadedFile> = client.sendImage(channelType, channelId, file, callback)

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
    public fun deleteFile(url: String): Call<Unit> = client.deleteFile(channelType, channelId, url)

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
    public fun deleteImage(url: String): Call<Unit> = client.deleteImage(channelType, channelId, url)

    /**
     * Sends the reaction.
     * Use [enforceUnique] parameter to specify whether the reaction should replace other reactions added by the
     * current user.
     *
     * @see [ChatClient.sendReaction]
     *
     * @param reaction The [Reaction] to send.
     * @param enforceUnique Flag to determine whether the reaction should replace other ones added by the current user.
     * @param skipPush If set to "true", skips sending push notification when reacting to a message.
     *
     * @return Executable async [Call] responsible for sending the reaction.
     */
    @CheckResult
    @JvmOverloads
    public fun sendReaction(
        reaction: Reaction,
        enforceUnique: Boolean = false,
        skipPush: Boolean = false,
    ): Call<Reaction> = client.sendReaction(reaction, enforceUnique, cid, skipPush)

    @CheckResult
    public fun sendAction(request: SendActionRequest): Call<Message> = client.sendAction(request)

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
    public fun deleteReaction(messageId: String, reactionType: String): Call<Message> = client.deleteReaction(messageId = messageId, reactionType = reactionType, cid = cid)

    @CheckResult
    public fun getReactions(messageId: String, offset: Int, limit: Int): Call<List<Reaction>> = client.getReactions(messageId, offset, limit)

    @CheckResult
    public fun getReactions(
        messageId: String,
        firstReactionId: String,
        limit: Int,
    ): Call<List<Message>> = client.getRepliesMore(messageId, firstReactionId, limit)

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
    public fun update(message: Message? = null, extraData: Map<String, Any> = emptyMap()): Call<Channel> = client.updateChannel(channelType, channelId, message, extraData)

    /**
     * Updates specific fields of channel data retaining the custom data fields which were set previously.
     *
     * @param set The key-value data which will be added to the existing channel data object.
     * @param unset The list of fields which will be removed from the existing channel data object.
     */
    @CheckResult
    public fun updatePartial(set: Map<String, Any> = emptyMap(), unset: List<String> = emptyList()): Call<Channel> = client.updateChannelPartial(channelType, channelId, set, unset)

    /**
     * Updates specific fields of custom data for a given member.
     *
     * @param userId The user id of the member to update.
     * @param set The key-value data to be updated in the member data.
     * @param unset The list of keys to be removed from the member data.
     */
    @CheckResult
    public fun partialUpdateMember(
        userId: String,
        set: Map<String, Any> = emptyMap(),
        unset: List<String> = emptyList(),
    ): Call<Member> = client.partialUpdateMember(channelType, channelId, userId, set, unset)

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
    public fun enableSlowMode(cooldownTimeInSeconds: Int): Call<Channel> = client.enableSlowMode(channelType, channelId, cooldownTimeInSeconds)

    /**
     * Disables slow mode for the channel.
     *
     * @return Executable async [Call] responsible for disabling slow mode.
     */
    @CheckResult
    public fun disableSlowMode(): Call<Channel> = client.disableSlowMode(channelType, channelId)

    /**
     * Adds members to a given channel.
     *
     * @see [ChatClient.addMembers]
     *
     * @param memberIds The list of the member ids to be added.
     * @param systemMessage The system message object that will be shown in the channel.
     * @param hideHistory Hides the history of the channel to the added member.
     * @param skipPush Skip sending push notifications.
     *
     * @return Executable async [Call] responsible for adding the members.
     */
    @CheckResult
    public fun addMembers(
        memberIds: List<String>,
        systemMessage: Message? = null,
        hideHistory: Boolean? = null,
        skipPush: Boolean? = null,
    ): Call<Channel> = client.addMembers(
        channelType = channelType,
        channelId = channelId,
        memberIds = memberIds,
        systemMessage = systemMessage,
        hideHistory = hideHistory,
        skipPush = skipPush,
    )

    /**
     * Adds members with extra data to a given channel.
     * @see [ChatClient.addMembers]
     *
     * @param params The [AddMembersParams] holding data about the members to be added.
     *
     * @return Executable async [Call] responsible for adding the members.
     */
    @CheckResult
    public fun addMembers(params: AddMembersParams): Call<Channel> = client.addMembers(
        channelType = channelType,
        channelId = channelId,
        params = params,
    )

    /**
     * Removes members from a given channel.
     *
     * @see [ChatClient.removeMembers]
     *
     * @param memberIds The list of the member ids to be removed.
     * @param systemMessage The system message object that will be shown in the channel.
     * @param skipPush Skip sending push notifications.
     *
     * @return Executable async [Call] responsible for removing the members.
     */
    @CheckResult
    public fun removeMembers(
        memberIds: List<String>,
        systemMessage: Message? = null,
        skipPush: Boolean? = null,
    ): Call<Channel> = client.removeMembers(
        channelType = channelType,
        channelId = channelId,
        memberIds = memberIds,
        systemMessage = systemMessage,
        skipPush = skipPush,
    )

    /**
     * Invites members to a given channel.
     *
     * @see [ChatClient.inviteMembers]
     *
     * @param memberIds The list of the member ids to be invited.
     * @param systemMessage The system message object that will be shown in the channel.
     * @param skipPush Skip sending push notifications.
     *
     * @return Executable async [Call] responsible for inviting the members.
     */
    @CheckResult
    public fun inviteMembers(
        memberIds: List<String>,
        systemMessage: Message? = null,
        skipPush: Boolean? = null,
    ): Call<Channel> = client.inviteMembers(
        channelType = channelType,
        channelId = channelId,
        memberIds = memberIds,
        systemMessage = systemMessage,
        skipPush = skipPush,
    )

    @CheckResult
    public fun acceptInvite(message: String?): Call<Channel> = client.acceptInvite(channelType, channelId, message)

    @CheckResult
    public fun rejectInvite(): Call<Channel> = client.rejectInvite(channelType, channelId)

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
    public fun mute(expiration: Int? = null): Call<Unit> = client.muteChannel(channelType, channelId, expiration)

    /**
     * Unmutes a channel for the current user. Triggers `notification.channel_mutes_updated`
     * event.
     *
     * @return Executable async [Call] responsible for unmuting a channel.
     *
     * @see [NotificationChannelMutesUpdatedEvent]
     */
    @CheckResult
    public fun unmute(): Call<Unit> = client.unmuteChannel(channelType, channelId)

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
    public fun muteUser(userId: String, timeout: Int? = null): Call<Mute> = client.muteUser(userId, timeout)

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
    public fun unmuteUser(userId: String): Call<Unit> = client.unmuteUser(userId)

    @CheckResult
    public fun muteCurrentUser(): Call<Mute> = client.muteCurrentUser()

    @CheckResult
    public fun unmuteCurrentUser(): Call<Unit> = client.unmuteCurrentUser()

    /**
     * Sends a start typing event [EventType.TYPING_START] in this channel to the server.
     *
     * @param parentId Set this field to `message.id` to indicate that typing event is happening in a thread.
     *
     * @return Executable async [Call] which completes with [Result] having [ChatEvent] data if successful or
     * [Error] if fails.
     */
    @CheckResult
    @JvmOverloads
    public fun keystroke(parentId: String? = null): Call<ChatEvent> = client.keystroke(channelType, channelId, parentId)

    /**
     * Sends a stop typing event [EventType.TYPING_STOP] in this channel to the server.
     *
     * @param parentId Set this field to `message.id` to indicate that typing event is happening in a thread.
     *
     * @return Executable async [Call] which completes with [Result] having [ChatEvent] data if successful or
     * [Error] if fails.
     */
    @CheckResult
    @JvmOverloads
    public fun stopTyping(parentId: String? = null): Call<ChatEvent> = client.stopTyping(channelType, channelId, parentId)

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
    ): Call<ChatEvent> = client.sendEvent(eventType, channelType, channelId, extraData)

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
        sort: QuerySorter<Member>,
        members: List<Member> = emptyList(),
    ): Call<List<Member>> = client.queryMembers(channelType, channelId, offset, limit, filter, sort, members)

    @CheckResult
    public fun getFileAttachments(offset: Int, limit: Int): Call<List<Attachment>> = client.getFileAttachments(channelType, channelId, offset, limit)

    @CheckResult
    public fun getImageAttachments(offset: Int, limit: Int): Call<List<Attachment>> = client.getImageAttachments(channelType, channelId, offset, limit)

    /**
     * Returns a [Call] with messages that contain at least one desired type attachment but
     * not necessarily all of them will have a specified type.
     *
     * @param offset The messages offset.
     * @param limit Max limit messages to be fetched.
     * @param types Desired attachment's types list.
     */
    @CheckResult
    public fun getMessagesWithAttachments(offset: Int, limit: Int, types: List<String>): Call<List<Message>> = client.getMessagesWithAttachments(
        channelType = channelType,
        channelId = channelId,
        offset = offset,
        limit = limit,
        types = types,
    )

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
        sort: QuerySorter<Message>,
        pagination: PinnedMessagesPagination,
    ): Call<List<Message>> = client.getPinnedMessages(
        channelType = channelType,
        channelId = channelId,
        limit = limit,
        sort = sort,
        pagination = pagination,
    )

    @CheckResult
    public fun pinMessage(message: Message, expirationDate: Date?): Call<Message> = client.pinMessage(message, expirationDate)

    @CheckResult
    public fun pinMessage(message: Message, timeout: Int): Call<Message> = client.pinMessage(message, timeout)

    @CheckResult
    public fun unpinMessage(message: Message): Call<Message> = client.unpinMessage(message)

    /**
     * Pins the channel for the current user.
     *
     * @return Executable async [Call] responsible for pinning the channel.
     */
    @CheckResult
    public fun pin(): Call<Member> = client.pinChannel(channelType, channelId)

    /**
     * Unpins the channel for the current user.
     *
     * @return Executable async [Call] responsible for unpinning the channel.
     */
    @CheckResult
    public fun unpin(): Call<Member> = client.unpinChannel(channelType, channelId)

    /**
     * Archives the channel for the current user.
     *
     * @return Executable async [Call] responsible for archiving the channel.
     */
    @CheckResult
    public fun archive(): Call<Member> = client.archiveChannel(channelType, channelId)

    /**
     * Un-archives the channel for the current user.
     *
     * @return Executable async [Call] responsible for un-archiving the channel.
     */
    @CheckResult
    public fun unarchive(): Call<Member> = client.unarchiveChannel(channelType, channelId)
}
