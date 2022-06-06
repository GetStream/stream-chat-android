package io.getstream.chat.android.client.events.cache

import io.getstream.chat.android.client.Mother
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.TypingStopEvent
import io.getstream.chat.android.client.events.caching.TypingEventCache
import io.getstream.chat.android.client.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
internal class TypingEventCacheTests {

    @Test
    fun `When TypingStartEvent is processed Should emit TypingStopEvent `() = runTest {
        val typingStartEvent = createTypingStartEvent()

        // First processed event will be TypingStartEvent
        // Second processed event will be TypingStopEvent
        val typingEventCache = TypingEventCache(
            coroutineScope = this,
            cleanStaleEventMs = 0L
        ) { typingStopEvent ->
            if (typingStopEvent is TypingStopEvent) {
                assert(
                    // Check that they contain the same essential values
                    typingStartEvent.cid == typingStopEvent.cid
                        && typingStartEvent.type == typingStopEvent.type
                        && typingStartEvent.user == typingStopEvent.user
                )
            }
        }

        typingEventCache.processEvent(typingStartEvent)
    }

    /**
     * The delay time is the time needed to "clean"
     * the start typing event defined by [TypingEventCache.cleanStaleEventMs].
     */
    @Test
    fun `Given TypingStartEvent was processed When the delay time has elapsed Should contain empty cache`() =
        runTest {
            val typingStartEvent = createTypingStartEvent()

            val typingEventCache = TypingEventCache(
                coroutineScope = this,
            ) { }

            typingEventCache.processEvent(typingStartEvent)

            advanceUntilIdle()

            assert(typingEventCache.currentlyTypingUsers.isEmpty())
        }

    /**
     * Counterpart is a typing event that is a different instance but
     * contains identical important values such as cid, user, etc.
     */
    @Test
    fun `Given TypingStartEvent was processed When TypingStopEvent counterpart is processed Should contain empty cache`() {
        val typingStartEvent = createTypingStartEvent()
        val typingStopEvent = createTypingStopEvent(
            cid = typingStartEvent.cid,
            user = typingStartEvent.user
        )

        val typingEventCache = TypingEventCache(
            coroutineScope = CoroutineScope(Dispatchers.IO),
        ) { }

        typingEventCache.processEvent(typingStartEvent)
        typingEventCache.processEvent(typingStopEvent)

        assert(typingEventCache.currentlyTypingUsers.isEmpty())
    }

    private fun createTypingStartEvent(
        type: String = "mockType",
        createdAt: Date = Date(),
        user: User = Mother.randomUser(),
        cid: String = "messaging:123",
        channelId: String = "123",
        channelType: String = "messaging",
        parentId: String? = null,
    ) = TypingStartEvent(type = type,
        createdAt = createdAt,
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        parentId = parentId)

    private fun createTypingStopEvent(
        type: String = "mockType",
        createdAt: Date = Date(),
        user: User = Mother.randomUser(),
        cid: String = "messaging:123",
        channelId: String = "123",
        channelType: String = "messaging",
        parentId: String? = null,
    ) = TypingStopEvent(type = type,
        createdAt = createdAt,
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        parentId = parentId)
}


