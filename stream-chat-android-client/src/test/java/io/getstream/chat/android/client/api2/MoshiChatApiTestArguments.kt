/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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
import io.getstream.chat.android.client.Mother.randomDownstreamDraftDto
import io.getstream.chat.android.client.Mother.randomDownstreamMessageDto
import io.getstream.chat.android.client.Mother.randomUnreadChannelByTypeDto
import io.getstream.chat.android.client.Mother.randomUnreadChannelDto
import io.getstream.chat.android.client.Mother.randomUnreadCountByTeamDto
import io.getstream.chat.android.client.Mother.randomUnreadDto
import io.getstream.chat.android.client.Mother.randomUnreadThreadDto
import io.getstream.chat.android.client.api.FakeResponse
import io.getstream.chat.android.client.api2.model.dto.AttachmentDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamLocationDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamReminderDto
import io.getstream.chat.android.client.api2.model.dto.HealthEventDto
import io.getstream.chat.android.client.api2.model.dto.utils.internal.ExactDate
import io.getstream.chat.android.client.api2.model.requests.UpdateLiveLocationRequest
import io.getstream.chat.android.client.api2.model.requests.UpdateMemberPartialResponse
import io.getstream.chat.android.client.api2.model.response.AppSettingsResponse
import io.getstream.chat.android.client.api2.model.response.BlockUserResponse
import io.getstream.chat.android.client.api2.model.response.ChannelResponse
import io.getstream.chat.android.client.api2.model.response.CompletableResponse
import io.getstream.chat.android.client.api2.model.response.DevicesResponse
import io.getstream.chat.android.client.api2.model.response.EventResponse
import io.getstream.chat.android.client.api2.model.response.FlagResponse
import io.getstream.chat.android.client.api2.model.response.MessageResponse
import io.getstream.chat.android.client.api2.model.response.MessagesResponse
import io.getstream.chat.android.client.api2.model.response.MuteUserResponse
import io.getstream.chat.android.client.api2.model.response.PollOptionResponse
import io.getstream.chat.android.client.api2.model.response.PollResponse
import io.getstream.chat.android.client.api2.model.response.PollVoteResponse
import io.getstream.chat.android.client.api2.model.response.QueryBannedUsersResponse
import io.getstream.chat.android.client.api2.model.response.QueryBlockedUsersResponse
import io.getstream.chat.android.client.api2.model.response.QueryChannelsResponse
import io.getstream.chat.android.client.api2.model.response.QueryDraftMessagesResponse
import io.getstream.chat.android.client.api2.model.response.QueryMembersResponse
import io.getstream.chat.android.client.api2.model.response.QueryPollVotesResponse
import io.getstream.chat.android.client.api2.model.response.QueryPollsResponse
import io.getstream.chat.android.client.api2.model.response.QueryReactionsResponse
import io.getstream.chat.android.client.api2.model.response.QueryThreadsResponse
import io.getstream.chat.android.client.api2.model.response.ReactionResponse
import io.getstream.chat.android.client.api2.model.response.ReactionsResponse
import io.getstream.chat.android.client.api2.model.response.ReminderResponse
import io.getstream.chat.android.client.api2.model.response.SearchMessagesResponse
import io.getstream.chat.android.client.api2.model.response.SyncHistoryResponse
import io.getstream.chat.android.client.api2.model.response.ThreadResponse
import io.getstream.chat.android.client.api2.model.response.TokenResponse
import io.getstream.chat.android.client.api2.model.response.UpdateUsersResponse
import io.getstream.chat.android.client.api2.model.response.UsersResponse
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.models.QueryRemindersResult
import io.getstream.chat.android.models.UnreadChannel
import io.getstream.chat.android.models.UnreadChannelByType
import io.getstream.chat.android.models.UnreadCounts
import io.getstream.chat.android.models.UnreadThread
import io.getstream.chat.android.models.UploadedFile
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomDateOrNull
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomLocation
import io.getstream.chat.android.randomPendingMessageMetadata
import io.getstream.chat.android.randomString
import io.getstream.result.Error
import io.getstream.result.Result
import okhttp3.ResponseBody
import org.junit.jupiter.params.provider.Arguments

@Suppress("UNUSED", "LargeClass")
internal object MoshiChatApiTestArguments {

    @JvmStatic
    fun appSettingsInput() = listOf(
        Arguments.of(RetroSuccess(Mother.randomAppSettingsResponse()).toRetrofitCall(), Result.Success::class),
        Arguments.of(RetroError<AppSettingsResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    @JvmStatic
    fun sendMessageInput() = messageResponseArguments()

    @JvmStatic
    fun createDraftMessageInput() = draftMessageResponseArguments()

    @JvmStatic
    fun queryDraftMessageInput() = listOf(
        Arguments.of(
            RetroSuccess(Mother.randomQueryDraftMessagesResponse()).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<QueryDraftMessagesResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    @JvmStatic
    fun updateMessageInput() = messageResponseArguments()

    @JvmStatic
    fun partialUpdateMessageInput() = messageResponseArguments()

    @JvmStatic
    fun getMessageInput() = messageResponseArguments()

    @JvmStatic
    fun getPendingMessageInput() = messageResponseArguments()

    @JvmStatic
    fun deleteMessageInput() = listOf(
        Arguments.of(
            true,
            false,
            RetroSuccess(MessageResponse(randomDownstreamMessageDto())).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(
            false,
            true,
            RetroSuccess(MessageResponse(randomDownstreamMessageDto())).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(
            false,
            false,
            RetroSuccess(MessageResponse(randomDownstreamMessageDto())).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(
            true,
            true,
            RetroSuccess(MessageResponse(randomDownstreamMessageDto())).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(
            true,
            false,
            RetroError<MessageResponse>(statusCode = randomInt()).toRetrofitCall(),
            Result.Failure::class,
        ),
        Arguments.of(
            false,
            true,
            RetroError<MessageResponse>(statusCode = randomInt()).toRetrofitCall(),
            Result.Failure::class,
        ),
        Arguments.of(
            false,
            false,
            RetroError<MessageResponse>(statusCode = randomInt()).toRetrofitCall(),
            Result.Failure::class,
        ),
        Arguments.of(
            true,
            true,
            RetroError<MessageResponse>(statusCode = randomInt()).toRetrofitCall(),
            Result.Failure::class,
        ),
    )

    @JvmStatic
    fun getReactionsInput() = listOf(
        Arguments.of(
            RetroSuccess(ReactionsResponse(listOf(Mother.randomDownstreamReactionDto()))).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<ReactionsResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    @JvmStatic
    fun queryReactionsInput() = listOf(
        Arguments.of(
            RetroSuccess(
                QueryReactionsResponse(
                    reactions = listOf(Mother.randomDownstreamReactionDto()),
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
            RetroSuccess(ReactionResponse(Mother.randomDownstreamReactionDto())).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<ReactionResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    @JvmStatic
    fun deleteReactionInput() = messageResponseArguments()

    @JvmStatic
    fun addDeviceInput() = completableResponseArguments()

    @JvmStatic
    fun deleteDeviceInput() = completableResponseArguments()

    @JvmStatic
    fun getDevicesInput() = listOf(
        Arguments.of(
            RetroSuccess(DevicesResponse(listOf(Mother.randomDeviceDto()))).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<DevicesResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
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
            RetroSuccess(QueryBannedUsersResponse(listOf(Mother.randomBannedUserResponse()))).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(
            RetroError<QueryBannedUsersResponse>(statusCode = 500).toRetrofitCall(),
            Result.Failure::class,
        ),
    )

    @JvmStatic
    fun enableSlowModeInput() = channelResponseArguments()

    @JvmStatic
    fun disableSlowModeInput() = channelResponseArguments()

    @JvmStatic
    fun stopWatchingInput() = completableResponseArguments()

    @JvmStatic
    fun getPinnedMessagesInput() = messagesResponseArguments()

    @JvmStatic
    fun updateChannelInput() = channelResponseArguments()

    @JvmStatic
    fun updateChannelPartialInput() = channelResponseArguments()

    @JvmStatic
    fun showChannelInput() = completableResponseArguments()

    @JvmStatic
    fun hideChannelInput() = completableResponseArguments()

    @JvmStatic
    fun truncateChannelInput() = channelResponseArguments()

    @JvmStatic
    fun rejectInviteInput() = channelResponseArguments()

    @JvmStatic
    fun acceptInviteInput() = channelResponseArguments()

    @JvmStatic
    fun deleteChannelInput() = channelResponseArguments()

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
    fun addMembersInput() = channelResponseArguments()

    @JvmStatic
    fun removeMembersInput() = channelResponseArguments()

    @JvmStatic
    fun inviteMembersInput() = channelResponseArguments()

    @JvmStatic
    fun partialUpdateMemberInput() = listOf(
        Arguments.of(
            RetroSuccess(UpdateMemberPartialResponse(Mother.randomDownstreamMemberDto())).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(
            RetroError<UpdateMemberPartialResponse>(statusCode = 500).toRetrofitCall(),
            Result.Failure::class,
        ),
    )

    @JvmStatic
    fun getNewerRepliesInput() = messagesResponseArguments()

    @JvmStatic
    fun getRepliesInput() = messagesResponseArguments()

    @JvmStatic
    fun getRepliesMoreInput() = messagesResponseArguments()

    @JvmStatic
    fun sendActionInput() = messageResponseArguments()

    @JvmStatic
    fun updateUsersInput() = updateUsersResponseArguments()

    @JvmStatic
    fun blockUserInput() = listOf(
        Arguments.of(RetroSuccess(Mother.randomBlockUserResponse()).toRetrofitCall(), Result.Success::class),
        Arguments.of(RetroError<BlockUserResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    @JvmStatic
    fun unblockUserInput() = listOf(
        Arguments.of(RetroSuccess(Mother.randomUnblockUserResponse()).toRetrofitCall(), Result.Success::class),
        Arguments.of(RetroError<BlockUserResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    @JvmStatic
    fun queryBlockedUsersInput() = listOf(
        Arguments.of(
            RetroSuccess(QueryBlockedUsersResponse(listOf(Mother.randomDownstreamUserBlockDto()))).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(
            RetroError<QueryBlockedUsersResponse>(statusCode = 500).toRetrofitCall(),
            Result.Failure::class,
        ),
    )

    @JvmStatic
    fun partialUpdateUserInput() = updateUsersResponseArguments()

    @JvmStatic
    fun getGuestUserInput() = listOf(
        Arguments.of(RetroSuccess(Mother.randomTokenResponse()).toRetrofitCall(), Result.Success::class),
        Arguments.of(RetroError<TokenResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    @JvmStatic
    fun translateInput() = messageResponseArguments()

    @JvmStatic
    fun ogInput() = listOf(
        Arguments.of(RetroSuccess(Mother.randomAttachmentDto()).toRetrofitCall(), Result.Success::class),
        Arguments.of(RetroError<AttachmentDto>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    @JvmStatic
    fun searchMessagesInput() = searchMessagesResponseArguments()

    @JvmStatic
    fun queryChannelsInput() = listOf(
        Arguments.of(
            RetroSuccess(
                QueryChannelsResponse(
                    listOf(
                        ChannelResponse(
                            channel = Mother.randomDownstreamChannelDto(),
                            hidden = randomBoolean(),
                            membership = Mother.randomDownstreamMemberDto(),
                            hide_messages_before = randomDateOrNull(),
                            draft = randomDownstreamDraftDto(),
                        ),
                    ),
                ),
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<QueryChannelsResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    @JvmStatic
    fun queryChannelInput() = channelResponseArguments()

    @JvmStatic
    fun queryUsersInput() = listOf(
        Arguments.of(
            RetroSuccess(UsersResponse(listOf(Mother.randomDownstreamUserDto()))).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<UsersResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    @JvmStatic
    fun queryMembersInput() = listOf(
        Arguments.of(
            RetroSuccess(QueryMembersResponse(listOf(Mother.randomDownstreamMemberDto()))).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<QueryMembersResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
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
                    threads = listOf(Mother.randomDownstreamThreadDto()),
                    duration = randomString(),
                    prev = randomString(),
                    next = randomString(),
                ),
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<QueryThreadsResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    @JvmStatic
    fun getThreadInput() = threadResponseArguments()

    @JvmStatic
    fun partialUpdateThreadInput() = threadResponseArguments()

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
            RetroSuccess(Mother.randomQueryPollVotesResponse()).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(
            RetroError<QueryPollVotesResponse>(statusCode = 500).toRetrofitCall(),
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
    fun createReminderInput() = reminderResponseArguments()

    @JvmStatic
    fun updateReminderInput() = reminderResponseArguments()

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
                message_id = location.messageId,
                latitude = location.latitude,
                longitude = location.longitude,
                created_by_device_id = location.deviceId,
            )
            val response = DownstreamLocationDto(
                message_id = location.messageId,
                channel_cid = location.cid,
                user_id = location.userId,
                latitude = location.latitude,
                longitude = location.longitude,
                created_by_device_id = location.deviceId,
                end_at = location.endAt,
            )
            Arguments.of(location, request, response)
        },
    )

    @JvmStatic
    fun stopLiveLocation() = listOf(
        run {
            val location = randomLocation()
            val request = UpdateLiveLocationRequest(
                message_id = location.messageId,
                created_by_device_id = location.deviceId,
                end_at = location.endAt,
            )
            val response = DownstreamLocationDto(
                message_id = location.messageId,
                channel_cid = location.cid,
                user_id = location.userId,
                latitude = location.latitude,
                longitude = location.longitude,
                created_by_device_id = location.deviceId,
                end_at = location.endAt,
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
                messagesCount = dto.total_unread_count,
                threadsCount = dto.total_unread_threads_count,
                messagesCountByTeam = dto.total_unread_count_by_team!!,
                channels = dto.channels.map { dto ->
                    UnreadChannel(
                        cid = dto.channel_id,
                        messagesCount = dto.unread_count,
                        lastRead = dto.last_read,
                    )
                },
                threads = dto.threads.map { dto ->
                    UnreadThread(
                        parentMessageId = dto.parent_message_id,
                        messagesCount = dto.unread_count,
                        lastRead = dto.last_read,
                        lastReadMessageId = dto.last_read_message_id,
                    )
                },
                channelsByType = dto.channel_type.map { dto ->
                    UnreadChannelByType(
                        channelType = dto.channel_type,
                        channelsCount = dto.channel_count,
                        messagesCount = dto.unread_count,
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
                    Mother.randomDownstreamMuteDto(),
                    Mother.randomDownstreamUserDto(),
                ),
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<CompletableResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    private fun completableResponseArguments() = listOf(
        Arguments.of(RetroSuccess(CompletableResponse("")).toRetrofitCall(), Result.Success::class),
        Arguments.of(RetroError<CompletableResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
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

    private fun channelResponseArguments() = listOf(
        Arguments.of(
            RetroSuccess(
                ChannelResponse(
                    channel = Mother.randomDownstreamChannelDto(),
                    hidden = randomBoolean(),
                    membership = Mother.randomDownstreamMemberDto(),
                    hide_messages_before = randomDateOrNull(),
                    draft = randomDownstreamDraftDto(),
                ),
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<ChannelResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    private fun messageResponseArguments() = listOf(
        Arguments.of(
            RetroSuccess(
                MessageResponse(
                    message = randomDownstreamMessageDto(),
                    pending_message_metadata = randomPendingMessageMetadata(),
                ),
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<MessageResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    private fun draftMessageResponseArguments() = listOf(
        Arguments.of(
            RetroSuccess(Mother.randomDraftMessageResponse()).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<MessageResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    private fun messagesResponseArguments() = listOf(
        Arguments.of(
            RetroSuccess(MessagesResponse(listOf(randomDownstreamMessageDto()))).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<MessagesResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    private fun updateUsersResponseArguments(): List<Arguments> {
        val userId = randomString()
        val user = Mother.randomDownstreamUserDto()
        val response = UpdateUsersResponse(mapOf(userId to user))
        return listOf(
            Arguments.of(RetroSuccess(response).toRetrofitCall(), Result.Success::class),
            Arguments.of(RetroError<UpdateUsersResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
        )
    }

    private fun searchMessagesResponseArguments() = listOf(
        Arguments.of(
            RetroSuccess(
                SearchMessagesResponse(
                    results = listOf(MessageResponse(randomDownstreamMessageDto())),
                    next = randomString(),
                    previous = randomString(),
                    resultsWarning = Mother.randomSearchWarningDto(),
                ),
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<SearchMessagesResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    private fun threadResponseArguments() = listOf(
        Arguments.of(
            RetroSuccess(
                ThreadResponse(
                    thread = Mother.randomDownstreamThreadDto(),
                    duration = randomString(),
                ),
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<ThreadResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    private fun pollResponseArguments() = listOf(
        Arguments.of(
            RetroSuccess(
                PollResponse(
                    poll = Mother.randomDownstreamPollDto(),
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
                    poll_option = Mother.randomDownstreamOptionDto(),
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
                    vote = Mother.randomDownstreamVoteDto(),
                ),
            ).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<PollVoteResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )

    @JvmStatic
    private fun reminderResponseArguments() = listOf(
        Arguments.of(
            RetroSuccess(ReminderResponse(Mother.randomDownstreamReminderDto())).toRetrofitCall(),
            Result.Success::class,
        ),
        Arguments.of(RetroError<DownstreamReminderDto>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
    )
}
