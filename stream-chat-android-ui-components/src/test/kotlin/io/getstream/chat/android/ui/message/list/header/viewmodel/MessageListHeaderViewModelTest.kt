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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.validateMockitoUsage
import org.mockito.kotlin.whenever
import java.util.Date

private const val CID = "messaging:CID"
private const val CHANNEL_TYPE = "messaging"
private const val CHANNEL_ID = "CID"
private const val CHANNEL_NAME = "channel"

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class)
internal class MessageListHeaderViewModelTest {

    private val members = createMembers { createMember(User(name = "user")) }

    private val membersStateFlow = MutableStateFlow(createMembers())
    private val channelDataStateFlow = MutableStateFlow(ChannelData(Channel()))

    private val channelState: ChannelState = spy {
        whenever(it.members).doAnswer { membersStateFlow }
        whenever(it.channelData).doAnswer { channelDataStateFlow }
        whenever(it.toChannel()).doAnswer { returnChannel() }
    }

    private val stateRegistry: StateRegistry = mock {
        whenever(it.channel(any(), any())) doReturn channelState
    }
    private val chatClient: ChatClient = mock {
        whenever(it.queryChannel(any(), any(), any())) doReturn Channel().asCall()
    }

    init {
        StateRegistry.instance = stateRegistry
    }

    @AfterEach
    fun validate(){
        validateMockitoUsage()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `When channel members update should update channel`() = runBlockingTest {
        // given
        val messageListHeaderViewModel = MessageListHeaderViewModel(CID, chatClient = chatClient)
        var result: Channel? = null
        launch {
            result = messageListHeaderViewModel._channel.take(2).toList().last()
        }

        // when
        membersStateFlow.emit(members)
        advanceUntilIdle()
        val expectedValue = channelDataStateFlow.value
            .toChannel()
            .apply {
                members = this@MessageListHeaderViewModelTest.members
            }

        // should
        assertEquals(actual = result, expected = expectedValue)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `When channel data updates should update channel`() = runBlockingTest {
        // given
        val messageListHeaderViewModel = MessageListHeaderViewModel(CID, chatClient = chatClient)
        var result: Channel? = null
        launch {
            result = messageListHeaderViewModel._channel.take(2).toList().last()
        }

        // when
        val channel = Channel(cid = CID, id = CHANNEL_ID, type = CHANNEL_TYPE, name = CHANNEL_NAME)
        val channelData = ChannelData(channel)
        channelDataStateFlow.emit(channelData)
        advanceUntilIdle()
        val expectedValue = channel.apply {
            members = membersStateFlow.value
        }

        // should
        assertEquals(actual = result, expected = expectedValue)
    }

    private fun returnChannel(): Channel {
        return channelDataStateFlow.value
            .toChannel()
            .apply { members = membersStateFlow.value }
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
