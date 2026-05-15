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

package io.getstream.chat.android.state.plugin.listener.internal

import io.getstream.chat.android.client.internal.state.plugin.QueryChannelsIdentifier
import io.getstream.chat.android.models.GroupedChannels
import io.getstream.chat.android.models.GroupedChannelsGroup
import io.getstream.chat.android.models.GroupedChannelsGroupQuery
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.state.plugin.logic.querychannels.internal.QueryChannelsLogic
import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.state.plugin.state.querychannels.GroupedQueryConfig
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class QueryGroupedChannelsListenerStateTest {

    private val queryChannelsLogic: QueryChannelsLogic = mock()
    private val logic: LogicRegistry = mock {
        on(it.queryChannels(any<QueryChannelsIdentifier>())) doReturn queryChannelsLogic
    }
    private val globalState: MutableGlobalState = mock()
    private val listener = QueryGroupedChannelsListenerState(logic, globalState)

    @Test
    fun `successful result merges returned unread counts into existing global state`() = runTest {
        // given
        whenever(globalState.groupedUnreadChannels) doReturn MutableStateFlow(
            mapOf("direct" to 4, "support" to 1),
        )
        doNothing().`when`(globalState).setGroupedUnreadChannels(any())
        val result = Result.Success(
            value = GroupedChannels(
                groups = mapOf(
                    "support" to GroupedChannelsGroup(
                        groupKey = "support",
                        channels = emptyList(),
                        unreadChannels = 7,
                        next = null,
                        prev = null,
                    ),
                ),
            ),
        )
        // when
        listener.onQueryGroupedChannelsResult(
            result = result,
            limit = null,
            groups = mapOf("support" to GroupedChannelsGroupQuery(next = "cursor")),
            watch = false,
            presence = false,
        )
        // then - merged: direct stays at 4, support updated to 7
        verify(globalState, times(1)).setGroupedUnreadChannels(
            mapOf("direct" to 4, "support" to 7),
        )
    }

    @Test
    fun `successful result with groups null merges into existing state`() = runTest {
        // given
        whenever(globalState.groupedUnreadChannels) doReturn MutableStateFlow(
            mapOf("direct" to 4),
        )
        doNothing().`when`(globalState).setGroupedUnreadChannels(any())
        val result = Result.Success(
            value = GroupedChannels(
                groups = mapOf(
                    "direct" to GroupedChannelsGroup(
                        groupKey = "direct",
                        channels = emptyList(),
                        unreadChannels = 2,
                        next = null,
                        prev = null,
                    ),
                    "support" to GroupedChannelsGroup(
                        groupKey = "support",
                        channels = emptyList(),
                        unreadChannels = 0,
                        next = null,
                        prev = null,
                    ),
                ),
            ),
        )
        // when - groups param is null (default set requested)
        listener.onQueryGroupedChannelsResult(
            result = result,
            limit = null,
            groups = null,
            watch = false,
            presence = false,
        )
        // then - direct updated to 2, support added with 0; merging preserves any pre-existing keys
        verify(globalState, times(1)).setGroupedUnreadChannels(
            mapOf("direct" to 2, "support" to 0),
        )
    }

    @Test
    fun `failure result does not touch global state`() = runTest {
        // given
        val result = Result.Failure(Error.GenericError("network"))
        // when
        listener.onQueryGroupedChannelsResult(
            result = result,
            limit = null,
            groups = null,
            watch = false,
            presence = false,
        )
        // then
        verify(globalState, never()).setGroupedUnreadChannels(any())
    }

    @Test
    fun `success routes each returned group to the matching Grouped identifier as first page when no next cursor was requested`() =
        runTest {
            // given
            whenever(globalState.groupedUnreadChannels) doReturn MutableStateFlow(emptyMap())
            doNothing().`when`(globalState).setGroupedUnreadChannels(any())
            val groupDirect = GroupedChannelsGroup(
                groupKey = "direct",
                channels = emptyList(),
                unreadChannels = 0,
                next = null,
                prev = null,
            )
            val groupSupport = GroupedChannelsGroup(
                groupKey = "support",
                channels = emptyList(),
                unreadChannels = 0,
                next = "cursor-support",
                prev = null,
            )
            val result = Result.Success(
                value = GroupedChannels(
                    groups = mapOf("direct" to groupDirect, "support" to groupSupport),
                ),
            )
            // when - groups param is null (default set requested → both treated as first page)
            listener.onQueryGroupedChannelsResult(
                result = result,
                limit = null,
                groups = null,
                watch = false,
                presence = false,
            )
            // then
            verify(logic).queryChannels(eq(QueryChannelsIdentifier.Grouped("direct")))
            verify(logic).queryChannels(eq(QueryChannelsIdentifier.Grouped("support")))
            verify(queryChannelsLogic).applyGroupedResult(groupDirect, isFirstPage = true)
            verify(queryChannelsLogic).applyGroupedResult(groupSupport, isFirstPage = true)
        }

    @Test
    fun `onQueryGroupedChannelsRequest with explicit groups writes config per requested key`() = runTest {
        // when
        listener.onQueryGroupedChannelsRequest(
            limit = 20,
            groups = mapOf(
                "a" to GroupedChannelsGroupQuery(limit = 5),
                "b" to GroupedChannelsGroupQuery(),
            ),
            watch = true,
            presence = false,
        )
        // then - per-group override captured for "a", request-level only for "b"
        verify(queryChannelsLogic).setGroupedQueryConfig(
            GroupedQueryConfig(limit = 20, pageSize = 5, watch = true, presence = false),
        )
        verify(queryChannelsLogic).setGroupedQueryConfig(
            GroupedQueryConfig(limit = 20, pageSize = null, watch = true, presence = false),
        )
        verify(logic).queryChannels(eq(QueryChannelsIdentifier.Grouped("a")))
        verify(logic).queryChannels(eq(QueryChannelsIdentifier.Grouped("b")))
    }

    @Test
    fun `onQueryGroupedChannelsRequest with null groups writes nothing`() = runTest {
        // when
        listener.onQueryGroupedChannelsRequest(
            limit = null,
            groups = null,
            watch = true,
            presence = false,
        )
        // then - no per-group keys to write to; defer to result-side capture
        verify(queryChannelsLogic, never()).setGroupedQueryConfig(any())
    }

    @Test
    fun `success writes captured config with per-group override to matching group and general limit to others`() =
        runTest {
            // given
            whenever(globalState.groupedUnreadChannels) doReturn MutableStateFlow(emptyMap())
            doNothing().`when`(globalState).setGroupedUnreadChannels(any())
            val groupA = GroupedChannelsGroup(groupKey = "a", channels = emptyList())
            val groupB = GroupedChannelsGroup(groupKey = "b", channels = emptyList())
            val result = Result.Success(
                value = GroupedChannels(groups = mapOf("a" to groupA, "b" to groupB)),
            )
            // when - "a" has a per-group limit override; "b" only the request-level fallback
            listener.onQueryGroupedChannelsResult(
                result = result,
                limit = 20,
                groups = mapOf("a" to GroupedChannelsGroupQuery(limit = 5)),
                watch = true,
                presence = false,
            )
            // then - per-group override captured for "a", request-level only for "b"
            verify(queryChannelsLogic).setGroupedQueryConfig(
                GroupedQueryConfig(limit = 20, pageSize = 5, watch = true, presence = false),
            )
            verify(queryChannelsLogic).setGroupedQueryConfig(
                GroupedQueryConfig(limit = 20, pageSize = null, watch = true, presence = false),
            )
        }

    @Test
    fun `success with all-null request still captures watch and presence flags`() = runTest {
        // given - recovery-shaped call where the SDK relies solely on server defaults
        whenever(globalState.groupedUnreadChannels) doReturn MutableStateFlow(emptyMap())
        doNothing().`when`(globalState).setGroupedUnreadChannels(any())
        val group = GroupedChannelsGroup(groupKey = "direct", channels = emptyList())
        val result = Result.Success(GroupedChannels(groups = mapOf("direct" to group)))
        // when
        listener.onQueryGroupedChannelsResult(
            result = result,
            limit = null,
            groups = null,
            watch = true,
            presence = true,
        )
        // then - config still written so subsequent pagination knows the watch/presence flags
        verify(queryChannelsLogic).setGroupedQueryConfig(
            GroupedQueryConfig(limit = null, pageSize = null, watch = true, presence = true),
        )
    }

    @Test
    fun `success treats keys with requested next cursor as paginated`() = runTest {
        // given
        whenever(globalState.groupedUnreadChannels) doReturn MutableStateFlow(emptyMap())
        doNothing().`when`(globalState).setGroupedUnreadChannels(any())
        val groupSupport = GroupedChannelsGroup(
            groupKey = "support",
            channels = emptyList(),
            unreadChannels = 0,
            next = null,
            prev = null,
        )
        val result = Result.Success(
            value = GroupedChannels(groups = mapOf("support" to groupSupport)),
        )
        // when - the request passed a next cursor for "support"
        listener.onQueryGroupedChannelsResult(
            result = result,
            limit = null,
            groups = mapOf("support" to GroupedChannelsGroupQuery(next = "cursor")),
            watch = false,
            presence = false,
        )
        // then
        verify(queryChannelsLogic).applyGroupedResult(groupSupport, isFirstPage = false)
    }
}
