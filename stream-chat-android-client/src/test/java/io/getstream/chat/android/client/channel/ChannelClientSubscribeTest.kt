package io.getstream.chat.android.client.channel

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.ChatEventListener
import io.getstream.chat.android.client.events.ChannelCreatedEvent
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
        val CHANNEL_EVENT = ChannelCreatedEvent(
            EventType.CHANNEL_CREATED,
            Date(),
            CID,
            CHANNEL_TYPE,
            CHANNEL_ID,
            User(),
            null,
            Channel()
        )
        val OTHER_CHANNEL_EVENT = NewMessageEvent(
            EventType.CHANNEL_CREATED,
            Date(),
            User(),
            OTHER_CID,
            OTHER_CHANNEL_TYPE,
            OTHER_CHANNEL_ID,
            Message(),
            null,
            null,
            null
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
