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

package io.getstream.chat.android.client

import io.getstream.chat.android.PrivacySettings
import io.getstream.chat.android.TypingIndicators
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.chatclient.BaseChatClientTest
import io.getstream.chat.android.client.clientstate.UserState
import io.getstream.chat.android.client.errors.cause.StreamChannelNotFoundException
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.parser.EventArguments
import io.getstream.chat.android.client.plugin.Plugin
import io.getstream.chat.android.client.query.AddMembersParams
import io.getstream.chat.android.client.query.CreateChannelParams
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.client.utils.verifyGenericError
import io.getstream.chat.android.client.utils.verifyNetworkError
import io.getstream.chat.android.client.utils.verifySuccess
import io.getstream.chat.android.client.utils.verifyThrowableError
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.MemberData
import io.getstream.chat.android.models.User
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomExtraData
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.call.Call
import io.getstream.result.call.CoroutineCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.spy
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.mockito.kotlin.wheneverBlocking

/**
 * Tests for the [ChatClient] channels endpoints.
 */
@Suppress("LargeClass")
internal class ChatClientChannelApiTests : BaseChatClientTest() {

    @Test
    fun queryChannelsInternalSuccess() = runTest {
        // given
        val request = Mother.randomQueryChannelsRequest()
        val channels = listOf(randomChannel())
        val sut = Fixture()
            .givenQueryChannelsResult(RetroSuccess(channels).toRetrofitCall())
            .get()
        // when
        val result = sut.queryChannelsInternal(request).await()
        // then
        verifySuccess(result, channels)
    }

    @Test
    fun queryChannelsInternalError() = runTest {
        // given
        val request = Mother.randomQueryChannelsRequest()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenQueryChannelsResult(RetroError<List<Channel>>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.queryChannelsInternal(request).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun getChannelByCidSuccess() = runTest {
        // given
        val cid = randomCID()
        val messageLimit = randomInt()
        val memberLimit = randomInt()
        val state = randomBoolean()
        val response = randomChannel()
        val sut = Fixture()
            .givenQueryChannelsResult(RetroSuccess(listOf(response)).toRetrofitCall())
            .get()
        // when
        val result = sut.getChannel(cid, messageLimit, memberLimit, state).await()
        // then
        verifySuccess(result, response)
    }

    @Test
    fun getChannelByCIdSuccessWithoutResults() = runTest {
        // given
        val cid = randomCID()
        val messageLimit = randomInt()
        val memberLimit = randomInt()
        val state = randomBoolean()
        val sut = Fixture()
            .givenQueryChannelsResult(RetroSuccess(emptyList<Channel>()).toRetrofitCall())
            .get()
        // when
        val result = sut.getChannel(cid, messageLimit, memberLimit, state).await()
        // then
        val expectedCause = StreamChannelNotFoundException(cid)
        verifyThrowableError(result, expectedCause.message, expectedCause::class.java)
    }

    @Test
    fun getChannelByCidError() = runTest {
        // given
        val cid = randomCID()
        val messageLimit = randomInt()
        val memberLimit = randomInt()
        val state = randomBoolean()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenQueryChannelsResult(RetroError<List<Channel>>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.getChannel(cid, messageLimit, memberLimit, state).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun getChannelByTypeAndIdSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val messageLimit = positiveRandomInt()
        val memberLimit = positiveRandomInt()
        val state = randomBoolean()
        val response = randomChannel()
        val sut = Fixture()
            .givenQueryChannelsResult(RetroSuccess(listOf(response)).toRetrofitCall())
            .get()
        // when
        val result = sut.getChannel(channelType, channelId, messageLimit, memberLimit, state).await()
        // then
        verifySuccess(result, response)
    }

    @Test
    fun getChannelByTypeAndIdError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val messageLimit = positiveRandomInt()
        val memberLimit = positiveRandomInt()
        val state = randomBoolean()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenQueryChannelsResult(RetroError<List<Channel>>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.getChannel(channelType, channelId, messageLimit, memberLimit, state).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun queryChannelSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val response = randomChannel()
        val request = Mother.randomQueryChannelRequest()
        val sut = Fixture()
            .givenQueryChannelResult { Result.Success(response) }
            .get()
        // when
        val result = sut.queryChannel(channelType, channelId, request).await()
        // then
        verifySuccess(result, response)
    }

    @Test
    fun queryChannelPreconditionFailed() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val response = randomChannel()
        val request = Mother.randomQueryChannelRequest()
        val plugin = mock<Plugin>().apply {
            whenever(this.onQueryChannelPrecondition(any(), any(), any()))
                .thenReturn(Result.Failure(Error.GenericError("")))
        }
        var apiCalled = false
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenQueryChannelResult {
                apiCalled = true
                Result.Success(response)
            }
            .get()
        // when
        val result = sut.queryChannel(channelType, channelId, request).await()
        // then
        verifyGenericError(result, "")
        apiCalled `should be equal to` false
        val inOrder = Mockito.inOrder(plugin)
        inOrder.verify(plugin).onQueryChannelPrecondition(channelType, channelId, request)
        inOrder.verify(plugin, never()).onQueryChannelRequest(channelType, channelId, request)
        inOrder.verify(plugin, never()).onQueryChannelResult(result, channelType, channelId, request)
    }

    @Test
    fun queryChannelSuccessInvokesPluginsInCorrectOrder() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val response = randomChannel()
        val request = Mother.randomQueryChannelRequest()
        val plugin = mock<Plugin>()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenQueryChannelResult { Result.Success(response) }
            .get()
        // when
        val result = sut.queryChannel(channelType, channelId, request, skipOnRequest = false).await()
        // then
        verifySuccess(result, response)
        val inOrder = Mockito.inOrder(plugin)
        inOrder.verify(plugin).onQueryChannelPrecondition(channelType, channelId, request)
        inOrder.verify(plugin).onQueryChannelRequest(channelType, channelId, request)
        inOrder.verify(plugin).onQueryChannelResult(result, channelType, channelId, request)
    }

    @Test
    fun queryChannelError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val request = QueryChannelRequest()
        val sut = Fixture()
            .givenQueryChannelResult { Result.Failure(Error.GenericError("")) }
            .get()
        // when
        val result = sut.queryChannel(channelType, channelId, request).await()
        // then
        verifyGenericError(result, "")
    }

    @Test
    fun createChannelSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val memberIds = listOf(randomString())
        val extraData = randomExtraData(1)
        val plugin = mock<Plugin>()
        val user = randomUser()
        val channel = randomChannel()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenUser(user)
            .givenQueryChannelResult { Result.Success(channel) }
            .get()
        // when
        val result = sut.createChannel(channelType, channelId, memberIds, extraData).await()
        // then
        val expectedParams = CreateChannelParams(
            members = memberIds.map { MemberData(it) },
            extraData = extraData,
        )
        verifySuccess(result, channel)
        val inOrder = Mockito.inOrder(plugin)
        inOrder.verify(plugin).onCreateChannelPrecondition(user, channelId, memberIds)
        inOrder.verify(plugin).onCreateChannelRequest(channelType, channelId, expectedParams, user)
        inOrder.verify(plugin).onCreateChannelResult(channelType, channelId, memberIds, result)
    }

    @Test
    fun createChannelError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val memberIds = listOf(randomString())
        val extraData = randomExtraData(1)
        val plugin = mock<Plugin>()
        val user = randomUser()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenUser(user)
            .givenQueryChannelResult { Result.Failure(Error.GenericError("")) }
            .get()
        // when
        val result = sut.createChannel(channelType, channelId, memberIds, extraData).await()
        // then
        val expectedParams = CreateChannelParams(
            members = memberIds.map { MemberData(it) },
            extraData = extraData,
        )
        verifyGenericError(result, "")
        val inOrder = Mockito.inOrder(plugin)
        inOrder.verify(plugin).onCreateChannelPrecondition(user, channelId, memberIds)
        inOrder.verify(plugin).onCreateChannelRequest(channelType, channelId, expectedParams, user)
        inOrder.verify(plugin).onCreateChannelResult(channelType, channelId, memberIds, result)
    }

    @Test
    fun showChannelSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val sut = Fixture()
            .givenShowChannelResult(RetroSuccess(Unit).toRetrofitCall())
            .get()
        // when
        val result = sut.showChannel(channelType, channelId).await()
        // then
        verifySuccess(result, Unit)
    }

    @Test
    fun showChannelError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenShowChannelResult(RetroError<Unit>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.showChannel(channelType, channelId).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun deleteChannelSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val response = randomChannel()
        val currentUser = randomUser()
        val plugin = mock<Plugin>()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenUser(currentUser)
            .givenDeleteChannelResult(RetroSuccess(response).toRetrofitCall())
            .get()
        // when
        val result = sut.deleteChannel(channelType, channelId).await()
        // then
        verifySuccess(result, response)
        val inOrder = Mockito.inOrder(plugin)
        inOrder.verify(plugin).onDeleteChannelPrecondition(currentUser, channelType, channelId)
        inOrder.verify(plugin).onDeleteChannelRequest(currentUser, channelType, channelId)
        inOrder.verify(plugin).onDeleteChannelResult(channelType, channelId, result)
    }

    @Test
    fun deleteChannelError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val errorCode = positiveRandomInt()
        val currentUser = randomUser()
        val plugin = mock<Plugin>()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenUser(currentUser)
            .givenDeleteChannelResult(RetroError<Channel>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.deleteChannel(channelType, channelId).await()
        // then
        verifyNetworkError(result, errorCode)
        val inOrder = Mockito.inOrder(plugin)
        inOrder.verify(plugin).onDeleteChannelPrecondition(currentUser, channelType, channelId)
        inOrder.verify(plugin).onDeleteChannelRequest(currentUser, channelType, channelId)
        inOrder.verify(plugin).onDeleteChannelResult(channelType, channelId, result)
    }

    @Test
    fun hideChannelSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val clearHistory = randomBoolean()
        val plugin = mock<Plugin>()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenHideChannelResult(RetroSuccess(Unit).toRetrofitCall())
            .get()
        // when
        val result = sut.hideChannel(channelType, channelId, clearHistory).await()
        // then
        verifySuccess(result, Unit)
        val inOrder = Mockito.inOrder(plugin)
        inOrder.verify(plugin).onHideChannelPrecondition(channelType, channelId, clearHistory)
        inOrder.verify(plugin).onHideChannelRequest(channelType, channelId, clearHistory)
        inOrder.verify(plugin).onHideChannelResult(result, channelType, channelId, clearHistory)
    }

    @Test
    fun hideChannelError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val clearHistory = randomBoolean()
        val errorCode = positiveRandomInt()
        val plugin = mock<Plugin>()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenHideChannelResult(RetroError<Unit>(errorCode).toRetrofitCall())
            .get()
        // then
        val result = sut.hideChannel(channelType, channelId, clearHistory).await()
        // then
        verifyNetworkError(result, errorCode)
        val inOrder = Mockito.inOrder(plugin)
        inOrder.verify(plugin).onHideChannelPrecondition(channelType, channelId, clearHistory)
        inOrder.verify(plugin).onHideChannelRequest(channelType, channelId, clearHistory)
        inOrder.verify(plugin).onHideChannelResult(result, channelType, channelId, clearHistory)
    }

    @Test
    fun truncateChannelSuccess() = runTest {
        val channelType = randomString()
        val channelId = randomString()
        val systemMessage = randomMessage()
        val response = randomChannel()
        val sut = Fixture()
            .givenTruncateChannelResult(RetroSuccess(response).toRetrofitCall())
            .get()
        // when
        val result = sut.truncateChannel(channelType, channelId, systemMessage).await()
        // then
        verifySuccess(result, response)
    }

    @Test
    fun truncateChannelError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val systemMessage = randomMessage()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenTruncateChannelResult(RetroError<Channel>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.truncateChannel(channelType, channelId, systemMessage).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun updateChannelSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val updateMessage = randomMessage()
        val updateChannelData = mapOf<String, Any>()
        val responseChannel = Channel()
        val sut = Fixture()
            .givenUpdateChannelResult(RetroSuccess(responseChannel).toRetrofitCall())
            .get()
        // when
        val result = sut.updateChannel(channelType, channelId, updateMessage, updateChannelData).await()
        // then
        verifySuccess(result, responseChannel)
    }

    @Test
    fun updateChannelError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val updateMessage = randomMessage()
        val updateChannelData = mapOf<String, Any>()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenUpdateChannelResult(RetroError<Channel>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.updateChannel(channelType, channelId, updateMessage, updateChannelData).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun updateChannelPartialSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val set = emptyMap<String, Any>()
        val unset = emptyList<String>()
        val response = randomChannel()
        val sut = Fixture()
            .givenUpdateChannelPartialResult(RetroSuccess(response).toRetrofitCall())
            .get()
        // when
        val result = sut.updateChannelPartial(channelType, channelId, set, unset).await()
        // then
        verifySuccess(result, response)
    }

    @Test
    fun updateChannelPartialError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val set = emptyMap<String, Any>()
        val unset = emptyList<String>()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenUpdateChannelPartialResult(RetroError<Channel>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.updateChannelPartial(channelType, channelId, set, unset).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun pinChannelSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val user = randomUser()
        val response = randomMember()
        val sut = spy(
            Fixture()
                .givenUser(user)
                .givenPartialUpdateMemberResult(RetroSuccess(response).toRetrofitCall())
                .get(),
        )
        // when
        val result = sut.pinChannel(channelType, channelId).await()
        // then
        verifySuccess(result, response)
        verify(sut).partialUpdateMember(channelType, channelId, user.id, mapOf("pinned" to true), emptyList())
    }

    @Test
    fun pinChannelError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val user = randomUser()
        val errorCode = positiveRandomInt()
        val sut = spy(
            Fixture()
                .givenUser(user)
                .givenPartialUpdateMemberResult(RetroError<Member>(errorCode).toRetrofitCall())
                .get(),
        )
        // when
        val result = sut.pinChannel(channelType, channelId).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(sut).partialUpdateMember(channelType, channelId, user.id, mapOf("pinned" to true), emptyList())
    }

    @Test
    fun unpinChannelSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val user = randomUser()
        val response = randomMember()
        val sut = spy(
            Fixture()
                .givenUser(user)
                .givenPartialUpdateMemberResult(RetroSuccess(response).toRetrofitCall())
                .get(),
        )
        // when
        val result = sut.unpinChannel(channelType, channelId).await()
        // then
        verifySuccess(result, response)
        verify(sut).partialUpdateMember(channelType, channelId, user.id, emptyMap(), listOf("pinned"))
    }

    @Test
    fun unpinChannelError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val user = randomUser()
        val errorCode = positiveRandomInt()
        val sut = spy(
            Fixture()
                .givenUser(user)
                .givenPartialUpdateMemberResult(RetroError<Member>(errorCode).toRetrofitCall())
                .get(),
        )
        // when
        val result = sut.unpinChannel(channelType, channelId).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(sut).partialUpdateMember(channelType, channelId, user.id, emptyMap(), listOf("pinned"))
    }

    @Test
    fun archiveChannelSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val response = randomMember()
        val user = randomUser()
        val sut = spy(
            Fixture()
                .givenUser(user)
                .givenPartialUpdateMemberResult(RetroSuccess(response).toRetrofitCall())
                .get(),
        )
        // when
        val result = sut.archiveChannel(channelType, channelId).await()
        // then
        verifySuccess(result, response)
        verify(sut).partialUpdateMember(channelType, channelId, user.id, mapOf("archived" to true), emptyList())
    }

    @Test
    fun archiveChannelError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val errorCode = positiveRandomInt()
        val user = randomUser()
        val sut = spy(
            Fixture()
                .givenUser(user)
                .givenPartialUpdateMemberResult(RetroError<Member>(errorCode).toRetrofitCall())
                .get(),
        )
        // when
        val result = sut.archiveChannel(channelType, channelId).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(sut).partialUpdateMember(channelType, channelId, user.id, mapOf("archived" to true), emptyList())
    }

    @Test
    fun unarchiveChannelSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val response = randomMember()
        val user = randomUser()
        val sut = spy(
            Fixture()
                .givenUser(user)
                .givenPartialUpdateMemberResult(RetroSuccess(response).toRetrofitCall())
                .get(),
        )
        // when
        val result = sut.unarchiveChannel(channelType, channelId).await()
        // then
        verifySuccess(result, response)
        verify(sut).partialUpdateMember(channelType, channelId, user.id, emptyMap(), listOf("archived"))
    }

    @Test
    fun unarchiveChannelError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val errorCode = positiveRandomInt()
        val user = randomUser()
        val sut = spy(
            Fixture()
                .givenUser(user)
                .givenPartialUpdateMemberResult(RetroError<Member>(errorCode).toRetrofitCall())
                .get(),
        )
        // when
        val result = sut.unarchiveChannel(channelType, channelId).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(sut).partialUpdateMember(channelType, channelId, user.id, emptyMap(), listOf("archived"))
    }

    @Test
    fun partialUpdateMemberSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val userId = randomString()
        val set = emptyMap<String, Any>()
        val unset = emptyList<String>()
        val member = randomMember()
        val sut = Fixture()
            .givenPartialUpdateMemberResult(RetroSuccess(member).toRetrofitCall())
            .get()
        // when
        val result = sut.partialUpdateMember(channelType, channelId, userId, set, unset).await()
        // then
        verifySuccess(result, member)
    }

    @Test
    fun partialUpdateMemberError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val userId = randomString()
        val set = emptyMap<String, Any>()
        val unset = emptyList<String>()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenPartialUpdateMemberResult(RetroError<Member>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.partialUpdateMember(channelType, channelId, userId, set, unset).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun enableSlowModeSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val cooldown = 60 // below max allowed value of 120
        val response = randomChannel()
        val sut = Fixture()
            .givenEnableSlowModeResult(RetroSuccess(response).toRetrofitCall())
            .get()
        // when
        val result = sut.enableSlowMode(channelType, channelId, cooldown).await()
        // then
        verifySuccess(result, response)
    }

    @Test
    fun enableSlowModeCooldownTooHighError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val cooldown = 121 // above max allowed value of 120
        val sut = Fixture().get()
        // when
        val result = sut.enableSlowMode(channelType, channelId, cooldown).await()
        // then
        val message = "You can't specify a value outside the range 1-120 for cooldown duration."
        verifyGenericError(result, message)
    }

    @Test
    fun enableSlowModeCooldownTooLowError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val cooldown = 0 // below min allowed value of 1
        val sut = Fixture().get()
        // when
        val result = sut.enableSlowMode(channelType, channelId, cooldown).await()
        // then
        val message = "You can't specify a value outside the range 1-120 for cooldown duration."
        verifyGenericError(result, message)
    }

    @Test
    fun enableSlowModeError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val cooldown = 60
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenEnableSlowModeResult(RetroError<Channel>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.enableSlowMode(channelType, channelId, cooldown).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun disableSlowModeSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val response = randomChannel()
        val sut = Fixture()
            .givenDisableSlowModeResult(RetroSuccess(response).toRetrofitCall())
            .get()
        // when
        val result = sut.disableSlowMode(channelType, channelId).await()
        // then
        verifySuccess(result, response)
    }

    @Test
    fun disableSlowModeError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenDisableSlowModeResult(RetroError<Channel>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.disableSlowMode(channelType, channelId).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun acceptInviteSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val responseChannel = randomChannel()
        val acceptInviteMessage = randomString()
        val sut = Fixture()
            .givenAcceptInviteResult(RetroSuccess(responseChannel).toRetrofitCall())
            .get()
        // when
        val result = sut.acceptInvite(channelType, channelId, acceptInviteMessage).await()
        // then
        verifySuccess(result, responseChannel)
    }

    @Test
    fun acceptInviteError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val acceptInviteMessage = randomString()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenAcceptInviteResult(RetroError<Channel>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.acceptInvite(channelType, channelId, acceptInviteMessage).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun rejectInviteSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val responseChannel = randomChannel()
        val sut = Fixture()
            .givenRejectInviteResult(RetroSuccess(responseChannel).toRetrofitCall())
            .get()
        // when
        val result = sut.rejectInvite(channelType, channelId).await()
        // then
        verifySuccess(result, responseChannel)
    }

    @Test
    fun rejectInviteError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenRejectInviteResult(RetroError<Channel>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.rejectInvite(channelType, channelId).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun sendEventSuccess() = runTest {
        // given
        val eventType = randomString()
        val channelType = randomString()
        val channelId = randomString()
        val extraData = emptyMap<Any, Any>()
        val response = EventArguments.randomEvent()
        val sut = Fixture()
            .givenSendEventResult(RetroSuccess(response).toRetrofitCall())
            .get()
        // when
        val result = sut.sendEvent(eventType, channelType, channelId, extraData).await()
        // then
        verifySuccess(result, response)
    }

    @Test
    fun sendEventError() = runTest {
        // given
        val eventType = randomString()
        val channelType = randomString()
        val channelId = randomString()
        val extraData = emptyMap<Any, Any>()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenSendEventResult(RetroError<ChatEvent>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.sendEvent(eventType, channelType, channelId, extraData).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun markAllReadSuccess() = runTest {
        // given
        val plugin = mock<Plugin>()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenMarkAllReadResult(RetroSuccess(Unit).toRetrofitCall())
            .get()
        // when
        val result = sut.markAllRead().await()
        // then
        verifySuccess(result, Unit)
        verify(plugin).onMarkAllReadRequest()
    }

    @Test
    fun markAllReadError() = runTest {
        // given
        val plugin = mock<Plugin>()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenMarkAllReadResult(RetroError<Unit>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.markAllRead().await()
        // then
        verifyNetworkError(result, errorCode)
        verify(plugin).onMarkAllReadRequest()
    }

    @Test
    fun markMessageReadSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val messageId = randomString()
        val sut = Fixture()
            .givenMarkReadResult(RetroSuccess(Unit).toRetrofitCall())
            .get()
        // when
        val result = sut.markMessageRead(channelType, channelId, messageId).await()
        // then
        verifySuccess(result, Unit)
    }

    @Test
    fun markMessageReadError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val messageId = randomString()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenMarkReadResult(RetroError<Unit>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.markMessageRead(channelType, channelId, messageId).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun markMessageAsDeliveredSuccess() = runTest {
        // given
        val messageId = randomString()
        val result = randomBoolean()
        val sut = Fixture()
            .givenMarkDeliveredResult(messageId, result)
            .get()
        // when
        val actual = sut.markMessageAsDelivered(messageId).await()
        // then
        verifySuccess(actual, result)
    }

    @Test
    fun markMessageAsDeliveredError() = runTest {
        // given
        val messageId = randomString()
        val exception = RuntimeException("Error")
        val sut = Fixture()
            .givenMarkDeliveredResult(messageId, exception)
            .get()
        // when
        val actual = sut.markMessageAsDelivered(messageId).await()
        // then
        verifyGenericError(actual, exception.message!!)
    }

    @Test
    fun markReadSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val plugin = mock<Plugin>()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenMarkReadResult(RetroSuccess(Unit).toRetrofitCall())
            .get()
        // when
        val result = sut.markRead(channelType, channelId).await()
        // then
        verifySuccess(result, Unit)
        verify(plugin).onChannelMarkReadPrecondition(channelType, channelId)
    }

    @Test
    fun markReadPreconditionFailed() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val plugin = mock<Plugin>().apply {
            whenever(onChannelMarkReadPrecondition(channelType, channelId))
                .thenReturn(Result.Failure(Error.GenericError("")))
        }
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenMarkReadResult(RetroSuccess(Unit).toRetrofitCall())
            .get()
        // when
        val result = sut.markRead(channelType, channelId).await()
        // then
        verifyGenericError(result, "")
        verify(plugin).onChannelMarkReadPrecondition(channelType, channelId)
    }

    @Test
    fun markReadError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val errorCode = positiveRandomInt()
        val plugin = mock<Plugin>()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenMarkReadResult(RetroError<Unit>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.markRead(channelType, channelId).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(plugin).onChannelMarkReadPrecondition(channelType, channelId)
    }

    @Test
    fun markUnreadByIdSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val messageId = randomString()
        val sut = Fixture()
            .givenMarkUnreadResult(RetroSuccess(Unit).toRetrofitCall())
            .get()
        // when
        val result = sut.markUnread(channelType, channelId, messageId).await()
        // then
        verifySuccess(result, Unit)
    }

    @Test
    fun markUnreadByIdError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val messageId = randomString()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenMarkUnreadResult(RetroError<Unit>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.markUnread(channelType, channelId, messageId).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun markUnreadByTimestampSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val timestamp = randomDate()
        val sut = Fixture()
            .givenMarkUnreadResult(RetroSuccess(Unit).toRetrofitCall())
            .get()
        // when
        val result = sut.markUnread(channelType, channelId, timestamp).await()
        // then
        verifySuccess(result, Unit)
    }

    @Test
    fun markUnreadByTimestampError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val timestamp = randomDate()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenMarkUnreadResult(RetroError<Unit>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.markUnread(channelType, channelId, timestamp).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun markThreadReadSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val threadId = randomString()
        val sut = Fixture()
            .givenMarkThreadReadResult(RetroSuccess(Unit).toRetrofitCall())
            .get()
        // when
        val result = sut.markThreadRead(channelType, channelId, threadId).await()
        // then
        verifySuccess(result, Unit)
    }

    @Test
    fun markThreadReadError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val threadId = randomString()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenMarkThreadReadResult(RetroError<Unit>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.markThreadRead(channelType, channelId, threadId).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun markThreadUnreadSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val threadId = randomString()
        val sut = Fixture()
            .givenMarkUnreadResult(RetroSuccess(Unit).toRetrofitCall())
            .get()
        // when
        val result = sut.markThreadUnread(channelType, channelId, threadId).await()
        // then
        verifySuccess(result, Unit)
    }

    @Test
    fun markThreadUnreadError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val threadId = randomString()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenMarkUnreadResult(RetroError<Unit>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.markThreadUnread(channelType, channelId, threadId).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun markThreadUnreadFromMessageIdSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val threadId = randomString()
        val messageId = randomString()
        val sut = Fixture()
            .givenMarkUnreadResult(RetroSuccess(Unit).toRetrofitCall())
            .get()
        // when
        val result = sut.markThreadUnread(channelType, channelId, threadId, messageId).await()
        // then
        verifySuccess(result, Unit)
    }

    @Test
    fun markThreadUnreadFromMessageIdError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val threadId = randomString()
        val messageId = randomString()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenMarkUnreadResult(RetroError<Unit>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.markThreadUnread(channelType, channelId, threadId, messageId).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun queryChannelsSuccess() = runTest {
        // given
        val request = Mother.randomQueryChannelsRequest()
        val channel = randomChannel()
        val plugin = mock<Plugin>()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenQueryChannelsResult(RetroSuccess(listOf(channel)).toRetrofitCall())
            .get()
        // when
        val result = sut.queryChannels(request).await()
        // then
        verifySuccess(result, listOf(channel))
        val inOrder = Mockito.inOrder(plugin)
        inOrder.verify(plugin).onQueryChannelsPrecondition(request)
        inOrder.verify(plugin).onQueryChannelsRequest(request)
        inOrder.verify(plugin).onQueryChannelsResult(result, request)
    }

    @Test
    fun queryChannelsError() = runTest {
        // given
        val request = Mother.randomQueryChannelsRequest()
        val plugin = mock<Plugin>()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenQueryChannelsResult(RetroError<List<Channel>>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.queryChannels(request).await()
        // then
        verifyNetworkError(result, errorCode)
        val inOrder = Mockito.inOrder(plugin)
        inOrder.verify(plugin).onQueryChannelsPrecondition(request)
        inOrder.verify(plugin).onQueryChannelsRequest(request)
        inOrder.verify(plugin).onQueryChannelsResult(result, request)
    }

    @Test
    fun stopWatchingSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val sut = Fixture()
            .givenStopWatchingResult(RetroSuccess(Unit).toRetrofitCall())
            .get()
        // when
        val result = sut.stopWatching(channelType, channelId).await()
        // then
        verifySuccess(result, Unit)
    }

    @Test
    fun stopWatchingError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenStopWatchingResult(RetroError<Unit>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.stopWatching(channelType, channelId).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun addMembersSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val memberIds = listOf(randomString())
        val systemMessage = randomMessage()
        val hideHistory = randomBoolean()
        val hideHistoryBefore = randomDate()
        val skipPush = randomBoolean()
        val response = randomChannel()
        val sut = Fixture()
            .givenAddMembersResult(RetroSuccess(response).toRetrofitCall())
            .get()
        // when
        val result = sut
            .addMembers(channelType, channelId, memberIds, systemMessage, hideHistory, hideHistoryBefore, skipPush)
            .await()
        // then
        verifySuccess(result, response)
    }

    @Test
    fun addMembersWithDefaultArgumentsSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val memberIds = listOf(randomString())
        val response = randomChannel()
        val sut = spy(
            Fixture()
                .givenAddMembersResult(RetroSuccess(response).toRetrofitCall())
                .get(),
        )
        // when
        val result = sut.addMembers(channelType, channelId, memberIds).await()
        // then
        verifySuccess(result, response)
        verify(sut).addMembers(channelType, channelId, memberIds, null, null, null, null)
    }

    @Test
    fun addMembersError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val memberIds = listOf(randomString())
        val systemMessage = randomMessage()
        val hideHistory = randomBoolean()
        val hideHistoryBefore = randomDate()
        val skipPush = randomBoolean()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenAddMembersResult(RetroError<Channel>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut
            .addMembers(channelType, channelId, memberIds, systemMessage, hideHistory, hideHistoryBefore, skipPush)
            .await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun addMembersWithParamsSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val memberIds = listOf(randomString())
        val systemMessage = randomMessage()
        val hideHistory = randomBoolean()
        val hideHistoryBefore = randomDate()
        val skipPush = randomBoolean()
        val params = AddMembersParams(
            members = memberIds.map { MemberData(it) },
            systemMessage = systemMessage,
            hideHistory = hideHistory,
            hideHistoryBefore = hideHistoryBefore,
            skipPush = skipPush,
        )
        val response = randomChannel()
        val sut = Fixture()
            .givenAddMembersResult(RetroSuccess(response).toRetrofitCall())
            .get()
        // when
        val result = sut.addMembers(channelType, channelId, params).await()
        // then
        verifySuccess(result, response)
    }

    @Test
    fun addMembersWithParamsError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val memberIds = listOf(randomString())
        val systemMessage = randomMessage()
        val hideHistory = randomBoolean()
        val hideHistoryBefore = randomDate()
        val skipPush = randomBoolean()
        val params = AddMembersParams(
            members = memberIds.map { MemberData(it) },
            systemMessage = systemMessage,
            hideHistory = hideHistory,
            hideHistoryBefore = hideHistoryBefore,
            skipPush = skipPush,
        )
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenAddMembersResult(RetroError<Channel>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.addMembers(channelType, channelId, params).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun removeMembersSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val memberIds = listOf(randomString())
        val systemMessage = randomMessage()
        val skipPush = randomBoolean()
        val response = Channel()
        val sut = Fixture()
            .givenRemoveMembersResult(RetroSuccess(response).toRetrofitCall())
            .get()
        // when
        val result = sut.removeMembers(channelType, channelId, memberIds, systemMessage, skipPush).await()
        // then
        verifySuccess(result, response)
    }

    @Test
    fun removeMembersError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val memberIds = listOf(randomString())
        val systemMessage = randomMessage()
        val skipPush = randomBoolean()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenRemoveMembersResult(RetroError<Channel>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.removeMembers(channelType, channelId, memberIds, systemMessage, skipPush).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun inviteMembersSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val memberIds = listOf(randomString())
        val systemMessage = randomMessage()
        val skipPush = randomBoolean()
        val response = randomChannel()
        val sut = Fixture()
            .givenInviteMembersResult(RetroSuccess(response).toRetrofitCall())
            .get()
        // when
        val result = sut.inviteMembers(channelType, channelId, memberIds, systemMessage, skipPush).await()
        // then
        verifySuccess(result, response)
    }

    @Test
    fun inviteMembersError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val memberIds = listOf(randomString())
        val systemMessage = randomMessage()
        val skipPush = randomBoolean()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenInviteMembersResult(RetroError<Channel>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.inviteMembers(channelType, channelId, memberIds, systemMessage, skipPush).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun keystrokeSuccess() = runTest {
        // given
        val user = randomUser(
            privacySettings = PrivacySettings(typingIndicators = TypingIndicators(true)),
        )
        val channelType = randomString()
        val channelId = randomString()
        val parentId = randomString()
        val plugin = mock<Plugin>()
        val event = EventArguments.randomEvent()
        val sut = Fixture()
            .givenUser(user)
            .givenPlugin(plugin)
            .givenSendEventResult(RetroSuccess(event).toRetrofitCall())
            .get()
        // when
        val result = sut.keystroke(channelType, channelId, parentId).await()
        // then
        verifySuccess(result, event)
        val inOrder = Mockito.inOrder(plugin)
        inOrder.verify(plugin).onTypingEventPrecondition(any(), any(), any(), any(), any())
        inOrder.verify(plugin).onTypingEventRequest(any(), any(), any(), any(), any())
        inOrder.verify(plugin).onTypingEventResult(any(), any(), any(), any(), any(), any())
    }

    @Test
    fun keystrokeNotEnabledError() = runTest {
        // given
        val user = randomUser(
            privacySettings = PrivacySettings(typingIndicators = TypingIndicators(false)),
        )
        val channelType = randomString()
        val channelId = randomString()
        val parentId = randomString()
        val plugin = mock<Plugin>()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenUser(user)
            .get()
        // when
        val result = sut.keystroke(channelType, channelId, parentId).await()
        // then
        val message = "Typing indicators are disabled for the current user."
        verifyGenericError(result, message)
        verifyNoInteractions(plugin)
    }

    @Test
    fun keystrokeError() = runTest {
        // given
        val user = randomUser()
        val channelType = randomString()
        val channelId = randomString()
        val parentId = randomString()
        val errorCode = positiveRandomInt()
        val plugin = mock<Plugin>()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenUser(user)
            .givenSendEventResult(RetroError<ChatEvent>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.keystroke(channelType, channelId, parentId).await()
        // then
        verifyNetworkError(result, errorCode)
        val inOrder = Mockito.inOrder(plugin)
        inOrder.verify(plugin).onTypingEventPrecondition(any(), any(), any(), any(), any())
        inOrder.verify(plugin).onTypingEventRequest(any(), any(), any(), any(), any())
        inOrder.verify(plugin).onTypingEventResult(any(), any(), any(), any(), any(), any())
    }

    @Test
    fun stopTypingSuccess() = runTest {
        // given
        val user = randomUser()
        val channelType = randomString()
        val channelId = randomString()
        val parentId = randomString()
        val plugin = mock<Plugin>()
        val event = EventArguments.randomEvent()
        val sut = Fixture()
            .givenUser(user)
            .givenPlugin(plugin)
            .givenSendEventResult(RetroSuccess(event).toRetrofitCall())
            .get()
        // when
        val result = sut.stopTyping(channelType, channelId, parentId).await()
        // then
        verifySuccess(result, event)
        val inOrder = Mockito.inOrder(plugin)
        inOrder.verify(plugin).onTypingEventPrecondition(any(), any(), any(), any(), any())
        inOrder.verify(plugin).onTypingEventRequest(any(), any(), any(), any(), any())
        inOrder.verify(plugin).onTypingEventResult(any(), any(), any(), any(), any(), any())
    }

    @Test
    fun stopTypingNotEnabledError() = runTest {
        // given
        val user = randomUser(
            privacySettings = PrivacySettings(typingIndicators = TypingIndicators(false)),
        )
        val channelType = randomString()
        val channelId = randomString()
        val parentId = randomString()
        val plugin = mock<Plugin>()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenUser(user)
            .get()
        // when
        val result = sut.stopTyping(channelType, channelId, parentId).await()
        // then
        val message = "Typing indicators are disabled for the current user."
        verifyGenericError(result, message)
        verifyNoInteractions(plugin)
    }

    @Test
    fun stopTypingError() = runTest {
        // given
        val user = randomUser()
        val channelType = randomString()
        val channelId = randomString()
        val parentId = randomString()
        val errorCode = positiveRandomInt()
        val plugin = mock<Plugin>()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenUser(user)
            .givenSendEventResult(RetroError<ChatEvent>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.stopTyping(channelType, channelId, parentId).await()
        // then
        verifyNetworkError(result, errorCode)
        val inOrder = Mockito.inOrder(plugin)
        inOrder.verify(plugin).onTypingEventPrecondition(any(), any(), any(), any(), any())
        inOrder.verify(plugin).onTypingEventRequest(any(), any(), any(), any(), any())
        inOrder.verify(plugin).onTypingEventResult(any(), any(), any(), any(), any(), any())
    }

    internal inner class Fixture {

        fun givenQueryChannelsResult(result: Call<List<Channel>>) = apply {
            whenever(api.queryChannels(any())).thenReturn(result)
        }

        fun givenQueryChannelResult(resultProvider: () -> Result<Channel>) = apply {
            whenever(api.queryChannel(any(), any(), any())) doAnswer {
                CoroutineCall(testCoroutines.scope) {
                    resultProvider()
                }
            }
        }

        fun givenShowChannelResult(result: Call<Unit>) = apply {
            whenever(api.showChannel(any(), any())).thenReturn(result)
        }

        fun givenDeleteChannelResult(result: Call<Channel>) = apply {
            whenever(api.deleteChannel(any(), any())).thenReturn(result)
        }

        fun givenHideChannelResult(result: Call<Unit>) = apply {
            whenever(api.hideChannel(any(), any(), any())).thenReturn(result)
        }

        fun givenTruncateChannelResult(result: Call<Channel>) = apply {
            whenever(api.truncateChannel(any(), any(), any())).thenReturn(result)
        }

        fun givenUpdateChannelResult(result: Call<Channel>) = apply {
            whenever(api.updateChannel(any(), any(), any(), any())).thenReturn(result)
        }

        fun givenUpdateChannelPartialResult(result: Call<Channel>) = apply {
            whenever(api.updateChannelPartial(any(), any(), any(), any())).thenReturn(result)
        }

        fun givenPartialUpdateMemberResult(result: Call<Member>) = apply {
            whenever(api.partialUpdateMember(any(), any(), any(), any(), any())).thenReturn(result)
        }

        fun givenEnableSlowModeResult(result: Call<Channel>) = apply {
            whenever(api.enableSlowMode(any(), any(), any())).thenReturn(result)
        }

        fun givenDisableSlowModeResult(result: Call<Channel>) = apply {
            whenever(api.disableSlowMode(any(), any())).thenReturn(result)
        }

        fun givenAcceptInviteResult(result: Call<Channel>) = apply {
            whenever(api.acceptInvite(any(), any(), any())).thenReturn(result)
        }

        fun givenRejectInviteResult(result: Call<Channel>) = apply {
            whenever(api.rejectInvite(any(), any())).thenReturn(result)
        }

        fun givenSendEventResult(result: Call<ChatEvent>) = apply {
            whenever(api.sendEvent(any(), any(), any(), any())).thenReturn(result)
        }

        fun givenMarkAllReadResult(result: Call<Unit>) = apply {
            whenever(api.markAllRead()).thenReturn(result)
        }

        fun givenMarkReadResult(result: Call<Unit>) = apply {
            whenever(api.markRead(any(), any(), any())).thenReturn(result)
        }

        fun givenMarkDeliveredResult(messageId: String, result: Boolean) = apply {
            wheneverBlocking { mockMessageReceiptManager.markMessageAsDelivered(messageId) }.thenReturn(result)
        }

        fun givenMarkDeliveredResult(messageId: String, exception: RuntimeException) = apply {
            wheneverBlocking { mockMessageReceiptManager.markMessageAsDelivered(messageId) }.thenThrow(exception)
        }

        fun givenMarkUnreadResult(result: Call<Unit>) = apply {
            whenever(api.markUnread(any(), any(), anyOrNull(), anyOrNull(), anyOrNull())).thenReturn(result)
        }

        fun givenMarkThreadReadResult(result: Call<Unit>) = apply {
            whenever(api.markThreadRead(any(), any(), any())).thenReturn(result)
        }

        fun givenStopWatchingResult(result: Call<Unit>) = apply {
            whenever(api.stopWatching(any(), any())).thenReturn(result)
        }

        fun givenAddMembersResult(result: Call<Channel>) = apply {
            whenever(
                api.addMembers(
                    any(),
                    any(),
                    any(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                ),
            ).thenReturn(result)
        }

        fun givenRemoveMembersResult(result: Call<Channel>) = apply {
            whenever(api.removeMembers(any(), any(), any(), any(), any())).thenReturn(result)
        }

        fun givenInviteMembersResult(result: Call<Channel>) = apply {
            whenever(api.inviteMembers(any(), any(), any(), any(), any())).thenReturn(result)
        }

        fun givenUser(user: User) = apply {
            whenever(userStateService.state) doReturn UserState.UserSet(user)
            whenever(mutableClientState.user) doReturn MutableStateFlow(user)
        }

        fun givenPlugin(plugin: Plugin) = apply {
            plugins.add(plugin)
        }

        fun get(): ChatClient = chatClient
    }
}
