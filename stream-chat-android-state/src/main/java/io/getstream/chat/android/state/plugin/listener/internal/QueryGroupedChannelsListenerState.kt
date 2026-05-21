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
import io.getstream.chat.android.state.event.handler.grouped.internal.GroupedUnreadChannelsUpdater
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.state.plugin.state.querychannels.GroupedQueryConfig
import io.getstream.result.Result

internal class QueryGroupedChannelsListenerState(
    private val logic: LogicRegistry,
    private val globalState: MutableGlobalState,
    private val groupedUnreadChannelsUpdater: GroupedUnreadChannelsUpdater,
) : QueryGroupedChannelsListener {

    override suspend fun onQueryGroupedChannelsRequest(
        limit: Int?,
        groups: Map<String, GroupedChannelsGroupQuery>?,
        watch: Boolean,
        presence: Boolean,
    ) {
        // Capture config for every explicitly named group BEFORE the network call so that a
        // failed request still leaves enough state for SyncManager to retry with the same
        // parameters. When `groups == null` the caller is relying on the server's default group
        // set — we don't know the keys until the response, so we defer to the result-side capture
        // (which only fires on success, but in that case there is no failure to recover from).
        groups?.forEach { (key, groupQuery) ->
            logic.queryChannels(QueryChannelsIdentifier.Grouped(key))
                .setGroupedQueryConfig(
                    GroupedQueryConfig(
                        limit = limit,
                        pageSize = groupQuery.limit,
                        watch = watch,
                        presence = presence,
                    ),
                )
        }
    }

    override suspend fun onQueryGroupedChannelsResult(
        result: Result<GroupedChannels>,
        limit: Int?,
        groups: Map<String, GroupedChannelsGroupQuery>?,
        watch: Boolean,
        presence: Boolean,
    ) {
        if (result !is Result.Success) return

        val next = groupedUnreadChannelsUpdater.calculateUpdatedCounts(
            current = globalState.groupedUnreadChannels.value,
            result = result.value,
        )
        globalState.setGroupedUnreadChannels(next)

        // Route each returned group's channels into the per-group state. The captured config lets
        // both ChannelListViewModel.loadMoreGroupedChannels and SyncManager.updateGroupedQueryChannels
        // reuse the caller's original parameters on paginated and recovery calls respectively.
        result.value.groups.forEach { (key, group) ->
            // A request without a `next` cursor for this key (or no per-group query at all) is
            // a first-page request → replace channels. With a `next` cursor → paginated → append.
            val isFirstPage = groups?.get(key)?.next == null
            val perGroupLimit = groups?.get(key)?.limit
            val queryLogic = logic.queryChannels(QueryChannelsIdentifier.Grouped(key))
            queryLogic.setGroupedQueryConfig(
                GroupedQueryConfig(
                    limit = limit,
                    pageSize = perGroupLimit,
                    watch = watch,
                    presence = presence,
                ),
            )
            queryLogic.applyGroupedResult(group, isFirstPage = isFirstPage)
        }
    }
}
