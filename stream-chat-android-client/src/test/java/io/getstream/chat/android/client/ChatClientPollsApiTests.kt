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
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.Vote
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
        whenever(api.suggestPollOption(any(), any()))
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
        whenever(api.suggestPollOption(any(), any()))
            .thenReturn(RetroError<Option>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.suggestPollOption(pollId, option).await()
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
}
