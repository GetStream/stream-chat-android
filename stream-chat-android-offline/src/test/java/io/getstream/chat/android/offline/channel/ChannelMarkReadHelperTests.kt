package io.getstream.chat.android.offline.channel

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.internal.utils.ChannelMarkReadHelper
import io.getstream.chat.android.offline.plugin.internal.logic.LogicRegistry
import io.getstream.chat.android.offline.plugin.internal.logic.channel.ChannelLogic
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.offline.plugin.state.channel.internal.ChannelMutableState
import io.getstream.chat.android.offline.plugin.state.global.GlobalState
import io.getstream.chat.android.offline.randomMessage
import io.getstream.chat.android.offline.randomUser
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.randomCID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

@ExperimentalCoroutinesApi
internal class ChannelMarkReadHelperTests {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    @Test
    fun `Given channel without read events When marking channel as read Should return false`() = runBlockingTest {
        val config = mock<Config> {
            on { it.readEventsEnabled } doReturn false
        }
        val (channelType, channelId) = randomCID().cidToTypeAndId()
        val channelMutableState = mock<ChannelMutableState> {
            on { it.channelConfig } doReturn MutableStateFlow(config)
        }
        val sut = Fixture()
            .givenChannelState(channelType = channelType, channelId = channelId, channelMutableState)
            .get()

        val result = sut.markChannelReadLocallyIfNeeded(channelType = channelType, channelId = channelId)

        result `should be equal to` false
    }

    @Test
    fun `Given channel without messages When marking channel as read Should return false`() = runBlockingTest {
        val config = mock<Config> {
            on { it.readEventsEnabled } doReturn true
        }
        val (channelType, channelId) = randomCID().cidToTypeAndId()
        val channelMutableState = mock<ChannelMutableState> {
            on { it.channelConfig } doReturn MutableStateFlow(config)
            on { it.sortedMessages } doReturn MutableStateFlow(emptyList())
        }
        val sut = Fixture()
            .givenChannelState(channelType = channelType, channelId = channelId, channelMutableState)
            .get()

        val result = sut.markChannelReadLocallyIfNeeded(channelType = channelType, channelId = channelId)

        result `should be equal to` false
    }

    @Test
    fun `Given read channel When marking channel as read Should return false`() = runBlockingTest {
        val config = mock<Config> {
            on { it.readEventsEnabled } doReturn true
        }
        val (channelType, channelId) = randomCID().cidToTypeAndId()
        val now = Date()
        val channelMutableState = mock<ChannelMutableState> {
            on { it.channelConfig } doReturn MutableStateFlow(config)
            on { it.sortedMessages } doReturn MutableStateFlow(listOf(randomMessage(createdAt = now)))
            on { it.lastMarkReadEvent } doReturn Date(now.time + 1000)
        }
        val sut = Fixture()
            .givenChannelState(channelType = channelType, channelId = channelId, channelMutableState)
            .get()

        val result = sut.markChannelReadLocallyIfNeeded(channelType = channelType, channelId = channelId)

        result `should be equal to` false
    }

    @Test
    fun `Given user not set When marking channel as read Should return false`() = runBlockingTest {
        val config = mock<Config> {
            on { it.readEventsEnabled } doReturn true
        }
        val (channelType, channelId) = randomCID().cidToTypeAndId()
        val channelMutableState = mock<ChannelMutableState> {
            on { it.channelConfig } doReturn MutableStateFlow(config)
            on { it.sortedMessages } doReturn MutableStateFlow(listOf(randomMessage()))
        }
        val sut = Fixture()
            .givenChannelState(channelType = channelType, channelId = channelId, channelMutableState)
            .get()

        val result = sut.markChannelReadLocallyIfNeeded(channelType = channelType, channelId = channelId)

        result `should be equal to` false
    }

    @Test
    fun `Given offline user When marking channel as read Should return false`() = runBlockingTest {
        val config = mock<Config> {
            on { it.readEventsEnabled } doReturn true
        }
        val (channelType, channelId) = randomCID().cidToTypeAndId()
        val channelMutableState = mock<ChannelMutableState> {
            on { it.channelConfig } doReturn MutableStateFlow(config)
            on { it.sortedMessages } doReturn MutableStateFlow(listOf(randomMessage()))
        }
        val sut = Fixture()
            .givenChannelState(channelType = channelType, channelId = channelId, channelMutableState)
            .givenCurrentUser(randomUser())
            .get()

        val result = sut.markChannelReadLocallyIfNeeded(channelType = channelType, channelId = channelId)

        result `should be equal to` false
    }

    @Test
    fun `When marking channel as read Should properly update last mark read date`() = runBlockingTest {
        val config = mock<Config> {
            on { it.readEventsEnabled } doReturn true
        }
        val (channelType, channelId) = randomCID().cidToTypeAndId()
        val channelMutableState = mock<ChannelMutableState> {
            on { it.channelConfig } doReturn MutableStateFlow(config)
            on { it.sortedMessages } doReturn MutableStateFlow(listOf(randomMessage()))
        }
        val channelLogic = mock<ChannelLogic>()
        val sut = Fixture()
            .givenChannelState(channelType = channelType, channelId = channelId, channelMutableState)
            .givenChannelLogic(channelType = channelType, channelId = channelId, channelLogic)
            .givenCurrentUser(randomUser())
            .givenOnlineUser()
            .get()

        sut.markChannelReadLocallyIfNeeded(channelType = channelType, channelId = channelId)

        verify(channelMutableState).lastMarkReadEvent = any()
    }

    @Test
    fun `When marking channel as read Should return true`() = runBlockingTest {
        val config = mock<Config> {
            on { it.readEventsEnabled } doReturn true
        }
        val (channelType, channelId) = randomCID().cidToTypeAndId()
        val channelMutableState = mock<ChannelMutableState> {
            on { it.channelConfig } doReturn MutableStateFlow(config)
            on { it.sortedMessages } doReturn MutableStateFlow(listOf(randomMessage()))
        }
        val channelLogic = mock<ChannelLogic>()
        val sut = Fixture()
            .givenChannelState(channelType = channelType, channelId = channelId, channelMutableState)
            .givenChannelLogic(channelType = channelType, channelId = channelId, channelLogic)
            .givenCurrentUser(randomUser())
            .givenOnlineUser()
            .get()

        val result = sut.markChannelReadLocallyIfNeeded(channelType = channelType, channelId = channelId)

        result `should be equal to` true
    }

    private class Fixture {

        private val chatClient = mock<ChatClient>()
        private val logic = mock<LogicRegistry>()
        private val state = mock<StateRegistry>()
        private val globalState = mock<GlobalState>()

        fun givenChannelState(channelType: String, channelId: String, channelState: ChannelMutableState) = apply {
            whenever(state.channel(channelType = channelType, channelId = channelId)) doReturn channelState
        }

        fun givenChannelLogic(channelType: String, channelId: String, channelLogic: ChannelLogic) = apply {
            whenever(logic.channel(channelType = channelType, channelId = channelId)) doReturn channelLogic
        }

        fun givenCurrentUser(user: User) = apply {
            whenever(chatClient.getCurrentUser()) doReturn user
        }

        fun givenOnlineUser() = apply {
            whenever(globalState.isOnline()) doReturn true
        }

        fun get(): ChannelMarkReadHelper = ChannelMarkReadHelper(chatClient, logic, state, globalState)
    }
}
