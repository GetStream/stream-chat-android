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

package io.getstream.chat.android.client.internal.state.plugin.listener.internal

import io.getstream.chat.android.client.api.models.PredefinedFilter
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QueryChannelsResult
import io.getstream.chat.android.client.internal.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.client.internal.state.plugin.logic.querychannels.internal.QueryChannelsLogic
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.randomChannel
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

internal class QueryChannelsListenerStateTest {

    private lateinit var queryChannelsLogic: QueryChannelsLogic
    private lateinit var logicRegistry: LogicRegistry
    private lateinit var queryingChannelsFree: MutableStateFlow<Boolean>
    private lateinit var listener: QueryChannelsListenerState

    @BeforeEach
    fun setUp() {
        queryChannelsLogic = mock()
        logicRegistry = mock {
            on { queryChannels(any<QueryChannelsRequest>()) } doReturn queryChannelsLogic
        }
        queryingChannelsFree = MutableStateFlow(true)
        listener = QueryChannelsListenerState(logicRegistry, queryingChannelsFree)
    }

    @Test
    fun `onQueryChannelsResult applies resolved spec when predefinedFilter is present`() = runTest {
        // Given
        val request = QueryChannelsRequest(
            limit = 30,
            predefinedFilter = "my-filter",
            filterValues = mapOf("a" to 1),
        )
        val resolvedFilter = Filters.eq("type", "messaging")
        val resolvedSort = QuerySortByField.descByName<Channel>("last_message_at")
        val result = Result.Success(
            QueryChannelsResult(
                channels = listOf(randomChannel()),
                predefinedFilter = PredefinedFilter(resolvedFilter, resolvedSort),
            ),
        )

        // When
        listener.onQueryChannelsResult(result, request)

        // Then
        verify(queryChannelsLogic).applyResolvedSpec(eq(resolvedFilter), eq(resolvedSort))
    }

    @Test
    fun `onQueryChannelsResult does not apply resolved spec for plain success without predefinedFilter`() = runTest {
        // Given
        val request = QueryChannelsRequest(
            filter = Filters.eq("type", "messaging"),
            querySort = QuerySortByField.descByName("last_message_at"),
            limit = 30,
        )
        val result = Result.Success(
            QueryChannelsResult(channels = emptyList(), predefinedFilter = null),
        )

        // When
        listener.onQueryChannelsResult(result, request)

        // Then
        verify(queryChannelsLogic, never()).applyResolvedSpec(any(), any())
    }

    @Test
    fun `onQueryChannelsResult does not apply resolved spec on failure`() = runTest {
        // Given
        val request = QueryChannelsRequest(
            limit = 30,
            predefinedFilter = "my-filter",
        )
        val result: Result<QueryChannelsResult> = Result.Failure(Error.GenericError("boom"))

        // When
        listener.onQueryChannelsResult(result, request)

        // Then
        verify(queryChannelsLogic, never()).applyResolvedSpec(any(), any())
    }

    @Test
    fun `onQueryChannelsResult forwards channels to the logic and frees the channel-querying flag`() = runTest {
        // Given
        val request = QueryChannelsRequest(filter = Filters.neutral(), limit = 30)
        val channels = listOf(randomChannel())
        val result = Result.Success(QueryChannelsResult(channels = channels, predefinedFilter = null))
        queryingChannelsFree.value = false

        // When
        listener.onQueryChannelsResult(result, request)

        // Then
        verify(queryChannelsLogic).onQueryChannelsResult(any(), eq(request))
        assertTrue(queryingChannelsFree.value)
    }
}
