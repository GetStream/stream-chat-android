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

package io.getstream.chat.android.client.internal.state.errorhandler.internal

import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class QueryMembersErrorHandlerImplTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private val error = Error.GenericError("offline")
    private val anna = randomMember(user = randomUser(id = "anna", name = "Anna Smith"))
    private val bob = randomMember(user = randomUser(id = "bob", name = "Bob Jones"))
    private val anton = randomMember(user = randomUser(id = "anton", name = "Anton Berg"))
    private val storedMembers = listOf(bob, anton, anna)

    private val channelRepository: ChannelRepository = mock {
        onBlocking { it.selectMembersForChannel("messaging:general") } doReturn storedMembers
    }

    @Test
    fun `offline query returns only the stored members that match the filter`() = runTest {
        val result = queryMembers(filter = Filters.autocomplete("name", "an"))

        result shouldBeEqualTo Result.Success(listOf(anna, anton))
    }

    @Test
    fun `offline query applies the limit after the filter`() = runTest {
        val result = queryMembers(filter = Filters.autocomplete("name", "anton"), limit = 1)

        result shouldBeEqualTo Result.Success(listOf(anton))
    }

    @Test
    fun `offline query sorted by name orders members by user name`() = runTest {
        val result = queryMembers(filter = Filters.neutral(), sort = QuerySortByField.ascByName("name"))

        result shouldBeEqualTo Result.Success(listOf(anna, anton, bob))
    }

    @Test
    fun `online query returns the original error`() = runTest {
        val result = queryMembers(filter = Filters.neutral(), isOnline = true)

        result shouldBeEqualTo Result.Failure(error)
    }

    private suspend fun queryMembers(
        filter: FilterObject,
        limit: Int = 0,
        sort: QuerySorter<Member> = QuerySortByField.ascByName("name"),
        isOnline: Boolean = false,
    ): Result<List<Member>> {
        val clientState: ClientState = mock { on { it.isOnline } doReturn isOnline }
        return QueryMembersErrorHandlerImpl(testCoroutines.scope, clientState, channelRepository)
            .onQueryMembersError(
                originalCall = TestCall(Result.Failure(error)),
                channelType = "messaging",
                channelId = "general",
                offset = 0,
                limit = limit,
                filter = filter,
                sort = sort,
                members = emptyList(),
            )
            .await()
    }
}
