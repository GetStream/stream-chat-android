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

package io.getstream.chat.android.compose.sample.feature.channel.list

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.sample.ChatHelper
import io.getstream.chat.android.compose.sample.data.CustomSettings
import io.getstream.chat.android.compose.sample.data.StagingEnvironment
import io.getstream.chat.android.compose.viewmodel.channels.ChannelListSearchOptions
import io.getstream.chat.android.compose.viewmodel.channels.ChannelListViewModelFactory
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.querysort.QuerySortByField

/**
 * Builds the channel list factory used by the sample.
 *
 * When the local unread count is enabled, an explicit filter including livestream channels is used,
 * to make the feature testable. Otherwise, the predefined server-side filter is used, which resolves
 * to: messaging channels the current user is a member of, without a draft, sorted by "pinned_at" and
 * "last_updated" descending.
 *
 * When member name search is enabled, the channel search also matches channels by the name of a member.
 */
fun sampleChannelListViewModelFactory(settings: CustomSettings): ChannelListViewModelFactory {
    val chatClient = ChatClient.instance()
    val currentUserId = chatClient.getCurrentUser()?.id.orEmpty()
    val searchOptions = if (settings.isMemberNameSearchEnabled) {
        MemberNameSearchOptions()
    } else {
        ChannelListSearchOptions()
    }
    // The staging app has no predefined filter.
    return if (settings.isLocalUnreadCountEnabled || ChatHelper.apiKey == StagingEnvironment.API_KEY) {
        ChannelListViewModelFactory(
            chatClient = chatClient,
            querySort = QuerySortByField<Channel>().desc("pinned_at").desc("last_updated"),
            filters = Filters.and(
                Filters.`in`("type", listOf("messaging", "livestream")),
                Filters.`in`("members", listOf(currentUserId)),
                Filters.or(Filters.notExists("draft"), Filters.eq("draft", false)),
            ),
            chatEventHandlerFactory = CustomChatEventHandlerFactory(),
            searchOptions = searchOptions,
        )
    } else {
        ChannelListViewModelFactory(
            chatClient = chatClient,
            predefinedFilterName = "android_sample_filter",
            filterValues = mapOf(
                "channel_type" to "messaging",
                "user_id" to currentUserId,
            ),
            chatEventHandlerFactory = CustomChatEventHandlerFactory(),
            searchOptions = searchOptions,
        )
    }
}

/**
 * Searches channels by their name or by the name of one of their members, among the channels the current user is a
 * member of.
 */
private class MemberNameSearchOptions : ChannelListSearchOptions() {
    override fun channelFilter(params: ChannelSearchParams): FilterObject {
        val searchFilter = Filters.and(
            Filters.or(
                Filters.autocomplete("name", params.query),
                Filters.autocomplete("member.user.name", params.query),
            ),
            Filters.`in`("members", listOfNotNull(params.currentUser?.id)),
        )
        return params.listFilter?.let { Filters.and(it, searchFilter) } ?: searchFilter
    }
}
