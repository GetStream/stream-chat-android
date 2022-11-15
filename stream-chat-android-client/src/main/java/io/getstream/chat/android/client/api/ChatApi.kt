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

package io.getstream.chat.android.client.api

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.api.models.PinnedMessagesPagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.api.models.SearchMessagesRequest
import io.getstream.chat.android.client.api.models.SendActionRequest
import io.getstream.chat.android.client.api.models.querysort.QuerySorter
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.BannedUser
import io.getstream.chat.android.client.models.BannedUsersSort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Flag
import io.getstream.chat.android.client.models.GuestUser
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.SearchMessagesResult
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.models.AppSettings
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.UploadedFile
import io.getstream.chat.android.models.UploadedImage
import io.getstream.chat.android.models.VideoCallInfo
import io.getstream.chat.android.models.VideoCallToken
import okhttp3.ResponseBody
import java.io.File
import java.util.Date

@Suppress("TooManyFunctions", "LongParameterList")
internal interface ChatApi {

    fun setConnection(userId: String, connectionId: String)

    fun appSettings(): Call<AppSettings>

    @CheckResult
    fun sendFile(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback? = null,
    ): Call<UploadedFile>

    @CheckResult
    fun sendImage(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback? = null,
    ): Call<UploadedImage>

    @CheckResult
    fun deleteFile(channelType: String, channelId: String, url: String): Call<Unit>

    @CheckResult
    fun deleteImage(channelType: String, channelId: String, url: String): Call<Unit>

    @CheckResult
    fun addDevice(device: Device): Call<Unit>

    @CheckResult
    fun deleteDevice(device: Device): Call<Unit>

    @CheckResult
    fun getDevices(): Call<List<Device>>

    @CheckResult
    fun searchMessages(request: SearchMessagesRequest): Call<List<Message>>

    @CheckResult
    fun searchMessages(
        channelFilter: FilterObject,
        messageFilter: FilterObject,
        offset: Int?,
        limit: Int?,
        next: String?,
        sort: QuerySorter<Message>?,
    ): Call<SearchMessagesResult>

    @CheckResult
    fun getRepliesMore(
        messageId: String,
        firstId: String,
        limit: Int,
    ): Call<List<Message>>

    @CheckResult
    fun getReplies(messageId: String, limit: Int): Call<List<Message>>

    @CheckResult
    fun getReactions(
        messageId: String,
        offset: Int,
        limit: Int,
    ): Call<List<Reaction>>

    @CheckResult
    fun sendReaction(reaction: Reaction, enforceUnique: Boolean): Call<Reaction>

    @CheckResult
    fun sendReaction(
        messageId: String,
        reactionType: String,
        enforceUnique: Boolean,
    ): Call<Reaction> {
        return sendReaction(
            reaction = Reaction(
                messageId = messageId,
                type = reactionType,
                score = 0
            ),
            enforceUnique = enforceUnique
        )
    }

    @CheckResult
    fun deleteReaction(messageId: String, reactionType: String): Call<Message>

    @CheckResult
    fun deleteMessage(messageId: String, hard: Boolean = false): Call<Message>

    @CheckResult
    fun sendAction(request: SendActionRequest): Call<Message>

    @CheckResult
    fun getMessage(messageId: String): Call<Message>

    @CheckResult
    fun sendMessage(
        channelType: String,
        channelId: String,
        message: Message,
    ): Call<Message>

    @CheckResult
    fun muteChannel(
        channelType: String,
        channelId: String,
        expiration: Int?,
    ): Call<Unit>

    @CheckResult
    fun unmuteChannel(
        channelType: String,
        channelId: String,
    ): Call<Unit>

    @CheckResult
    fun updateMessage(
        message: Message,
    ): Call<Message>

    @CheckResult
    fun partialUpdateMessage(
        messageId: String,
        set: Map<String, Any>,
        unset: List<String>,
    ): Call<Message>

    @CheckResult
    fun stopWatching(
        channelType: String,
        channelId: String,
    ): Call<Unit>

    @CheckResult
    fun getPinnedMessages(
        channelType: String,
        channelId: String,
        limit: Int,
        sort: QuerySorter<Message>,
        pagination: PinnedMessagesPagination,
    ): Call<List<Message>>

    @CheckResult
    fun queryChannels(query: QueryChannelsRequest): Call<List<Channel>>

    @CheckResult
    fun updateUsers(users: List<User>): Call<List<User>>

    @CheckResult
    fun partialUpdateUser(
        id: String,
        set: Map<String, Any>,
        unset: List<String>,
    ): Call<User>

    fun queryChannel(
        channelType: String,
        channelId: String = "",
        query: QueryChannelRequest,
    ): Call<Channel>

    @CheckResult
    fun updateChannel(
        channelType: String,
        channelId: String,
        extraData: Map<String, Any>,
        updateMessage: Message?,
    ): Call<Channel>

    @CheckResult
    fun updateChannelPartial(
        channelType: String,
        channelId: String,
        set: Map<String, Any>,
        unset: List<String>,
    ): Call<Channel>

    @CheckResult
    fun enableSlowMode(
        channelType: String,
        channelId: String,
        cooldownTimeInSeconds: Int,
    ): Call<Channel>

    @CheckResult
    fun disableSlowMode(
        channelType: String,
        channelId: String,
    ): Call<Channel>

    @CheckResult
    fun markRead(
        channelType: String,
        channelId: String,
        messageId: String = "",
    ): Call<Unit>

    @CheckResult
    fun showChannel(channelType: String, channelId: String): Call<Unit>

    @CheckResult
    fun hideChannel(
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ): Call<Unit>

    @CheckResult
    fun truncateChannel(
        channelType: String,
        channelId: String,
        systemMessage: Message?,
    ): Call<Channel>

    @CheckResult
    fun rejectInvite(channelType: String, channelId: String): Call<Channel>

    @CheckResult
    fun muteCurrentUser(): Call<Mute>

    @CheckResult
    fun unmuteCurrentUser(): Call<Unit>

    @CheckResult
    fun acceptInvite(
        channelType: String,
        channelId: String,
        message: String?,
    ): Call<Channel>

    @CheckResult
    fun deleteChannel(channelType: String, channelId: String): Call<Channel>

    @CheckResult
    fun markAllRead(): Call<Unit>

    @CheckResult
    fun getGuestUser(userId: String, userName: String): Call<GuestUser>

    @CheckResult
    fun queryUsers(queryUsers: QueryUsersRequest): Call<List<User>>

    @CheckResult
    fun addMembers(
        channelType: String,
        channelId: String,
        members: List<String>,
        systemMessage: Message?,
    ): Call<Channel>

    @CheckResult
    fun removeMembers(
        channelType: String,
        channelId: String,
        members: List<String>,
        systemMessage: Message?,
    ): Call<Channel>

    @CheckResult
    fun queryMembers(
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySorter<Member>,
        members: List<Member>,
    ): Call<List<Member>>

    @CheckResult
    fun muteUser(
        userId: String,
        timeout: Int?,
    ): Call<Mute>

    @CheckResult
    fun unmuteUser(
        userId: String,
    ): Call<Unit>

    @CheckResult
    fun flagUser(userId: String): Call<Flag>

    @CheckResult
    fun unflagUser(userId: String): Call<Flag>

    @CheckResult
    fun flagMessage(messageId: String): Call<Flag>

    @CheckResult
    fun unflagMessage(messageId: String): Call<Flag>

    @CheckResult
    fun banUser(
        targetId: String,
        timeout: Int?,
        reason: String?,
        channelType: String,
        channelId: String,
        shadow: Boolean,
    ): Call<Unit>

    @CheckResult
    fun unbanUser(
        targetId: String,
        channelType: String,
        channelId: String,
        shadow: Boolean,
    ): Call<Unit>

    @CheckResult
    fun queryBannedUsers(
        filter: FilterObject,
        sort: QuerySorter<BannedUsersSort>,
        offset: Int?,
        limit: Int?,
        createdAtAfter: Date?,
        createdAtAfterOrEqual: Date?,
        createdAtBefore: Date?,
        createdAtBeforeOrEqual: Date?,
    ): Call<List<BannedUser>>

    @CheckResult
    fun createVideoCall(channelId: String, channelType: String, callId: String, callType: String): Call<VideoCallInfo>

    @CheckResult
    fun getVideoCallToken(callId: String): Call<VideoCallToken>

    @CheckResult
    fun sendEvent(
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>,
    ): Call<ChatEvent>

    @CheckResult
    fun translate(messageId: String, language: String): Call<Message>

    @CheckResult
    fun getSyncHistory(channelIds: List<String>, lastSyncAt: String): Call<List<ChatEvent>>

    @CheckResult
    fun downloadFile(fileUrl: String): Call<ResponseBody>

    fun warmUp()

    fun releseConnection()
}
