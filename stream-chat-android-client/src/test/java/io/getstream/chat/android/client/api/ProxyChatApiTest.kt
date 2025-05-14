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

package io.getstream.chat.android.client.api

import io.getstream.chat.android.client.interceptor.SendMessageInterceptor
import io.getstream.chat.android.client.scope.ClientScope
import io.getstream.chat.android.client.scope.UserScope
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalStreamChatApi::class)
internal class ProxyChatApiTest {

    @Test
    fun `sendMessage without interceptor delegates call to original api`() = runTest {
        // Given
        val channelType = randomString()
        val channelId = randomString()
        val message = randomMessage()
        val originalApi = mock<ChatApi>()
        whenever(originalApi.sendMessage(any(), any(), any()))
            .doReturn(RetroSuccess(message).toRetrofitCall())
        val proxyApi = ProxyChatApi(originalApi, UserScope(ClientScope()), null)
        // When
        proxyApi.sendMessage(channelType, channelId, message).await()
        // Then
        verify(originalApi).sendMessage(channelType, channelId, message)
    }

    @Test
    fun `sendMessage with interceptor calls interceptor`() = runTest {
        // Given
        val channelType = randomString()
        val channelId = randomString()
        val message = randomMessage()
        val originalApi = mock<ChatApi>()
        val sendMessageInterceptor = mock<SendMessageInterceptor>()
        val proxyApi = ProxyChatApi(originalApi, UserScope(ClientScope()), sendMessageInterceptor)
        // When
        proxyApi.sendMessage(channelType, channelId, message).await()
        // Then
        verify(originalApi, never()).sendMessage(any(), any(), any())
        verify(sendMessageInterceptor).sendMessage(channelType, channelId, message)
    }
}
