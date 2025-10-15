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

package io.getstream.chat.android.client

import io.getstream.chat.android.client.chatclient.BaseChatClientTest
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.client.utils.verifyNetworkError
import io.getstream.chat.android.client.utils.verifySuccess
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.QueryPollsResult
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomPoll
import io.getstream.chat.android.randomPollConfig
import io.getstream.chat.android.randomPollOption
import io.getstream.chat.android.randomPollVote
import io.getstream.chat.android.randomString
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Tests for the Poll functionality of the [ChatClient].
 */
internal class ChatClientPollsApiTests : BaseChatClientTest() {

    @Test
    fun sendPollSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val pollConfig = randomPollConfig()
        val poll = randomPoll()
        val message = randomMessage(poll = poll)
        whenever(api.createPoll(any()))
            .thenReturn(RetroSuccess(poll).toRetrofitCall())
        whenever(attachmentsSender.sendAttachments(any(), any(), any(), any()))
            .thenReturn(Result.Success(message))
        whenever(api.sendMessage(any(), any(), any()))
            .thenReturn(RetroSuccess(message).toRetrofitCall())
        // when
        val result = chatClient.sendPoll(channelType, channelId, pollConfig).await()
        // then
        verifySuccess(result, message)
    }

    @Test
    fun sendPollError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val pollConfig = randomPollConfig()
        val errorCode = positiveRandomInt()
        whenever(api.createPoll(any()))
            .thenReturn(RetroError<Poll>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.sendPoll(channelType, channelId, pollConfig).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun getPollSuccess() = runTest {
        // given
        val pollId = randomString()
        val poll = randomPoll()
        whenever(api.getPoll(any()))
            .thenReturn(RetroSuccess(poll).toRetrofitCall())
        // when
        val result = chatClient.getPoll(pollId).await()
        // then
        verifySuccess(result, poll)
    }

    @Test
    fun getPollError() = runTest {
        // given
        val pollId = randomString()
        val errorCode = positiveRandomInt()
        whenever(api.getPoll(any()))
            .thenReturn(RetroError<Poll>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.getPoll(pollId).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun suggestPollOptionSuccess() = runTest {
        // given
        val pollId = randomString()
        val option = randomString()
        val resultOption = randomPollOption()
        whenever(api.createPollOption(any(), any()))
            .thenReturn(RetroSuccess(resultOption).toRetrofitCall())
        // when
        val result = chatClient.suggestPollOption(pollId, option).await()
        // then
        assert(result.isSuccess)
    }

    @Test
    fun suggestPollOptionError() = runTest {
        // given
        val pollId = randomString()
        val option = randomString()
        val errorCode = positiveRandomInt()
        whenever(api.createPollOption(any(), any()))
            .thenReturn(RetroError<Option>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.suggestPollOption(pollId, option).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun createPollOptionSuccess() = runTest {
        // given
        val pollId = randomString()
        val option = Mother.randomCreatePollOptionRequest()
        val resultOption = randomPollOption()
        whenever(api.createPollOption(any(), any()))
            .thenReturn(RetroSuccess(resultOption).toRetrofitCall())
        // when
        val result = chatClient.createPollOption(pollId, option).await()
        // then
        assert(result.isSuccess)
    }

    @Test
    fun createPollOptionError() = runTest {
        // given
        val pollId = randomString()
        val option = Mother.randomCreatePollOptionRequest()
        val errorCode = positiveRandomInt()
        whenever(api.createPollOption(any(), any()))
            .thenReturn(RetroError<Option>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.createPollOption(pollId, option).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun updatePollOptionSuccess() = runTest {
        // given
        val pollId = randomString()
        val option = Mother.randomUpdatePollOptionRequest()
        val resultOption = randomPollOption()
        whenever(api.updatePollOption(any(), any()))
            .thenReturn(RetroSuccess(resultOption).toRetrofitCall())
        // when
        val result = chatClient.updatePollOption(pollId, option).await()
        // then
        assert(result.isSuccess)
    }

    @Test
    fun updatePollOptionError() = runTest {
        // given
        val pollId = randomString()
        val option = Mother.randomUpdatePollOptionRequest()
        val errorCode = positiveRandomInt()
        whenever(api.updatePollOption(any(), any()))
            .thenReturn(RetroError<Option>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.updatePollOption(pollId, option).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun castPollVoteSuccess() = runTest {
        // given
        val messageId = randomString()
        val pollId = randomString()
        val option = randomPollOption()
        val resultVote = randomPollVote()
        whenever(api.castPollVote(any(), any(), any()))
            .thenReturn(RetroSuccess(resultVote).toRetrofitCall())
        // when
        val result = chatClient.castPollVote(messageId, pollId, option).await()
        // then
        assert(result.isSuccess)
    }

    @Test
    fun castPollVoteError() = runTest {
        // given
        val messageId = randomString()
        val pollId = randomString()
        val option = randomPollOption()
        val errorCode = positiveRandomInt()
        whenever(api.castPollVote(any(), any(), any()))
            .thenReturn(RetroError<Vote>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.castPollVote(messageId, pollId, option).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun castPollAnswerSuccess() = runTest {
        // given
        val messageId = randomString()
        val pollId = randomString()
        val answer = randomString()
        val vote = randomPollVote()
        whenever(api.castPollAnswer(any(), any(), any()))
            .thenReturn(RetroSuccess(vote).toRetrofitCall())
        // when
        val result = chatClient.castPollAnswer(messageId, pollId, answer).await()
        // then
        verifySuccess(result, vote)
    }

    @Test
    fun castPollAnswerError() = runTest {
        // given
        val messageId = randomString()
        val pollId = randomString()
        val answer = randomString()
        val errorCode = positiveRandomInt()
        whenever(api.castPollAnswer(any(), any(), any()))
            .thenReturn(RetroError<Vote>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.castPollAnswer(messageId, pollId, answer).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun removePollVoteSuccess() = runTest {
        // given
        val messageId = randomString()
        val pollId = randomString()
        val vote = randomPollVote()
        whenever(api.removePollVote(any(), any(), any()))
            .thenReturn(RetroSuccess(vote).toRetrofitCall())
        // when
        val result = chatClient.removePollVote(messageId, pollId, vote).await()
        // then
        assert(result.isSuccess)
    }

    @Test
    fun removePollVoteError() = runTest {
        // given
        val messageId = randomString()
        val pollId = randomString()
        val vote = randomPollVote()
        val errorCode = positiveRandomInt()
        whenever(api.removePollVote(any(), any(), any()))
            .thenReturn(RetroError<Vote>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.removePollVote(messageId, pollId, vote).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun partialUpdatePollSuccess() = runTest {
        // given
        val pollId = randomString()
        val set = mapOf("name" to "New poll name")
        val unset = listOf("custom_property")
        val poll = randomPoll()
        whenever(api.partialUpdatePoll(pollId, set, unset))
            .thenReturn(RetroSuccess(poll).toRetrofitCall())
        // when
        val result = chatClient.partialUpdatePoll(pollId, set, unset).await()
        // then
        verifySuccess(result, poll)
    }

    @Test
    fun partialUpdatePollError() = runTest {
        // given
        val pollId = randomString()
        val set = mapOf("name" to "New poll name")
        val unset = listOf("custom_property")
        val errorCode = positiveRandomInt()
        whenever(api.partialUpdatePoll(pollId, set, unset))
            .thenReturn(RetroError<Poll>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.partialUpdatePoll(pollId, set, unset).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun closePollSuccess() = runTest {
        // given
        val pollId = randomString()
        val poll = randomPoll(closed = true)
        whenever(api.closePoll(any()))
            .thenReturn(RetroSuccess(poll).toRetrofitCall())
        // when
        val result = chatClient.closePoll(pollId).await()
        // then
        verifySuccess(result, poll)
    }

    @Test
    fun closePollError() = runTest {
        // given
        val pollId = randomString()
        val errorCode = positiveRandomInt()
        whenever(api.closePoll(any()))
            .thenReturn(RetroError<Poll>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.closePoll(pollId).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun deletePollOptionSuccess() = runTest {
        // given
        val pollId = randomString()
        val optionId = randomString()
        whenever(api.deletePollOption(any(), any()))
            .thenReturn(RetroSuccess(Unit).toRetrofitCall())
        // when
        val result = chatClient.deletePollOption(pollId, optionId).await()
        // then
        verifySuccess(result, Unit)
    }

    @Test
    fun deletePollOptionError() = runTest {
        // given
        val pollId = randomString()
        val optionId = randomString()
        val errorCode = positiveRandomInt()
        whenever(api.deletePollOption(any(), any()))
            .thenReturn(RetroError<Unit>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.deletePollOption(pollId, optionId).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun deletePollSuccess() = runTest {
        // given
        val pollId = randomString()
        whenever(api.deletePoll(any()))
            .thenReturn(RetroSuccess(Unit).toRetrofitCall())
        // when
        val result = chatClient.deletePoll(pollId).await()
        // then
        verifySuccess(result, Unit)
    }

    @Test
    fun deletePollError() = runTest {
        // given
        val pollId = randomString()
        val errorCode = positiveRandomInt()
        whenever(api.deletePoll(any()))
            .thenReturn(RetroError<Unit>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.deletePoll(pollId).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun queryPollsSuccessWithAllParameters() = runTest {
        // given
        val filter = Filters.eq("is_closed", false)
        val limit = positiveRandomInt(10)
        val next = randomString()
        val sort = QuerySortByField.descByName<Poll>("created_at")
        val poll1 = randomPoll()
        val poll2 = randomPoll()
        val expectedResult = QueryPollsResult(
            polls = listOf(poll1, poll2),
            next = randomString(),
        )
        whenever(api.queryPolls(any(), any(), any(), any()))
            .thenReturn(RetroSuccess(expectedResult).toRetrofitCall())
        // when
        val result = chatClient.queryPolls(filter, limit, next, sort).await()
        // then
        verifySuccess(result, expectedResult)
        verify(api).queryPolls(eq(filter), eq(limit), eq(next), eq(sort))
    }

    @Test
    fun queryPollsSuccessWithMinimalParameters() = runTest {
        // given
        val expectedResult = QueryPollsResult(
            polls = listOf(randomPoll()),
            next = null,
        )
        whenever(api.queryPolls(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()))
            .thenReturn(RetroSuccess(expectedResult).toRetrofitCall())
        // when
        val result = chatClient.queryPolls().await()
        // then
        verifySuccess(result, expectedResult)
        verify(api).queryPolls(eq(null), eq(null), eq(null), eq(null))
    }

    @Test
    fun queryPollsError() = runTest {
        // given
        val filter = Filters.eq("is_closed", true)
        val limit = positiveRandomInt(10)
        val next = randomString()
        val sort = QuerySortByField.descByName<Poll>("updated_at")
        val errorCode = positiveRandomInt()
        whenever(api.queryPolls(any(), any(), any(), any()))
            .thenReturn(RetroError<QueryPollsResult>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.queryPolls(filter, limit, next, sort).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun queryPollVotesSuccessWithAllParameters() = runTest {
        // given
        val pollId = randomString()
        val filter = Filters.eq("user_id", randomString())
        val limit = positiveRandomInt(10)
        val next = randomString()
        val sort = QuerySortByField.descByName<Vote>("created_at")
        val vote1 = randomPollVote()
        val vote2 = randomPollVote()
        val expectedResult = Mother.randomQueryPollVotesResult(
            votes = listOf(vote1, vote2),
            next = randomString(),
        )
        whenever(api.queryPollVotes(any(), any(), any(), any(), any()))
            .thenReturn(RetroSuccess(expectedResult).toRetrofitCall())
        // when
        val result = chatClient.queryPollVotes(pollId, filter, limit, next, sort).await()
        // then
        verifySuccess(result, expectedResult)
        verify(api).queryPollVotes(eq(pollId), eq(filter), eq(limit), eq(next), eq(sort))
    }

    @Test
    fun queryPollVotesSuccessWithMinimalParameters() = runTest {
        // given
        val pollId = randomString()
        val expectedResult = Mother.randomQueryPollVotesResult(
            votes = listOf(randomPollVote()),
            next = null,
        )
        whenever(api.queryPollVotes(any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()))
            .thenReturn(RetroSuccess(expectedResult).toRetrofitCall())
        // when
        val result = chatClient.queryPollVotes(pollId).await()
        // then
        verifySuccess(result, expectedResult)
        verify(api).queryPollVotes(eq(pollId), eq(null), eq(null), eq(null), eq(null))
    }

    @Test
    fun queryPollVotesError() = runTest {
        // given
        val pollId = randomString()
        val filter = Filters.eq("option_id", randomString())
        val limit = positiveRandomInt(10)
        val next = randomString()
        val sort = QuerySortByField.descByName<Vote>("updated_at")
        val errorCode = positiveRandomInt()
        whenever(api.queryPollVotes(any(), any(), any(), any(), any()))
            .thenReturn(RetroError<io.getstream.chat.android.models.QueryPollVotesResult>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.queryPollVotes(pollId, filter, limit, next, sort).await()
        // then
        verifyNetworkError(result, errorCode)
    }
}
