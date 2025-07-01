/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.client.ChatEventListener
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.test.randomConnectedEvent
import io.getstream.chat.android.client.test.randomMarkAllReadEvent
import io.getstream.chat.android.client.test.randomNewMessageEvent
import io.getstream.chat.android.client.test.randomNotificationAddedToChannelEvent
import io.getstream.chat.android.client.test.randomNotificationChannelDeletedEvent
import io.getstream.chat.android.client.test.randomNotificationChannelMutesUpdatedEvent
import io.getstream.chat.android.client.test.randomNotificationChannelTruncatedEvent
import io.getstream.chat.android.client.test.randomNotificationMarkReadEvent
import io.getstream.chat.android.client.test.randomNotificationMarkUnreadEvent
import io.getstream.chat.android.client.test.randomNotificationMessageNewEvent
import io.getstream.chat.android.client.test.randomNotificationMutesUpdatedEvent
import io.getstream.chat.android.client.test.randomPollDeletedEvent
import io.getstream.chat.android.client.utils.observable.Disposable
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.ChannelMute
import io.getstream.chat.android.models.Mute
import io.getstream.chat.android.models.User
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomChannelMute
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomMute
import io.getstream.chat.android.randomPoll
import io.getstream.chat.android.randomString
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
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

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
            .withMutableGlobalState(MutableGlobalState(currentUser.id))
            .get(this)
        // when
        handler.handleEvents(event)
        // then
        verify(repos).deletePoll(event.poll.id)
    }

    internal class Fixture {
        private var currentUser = randomUser()
        private val subscribeForEvents: (ChatEventListener<ChatEvent>) -> Disposable =
            { _ -> EventHandlerSequential.EMPTY_DISPOSABLE }
        private val logicRegistry: LogicRegistry = mock()
        private val stateRegistry: StateRegistry = mock()
        private val clientState: ClientState = mock {
            on(it.user) doReturn MutableStateFlow(currentUser)
        }
        private var mutableGlobalState: MutableGlobalState? = null
        private var repos: RepositoryFacade = mock()
        private val sideEffect: suspend () -> Unit = {}
        private val syncedEvents: Flow<List<ChatEvent>> = emptyFlow()

        fun withReadEventsCapability(cid: String) = apply {
            repos.stub {
                onBlocking {
                    selectChannel(cid)
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
            scope = scope,
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
        private val currentUser = randomUser()
        private val prepareFixtureWithCurrentUser: Fixture.() -> Unit = {
            withCurrentUser(currentUser)
        }

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
