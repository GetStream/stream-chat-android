package io.getstream.chat.android.ui.message.list.header.viewmodel

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.model.channel.ChannelData
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import io.getstream.chat.android.test.InstantTaskExecutorExtension
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.ui.createMember
import io.getstream.chat.android.ui.createMembers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.internal.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import java.util.Date

private const val CID = "messaging:CID"
private const val CHANNEL_TYPE = "messaging"
private const val CHANNEL_ID = "CID"
private const val CHANNEL_NAME = "channel"

@ExtendWith(InstantTaskExecutorExtension::class)
internal class MessageListHeaderViewModelTest {

    private val membersMock = MutableStateFlow(createMembers())
    private val channelDataMock = MutableStateFlow(ChannelData(Channel()))

    private val channelState: ChannelState = spy {
        whenever(it.members).doAnswer { membersMock }
        whenever(it.channelData).doAnswer { channelDataMock }
        whenever(it.toChannel()).doAnswer { returnChannel() }
    }

    private val stateRegistry: StateRegistry = mock {
        whenever(it.channel(any(), any())) doReturn channelState
    }
    private val chatClient: ChatClient = mock {
        whenever(it.queryChannel(any(), any(), any())) doReturn Channel().asCall()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    private fun setup() {
        StateRegistry.instance = stateRegistry
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `When channel members update should update channel`() = runBlockingTest {
        val messageListHeaderViewModel = MessageListHeaderViewModel(CID, chatClient = chatClient)

        var result: Channel? = null

        launch {
            result = messageListHeaderViewModel._channel.take(2).toList().last()
        }

        val memberList = createMembers { createMember(User(name = "user")) }
        membersMock.emit(memberList)
        advanceUntilIdle()

        val expectedValue = channelDataMock.value
            .toChannel()
            .apply {
                members = memberList
            }

        assertEquals(actual = result, expected = expectedValue)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `When channel data updates should update channel`() = runBlockingTest {
        val messageListHeaderViewModel = MessageListHeaderViewModel(CID, chatClient = chatClient)

        var result: Channel? = null

        launch {
            result = messageListHeaderViewModel._channel.take(2).toList().last()
        }

        val channel = Channel(cid = CID, id = CHANNEL_ID, type = CHANNEL_TYPE, name = CHANNEL_NAME)
        val channelData = ChannelData(channel)
        channelDataMock.emit(channelData)
        advanceUntilIdle()

        val expectedValue = channel.apply {
            members = membersMock.value
        }

        assertEquals(actual = result, expected = expectedValue)
    }

    private fun returnChannel(): Channel {
        return channelDataMock.value
            .toChannel()
            .apply { members = membersMock.value }
    }

    private fun ChannelData.toChannel(
        id: String = this.channelId,
        type: String = this.type,
        cid: String = this.cid,
        name: String = this.name,
        image: String = this.image,
        createdBy: User = this.createdBy,
        cooldown: Int = this.cooldown,
        frozen: Boolean = this.frozen,
        createdAt: Date? = this.createdAt,
        updatedAt: Date? = this.updatedAt,
        deletedAt: Date? = this.deletedAt,
        memberCount: Int = this.memberCount,
        team: String = this.team,
        extraData: MutableMap<String, Any> = this.extraData,
    ): Channel {
        return Channel(
            id = id,
            type = type,
            cid = cid,
            name = name,
            image = image,
            createdBy = createdBy,
            cooldown = cooldown,
            frozen = frozen,
            createdAt = createdAt,
            updatedAt = updatedAt,
            deletedAt = deletedAt,
            memberCount = memberCount,
            team = team,
            extraData = extraData
        )
    }
}
