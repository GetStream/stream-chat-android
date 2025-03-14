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
import io.getstream.chat.android.models.VideoCallInfo
import io.getstream.chat.android.models.VideoCallToken
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomVideoCallInfo
import io.getstream.chat.android.randomVideoCallToken
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

/**
 * Tests covering the calls functionality of the [ChatClient].
 */
internal class ChatClientCallApiTests : BaseChatClientTest() {

    @Test
    fun createVideoCallSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val callType = randomString()
        val callId = randomString()
        val videoCallInfo = randomVideoCallInfo()
        whenever(api.createVideoCall(any(), any(), any(), any()))
            .thenReturn(RetroSuccess(videoCallInfo).toRetrofitCall())
        // when
        val result = chatClient.createVideoCall(channelType, channelId, callType, callId).await()
        // then
        verifySuccess(result, videoCallInfo)
    }

    @Test
    fun createVideoCallError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val callType = randomString()
        val callId = randomString()
        val statusCode = positiveRandomInt()
        whenever(api.createVideoCall(any(), any(), any(), any()))
            .thenReturn(RetroError<VideoCallInfo>(statusCode).toRetrofitCall())
        // when
        val result = chatClient.createVideoCall(channelType, channelId, callType, callId).await()
        // then
        verifyNetworkError(result, statusCode)
    }

    @Test
    fun getVideoCallTokenSuccess() = runTest {
        // given
        val callId = randomString()
        val videoCallToken = randomVideoCallToken()
        whenever(api.getVideoCallToken(any()))
            .thenReturn(RetroSuccess(videoCallToken).toRetrofitCall())
        // when
        val result = chatClient.getVideoCallToken(callId).await()
        // then
        verifySuccess(result, videoCallToken)
    }

    @Test
    fun getVideoCallTokenError() = runTest {
        // given
        val callId = randomString()
        val statusCode = positiveRandomInt()
        whenever(api.getVideoCallToken(any()))
            .thenReturn(RetroError<VideoCallToken>(statusCode).toRetrofitCall())
        // when
        val result = chatClient.getVideoCallToken(callId).await()
        // then
        verifyNetworkError(result, statusCode)
    }
}
