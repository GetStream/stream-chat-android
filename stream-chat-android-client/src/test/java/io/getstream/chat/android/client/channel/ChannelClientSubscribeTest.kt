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

package io.getstream.chat.android.client.channel

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.ChatEventListener
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.util.Date

internal class ChannelClientSubscribeTest {
    private companion object {
        const val CHANNEL_TYPE = "messaging"
        const val CHANNEL_ID = "channelId"
        const val CID = "$CHANNEL_TYPE:$CHANNEL_ID"

        const val OTHER_CHANNEL_TYPE = "livestream"
        const val OTHER_CHANNEL_ID = "my-game"
        const val OTHER_CID = "$OTHER_CHANNEL_TYPE:$OTHER_CHANNEL_ID"

        val NON_CHANNEL_EVENT = ConnectedEvent(EventType.HEALTH_CHECK, Date(), User(), "")
        val CHANNEL_EVENT = ChannelUpdatedEvent(
            EventType.CHANNEL_UPDATED,
            Date(),
            CID,
            CHANNEL_TYPE,
            CHANNEL_ID,
            null,
            Channel(),
        )
        val OTHER_CHANNEL_EVENT = NewMessageEvent(
            EventType.MESSAGE_NEW,
            Date(),
            User(),
            OTHER_CID,
            OTHER_CHANNEL_TYPE,
            OTHER_CHANNEL_ID,
            Message(),
            0,
            0,
            0
        )
    }

    private lateinit var client: ChatClient
    private lateinit var channelClient: ChannelClient

    private lateinit var result: MutableList<ChatEvent>

    @BeforeEach
    fun setUp() {
        client = mock()
        channelClient = ChannelClient(
            CHANNEL_TYPE,
            CHANNEL_ID,
            client
        )
        result = mutableListOf()
    }

    @Test
    fun `When subscribing to channel events Then only the events of the given channel should be delivered`() {
        channelClient.subscribe {
            result.add(it)
        }
        val captor = argumentCaptor<ChatEventListener<ChatEvent>>()
        verify(client).subscribe(captor.capture())
        val listener = captor.firstValue

        listener.onEvent(CHANNEL_EVENT)
        listener.onEvent(NON_CHANNEL_EVENT)
        listener.onEvent(OTHER_CHANNEL_EVENT)

        result shouldBeEqualTo listOf(CHANNEL_EVENT)
    }
}
