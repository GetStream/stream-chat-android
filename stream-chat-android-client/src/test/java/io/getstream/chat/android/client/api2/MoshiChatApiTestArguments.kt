/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.client.Mother
import io.getstream.chat.android.client.Mother.randomUnreadChannelByTypeDto
import io.getstream.chat.android.client.Mother.randomUnreadChannelDto
import io.getstream.chat.android.client.Mother.randomUnreadCountByTeamDto
import io.getstream.chat.android.client.Mother.randomUnreadDto
import io.getstream.chat.android.client.Mother.randomUnreadThreadDto
import io.getstream.chat.android.client.Mother.toChannelStateResponse
import io.getstream.chat.android.client.api.FakeResponse
import io.getstream.chat.android.client.api2.model.response.FlagResponse
import io.getstream.chat.android.client.api2.model.response.MuteUserResponse
import io.getstream.chat.android.client.parser2.GetMessageResponseParityTest
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.models.UnreadChannel
import io.getstream.chat.android.models.UnreadChannelByType
import io.getstream.chat.android.models.UnreadCounts
import io.getstream.chat.android.models.UnreadThread
import io.getstream.chat.android.models.UploadedFile
import io.getstream.chat.android.network.apis.ChatEndpoints
import io.getstream.chat.android.network.models.AddUserGroupMembersResponse
import io.getstream.chat.android.network.models.BlockUsersResponse
import io.getstream.chat.android.network.models.ChannelStateResponse
import io.getstream.chat.android.network.models.ChannelStateResponseFields
import io.getstream.chat.android.network.models.CreateGuestResponse
import io.getstream.chat.android.network.models.CreateReminderResponse
import io.getstream.chat.android.network.models.CreateUserGroupResponse
import io.getstream.chat.android.network.models.DeleteChannelResponse
import io.getstream.chat.android.network.models.DeleteMessageResponse
import io.getstream.chat.android.network.models.DeleteReactionResponse
import io.getstream.chat.android.network.models.DeleteReminderResponse
import io.getstream.chat.android.network.models.GetBlockedUsersResponse
import io.getstream.chat.android.network.models.GetPinnedMessagesResponse
import io.getstream.chat.android.network.models.GetReactionsResponse
import io.getstream.chat.android.network.models.GetRepliesResponse
import io.getstream.chat.android.network.models.GetThreadResponse
import io.getstream.chat.android.network.models.GetUserGroupResponse
import io.getstream.chat.android.network.models.GroupedChannelsBucket
import io.getstream.chat.android.network.models.GroupedQueryChannelsResponse
import io.getstream.chat.android.network.models.HideChannelResponse
import io.getstream.chat.android.network.models.ListDevicesResponse
import io.getstream.chat.android.network.models.ListUserGroupsResponse
import io.getstream.chat.android.network.models.MarkDeliveredResponse
import io.getstream.chat.android.network.models.MarkReadResponse
import io.getstream.chat.android.network.models.MembersResponse
import io.getstream.chat.android.network.models.MessageActionResponse
import io.getstream.chat.android.network.models.ParsedPredefinedFilterResponse
import io.getstream.chat.android.network.models.PollOptionResponse
import io.getstream.chat.android.network.models.PollResponse
import io.getstream.chat.android.network.models.PollVoteResponse
import io.getstream.chat.android.network.models.QueryBannedUsersResponse
import io.getstream.chat.android.network.models.QueryChannelsResponse
import io.getstream.chat.android.network.models.QueryReactionsResponse
import io.getstream.chat.android.network.models.QueryThreadsResponse
import io.getstream.chat.android.network.models.QueryUsersResponse
import io.getstream.chat.android.network.models.RemoveUserGroupMembersResponse
import io.getstream.chat.android.network.models.Response
import io.getstream.chat.android.network.models.SearchResponse
import io.getstream.chat.android.network.models.SearchResult
import io.getstream.chat.android.network.models.SearchRolesResponse
import io.getstream.chat.android.network.models.SearchUserGroupsResponse
import io.getstream.chat.android.network.models.SendMessageResponse
import io.getstream.chat.android.network.models.SendReactionResponse
import io.getstream.chat.android.network.models.SharedLocationResponse
import io.getstream.chat.android.network.models.ShowChannelResponse
import io.getstream.chat.android.network.models.SortParamRequest
import io.getstream.chat.android.network.models.TranslateMessageResponse
import io.getstream.chat.android.network.models.TruncateChannelResponse
import io.getstream.chat.android.network.models.UnblockUsersResponse
import io.getstream.chat.android.network.models.UpdateChannelPartialResponse
import io.getstream.chat.android.network.models.UpdateChannelResponse
import io.getstream.chat.android.network.models.UpdateLiveLocationRequest
import io.getstream.chat.android.network.models.UpdateMemberPartialResponse
import io.getstream.chat.android.network.models.UpdateMessagePartialResponse
import io.getstream.chat.android.network.models.UpdateMessageResponse
import io.getstream.chat.android.network.models.UpdateReminderResponse
import io.getstream.chat.android.network.models.UpdateThreadPartialResponse
import io.getstream.chat.android.network.models.UpdateUserGroupResponse
import io.getstream.chat.android.network.models.UpdateUsersResponse
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomLocation
import io.getstream.chat.android.randomString
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.call.map
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.params.provider.Arguments
import org.mockito.kotlin.any
import org.mockito.kotlin.wheneverBlocking

@Suppress("UNUSED", "LargeClass")
internal object MoshiChatApiTestArguments {

    @JvmStatic
    fun appSettingsInput() = endpointArgs(Mother.randomAppSettingsResponse())

    @JvmStatic
    fun sendMessageInput() =
        responseArguments(SendMessageResponse(duration = "1ms", message = Mother.randomMessageResponse()))

    @JvmStatic
    fun createDraftMessageInput() = draftMessageResponseArguments()

    @JvmStatic
    fun deleteDraftMessageInput() = endpointArgs(Response(randomString()))

    @JvmStatic
    fun queryDraftMessageInput() = endpointArgs(Mother.randomQueryDraftsResponse())

    @JvmStatic
    fun updateMessageInput() =
        responseArguments(UpdateMessageResponse(duration = "1ms", message = Mother.randomMessageResponse()))

    @JvmStatic
    fun partialUpdateMessageInput() = responseArguments(
        UpdateMessagePartialResponse(duration = "1ms", message = Mother.randomMessageResponse()),
        missingMessage = UpdateMessagePartialResponse(duration = "1ms", message = null),
    )

    @JvmStatic
    fun getMessageInput() = getMessageResponseArguments()

    @JvmStatic
    fun getPendingMessageInput() = getMessageResponseArguments()

    @JvmStatic
    fun deleteMessageInput() = listOf(
        Arguments.of(
            true,
            false,
            randomDeleteMessageResponse(),
            Result.Success::class,
        ),
        Arguments.of(
            false,
            true,
            randomDeleteMessageResponse(),
            Result.Success::class,
        ),
        Arguments.of(
            false,
            false,
            randomDeleteMessageResponse(),
            Result.Success::class,
        ),
        Arguments.of(
            true,
            true,
            randomDeleteMessageResponse(),
            Result.Success::class,
        ),
        Arguments.of(
            true,
            false,
            RetroError<DeleteMessageResponse>(statusCode = 500).toHttpException(),
            Result.Failure::class,
        ),
        Arguments.of(
            false,
            true,
            RetroError<DeleteMessageResponse>(statusCode = 500).toHttpException(),
            Result.Failure::class,
        ),
        Arguments.of(
            false,
            false,
            RetroError<DeleteMessageResponse>(statusCode = 500).toHttpException(),
            Result.Failure::class,
        ),
        Arguments.of(
            true,
            true,
            RetroError<DeleteMessageResponse>(statusCode = 500).toHttpException(),
            Result.Failure::class,
        ),
    )

    @JvmStatic
    fun getReactionsInput() = endpointArgs(
        GetReactionsResponse(
            duration = randomString(),
            reactions = listOf(Mother.randomReactionResponse()),
        ),
    )

    @JvmStatic
    fun queryReactionsInput() = endpointArgs(
        QueryReactionsResponse(
            duration = randomString(),
            reactions = listOf(Mother.randomReactionResponse()),
            next = randomString(),
        ),
    )

    @JvmStatic
    fun sendReactionInput() = endpointArgs(
        SendReactionResponse(
            duration = "1ms",
            message = Mother.randomMessageResponse(),
            reaction = Mother.randomReactionResponse(),
        ),
    )

    @JvmStatic
    fun deleteReactionInput() = responseArguments(
        DeleteReactionResponse(
            duration = "1ms",
            message = Mother.randomMessageResponse(),
            reaction = Mother.randomReactionResponse(),
        ),
    )

    @JvmStatic
    fun addDeviceInput() = endpointArgs(Response(randomString()))

    @JvmStatic
    fun deleteDeviceInput() = endpointArgs(Response(randomString()))

    @JvmStatic
    fun getDevicesInput() =
        endpointArgs(ListDevicesResponse(duration = randomString(), devices = listOf(Mother.randomDeviceResponse())))

    @JvmStatic
    fun muteCurrentUserInput() = muteUserResponseArguments()

    @JvmStatic
    fun muteUserInput() = muteUserResponseArguments()

    @JvmStatic
    fun unmuteCurrentUserInput() = completableResponseArguments()

    @JvmStatic
    fun unmuteUserInput() = completableResponseArguments()

    @JvmStatic
    fun muteChannelInput() = completableResponseArguments()

    @JvmStatic
    fun unmuteChannelInput() = completableResponseArguments()

    @JvmStatic
    fun sendFileInput() = uploadedFileArguments()

    @JvmStatic
    fun sendImageInput() = uploadedFileArguments()

    @JvmStatic
    fun deleteFileInput() = deleteFileArguments()

    @JvmStatic
    fun deleteImageInput() = deleteFileArguments()

    @JvmStatic
    fun flagUserInput() = flagResponseArguments()

    @JvmStatic
    fun flagMessageInput() = flagResponseArguments()

    @JvmStatic
    fun unflagUserInput() = flagResponseArguments()

    @JvmStatic
    fun unflagMessageInput() = flagResponseArguments()

    @JvmStatic
    fun banUserInput() = completableResponseArguments()

    @JvmStatic
    fun unbanUserInput() = completableResponseArguments()

    @JvmStatic
    fun queryBannedUsersInput() = listOf(
        Arguments.of(
            RetroSuccess(
                QueryBannedUsersResponse(duration = randomString(), bans = listOf(Mother.randomBanResponse())),
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(
            RetroError<QueryBannedUsersResponse>(statusCode = 500).toRetrofitCall(),
            Result.Failure::class,
        ),
        // A ban the mapper cannot map fails the whole call rather than being dropped from the list.
        Arguments.of(
            RetroSuccess(
                QueryBannedUsersResponse(
                    duration = randomString(),
                    bans = listOf(Mother.randomBanResponse(user = null)),
                ),
            ).toRetrofitCall(),
            Result.Failure::class,
        ),
    )

    @JvmStatic
    fun enableSlowModeInput() = updateChannelPartialResponseArguments()

    @JvmStatic
    fun disableSlowModeInput() = updateChannelPartialResponseArguments()

    @JvmStatic
    fun stopWatchingInput() = completableResponseArguments()

    @JvmStatic
    fun getPinnedMessagesInput() = pinnedMessagesResponseArguments()

    @JvmStatic
    fun updateChannelInput() = updateChannelResponseArguments()

    @JvmStatic
    fun updateChannelPartialInput() = updateChannelPartialResponseArguments()

    @JvmStatic
    fun showChannelInput() = endpointArgs(ShowChannelResponse(randomString()))

    @JvmStatic
    fun hideChannelInput() = endpointArgs(HideChannelResponse(randomString()))

    @JvmStatic
    fun truncateChannelInput() = truncateChannelResponseArguments()

    @JvmStatic
    fun rejectInviteInput() = updateChannelResponseArguments()

    @JvmStatic
    fun acceptInviteInput() = updateChannelResponseArguments()

    @JvmStatic
    fun deleteChannelInput() = deleteChannelResponseArguments()

    @JvmStatic
    fun markReadInput() = endpointArgs(MarkReadResponse(randomString()))

    @JvmStatic
    fun markDeliveredInput() = endpointArgs(MarkDeliveredResponse(randomString()))

    @JvmStatic
    fun markThreadReadInput() = endpointArgs(MarkReadResponse(randomString()))

    @JvmStatic
    fun markUnreadInput() = endpointArgs(Response(randomString()))

    @JvmStatic
    fun markThreadUnreadInput() = completableResponseArguments()

    @JvmStatic
    fun markAllReadInput() = endpointArgs(MarkReadResponse(randomString()))

    @JvmStatic
    fun addMembersInput() = updateChannelResponseArguments()

    @JvmStatic
    fun removeMembersInput() = updateChannelResponseArguments()

    @JvmStatic
    fun inviteMembersInput() = updateChannelResponseArguments()

    @JvmStatic
    fun partialUpdateMemberInput() = listOf(
        Arguments.of(
            UpdateMemberPartialResponse(
                duration = randomString(),
                channelMember = Mother.randomChannelMemberResponse(),
            ),
            Result.Success::class,
        ),
        Arguments.of(
            RetroError<UpdateMemberPartialResponse>(statusCode = 500).toHttpException(),
            Result.Failure::class,
        ),
        // The member is optional in the response schema: a 200 without it must fail, not crash.
        Arguments.of(
            UpdateMemberPartialResponse(duration = randomString(), channelMember = null),
            Result.Failure::class,
        ),
    )

    @JvmStatic
    fun getNewerRepliesInput() = listOf(
        Arguments.of(RetroSuccess(randomRepliesResponse()).toRetrofitCall(), Result.Success::class),
        Arguments.of(RetroError<GetRepliesResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    @JvmStatic
    fun getRepliesInput() = repliesResponseArguments()

    @JvmStatic
    fun getRepliesMoreInput() = repliesResponseArguments()

    @JvmStatic
    fun getRepliesAroundInput() = repliesResponseArguments()

    @JvmStatic
    fun sendActionInput() = responseArguments(
        MessageActionResponse(duration = "1ms", message = Mother.randomMessageResponse()),
        missingMessage = MessageActionResponse(duration = "1ms", message = null),
    )

    @JvmStatic
    fun updateUsersInput() = updateUsersResponseArguments()

    @JvmStatic
    fun blockUserInput() = listOf(
        Arguments.of(RetroSuccess(Mother.randomBlockUsersResponse()).toRetrofitCall(), Result.Success::class),
        Arguments.of(RetroError<BlockUsersResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    @JvmStatic
    fun unblockUserInput() = listOf(
        Arguments.of(RetroSuccess(Mother.randomUnblockUsersResponse()).toRetrofitCall(), Result.Success::class),
        Arguments.of(RetroError<UnblockUsersResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    @JvmStatic
    fun queryBlockedUsersInput() = listOf(
        Arguments.of(
            RetroSuccess(
                GetBlockedUsersResponse(duration = "1ms", blocks = listOf(Mother.randomBlockedUserResponse())),
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(
            RetroError<GetBlockedUsersResponse>(statusCode = 500).toRetrofitCall(),
            Result.Failure::class,
        ),
    )

    @JvmStatic
    fun partialUpdateUserInput() = updateUsersResponseArguments()

    @JvmStatic
    fun getGuestUserInput() = listOf(
        Arguments.of(RetroSuccess(Mother.randomCreateGuestResponse()).toRetrofitCall(), Result.Success::class),
        Arguments.of(RetroError<CreateGuestResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    @JvmStatic
    fun translateInput() =
        responseArguments(TranslateMessageResponse(duration = "1ms", message = Mother.randomMessageResponse()))

    @JvmStatic
    fun ogInput() = endpointArgs(Mother.randomGetOGResponse())

    @JvmStatic
    fun searchMessagesInput() = searchMessagesResponseArguments()

    @JvmStatic
    fun queryChannelsInput() = listOf(
        Arguments.of(
            QueryChannelsResponse(
                duration = "1ms",
                channels = listOf(
                    Mother.randomChannelStateResponseFields(),
                ),
            ),
            Result.Success::class,
        ),
        Arguments.of(RetroError<QueryChannelsResponse>(statusCode = 500).toHttpException(), Result.Failure::class),
    )

    @JvmStatic
    fun queryGroupedChannelsInput() = listOf(
        Arguments.of(
            GroupedQueryChannelsResponse(
                groups = mapOf(
                    "all-open" to GroupedChannelsBucket(
                        channels = listOf(
                            Mother.randomChannelStateResponseFields(),
                        ),
                        unreadChannels = positiveRandomInt(),
                        next = null,
                        prev = null,
                    ),
                ),
                duration = "12ms",
            ),
            Result.Success::class,
        ),
        Arguments.of(
            RetroError<GroupedQueryChannelsResponse>(statusCode = 500).toHttpException(),
            Result.Failure::class,
        ),
    )

    @JvmStatic
    fun queryChannelsWithPredefinedFilterInput() = listOf(
        Arguments.of(
            QueryChannelsResponse(
                duration = "1ms",
                channels = listOf(
                    Mother.randomChannelStateResponseFields(),
                ),
                predefinedFilter = ParsedPredefinedFilterResponse(
                    name = "android_sample_filter",
                    filter = mapOf("type" to "messaging"),
                    sort = listOf(SortParamRequest(field = "last_message_at", direction = -1)),
                ),
            ),
            Result.Success::class,
        ),
        Arguments.of(RetroError<QueryChannelsResponse>(statusCode = 500).toHttpException(), Result.Failure::class),
    )

    @JvmStatic
    fun queryChannelInput() = listOf(
        Arguments.of(
            Mother.randomChannelStateResponseFields().toChannelStateResponse(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<ChannelStateResponse>(statusCode = 500).toHttpException(), Result.Failure::class),
    )

    /**
     * Rows of (stub serving a channel state, call reading the mapped channels back) for each REST path
     * that returns channel state.
     */
    @JvmStatic
    fun channelStateInput() = listOf(
        Arguments.of(
            "queryChannel",
            { api: ChatEndpoints, response: ChannelStateResponseFields ->
                wheneverBlocking { api.getOrCreateChannel(any(), any(), any(), any()) }
                    .thenReturn(response.toChannelStateResponse())
            },
            { sut: MoshiChatApi ->
                sut.queryChannel(randomString(), randomString(), Mother.randomQueryChannelRequest())
                    .map { listOf(it) }
            },
        ),
        Arguments.of(
            "queryChannels",
            { api: ChatEndpoints, response: ChannelStateResponseFields ->
                val channels = QueryChannelsResponse(duration = "1ms", channels = listOf(response))
                wheneverBlocking { api.queryChannels(any(), any()) }.thenReturn(channels)
            },
            { sut: MoshiChatApi ->
                sut.queryChannels(Mother.randomQueryChannelsRequest()).map { it.channels }
            },
        ),
        Arguments.of(
            "queryGroupedChannels",
            { api: ChatEndpoints, response: ChannelStateResponseFields ->
                val group = GroupedChannelsBucket(channels = listOf(response))
                wheneverBlocking { api.groupedQueryChannels(any(), any()) }
                    .thenReturn(GroupedQueryChannelsResponse(groups = mapOf("all" to group), duration = "1ms"))
            },
            { sut: MoshiChatApi ->
                sut.queryGroupedChannels(limit = null, groups = null, sort = null, watch = false, presence = false)
                    .map { grouped -> grouped.groups.values.flatMap { it.channels } }
            },
        ),
    )

    @JvmStatic
    fun queryUsersInput() = listOf(
        Arguments.of(
            RetroSuccess(
                QueryUsersResponse(duration = randomString(), users = listOf(Mother.randomFullUserResponse())),
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<QueryUsersResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    @JvmStatic
    fun queryMembersInput() = listOf(
        Arguments.of(
            RetroSuccess(
                MembersResponse(duration = randomString(), members = listOf(Mother.randomChannelMemberResponse())),
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<MembersResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    @JvmStatic
    fun sendEventInput() = listOf(
        Arguments.of(RetroSuccess(SENT_TYPING_EVENT.toResponseBody()).toRetrofitCall(), Result.Success::class),
        // A body without the event the SDK reads fails rather than crashing.
        Arguments.of(RetroSuccess("{}".toResponseBody()).toRetrofitCall(), Result.Failure::class),
        Arguments.of(RetroError<ResponseBody>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    @JvmStatic
    fun getSyncHistoryInput() = listOf(
        Arguments.of(RetroSuccess(SYNCED_EVENTS.toResponseBody()).toRetrofitCall(), Result.Success::class),
        Arguments.of(RetroSuccess("{}".toResponseBody()).toRetrofitCall(), Result.Success::class),
        Arguments.of(RetroSuccess(SYNCED_UNREADABLE_EVENT.toResponseBody()).toRetrofitCall(), Result.Success::class),
        // A body whose envelope can't be read fails the whole sync.
        Arguments.of(RetroSuccess("""{"events": {}}""".toResponseBody()).toRetrofitCall(), Result.Failure::class),
        Arguments.of(RetroError<ResponseBody>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    @JvmStatic
    fun downloadFileInput() = listOf(
        Arguments.of(RetroSuccess(FakeResponse.Body(randomString())).toRetrofitCall(), Result.Success::class),
        Arguments.of(RetroError<ResponseBody>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    @JvmStatic
    fun queryThreadsInput() = listOf(
        Arguments.of(
            RetroSuccess(
                QueryThreadsResponse(
                    threads = listOf(Mother.randomThreadStateResponse()),
                    duration = randomString(),
                    prev = randomString(),
                    next = randomString(),
                ),
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(
            RetroSuccess(
                QueryThreadsResponse(
                    threads = listOf(Mother.randomThreadStateResponse(parentMessage = null)),
                    duration = randomString(),
                ),
            ).toRetrofitCall(),
            Result.Failure::class,
        ),
        Arguments.of(RetroError<QueryThreadsResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    @JvmStatic
    fun getThreadInput() = threadResponseArguments()

    @JvmStatic
    fun partialUpdateThreadInput() = threadInfoResponseArguments()

    @JvmStatic
    fun castPollVoteInput() = pollVoteResponseArguments()

    @JvmStatic
    fun castPollAnswerInput() = pollVoteResponseArguments()

    @JvmStatic
    fun removePollVoteInput() = pollVoteResponseArguments()

    @JvmStatic
    fun partialUpdatePollInput() = pollResponseArguments()

    @JvmStatic
    fun closePollInput() = pollResponseArguments()

    @JvmStatic
    fun createPollInput() = pollResponseArguments()

    @JvmStatic
    fun updatePollInput() = pollResponseArguments()

    @JvmStatic
    fun getPollInput() = pollResponseArguments()

    @JvmStatic
    fun deletePollInput() = endpointArgs(Response(randomString()))

    @JvmStatic
    fun queryPollsInput() = endpointArgs(Mother.randomQueryPollsResponse())

    @JvmStatic
    fun queryPollVotesInput() = endpointArgs(Mother.randomPollVotesResponse())

    @JvmStatic
    fun createPollOptionInput() = pollOptionResponseArguments()

    @JvmStatic
    fun updatePollOptionInput() = pollOptionResponseArguments()

    @JvmStatic
    fun deletePollOptionInput() = endpointArgs(Response(randomString()))

    @JvmStatic
    fun createReminderInput() =
        endpointArgs(CreateReminderResponse(randomString(), Mother.randomReminderResponseData()))

    @JvmStatic
    fun updateReminderInput() =
        endpointArgs(UpdateReminderResponse(randomString(), Mother.randomReminderResponseData()))

    @JvmStatic
    fun deleteReminderInput() = endpointArgs(DeleteReminderResponse(randomString()))

    @JvmStatic
    fun queryRemindersInput() = endpointArgs(Mother.randomQueryRemindersResponse())

    @JvmStatic
    fun updateLiveLocation() = listOf(
        run {
            val location = randomLocation()
            val request = UpdateLiveLocationRequest(
                messageId = location.messageId,
                latitude = location.latitude,
                longitude = location.longitude,
            )
            val response = SharedLocationResponse(
                messageId = location.messageId,
                channelCid = location.cid,
                userId = location.userId,
                latitude = location.latitude,
                longitude = location.longitude,
                createdByDeviceId = location.deviceId,
                endAt = location.endAt,
                createdAt = randomDate(),
                updatedAt = randomDate(),
                duration = randomString(),
            )
            Arguments.of(location, request, response)
        },
    )

    @JvmStatic
    fun stopLiveLocation() = listOf(
        run {
            val location = randomLocation()
            val request = UpdateLiveLocationRequest(
                messageId = location.messageId,
                endAt = location.endAt,
            )
            val response = SharedLocationResponse(
                messageId = location.messageId,
                channelCid = location.cid,
                userId = location.userId,
                latitude = location.latitude,
                longitude = location.longitude,
                createdByDeviceId = location.deviceId,
                endAt = location.endAt,
                createdAt = randomDate(),
                updatedAt = randomDate(),
                duration = randomString(),
            )
            Arguments.of(location, request, response)
        },
    )

    @JvmStatic
    fun getUnreadCounts() = listOf(
        run {
            val dto = randomUnreadDto(
                totalUnreadCountByTeam = mapOf(randomUnreadCountByTeamDto()),
                channels = listOf(randomUnreadChannelDto()),
                threads = listOf(randomUnreadThreadDto()),
                channelType = listOf(randomUnreadChannelByTypeDto()),
            )
            val model = UnreadCounts(
                messagesCount = dto.totalUnreadCount,
                threadsCount = dto.totalUnreadThreadsCount,
                messagesCountByTeam = dto.totalUnreadCountByTeam.orEmpty(),
                channels = dto.channels.map { channel ->
                    UnreadChannel(
                        cid = channel.channelId,
                        messagesCount = channel.unreadCount,
                        lastRead = channel.lastRead,
                    )
                },
                threads = dto.threads.map { thread ->
                    UnreadThread(
                        parentMessageId = thread.parentMessageId,
                        messagesCount = thread.unreadCount,
                        lastRead = thread.lastRead,
                        lastReadMessageId = thread.lastReadMessageId,
                    )
                },
                channelsByType = dto.channelType.map { channelType ->
                    UnreadChannelByType(
                        channelType = channelType.channelType,
                        channelsCount = channelType.channelCount,
                        messagesCount = channelType.unreadCount,
                    )
                },
            )
            Arguments.of(model, dto)
        },
    )

    private fun muteUserResponseArguments() = listOf(
        Arguments.of(
            RetroSuccess(
                MuteUserResponse(Mother.randomUserMuteResponse()),
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<Response>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    private fun completableResponseArguments() = listOf(
        Arguments.of(RetroSuccess(Response("")).toRetrofitCall(), Result.Success::class),
        Arguments.of(RetroError<Response>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    private fun uploadedFileArguments() = listOf(
        Arguments.of(Result.Success(UploadedFile(randomString())), Result.Success::class),
        Arguments.of(Result.Failure(Error.GenericError(randomString())), Result.Failure::class),
    )

    private fun deleteFileArguments() = listOf(
        Arguments.of(Result.Success(Unit), Result.Success::class),
        Arguments.of(Result.Failure(Error.GenericError(randomString())), Result.Failure::class),
    )

    private fun flagResponseArguments() = listOf(
        Arguments.of(
            RetroSuccess(FlagResponse(Mother.randomDownstreamFlagDto())).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<FlagResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    private fun updateChannelResponseArguments() = listOf(
        Arguments.of(
            UpdateChannelResponse(
                duration = randomString(),
                members = listOf(Mother.randomChannelMemberResponse()),
                channel = Mother.randomChannelResponse(),
            ),
            Result.Success::class,
        ),
        Arguments.of(
            UpdateChannelResponse(duration = randomString()),
            Result.Failure::class,
        ),
        Arguments.of(RetroError<UpdateChannelResponse>(statusCode = 500).toHttpException(), Result.Failure::class),
    )

    private fun updateChannelPartialResponseArguments() = listOf(
        Arguments.of(
            UpdateChannelPartialResponse(
                duration = randomString(),
                members = listOf(Mother.randomChannelMemberResponse()),
                channel = Mother.randomChannelResponse(),
            ),
            Result.Success::class,
        ),
        Arguments.of(
            UpdateChannelPartialResponse(duration = randomString()),
            Result.Failure::class,
        ),
        Arguments.of(
            RetroError<UpdateChannelPartialResponse>(statusCode = 500).toHttpException(),
            Result.Failure::class,
        ),
    )

    private fun truncateChannelResponseArguments() = listOf(
        Arguments.of(
            TruncateChannelResponse(duration = randomString(), channel = Mother.randomChannelResponse()),
            Result.Success::class,
        ),
        Arguments.of(
            TruncateChannelResponse(duration = randomString()),
            Result.Failure::class,
        ),
        Arguments.of(RetroError<TruncateChannelResponse>(statusCode = 500).toHttpException(), Result.Failure::class),
    )

    private fun deleteChannelResponseArguments() = listOf(
        Arguments.of(
            DeleteChannelResponse(duration = randomString(), channel = Mother.randomChannelResponse()),
            Result.Success::class,
        ),
        Arguments.of(
            DeleteChannelResponse(duration = randomString()),
            Result.Failure::class,
        ),
        Arguments.of(RetroError<DeleteChannelResponse>(statusCode = 500).toHttpException(), Result.Failure::class),
    )

    private fun randomDeleteMessageResponse() =
        DeleteMessageResponse(duration = "1ms", message = Mother.randomMessageResponse())

    private inline fun <reified T : Any> responseArguments(response: T, missingMessage: T? = null) = listOfNotNull(
        Arguments.of(response, Result.Success::class),
        missingMessage?.let { Arguments.of(it, Result.Failure::class) },
        Arguments.of(RetroError<T>(statusCode = 500).toHttpException(), Result.Failure::class),
    )

    private fun getMessageResponseArguments() = responseArguments(GetMessageResponseParityTest.getMessageResponse())

    private fun draftMessageResponseArguments() = endpointArgs(Mother.randomCreateDraftResponse())

    private fun randomRepliesResponse() =
        GetRepliesResponse(duration = "1ms", messages = listOf(Mother.randomMessageResponse()))

    private fun repliesResponseArguments() = endpointArgs(randomRepliesResponse())

    private fun pinnedMessagesResponseArguments() = listOf(
        Arguments.of(
            RetroSuccess(
                GetPinnedMessagesResponse(duration = "1ms", messages = listOf(Mother.randomMessageResponse())),
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<GetPinnedMessagesResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    private fun updateUsersResponseArguments(): List<Arguments> {
        val userId = randomString()
        val user = Mother.randomFullUserResponse()
        val response = UpdateUsersResponse(
            duration = randomString(),
            membershipDeletionTaskId = randomString(),
            users = mapOf(userId to user),
        )
        return listOf(
            Arguments.of(RetroSuccess(response).toRetrofitCall(), Result.Success::class),
            Arguments.of(RetroError<UpdateUsersResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
        )
    }

    private fun searchMessagesResponseArguments() = listOf(
        Arguments.of(
            RetroSuccess(
                SearchResponse(
                    duration = randomString(),
                    results = listOf(SearchResult(Mother.randomSearchResultMessage())),
                    next = randomString(),
                    previous = randomString(),
                    resultsWarning = Mother.randomSearchWarningResponse(),
                ),
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<SearchResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    private fun threadResponseArguments() = listOf(
        Arguments.of(
            RetroSuccess(
                GetThreadResponse(
                    thread = Mother.randomThreadStateResponse(),
                    duration = randomString(),
                ),
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(
            RetroSuccess(
                GetThreadResponse(
                    thread = Mother.randomThreadStateResponse(lastMessageAt = null),
                    duration = randomString(),
                ),
            ).toRetrofitCall(),
            Result.Failure::class,
        ),
        Arguments.of(RetroError<GetThreadResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    private fun threadInfoResponseArguments() = listOf(
        Arguments.of(
            RetroSuccess(
                UpdateThreadPartialResponse(
                    thread = Mother.randomThreadResponse(),
                    duration = randomString(),
                ),
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<UpdateThreadPartialResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    private fun pollResponseArguments() =
        endpointArgs(PollResponse(poll = Mother.randomPollResponseData(), duration = randomString()))

    private fun pollOptionResponseArguments() =
        endpointArgs(PollOptionResponse(duration = randomString(), pollOption = Mother.randomPollOptionResponseData()))

    private fun pollVoteResponseArguments() =
        endpointArgs(PollVoteResponse(duration = randomString(), vote = Mother.randomPollVoteResponseData()))

    /** A response or the exception a generated suspend endpoint throws, with the result each must give. */
    private fun <T : Any> endpointArgs(success: T) = listOf(
        Arguments.of(success, Result.Success::class),
        Arguments.of(RetroError<T>(statusCode = 500).toHttpException(), Result.Failure::class),
    )

    private fun userGroup() = Mother.randomUserGroupResponse()

    @JvmStatic
    fun createUserGroupResponseInput() =
        endpointArgs(CreateUserGroupResponse(duration = randomString(), userGroup = userGroup()))

    @JvmStatic
    fun getUserGroupResponseInput() =
        endpointArgs(GetUserGroupResponse(duration = randomString(), userGroup = userGroup()))

    @JvmStatic
    fun updateUserGroupResponseInput() =
        endpointArgs(UpdateUserGroupResponse(duration = randomString(), userGroup = userGroup()))

    @JvmStatic
    fun addUserGroupMembersResponseInput() =
        endpointArgs(AddUserGroupMembersResponse(duration = randomString(), userGroup = userGroup()))

    @JvmStatic
    fun removeUserGroupMembersResponseInput() =
        endpointArgs(RemoveUserGroupMembersResponse(duration = randomString(), userGroup = userGroup()))

    @JvmStatic
    fun listUserGroupsResponseInput() =
        endpointArgs(ListUserGroupsResponse(duration = randomString(), userGroups = listOf(userGroup())))

    @JvmStatic
    fun searchUserGroupsResponseInput() =
        endpointArgs(SearchUserGroupsResponse(duration = randomString(), userGroups = listOf(userGroup())))

    @JvmStatic
    fun deleteUserGroupInput() = endpointArgs(Response(randomString()))

    @JvmStatic
    fun searchRolesInput() =
        endpointArgs(SearchRolesResponse(duration = randomString(), roles = listOf(Mother.randomRoleDto())))
}
