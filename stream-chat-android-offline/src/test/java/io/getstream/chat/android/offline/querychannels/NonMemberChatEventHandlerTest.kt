package io.getstream.chat.android.offline.querychannels

import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.offline.event.handler.EventHandlingResult
import io.getstream.chat.android.offline.event.handler.NonMemberChatEventHandler
import io.getstream.chat.android.offline.randomChannel
import io.getstream.chat.android.offline.randomMemberAddedEvent
import io.getstream.chat.android.offline.randomMemberRemovedEvent
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class NonMemberChatEventHandlerTest {

    @Test
    fun `Given channel is cached, When received MemberAddedEvent, Should remove be removed`() {
        val cid = randomString()
        val channel = randomChannel(cid = cid)
        val eventHandler = NonMemberChatEventHandler(MutableStateFlow(listOf(channel)))

        val event = randomMemberAddedEvent(cid)

        val result = eventHandler.handleMemberAddedEvent(event, Filters.neutral(), channel)

        result `should be equal to` EventHandlingResult.Remove(cid)
    }

    @Test
    fun `Given channel is not present, When received MemberAddedEvent, Should skip be skipped`() {
        val cid = randomString()
        val eventHandler = NonMemberChatEventHandler(MutableStateFlow(emptyList()))

        val event = randomMemberAddedEvent(cid)

        val result = eventHandler.handleMemberAddedEvent(event, Filters.neutral(), randomChannel(cid))

        result `should be equal to` EventHandlingResult.Skip
    }

    @Test
    fun `Given channel is not cached, When received MemberAddedEvent, Should skip the channel`() {
        val cid = randomString()
        val channel = randomChannel(cid = cid)
        val eventHandler = NonMemberChatEventHandler(MutableStateFlow(listOf(channel)))

        val event = randomMemberAddedEvent(cid)

        val result = eventHandler.handleMemberAddedEvent(event, Filters.neutral(), null)

        result `should be equal to` EventHandlingResult.Skip
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given channel is not present, When received MemberRemovedEvent and channel cached, Should channel be added`() = runBlockingTest {
        val cid = randomString()
        val channel = randomChannel(cid = cid)
        val eventHandler = NonMemberChatEventHandler(MutableStateFlow(emptyList()))

        val event = randomMemberRemovedEvent(cid)
        val result = eventHandler.handleMemberRemovedEvent(event, Filters.neutral(), channel)

        result `should be equal to` EventHandlingResult.Add(channel)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given channel is present, When received MemberRemovedEvent and channel cached, Should channel be skipped`() = runBlockingTest {
        val cid = randomString()
        val channel = randomChannel(cid = cid)
        val eventHandler = NonMemberChatEventHandler(MutableStateFlow(listOf(channel)))

        val event = randomMemberRemovedEvent(cid)
        val result = eventHandler.handleMemberRemovedEvent(event, Filters.neutral(), channel)

        result `should be equal to` EventHandlingResult.Skip
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given channel is present, When received MemberRemovedEvent but channel is not cached, Should event be skipped`() =
        runBlockingTest {
            val cid = randomString()
            val eventHandler = NonMemberChatEventHandler(MutableStateFlow(emptyList()),)

            val event = randomMemberRemovedEvent(cid)
            val result = eventHandler.handleMemberRemovedEvent(event, Filters.neutral(), null)

            result `should be equal to` EventHandlingResult.Skip
        }
}
