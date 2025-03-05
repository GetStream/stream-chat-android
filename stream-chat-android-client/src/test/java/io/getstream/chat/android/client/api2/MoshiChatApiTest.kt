package io.getstream.chat.android.client.api2

import io.getstream.chat.android.client.Mother
import io.getstream.chat.android.client.api.RetrofitCdnApi
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
import io.getstream.chat.android.client.api2.model.requests.AddDeviceRequest
import io.getstream.chat.android.client.api2.model.requests.MuteChannelRequest
import io.getstream.chat.android.client.api2.model.requests.MuteUserRequest
import io.getstream.chat.android.client.api2.model.requests.PartialUpdateMessageRequest
import io.getstream.chat.android.client.api2.model.response.AppSettingsResponse
import io.getstream.chat.android.client.api2.model.response.CompletableResponse
import io.getstream.chat.android.client.api2.model.response.DevicesResponse
import io.getstream.chat.android.client.api2.model.response.MessageResponse
import io.getstream.chat.android.client.api2.model.response.MuteUserResponse
import io.getstream.chat.android.client.api2.model.response.ReactionResponse
import io.getstream.chat.android.client.api2.model.response.ReactionsResponse
import io.getstream.chat.android.client.call.RetrofitCall
import io.getstream.chat.android.client.scope.ClientScope
import io.getstream.chat.android.client.scope.UserScope
import io.getstream.chat.android.client.uploader.FileTransformer
import io.getstream.chat.android.client.uploader.FileUploader
import io.getstream.chat.android.client.uploader.NoOpFileTransformer
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.UploadedFile
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomDevice
import io.getstream.chat.android.randomFile
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomReaction
import io.getstream.chat.android.randomString
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
            Arguments.of(RetroError<AppSettingsResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class)
        )

        @JvmStatic
        fun sendMessageInput() = listOf(
            Arguments.of(
                RetroSuccess(MessageResponse(Mother.randomDownstreamMessageDto())).toRetrofitCall(),
                Result.Success::class
            ),
            Arguments.of(RetroError<MessageResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class)
        )

        @JvmStatic
        fun updateMessageInput() = listOf(
            Arguments.of(
                RetroSuccess(MessageResponse(Mother.randomDownstreamMessageDto())).toRetrofitCall(),
                Result.Success::class
            ),
            Arguments.of(RetroError<MessageResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class)
        )

        @JvmStatic
        fun partialUpdateMessageInput() = listOf(
            Arguments.of(
                RetroSuccess(MessageResponse(Mother.randomDownstreamMessageDto())).toRetrofitCall(),
                Result.Success::class
            ),
            Arguments.of(RetroError<MessageResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class)
        )

        @JvmStatic
        fun getMessageInput() = listOf(
            Arguments.of(
                RetroSuccess(MessageResponse(Mother.randomDownstreamMessageDto())).toRetrofitCall(),
                Result.Success::class,
            ),
            Arguments.of(RetroError<MessageResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class)
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
                Result.Failure::class
            )
        )

        @JvmStatic
        fun getReactionsInput() = listOf(
            Arguments.of(
                RetroSuccess(ReactionsResponse(listOf(Mother.randomDownstreamReactionDto()))).toRetrofitCall(),
                Result.Success::class
            ),
            Arguments.of(RetroError<ReactionsResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class)
        )

        @JvmStatic
        fun sendReactionInput() = listOf(
            Arguments.of(
                RetroSuccess(ReactionResponse(Mother.randomDownstreamReactionDto())).toRetrofitCall(),
                Result.Success::class
            ),
            Arguments.of(RetroError<ReactionsResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class)
        )

        @JvmStatic
        fun deleteReactionInput() = listOf(
            Arguments.of(
                RetroSuccess(MessageResponse(Mother.randomDownstreamMessageDto())).toRetrofitCall(),
                Result.Success::class
            ),
            Arguments.of(RetroError<MessageResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class)
        )

        @JvmStatic
        fun addDeviceInput() = completableResponseArguments()

        @JvmStatic
        fun deleteDeviceInput() = completableResponseArguments()

        @JvmStatic
        fun getDevicesInput() = listOf(
            Arguments.of(
                RetroSuccess(DevicesResponse(listOf(Mother.randomDeviceDto()))).toRetrofitCall(),
                Result.Success::class
            ),
            Arguments.of(RetroError<DevicesResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class)
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
        fun deleteImage() = deleteFileArguments()

        fun muteUserResponseArguments() = listOf(
            Arguments.of(
                RetroSuccess(
                    MuteUserResponse(
                        Mother.randomDownstreamMuteDto(),
                        Mother.randomDownstreamUserDto()
                    )
                ).toRetrofitCall(),
                Result.Success::class,
            ),
            Arguments.of(RetroError<CompletableResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class)
        )

        fun completableResponseArguments() = listOf(
            Arguments.of(RetroSuccess(CompletableResponse("")).toRetrofitCall(), Result.Success::class),
            Arguments.of(RetroError<CompletableResponse>(statusCode = 500).toRetrofitCall(), Result.Failure::class)
        )

        fun uploadedFileArguments() = listOf(
            Arguments.of(Result.Success(UploadedFile(randomString())), Result.Success::class),
            Arguments.of(Result.Failure(Error.GenericError(randomString())), Result.Failure::class)
        )

        fun deleteFileArguments() = listOf(
            Arguments.of(Result.Success(Unit), Result.Success::class),
            Arguments.of(Result.Failure(Error.GenericError(randomString())), Result.Failure::class)
        )
    }
}