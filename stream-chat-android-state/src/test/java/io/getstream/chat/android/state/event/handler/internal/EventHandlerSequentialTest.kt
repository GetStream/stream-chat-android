package io.getstream.chat.android.state.event.handler.internal

import io.getstream.chat.android.client.ChatEventListener
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.test.randomMarkAllReadEvent
import io.getstream.chat.android.client.test.randomNewMessageEvent
import io.getstream.chat.android.client.test.randomNotificationAddedToChannelEvent
import io.getstream.chat.android.client.test.randomNotificationChannelDeletedEvent
import io.getstream.chat.android.client.test.randomNotificationChannelTruncatedEvent
import io.getstream.chat.android.client.test.randomNotificationMarkReadEvent
import io.getstream.chat.android.client.test.randomNotificationMarkUnreadEvent
import io.getstream.chat.android.client.test.randomNotificationMessageNewEvent
import io.getstream.chat.android.client.utils.observable.Disposable
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.state.plugin.state.StateRegistry
import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

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
        val mutableGlobalState = MutableGlobalState().apply {
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

    internal class Fixture {
        private val currentUser = randomUser()
        private val subscribeForEvents: (ChatEventListener<ChatEvent>) -> Disposable =
            { _ -> EventHandlerSequential.EMPTY_DISPOSABLE}
        private val logicRegistry: LogicRegistry = mock()
        private val stateRegistry: StateRegistry = mock()
        private val clientState: ClientState = mock {
            on(it.user) doReturn MutableStateFlow(currentUser)
        }
        private var mutableGlobalState: MutableGlobalState? = null
        private val repos: RepositoryFacade = mock()
        private val sideEffect: suspend () -> Unit = {}
        private val syncedEvents: Flow<List<ChatEvent>> = emptyFlow()

        fun withReadEventsCapability(cid: String) = apply {
            repos.stub {
                onBlocking {
                    selectChannels(listOf(cid))
                } doReturn listOf(randomChannel(ownCapabilities = setOf(ChannelCapabilities.READ_EVENTS)))
            }

        }

        fun withMutableGlobalState(mutableGlobalState: MutableGlobalState) = apply {
            this.mutableGlobalState = mutableGlobalState
        }

        fun get(scope: CoroutineScope) = EventHandlerSequential(
            currentUserId = currentUser.id,
            subscribeForEvents = subscribeForEvents,
            logicRegistry = logicRegistry,
            stateRegistry = stateRegistry,
            clientState = clientState,
            mutableGlobalState = mutableGlobalState ?: MutableGlobalState(),
            repos = repos,
            sideEffect = sideEffect,
            syncedEvents = syncedEvents,
            scope = scope
        )
    }

    companion object {
        private val initialTotalunreadCount: Int = positiveRandomInt()
        private val initialChannelUnreadCount: Int = positiveRandomInt()
        private val totalUnreadCount = positiveRandomInt()
        private val unreadChannelCount = positiveRandomInt()
        private val randomCid = randomCID()
        private val prepareFixtureWithReadCapability: Fixture.() -> Unit = {
            withReadEventsCapability(randomCid)
        }
        private val neutralPrepareFixture: Fixture.() -> Unit = { }

        @JvmStatic
        fun unreadCountArguments() = unreadArgumentMarkAllReadEvent() +
            unreadArgumentNewMessageEvent() +
            unreadArgumentNotificationAddedToChannelEvent() +
            unreadArgumentNotificationChannelDeletedEvent() +
            unreadArgumentNotificationChannelTruncatedEvent() +
            unreadArgumentNotificationMarkReadEvent() +
            unreadArgumentNotificationMarkUnreadEvent() +
            unreadArgumentNotificationMessageNewEvent()

        private fun unreadArgumentNotificationAddedToChannelEvent() = listOf(
            Arguments.of(
                listOf(
                    randomNotificationAddedToChannelEvent(
                        cid = randomCid,
                        totalUnreadCount = totalUnreadCount,
                        unreadChannels = unreadChannelCount,
                    )
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
                    )
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
                    )
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
                    )
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
                    )
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
                    )
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
                    )
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
                    )
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
                    )
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
                    )
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
                    )
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
                    )
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
                    )
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
                    )
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
                    )
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
                    )
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
                    )
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
                    )
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
                    )
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
                    )
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