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

package io.getstream.chat.android.client.api2

import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.models.GetThreadOptions
import io.getstream.chat.android.client.api.models.PinnedMessagesPagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QueryThreadsRequest
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.api.models.SearchMessagesRequest
import io.getstream.chat.android.client.api2.endpoint.ChannelApi
import io.getstream.chat.android.client.api2.endpoint.ConfigApi
import io.getstream.chat.android.client.api2.endpoint.DeviceApi
import io.getstream.chat.android.client.api2.endpoint.FileDownloadApi
import io.getstream.chat.android.client.api2.endpoint.GeneralApi
import io.getstream.chat.android.client.api2.endpoint.GuestApi
import io.getstream.chat.android.client.api2.endpoint.MessageApi
import io.getstream.chat.android.client.api2.endpoint.ModerationApi
import io.getstream.chat.android.client.api2.endpoint.OpenGraphApi
import io.getstream.chat.android.client.api2.endpoint.PollsApi
import io.getstream.chat.android.client.api2.endpoint.ThreadsApi
import io.getstream.chat.android.client.api2.endpoint.UserApi
import io.getstream.chat.android.client.api2.endpoint.VideoCallApi
import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.api2.mapping.DtoMapping
import io.getstream.chat.android.client.api2.mapping.EventMapping
import io.getstream.chat.android.client.api2.mapping.toDomain
import io.getstream.chat.android.client.api2.model.dto.PartialUpdateUserDto
import io.getstream.chat.android.client.api2.model.requests.AcceptInviteRequest
import io.getstream.chat.android.client.api2.model.requests.AddDeviceRequest
import io.getstream.chat.android.client.api2.model.requests.AddMembersRequest
import io.getstream.chat.android.client.api2.model.requests.BanUserRequest
import io.getstream.chat.android.client.api2.model.requests.BlockUserRequest
import io.getstream.chat.android.client.api2.model.requests.FlagMessageRequest
import io.getstream.chat.android.client.api2.model.requests.FlagRequest
import io.getstream.chat.android.client.api2.model.requests.FlagUserRequest
import io.getstream.chat.android.client.api2.model.requests.GuestUserRequest
import io.getstream.chat.android.client.api2.model.requests.HideChannelRequest
import io.getstream.chat.android.client.api2.model.requests.InviteMembersRequest
import io.getstream.chat.android.client.api2.model.requests.MarkReadRequest
import io.getstream.chat.android.client.api2.model.requests.MarkUnreadRequest
import io.getstream.chat.android.client.api2.model.requests.MuteChannelRequest
import io.getstream.chat.android.client.api2.model.requests.MuteUserRequest
import io.getstream.chat.android.client.api2.model.requests.PartialUpdateMessageRequest
import io.getstream.chat.android.client.api2.model.requests.PartialUpdateThreadRequest
import io.getstream.chat.android.client.api2.model.requests.PartialUpdateUsersRequest
import io.getstream.chat.android.client.api2.model.requests.PinnedMessagesRequest
import io.getstream.chat.android.client.api2.model.requests.PollRequest
import io.getstream.chat.android.client.api2.model.requests.PollUpdateRequest
import io.getstream.chat.android.client.api2.model.requests.PollVoteRequest
import io.getstream.chat.android.client.api2.model.requests.QueryBannedUsersRequest
import io.getstream.chat.android.client.api2.model.requests.ReactionRequest
import io.getstream.chat.android.client.api2.model.requests.RejectInviteRequest
import io.getstream.chat.android.client.api2.model.requests.RemoveMembersRequest
import io.getstream.chat.android.client.api2.model.requests.SendActionRequest
import io.getstream.chat.android.client.api2.model.requests.SendEventRequest
import io.getstream.chat.android.client.api2.model.requests.SendMessageRequest
import io.getstream.chat.android.client.api2.model.requests.SuggestPollOptionRequest
import io.getstream.chat.android.client.api2.model.requests.SyncHistoryRequest
import io.getstream.chat.android.client.api2.model.requests.TruncateChannelRequest
import io.getstream.chat.android.client.api2.model.requests.UnblockUserRequest
import io.getstream.chat.android.client.api2.model.requests.UpdateChannelPartialRequest
import io.getstream.chat.android.client.api2.model.requests.UpdateChannelRequest
import io.getstream.chat.android.client.api2.model.requests.UpdateCooldownRequest
import io.getstream.chat.android.client.api2.model.requests.UpdateMemberPartialRequest
import io.getstream.chat.android.client.api2.model.requests.UpdateMessageRequest
import io.getstream.chat.android.client.api2.model.requests.UpdateUsersRequest
import io.getstream.chat.android.client.api2.model.requests.UpstreamOptionDto
import io.getstream.chat.android.client.api2.model.requests.UpstreamVoteDto
import io.getstream.chat.android.client.api2.model.requests.VideoCallCreateRequest
import io.getstream.chat.android.client.api2.model.requests.VideoCallTokenRequest
import io.getstream.chat.android.client.api2.model.response.ChannelResponse
import io.getstream.chat.android.client.api2.model.response.CreateVideoCallResponse
import io.getstream.chat.android.client.api2.model.response.TranslateMessageRequest
import io.getstream.chat.android.client.api2.model.response.VideoCallTokenResponse
import io.getstream.chat.android.client.call.RetrofitCall
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.extensions.enrichWithCid
import io.getstream.chat.android.client.extensions.syncUnreadCountWithReads
import io.getstream.chat.android.client.helpers.CallPostponeHelper
import io.getstream.chat.android.client.parser.toMap
import io.getstream.chat.android.client.scope.UserScope
import io.getstream.chat.android.client.uploader.FileTransformer
import io.getstream.chat.android.client.uploader.FileUploader
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.models.AppSettings
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.BannedUser
import io.getstream.chat.android.models.BannedUsersSort
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Flag
import io.getstream.chat.android.models.GuestUser
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.MemberData
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Mute
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.PollConfig
import io.getstream.chat.android.models.QueryThreadsResult
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.SearchMessagesResult
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.UploadedFile
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.UserBlock
import io.getstream.chat.android.models.VideoCallInfo
import io.getstream.chat.android.models.VideoCallToken
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.models.VotingVisibility
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.log.taggedLogger
import io.getstream.result.Result
import io.getstream.result.call.Call
import io.getstream.result.call.CoroutineCall
import io.getstream.result.call.map
import io.getstream.result.call.toUnitCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import okhttp3.ResponseBody
import java.io.File
import java.util.Date
import io.getstream.chat.android.client.api.models.SendActionRequest as DomainSendActionRequest

@Suppress("TooManyFunctions", "LargeClass")
internal class MoshiChatApi
@Suppress("LongParameterList")
constructor(
    private val domainMapping: DomainMapping,
    private val eventMapping: EventMapping,
    private val dtoMapping: DtoMapping,
    private val fileUploader: FileUploader,
    private val fileTransformer: FileTransformer,
    private val userApi: UserApi,
    private val guestApi: GuestApi,
    private val messageApi: MessageApi,
    private val channelApi: ChannelApi,
    private val deviceApi: DeviceApi,
    private val moderationApi: ModerationApi,
    private val generalApi: GeneralApi,
    private val configApi: ConfigApi,
    private val callApi: VideoCallApi,
    private val fileDownloadApi: FileDownloadApi,
    private val ogApi: OpenGraphApi,
    private val threadsApi: ThreadsApi,
    private val pollsApi: PollsApi,
    private val coroutineScope: CoroutineScope,
    private val userScope: UserScope,
) : ChatApi {

    private val logger by taggedLogger("Chat:MoshiChatApi")

    private val callPostponeHelper: CallPostponeHelper by lazy {
        CallPostponeHelper(
            awaitConnection = {
                _connectionId.first { id -> id.isNotEmpty() }
            },
            userScope = userScope,
        )
    }

    @Volatile
    private var userId: String = ""
        get() {
            if (field == "") {
                logger.e { "userId accessed before being set. Did you forget to call ChatClient.connectUser()?" }
            }
            return field
        }

    private val _connectionId: MutableStateFlow<String> = MutableStateFlow("")

    private val connectionId: String
        get() {
            if (_connectionId.value == "") {
                logger.e { "connectionId accessed before being set. Did you forget to call ChatClient.connectUser()?" }
            }
            return _connectionId.value
        }

    override fun setConnection(userId: String, connectionId: String) {
        logger.d { "[setConnection] userId: '$userId', connectionId: '$connectionId'" }
        this.userId = userId
        this._connectionId.value = connectionId
    }

    override fun releaseConnection() {
        this._connectionId.value = ""
    }

    override fun appSettings(): Call<AppSettings> =
        configApi.getAppSettings().mapDomain { it.toDomain() }

    override fun sendMessage(
        channelType: String,
        channelId: String,
        message: Message,
    ): Call<Message> = messageApi.sendMessage(
        channelType = channelType,
        channelId = channelId,
        message = SendMessageRequest(
            message = with(dtoMapping) { message.toDto() },
            skip_push = message.skipPushNotification,
            skip_enrich_url = message.skipEnrichUrl,
        ),
    ).mapDomain { response ->
        response.message.toDomain()
    }

    override fun updateMessage(
        message: Message,
    ): Call<Message> {
        return messageApi.updateMessage(
            messageId = message.id,
            message = UpdateMessageRequest(
                message = with(dtoMapping) { message.toDto() },
                skip_enrich_url = message.skipEnrichUrl,
            ),
        ).mapDomain { response ->
            response.message.toDomain()
        }
    }

    override fun partialUpdateMessage(
        messageId: String,
        set: Map<String, Any>,
        unset: List<String>,
        skipEnrichUrl: Boolean,
    ): Call<Message> {
        return messageApi.partialUpdateMessage(
            messageId = messageId,
            body = PartialUpdateMessageRequest(
                set = set,
                unset = unset,
                skip_enrich_url = skipEnrichUrl,
            ),
        ).mapDomain { response ->
            response.message.toDomain()
        }
    }

    override fun getMessage(messageId: String): Call<Message> {
        return messageApi.getMessage(
            messageId = messageId,
        ).mapDomain { response ->
            response.message.toDomain()
        }
    }

    override fun deleteMessage(messageId: String, hard: Boolean): Call<Message> {
        return messageApi.deleteMessage(
            messageId = messageId,
            hard = if (hard) true else null,
        ).mapDomain { response ->
            response.message.toDomain()
        }
    }

    override fun getReactions(
        messageId: String,
        offset: Int,
        limit: Int,
    ): Call<List<Reaction>> {
        return messageApi.getReactions(
            messageId = messageId,
            offset = offset,
            limit = limit,
        ).mapDomain { response ->
            response.reactions.map {
                it.toDomain()
            }
        }
    }

    override fun sendReaction(reaction: Reaction, enforceUnique: Boolean): Call<Reaction> {
        return messageApi.sendReaction(
            messageId = reaction.messageId,
            request = ReactionRequest(
                reaction = with(dtoMapping) { reaction.toDto() },
                enforce_unique = enforceUnique,
            ),
        ).mapDomain { response ->
            response.reaction.toDomain()
        }
    }

    override fun deleteReaction(
        messageId: String,
        reactionType: String,
    ): Call<Message> {
        return messageApi.deleteReaction(
            messageId = messageId,
            reactionType = reactionType,
        ).mapDomain { response ->
            response.message.toDomain()
        }
    }

    override fun addDevice(device: Device): Call<Unit> {
        return deviceApi.addDevices(
            request = AddDeviceRequest(
                device.token,
                device.pushProvider.key,
                device.providerName,
            ),
        ).toUnitCall()
    }

    override fun deleteDevice(device: Device): Call<Unit> {
        return deviceApi.deleteDevice(deviceId = device.token).toUnitCall()
    }

    override fun getDevices(): Call<List<Device>> {
        return deviceApi.getDevices().mapDomain { response ->
            response.devices.map { it.toDomain() }
        }
    }

    override fun muteCurrentUser(): Call<Mute> {
        return muteUser(
            userId = userId,
            timeout = null,
        )
    }

    override fun unmuteCurrentUser(): Call<Unit> {
        return unmuteUser(userId)
    }

    override fun muteUser(
        userId: String,
        timeout: Int?,
    ): Call<Mute> {
        return moderationApi.muteUser(
            body = MuteUserRequest(
                target_id = userId,
                user_id = this.userId,
                timeout = timeout,
            ),
        ).mapDomain { response ->
            response.mute.toDomain()
        }
    }

    override fun unmuteUser(userId: String): Call<Unit> {
        return moderationApi.unmuteUser(
            body = MuteUserRequest(
                target_id = userId,
                user_id = this.userId,
                timeout = null,
            ),
        ).toUnitCall()
    }

    override fun muteChannel(
        channelType: String,
        channelId: String,
        expiration: Int?,
    ): Call<Unit> {
        return moderationApi.muteChannel(
            body = MuteChannelRequest(
                channel_cid = "$channelType:$channelId",
                expiration = expiration,
            ),
        ).toUnitCall()
    }

    override fun unmuteChannel(
        channelType: String,
        channelId: String,
    ): Call<Unit> {
        return moderationApi.unmuteChannel(
            body = MuteChannelRequest(
                channel_cid = "$channelType:$channelId",
                expiration = null,
            ),
        ).toUnitCall()
    }

    override fun sendFile(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback?,
    ): Call<UploadedFile> = CoroutineCall(coroutineScope) {
        fileTransformer.transform(file)
            .let { transformedFile ->
                if (callback != null) {
                    fileUploader.sendFile(
                        channelType = channelType,
                        channelId = channelId,
                        userId = userId,
                        file = transformedFile,
                        callback,
                    )
                } else {
                    fileUploader.sendFile(
                        channelType = channelType,
                        channelId = channelId,
                        userId = userId,
                        file = transformedFile,
                    )
                }
            }
    }

    override fun sendImage(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback?,
    ): Call<UploadedFile> = CoroutineCall(coroutineScope) {
        fileTransformer.transform(file)
            .let { transformedFile ->
                if (callback != null) {
                    fileUploader.sendImage(
                        channelType = channelType,
                        channelId = channelId,
                        userId = userId,
                        file = transformedFile,
                        callback,
                    )
                } else {
                    fileUploader.sendImage(
                        channelType = channelType,
                        channelId = channelId,
                        userId = userId,
                        file = transformedFile,
                    )
                }
            }
    }

    override fun deleteFile(channelType: String, channelId: String, url: String): Call<Unit> {
        return CoroutineCall(coroutineScope) {
            fileUploader.deleteFile(
                channelType = channelType,
                channelId = channelId,
                userId = userId,
                url = url,
            )
            Result.Success(Unit)
        }
    }

    override fun deleteImage(channelType: String, channelId: String, url: String): Call<Unit> {
        return CoroutineCall(coroutineScope) {
            fileUploader.deleteImage(
                channelType = channelType,
                channelId = channelId,
                userId = userId,
                url = url,
            )
            Result.Success(Unit)
        }
    }

    override fun flagUser(
        userId: String,
        reason: String?,
        customData: Map<String, String>,
    ): Call<Flag> =
        flag(
            FlagUserRequest(
                targetUserId = userId,
                reason = reason,
                custom = customData,
            ),
        )

    override fun unflagUser(userId: String): Call<Flag> =
        unflag(mutableMapOf("target_user_id" to userId))

    override fun flagMessage(
        messageId: String,
        reason: String?,
        customData: Map<String, String>,
    ): Call<Flag> =
        flag(
            FlagMessageRequest(
                targetMessageId = messageId,
                reason = reason,
                custom = customData,
            ),
        )

    override fun unflagMessage(messageId: String): Call<Flag> =
        unflag(mutableMapOf("target_message_id" to messageId))

    private fun flag(body: FlagRequest): Call<Flag> {
        return moderationApi.flag(body = body).mapDomain { response ->
            response.flag.toDomain()
        }
    }

    private fun unflag(body: Map<String, String>): Call<Flag> {
        return moderationApi.unflag(body = body).mapDomain { response ->
            response.flag.toDomain()
        }
    }

    override fun banUser(
        targetId: String,
        timeout: Int?,
        reason: String?,
        channelType: String,
        channelId: String,
        shadow: Boolean,
    ): Call<Unit> {
        return moderationApi.banUser(
            body = BanUserRequest(
                target_user_id = targetId,
                timeout = timeout,
                reason = reason,
                type = channelType,
                id = channelId,
                shadow = shadow,
            ),
        ).toUnitCall()
    }

    override fun unbanUser(
        targetId: String,
        channelType: String,
        channelId: String,
        shadow: Boolean,
    ): Call<Unit> {
        return moderationApi.unbanUser(
            targetUserId = targetId,
            channelId = channelId,
            channelType = channelType,
            shadow = shadow,
        ).toUnitCall()
    }

    override fun queryBannedUsers(
        filter: FilterObject,
        sort: QuerySorter<BannedUsersSort>,
        offset: Int?,
        limit: Int?,
        createdAtAfter: Date?,
        createdAtAfterOrEqual: Date?,
        createdAtBefore: Date?,
        createdAtBeforeOrEqual: Date?,
    ): Call<List<BannedUser>> {
        return moderationApi.queryBannedUsers(
            payload = QueryBannedUsersRequest(
                filter_conditions = filter.toMap(),
                sort = sort.toDto(),
                offset = offset,
                limit = limit,
                created_at_after = createdAtAfter,
                created_at_after_or_equal = createdAtAfterOrEqual,
                created_at_before = createdAtBefore,
                created_at_before_or_equal = createdAtBeforeOrEqual,
            ),
        ).mapDomain { response ->
            response.bans.map {
                it.toDomain()
            }
        }
    }

    override fun enableSlowMode(
        channelType: String,
        channelId: String,
        cooldownTimeInSeconds: Int,
    ): Call<Channel> = updateCooldown(
        channelType = channelType,
        channelId = channelId,
        cooldownTimeInSeconds = cooldownTimeInSeconds,
    )

    override fun disableSlowMode(
        channelType: String,
        channelId: String,
    ): Call<Channel> = updateCooldown(
        channelType = channelType,
        channelId = channelId,
        cooldownTimeInSeconds = 0,
    )

    private fun updateCooldown(
        channelType: String,
        channelId: String,
        cooldownTimeInSeconds: Int,
    ): Call<Channel> {
        return channelApi.updateCooldown(
            channelType = channelType,
            channelId = channelId,
            body = UpdateCooldownRequest.create(cooldownTimeInSeconds),
        ).map(this::flattenChannel)
    }

    override fun stopWatching(channelType: String, channelId: String): Call<Unit> = postponeCall {
        channelApi.stopWatching(
            channelType = channelType,
            channelId = channelId,
            connectionId = connectionId,
            body = emptyMap(),
        ).toUnitCall()
    }

    override fun getPinnedMessages(
        channelType: String,
        channelId: String,
        limit: Int,
        sort: QuerySorter<Message>,
        pagination: PinnedMessagesPagination,
    ): Call<List<Message>> {
        return channelApi.getPinnedMessages(
            channelType = channelType,
            channelId = channelId,
            payload = PinnedMessagesRequest.create(
                limit = limit,
                sort = sort,
                pagination = pagination,
            ),
        ).mapDomain { response ->
            response.messages.map {
                it.toDomain()
            }
        }
    }

    override fun updateChannel(
        channelType: String,
        channelId: String,
        extraData: Map<String, Any>,
        updateMessage: Message?,
    ): Call<Channel> {
        return channelApi.updateChannel(
            channelType = channelType,
            channelId = channelId,
            body = with(dtoMapping) {
                UpdateChannelRequest(
                    extraData,
                    updateMessage?.toDto(),
                )
            },
        ).map(this::flattenChannel)
    }

    override fun updateChannelPartial(
        channelType: String,
        channelId: String,
        set: Map<String, Any>,
        unset: List<String>,
    ): Call<Channel> {
        return channelApi.updateChannelPartial(
            channelType = channelType,
            channelId = channelId,
            body = UpdateChannelPartialRequest(set, unset),
        ).map(this::flattenChannel)
    }

    override fun showChannel(
        channelType: String,
        channelId: String,
    ): Call<Unit> {
        return channelApi.showChannel(
            channelType = channelType,
            channelId = channelId,
            body = emptyMap(),
        ).toUnitCall()
    }

    override fun hideChannel(
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ): Call<Unit> {
        return channelApi.hideChannel(
            channelType = channelType,
            channelId = channelId,
            body = HideChannelRequest(clearHistory),
        ).toUnitCall()
    }

    override fun truncateChannel(
        channelType: String,
        channelId: String,
        systemMessage: Message?,
    ): Call<Channel> {
        return channelApi.truncateChannel(
            channelType = channelType,
            channelId = channelId,
            body = with(dtoMapping) { TruncateChannelRequest(message = systemMessage?.toDto()) },
        ).map(this::flattenChannel)
    }

    override fun rejectInvite(channelType: String, channelId: String): Call<Channel> {
        return channelApi.rejectInvite(
            channelType = channelType,
            channelId = channelId,
            body = RejectInviteRequest(),
        ).map(this::flattenChannel)
    }

    override fun acceptInvite(
        channelType: String,
        channelId: String,
        message: String?,
    ): Call<Channel> {
        return channelApi.acceptInvite(
            channelType = channelType,
            channelId = channelId,
            body = AcceptInviteRequest.create(userId = userId, message = message),
        ).map(this::flattenChannel)
    }

    override fun deleteChannel(channelType: String, channelId: String): Call<Channel> {
        return channelApi.deleteChannel(
            channelType = channelType,
            channelId = channelId,
        ).map(this::flattenChannel)
    }

    override fun markRead(channelType: String, channelId: String, messageId: String): Call<Unit> {
        return channelApi.markRead(
            channelType = channelType,
            channelId = channelId,
            request = MarkReadRequest(message_id = messageId),
        ).toUnitCall()
    }

    override fun markThreadRead(channelType: String, channelId: String, threadId: String): Call<Unit> {
        return channelApi.markRead(
            channelType = channelType,
            channelId = channelId,
            request = MarkReadRequest(thread_id = threadId),
        ).toUnitCall()
    }

    override fun markUnread(channelType: String, channelId: String, messageId: String): Call<Unit> {
        return channelApi.markUnread(
            channelType = channelType,
            channelId = channelId,
            request = MarkUnreadRequest(messageId),
        ).toUnitCall()
    }

    override fun markThreadUnread(
        channelType: String,
        channelId: String,
        threadId: String,
        messageId: String,
    ): Call<Unit> {
        return channelApi.markUnread(
            channelType = channelType,
            channelId = channelId,
            request = MarkUnreadRequest(
                thread_id = threadId,
                message_id = messageId,
            ),
        ).toUnitCall()
    }

    override fun markAllRead(): Call<Unit> {
        return channelApi.markAllRead().toUnitCall()
    }

    override fun addMembers(
        channelType: String,
        channelId: String,
        members: List<MemberData>,
        systemMessage: Message?,
        hideHistory: Boolean?,
        skipPush: Boolean?,
    ): Call<Channel> {
        return channelApi.addMembers(
            channelType = channelType,
            channelId = channelId,
            body = with(dtoMapping) {
                AddMembersRequest(
                    add_members = members.map { it.toDto() },
                    message = systemMessage?.toDto(),
                    hide_history = hideHistory,
                    skip_push = skipPush,
                )
            },
        ).map(this::flattenChannel)
    }

    override fun removeMembers(
        channelType: String,
        channelId: String,
        members: List<String>,
        systemMessage: Message?,
        skipPush: Boolean?,
    ): Call<Channel> {
        return channelApi.removeMembers(
            channelType = channelType,
            channelId = channelId,
            body = with(dtoMapping) {
                RemoveMembersRequest(
                    members,
                    systemMessage?.toDto(),
                    skipPush,
                )
            },
        ).map(this::flattenChannel)
    }

    override fun inviteMembers(
        channelType: String,
        channelId: String,
        members: List<String>,
        systemMessage: Message?,
        skipPush: Boolean?,
    ): Call<Channel> {
        return channelApi.inviteMembers(
            channelType = channelType,
            channelId = channelId,
            body = with(dtoMapping) {
                InviteMembersRequest(
                    members,
                    systemMessage?.toDto(),
                    skipPush,
                )
            },
        ).map(this::flattenChannel)
    }

    override fun partialUpdateMember(
        channelType: String,
        channelId: String,
        userId: String,
        set: Map<String, Any>,
        unset: List<String>,
    ): Call<Member> {
        return channelApi.partialUpdateMember(
            channelType = channelType,
            channelId = channelId,
            userId = userId,
            body = UpdateMemberPartialRequest(set, unset),
        ).mapDomain { response ->
            response.channel_member.toDomain()
        }
    }

    private fun flattenChannel(response: ChannelResponse): Channel = with(domainMapping) {
        return response.channel.toDomain(
            eventChatLastMessageAt = null,
        ).let { channel ->
            channel.copy(
                watcherCount = response.watcher_count,
                read = response.read.map {
                    it.toDomain(
                        lastReceivedEventDate = channel.lastMessageAt ?: it.last_read,
                    )
                },
                members = response.members.map { it.toDomain() },
                membership = response.membership?.toDomain(),
                messages = response.messages.map {
                    it.toDomain().enrichWithCid(channel.cid)
                },
                pinnedMessages = response.pinned_messages.map {
                    it.toDomain().enrichWithCid(channel.cid)
                },
                watchers = response.watchers.map {
                    it.toDomain()
                },
                hidden = response.hidden,
                hiddenMessagesBefore = response.hide_messages_before,
            ).syncUnreadCountWithReads()
        }
    }

    override fun getNewerReplies(
        parentId: String,
        limit: Int,
        lastId: String?,
    ): Call<List<Message>> = messageApi.getNewerReplies(
        parentId = parentId,
        limit = limit,
        lastId = lastId,
    ).mapDomain { response ->
        response.messages.map {
            it.toDomain()
        }
    }

    override fun getReplies(messageId: String, limit: Int): Call<List<Message>> {
        return messageApi.getReplies(
            messageId = messageId,
            limit = limit,
        ).mapDomain { response ->
            response.messages.map {
                it.toDomain()
            }
        }
    }

    override fun getRepliesMore(messageId: String, firstId: String, limit: Int): Call<List<Message>> {
        return messageApi.getRepliesMore(
            messageId = messageId,
            limit = limit,
            firstId = firstId,
        ).mapDomain { response ->
            response.messages.map {
                it.toDomain()
            }
        }
    }

    override fun sendAction(request: DomainSendActionRequest): Call<Message> {
        return messageApi.sendAction(
            messageId = request.messageId,
            request = SendActionRequest(
                channel_id = request.channelId,
                message_id = request.messageId,
                type = request.type,
                form_data = request.formData,
            ),
        ).mapDomain { response ->
            response.message.toDomain()
        }
    }

    override fun updateUsers(users: List<User>): Call<List<User>> {
        return userApi.updateUsers(
            connectionId = connectionId,
            body = with(dtoMapping) {
                UpdateUsersRequest(users.associateBy({ it.id }, { it.toDto() }))
            },
        ).mapDomain { response ->
            response.users.values.map {
                it.toDomain()
            }
        }
    }

    override fun blockUser(userId: String): Call<UserBlock> {
        return userApi.blockUser(
            body = BlockUserRequest(userId),
        ).mapDomain { response ->
            response.toDomain()
        }
    }

    override fun queryBlockedUsers(): Call<List<UserBlock>> =
        userApi.queryBlockedUsers().mapDomain {
            it.blocks.toDomain()
        }

    override fun unblockUser(userId: String): Call<Unit> {
        return userApi.unblockUser(body = UnblockUserRequest(userId)).toUnitCall()
    }

    override fun partialUpdateUser(id: String, set: Map<String, Any>, unset: List<String>): Call<User> {
        return userApi.partialUpdateUsers(
            connectionId = connectionId,
            body = PartialUpdateUsersRequest(
                listOf(PartialUpdateUserDto(id = id, set = set, unset = unset)),
            ),
        ).mapDomain { response ->
            response.users[id]!!.toDomain()
        }
    }

    override fun getGuestUser(userId: String, userName: String): Call<GuestUser> {
        return guestApi.getGuestUser(
            body = GuestUserRequest.create(userId, userName),
        ).mapDomain { response ->
            GuestUser(
                response.user.toDomain(),
                response.access_token,
            )
        }
    }

    override fun translate(messageId: String, language: String): Call<Message> {
        return messageApi.translate(
            messageId = messageId,
            request = TranslateMessageRequest(language),
        ).mapDomain { response ->
            response.message.toDomain()
        }
    }

    override fun og(url: String): Call<Attachment> =
        ogApi.get(url).mapDomain { it.toDomain() }

    override fun searchMessages(request: SearchMessagesRequest): Call<List<Message>> {
        val newRequest = io.getstream.chat.android.client.api2.model.requests.SearchMessagesRequest(
            filter_conditions = request.channelFilter.toMap(),
            message_filter_conditions = request.messageFilter.toMap(),
            offset = request.offset,
            limit = request.limit,
            next = request.next,
            sort = request.sort,
        )
        return generalApi.searchMessages(newRequest)
            .mapDomain { response ->
                response.results.map { resp ->
                    resp.message.toDomain()
                        .let { message ->
                            (message.cid.takeUnless(CharSequence::isBlank) ?: message.channelInfo?.cid)
                                ?.let(message::enrichWithCid)
                                ?: message
                        }
                }
            }
    }

    override fun searchMessages(
        channelFilter: FilterObject,
        messageFilter: FilterObject,
        offset: Int?,
        limit: Int?,
        next: String?,
        sort: QuerySorter<Message>?,
    ): Call<SearchMessagesResult> {
        val newRequest = io.getstream.chat.android.client.api2.model.requests.SearchMessagesRequest(
            filter_conditions = channelFilter.toMap(),
            message_filter_conditions = messageFilter.toMap(),
            offset = offset,
            limit = limit,
            next = next,
            sort = sort?.toDto(),
        )
        return generalApi.searchMessages(newRequest)
            .mapDomain { response ->
                val results = response.results

                val messages = results.map { resp ->
                    resp.message.toDomain().let { message ->
                        (message.cid.takeUnless(CharSequence::isBlank) ?: message.channelInfo?.cid)
                            ?.let(message::enrichWithCid)
                            ?: message
                    }
                }
                SearchMessagesResult(
                    messages = messages,
                    next = response.next,
                    previous = response.previous,
                    resultsWarning = response.resultsWarning?.toDomain(),
                )
            }
    }

    override fun queryChannels(query: QueryChannelsRequest): Call<List<Channel>> {
        val request = io.getstream.chat.android.client.api2.model.requests.QueryChannelsRequest(
            filter_conditions = query.filter.toMap(),
            offset = query.offset,
            limit = query.limit,
            sort = query.sort,
            message_limit = query.messageLimit,
            member_limit = query.memberLimit,
            state = query.state,
            watch = query.watch,
            presence = query.presence,
        )

        val lazyQueryChannelsCall = {
            channelApi.queryChannels(
                connectionId = connectionId,
                request = request,
            ).map { response -> response.channels.map(this::flattenChannel) }
        }

        val isConnectionRequired = query.watch || query.presence
        return if (connectionId.isBlank() && isConnectionRequired) {
            logger.i { "[queryChannels] postponing because an active connection is required" }
            postponeCall(lazyQueryChannelsCall)
        } else {
            lazyQueryChannelsCall()
        }
    }

    override fun queryChannel(channelType: String, channelId: String, query: QueryChannelRequest): Call<Channel> {
        val request = io.getstream.chat.android.client.api2.model.requests.QueryChannelRequest(
            state = query.state,
            watch = query.watch,
            presence = query.presence,
            messages = query.messages,
            watchers = query.watchers,
            members = query.members,
            data = query.data,
        )

        val lazyQueryChannelCall = {
            if (channelId.isEmpty()) {
                channelApi.queryChannel(
                    channelType = channelType,
                    connectionId = connectionId,
                    request = request,
                )
            } else {
                channelApi.queryChannel(
                    channelType = channelType,
                    channelId = channelId,
                    connectionId = connectionId,
                    request = request,
                )
            }.map(::flattenChannel)
        }

        val isConnectionRequired = query.watch || query.presence
        return if (connectionId.isBlank() && isConnectionRequired) {
            logger.i { "[queryChannel] postponing because an active connection is required" }
            postponeCall(lazyQueryChannelCall)
        } else {
            lazyQueryChannelCall()
        }
    }

    override fun queryUsers(queryUsers: QueryUsersRequest): Call<List<User>> {
        val request = io.getstream.chat.android.client.api2.model.requests.QueryUsersRequest(
            filter_conditions = queryUsers.filter.toMap(),
            offset = queryUsers.offset,
            limit = queryUsers.limit,
            sort = queryUsers.sort,
            presence = queryUsers.presence,
        )
        val lazyQueryUsersCall = {
            userApi.queryUsers(
                connectionId,
                request,
            ).mapDomain { response ->
                response.users.map {
                    it.toDomain()
                }
            }
        }

        return if (connectionId.isBlank() && queryUsers.presence) {
            postponeCall(lazyQueryUsersCall)
        } else {
            lazyQueryUsersCall()
        }
    }

    override fun queryMembers(
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySorter<Member>,
        members: List<Member>,
    ): Call<List<Member>> {
        val request = with(dtoMapping) {
            io.getstream.chat.android.client.api2.model.requests.QueryMembersRequest(
                type = channelType,
                id = channelId,
                filter_conditions = filter.toMap(),
                offset = offset,
                limit = limit,
                sort = sort.toDto(),
                members = members.map { it.toDto() },
            )
        }

        return generalApi.queryMembers(request)
            .mapDomain { response ->
                response.members.map {
                    it.toDomain()
                }
            }
    }

    override fun createVideoCall(
        channelId: String,
        channelType: String,
        callId: String,
        callType: String,
    ): Call<VideoCallInfo> {
        return callApi.createCall(
            channelId = channelId,
            channelType = channelType,
            request = VideoCallCreateRequest(id = callId, type = callType),
        ).map(CreateVideoCallResponse::toDomain)
    }

    override fun getVideoCallToken(callId: String): Call<VideoCallToken> {
        return callApi.getCallToken(callId, VideoCallTokenRequest(callId)).map(VideoCallTokenResponse::toDomain)
    }

    override fun sendEvent(
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>,
    ): Call<ChatEvent> = with(eventMapping) {
        val map = mutableMapOf<Any, Any>("type" to eventType)
        map.putAll(extraData)

        return channelApi.sendEvent(
            channelType = channelType,
            channelId = channelId,
            request = SendEventRequest(map),
        ).map { response ->
            response.event.toDomain()
        }
    }

    override fun getSyncHistory(channelIds: List<String>, lastSyncAt: String): Call<List<ChatEvent>> =
        with(eventMapping) {
            return generalApi.getSyncHistory(
                body = SyncHistoryRequest(channelIds, lastSyncAt),
                connectionId = connectionId,
            ).map { response ->
                response.events.map {
                    it.toDomain()
                }
            }
        }

    override fun downloadFile(fileUrl: String): Call<ResponseBody> {
        return fileDownloadApi.downloadFile(fileUrl)
    }

    /**
     * Queries a list of threads for the current user.
     *
     * @param query The [QueryThreadsRequest] model holding the data relevant for the `queryThreads` call.
     */
    override fun queryThreads(
        query: QueryThreadsRequest,
    ): Call<QueryThreadsResult> {
        val lazyQueryThreads = {
            threadsApi.queryThreads(
                connectionId,
                with(dtoMapping) {
                    io.getstream.chat.android.client.api2.model.requests.QueryThreadsRequest(
                        watch = query.watch,
                        limit = query.limit,
                        member_limit = query.memberLimit,
                        next = query.next,
                        participant_limit = query.participantLimit,
                        prev = query.prev,
                        reply_limit = query.replyLimit,
                        user = query.user?.toDto(),
                        user_id = query.userId,
                    )
                },
            ).mapDomain { response ->
                QueryThreadsResult(
                    threads = response.threads.map { it.toDomain() },
                    prev = response.prev,
                    next = response.next,
                )
            }
        }
        return if (connectionId.isBlank() && query.watch) {
            logger.i { "[queryThreads] postponing because an active connection is required" }
            postponeCall(lazyQueryThreads)
        } else {
            lazyQueryThreads()
        }
    }

    /**
     * Get a thread by message id.
     *
     * @param messageId The message id of the thread to retrieve.
     * @param options The options for the request.
     */
    override fun getThread(messageId: String, options: GetThreadOptions): Call<Thread> {
        val lazyGetThread = {
            threadsApi.getThread(
                messageId,
                connectionId,
                options.toMap(),
            ).mapDomain { response ->
                response.thread.toDomain()
            }
        }
        return if (connectionId.isBlank() && options.watch) {
            logger.i { "[getThread] postponing because an active connection is required" }
            postponeCall(lazyGetThread)
        } else {
            lazyGetThread()
        }
    }

    /**
     * Partially update a thread.
     *
     * @param messageId The message id of the thread to update.
     * @param set The fields to set.
     * @param unset The fields to unset.
     */
    override fun partialUpdateThread(messageId: String, set: Map<String, Any>, unset: List<String>): Call<Thread> {
        return threadsApi.partialUpdateThread(
            messageId = messageId,
            body = PartialUpdateThreadRequest(
                set = set,
                unset = unset,
            ),
        ).mapDomain { response ->
            response.thread.toDomain()
        }
    }

    override fun castPollVote(
        messageId: String,
        pollId: String,
        optionId: String,
    ): Call<Vote> = castVote(
        messageId = messageId,
        pollId = pollId,
        vote = UpstreamVoteDto(option_id = optionId),
    )

    override fun castPollAnswer(
        messageId: String,
        pollId: String,
        answer: String,
    ): Call<Vote> = castVote(
        messageId = messageId,
        pollId = pollId,
        vote = UpstreamVoteDto(answer_text = answer),
    )

    private fun castVote(
        messageId: String,
        pollId: String,
        vote: UpstreamVoteDto,
    ): Call<Vote> =
        pollsApi.castPollVote(
            messageId,
            pollId,
            PollVoteRequest(vote),
        ).mapDomain { it.vote.toDomain() }

    override fun removePollVote(messageId: String, pollId: String, voteId: String): Call<Vote> =
        pollsApi.removePollVote(
            messageId,
            pollId,
            voteId,
        ).mapDomain { it.vote.toDomain() }

    override fun closePoll(pollId: String): Call<Poll> =
        pollsApi.updatePoll(
            pollId,
            PollUpdateRequest(
                set = mapOf("is_closed" to true),
            ),
        ).mapDomain { it.poll.toDomain() }

    override fun suggestPollOption(pollId: String, option: String): Call<Option> =
        pollsApi.suggestPollOption(
            pollId,
            SuggestPollOptionRequest(option),
        ).mapDomain { it.poll_option.toDomain() }

    override fun createPoll(pollConfig: PollConfig): Call<Poll> {
        return pollsApi.createPoll(
            PollRequest(
                name = pollConfig.name,
                description = pollConfig.description,
                options = pollConfig.options.map(::UpstreamOptionDto),
                voting_visibility = when (pollConfig.votingVisibility) {
                    VotingVisibility.PUBLIC -> PollRequest.VOTING_VISIBILITY_PUBLIC
                    VotingVisibility.ANONYMOUS -> PollRequest.VOTING_VISIBILITY_ANONYMOUS
                },
                enforce_unique_vote = pollConfig.enforceUniqueVote,
                max_votes_allowed = pollConfig.maxVotesAllowed,
                allow_user_suggested_options = pollConfig.allowUserSuggestedOptions,
                allow_answers = pollConfig.allowAnswers,
            ),
        ).mapDomain { it.poll.toDomain() }
    }

    override fun warmUp() {
        generalApi.warmUp().enqueue()
    }

    private fun <T : Any> postponeCall(call: () -> Call<T>): Call<T> {
        return callPostponeHelper.postponeCall(call)
    }

    private fun <T : Any, R : Any> RetrofitCall<T>.mapDomain(transform: DomainMapping.(T) -> R): Call<R> =
        map { domainMapping.transform(it) }
}
