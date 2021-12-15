package io.getstream.chat.android.offline.querychannels

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.utils.Result
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

    private val chatClient: ChatClient = mock()

    @Test
    fun `Given channel is present, When received MemberAddedEvent, Should channel be removed`() {
        val cid = randomString()
        val channel = randomChannel(cid = cid)
        val eventHandler = NonMemberChatEventHandler(chatClient, MutableStateFlow(listOf(channel)))

        val event = randomMemberAddedEvent(cid)

        val result = eventHandler.handleMemberAddedEvent(event, Filters.neutral())

        result `should be equal to` EventHandlingResult.Remove(cid)
    }

    @Test
    fun `Given channel is not present, When received MemberAddedEvent, Should event be skipped`() {
        val cid = randomString()
        val eventHandler = NonMemberChatEventHandler(chatClient, MutableStateFlow(emptyList()))

        val event = randomMemberAddedEvent(cid)

        val result = eventHandler.handleMemberAddedEvent(event, Filters.neutral())

        result `should be equal to` EventHandlingResult.Skip
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given channel is not present, When received MemberRemovedEvent, Should channel be added`() = runBlockingTest {
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
    fun `Given channel is present, When received MemberRemovedEvent, Should channel be skipped`() = runBlockingTest {
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
    fun `Given channel is present, When received MemberRemovedEvent but the filter fails, Should event be skipped`() =
        runBlockingTest {
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
}
