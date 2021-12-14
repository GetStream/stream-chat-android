package io.getstream.chat.android.offline.querychannels

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.MemberAddedEvent
import io.getstream.chat.android.client.events.MemberRemovedEvent
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.randomChannel
import io.getstream.chat.android.offline.randomMember
import io.getstream.chat.android.offline.randomUser
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import java.util.Date

internal class NonMemberChatEventHandlerTest {

    private val chatClient: ChatClient = mock()

    @Test
    fun `When received MemberAddedEvent and the channel is present, it should be removed`() {
        val cid = randomString()
        val channel = randomChannel(cid = cid)
        val eventHandler = NonMemberChatEventHandler(chatClient, MutableStateFlow(listOf(channel)))

        val event = randomMemberAddedEvent(cid)

        val result = eventHandler.handleMemberAddedEvent(event, Filters.neutral())

        result `should be equal to` EventHandlingResult.Remove(cid)
    }

    @Test
    fun `When received MemberAddedEvent and the channel is absent, it should be skipped`() {
        val cid = randomString()
        val eventHandler = NonMemberChatEventHandler(chatClient, MutableStateFlow(emptyList()))

        val event = randomMemberAddedEvent(cid)

        val result = eventHandler.handleMemberAddedEvent(event, Filters.neutral())

        result `should be equal to` EventHandlingResult.Skip
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `When received MemberRemovedEvent and the channel is absent, it should be added`() = runBlockingTest {
        val cid = randomString()
        val channel = randomChannel(cid = cid)
        val eventHandler = NonMemberChatEventHandler(
            chatClient,
            MutableStateFlow(emptyList()),
            mock {
                on(it.filter(any(), any(), any())) doReturn Result.success(listOf(channel))
            }
        )

        val event = randomMemberRemovedEvent(cid)
        val result = eventHandler.handleMemberRemovedEvent(event, Filters.neutral())

        result `should be equal to` EventHandlingResult.Add(channel)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `When received MemberRemovedEvent and the channel is present, it should be skipped`() = runBlockingTest {
        val cid = randomString()
        val channel = randomChannel(cid = cid)
        val eventHandler = NonMemberChatEventHandler(
            chatClient,
            MutableStateFlow(listOf(channel)),
            mock {
                on(it.filter(any(), any(), any())) doReturn Result.success(listOf(channel))
            }
        )

        val event = randomMemberRemovedEvent(cid)
        val result = eventHandler.handleMemberRemovedEvent(event, Filters.neutral())

        result `should be equal to` EventHandlingResult.Skip
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `When received MemberRemovedEvent and the channel is present, but the filter fails, it should be skipped`() = runBlockingTest {
        val cid = randomString()
        val channel = randomChannel(cid = cid)
        val eventHandler = NonMemberChatEventHandler(
            chatClient,
            MutableStateFlow(emptyList()),
            mock {
                on(it.filter(any(), any(), any())) doReturn Result.success(emptyList())
            }
        )

        val event = randomMemberRemovedEvent(cid)
        val result = eventHandler.handleMemberRemovedEvent(event, Filters.neutral())

        result `should be equal to` EventHandlingResult.Skip
    }

    private fun randomMemberAddedEvent(
        cid: String = randomString(),
    ): MemberAddedEvent {
        return MemberAddedEvent(
            type = randomString(),
            createdAt = Date(),
            user = randomUser(),
            cid = cid,
            channelType = randomString(),
            channelId = randomString(),
            member = randomMember()
        )
    }

    private fun randomMemberRemovedEvent(
        cid: String = randomString(),
    ): MemberRemovedEvent {
        return MemberRemovedEvent(
            type = randomString(),
            createdAt = Date(),
            user = randomUser(),
            cid = cid,
            channelType = randomString(),
            channelId = randomString(),
        )
    }
}
