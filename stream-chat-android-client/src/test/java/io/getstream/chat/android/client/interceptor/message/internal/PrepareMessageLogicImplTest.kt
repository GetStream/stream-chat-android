/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.interceptor.message.internal

import io.getstream.chat.android.client.channel.ChannelMessagesUpdateLogic
import io.getstream.chat.android.client.channel.state.ChannelStateLogicProvider
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.randomAttachment
import io.getstream.chat.android.randomFile
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should not be equal to`
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class PrepareMessageLogicImplTest {

    private val channelMessagesUpdateLogic: ChannelMessagesUpdateLogic = mock()

    private val clientState: ClientState = mock()
    private val logic: ChannelStateLogicProvider = mock {
        on(it.channelStateLogic(any(), any())) doReturn channelMessagesUpdateLogic
    }
    private val prepareMessageInterceptorImpl = PrepareMessageLogicImpl(clientState, logic)

    @Test
    fun `given a message has attachments, the status should be updated accordingly`() {
        val attachment: Attachment = randomAttachment(upload = randomFile())
        val messageWithAttachments = randomMessage(
            attachments = mutableListOf(attachment),
            syncStatus = SyncStatus.SYNC_NEEDED,
        )

        val preparedMessage = prepareMessageInterceptorImpl.prepareMessage(
            messageWithAttachments,
            randomString(),
            randomString(),
            randomUser(),
        )

        preparedMessage.syncStatus `should be equal to` SyncStatus.AWAITING_ATTACHMENTS
    }

    @Test
    fun `given message is prepared, it should always have id, user, cid, type, createdLocallyAt and syncStatus`() {
        val newUser = randomUser()
        val attachment: Attachment = randomAttachment().copy(upload = randomFile())
        val messageWithAttachments = randomMessage(
            attachments = mutableListOf(attachment),
            syncStatus = SyncStatus.SYNC_NEEDED,
            id = "",
            cid = "",
            user = randomUser(),
            type = "",
            createdAt = null,
        )

        val preparedMessage = prepareMessageInterceptorImpl.prepareMessage(
            messageWithAttachments,
            randomString(),
            randomString(),
            newUser,
        )

        preparedMessage.run {
            id `should not be equal to` ""
            cid `should not be equal to` ""
            user `should be equal to` newUser
            type `should not be equal to` ""
            createdLocallyAt `should not be equal to` null
            syncStatus `should be equal to` SyncStatus.AWAITING_ATTACHMENTS
        }
    }

    @Test
    fun `given a message doesn't have attachments and user is online, the status should be updated accordingly`() {
        whenever(clientState.isNetworkAvailable) doReturn true

        val messageWithAttachments = randomMessage(
            attachments = mutableListOf(),
            syncStatus = SyncStatus.SYNC_NEEDED,
        )

        val preparedMessage = prepareMessageInterceptorImpl.prepareMessage(
            messageWithAttachments,
            randomString(),
            randomString(),
            randomUser(),
        )

        preparedMessage.syncStatus `should be equal to` SyncStatus.IN_PROGRESS
    }

    @Test
    fun `given a message doesn't have attachments and user is offline, the status should be updated accordingly`() {
        whenever(clientState.isNetworkAvailable) doReturn false

        val messageWithAttachments = randomMessage(
            attachments = mutableListOf(),
            syncStatus = SyncStatus.SYNC_NEEDED,
        )

        val preparedMessage = prepareMessageInterceptorImpl.prepareMessage(
            messageWithAttachments,
            randomString(),
            randomString(),
            randomUser(),
        )

        preparedMessage.syncStatus `should be equal to` SyncStatus.SYNC_NEEDED
    }

    @Test
    fun `given message id and cid is empty, they should be generated`() {
        val messageWithAttachments = randomMessage(
            cid = "",
            id = "",
        )

        val preparedMessage = prepareMessageInterceptorImpl.prepareMessage(
            messageWithAttachments,
            randomString(),
            randomString(),
            randomUser(),
        )

        preparedMessage.cid `should not be equal to` ""
        preparedMessage.id `should not be equal to` ""
    }

    @Test
    fun `given message's attachment upload id is empty, it should be generated`() {
        val attachment = randomAttachment().copy(upload = randomFile())
        val messageWithAttachments = randomMessage(
            attachments = mutableListOf(attachment),
        )

        println("initialAttachments: ${messageWithAttachments.attachments}")

        val result = prepareMessageInterceptorImpl.prepareMessage(
            messageWithAttachments,
            randomString(),
            randomString(),
            randomUser(),
        )

        println("resultAttachments: ${result.attachments}")

        result.attachments.first().uploadId `should not be equal to` null
    }
}
