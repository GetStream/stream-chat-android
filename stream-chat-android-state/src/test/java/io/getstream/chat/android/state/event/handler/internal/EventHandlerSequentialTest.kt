/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.state.event.handler.internal

import app.cash.turbine.test
import io.getstream.chat.android.client.ChatEventListener
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.test.randomChannelDeletedEvent
import io.getstream.chat.android.client.test.randomChannelUpdatedEvent
import io.getstream.chat.android.client.test.randomConnectedEvent
import io.getstream.chat.android.client.test.randomMarkAllReadEvent
import io.getstream.chat.android.client.test.randomMessageUpdateEvent
import io.getstream.chat.android.client.test.randomNewMessageEvent
import io.getstream.chat.android.client.test.randomNotificationAddedToChannelEvent
import io.getstream.chat.android.client.test.randomNotificationChannelDeletedEvent
import io.getstream.chat.android.client.test.randomNotificationChannelMutesUpdatedEvent
import io.getstream.chat.android.client.test.randomNotificationChannelTruncatedEvent
import io.getstream.chat.android.client.test.randomNotificationMarkReadEvent
import io.getstream.chat.android.client.test.randomNotificationMarkUnreadEvent
import io.getstream.chat.android.client.test.randomNotificationMessageNewEvent
import io.getstream.chat.android.client.test.randomNotificationMutesUpdatedEvent
import io.getstream.chat.android.client.test.randomNotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.test.randomPollDeletedEvent
import io.getstream.chat.android.client.utils.observable.Disposable
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.ChannelMute
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.Mute
import io.getstream.chat.android.models.User
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomChannelMute
import io.getstream.chat.android.randomLocation
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomMute
import io.getstream.chat.android.randomPoll
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.state.event.handler.internal.batch.BatchEvent
import io.getstream.chat.android.state.plugin.config.MessageBufferConfig
import io.getstream.chat.android.state.plugin.config.MessageBufferOverflow
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.state.plugin.state.StateRegistry
import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.atomic.AtomicReference

@kotlinx.coroutines.ExperimentalCoroutinesApi
internal class EventHandlerSequentialTest {

    @ParameterizedTest
    @MethodSource("unreadCountArguments")
    internal fun `GlobalState should be updated with proper unreadCount and channelUnreadCount values`(
        events: List<ChatEvent>,
        initialTotalunreadCount: Int,
        initialChannelUnreadCount: Int,
        prepareFixture: Fixture.() -> Unit,
        expectedTotalUnreadCount: Int,
        expectedChannelUnreadCount: Int,
    ) = runTest {
        val mutableGlobalState = MutableGlobalState(currentUser.id).apply {
            setTotalUnreadCount(initialTotalunreadCount)
            setChannelUnreadCount(initialChannelUnreadCount)
        }
        val handler = Fixture()
            .withMutableGlobalState(mutableGlobalState)
            .apply(prepareFixture)
            .get(this)

        handler.handleEvents(*events.toTypedArray())

        mutableGlobalState.totalUnreadCount.value `should be equal to` expectedTotalUnreadCount
        mutableGlobalState.channelUnreadCount.value `should be equal to` expectedChannelUnreadCount
    }

    @ParameterizedTest
    @MethodSource("hasOwnUserArguments")
    internal fun `GlobalState should be updated with info from 'me' user`(
        events: List<ChatEvent>,
        prepareFixture: Fixture.() -> Unit,
        expectedBanned: Boolean,
        expectedMutedUsers: List<Mute>,
        expectedChannelMutes: List<ChannelMute>,
        expectedBlockedUserIds: List<String>,
    ) = runTest {
        val mutableGlobalState = MutableGlobalState(currentUser.id).apply {
            setBanned(false)
            setMutedUsers(emptyList())
            setChannelMutes(emptyList())
            setBlockedUserIds(emptyList())
        }

        val handler = Fixture()
            .withMutableGlobalState(mutableGlobalState)
            .apply(prepareFixture)
            .get(this)

        handler.handleEvents(*events.toTypedArray())

        mutableGlobalState.banned.value `should be equal to` expectedBanned
        mutableGlobalState.muted.value `should be equal to` expectedMutedUsers
        mutableGlobalState.channelMutes.value `should be equal to` expectedChannelMutes
        mutableGlobalState.blockedUserIds.value `should be equal to` expectedBlockedUserIds
    }

    @Test
    fun `When handling PollDeletedEvent, The the poll should be deleted from local storage`() = runTest {
        // given
        val deletedPoll = randomPoll()
        val event = randomPollDeletedEvent(poll = deletedPoll)
        val message = randomMessage(poll = deletedPoll)
        val repos: RepositoryFacade = mock()
        whenever(repos.selectMessages(any())) doReturn listOf(message)
        whenever(repos.selectMessagesWithPoll(deletedPoll.id)) doReturn listOf(message)
        whenever(repos.selectChannels(any())) doReturn emptyList()
        whenever(repos.selectThreads(any())) doReturn emptyList()
        val handler = Fixture()
            .withRepositoryFacade(repos)
            .get(this)
        // when
        handler.handleEvents(event)
        // then
        verify(repos).deletePoll(event.poll.id)
    }

    @ParameterizedTest
    @MethodSource("groupedUnreadChannelsArguments")
    internal fun `GlobalState should be updated with proper groupedUnreadChannels values`(
        events: List<ChatEvent>,
        initialGroupedUnreadChannels: Map<String, Int>,
        prepareFixture: Fixture.() -> Unit,
        expectedGroupedUnreadChannels: Map<String, Int>,
    ) = runTest {
        val mutableGlobalState = MutableGlobalState(currentUser.id).apply {
            setGroupedUnreadChannels(initialGroupedUnreadChannels)
        }
        val handler = Fixture()
            .withMutableGlobalState(mutableGlobalState)
            .apply(prepareFixture)
            .get(this)

        handler.handleEvents(*events.toTypedArray())

        mutableGlobalState.groupedUnreadChannels.value `should be equal to` expectedGroupedUnreadChannels
    }

    @Test
    fun `ChannelUpdatedEvent migrates grouped unread count when group field changes`() = runTest {
        val cid = "messaging:channel-id"
        val mutableGlobalState = MutableGlobalState(currentUser.id).apply {
            setGroupedUnreadChannels(mapOf("a" to 1, "b" to 0))
        }
        val newChannel = randomChannel(
            id = "channel-id",
            type = "messaging",
            extraData = mapOf("group" to "b"),
        )
        val handler = Fixture()
            .withCurrentUser(currentUser)
            .withMutableGlobalState(mutableGlobalState)
            .withActiveChannel(
                channelType = "messaging",
                channelId = "channel-id",
                extraData = mapOf("group" to "a"),
                unreadMessages = 1,
            )
            .get(this)

        handler.handleEvents(randomChannelUpdatedEvent(cid = cid, channel = newChannel))

        mutableGlobalState.groupedUnreadChannels.value `should be equal to` mapOf("a" to 0, "b" to 1)
    }

    @Test
    fun `Two ChannelUpdatedEvents for same cid in one batch apply the migration only once`() = runTest {
        val cid = "messaging:channel-id"
        val mutableGlobalState = MutableGlobalState(currentUser.id).apply {
            setGroupedUnreadChannels(mapOf("a" to 2, "b" to 0))
        }
        val newChannel = randomChannel(
            id = "channel-id",
            type = "messaging",
            extraData = mapOf("group" to "b"),
        )
        val handler = Fixture()
            .withCurrentUser(currentUser)
            .withMutableGlobalState(mutableGlobalState)
            .withActiveChannel(
                channelType = "messaging",
                channelId = "channel-id",
                extraData = mapOf("group" to "a"),
                unreadMessages = 1,
            )
            .get(this)

        handler.handleEvents(
            randomChannelUpdatedEvent(cid = cid, channel = newChannel),
            randomChannelUpdatedEvent(cid = cid, channel = newChannel),
        )

        // Without dedup the delta would be applied twice -> {"a" to 0, "b" to 2}.
        mutableGlobalState.groupedUnreadChannels.value `should be equal to` mapOf("a" to 1, "b" to 1)
    }

    @Test
    fun `NotificationRemovedFromChannelEvent before ChannelUpdatedEvent same cid suppresses migration`() = runTest {
        val cid = "messaging:channel-id"
        val mutableGlobalState = MutableGlobalState(currentUser.id).apply {
            setGroupedUnreadChannels(mapOf("a" to 1, "b" to 0))
        }
        val newChannel = randomChannel(
            id = "channel-id",
            type = "messaging",
            extraData = mapOf("group" to "b"),
        )
        val handler = Fixture()
            .withCurrentUser(currentUser)
            .withMutableGlobalState(mutableGlobalState)
            .withActiveChannel(
                channelType = "messaging",
                channelId = "channel-id",
                extraData = mapOf("group" to "a"),
                unreadMessages = 1,
            )
            .get(this)

        handler.handleEvents(
            randomNotificationRemovedFromChannelEvent(cid = cid, member = randomMember(user = currentUser)),
            randomChannelUpdatedEvent(cid = cid, channel = newChannel),
        )

        // Removed-from-channel marks the cid as destroyed for this batch, so the subsequent
        // channel.updated delta does not migrate counts.
        mutableGlobalState.groupedUnreadChannels.value `should be equal to` mapOf("a" to 1, "b" to 0)
    }

    @Test
    fun `NotificationMarkReadEvent before ChannelUpdatedEvent same cid does not double-apply delta`() = runTest {
        val cid = "messaging:channel-id"
        val mutableGlobalState = MutableGlobalState(currentUser.id).apply {
            setGroupedUnreadChannels(mapOf("a" to 1, "b" to 0))
        }
        val newChannel = randomChannel(
            id = "channel-id",
            type = "messaging",
            extraData = mapOf("group" to "b"),
        )
        val handler = Fixture()
            .withCurrentUser(currentUser)
            .withMutableGlobalState(mutableGlobalState)
            .withActiveChannel(
                channelType = "messaging",
                channelId = "channel-id",
                extraData = mapOf("group" to "a"),
                unreadMessages = 1,
            )
            .get(this)

        // HGUC mark_read carries authoritative {a:0, b:0}; channel.updated must NOT then dec a/inc b
        // on top (the channel is now read).
        handler.handleEvents(
            randomNotificationMarkReadEvent(
                cid = cid,
                user = currentUser,
                groupedUnreadChannels = mapOf("a" to 0, "b" to 0),
            ),
            randomChannelUpdatedEvent(cid = cid, channel = newChannel),
        )

        mutableGlobalState.groupedUnreadChannels.value `should be equal to` mapOf("a" to 0, "b" to 0)
    }

    @Test
    fun `MarkAllReadEvent before ChannelUpdatedEvent suppresses migration`() = runTest {
        val cid = "messaging:channel-id"
        val mutableGlobalState = MutableGlobalState(currentUser.id).apply {
            setGroupedUnreadChannels(mapOf("a" to 1, "b" to 0))
        }
        val newChannel = randomChannel(
            id = "channel-id",
            type = "messaging",
            extraData = mapOf("group" to "b"),
        )
        val handler = Fixture()
            .withCurrentUser(currentUser)
            .withMutableGlobalState(mutableGlobalState)
            .withActiveChannel(
                channelType = "messaging",
                channelId = "channel-id",
                extraData = mapOf("group" to "a"),
                unreadMessages = 1,
            )
            .get(this)

        handler.handleEvents(
            randomMarkAllReadEvent(user = currentUser),
            randomChannelUpdatedEvent(cid = cid, channel = newChannel),
        )

        // MarkAllRead conceptually clears unread on all channels; delta no-ops despite stale cache.
        mutableGlobalState.groupedUnreadChannels.value `should be equal to` mapOf("a" to 1, "b" to 0)
    }

    @ParameterizedTest
    @MethodSource("sharedLocationArguments")
    fun `GlobalState should be updated with shared locations`(
        events: List<ChatEvent>,
        locations: List<Location>,
    ) = runTest {
        val mutableGlobalState = MutableGlobalState(currentUser.id)

        val sut = Fixture()
            .withMutableGlobalState(mutableGlobalState)
            .apply(prepareFixtureWithReadCapability)
            .get(this)

        sut.handleEvents(*events.toTypedArray())

        mutableGlobalState.activeLiveLocations.test {
            assertEquals(locations, awaitItem())
        }
    }

    @Test
    fun `When buffer overflows with DROP_OLDEST, the oldest queued NewMessageEvent is dropped`() = runTest {
        val fixture = Fixture()
            .withBufferConfig(
                MessageBufferConfig(
                    channelTypes = setOf(BUFFERED_CHANNEL_TYPE),
                    capacity = 1,
                    overflow = MessageBufferOverflow.DROP_OLDEST,
                ),
            )
            .withReadEventsCapabilityForAny()
            .pauseSideEffect()
        val handler = fixture.get(this)
        val first = newMessageEventOnBufferedType()
        val second = newMessageEventOnBufferedType()
        val third = newMessageEventOnBufferedType()
        val fourth = newMessageEventOnBufferedType()

        handler.startListening()
        advanceUntilIdle()
        val listener = fixture.listener()

        listener.onEvent(first)
        advanceUntilIdle() // collector picks up first, suspends on sideEffect
        listener.onEvent(second)
        listener.onEvent(third)
        listener.onEvent(fourth)
        advanceUntilIdle() // give the buffer a chance to apply DROP_OLDEST

        fixture.releaseSideEffect()
        advanceUntilIdle()

        val processedEvents = capturedBatchEvents(fixture).flatMap { it.sortedEvents }
        // The first event was already in the collector when overflow happened, and the latest
        // emitted event survives in the buffer. The events in between are dropped.
        assertTrue(first in processedEvents) { "Expected the first emitted event to survive" }
        assertTrue(fourth in processedEvents) { "Expected the latest emitted event to survive" }
        assertFalse(second in processedEvents) { "Expected the second event to be dropped (oldest queued)" }
        assertFalse(third in processedEvents) { "Expected the third event to be dropped (oldest queued)" }
    }

    @Test
    fun `When buffer would overflow with DROP_LATEST, the latest NewMessageEvent is dropped`() = runTest {
        val fixture = Fixture()
            .withBufferConfig(
                MessageBufferConfig(
                    channelTypes = setOf(BUFFERED_CHANNEL_TYPE),
                    capacity = 1,
                    overflow = MessageBufferOverflow.DROP_LATEST,
                ),
            )
            .withReadEventsCapabilityForAny()
            .pauseSideEffect()
        val handler = fixture.get(this)
        val first = newMessageEventOnBufferedType()
        val second = newMessageEventOnBufferedType()
        val third = newMessageEventOnBufferedType()
        val fourth = newMessageEventOnBufferedType()

        handler.startListening()
        advanceUntilIdle()
        val listener = fixture.listener()

        listener.onEvent(first)
        advanceUntilIdle()
        listener.onEvent(second)
        listener.onEvent(third)
        listener.onEvent(fourth)
        advanceUntilIdle()

        fixture.releaseSideEffect()
        advanceUntilIdle()

        val processedEvents = capturedBatchEvents(fixture).flatMap { it.sortedEvents }
        // With DROP_LATEST the in-flight first event survives, the second event takes the buffer
        // slot and is processed once the gate releases; later emissions are dropped.
        assertTrue(first in processedEvents) { "Expected the first emitted event to survive" }
        assertTrue(second in processedEvents) { "Expected the second event to fit the buffer slot" }
        assertFalse(third in processedEvents) { "Expected the third event to be dropped (latest)" }
        assertFalse(fourth in processedEvents) { "Expected the fourth event to be dropped (latest)" }
    }

    @Test
    fun `When NewMessageEvent channelType is not buffered, no event is dropped`() = runTest {
        val fixture = Fixture()
            .withBufferConfig(
                MessageBufferConfig(
                    channelTypes = setOf("livestream"),
                    capacity = 1,
                    overflow = MessageBufferOverflow.DROP_OLDEST,
                ),
            )
            .withReadEventsCapabilityForAny()
            .pauseSideEffect()
        val handler = fixture.get(this)
        val events = List(4) { newMessageEventOnBufferedType() }

        handler.startListening()
        advanceUntilIdle()
        val listener = fixture.listener()

        events.forEach { listener.onEvent(it) }
        advanceUntilIdle()

        fixture.releaseSideEffect()
        advanceUntilIdle()

        val processedEvents = capturedBatchEvents(fixture).flatMap { it.sortedEvents }
        events.forEach { event ->
            assertTrue(event in processedEvents) {
                "Expected non-buffered channelType event to be processed (no drop)"
            }
        }
    }

    @Test
    fun `When listener is bombarded with thousands of NewMessageEvents, the buffer drops old ones`() = runTest {
        val capacity = 100
        val totalEmissions = 5_000
        val fixture = Fixture()
            .withBufferConfig(
                MessageBufferConfig(
                    channelTypes = setOf(BUFFERED_CHANNEL_TYPE),
                    capacity = capacity,
                    overflow = MessageBufferOverflow.DROP_OLDEST,
                ),
            )
            .withReadEventsCapabilityForAny()
        val handler = fixture.get(this)

        handler.startListening()
        advanceUntilIdle()
        val listener = fixture.listener()

        // Bombard the listener synchronously. Because the test dispatcher won't run the
        // collector between tryEmit calls, the buffer fills past `capacity` and DROP_OLDEST
        // evicts the oldest events.
        val emitted = List(totalEmissions) { newMessageEventOnBufferedType() }
        emitted.forEach { listener.onEvent(it) }
        advanceUntilIdle()

        val processed = capturedBatchEvents(fixture).flatMap { it.sortedEvents }.toSet()
        assertTrue(processed.size < totalEmissions) {
            "Expected drops under load: processed=${processed.size}, emitted=$totalEmissions"
        }
        assertTrue(emitted.last() in processed) {
            "Expected the latest emitted event to survive DROP_OLDEST overflow"
        }
        assertFalse(emitted.first() in processed) {
            "Expected the oldest emitted event to be dropped under load"
        }
    }

    private fun capturedBatchEvents(fixture: Fixture): List<BatchEvent> {
        val captor = argumentCaptor<BatchEvent>()
        verify(fixture.stateRegistry, atLeast(0)).handleBatchEvent(captor.capture())
        return captor.allValues
    }

    private fun newMessageEventOnBufferedType() = randomNewMessageEvent(
        cid = "$BUFFERED_CHANNEL_TYPE:${randomString()}",
        channelType = BUFFERED_CHANNEL_TYPE,
    )

    internal class Fixture {
        private var currentUser = randomUser()
        private val capturedListener = AtomicReference<ChatEventListener<ChatEvent>>()
        private val subscribeForEvents: (ChatEventListener<ChatEvent>) -> Disposable = { listener ->
            capturedListener.set(listener)
            EventHandlerSequential.EMPTY_DISPOSABLE
        }
        private val logicRegistry: LogicRegistry = mock()
        val stateRegistry: StateRegistry = mock()
        private val clientState: ClientState = mock {
            on(it.user) doReturn MutableStateFlow(currentUser)
        }
        private var mutableGlobalState: MutableGlobalState? = null
        private var repos: RepositoryFacade = mock()
        private var sideEffectGate: CompletableDeferred<Unit> = CompletableDeferred(Unit)
        private val sideEffect: suspend () -> Unit = { sideEffectGate.await() }
        private val syncedEvents: Flow<List<ChatEvent>> = emptyFlow()
        private var bufferConfig: MessageBufferConfig = MessageBufferConfig()

        fun withReadEventsCapability(cid: String) = apply {
            repos.stub {
                onBlocking {
                    selectChannel(cid)
                } doReturn randomChannel(ownCapabilities = setOf(ChannelCapabilities.READ_EVENTS))
            }
        }

        fun withReadEventsCapabilityForAny() = apply {
            repos.stub {
                onBlocking {
                    selectChannel(any())
                } doReturn randomChannel(ownCapabilities = setOf(ChannelCapabilities.READ_EVENTS))
            }
        }

        fun withCurrentUser(user: User) = apply {
            currentUser = user
        }

        fun withMutableGlobalState(mutableGlobalState: MutableGlobalState) = apply {
            this.mutableGlobalState = mutableGlobalState
        }

        fun withRepositoryFacade(repos: RepositoryFacade) = apply {
            this.repos = repos
        }

        fun withBufferConfig(config: MessageBufferConfig) = apply {
            this.bufferConfig = config
        }

        fun pauseSideEffect() = apply {
            sideEffectGate = CompletableDeferred()
        }

        fun releaseSideEffect() {
            sideEffectGate.complete(Unit)
        }

        fun listener(): ChatEventListener<ChatEvent> = capturedListener.get()

        /**
         * Stubs [stateRegistry] so the channel identified by [channelType] / [channelId] is
         * active and `mutableChannel(...).toChannel()` returns a channel with the given
         * [extraData] and a single [ChannelUserRead] for [currentUser] carrying [unreadMessages].
         */
        fun withActiveChannel(
            channelType: String,
            channelId: String,
            extraData: Map<String, Any>,
            unreadMessages: Int,
        ) = apply {
            val cached = randomChannel(
                id = channelId,
                type = channelType,
                extraData = extraData,
                read = listOf(
                    io.getstream.chat.android.randomChannelUserRead(
                        user = currentUser,
                        unreadMessages = unreadMessages,
                    ),
                ),
            )
            val channelMutableState: io.getstream.chat.android.state.plugin.state.channel.internal.ChannelMutableState =
                mock {
                    on { toChannel() } doReturn cached
                }
            whenever(stateRegistry.isActiveChannel(channelType, channelId)) doReturn true
            whenever(stateRegistry.mutableChannel(channelType, channelId)) doReturn channelMutableState
        }

        fun get(scope: CoroutineScope) = EventHandlerSequential(
            currentUserId = currentUser.id,
            subscribeForEvents = subscribeForEvents,
            logicRegistry = logicRegistry,
            stateRegistry = stateRegistry,
            clientState = clientState,
            mutableGlobalState = mutableGlobalState ?: MutableGlobalState(currentUser.id),
            repos = repos,
            sideEffect = sideEffect,
            syncedEvents = syncedEvents,
            bufferConfig = bufferConfig,
            scope = scope,
        )
    }

    companion object {
        private const val BUFFERED_CHANNEL_TYPE = "messaging"
        private val initialTotalunreadCount: Int = positiveRandomInt()
        private val initialChannelUnreadCount: Int = positiveRandomInt()
        private val totalUnreadCount = positiveRandomInt()
        private val unreadChannelCount = positiveRandomInt()
        private val randomCid = randomCID()
        private val prepareFixtureWithReadCapability: Fixture.() -> Unit = {
            withReadEventsCapability(randomCid)
        }
        private val neutralPrepareFixture: Fixture.() -> Unit = { }
        private val currentUser = randomUser()
        private val prepareFixtureWithCurrentUser: Fixture.() -> Unit = {
            withCurrentUser(currentUser)
        }

        private val groupedUnreadChannels = mapOf("direct" to positiveRandomInt(), "support" to positiveRandomInt())
        private val initialGroupedUnreadChannels = mapOf("old" to positiveRandomInt())

        @Suppress("LongMethod")
        @JvmStatic
        fun groupedUnreadChannelsArguments() = listOf(
            // NewMessageEvent with grouped unreads updates GlobalState
            Arguments.of(
                listOf(
                    randomNewMessageEvent(
                        cid = randomCid,
                        groupedUnreadChannels = groupedUnreadChannels,
                    ),
                ),
                initialGroupedUnreadChannels,
                prepareFixtureWithReadCapability,
                groupedUnreadChannels,
            ),
            // NotificationMarkReadEvent with grouped unreads updates GlobalState
            Arguments.of(
                listOf(
                    randomNotificationMarkReadEvent(
                        groupedUnreadChannels = groupedUnreadChannels,
                    ),
                ),
                initialGroupedUnreadChannels,
                neutralPrepareFixture,
                groupedUnreadChannels,
            ),
            // NotificationMarkUnreadEvent with grouped unreads updates GlobalState
            Arguments.of(
                listOf(
                    randomNotificationMarkUnreadEvent(
                        groupedUnreadChannels = groupedUnreadChannels,
                    ),
                ),
                initialGroupedUnreadChannels,
                neutralPrepareFixture,
                groupedUnreadChannels,
            ),
            // NotificationMessageNewEvent with grouped unreads updates GlobalState
            Arguments.of(
                listOf(
                    randomNotificationMessageNewEvent(
                        cid = randomCid,
                        groupedUnreadChannels = groupedUnreadChannels,
                    ),
                ),
                initialGroupedUnreadChannels,
                neutralPrepareFixture,
                groupedUnreadChannels,
            ),
            // NotificationChannelDeletedEvent with grouped unreads updates GlobalState
            Arguments.of(
                listOf(
                    randomNotificationChannelDeletedEvent(
                        cid = randomCid,
                        groupedUnreadChannels = groupedUnreadChannels,
                    ),
                ),
                initialGroupedUnreadChannels,
                neutralPrepareFixture,
                groupedUnreadChannels,
            ),
            // NotificationChannelTruncatedEvent with grouped unreads updates GlobalState
            Arguments.of(
                listOf(
                    randomNotificationChannelTruncatedEvent(
                        cid = randomCid,
                        groupedUnreadChannels = groupedUnreadChannels,
                    ),
                ),
                initialGroupedUnreadChannels,
                prepareFixtureWithReadCapability,
                groupedUnreadChannels,
            ),
            // Event with null grouped unreads preserves previous value
            Arguments.of(
                listOf(
                    randomNewMessageEvent(
                        cid = randomCid,
                        groupedUnreadChannels = null,
                    ),
                ),
                initialGroupedUnreadChannels,
                prepareFixtureWithReadCapability,
                initialGroupedUnreadChannels,
            ),
        )

        @JvmStatic
        fun unreadCountArguments() = unreadArgumentMarkAllReadEvent() +
            unreadArgumentNewMessageEvent() +
            unreadArgumentNotificationAddedToChannelEvent() +
            unreadArgumentNotificationChannelDeletedEvent() +
            unreadArgumentNotificationChannelTruncatedEvent() +
            unreadArgumentNotificationMarkReadEvent() +
            unreadArgumentNotificationMarkUnreadEvent() +
            unreadArgumentNotificationMessageNewEvent()

        @JvmStatic
        fun hasOwnUserArguments() = randomUser(
            currentUser.id,
            banned = randomBoolean(),
            mutes = listOf(randomMute()),
            channelMutes = listOf(randomChannelMute()),
            blockedUserIds = listOf(randomString()),
        ).let { me ->
            listOf(
                Arguments.of(
                    listOf(randomConnectedEvent(me = me)),
                    prepareFixtureWithCurrentUser,
                    me.banned,
                    me.mutes,
                    me.channelMutes,
                    me.blockedUserIds,
                ),
                Arguments.of(
                    listOf(randomNotificationChannelMutesUpdatedEvent(me = me)),
                    prepareFixtureWithCurrentUser,
                    me.banned,
                    me.mutes,
                    me.channelMutes,
                    me.blockedUserIds,
                ),
                Arguments.of(
                    listOf(randomNotificationMutesUpdatedEvent(me = me)),
                    prepareFixtureWithCurrentUser,
                    me.banned,
                    me.mutes,
                    me.channelMutes,
                    me.blockedUserIds,
                ),
            )
        }

        @JvmStatic
        fun sharedLocationArguments() = listOf(
            run {
                val message = randomMessage(
                    sharedLocation = randomLocation(userId = currentUser.id),
                )
                Arguments.of(
                    listOf(randomNewMessageEvent(cid = randomCid, message = message)),
                    listOf(message.sharedLocation),
                )
            },
            run {
                val message = randomMessage(
                    sharedLocation = randomLocation(userId = currentUser.id),
                )
                Arguments.of(
                    listOf(randomMessageUpdateEvent(cid = randomCid, message = message)),
                    listOf(message.sharedLocation),
                )
            },
            run {
                val location = randomLocation(userId = currentUser.id)
                val channel = randomChannel(activeLiveLocations = listOf(location))
                Arguments.of(
                    listOf(randomChannelUpdatedEvent(cid = randomCid, channel = channel)),
                    listOf(location),
                )
            },
            run {
                val location = randomLocation(userId = currentUser.id)
                val channel = randomChannel(activeLiveLocations = listOf(location))
                Arguments.of(
                    listOf(randomChannelDeletedEvent(cid = randomCid, channel = channel)),
                    listOf(location),
                )
            },
        )

        private fun unreadArgumentNotificationAddedToChannelEvent() = listOf(
            Arguments.of(
                listOf(
                    randomNotificationAddedToChannelEvent(
                        cid = randomCid,
                        totalUnreadCount = totalUnreadCount,
                        unreadChannels = unreadChannelCount,
                    ),
                ),
                initialTotalunreadCount,
                initialChannelUnreadCount,
                prepareFixtureWithReadCapability,
                totalUnreadCount,
                unreadChannelCount,
            ),
            Arguments.of(
                listOf(
                    randomNotificationAddedToChannelEvent(
                        cid = randomCid,
                        totalUnreadCount = totalUnreadCount,
                        unreadChannels = 0,
                    ),
                ),
                initialTotalunreadCount,
                initialChannelUnreadCount,
                prepareFixtureWithReadCapability,
                initialTotalunreadCount,
                initialChannelUnreadCount,
            ),
        )

        private fun unreadArgumentNotificationChannelDeletedEvent() = listOf(
            Arguments.of(
                listOf(
                    randomNotificationChannelDeletedEvent(
                        cid = randomCid,
                        totalUnreadCount = totalUnreadCount,
                        unreadChannels = unreadChannelCount,
                    ),
                ),
                initialTotalunreadCount,
                initialChannelUnreadCount,
                prepareFixtureWithReadCapability,
                totalUnreadCount,
                unreadChannelCount,
            ),
            Arguments.of(
                listOf(
                    randomNotificationChannelDeletedEvent(
                        cid = randomCid,
                        totalUnreadCount = totalUnreadCount,
                        unreadChannels = unreadChannelCount,
                    ),
                ),
                initialTotalunreadCount,
                initialChannelUnreadCount,
                neutralPrepareFixture,
                totalUnreadCount,
                unreadChannelCount,
            ),
            Arguments.of(
                listOf(
                    randomNotificationChannelDeletedEvent(
                        cid = randomCid,
                        totalUnreadCount = totalUnreadCount,
                        unreadChannels = 0,
                    ),
                ),
                initialTotalunreadCount,
                initialChannelUnreadCount,
                prepareFixtureWithReadCapability,
                initialTotalunreadCount,
                initialChannelUnreadCount,
            ),
            Arguments.of(
                listOf(
                    randomNotificationChannelDeletedEvent(
                        cid = randomCid,
                        totalUnreadCount = totalUnreadCount,
                        unreadChannels = 0,
                    ),
                ),
                initialTotalunreadCount,
                initialChannelUnreadCount,
                neutralPrepareFixture,
                initialTotalunreadCount,
                initialChannelUnreadCount,
            ),
        )

        private fun unreadArgumentNewMessageEvent() = listOf(
            Arguments.of(
                listOf(
                    randomNewMessageEvent(
                        cid = randomCid,
                        totalUnreadCount = totalUnreadCount,
                        unreadChannels = unreadChannelCount,
                    ),
                ),
                initialTotalunreadCount,
                initialChannelUnreadCount,
                prepareFixtureWithReadCapability,
                totalUnreadCount,
                unreadChannelCount,
            ),
            Arguments.of(
                listOf(
                    randomNewMessageEvent(
                        cid = randomCid,
                        totalUnreadCount = totalUnreadCount,
                        unreadChannels = unreadChannelCount,
                    ),
                ),
                initialTotalunreadCount,
                initialChannelUnreadCount,
                neutralPrepareFixture,
                initialTotalunreadCount,
                initialChannelUnreadCount,
            ),
        )

        private fun unreadArgumentNotificationMessageNewEvent() = listOf(
            Arguments.of(
                listOf(
                    randomNotificationMessageNewEvent(
                        cid = randomCid,
                        totalUnreadCount = totalUnreadCount,
                        unreadChannels = unreadChannelCount,
                    ),
                ),
                initialTotalunreadCount,
                initialChannelUnreadCount,
                prepareFixtureWithReadCapability,
                totalUnreadCount,
                unreadChannelCount,
            ),
            Arguments.of(
                listOf(
                    randomNotificationMessageNewEvent(
                        cid = randomCid,
                        totalUnreadCount = totalUnreadCount,
                        unreadChannels = unreadChannelCount,
                    ),
                ),
                initialTotalunreadCount,
                initialChannelUnreadCount,
                neutralPrepareFixture,
                totalUnreadCount,
                unreadChannelCount,
            ),
            Arguments.of(
                listOf(
                    randomNotificationMessageNewEvent(
                        cid = randomCid,
                        totalUnreadCount = totalUnreadCount,
                        unreadChannels = 0,
                    ),
                ),
                initialTotalunreadCount,
                initialChannelUnreadCount,
                neutralPrepareFixture,
                initialTotalunreadCount,
                initialChannelUnreadCount,
            ),
            Arguments.of(
                listOf(
                    randomNotificationMessageNewEvent(
                        cid = randomCid,
                        totalUnreadCount = totalUnreadCount,
                        unreadChannels = 0,
                    ),
                ),
                initialTotalunreadCount,
                initialChannelUnreadCount,
                prepareFixtureWithReadCapability,
                initialTotalunreadCount,
                initialChannelUnreadCount,
            ),
        )

        private fun unreadArgumentNotificationChannelTruncatedEvent() = listOf(
            Arguments.of(
                listOf(
                    randomNotificationChannelTruncatedEvent(
                        cid = randomCid,
                        totalUnreadCount = totalUnreadCount,
                        unreadChannels = unreadChannelCount,
                    ),
                ),
                initialTotalunreadCount,
                initialChannelUnreadCount,
                prepareFixtureWithReadCapability,
                totalUnreadCount,
                unreadChannelCount,
            ),
            Arguments.of(
                listOf(
                    randomNewMessageEvent(
                        cid = randomCid,
                        totalUnreadCount = totalUnreadCount,
                        unreadChannels = unreadChannelCount,
                    ),
                ),
                initialTotalunreadCount,
                initialChannelUnreadCount,
                neutralPrepareFixture,
                initialTotalunreadCount,
                initialChannelUnreadCount,
            ),
        )

        private fun unreadArgumentMarkAllReadEvent() = listOf(
            Arguments.of(
                listOf(
                    randomMarkAllReadEvent(
                        totalUnreadCount = totalUnreadCount,
                        unreadChannels = unreadChannelCount,
                    ),
                ),
                initialTotalunreadCount,
                initialChannelUnreadCount,
                prepareFixtureWithReadCapability,
                totalUnreadCount,
                unreadChannelCount,
            ),
            Arguments.of(
                listOf(
                    randomMarkAllReadEvent(
                        totalUnreadCount = totalUnreadCount,
                        unreadChannels = unreadChannelCount,
                    ),
                ),
                initialTotalunreadCount,
                initialChannelUnreadCount,
                neutralPrepareFixture,
                totalUnreadCount,
                unreadChannelCount,
            ),
        )

        private fun unreadArgumentNotificationMarkReadEvent() = listOf(
            Arguments.of(
                listOf(
                    randomNotificationMarkReadEvent(
                        totalUnreadCount = totalUnreadCount,
                        unreadChannels = unreadChannelCount,
                    ),
                ),
                initialTotalunreadCount,
                initialChannelUnreadCount,
                prepareFixtureWithReadCapability,
                totalUnreadCount,
                unreadChannelCount,
            ),
            Arguments.of(
                listOf(
                    randomNotificationMarkReadEvent(
                        totalUnreadCount = totalUnreadCount,
                        unreadChannels = unreadChannelCount,
                    ),
                ),
                initialTotalunreadCount,
                initialChannelUnreadCount,
                neutralPrepareFixture,
                totalUnreadCount,
                unreadChannelCount,
            ),
        )

        private fun unreadArgumentNotificationMarkUnreadEvent() = listOf(
            Arguments.of(
                listOf(
                    randomNotificationMarkUnreadEvent(
                        totalUnreadCount = totalUnreadCount,
                        unreadChannels = unreadChannelCount,
                    ),
                ),
                initialTotalunreadCount,
                initialChannelUnreadCount,
                prepareFixtureWithReadCapability,
                totalUnreadCount,
                unreadChannelCount,
            ),
            Arguments.of(
                listOf(
                    randomNotificationMarkUnreadEvent(
                        totalUnreadCount = totalUnreadCount,
                        unreadChannels = unreadChannelCount,
                    ),
                ),
                initialTotalunreadCount,
                initialChannelUnreadCount,
                neutralPrepareFixture,
                totalUnreadCount,
                unreadChannelCount,
            ),
        )
    }
}
