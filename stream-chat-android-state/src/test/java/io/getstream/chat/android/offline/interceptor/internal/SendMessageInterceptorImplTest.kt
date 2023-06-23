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

import io.getstream.chat.android.client.interceptor.message.PrepareMessageLogic
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.test.randomChannel
import io.getstream.chat.android.client.test.randomMessage
import io.getstream.chat.android.client.test.randomUser
import io.getstream.chat.android.offline.model.message.attachments.UploadAttachmentsNetworkType
import io.getstream.chat.android.offline.plugin.logic.channel.internal.ChannelLogic
import io.getstream.chat.android.offline.plugin.logic.channel.thread.internal.ThreadLogic
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be`
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
internal class SendMessageInterceptorImplTest {
    private val clientState: ClientState = mock {
        on(it.user) doReturn MutableStateFlow(randomUser())
    }

    private val channelLogic: ChannelLogic = mock {
        on(it.toChannel()) doReturn randomChannel()
    }

    private val threadLogic: ThreadLogic = mock {}

    private val logic: LogicRegistry = mock {
        on(it.channel(any(), any())) doReturn channelLogic
        on(it.thread(any())) doReturn threadLogic
    }

    private val prepareMessage: PrepareMessageLogic = mock {
        on(it.prepareMessage(any(), any(), any(), any())) doAnswer { invocationOnMock ->
            invocationOnMock.arguments[0] as Message
        }
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
        prepareMessage,
        randomUser()
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

    @Test
    fun `when send message in thread with show in channel, message should be added to channelState and threadState`() = runTest {
        val messageToSend = randomMessage(text = randomString(), parentId = randomString(), showInChannel = true)

        whenever(logic.channelFromMessage(messageToSend)) doReturn channelLogic
        whenever(logic.threadFromMessage(messageToSend)) doReturn threadLogic

        val result = sendMessageInterceptorImpl.interceptMessage(
            randomString(),
            randomString(),
            messageToSend,
            isRetrying = false
        )

        result.isSuccess `should be` true

        val resultMessage = result.data()

        verify(threadLogic).upsertMessage(
            argThat {
                this.id == resultMessage.id &&
                    this.parentId == resultMessage.parentId &&
                    this.text == resultMessage.text
            }
        )
        verify(channelLogic).upsertMessage(
            argThat {
                this.id == resultMessage.id &&
                    this.parentId == resultMessage.parentId &&
                    this.text == resultMessage.text
            }
        )
    }

    @Test
    fun `when send message in thread without show in channel, message should be added to threadState`() = runTest {
        val messageToSend = randomMessage(text = randomString(), parentId = randomString(), showInChannel = false)

        logic.channelFromMessage(messageToSend) `should be` null

        whenever(logic.threadFromMessage(messageToSend)) doReturn threadLogic

        val result = sendMessageInterceptorImpl.interceptMessage(
            randomString(),
            randomString(),
            messageToSend,
            isRetrying = false
        )

        result.isSuccess `should be` true

        val resultMessage = result.data()

        verify(threadLogic).upsertMessage(
            argThat {
                this.id == resultMessage.id &&
                    this.parentId == resultMessage.parentId &&
                    this.text == resultMessage.text
            }
        )
    }

    @Test
    fun `when send message in channel, message should be added to channelState`() = runTest {
        val messageToSend = randomMessage(text = randomString(), parentId = null)

        whenever(logic.channelFromMessage(messageToSend)) doReturn channelLogic

        logic.threadFromMessage(messageToSend) `should be` null

        val result = sendMessageInterceptorImpl.interceptMessage(
            randomString(),
            randomString(),
            messageToSend,
            isRetrying = false
        )

        result.isSuccess `should be` true

        val resultMessage = result.data()

        verify(channelLogic).upsertMessage(
            argThat {
                this.id == resultMessage.id &&
                    this.parentId == resultMessage.parentId &&
                    this.text == resultMessage.text
            }
        )
    }

    @Test
    fun `when send message with non-File attachment, result must be Successful`() = runTest {
        val locationAttachment = Attachment(
            type = "location",
            extraData = mutableMapOf("latitude" to 1.0, "longitude" to 2.0),
            uploadState = Attachment.UploadState.Success,
        )
        val messageToSend = randomMessage(
            parentId = null,
            text = randomString(),
            attachments = arrayListOf(locationAttachment),
        )

        whenever(logic.channelFromMessage(messageToSend)) doReturn channelLogic

        val result = sendMessageInterceptorImpl.interceptMessage(
            randomString(),
            randomString(),
            messageToSend,
            isRetrying = false
        )

        result.isSuccess `should be` true

        val resultMessage = result.data()

        verify(channelLogic).upsertMessage(
            argThat {
                this.id == resultMessage.id &&
                    this.parentId == resultMessage.parentId &&
                    this.text == resultMessage.text
            }
        )
    }
}
