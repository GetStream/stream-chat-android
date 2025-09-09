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
import io.getstream.chat.android.client.api.models.PinnedMessagesPagination
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
import io.getstream.chat.android.client.api2.endpoint.RemindersApi
import io.getstream.chat.android.client.api2.endpoint.ThreadsApi
import io.getstream.chat.android.client.api2.endpoint.UserApi
import io.getstream.chat.android.client.api2.endpoint.VideoCallApi
import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.api2.mapping.DtoMapping
import io.getstream.chat.android.client.api2.mapping.EventMapping
import io.getstream.chat.android.client.api2.model.dto.AttachmentDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamLocationDto
import io.getstream.chat.android.client.api2.model.dto.PartialUpdateUserDto
import io.getstream.chat.android.client.api2.model.dto.UnreadDto
import io.getstream.chat.android.client.api2.model.requests.AcceptInviteRequest
import io.getstream.chat.android.client.api2.model.requests.AddDeviceRequest
import io.getstream.chat.android.client.api2.model.requests.BanUserRequest
import io.getstream.chat.android.client.api2.model.requests.BlockUserRequest
import io.getstream.chat.android.client.api2.model.requests.FlagMessageRequest
import io.getstream.chat.android.client.api2.model.requests.FlagUserRequest
import io.getstream.chat.android.client.api2.model.requests.GuestUserRequest
import io.getstream.chat.android.client.api2.model.requests.HideChannelRequest
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
import io.getstream.chat.android.client.api2.model.requests.QueryRemindersRequest
import io.getstream.chat.android.client.api2.model.requests.RejectInviteRequest
import io.getstream.chat.android.client.api2.model.requests.ReminderRequest
import io.getstream.chat.android.client.api2.model.requests.SendActionRequest
import io.getstream.chat.android.client.api2.model.requests.SendEventRequest
import io.getstream.chat.android.client.api2.model.requests.SuggestPollOptionRequest
import io.getstream.chat.android.client.api2.model.requests.UnblockUserRequest
import io.getstream.chat.android.client.api2.model.requests.UpdateChannelPartialRequest
import io.getstream.chat.android.client.api2.model.requests.UpdateCooldownRequest
import io.getstream.chat.android.client.api2.model.requests.UpdateLiveLocationRequest
import io.getstream.chat.android.client.api2.model.requests.UpdateMemberPartialRequest
import io.getstream.chat.android.client.api2.model.requests.UpdateMemberPartialResponse
import io.getstream.chat.android.client.api2.model.requests.UpstreamOptionDto
import io.getstream.chat.android.client.api2.model.requests.UpstreamVoteDto
import io.getstream.chat.android.client.api2.model.response.AppSettingsResponse
import io.getstream.chat.android.client.api2.model.response.BlockUserResponse
import io.getstream.chat.android.client.api2.model.response.ChannelResponse
import io.getstream.chat.android.client.api2.model.response.CompletableResponse
import io.getstream.chat.android.client.api2.model.response.DevicesResponse
import io.getstream.chat.android.client.api2.model.response.DraftMessageResponse
import io.getstream.chat.android.client.api2.model.response.EventResponse
import io.getstream.chat.android.client.api2.model.response.FlagResponse
import io.getstream.chat.android.client.api2.model.response.MessageResponse
import io.getstream.chat.android.client.api2.model.response.MessagesResponse
import io.getstream.chat.android.client.api2.model.response.MuteUserResponse
import io.getstream.chat.android.client.api2.model.response.PollResponse
import io.getstream.chat.android.client.api2.model.response.PollVoteResponse
import io.getstream.chat.android.client.api2.model.response.QueryBannedUsersResponse
import io.getstream.chat.android.client.api2.model.response.QueryBlockedUsersResponse
import io.getstream.chat.android.client.api2.model.response.QueryChannelsResponse
import io.getstream.chat.android.client.api2.model.response.QueryDraftMessagesResponse
import io.getstream.chat.android.client.api2.model.response.QueryMembersResponse
import io.getstream.chat.android.client.api2.model.response.QueryRemindersResponse
import io.getstream.chat.android.client.api2.model.response.QueryThreadsResponse
import io.getstream.chat.android.client.api2.model.response.ReactionResponse
import io.getstream.chat.android.client.api2.model.response.ReactionsResponse
import io.getstream.chat.android.client.api2.model.response.ReminderResponse
import io.getstream.chat.android.client.api2.model.response.SearchMessagesResponse
import io.getstream.chat.android.client.api2.model.response.SuggestPollOptionResponse
import io.getstream.chat.android.client.api2.model.response.SyncHistoryResponse
import io.getstream.chat.android.client.api2.model.response.ThreadResponse
import io.getstream.chat.android.client.api2.model.response.TokenResponse
import io.getstream.chat.android.client.api2.model.response.TranslateMessageRequest
import io.getstream.chat.android.client.api2.model.response.UnblockUserResponse
import io.getstream.chat.android.client.api2.model.response.UpdateUsersResponse
import io.getstream.chat.android.client.api2.model.response.UsersResponse
import io.getstream.chat.android.client.call.RetrofitCall
import io.getstream.chat.android.client.parser.toMap
import io.getstream.chat.android.client.scope.ClientScope
import io.getstream.chat.android.client.scope.UserScope
import io.getstream.chat.android.client.uploader.FileTransformer
import io.getstream.chat.android.client.uploader.FileUploader
import io.getstream.chat.android.client.uploader.NoOpFileTransformer
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.client.utils.verifyNetworkError
import io.getstream.chat.android.client.utils.verifySuccess
import io.getstream.chat.android.models.BannedUsersSort
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageReminder
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.UnreadCounts
import io.getstream.chat.android.models.UploadedFile
import io.getstream.chat.android.models.VotingVisibility
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomChatNetworkError
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomDevice
import io.getstream.chat.android.randomDraftMessage
import io.getstream.chat.android.randomFile
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMemberData
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomPollConfig
import io.getstream.chat.android.randomReaction
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUploadedFile
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.amshove.kluent.`should be instance of`
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.reflect.KClass

@Suppress("LargeClass")
internal class MoshiChatApiTest {

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#appSettingsInput")
    fun testAppSettings(call: RetrofitCall<AppSettingsResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ConfigApi>()
        whenever(api.getAppSettings()).doReturn(call)
        val sut = Fixture()
            .withConfigApi(api)
            .get()
        // when
        val result = sut.appSettings().await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).getAppSettings()
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#sendMessageInput")
    fun testSendMessage(call: RetrofitCall<MessageResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<MessageApi>()
        whenever(api.sendMessage(any(), any(), any())).doReturn(call)
        val sut = Fixture()
            .withMessageApi(api)
            .get()
        // when
        val result = sut.sendMessage(randomString(), randomString(), randomMessage()).await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).sendMessage(any(), any(), any())
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#createDraftMessageInput")
    fun testCreateDraftMessage(call: RetrofitCall<DraftMessageResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<MessageApi>()
        whenever(api.createDraftMessage(any(), any(), any())).doReturn(call)
        val sut = Fixture()
            .withMessageApi(api)
            .get()
        // when
        val result = sut.createDraftMessage(randomString(), randomString(), randomDraftMessage()).await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).createDraftMessage(any(), any(), any())
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#createDraftMessageInput")
    fun testDeleteDraftMessage(call: RetrofitCall<CompletableResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<MessageApi>()
        whenever(api.deleteDraftMessage(any(), any(), any())).doReturn(call)
        val sut = Fixture()
            .withMessageApi(api)
            .get()
        // when
        val result = sut.deleteDraftMessage(randomString(), randomString(), randomDraftMessage()).await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).deleteDraftMessage(any(), any(), any())
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#queryDraftMessageInput")
    fun testQueryDraftMessage(call: RetrofitCall<QueryDraftMessagesResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<MessageApi>()
        whenever(api.queryDraftMessages(any())).doReturn(call)
        val sut = Fixture()
            .withMessageApi(api)
            .get()
        // when
        val result = sut.queryDraftMessages(
            offset = randomInt(),
            limit = randomInt(),
        ).await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).queryDraftMessages(any())
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#queryDraftMessageInput")
    fun testQueryDrafts(call: RetrofitCall<QueryDraftMessagesResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<MessageApi>()
        whenever(api.queryDrafts(any())).doReturn(call)
        val sut = Fixture()
            .withMessageApi(api)
            .get()
        // when
        val result = sut.queryDrafts(
            filter = Filters.neutral(),
            limit = positiveRandomInt(),
            next = randomString(),
            sort = QuerySortByField(),
        ).await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).queryDrafts(any())
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#updateMessageInput")
    fun testUpdateMessage(call: RetrofitCall<MessageResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<MessageApi>()
        whenever(api.updateMessage(any(), any())).doReturn(call)
        val sut = Fixture()
            .withMessageApi(api)
            .get()
        // when
        val message = randomMessage()
        val result = sut.updateMessage(message).await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).updateMessage(eq(message.id), any())
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#partialUpdateMessageInput")
    fun testPartialUpdateMessage(call: RetrofitCall<MessageResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<MessageApi>()
        whenever(api.partialUpdateMessage(any(), any())).doReturn(call)
        val sut = Fixture()
            .withMessageApi(api)
            .get()
        // when
        val messageId = randomString()
        val set = emptyMap<String, Any>()
        val unset = emptyList<String>()
        val skipEnrichUrl = randomBoolean()
        val result = sut.partialUpdateMessage(messageId, set, unset, skipEnrichUrl).await()
        // then
        val expectedRequest = PartialUpdateMessageRequest(
            set = set,
            unset = unset,
            skip_enrich_url = skipEnrichUrl,
        )
        result `should be instance of` expected
        verify(api, times(1)).partialUpdateMessage(messageId, expectedRequest)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#getMessageInput")
    fun testGetMessage(call: RetrofitCall<MessageResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<MessageApi>()
        whenever(api.getMessage(any())).doReturn(call)
        val sut = Fixture()
            .withMessageApi(api)
            .get()
        // when
        val messageId = randomString()
        val result = sut.getMessage(messageId).await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).getMessage(messageId)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#getPendingMessageInput")
    fun testGetPendingMessage(call: RetrofitCall<MessageResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<MessageApi>()
        whenever(api.getMessage(any())).doReturn(call)
        val sut = Fixture()
            .withMessageApi(api)
            .get()
        // when
        val messageId = randomString()
        val result = sut.getPendingMessage(messageId).await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).getMessage(messageId)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#deleteMessageInput")
    fun testDeleteMessage(
        hard: Boolean,
        call: RetrofitCall<MessageResponse>,
        expected: KClass<*>,
    ) = runTest {
        // given
        val api = mock<MessageApi>()
        whenever(api.deleteMessage(any(), anyOrNull())).doReturn(call)
        val sut = Fixture()
            .withMessageApi(api)
            .get()
        // when
        val messageId = randomString()
        val result = sut.deleteMessage(messageId, hard).await()
        // then
        val expectedHard = if (hard) true else null
        result `should be instance of` expected
        verify(api, times(1)).deleteMessage(messageId, expectedHard)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#getReactionsInput")
    fun testGetReactions(call: RetrofitCall<ReactionsResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<MessageApi>()
        whenever(api.getReactions(any(), any(), any())).doReturn(call)
        val sut = Fixture()
            .withMessageApi(api)
            .get()
        // when
        val messageId = randomString()
        val offset = randomInt()
        val limit = randomInt()
        val result = sut.getReactions(messageId, offset, limit).await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).getReactions(messageId, offset, limit)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#sendReactionInput")
    fun testSendReaction(call: RetrofitCall<ReactionResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<MessageApi>()
        whenever(api.sendReaction(any(), any())).doReturn(call)
        val sut = Fixture()
            .withMessageApi(api)
            .get()
        // when
        val reaction = randomReaction()
        val enforceUnique = randomBoolean()
        val result = sut.sendReaction(reaction, enforceUnique).await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).sendReaction(eq(reaction.messageId), any())
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#deleteReactionInput")
    fun testDeleteReaction(call: RetrofitCall<MessageResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<MessageApi>()
        whenever(api.deleteReaction(any(), any())).doReturn(call)
        val sut = Fixture()
            .withMessageApi(api)
            .get()
        // when
        val messageId = randomString()
        val reactionType = randomString()
        val result = sut.deleteReaction(messageId, reactionType).await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).deleteReaction(messageId, reactionType)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#addDeviceInput")
    fun testAddDevice(call: RetrofitCall<CompletableResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<DeviceApi>()
        whenever(api.addDevices(any())).doReturn(call)
        val sut = Fixture()
            .withDeviceApi(api)
            .get()
        // when
        val device = randomDevice()
        val result = sut.addDevice(device).await()
        // then
        val expectedRequest = AddDeviceRequest(
            id = device.token,
            push_provider = device.pushProvider.key,
            push_provider_name = device.providerName,
        )
        result `should be instance of` expected
        verify(api, times(1)).addDevices(expectedRequest)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#deleteDeviceInput")
    fun testDeleteDevice(call: RetrofitCall<CompletableResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<DeviceApi>()
        whenever(api.deleteDevice(any())).doReturn(call)
        val sut = Fixture()
            .withDeviceApi(api)
            .get()
        // when
        val device = randomDevice()
        val result = sut.deleteDevice(device).await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).deleteDevice(device.token)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#getDevicesInput")
    fun testGetDevices(call: RetrofitCall<DevicesResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<DeviceApi>()
        whenever(api.getDevices()).doReturn(call)
        val sut = Fixture()
            .withDeviceApi(api)
            .get()
        // when
        val result = sut.getDevices().await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).getDevices()
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#muteCurrentUserInput")
    fun testMuteCurrentUser(call: RetrofitCall<MuteUserResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ModerationApi>()
        whenever(api.muteUser(any())).doReturn(call)
        val sut = Fixture()
            .withModerationApi(api)
            .get()
        // when
        val userId = randomString()
        val connectionId = randomString()
        sut.setConnection(userId = userId, connectionId = connectionId)
        val result = sut.muteCurrentUser().await()
        // then
        val expectedRequest = MuteUserRequest(
            target_id = userId,
            user_id = userId,
            timeout = null,
        )
        result `should be instance of` expected
        verify(api, times(1)).muteUser(expectedRequest)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#muteUserInput")
    fun testMuteUser(call: RetrofitCall<MuteUserResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ModerationApi>()
        whenever(api.muteUser(any())).doReturn(call)
        val sut = Fixture()
            .withModerationApi(api)
            .get()
        // when
        val userId = randomString()
        val connectionId = randomString()
        val targetId = randomString()
        val timeout = randomInt()
        sut.setConnection(userId = userId, connectionId = connectionId)
        val result = sut.muteUser(targetId, timeout).await()
        // then
        val expectedRequest = MuteUserRequest(
            target_id = targetId,
            user_id = userId,
            timeout = timeout,
        )
        result `should be instance of` expected
        verify(api, times(1)).muteUser(expectedRequest)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#unmuteCurrentUserInput")
    fun testUnmuteCurrentUser(call: RetrofitCall<CompletableResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ModerationApi>()
        whenever(api.unmuteUser(any())).doReturn(call)
        val sut = Fixture()
            .withModerationApi(api)
            .get()
        // when
        val userId = randomString()
        val connectionId = randomString()
        sut.setConnection(userId = userId, connectionId = connectionId)
        val result = sut.unmuteCurrentUser().await()
        // then
        val expectedRequest = MuteUserRequest(
            target_id = userId,
            user_id = userId,
            timeout = null,
        )
        result `should be instance of` expected
        verify(api, times(1)).unmuteUser(expectedRequest)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#unmuteUserInput")
    fun testUnmuteUser(call: RetrofitCall<CompletableResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ModerationApi>()
        whenever(api.unmuteUser(any())).doReturn(call)
        val sut = Fixture()
            .withModerationApi(api)
            .get()
        // when
        val userId = randomString()
        val connectionId = randomString()
        val targetId = randomString()
        sut.setConnection(userId = userId, connectionId = connectionId)
        val result = sut.unmuteUser(targetId).await()
        // then
        val expectedRequest = MuteUserRequest(
            target_id = targetId,
            user_id = userId,
            timeout = null,
        )
        result `should be instance of` expected
        verify(api, times(1)).unmuteUser(expectedRequest)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#muteChannelInput")
    fun testMuteChannel(call: RetrofitCall<CompletableResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ModerationApi>()
        whenever(api.muteChannel(any())).doReturn(call)
        val sut = Fixture()
            .withModerationApi(api)
            .get()
        // when
        val channelType = randomString()
        val channelId = randomString()
        val expiration = positiveRandomInt()
        val result = sut.muteChannel(channelType, channelId, expiration).await()
        // then
        val expectedRequest = MuteChannelRequest(
            channel_cid = "$channelType:$channelId",
            expiration = expiration,
        )
        result `should be instance of` expected
        verify(api, times(1)).muteChannel(expectedRequest)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#unmuteChannelInput")
    fun testUnmuteChannel(call: RetrofitCall<CompletableResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ModerationApi>()
        whenever(api.unmuteChannel(any())).doReturn(call)
        val sut = Fixture()
            .withModerationApi(api)
            .get()
        // when
        val channelType = randomString()
        val channelId = randomString()
        val result = sut.unmuteChannel(channelType, channelId).await()
        // then
        val expectedRequest = MuteChannelRequest(
            channel_cid = "$channelType:$channelId",
            expiration = null,
        )
        result `should be instance of` expected
        verify(api, times(1)).unmuteChannel(expectedRequest)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#sendFileInput")
    fun testSendFileWithCallback(fileUploaderResult: Result<UploadedFile>, expected: KClass<*>) = runTest {
        // given
        val fileUploader = mock<FileUploader>()
        whenever(fileUploader.sendFile(any(), any(), any(), any(), any())).doReturn(fileUploaderResult)
        val fileTransformer = spy<NoOpFileTransformer>()
        val sut = Fixture()
            .withFileUploader(fileUploader)
            .withFileTransformer(fileTransformer)
            .get()
        // when
        val userId = randomString()
        val connectionId = randomString()
        val channelType = randomString()
        val channelId = randomString()
        val file = randomFile()
        val callback = mock<ProgressCallback>()
        sut.setConnection(userId = userId, connectionId = connectionId)
        val result = sut.sendFile(channelType, channelId, file, callback).await()
        // then
        result `should be instance of` expected
        verify(fileUploader, times(1)).sendFile(channelType, channelId, userId, file, callback)
        verify(fileUploader, never()).sendFile(any(), any(), any(), any())
        verify(fileTransformer, times(1)).transform(file)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#sendFileInput")
    fun testSendFileWithoutCallback(fileUploaderResult: Result<UploadedFile>, expected: KClass<*>) = runTest {
        // given
        val fileUploader = mock<FileUploader>()
        whenever(fileUploader.sendFile(any(), any(), any(), any())).doReturn(fileUploaderResult)
        val fileTransformer = spy<NoOpFileTransformer>()
        val sut = Fixture()
            .withFileUploader(fileUploader)
            .withFileTransformer(fileTransformer)
            .get()
        // when
        val userId = randomString()
        val connectionId = randomString()
        val channelType = randomString()
        val channelId = randomString()
        val file = randomFile()
        sut.setConnection(userId = userId, connectionId = connectionId)
        val result = sut.sendFile(channelType, channelId, file).await()
        // then
        result `should be instance of` expected
        verify(fileUploader, times(1)).sendFile(channelType, channelId, userId, file)
        verify(fileUploader, never()).sendFile(any(), any(), any(), any(), any())
        verify(fileTransformer, times(1)).transform(file)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#sendImageInput")
    fun testSendImageWithCallback(fileUploaderResult: Result<UploadedFile>, expected: KClass<*>) = runTest {
        // given
        val fileUploader = mock<FileUploader>()
        whenever(fileUploader.sendImage(any(), any(), any(), any(), any())).doReturn(fileUploaderResult)
        val fileTransformer = spy<NoOpFileTransformer>()
        val sut = Fixture()
            .withFileUploader(fileUploader)
            .withFileTransformer(fileTransformer)
            .get()
        // when
        val userId = randomString()
        val connectionId = randomString()
        val channelType = randomString()
        val channelId = randomString()
        val file = randomFile()
        val callback = mock<ProgressCallback>()
        sut.setConnection(userId = userId, connectionId = connectionId)
        val result = sut.sendImage(channelType, channelId, file, callback).await()
        // then
        result `should be instance of` expected
        verify(fileUploader, times(1)).sendImage(channelType, channelId, userId, file, callback)
        verify(fileUploader, never()).sendImage(any(), any(), any(), any())
        verify(fileTransformer, times(1)).transform(file)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#sendImageInput")
    fun testSendImageWithoutCallback(fileUploaderResult: Result<UploadedFile>, expected: KClass<*>) = runTest {
        // given
        val fileUploader = mock<FileUploader>()
        whenever(fileUploader.sendImage(any(), any(), any(), any())).doReturn(fileUploaderResult)
        val fileTransformer = spy<NoOpFileTransformer>()
        val sut = Fixture()
            .withFileUploader(fileUploader)
            .withFileTransformer(fileTransformer)
            .get()
        // when
        val userId = randomString()
        val connectionId = randomString()
        val channelType = randomString()
        val channelId = randomString()
        val file = randomFile()
        sut.setConnection(userId = userId, connectionId = connectionId)
        val result = sut.sendImage(channelType, channelId, file).await()
        // then
        result `should be instance of` expected
        verify(fileUploader, times(1)).sendImage(channelType, channelId, userId, file)
        verify(fileUploader, never()).sendImage(any(), any(), any(), any(), any())
        verify(fileTransformer, times(1)).transform(file)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#deleteFileInput")
    fun testDeleteFile(fileUploaderResult: Result<Unit>, expected: KClass<*>) = runTest {
        // given
        val fileUploader = mock<FileUploader>()
        whenever(fileUploader.deleteFile(any(), any(), any(), any())).doReturn(fileUploaderResult)
        val sut = Fixture()
            .withFileUploader(fileUploader)
            .get()
        // when
        val userId = randomString()
        val connectionId = randomString()
        val channelType = randomString()
        val channelId = randomString()
        val url = randomString()
        sut.setConnection(userId = userId, connectionId = connectionId)
        val result = sut.deleteFile(channelType, channelId, url).await()
        // then
        result `should be instance of` expected
        verify(fileUploader, times(1)).deleteFile(channelType, channelId, userId, url)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#deleteImageInput")
    fun testDeleteImage(fileUploaderResult: Result<Unit>, expected: KClass<*>) = runTest {
        // given
        val fileUploader = mock<FileUploader>()
        whenever(fileUploader.deleteImage(any(), any(), any(), any())).doReturn(fileUploaderResult)
        val sut = Fixture()
            .withFileUploader(fileUploader)
            .get()
        // when
        val userId = randomString()
        val connectionId = randomString()
        val channelType = randomString()
        val channelId = randomString()
        val url = randomString()
        sut.setConnection(userId = userId, connectionId = connectionId)
        val result = sut.deleteImage(channelType, channelId, url).await()
        // then
        result `should be instance of` expected
        verify(fileUploader, times(1)).deleteImage(channelType, channelId, userId, url)
    }

    @Test
    fun testUploadStandaloneFileSuccessWithProgress() = runTest {
        val file = randomFile()
        val progressCallback = mock<ProgressCallback>()
        val uploadedFile = randomUploadedFile()
        val fileUploader = mock<FileUploader> {
            on { uploadFile(file, progressCallback) } doReturn Result.Success(uploadedFile)
        }
        val fileTransformer = mock<FileTransformer> {
            on { transform(file) } doReturn file
        }
        val sut = Fixture()
            .withFileUploader(fileUploader)
            .withFileTransformer(fileTransformer)
            .get()

        val result = sut.uploadFile(file, progressCallback).await()

        verifySuccess(result, equalsTo = uploadedFile)
        verify(progressCallback).onSuccess(uploadedFile.file)
    }

    @Test
    fun testUploadStandaloneFileErrorWithProgress() = runTest {
        val file = randomFile()
        val progressCallback = mock<ProgressCallback>()
        val error = randomChatNetworkError()
        val fileUploader = mock<FileUploader> {
            on { uploadFile(file, progressCallback) } doReturn Result.Failure(error)
        }
        val fileTransformer = mock<FileTransformer> {
            on { transform(file) } doReturn file
        }
        val sut = Fixture()
            .withFileUploader(fileUploader)
            .withFileTransformer(fileTransformer)
            .get()

        val result = sut.uploadFile(file, progressCallback).await()

        verifyNetworkError(result, statusCode = error.statusCode)
        verify(progressCallback).onError(error)
    }

    @Test
    fun testUploadStandaloneFileSuccessWithoutProgress() = runTest {
        val file = randomFile()
        val uploadedFile = randomUploadedFile()
        val fileUploader = mock<FileUploader> {
            on { uploadFile(file, progressCallback = null) } doReturn Result.Success(uploadedFile)
        }
        val fileTransformer = mock<FileTransformer> {
            on { transform(file) } doReturn file
        }
        val sut = Fixture()
            .withFileUploader(fileUploader)
            .withFileTransformer(fileTransformer)
            .get()

        val result = sut.uploadFile(file, progressCallback = null).await()

        verifySuccess(result, equalsTo = uploadedFile)
    }

    @Test
    fun testDeleteStandaloneFile() = runTest {
        val url = randomString()
        val fileUploader = mock<FileUploader> {
            on { deleteFile(url) } doReturn Result.Success(Unit)
        }
        val sut = Fixture()
            .withFileUploader(fileUploader)
            .get()

        val result = sut.deleteFile(url).await()

        verifySuccess(result, equalsTo = Unit)
    }

    @Test
    fun testUploadStandaloneImageSuccessWithProgress() = runTest {
        val file = randomFile()
        val progressCallback = mock<ProgressCallback>()
        val uploadedFile = randomUploadedFile()
        val fileUploader = mock<FileUploader> {
            on { uploadImage(file, progressCallback) } doReturn Result.Success(uploadedFile)
        }
        val fileTransformer = mock<FileTransformer> {
            on { transform(file) } doReturn file
        }
        val sut = Fixture()
            .withFileUploader(fileUploader)
            .withFileTransformer(fileTransformer)
            .get()

        val result = sut.uploadImage(file, progressCallback).await()

        verifySuccess(result, equalsTo = uploadedFile)
        verify(progressCallback).onSuccess(uploadedFile.file)
    }

    @Test
    fun testUploadStandaloneImageErrorWithProgress() = runTest {
        val file = randomFile()
        val progressCallback = mock<ProgressCallback>()
        val error = randomChatNetworkError()
        val fileUploader = mock<FileUploader> {
            on { uploadImage(file, progressCallback) } doReturn Result.Failure(error)
        }
        val fileTransformer = mock<FileTransformer> {
            on { transform(file) } doReturn file
        }
        val sut = Fixture()
            .withFileUploader(fileUploader)
            .withFileTransformer(fileTransformer)
            .get()

        val result = sut.uploadImage(file, progressCallback).await()

        verifyNetworkError(result, statusCode = error.statusCode)
        verify(progressCallback).onError(error)
    }

    @Test
    fun testUploadStandaloneImageSuccessWithoutProgress() = runTest {
        val file = randomFile()
        val uploadedFile = randomUploadedFile()
        val fileUploader = mock<FileUploader> {
            on { uploadImage(file, progressCallback = null) } doReturn Result.Success(uploadedFile)
        }
        val fileTransformer = mock<FileTransformer> {
            on { transform(file) } doReturn file
        }
        val sut = Fixture()
            .withFileUploader(fileUploader)
            .withFileTransformer(fileTransformer)
            .get()

        val result = sut.uploadImage(file, progressCallback = null).await()

        verifySuccess(result, equalsTo = uploadedFile)
    }

    @Test
    fun testDeleteStandaloneImage() = runTest {
        val url = randomString()
        val fileUploader = mock<FileUploader> {
            on { deleteImage(url) } doReturn Result.Success(Unit)
        }
        val sut = Fixture()
            .withFileUploader(fileUploader)
            .get()

        val result = sut.deleteImage(url).await()

        verifySuccess(result, equalsTo = Unit)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#flagUserInput")
    fun testFlagUser(call: RetrofitCall<FlagResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ModerationApi>()
        whenever(api.flag(any())).doReturn(call)
        val sut = Fixture()
            .withModerationApi(api)
            .get()
        // when
        val userId = randomString()
        val reason = randomString()
        val customData = emptyMap<String, String>()
        val result = sut.flagUser(userId, reason, customData).await()
        // then
        val expectedRequest = FlagUserRequest(
            targetUserId = userId,
            reason = reason,
            custom = customData,
        )
        result `should be instance of` expected
        verify(api, times(1)).flag(expectedRequest)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#flagMessageInput")
    fun testFlagMessage(call: RetrofitCall<FlagResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ModerationApi>()
        whenever(api.flag(any())).doReturn(call)
        val sut = Fixture()
            .withModerationApi(api)
            .get()
        // when
        val messageId = randomString()
        val reason = randomString()
        val customData = emptyMap<String, String>()
        val result = sut.flagMessage(messageId, reason, customData).await()
        // then
        val expectedRequest = FlagMessageRequest(
            targetMessageId = messageId,
            reason = reason,
            custom = customData,
        )
        result `should be instance of` expected
        verify(api, times(1)).flag(expectedRequest)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#unflagUserInput")
    fun testUnflagUser(call: RetrofitCall<FlagResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ModerationApi>()
        whenever(api.unflag(any())).doReturn(call)
        val sut = Fixture()
            .withModerationApi(api)
            .get()
        // when
        val userId = randomString()
        val result = sut.unflagUser(userId).await()
        // then
        val expectedBody = mapOf("target_user_id" to userId)
        result `should be instance of` expected
        verify(api, times(1)).unflag(expectedBody)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#unflagMessageInput")
    fun testUnflagMessage(call: RetrofitCall<FlagResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ModerationApi>()
        whenever(api.unflag(any())).doReturn(call)
        val sut = Fixture()
            .withModerationApi(api)
            .get()
        // when
        val messageId = randomString()
        val result = sut.unflagMessage(messageId).await()
        // then
        val expectedBody = mapOf("target_message_id" to messageId)
        result `should be instance of` expected
        verify(api, times(1)).unflag(expectedBody)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#banUserInput")
    fun testBanUser(call: RetrofitCall<CompletableResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ModerationApi>()
        whenever(api.banUser(any())).doReturn(call)
        val sut = Fixture()
            .withModerationApi(api)
            .get()
        // when
        val targetId = randomString()
        val timeout = randomInt()
        val reason = randomString()
        val channelType = randomString()
        val channelId = randomString()
        val shadow = randomBoolean()
        val result = sut.banUser(targetId, timeout, reason, channelType, channelId, shadow).await()
        // then
        val expectedBody = BanUserRequest(
            target_user_id = targetId,
            timeout = timeout,
            reason = reason,
            type = channelType,
            id = channelId,
            shadow = shadow,
        )
        result `should be instance of` expected
        verify(api, times(1)).banUser(expectedBody)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#unbanUserInput")
    fun testUnbanUser(call: RetrofitCall<CompletableResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ModerationApi>()
        whenever(api.unbanUser(any(), any(), any(), any())).doReturn(call)
        val sut = Fixture()
            .withModerationApi(api)
            .get()
        // when
        val targetId = randomString()
        val channelType = randomString()
        val channelId = randomString()
        val shadow = randomBoolean()
        val result = sut.unbanUser(targetId, channelType, channelId, shadow).await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).unbanUser(targetId, channelType, channelId, shadow)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#queryBannedUsersInput")
    fun testQueryBannedUsers(call: RetrofitCall<QueryBannedUsersResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ModerationApi>()
        whenever(api.queryBannedUsers(any())).doReturn(call)
        val sut = Fixture()
            .withModerationApi(api)
            .get()
        // when
        val filter = Filters.neutral()
        val sort = QuerySortByField.ascByName<BannedUsersSort>("created_at")
        val offset = randomInt()
        val limit = randomInt()
        val createdAtAfter = randomDate()
        val createdAtAfterOrEqual = randomDate()
        val createdAtBefore = randomDate()
        val createdAtBeforeOrEqual = randomDate()
        val result = sut.queryBannedUsers(
            filter = filter,
            sort = sort,
            offset = offset,
            limit = limit,
            createdAtAfter = createdAtAfter,
            createdAtAfterOrEqual = createdAtAfterOrEqual,
            createdAtBefore = createdAtBefore,
            createdAtBeforeOrEqual = createdAtBeforeOrEqual,
        ).await()
        // then
        val expectedPayload = QueryBannedUsersRequest(
            filter_conditions = filter.toMap(),
            sort = sort.toDto(),
            offset = offset,
            limit = limit,
            created_at_after = createdAtAfter,
            created_at_after_or_equal = createdAtAfterOrEqual,
            created_at_before = createdAtBefore,
            created_at_before_or_equal = createdAtBeforeOrEqual,
        )
        result `should be instance of` expected
        verify(api, times(1)).queryBannedUsers(expectedPayload)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#enableSlowModeInput")
    fun testEnableSlowMode(call: RetrofitCall<ChannelResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ChannelApi>()
        whenever(api.updateCooldown(any(), any(), any())).doReturn(call)
        val sut = Fixture()
            .withChannelApi(api)
            .get()
        // when
        val channelType = randomString()
        val channelId = randomString()
        val cooldown = randomInt()
        val result = sut.enableSlowMode(channelType, channelId, cooldown).await()
        // then
        val expectedBody = UpdateCooldownRequest.create(cooldown)
        result `should be instance of` expected
        verify(api, times(1)).updateCooldown(channelType, channelId, expectedBody)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#disableSlowModeInput")
    fun testDisableSlowMode(call: RetrofitCall<ChannelResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ChannelApi>()
        whenever(api.updateCooldown(any(), any(), any())).doReturn(call)
        val sut = Fixture()
            .withChannelApi(api)
            .get()
        // when
        val channelType = randomString()
        val channelId = randomString()
        val result = sut.disableSlowMode(channelType, channelId).await()
        // then
        val expectedBody = UpdateCooldownRequest.create(0)
        result `should be instance of` expected
        verify(api, times(1)).updateCooldown(channelType, channelId, expectedBody)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#stopWatchingInput")
    fun testStopWatching(call: RetrofitCall<CompletableResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ChannelApi>()
        whenever(api.stopWatching(any(), any(), any(), any())).doReturn(call)
        val sut = Fixture()
            .withChannelApi(api)
            .get()
        // when
        val userId = randomString()
        val connectionId = randomString()
        val channelType = randomString()
        val channelId = randomString()
        sut.setConnection(userId = userId, connectionId = connectionId)
        val result = sut.stopWatching(channelType, channelId).await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).stopWatching(channelType, channelId, connectionId, emptyMap())
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#getPinnedMessagesInput")
    fun testGetPinnedMessages(call: RetrofitCall<MessagesResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ChannelApi>()
        whenever(api.getPinnedMessages(any(), any(), any())).doReturn(call)
        val sut = Fixture()
            .withChannelApi(api)
            .get()
        // when
        val channelType = randomString()
        val channelId = randomString()
        val limit = randomInt()
        val sort = QuerySortByField.ascByName<Message>("created_at")
        val pagination = PinnedMessagesPagination.AroundMessage(randomString())
        val result = sut.getPinnedMessages(channelType, channelId, limit, sort, pagination).await()
        // then
        val expectedPayload = PinnedMessagesRequest.create(
            limit = limit,
            sort = sort,
            pagination = pagination,
        )
        result `should be instance of` expected
        verify(api, times(1)).getPinnedMessages(channelType, channelId, expectedPayload)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#updateChannelInput")
    fun testUpdateChannel(call: RetrofitCall<ChannelResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ChannelApi>()
        whenever(api.updateChannel(any(), any(), any())).doReturn(call)
        val sut = Fixture()
            .withChannelApi(api)
            .get()
        // when
        val channelType = randomString()
        val channelId = randomString()
        val extraData = emptyMap<String, Any>()
        val updateMessage = randomMessage()
        val result = sut.updateChannel(channelType, channelId, extraData, updateMessage).await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).updateChannel(eq(channelType), eq(channelId), any())
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#updateChannelPartialInput")
    fun testUpdateChannelPartial(call: RetrofitCall<ChannelResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ChannelApi>()
        whenever(api.updateChannelPartial(any(), any(), any())).doReturn(call)
        val sut = Fixture()
            .withChannelApi(api)
            .get()
        // when
        val channelType = randomString()
        val channelId = randomString()
        val set = emptyMap<String, Any>()
        val unset = emptyList<String>()
        val result = sut.updateChannelPartial(channelType, channelId, set, unset).await()
        // then
        val expectedBody = UpdateChannelPartialRequest(set = set, unset = unset)
        result `should be instance of` expected
        verify(api, times(1)).updateChannelPartial(channelType, channelId, expectedBody)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#showChannelInput")
    fun testShowChannel(call: RetrofitCall<CompletableResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ChannelApi>()
        whenever(api.showChannel(any(), any(), any())).doReturn(call)
        val sut = Fixture()
            .withChannelApi(api)
            .get()
        // when
        val channelType = randomString()
        val channelId = randomString()
        val result = sut.showChannel(channelType, channelId).await()
        // then
        val expectedBody = emptyMap<Any, Any>()
        result `should be instance of` expected
        verify(api, times(1)).showChannel(channelType, channelId, expectedBody)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#hideChannelInput")
    fun testHideChannel(call: RetrofitCall<CompletableResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ChannelApi>()
        whenever(api.hideChannel(any(), any(), any())).doReturn(call)
        val sut = Fixture()
            .withChannelApi(api)
            .get()
        // when
        val channelType = randomString()
        val channelId = randomString()
        val clearHistory = randomBoolean()
        val result = sut.hideChannel(channelType, channelId, clearHistory).await()
        // then
        val expectedBody = HideChannelRequest(clearHistory)
        result `should be instance of` expected
        verify(api, times(1)).hideChannel(channelType, channelId, expectedBody)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#truncateChannelInput")
    fun testTruncateChannel(call: RetrofitCall<ChannelResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ChannelApi>()
        whenever(api.truncateChannel(any(), any(), any())).doReturn(call)
        val sut = Fixture()
            .withChannelApi(api)
            .get()
        // when
        val channelType = randomString()
        val channelId = randomString()
        val systemMessage = randomMessage()
        val result = sut.truncateChannel(channelType, channelId, systemMessage).await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).truncateChannel(eq(channelType), eq(channelId), any())
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#rejectInviteInput")
    fun testRejectInvite(call: RetrofitCall<ChannelResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ChannelApi>()
        whenever(api.rejectInvite(any(), any(), any())).doReturn(call)
        val sut = Fixture()
            .withChannelApi(api)
            .get()
        // when
        val channelType = randomString()
        val channelId = randomString()
        val result = sut.rejectInvite(channelType, channelId).await()
        // then
        val expectedBody = RejectInviteRequest()
        result `should be instance of` expected
        verify(api, times(1)).rejectInvite(channelType, channelId, expectedBody)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#acceptInviteInput")
    fun testAcceptInvite(call: RetrofitCall<ChannelResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ChannelApi>()
        whenever(api.acceptInvite(any(), any(), any())).doReturn(call)
        val sut = Fixture()
            .withChannelApi(api)
            .get()
        // when
        val userId = randomString()
        val connectionId = randomString()
        val channelType = randomString()
        val channelId = randomString()
        val message = randomString()
        sut.setConnection(userId = userId, connectionId = connectionId)
        val result = sut.acceptInvite(channelType, channelId, message).await()
        // then
        val expectedBody = AcceptInviteRequest.create(userId, message)
        result `should be instance of` expected
        verify(api, times(1)).acceptInvite(channelType, channelId, expectedBody)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#deleteChannelInput")
    fun testDeleteChannel(call: RetrofitCall<ChannelResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ChannelApi>()
        whenever(api.deleteChannel(any(), any())).doReturn(call)
        val sut = Fixture()
            .withChannelApi(api)
            .get()
        // when
        val channelType = randomString()
        val channelId = randomString()
        val result = sut.deleteChannel(channelType, channelId).await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).deleteChannel(channelType, channelId)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#markReadInput")
    fun testMarkRead(call: RetrofitCall<CompletableResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ChannelApi>()
        whenever(api.markRead(any(), any(), any())).doReturn(call)
        val sut = Fixture()
            .withChannelApi(api)
            .get()
        // when
        val channelType = randomString()
        val channelId = randomString()
        val messageId = randomString()
        val result = sut.markRead(channelType, channelId, messageId).await()
        // then
        val expectedRequest = MarkReadRequest(message_id = messageId)
        result `should be instance of` expected
        verify(api, times(1)).markRead(channelType, channelId, expectedRequest)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#markThreadReadInput")
    fun testMarkThreadRead(call: RetrofitCall<CompletableResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ChannelApi>()
        whenever(api.markRead(any(), any(), any())).doReturn(call)
        val sut = Fixture()
            .withChannelApi(api)
            .get()
        // when
        val channelType = randomString()
        val channelId = randomString()
        val threadId = randomString()
        val result = sut.markThreadRead(channelType, channelId, threadId).await()
        // then
        val expectedRequest = MarkReadRequest(thread_id = threadId)
        result `should be instance of` expected
        verify(api, times(1)).markRead(channelType, channelId, expectedRequest)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#markUnreadInput")
    fun testMarkUnread(call: RetrofitCall<CompletableResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ChannelApi>()
        whenever(api.markUnread(any(), any(), any())).doReturn(call)
        val sut = Fixture()
            .withChannelApi(api)
            .get()
        // when
        val channelType = randomString()
        val channelId = randomString()
        val messageId = randomString()
        val result = sut.markUnread(channelType, channelId, messageId).await()
        // then
        val expectedRequest = MarkUnreadRequest(message_id = messageId)
        result `should be instance of` expected
        verify(api, times(1)).markUnread(channelType, channelId, expectedRequest)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#markThreadUnreadInput")
    fun testMarkThreadUnread(call: RetrofitCall<CompletableResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ChannelApi>()
        whenever(api.markUnread(any(), any(), any())).doReturn(call)
        val sut = Fixture()
            .withChannelApi(api)
            .get()
        // when
        val channelType = randomString()
        val channelId = randomString()
        val threadId = randomString()
        val messageId = randomString()
        val result = sut.markThreadUnread(channelType, channelId, threadId, messageId).await()
        // then
        val expectedRequest = MarkUnreadRequest(thread_id = threadId, message_id = messageId)
        result `should be instance of` expected
        verify(api, times(1)).markUnread(channelType, channelId, expectedRequest)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#markAllReadInput")
    fun testMarkAllRead(call: RetrofitCall<CompletableResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ChannelApi>()
        whenever(api.markAllRead()).doReturn(call)
        val sut = Fixture()
            .withChannelApi(api)
            .get()
        // when
        val result = sut.markAllRead().await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).markAllRead()
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#addMembersInput")
    fun testAddMembers(call: RetrofitCall<ChannelResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ChannelApi>()
        whenever(api.addMembers(any(), any(), any())).doReturn(call)
        val sut = Fixture()
            .withChannelApi(api)
            .get()
        // when
        val channelType = randomString()
        val channelId = randomString()
        val members = listOf(randomMemberData())
        val systemMessage = randomMessage()
        val hideHistory = randomBoolean()
        val skipPush = randomBoolean()
        val result = sut.addMembers(channelType, channelId, members, systemMessage, hideHistory, skipPush).await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).addMembers(eq(channelType), eq(channelId), any())
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#removeMembersInput")
    fun testRemoveMembers(call: RetrofitCall<ChannelResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ChannelApi>()
        whenever(api.removeMembers(any(), any(), any())).doReturn(call)
        val sut = Fixture()
            .withChannelApi(api)
            .get()
        // when
        val channelType = randomString()
        val channelId = randomString()
        val members = listOf(randomString())
        val systemMessage = randomMessage()
        val skipPush = randomBoolean()
        val result = sut.removeMembers(channelType, channelId, members, systemMessage, skipPush).await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).removeMembers(eq(channelType), eq(channelId), any())
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#inviteMembersInput")
    fun testInviteMembers(call: RetrofitCall<ChannelResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ChannelApi>()
        whenever(api.inviteMembers(any(), any(), any())).doReturn(call)
        val sut = Fixture()
            .withChannelApi(api)
            .get()
        // when
        val channelType = randomString()
        val channelId = randomString()
        val members = listOf(randomString())
        val systemMessage = randomMessage()
        val skipPush = randomBoolean()
        val result = sut.inviteMembers(channelType, channelId, members, systemMessage, skipPush).await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).inviteMembers(eq(channelType), eq(channelId), any())
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#partialUpdateMemberInput")
    fun testPartialUpdateMember(call: RetrofitCall<UpdateMemberPartialResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ChannelApi>()
        whenever(api.partialUpdateMember(any(), any(), any(), any())).doReturn(call)
        val sut = Fixture()
            .withChannelApi(api)
            .get()
        // when
        val channelType = randomString()
        val channelId = randomString()
        val userId = randomString()
        val set = emptyMap<String, Any>()
        val unset = emptyList<String>()
        val result = sut.partialUpdateMember(channelType, channelId, userId, set, unset).await()
        // then
        val expectedBody = UpdateMemberPartialRequest(set = set, unset = unset)
        result `should be instance of` expected
        verify(api, times(1)).partialUpdateMember(channelType, channelId, userId, expectedBody)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#getNewerRepliesInput")
    fun testGetNewerReplies(call: RetrofitCall<MessagesResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<MessageApi>()
        whenever(api.getNewerReplies(any(), any(), any())).doReturn(call)
        val sut = Fixture()
            .withMessageApi(api)
            .get()
        // when
        val parentId = randomString()
        val limit = randomInt()
        val lastId = randomString()
        val result = sut.getNewerReplies(parentId, limit, lastId).await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).getNewerReplies(parentId, limit, lastId)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#getRepliesInput")
    fun testGetReplies(call: RetrofitCall<MessagesResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<MessageApi>()
        whenever(api.getReplies(any(), any())).doReturn(call)
        val sut = Fixture()
            .withMessageApi(api)
            .get()
        // when
        val parentId = randomString()
        val limit = randomInt()
        val result = sut.getReplies(parentId, limit).await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).getReplies(parentId, limit)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#getRepliesMoreInput")
    fun testGetRepliesMore(call: RetrofitCall<MessagesResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<MessageApi>()
        whenever(api.getRepliesMore(any(), any(), any())).doReturn(call)
        val sut = Fixture()
            .withMessageApi(api)
            .get()
        // when
        val parentId = randomString()
        val limit = randomInt()
        val firstId = randomString()
        val result = sut.getRepliesMore(parentId, firstId, limit).await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).getRepliesMore(parentId, limit, firstId)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#sendActionInput")
    fun testSendAction(call: RetrofitCall<MessageResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<MessageApi>()
        whenever(api.sendAction(any(), any())).doReturn(call)
        val sut = Fixture()
            .withMessageApi(api)
            .get()
        // when
        val request = Mother.randomSendActionRequest()
        val result = sut.sendAction(request).await()
        // then
        val expectedRequest = SendActionRequest(
            channel_id = request.channelId,
            message_id = request.messageId,
            type = request.type,
            form_data = request.formData,
        )
        result `should be instance of` expected
        verify(api, times(1)).sendAction(request.messageId, expectedRequest)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#updateUsersInput")
    fun testUpdateUsers(call: RetrofitCall<UpdateUsersResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<UserApi>()
        whenever(api.updateUsers(any(), any())).doReturn(call)
        val sut = Fixture()
            .withUserApi(api)
            .get()
        // when
        val userUd = randomString()
        val connectionId = randomString()
        val users = listOf(randomUser())
        sut.setConnection(userId = userUd, connectionId = connectionId)
        val result = sut.updateUsers(users).await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).updateUsers(eq(connectionId), any())
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#blockUserInput")
    fun testBlockUser(call: RetrofitCall<BlockUserResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<UserApi>()
        whenever(api.blockUser(any())).doReturn(call)
        val sut = Fixture()
            .withUserApi(api)
            .get()
        // when
        val targetId = randomString()
        val result = sut.blockUser(targetId).await()
        // then
        val expectedBody = BlockUserRequest(targetId)
        result `should be instance of` expected
        verify(api, times(1)).blockUser(expectedBody)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#unblockUserInput")
    fun testUnblockUser(call: RetrofitCall<UnblockUserResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<UserApi>()
        whenever(api.unblockUser(any())).doReturn(call)
        val sut = Fixture()
            .withUserApi(api)
            .get()
        // when
        val targetId = randomString()
        val result = sut.unblockUser(targetId).await()
        // then
        val expectedBody = UnblockUserRequest(targetId)
        result `should be instance of` expected
        verify(api, times(1)).unblockUser(expectedBody)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#queryBlockedUsersInput")
    fun testQueryBlockedUsers(call: RetrofitCall<QueryBlockedUsersResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<UserApi>()
        whenever(api.queryBlockedUsers()).doReturn(call)
        val sut = Fixture()
            .withUserApi(api)
            .get()
        // when
        val result = sut.queryBlockedUsers().await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).queryBlockedUsers()
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#partialUpdateUserInput")
    fun testPartialUpdateUser(call: RetrofitCall<UpdateUsersResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<UserApi>()
        whenever(api.partialUpdateUsers(any(), any())).doReturn(call)
        val sut = Fixture()
            .withUserApi(api)
            .get()
        // when
        val userId = randomString()
        val connectionId = randomString()
        val targetUserId = randomString()
        val set = emptyMap<String, Any>()
        val unset = emptyList<String>()
        sut.setConnection(userId = userId, connectionId = connectionId)
        val result = sut.partialUpdateUser(targetUserId, set, unset).await()
        // then
        val expectedBody = PartialUpdateUsersRequest(
            users = listOf(PartialUpdateUserDto(targetUserId, set, unset)),
        )
        result `should be instance of` expected
        verify(api, times(1)).partialUpdateUsers(connectionId, expectedBody)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#getGuestUserInput")
    fun testGetGuestUser(call: RetrofitCall<TokenResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<GuestApi>()
        whenever(api.getGuestUser(any())).doReturn(call)
        val sut = Fixture()
            .withGuestApi(api)
            .get()
        // when
        val userId = randomString()
        val userName = randomString()
        val result = sut.getGuestUser(userId, userName).await()
        // then
        val expectedBody = GuestUserRequest.create(userId, userName)
        result `should be instance of` expected
        verify(api, times(1)).getGuestUser(expectedBody)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#translateInput")
    fun testTranslate(call: RetrofitCall<MessageResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<MessageApi>()
        whenever(api.translate(any(), any())).doReturn(call)
        val sut = Fixture()
            .withMessageApi(api)
            .get()
        // when
        val messageId = randomString()
        val language = randomString()
        val result = sut.translate(messageId, language).await()
        // then
        val expectedBody = TranslateMessageRequest(language)
        result `should be instance of` expected
        verify(api, times(1)).translate(messageId, expectedBody)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#ogInput")
    fun testOg(call: RetrofitCall<AttachmentDto>, expected: KClass<*>) = runTest {
        // given
        val api = mock<OpenGraphApi>()
        whenever(api.get(any())).doReturn(call)
        val sut = Fixture()
            .withOgApi(api)
            .get()
        // when
        val url = randomString()
        val result = sut.og(url).await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).get(url)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#searchMessagesInput")
    fun testSearchMessagesWithRequest(call: RetrofitCall<SearchMessagesResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<GeneralApi>()
        whenever(api.searchMessages(any())).doReturn(call)
        val sut = Fixture()
            .withGeneralApi(api)
            .get()
        // when
        val request = Mother.randomSearchMessagesRequest()
        val result = sut.searchMessages(request).await()
        // then
        val expectedPayload = io.getstream.chat.android.client.api2.model.requests.SearchMessagesRequest(
            filter_conditions = request.channelFilter.toMap(),
            message_filter_conditions = request.messageFilter.toMap(),
            offset = request.offset,
            limit = request.limit,
            next = request.next,
            sort = request.sort,
        )
        result `should be instance of` expected
        verify(api, times(1)).searchMessages(expectedPayload)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#searchMessagesInput")
    fun testSearchMessages(call: RetrofitCall<SearchMessagesResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<GeneralApi>()
        whenever(api.searchMessages(any())).doReturn(call)
        val sut = Fixture()
            .withGeneralApi(api)
            .get()
        // when
        val channelFilter = Filters.neutral()
        val messageFilter = Filters.neutral()
        val sort = QuerySortByField.ascByName<Message>("created_at")
        val offset = randomInt()
        val limit = randomInt()
        val next = randomString()
        val result = sut.searchMessages(channelFilter, messageFilter, offset, limit, next, sort).await()
        // then
        val expectedPayload = io.getstream.chat.android.client.api2.model.requests.SearchMessagesRequest(
            filter_conditions = channelFilter.toMap(),
            message_filter_conditions = messageFilter.toMap(),
            offset = offset,
            limit = limit,
            next = next,
            sort = sort.toDto(),
        )
        result `should be instance of` expected
        verify(api, times(1)).searchMessages(expectedPayload)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#queryChannelsInput")
    fun testQueryChannels(call: RetrofitCall<QueryChannelsResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ChannelApi>()
        whenever(api.queryChannels(any(), any())).doReturn(call)
        val sut = Fixture()
            .withChannelApi(api)
            .get()
        // when
        val userId = randomString()
        val connectionId = randomString()
        val query = Mother.randomQueryChannelsRequest()
        sut.setConnection(userId = userId, connectionId = connectionId)
        val result = sut.queryChannels(query).await()
        // then
        val expectedPayload = io.getstream.chat.android.client.api2.model.requests.QueryChannelsRequest(
            filter_conditions = query.filter.toMap(),
            sort = query.sort,
            offset = query.offset,
            limit = query.limit,
            message_limit = query.messageLimit,
            member_limit = query.memberLimit,
            state = query.state,
            watch = query.watch,
            presence = query.presence,
        )
        result `should be instance of` expected
        verify(api, times(1)).queryChannels(connectionId, expectedPayload)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#queryChannelInput")
    fun testQueryChannelWithoutChannelId(call: RetrofitCall<ChannelResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ChannelApi>()
        whenever(api.queryChannel(any(), any(), any())).doReturn(call)
        val sut = Fixture()
            .withChannelApi(api)
            .get()
        // when
        val userId = randomString()
        val connectionId = randomString()
        val channelType = randomString()
        val channelId = ""
        val query = Mother.randomQueryChannelRequest()
        sut.setConnection(userId = userId, connectionId = connectionId)
        val result = sut.queryChannel(channelType, channelId, query).await()
        // then
        val expectedPayload = io.getstream.chat.android.client.api2.model.requests.QueryChannelRequest(
            data = query.data,
            messages = query.messages,
            watchers = query.watchers,
            members = query.members,
            state = query.state,
            watch = query.watch,
            presence = query.presence,
        )
        result `should be instance of` expected
        verify(api, times(1)).queryChannel(channelType, connectionId, expectedPayload)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#queryChannelInput")
    fun testQueryChannelWithChannelId(call: RetrofitCall<ChannelResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ChannelApi>()
        whenever(api.queryChannel(any(), any(), any(), any())).doReturn(call)
        val sut = Fixture()
            .withChannelApi(api)
            .get()
        // when
        val userId = randomString()
        val connectionId = randomString()
        val channelType = randomString()
        val channelId = randomString()
        val query = Mother.randomQueryChannelRequest()
        sut.setConnection(userId = userId, connectionId = connectionId)
        val result = sut.queryChannel(channelType, channelId, query).await()
        // then
        val expectedPayload = io.getstream.chat.android.client.api2.model.requests.QueryChannelRequest(
            data = query.data,
            messages = query.messages,
            watchers = query.watchers,
            members = query.members,
            state = query.state,
            watch = query.watch,
            presence = query.presence,
        )
        result `should be instance of` expected
        verify(api, times(1)).queryChannel(channelType, channelId, connectionId, expectedPayload)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#queryUsersInput")
    fun testQueryUsers(call: RetrofitCall<UsersResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<UserApi>()
        whenever(api.queryUsers(any(), any())).doReturn(call)
        val sut = Fixture()
            .withUserApi(api)
            .get()
        // when
        val userId = randomString()
        val connectionId = randomString()
        val query = Mother.randomQueryUsersRequest()
        sut.setConnection(userId = userId, connectionId = connectionId)
        val result = sut.queryUsers(query).await()
        // then
        val expectedPayload = io.getstream.chat.android.client.api2.model.requests.QueryUsersRequest(
            filter_conditions = query.filter.toMap(),
            sort = query.sort,
            offset = query.offset,
            limit = query.limit,
            presence = query.presence,
        )
        result `should be instance of` expected
        verify(api, times(1)).queryUsers(connectionId, expectedPayload)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#queryMembersInput")
    fun testQueryMembers(call: RetrofitCall<QueryMembersResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<GeneralApi>()
        whenever(api.queryMembers(any())).doReturn(call)
        val sut = Fixture()
            .withGeneralApi(api)
            .get()
        // when
        val channelType = randomString()
        val channelId = randomString()
        val offset = randomInt()
        val limit = randomInt()
        val filter = Filters.neutral()
        val sort = QuerySortByField.ascByName<Member>("created_at")
        val members = listOf(randomMember())
        val result = sut.queryMembers(channelType, channelId, offset, limit, filter, sort, members).await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).queryMembers(any())
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#sendEventInput")
    fun testSendEvent(call: RetrofitCall<EventResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ChannelApi>()
        whenever(api.sendEvent(any(), any(), any())).doReturn(call)
        val sut = Fixture()
            .withChannelApi(api)
            .get()
        // when
        val eventType = randomString()
        val channelType = randomString()
        val channelId = randomString()
        val extraData = emptyMap<Any, Any>()
        val result = sut.sendEvent(eventType, channelType, channelId, extraData).await()
        // then
        val expectedRequest = SendEventRequest(
            event = extraData + mapOf("type" to eventType),
        )
        result `should be instance of` expected
        verify(api, times(1)).sendEvent(channelType, channelId, expectedRequest)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#getSyncHistoryInput")
    fun testGetSyncHistory(call: RetrofitCall<SyncHistoryResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<GeneralApi>()
        whenever(api.getSyncHistory(any(), any())).doReturn(call)
        val sut = Fixture()
            .withGeneralApi(api)
            .get()
        // when
        val userId = randomString()
        val connectionId = randomString()
        val channelIds = listOf(randomString())
        val lastSyncAt = randomString()
        sut.setConnection(userId = userId, connectionId = connectionId)
        val result = sut.getSyncHistory(channelIds, lastSyncAt).await()
        // then
        val expectedPayload =
            io.getstream.chat.android.client.api2.model.requests.SyncHistoryRequest(channelIds, lastSyncAt)
        result `should be instance of` expected
        verify(api, times(1)).getSyncHistory(expectedPayload, connectionId)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#downloadFileInput")
    fun testDownloadFile(call: RetrofitCall<ResponseBody>, expected: KClass<*>) = runTest {
        // given
        val api = mock<FileDownloadApi>()
        whenever(api.downloadFile(any())).doReturn(call)
        val sut = Fixture()
            .withFileDownloadApi(api)
            .get()
        // when
        val url = randomString()
        val result = sut.downloadFile(url).await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).downloadFile(url)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#queryThreadsInput")
    fun testQueryThreads(call: RetrofitCall<QueryThreadsResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ThreadsApi>()
        whenever(api.queryThreads(any(), any())).doReturn(call)
        val sut = Fixture()
            .withThreadsApi(api)
            .get()
        // when
        val userId = randomString()
        val connectionId = randomString()
        sut.setConnection(userId = userId, connectionId = connectionId)
        val request = Mother.randomQueryThreadsRequest()
        val result = sut.queryThreads(request).await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).queryThreads(eq(connectionId), any())
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#getThreadInput")
    fun testGetThread(call: RetrofitCall<ThreadResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ThreadsApi>()
        whenever(api.getThread(any(), any(), any())).doReturn(call)
        val sut = Fixture()
            .withThreadsApi(api)
            .get()
        // when
        val userId = randomString()
        val connectionId = randomString()
        val messageId = randomString()
        val options = Mother.randomGetThreadOptions()
        sut.setConnection(userId = userId, connectionId = connectionId)
        val result = sut.getThread(messageId, options).await()
        // then
        val expectedOptions = options.toMap()
        result `should be instance of` expected
        verify(api, times(1)).getThread(messageId, connectionId, expectedOptions)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#partialUpdateThreadInput")
    fun testPartialUpdateThread(call: RetrofitCall<ThreadResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<ThreadsApi>()
        whenever(api.partialUpdateThread(any(), any())).doReturn(call)
        val sut = Fixture()
            .withThreadsApi(api)
            .get()
        // when
        val messageId = randomString()
        val set = emptyMap<String, Any>()
        val unset = emptyList<String>()
        val result = sut.partialUpdateThread(messageId, set, unset).await()
        // then
        val expectedBody = PartialUpdateThreadRequest(set, unset)
        result `should be instance of` expected
        verify(api, times(1)).partialUpdateThread(messageId, expectedBody)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#castPollVoteInput")
    fun testCastPollVote(call: RetrofitCall<PollVoteResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<PollsApi>()
        whenever(api.castPollVote(any(), any(), any())).doReturn(call)
        val sut = Fixture()
            .withPollsApi(api)
            .get()
        // when
        val messageId = randomString()
        val pollId = randomString()
        val optionId = randomString()
        val result = sut.castPollVote(messageId, pollId, optionId).await()
        // then
        val expectedVote = PollVoteRequest(UpstreamVoteDto(option_id = optionId))
        result `should be instance of` expected
        verify(api, times(1)).castPollVote(messageId, pollId, expectedVote)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#castPollAnswerInput")
    fun testCastPollAnswer(call: RetrofitCall<PollVoteResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<PollsApi>()
        whenever(api.castPollVote(any(), any(), any())).doReturn(call)
        val sut = Fixture()
            .withPollsApi(api)
            .get()
        // when
        val messageId = randomString()
        val pollId = randomString()
        val answer = randomString()
        val result = sut.castPollAnswer(messageId, pollId, answer).await()
        // then
        val expectedAnswer = PollVoteRequest(UpstreamVoteDto(answer_text = answer))
        result `should be instance of` expected
        verify(api, times(1)).castPollVote(messageId, pollId, expectedAnswer)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#removePollVoteInput")
    fun testRemovePollVote(call: RetrofitCall<PollVoteResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<PollsApi>()
        whenever(api.removePollVote(any(), any(), any())).doReturn(call)
        val sut = Fixture()
            .withPollsApi(api)
            .get()
        // when
        val messageId = randomString()
        val pollId = randomString()
        val voteId = randomString()
        val result = sut.removePollVote(messageId, pollId, voteId).await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).removePollVote(messageId, pollId, voteId)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#closePollInput")
    fun testClosePoll(call: RetrofitCall<PollResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<PollsApi>()
        whenever(api.updatePoll(any(), any())).doReturn(call)
        val sut = Fixture()
            .withPollsApi(api)
            .get()
        // when
        val pollId = randomString()
        val result = sut.closePoll(pollId).await()
        // then
        val expectedRequest = PollUpdateRequest(set = mapOf("is_closed" to true))
        result `should be instance of` expected
        verify(api, times(1)).updatePoll(pollId, expectedRequest)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#suggestPollOptionInput")
    fun testSuggestPollOption(call: RetrofitCall<SuggestPollOptionResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<PollsApi>()
        whenever(api.suggestPollOption(any(), any())).doReturn(call)
        val sut = Fixture()
            .withPollsApi(api)
            .get()
        // when
        val pollId = randomString()
        val option = randomString()
        val result = sut.suggestPollOption(pollId, option).await()
        // then
        val expectedOption = SuggestPollOptionRequest(option)
        result `should be instance of` expected
        verify(api, times(1)).suggestPollOption(pollId, expectedOption)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#createPollInput")
    fun testCreatePoll(call: RetrofitCall<PollResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<PollsApi>()
        whenever(api.createPoll(any())).doReturn(call)
        val sut = Fixture()
            .withPollsApi(api)
            .get()
        // when
        val pollConfig = randomPollConfig()
        val result = sut.createPoll(pollConfig).await()
        // then
        val expectedBody = PollRequest(
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
        )
        result `should be instance of` expected
        verify(api, times(1)).createPoll(expectedBody)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#deletePollInput")
    fun testDeletePoll(call: RetrofitCall<CompletableResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<PollsApi>()
        whenever(api.deletePoll(any())).doReturn(call)
        val sut = Fixture()
            .withPollsApi(api)
            .get()
        // when
        val pollId = randomString()
        val result = sut.deletePoll(pollId).await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).deletePoll(pollId)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#createReminderInput")
    fun testCreateReminder(call: RetrofitCall<ReminderResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<RemindersApi>()
        whenever(api.createReminder(any(), any())).doReturn(call)
        val sut = Fixture()
            .withRemindersApi(api)
            .get()
        // when
        val messageId = randomString()
        val remindAt = randomDate()
        val result = sut.createReminder(messageId, remindAt).await()
        // then
        val expectedBody = ReminderRequest(remind_at = remindAt)
        result `should be instance of` expected
        verify(api, times(1)).createReminder(messageId, expectedBody)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#updateReminderInput")
    fun testUpdateReminder(call: RetrofitCall<ReminderResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<RemindersApi>()
        whenever(api.updateReminder(any(), any())).doReturn(call)
        val sut = Fixture()
            .withRemindersApi(api)
            .get()
        // when
        val messageId = randomString()
        val remindAt = randomDate()
        val result = sut.updateReminder(messageId, remindAt).await()
        // then
        val expectedBody = ReminderRequest(remind_at = remindAt)
        result `should be instance of` expected
        verify(api, times(1)).updateReminder(messageId, expectedBody)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#deleteReminderInput")
    fun testDeleteReminder(call: RetrofitCall<CompletableResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<RemindersApi>()
        whenever(api.deleteReminder(any())).doReturn(call)
        val sut = Fixture()
            .withRemindersApi(api)
            .get()
        // when
        val messageId = randomString()
        val result = sut.deleteReminder(messageId).await()
        // then
        result `should be instance of` expected
        verify(api, times(1)).deleteReminder(messageId)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#queryRemindersInput")
    fun testQueryReminders(call: RetrofitCall<QueryRemindersResponse>, expected: KClass<*>) = runTest {
        // given
        val api = mock<RemindersApi>()
        whenever(api.queryReminders(any())).doReturn(call)
        val sut = Fixture()
            .withRemindersApi(api)
            .get()
        // when
        val filter = Filters.neutral()
        val limit = positiveRandomInt()
        val next = randomString()
        val sort = QuerySortByField<MessageReminder>()
        val result = sut.queryReminders(filter, limit, next, sort).await()
        // then
        val expectedBody = QueryRemindersRequest(
            filter = filter.toMap(),
            limit = limit,
            next = next,
            sort = sort.toDto(),
        )
        result `should be instance of` expected
        verify(api, times(1)).queryReminders(expectedBody)
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#updateLiveLocation")
    fun testUpdateLiveLocation(
        location: Location,
        request: UpdateLiveLocationRequest,
        response: DownstreamLocationDto,
    ) = runTest {
        val api = mock<UserApi>()
        whenever(api.updateLiveLocation(request)) doReturn RetroSuccess(response).toRetrofitCall()
        val sut = Fixture()
            .withUserApi(api)
            .get()

        val result = sut.updateLiveLocation(location).await()

        assertTrue(result is Result.Success)
        assertEquals(location, result.getOrThrow())
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#stopLiveLocation")
    fun testStopLiveLocation(
        location: Location,
        request: UpdateLiveLocationRequest,
        response: DownstreamLocationDto,
    ) = runTest {
        val api = mock<UserApi>()
        whenever(api.updateLiveLocation(request)) doReturn RetroSuccess(response).toRetrofitCall()
        val sut = Fixture()
            .withUserApi(api)
            .get()

        val result = sut.stopLiveLocation(location).await()

        assertTrue(result is Result.Success)
        assertEquals(location, result.getOrThrow())
    }

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.MoshiChatApiTestArguments#getUnreadCounts")
    fun testGetUnreadCounts(
        model: UnreadCounts,
        dto: UnreadDto,
    ) = runTest {
        val api = mock<GeneralApi> {
            on { getUnreadCounts() } doReturn RetroSuccess(dto).toRetrofitCall()
        }
        val sut = Fixture()
            .withGeneralApi(api)
            .get()

        val result = sut.getUnreadCounts().await()

        assertTrue(result is Result.Success)
        assertEquals(model, result.getOrThrow())
    }

    @Test
    fun testWarmUp() = runTest {
        // given
        val api = mock<GeneralApi>()
        whenever(api.warmUp()).doReturn(RetroSuccess(randomString().toResponseBody()).toRetrofitCall())
        val sut = Fixture()
            .withGeneralApi(api)
            .get()
        // when
        sut.warmUp()
        // then
        verify(api, times(1)).warmUp()
    }

    private class Fixture {

        private var domainMapping = DomainMapping(
            currentUserIdProvider = { "" },
            channelTransformer = NoOpChannelTransformer,
            messageTransformer = NoOpMessageTransformer,
            userTransformer = NoOpUserTransformer,
        )
        private var eventMapping = EventMapping(domainMapping)
        private var dtoMapping = DtoMapping(
            messageTransformer = NoOpMessageTransformer,
            userTransformer = NoOpUserTransformer,
        )
        private var userApi: UserApi = mock()
        private var guestApi: GuestApi = mock()
        private var messageApi: MessageApi = mock()
        private var channelApi: ChannelApi = mock()
        private var deviceApi: DeviceApi = mock()
        private var moderationApi: ModerationApi = mock()
        private var generalApi: GeneralApi = mock()
        private var configApi: ConfigApi = mock()
        private var callApi: VideoCallApi = mock()
        private var fileDownloadApi: FileDownloadApi = mock()
        private var ogApi: OpenGraphApi = mock()
        private var threadsApi: ThreadsApi = mock()
        private var pollsApi: PollsApi = mock()
        private var remindersApi: RemindersApi = mock()

        private var fileUploader: FileUploader = mock()
        private var fileTransformer: FileTransformer = NoOpFileTransformer

        fun withUserApi(userApi: UserApi) = apply {
            this.userApi = userApi
        }

        fun withGuestApi(guestApi: GuestApi) = apply {
            this.guestApi = guestApi
        }

        fun withMessageApi(messageApi: MessageApi) = apply {
            this.messageApi = messageApi
        }

        fun withChannelApi(channelApi: ChannelApi) = apply {
            this.channelApi = channelApi
        }

        fun withDeviceApi(deviceApi: DeviceApi) = apply {
            this.deviceApi = deviceApi
        }

        fun withModerationApi(moderationApi: ModerationApi) = apply {
            this.moderationApi = moderationApi
        }

        fun withGeneralApi(generalApi: GeneralApi) = apply {
            this.generalApi = generalApi
        }

        fun withConfigApi(configApi: ConfigApi) = apply {
            this.configApi = configApi
        }

        fun withFileDownloadApi(fileDownloadApi: FileDownloadApi) = apply {
            this.fileDownloadApi = fileDownloadApi
        }

        fun withOgApi(ogApi: OpenGraphApi) = apply {
            this.ogApi = ogApi
        }

        fun withThreadsApi(threadsApi: ThreadsApi) = apply {
            this.threadsApi = threadsApi
        }

        fun withPollsApi(pollsApi: PollsApi) = apply {
            this.pollsApi = pollsApi
        }

        fun withRemindersApi(remindersApi: RemindersApi) = apply {
            this.remindersApi = remindersApi
        }

        fun withFileUploader(fileUploader: FileUploader) = apply {
            this.fileUploader = fileUploader
        }

        fun withFileTransformer(fileTransformer: FileTransformer) = apply {
            this.fileTransformer = fileTransformer
        }

        fun get(): MoshiChatApi {
            return MoshiChatApi(
                domainMapping = domainMapping,
                eventMapping = eventMapping,
                dtoMapping = dtoMapping,
                fileUploader = fileUploader,
                fileTransformer = fileTransformer,
                userApi = userApi,
                guestApi = guestApi,
                messageApi = messageApi,
                channelApi = channelApi,
                deviceApi = deviceApi,
                moderationApi = moderationApi,
                generalApi = generalApi,
                configApi = configApi,
                callApi = callApi,
                fileDownloadApi = fileDownloadApi,
                ogApi = ogApi,
                threadsApi = threadsApi,
                pollsApi = pollsApi,
                remindersApi = remindersApi,
                userScope = UserScope(ClientScope()),
                coroutineScope = testCoroutineExtension.scope,
            )
        }
    }

    companion object {

        @JvmField
        @RegisterExtension
        val testCoroutineExtension = TestCoroutineExtension()
    }
}
