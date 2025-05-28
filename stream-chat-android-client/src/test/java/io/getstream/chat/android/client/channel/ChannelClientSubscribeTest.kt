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
import io.getstream.chat.android.client.parser2.adapters.internal.StreamDateFormatter
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
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

        val streamDateFormatter = StreamDateFormatter()

        val createdAt = Date()
        val rawCreatedAt = streamDateFormatter.format(createdAt)

        val NON_CHANNEL_EVENT = ConnectedEvent(EventType.HEALTH_CHECK, createdAt, rawCreatedAt, User(), "")
        val CHANNEL_EVENT = ChannelUpdatedEvent(
            type = EventType.CHANNEL_UPDATED,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt,
            cid = CID,
            channelType = CHANNEL_TYPE,
            channelId = CHANNEL_ID,
            channel = Channel(),
            message = null,
        )
        val OTHER_CHANNEL_EVENT = NewMessageEvent(
            type = EventType.MESSAGE_NEW,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt,
            user = User(),
            cid = OTHER_CID,
            channelType = OTHER_CHANNEL_TYPE,
            channelId = OTHER_CHANNEL_ID,
            message = Message(),
            watcherCount = 0,
            totalUnreadCount = 0,
            unreadChannels = 0,
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
            client,
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
