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
import io.getstream.chat.android.client.api.RetrofitCdnApi
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
import io.getstream.chat.android.client.api2.endpoint.ThreadsApi
import io.getstream.chat.android.client.api2.endpoint.UserApi
import io.getstream.chat.android.client.api2.endpoint.VideoCallApi
import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.api2.mapping.DtoMapping
import io.getstream.chat.android.client.api2.mapping.EventMapping
import io.getstream.chat.android.client.api2.model.dto.AttachmentDto
import io.getstream.chat.android.client.api2.model.dto.PartialUpdateUserDto
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
import io.getstream.chat.android.client.api2.model.requests.PartialUpdateUsersRequest
import io.getstream.chat.android.client.api2.model.requests.PinnedMessagesRequest
import io.getstream.chat.android.client.api2.model.requests.QueryBannedUsersRequest
import io.getstream.chat.android.client.api2.model.requests.RejectInviteRequest
import io.getstream.chat.android.client.api2.model.requests.SendActionRequest
import io.getstream.chat.android.client.api2.model.requests.UnblockUserRequest
import io.getstream.chat.android.client.api2.model.requests.UpdateChannelPartialRequest
import io.getstream.chat.android.client.api2.model.requests.UpdateCooldownRequest
import io.getstream.chat.android.client.api2.model.requests.UpdateMemberPartialRequest
import io.getstream.chat.android.client.api2.model.requests.UpdateMemberPartialResponse
import io.getstream.chat.android.client.api2.model.response.AppSettingsResponse
import io.getstream.chat.android.client.api2.model.response.BlockUserResponse
import io.getstream.chat.android.client.api2.model.response.ChannelResponse
import io.getstream.chat.android.client.api2.model.response.CompletableResponse
import io.getstream.chat.android.client.api2.model.response.DevicesResponse
import io.getstream.chat.android.client.api2.model.response.FlagResponse
import io.getstream.chat.android.client.api2.model.response.MessageResponse
import io.getstream.chat.android.client.api2.model.response.MessagesResponse
import io.getstream.chat.android.client.api2.model.response.MuteUserResponse
import io.getstream.chat.android.client.api2.model.response.QueryBannedUsersResponse
import io.getstream.chat.android.client.api2.model.response.QueryBlockedUsersResponse
import io.getstream.chat.android.client.api2.model.response.ReactionResponse
import io.getstream.chat.android.client.api2.model.response.ReactionsResponse
import io.getstream.chat.android.client.api2.model.response.TokenResponse
import io.getstream.chat.android.client.api2.model.response.TranslateMessageRequest
import io.getstream.chat.android.client.api2.model.response.UnblockUserResponse
import io.getstream.chat.android.client.api2.model.response.UpdateUsersResponse
import io.getstream.chat.android.client.call.RetrofitCall
import io.getstream.chat.android.client.parser.toMap
import io.getstream.chat.android.client.scope.ClientScope
import io.getstream.chat.android.client.scope.UserScope
import io.getstream.chat.android.client.uploader.FileTransformer
import io.getstream.chat.android.client.uploader.FileUploader
import io.getstream.chat.android.client.uploader.NoOpFileTransformer
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.models.BannedUsersSort
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.UploadedFile
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomDateOrNull
import io.getstream.chat.android.randomDevice
import io.getstream.chat.android.randomFile
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomMemberData
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomReaction
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be instance of`
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
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
    @MethodSource("appSettingsInput")
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
    @MethodSource("sendMessageInput")
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
    @MethodSource("updateMessageInput")
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
    @MethodSource("partialUpdateMessageInput")
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
    @MethodSource("getMessageInput")
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
    @MethodSource("deleteMessageInput")
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
    @MethodSource("getReactionsInput")
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
    @MethodSource("sendReactionInput")
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
    @MethodSource("deleteReactionInput")
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
    @MethodSource("addDeviceInput")
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
    @MethodSource("deleteDeviceInput")
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
    @MethodSource("getDevicesInput")
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
    @MethodSource("muteCurrentUserInput")
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
    @MethodSource("muteUserInput")
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
    @MethodSource("unmuteCurrentUserInput")
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
    @MethodSource("unmuteUserInput")
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
    @MethodSource("muteChannelInput")
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
    @MethodSource("unmuteChannelInput")
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
    @MethodSource("sendFileInput")
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
    @MethodSource("sendFileInput")
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
    @MethodSource("sendImageInput")
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
    @MethodSource("sendImageInput")
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
    @MethodSource("deleteFileInput")
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
    @MethodSource("deleteImageInput")
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

    @ParameterizedTest
    @MethodSource("flagUserInput")
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
    @MethodSource("flagMessageInput")
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
    @MethodSource("unflagUserInput")
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
    @MethodSource("unflagMessageInput")
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
    @MethodSource("banUserInput")
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
    @MethodSource("unbanUserInput")
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
    @MethodSource("queryBannedUsersInput")
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
    @MethodSource("enableSlowModeInput")
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
    @MethodSource("disableSlowModeInput")
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
    @MethodSource("stopWatchingInput")
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
    @MethodSource("getPinnedMessagesInput")
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
    @MethodSource("updateChannelInput")
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
    @MethodSource("updateChannelPartialInput")
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
    @MethodSource("showChannelInput")
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
    @MethodSource("hideChannelInput")
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
    @MethodSource("truncateChannelInput")
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
    @MethodSource("rejectInviteInput")
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
    @MethodSource("acceptInviteInput")
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
    @MethodSource("deleteChannelInput")
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
    @MethodSource("markReadInput")
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
    @MethodSource("markThreadReadInput")
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
    @MethodSource("markUnreadInput")
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
    @MethodSource("markThreadUnreadInput")
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
    @MethodSource("markAllReadInput")
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
    @MethodSource("addMembersInput")
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
    @MethodSource("removeMembersInput")
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
    @MethodSource("inviteMembersInput")
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
    @MethodSource("partialUpdateMemberInput")
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
    @MethodSource("getNewerRepliesInput")
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
    @MethodSource("getRepliesInput")
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
    @MethodSource("getRepliesMoreInput")
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
    @MethodSource("sendActionInput")
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
    @MethodSource("updateUsersInput")
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
    @MethodSource("blockUserInput")
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
    @MethodSource("unblockUserInput")
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
    @MethodSource("queryBlockedUsersInput")
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
    @MethodSource("partialUpdateUserInput")
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
    @MethodSource("getGuestUserInput")
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
    @MethodSource("translateInput")
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
    @MethodSource("ogInput")
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

        private var cdnApi: RetrofitCdnApi = mock()

        private var fileUploader: FileUploader = mock()
        private var fileTransformer: FileTransformer = NoOpFileTransformer

        private var coroutineScope = TestScope()

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

        fun withCallApi(callApi: VideoCallApi) = apply {
            this.callApi = callApi
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

        fun withFileUploader(fileUploader: FileUploader) = apply {
            this.fileUploader = fileUploader
        }

        fun withFileTransformer(fileTransformer: FileTransformer) = apply {
            this.fileTransformer = fileTransformer
        }

        fun withCoroutineScope(coroutineScope: TestScope) = apply {
            this.coroutineScope = coroutineScope
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
                userScope = UserScope(ClientScope()),
                coroutineScope = testCoroutineExtension.scope,
            )
        }
    }

    companion object {

        @JvmField
        @RegisterExtension
        val testCoroutineExtension = TestCoroutineExtension()

        @JvmStatic
        fun appSettingsInput() = listOf(
            Arguments.of(RetroSuccess(Mother.randomAppSettingsResponse()).toRetrofitCall(), Result.Success::class),
            Arguments.of(RetroError<AppSettingsResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
        )

        @JvmStatic
        fun sendMessageInput() = listOf(
            Arguments.of(
                RetroSuccess(MessageResponse(Mother.randomDownstreamMessageDto())).toRetrofitCall(),
                Result.Success::class,
            ),
            Arguments.of(RetroError<MessageResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
        )

        @JvmStatic
        fun updateMessageInput() = listOf(
            Arguments.of(
                RetroSuccess(MessageResponse(Mother.randomDownstreamMessageDto())).toRetrofitCall(),
                Result.Success::class,
            ),
            Arguments.of(RetroError<MessageResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
        )

        @JvmStatic
        fun partialUpdateMessageInput() = listOf(
            Arguments.of(
                RetroSuccess(MessageResponse(Mother.randomDownstreamMessageDto())).toRetrofitCall(),
                Result.Success::class,
            ),
            Arguments.of(RetroError<MessageResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
        )

        @JvmStatic
        fun getMessageInput() = listOf(
            Arguments.of(
                RetroSuccess(MessageResponse(Mother.randomDownstreamMessageDto())).toRetrofitCall(),
                Result.Success::class,
            ),
            Arguments.of(RetroError<MessageResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
        )

        @JvmStatic
        fun deleteMessageInput() = listOf(
            Arguments.of(
                true,
                RetroSuccess(MessageResponse(Mother.randomDownstreamMessageDto())).toRetrofitCall(),
                Result.Success::class,
            ),
            Arguments.of(
                false,
                RetroSuccess(MessageResponse(Mother.randomDownstreamMessageDto())).toRetrofitCall(),
                Result.Success::class,
            ),
            Arguments.of(
                true,
                RetroError<MessageResponse>(statusCode = 500).toRetrofitCall(),
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
        fun sendReactionInput() = listOf(
            Arguments.of(
                RetroSuccess(ReactionResponse(Mother.randomDownstreamReactionDto())).toRetrofitCall(),
                Result.Success::class,
            ),
            Arguments.of(RetroError<ReactionsResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
        )

        @JvmStatic
        fun deleteReactionInput() = listOf(
            Arguments.of(
                RetroSuccess(MessageResponse(Mother.randomDownstreamMessageDto())).toRetrofitCall(),
                Result.Success::class,
            ),
            Arguments.of(RetroError<MessageResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
        )

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

        fun muteUserResponseArguments() = listOf(
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

        fun completableResponseArguments() = listOf(
            Arguments.of(RetroSuccess(CompletableResponse("")).toRetrofitCall(), Result.Success::class),
            Arguments.of(RetroError<CompletableResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
        )

        fun uploadedFileArguments() = listOf(
            Arguments.of(Result.Success(UploadedFile(randomString())), Result.Success::class),
            Arguments.of(Result.Failure(Error.GenericError(randomString())), Result.Failure::class),
        )

        fun deleteFileArguments() = listOf(
            Arguments.of(Result.Success(Unit), Result.Success::class),
            Arguments.of(Result.Failure(Error.GenericError(randomString())), Result.Success::class),
        )

        fun flagResponseArguments() = listOf(
            Arguments.of(
                RetroSuccess(FlagResponse(Mother.randomDownstreamFlagDto())).toRetrofitCall(),
                Result.Success::class,
            ),
            Arguments.of(RetroError<FlagResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
        )

        fun channelResponseArguments() = listOf(
            Arguments.of(
                RetroSuccess(
                    ChannelResponse(
                        channel = Mother.randomDownstreamChannelDto(),
                        hidden = randomBoolean(),
                        membership = Mother.randomDownstreamMemberDto(),
                        hide_messages_before = randomDateOrNull(),
                    ),
                ).toRetrofitCall(),
                Result.Success::class,
            ),
            Arguments.of(RetroError<ChannelResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
        )

        fun messageResponseArguments() = listOf(
            Arguments.of(
                RetroSuccess(MessageResponse(Mother.randomDownstreamMessageDto())).toRetrofitCall(),
                Result.Success::class,
            ),
            Arguments.of(RetroError<MessageResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
        )

        fun messagesResponseArguments() = listOf(
            Arguments.of(
                RetroSuccess(MessagesResponse(listOf(Mother.randomDownstreamMessageDto()))).toRetrofitCall(),
                Result.Success::class,
            ),
            Arguments.of(RetroError<MessagesResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
        )

        fun updateUsersResponseArguments(): List<Arguments> {
            val userId = randomString()
            val user = Mother.randomDownstreamUserDto()
            val response = UpdateUsersResponse(mapOf(userId to user))
            return listOf(
                Arguments.of(RetroSuccess(response).toRetrofitCall(), Result.Success::class),
                Arguments.of(RetroError<UpdateUsersResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class),
            )
        }
    }
}
