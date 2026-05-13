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
import io.getstream.chat.android.client.plugin.listeners.QueryGroupedChannelsListener
import io.getstream.chat.android.models.GroupedChannels
import io.getstream.chat.android.models.GroupedChannelsGroupQuery
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.result.Result

internal class QueryGroupedChannelsListenerState(
    private val logic: LogicRegistry,
    private val globalState: MutableGlobalState,
) : QueryGroupedChannelsListener {

    override suspend fun onQueryGroupedChannelsResult(
        result: Result<GroupedChannels>,
        limit: Int?,
        groups: Map<String, GroupedChannelsGroupQuery>?,
        watch: Boolean,
        presence: Boolean,
    ) {
        if (result !is Result.Success) return

        // The request may include any subset of groups (pagination, custom per-group limits,
        // or the default set). Always merge the returned counts into the existing map so groups
        // not present in this response retain their previous counts.
        val returnedUnreadCounts = result.value.groups.mapValues { (_, group) -> group.unreadChannels }
        val merged = globalState.groupedUnreadChannels.value + returnedUnreadCounts
        globalState.setGroupedUnreadChannels(merged)

        // Route each returned group's channels into the per-group state.
        result.value.groups.forEach { (key, group) ->
            // A request without a `next` cursor for this key (or no per-group query at all) is
            // a first-page request → replace channels. With a `next` cursor → paginated → append.
            val isFirstPage = groups?.get(key)?.next == null
            logic.queryChannels(QueryChannelsIdentifier.Grouped(key))
                .applyGroupedResult(group, isFirstPage = isFirstPage)
        }
    }
}
