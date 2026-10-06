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
import io.getstream.chat.android.client.api2.endpoint.ChannelApi
import io.getstream.chat.android.client.api2.model.dto.HealthEventDto
import io.getstream.chat.android.client.api2.model.dto.utils.internal.ExactDate
import io.getstream.chat.android.client.api2.model.response.EventResponse
import io.getstream.chat.android.client.api2.model.response.FlagResponse
import io.getstream.chat.android.client.api2.model.response.MuteUserResponse
import io.getstream.chat.android.client.api2.model.response.SyncHistoryResponse
import io.getstream.chat.android.client.parser2.GetMessageResponseParityTest
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.models.QueryRemindersResult
import io.getstream.chat.android.models.UnreadChannel
import io.getstream.chat.android.models.UnreadChannelByType
import io.getstream.chat.android.models.UnreadCounts
import io.getstream.chat.android.models.UnreadThread
import io.getstream.chat.android.models.UploadedFile
import io.getstream.chat.android.network.models.AddUserGroupMembersResponse
import io.getstream.chat.android.network.models.BlockUsersResponse
import io.getstream.chat.android.network.models.ChannelStateResponse
import io.getstream.chat.android.network.models.ChannelStateResponseFields
import io.getstream.chat.android.network.models.CreateDraftResponse
import io.getstream.chat.android.network.models.CreateGuestResponse
import io.getstream.chat.android.network.models.CreateReminderResponse
import io.getstream.chat.android.network.models.CreateUserGroupResponse
import io.getstream.chat.android.network.models.DeleteChannelResponse
import io.getstream.chat.android.network.models.DeleteMessageResponse
import io.getstream.chat.android.network.models.DeleteReactionResponse
import io.getstream.chat.android.network.models.GetApplicationResponse
import io.getstream.chat.android.network.models.GetBlockedUsersResponse
import io.getstream.chat.android.network.models.GetOGResponse
import io.getstream.chat.android.network.models.GetPinnedMessagesResponse
import io.getstream.chat.android.network.models.GetReactionsResponse
import io.getstream.chat.android.network.models.GetRepliesResponse
import io.getstream.chat.android.network.models.GetThreadResponse
import io.getstream.chat.android.network.models.GetUserGroupResponse
import io.getstream.chat.android.network.models.GroupedChannelsBucket
import io.getstream.chat.android.network.models.GroupedQueryChannelsResponse
import io.getstream.chat.android.network.models.ListDevicesResponse
import io.getstream.chat.android.network.models.ListUserGroupsResponse
import io.getstream.chat.android.network.models.MembersResponse
import io.getstream.chat.android.network.models.MessageActionResponse
import io.getstream.chat.android.network.models.ParsedPredefinedFilterResponse
import io.getstream.chat.android.network.models.PollOptionResponse
import io.getstream.chat.android.network.models.PollResponse
import io.getstream.chat.android.network.models.PollVoteResponse
import io.getstream.chat.android.network.models.PollVotesResponse
import io.getstream.chat.android.network.models.QueryBannedUsersResponse
import io.getstream.chat.android.network.models.QueryChannelsResponse
import io.getstream.chat.android.network.models.QueryDraftsResponse
import io.getstream.chat.android.network.models.QueryPollsResponse
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
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomLocation
import io.getstream.chat.android.randomString
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.call.map
import okhttp3.ResponseBody
import org.junit.jupiter.params.provider.Arguments
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

@Suppress("UNUSED", "LargeClass")
internal object MoshiChatApiTestArguments {

    @JvmStatic
    fun appSettingsInput() = listOf(
        Arguments.of(RetroSuccess(Mother.randomAppSettingsResponse()).toRetrofitCall(), Result.Success::class),
        Arguments.of(RetroError<GetApplicationResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    @JvmStatic
    fun sendMessageInput() =
        responseArguments(SendMessageResponse(duration = "1ms", message = Mother.randomMessageResponse()))

    @JvmStatic
    fun createDraftMessageInput() = draftMessageResponseArguments()

    @JvmStatic
    fun queryDraftMessageInput() = listOf(
        Arguments.of(
            RetroSuccess(Mother.randomQueryDraftsResponse()).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<QueryDraftsResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

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
            RetroSuccess(randomDeleteMessageResponse()).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(
            false,
            true,
            RetroSuccess(randomDeleteMessageResponse()).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(
            false,
            false,
            RetroSuccess(randomDeleteMessageResponse()).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(
            true,
            true,
            RetroSuccess(randomDeleteMessageResponse()).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(
            true,
            false,
            RetroError<DeleteMessageResponse>(statusCode = randomInt()).toRetrofitCall(),
            Result.Failure::class,
        ),
        Arguments.of(
            false,
            true,
            RetroError<DeleteMessageResponse>(statusCode = randomInt()).toRetrofitCall(),
            Result.Failure::class,
        ),
        Arguments.of(
            false,
            false,
            RetroError<DeleteMessageResponse>(statusCode = randomInt()).toRetrofitCall(),
            Result.Failure::class,
        ),
        Arguments.of(
            true,
            true,
            RetroError<DeleteMessageResponse>(statusCode = randomInt()).toRetrofitCall(),
            Result.Failure::class,
        ),
    )

    @JvmStatic
    fun getReactionsInput() = listOf(
        Arguments.of(
            RetroSuccess(
                GetReactionsResponse(
                    duration = randomString(),
                    reactions = listOf(Mother.randomReactionResponse()),
                ),
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<GetReactionsResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    @JvmStatic
    fun queryReactionsInput() = listOf(
        Arguments.of(
            RetroSuccess(
                QueryReactionsResponse(
                    duration = randomString(),
                    reactions = listOf(Mother.randomReactionResponse()),
                    next = randomString(),
                ),
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<QueryReactionsResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    @JvmStatic
    fun sendReactionInput() = listOf(
        Arguments.of(
            RetroSuccess(
                SendReactionResponse(
                    duration = "1ms",
                    message = Mother.randomMessageResponse(),
                    reaction = Mother.randomReactionResponse(),
                ),
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<SendReactionResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
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
    fun addDeviceInput() = completableResponseArguments()

    @JvmStatic
    fun deleteDeviceInput() = completableResponseArguments()

    @JvmStatic
    fun getDevicesInput() = listOf(
        Arguments.of(
            RetroSuccess(
                ListDevicesResponse(duration = randomString(), devices = listOf(Mother.randomDeviceResponse())),
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<ListDevicesResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

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
    fun showChannelInput() = completableResponseArguments()

    @JvmStatic
    fun hideChannelInput() = completableResponseArguments()

    @JvmStatic
    fun truncateChannelInput() = truncateChannelResponseArguments()

    @JvmStatic
    fun rejectInviteInput() = updateChannelResponseArguments()

    @JvmStatic
    fun acceptInviteInput() = updateChannelResponseArguments()

    @JvmStatic
    fun deleteChannelInput() = deleteChannelResponseArguments()

    @JvmStatic
    fun markReadInput() = completableResponseArguments()

    @JvmStatic
    fun markDeliveredInput() = completableResponseArguments()

    @JvmStatic
    fun markThreadReadInput() = completableResponseArguments()

    @JvmStatic
    fun markUnreadInput() = completableResponseArguments()

    @JvmStatic
    fun markThreadUnreadInput() = completableResponseArguments()

    @JvmStatic
    fun markAllReadInput() = completableResponseArguments()

    @JvmStatic
    fun addMembersInput() = updateChannelResponseArguments()

    @JvmStatic
    fun removeMembersInput() = updateChannelResponseArguments()

    @JvmStatic
    fun inviteMembersInput() = updateChannelResponseArguments()

    @JvmStatic
    fun partialUpdateMemberInput() = listOf(
        Arguments.of(
            RetroSuccess(
                UpdateMemberPartialResponse(
                    duration = randomString(),
                    channelMember = Mother.randomChannelMemberResponse(),
                ),
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(
            RetroError<UpdateMemberPartialResponse>(statusCode = 500).toRetrofitCall(),
            Result.Failure::class,
        ),
        // The member is optional in the response schema: a 200 without it must fail, not crash.
        Arguments.of(
            RetroSuccess(
                UpdateMemberPartialResponse(duration = randomString(), channelMember = null),
            ).toRetrofitCall(),
            Result.Failure::class,
        ),
    )

    @JvmStatic
    fun getNewerRepliesInput() = repliesResponseArguments()

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
    fun ogInput() = listOf(
        Arguments.of(RetroSuccess(Mother.randomGetOGResponse()).toRetrofitCall(), Result.Success::class),
        Arguments.of(RetroError<GetOGResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    @JvmStatic
    fun searchMessagesInput() = searchMessagesResponseArguments()

    @JvmStatic
    fun queryChannelsInput() = listOf(
        Arguments.of(
            RetroSuccess(
                QueryChannelsResponse(
                    duration = "1ms",
                    channels = listOf(
                        Mother.randomChannelStateResponseFields(),
                    ),
                ),
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<QueryChannelsResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    @JvmStatic
    fun queryGroupedChannelsInput() = listOf(
        Arguments.of(
            RetroSuccess(
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
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(
            RetroError<GroupedQueryChannelsResponse>(statusCode = 500).toRetrofitCall(),
            Result.Failure::class,
        ),
    )

    @JvmStatic
    fun queryChannelsWithPredefinedFilterInput() = listOf(
        Arguments.of(
            RetroSuccess(
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
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<QueryChannelsResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    @JvmStatic
    fun queryChannelInput() = listOf(
        Arguments.of(
            RetroSuccess(Mother.randomChannelStateResponseFields().toChannelStateResponse()).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<ChannelStateResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    /**
     * Rows of (stub serving a channel state, call reading the mapped channels back) for each REST path
     * that returns channel state.
     */
    @JvmStatic
    fun channelStateInput() = listOf(
        Arguments.of(
            "queryChannel",
            { api: ChannelApi, response: ChannelStateResponseFields ->
                whenever(api.queryChannel(any(), any(), any(), any()))
                    .doReturn(RetroSuccess(response.toChannelStateResponse()).toRetrofitCall())
            },
            { sut: MoshiChatApi ->
                sut.queryChannel(randomString(), randomString(), Mother.randomQueryChannelRequest())
                    .map { listOf(it) }
            },
        ),
        Arguments.of(
            "queryChannels",
            { api: ChannelApi, response: ChannelStateResponseFields ->
                val channels = QueryChannelsResponse(duration = "1ms", channels = listOf(response))
                whenever(api.queryChannels(any(), any())).doReturn(RetroSuccess(channels).toRetrofitCall())
            },
            { sut: MoshiChatApi ->
                sut.queryChannels(Mother.randomQueryChannelsRequest()).map { it.channels }
            },
        ),
        Arguments.of(
            "queryGroupedChannels",
            { api: ChannelApi, response: ChannelStateResponseFields ->
                val group = GroupedChannelsBucket(channels = listOf(response))
                whenever(api.queryGroupedChannels(any(), any())).doReturn(
                    RetroSuccess(
                        GroupedQueryChannelsResponse(groups = mapOf("all" to group), duration = "1ms"),
                    ).toRetrofitCall(),
                )
            },
            { sut: MoshiChatApi ->
                sut.queryGroupedChannels(limit = null, groups = null, watch = false, presence = false)
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
        Arguments.of(
            RetroSuccess(
                EventResponse(
                    event = HealthEventDto(
                        type = EventType.HEALTH_CHECK,
                        created_at = ExactDate(randomDate(), randomString()),
                        connection_id = randomString(),
                    ),
                    duration = randomString(),
                ),
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<EventResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    @JvmStatic
    fun getSyncHistoryInput() = listOf(
        Arguments.of(RetroSuccess(SyncHistoryResponse(emptyList())).toRetrofitCall(), Result.Success::class),
        Arguments.of(RetroError<SyncHistoryResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
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
    fun deletePollInput() = completableResponseArguments()

    @JvmStatic
    fun queryPollsInput() = listOf(
        Arguments.of(
            RetroSuccess(Mother.randomQueryPollsResponse()).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(
            RetroError<QueryPollsResponse>(statusCode = 500).toRetrofitCall(),
            Result.Failure::class,
        ),
    )

    @JvmStatic
    fun queryPollVotesInput() = listOf(
        Arguments.of(
            RetroSuccess(Mother.randomPollVotesResponse()).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(
            RetroError<PollVotesResponse>(statusCode = 500).toRetrofitCall(),
            Result.Failure::class,
        ),
    )

    @JvmStatic
    fun createPollOptionInput() = pollOptionResponseArguments()

    @JvmStatic
    fun updatePollOptionInput() = pollOptionResponseArguments()

    @JvmStatic
    fun deletePollOptionInput() = completableResponseArguments()

    @JvmStatic
    fun createReminderInput() =
        responseArgs(CreateReminderResponse(randomString(), Mother.randomReminderResponseData()))

    @JvmStatic
    fun updateReminderInput() =
        responseArgs(UpdateReminderResponse(randomString(), Mother.randomReminderResponseData()))

    @JvmStatic
    fun deleteReminderInput() = completableResponseArguments()

    @JvmStatic
    fun queryRemindersInput() = listOf(
        Arguments.of(
            RetroSuccess(Mother.randomQueryRemindersResponse()).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<QueryRemindersResult>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

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
                MuteUserResponse(
                    Mother.randomUserMuteResponse(),
                    Mother.randomOwnUserResponse(),
                ),
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
            RetroSuccess(
                UpdateChannelResponse(
                    duration = randomString(),
                    members = listOf(Mother.randomChannelMemberResponse()),
                    channel = Mother.randomChannelResponse(),
                ),
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(
            RetroSuccess(UpdateChannelResponse(duration = randomString())).toRetrofitCall(),
            Result.Failure::class,
        ),
        Arguments.of(RetroError<UpdateChannelResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    private fun updateChannelPartialResponseArguments() = listOf(
        Arguments.of(
            RetroSuccess(
                UpdateChannelPartialResponse(
                    duration = randomString(),
                    members = listOf(Mother.randomChannelMemberResponse()),
                    channel = Mother.randomChannelResponse(),
                ),
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(
            RetroSuccess(UpdateChannelPartialResponse(duration = randomString())).toRetrofitCall(),
            Result.Failure::class,
        ),
        Arguments.of(
            RetroError<UpdateChannelPartialResponse>(statusCode = 500).toRetrofitCall(),
            Result.Failure::class,
        ),
    )

    private fun truncateChannelResponseArguments() = listOf(
        Arguments.of(
            RetroSuccess(
                TruncateChannelResponse(duration = randomString(), channel = Mother.randomChannelResponse()),
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(
            RetroSuccess(TruncateChannelResponse(duration = randomString())).toRetrofitCall(),
            Result.Failure::class,
        ),
        Arguments.of(RetroError<TruncateChannelResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    private fun deleteChannelResponseArguments() = listOf(
        Arguments.of(
            RetroSuccess(
                DeleteChannelResponse(duration = randomString(), channel = Mother.randomChannelResponse()),
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(
            RetroSuccess(DeleteChannelResponse(duration = randomString())).toRetrofitCall(),
            Result.Failure::class,
        ),
        Arguments.of(RetroError<DeleteChannelResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    private fun randomDeleteMessageResponse() =
        DeleteMessageResponse(duration = "1ms", message = Mother.randomMessageResponse())

    private inline fun <reified T : Any> responseArguments(response: T, missingMessage: T? = null) = listOfNotNull(
        Arguments.of(RetroSuccess(response).toRetrofitCall(), Result.Success::class),
        missingMessage?.let { Arguments.of(RetroSuccess(it).toRetrofitCall(), Result.Failure::class) },
        Arguments.of(RetroError<T>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    private fun getMessageResponseArguments() = responseArguments(GetMessageResponseParityTest.getMessageResponse())

    private fun draftMessageResponseArguments() = listOf(
        Arguments.of(
            RetroSuccess(Mother.randomCreateDraftResponse()).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<CreateDraftResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    private fun repliesResponseArguments() = listOf(
        Arguments.of(
            RetroSuccess(GetRepliesResponse(duration = "1ms", messages = listOf(Mother.randomMessageResponse())))
                .toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<GetRepliesResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

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

    private fun pollResponseArguments() = listOf(
        Arguments.of(
            RetroSuccess(
                PollResponse(
                    poll = Mother.randomPollResponseData(),
                    duration = randomString(),
                ),
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<PollResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    private fun pollOptionResponseArguments() = listOf(
        Arguments.of(
            RetroSuccess(
                PollOptionResponse(
                    duration = randomString(),
                    pollOption = Mother.randomPollOptionResponseData(),
                ),
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<PollOptionResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    private fun pollVoteResponseArguments() = listOf(
        Arguments.of(
            RetroSuccess(
                PollVoteResponse(
                    duration = randomString(),
                    vote = Mother.randomPollVoteResponseData(),
                ),
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<PollVoteResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    private fun <T : Any> responseArgs(success: T) = listOf(
        Arguments.of(RetroSuccess(success).toRetrofitCall(), Result.Success::class),
        Arguments.of(RetroError<T>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    private fun userGroup() = Mother.randomUserGroupResponse()

    @JvmStatic
    fun createUserGroupResponseInput() =
        responseArgs(CreateUserGroupResponse(duration = randomString(), userGroup = userGroup()))

    @JvmStatic
    fun getUserGroupResponseInput() =
        responseArgs(GetUserGroupResponse(duration = randomString(), userGroup = userGroup()))

    @JvmStatic
    fun updateUserGroupResponseInput() =
        responseArgs(UpdateUserGroupResponse(duration = randomString(), userGroup = userGroup()))

    @JvmStatic
    fun addUserGroupMembersResponseInput() =
        responseArgs(AddUserGroupMembersResponse(duration = randomString(), userGroup = userGroup()))

    @JvmStatic
    fun removeUserGroupMembersResponseInput() =
        responseArgs(RemoveUserGroupMembersResponse(duration = randomString(), userGroup = userGroup()))

    @JvmStatic
    fun listUserGroupsResponseInput() =
        responseArgs(ListUserGroupsResponse(duration = randomString(), userGroups = listOf(userGroup())))

    @JvmStatic
    fun searchUserGroupsResponseInput() =
        responseArgs(SearchUserGroupsResponse(duration = randomString(), userGroups = listOf(userGroup())))

    @JvmStatic
    fun deleteUserGroupInput() = completableResponseArguments()

    @JvmStatic
    fun searchRolesInput() = listOf(
        Arguments.of(
            RetroSuccess(
                SearchRolesResponse(duration = randomString(), roles = listOf(Mother.randomRoleDto())),
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<SearchRolesResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )
}
