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

package io.getstream.chat.android.offline.plugin.logic.channel.internal

import io.getstream.chat.android.client.attachments.AttachmentUrlValidator
import io.getstream.chat.android.client.channel.state.ChannelMutableState
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.offline.randomMessage
import io.getstream.chat.android.test.randomDate
import io.getstream.chat.android.test.randomDateAfter
import io.getstream.chat.android.test.randomDateBefore
import kotlinx.coroutines.flow.MutableStateFlow
import org.amshove.kluent.`should not be equal to`
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class ChannelStateLogicImplTest {

    private val _messages: MutableStateFlow<Map<String, Message>> = MutableStateFlow(emptyMap())
    private val mutableState: ChannelMutableState = mock {
        on(it._messages) doReturn _messages
    }
    private val globalMutableState: MutableGlobalState = mock()
    private val attachmentUrlValidator: AttachmentUrlValidator = mock {
        on(it.updateValidAttachmentsUrl(any(), any())) doReturn emptyList()
    }

    private val channelStateLogicImpl = ChannelStateLogicImpl(mutableState, globalMutableState, attachmentUrlValidator)

    @Test
    fun `given a message is outdated it should not be upserted`() {
        val createdAt = randomDate()
        val createdLocallyAt = randomDateBefore(createdAt.time)
        val updatedAt = randomDateAfter(createdAt.time)
        val oldUpdatedAt = randomDateBefore(updatedAt.time)

        val recentMessage = randomMessage(
            user = User(id = "otherUserId"),
            createdAt = createdAt,
            createdLocallyAt = createdLocallyAt,
            updatedAt = updatedAt,
            updatedLocallyAt = updatedAt,
            deletedAt = null,
            silent = false,
            showInChannel = true
        )
        val oldMessage = randomMessage(
            user = User(id = "otherUserId"),
            createdAt = createdAt,
            createdLocallyAt = createdLocallyAt,
            updatedAt = oldUpdatedAt,
            updatedLocallyAt = oldUpdatedAt,
            deletedAt = null,
            silent = false,
            showInChannel = true
        )

        channelStateLogicImpl.upsertMessage(recentMessage)
        channelStateLogicImpl.upsertMessage(oldMessage)

        // Only the new message is available
        _messages.value `should not be equal to` mapOf(recentMessage.id to recentMessage)
    }
}
