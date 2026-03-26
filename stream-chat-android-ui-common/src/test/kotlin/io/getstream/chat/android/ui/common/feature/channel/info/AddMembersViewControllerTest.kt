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

package io.getstream.chat.android.ui.common.feature.channel.info

import app.cash.turbine.test
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomGenericError
import io.getstream.chat.android.randomMembers
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.ui.common.state.channel.info.AddMembersViewState
import io.getstream.result.Error
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class AddMembersViewControllerTest {

    @Test
    fun `initial state`() = runTest {
        val sut = Fixture().get(backgroundScope)

        assertEquals(AddMembersViewState(), sut.state.value)
    }

    @Test
    fun `channel members are synced into loadedMemberIds`() = runTest {
        val members = randomMembers(3)
        val sut = Fixture()
            .givenMembers(members)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            val state = awaitItem()
            assertEquals(members.map(Member::getUserId).toSet(), state.loadedMemberIds)
        }
    }

    @Test
    fun `channel members update is reflected reactively`() = runTest {
        val fixture = Fixture()
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(2) // Skip initial state and first search result

            val newMembers = randomMembers(2)
            fixture.givenMembers(newMembers)

            val state = awaitItem()
            assertEquals(newMembers.map(Member::getUserId).toSet(), state.loadedMemberIds)
        }
    }

    @Test
    fun `initial search loads users after debounce`() = runTest {
        val users = listOf(randomUser(), randomUser())
        val sut = Fixture()
            .givenQueryUsers(users = users)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial loading state

            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(users, state.searchResult)
        }
    }

    @Test
    fun `initial search error clears loading state`() = runTest {
        val sut = Fixture()
            .givenQueryUsers(error = randomGenericError())
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial loading state

            val state = awaitItem()
            assertFalse(state.isLoading)
            assertTrue(state.searchResult.isEmpty())
        }
    }

    @Test
    fun `QueryChanged updates the query`() = runTest {
        val sut = Fixture()
            .givenQueryUsers(users = emptyList())
            .get(backgroundScope)

        sut.state.test {
            skipItems(2) // Skip initial state and initial search result

            sut.onViewAction(AddMembersViewAction.QueryChanged("John"))

            assertEquals("John", awaitItem().query)
        }
    }

    @Test
    fun `QueryChanged preserves whitespace in the query`() = runTest {
        val sut = Fixture()
            .givenQueryUsers(users = emptyList())
            .get(backgroundScope)

        sut.state.test {
            skipItems(2) // Skip initial state and initial search result

            sut.onViewAction(AddMembersViewAction.QueryChanged("  John  "))

            assertEquals("  John  ", awaitItem().query)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `QueryChanged triggers a new search after debounce`() = runTest {
        val query = "Alice"
        val users = listOf(randomUser(), randomUser())
        val sut = Fixture()
            .givenQueryUsers(users = emptyList()) // initial empty-query search
            .givenQueryUsers(users = users) // search for "Alice"
            .get(backgroundScope)

        sut.state.test {
            skipItems(2) // Skip initial state and empty-query search result

            sut.onViewAction(AddMembersViewAction.QueryChanged(query))
            skipItems(1) // Skip the query-only state update

            assertTrue(awaitItem().isLoading) // New search triggered

            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(query, state.query)
            assertEquals(users, state.searchResult)
        }
    }

    @Test
    fun `QueryChanged search error clears loading state`() = runTest {
        val sut = Fixture()
            .givenQueryUsers(users = emptyList()) // initial search
            .givenQueryUsers(error = randomGenericError()) // error on next search
            .get(backgroundScope)

        sut.state.test {
            skipItems(2) // Skip initial state and empty-query search result

            sut.onViewAction(AddMembersViewAction.QueryChanged("Bob"))
            skipItems(1) // Skip query state update

            assertTrue(awaitItem().isLoading)

            val state = awaitItem()
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `UserClick selects a user`() = runTest {
        val user = randomUser()
        val sut = Fixture()
            .givenQueryUsers(users = listOf(user))
            .get(backgroundScope)

        sut.state.test {
            skipItems(2) // Skip initial state and search result

            sut.onViewAction(AddMembersViewAction.UserClick(user))

            val state = awaitItem()
            assertTrue(user.id in state.selectedUserIds)
            assertTrue(state.isSelected(user))
        }
    }

    @Test
    fun `UserClick deselects an already selected user`() = runTest {
        val user = randomUser()
        val sut = Fixture()
            .givenQueryUsers(users = listOf(user))
            .get(backgroundScope)

        sut.state.test {
            skipItems(2) // Skip initial state and search result

            sut.onViewAction(AddMembersViewAction.UserClick(user))
            skipItems(1) // Skip selected state

            sut.onViewAction(AddMembersViewAction.UserClick(user))

            val state = awaitItem()
            assertFalse(user.id in state.selectedUserIds)
            assertFalse(state.isSelected(user))
        }
    }

    @Test
    fun `UserClick toggles selection independently for multiple users`() = runTest {
        val user1 = randomUser()
        val user2 = randomUser()
        val sut = Fixture()
            .givenQueryUsers(users = listOf(user1, user2))
            .get(backgroundScope)

        sut.state.test {
            skipItems(2) // Skip initial state and search result

            sut.onViewAction(AddMembersViewAction.UserClick(user1))
            skipItems(1) // Skip user1 selected state

            sut.onViewAction(AddMembersViewAction.UserClick(user2))

            val state = awaitItem()
            assertTrue(user1.id in state.selectedUserIds)
            assertTrue(user2.id in state.selectedUserIds)
            assertEquals(listOf(user1, user2), state.selectedUsers)
        }
    }

    @Test
    fun `LoadMore appends results to searchResult`() = runTest {
        val initialUsers = listOf(randomUser(), randomUser())
        val moreUsers = listOf(randomUser(), randomUser())
        val sut = Fixture()
            .givenQueryUsers(users = initialUsers)
            .givenQueryUsers(users = moreUsers)
            .get(backgroundScope)

        sut.state.test {
            skipItems(2) // Skip initial state and initial search result

            sut.onViewAction(AddMembersViewAction.LoadMore)

            assertTrue(awaitItem().isLoadingMore) // Loading more

            val state = awaitItem()
            assertFalse(state.isLoadingMore)
            assertEquals(initialUsers + moreUsers, state.searchResult)
        }
    }

    @Test
    fun `LoadMore error clears isLoadingMore and retains existing results`() = runTest {
        val initialUsers = listOf(randomUser(), randomUser())
        val sut = Fixture()
            .givenQueryUsers(users = initialUsers)
            .givenQueryUsers(error = randomGenericError())
            .get(backgroundScope)

        sut.state.test {
            skipItems(2) // Skip initial state and initial search result

            sut.onViewAction(AddMembersViewAction.LoadMore)

            assertTrue(awaitItem().isLoadingMore) // Loading more

            val state = awaitItem()
            assertFalse(state.isLoadingMore)
            assertEquals(initialUsers, state.searchResult)
        }
    }

    private class Fixture {
        private val channelMembers = MutableStateFlow(emptyList<Member>())
        private val channelState: ChannelState = mock {
            on { members } doReturn channelMembers
        }
        private val chatClient: ChatClient = mock()
        private val queryUsersResults = mutableListOf<Pair<List<User>?, Error?>>()
        private var callCount = 0

        init {
            whenever(chatClient.queryUsers(any())) doAnswer {
                val index = callCount++
                val (users, err) = queryUsersResults.getOrElse(index) { Pair(emptyList<User>(), null) }
                err?.asCall() ?: (users ?: emptyList<User>()).asCall()
            }
        }

        fun givenMembers(members: List<Member>) = apply {
            channelMembers.value = members
        }

        fun givenQueryUsers(
            users: List<User>? = null,
            error: Error? = null,
        ) = apply {
            queryUsersResults.add(Pair(users, error))
        }

        fun get(scope: CoroutineScope) = AddMembersViewController(
            scope = scope,
            chatClient = chatClient,
            channelState = MutableStateFlow(channelState),
        )
    }
}
