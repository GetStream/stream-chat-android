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

package io.getstream.chat.android.offline.interceptor.internal

import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.test.randomChannel
import io.getstream.chat.android.client.test.randomMessage
import io.getstream.chat.android.client.test.randomUser
import io.getstream.chat.android.offline.model.message.attachments.UploadAttachmentsNetworkType
import io.getstream.chat.android.offline.plugin.logic.channel.internal.ChannelLogic
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be`
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
internal class SendMessageInterceptorImplTest {
    private val clientState: ClientState = mock {
        on(it.user) doReturn MutableStateFlow(randomUser())
    }

    private val channelLogic: ChannelLogic = mock {
        on(it.toChannel()) doReturn randomChannel()
    }

    private val logic: LogicRegistry = mock {
        on(it.channel(any(), any())) doReturn channelLogic
    }

    private val sendMessageInterceptorImpl = SendMessageInterceptorImpl(
        context = mock(),
        logic = logic,
        clientState = clientState,
        channelRepository = mock(),
        messageRepository = mock(),
        attachmentRepository = mock(),
        scope = TestScope(),
        networkType = UploadAttachmentsNetworkType.NOT_ROAMING,
    )

    @Test
    fun `when socket is not connected and there is no attachments result should be success`() = runTest {
        val result = sendMessageInterceptorImpl.interceptMessage(
            randomString(),
            randomString(),
            randomMessage(attachments = mutableListOf()),
            isRetrying = false
        )

        result.isSuccess `should be` true
    }
}
