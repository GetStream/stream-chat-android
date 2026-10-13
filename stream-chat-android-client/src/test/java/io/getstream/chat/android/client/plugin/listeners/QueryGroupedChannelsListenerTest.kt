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

package io.getstream.chat.android.client.plugin.listeners

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.GroupedChannels
import io.getstream.chat.android.models.GroupedChannelsGroupQuery
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class QueryGroupedChannelsListenerTest {

    @Test
    fun `sort overloads call the callbacks without the sort by default`() = runTest {
        val listener = RecordingListener()
        val groups = mapOf("expired" to GroupedChannelsGroupQuery(limit = 5))
        val result = Result.Success(GroupedChannels(groups = emptyMap()))
        val sort = QuerySortByField.descByName<Channel>("query_timestamp")

        listener.onQueryGroupedChannelsRequest(limit = 10, groups = groups, watch = true, presence = false, sort = sort)
        listener.onQueryGroupedChannelsResult(result, limit = 10, groups = groups, watch = true, presence = false, sort = sort)

        assertEquals(listOf("request" to groups, "result" to groups), listener.calls)
    }

    private class RecordingListener : QueryGroupedChannelsListener {
        val calls = mutableListOf<Pair<String, Map<String, GroupedChannelsGroupQuery>?>>()

        override suspend fun onQueryGroupedChannelsRequest(
            limit: Int?,
            groups: Map<String, GroupedChannelsGroupQuery>?,
            watch: Boolean,
            presence: Boolean,
        ) {
            calls += "request" to groups
        }

        override suspend fun onQueryGroupedChannelsResult(
            result: Result<GroupedChannels>,
            limit: Int?,
            groups: Map<String, GroupedChannelsGroupQuery>?,
            watch: Boolean,
            presence: Boolean,
        ) {
            calls += "result" to groups
        }
    }
}
