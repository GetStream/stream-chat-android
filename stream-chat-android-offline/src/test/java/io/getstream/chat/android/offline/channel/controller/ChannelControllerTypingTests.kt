package io.getstream.chat.android.offline.channel.controller

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.SynchronizedCoroutineTest
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.experimental.channel.logic.ChannelLogic
import io.getstream.chat.android.offline.experimental.channel.state.ChannelMutableState
import io.getstream.chat.android.offline.randomMessage
import io.getstream.chat.android.offline.randomUser
import io.getstream.chat.android.offline.repository.RepositoryFacade
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.util.Date

internal class ChannelControllerTypingTests : SynchronizedCoroutineTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    override fun getTestScope(): TestCoroutineScope = testCoroutines.scope

    @Test
    fun `When clean is invoked Then old typing indicators should be removed`() = runBlockingTest {
        val sut = Fixture(testCoroutines.scope, randomUser())
            .givenTypingEventsEnabled()
            .get()

        val user1 = randomUser()
        val user2 = randomUser()
        val typingEvent1 = TypingStartEvent(
            EventType.TYPING_START,
            Date(System.currentTimeMillis() - 20_000),
            user1,
            "channelType:channelId2",
            "channelType",
            "channelId2",
            null
        )
        val typingEvent2 = TypingStartEvent(
            EventType.TYPING_START,
            Date(),
            user2,
            "channelType:channelId1",
            "channelType",
            "channelId1",
            null
        )

        sut.setTyping(user1.id, typingEvent1)
        sut.setTyping(user2.id, typingEvent2)

        sut.typing.value.users.size `should be equal to` 2
        sut.clean()
        sut.typing.value.users.size `should be equal to` 1
    }

    @Test
    fun `When a user started typing Then subsequent keystroke events within a certain interval should not be sent to the server`() =
        runBlockingTest {
            val sut = Fixture(testCoroutines.scope, randomUser())
                .givenTypingEventsEnabled()
                .givenKeystrokeResult()
                .get()

            sut.keystroke(null).data() `should be equal to` true
            sut.keystroke(null).data() `should be equal to` false

            Thread.sleep(3001)

            sut.keystroke(null).data() `should be equal to` true
        }

    @Test
    fun `When a message is successfully marked as read Then the second invocation should be ignored`() = coroutineTest {
        val sut = Fixture(testCoroutines.scope, randomUser())
            .givenReadEventsEnabled()
            .get()

        sut.upsertMessage(randomMessage())

        sut.markRead() `should be equal to` true
        sut.markRead() `should be equal to` false
    }

    private class Fixture(private val scope: CoroutineScope, user: User) {
        private val repos: RepositoryFacade = mock()
        private val chatClient: ChatClient = mock()
        private val chatDomainImpl: ChatDomainImpl = mock()
        private val config: Config = mock()
        private val channelClient: ChannelClient = mock()
        private val userFlow = MutableStateFlow(user)

        init {
            whenever(chatClient.channel(any(), any())) doReturn channelClient
            whenever(chatClient.channel(any())) doReturn channelClient
            whenever(chatDomainImpl.user) doReturn userFlow
            whenever(chatDomainImpl.job) doReturn Job()
            whenever(chatDomainImpl.scope) doReturn scope
            whenever(chatDomainImpl.repos) doReturn repos
            whenever(chatDomainImpl.appContext) doReturn mock()
            whenever(chatDomainImpl.getChannelConfig(any())) doReturn config
        }

        fun givenReadEventsEnabled(): Fixture {
            whenever(config.readEventsEnabled) doReturn true
            return this
        }

        fun givenTypingEventsEnabled(): Fixture {
            whenever(config.typingEventsEnabled) doReturn true
            return this
        }

        fun givenKeystrokeResult(): Fixture {
            whenever(channelClient.keystroke()) doReturn TestCall(Result(mock<ChatEvent>()))
            return this
        }

        fun get(): ChannelController {
            val mutableState =
                ChannelMutableState("channelType", "channelId", scope, userFlow, MutableStateFlow(emptyMap()))
            return ChannelController(
                mutableState,
                ChannelLogic(mutableState, chatDomainImpl),
                chatClient,
                chatDomainImpl,
            )
        }
    }
}
