package io.getstream.chat.android.compose.viewmodel.channels

import io.getstream.chat.android.compose.state.QueryConfig
import io.getstream.chat.android.compose.state.channels.list.SearchQuery
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters

internal fun SearchQuery.getConfig(defaultConfig: QueryConfig<Channel>): QueryConfig<Channel> {
    return when (this) {
        is SearchQuery.Channels -> defaultConfig.copy(
            filters = createQueryChannelsFilter(defaultConfig.filters, query),
        )

        is SearchQuery.Messages -> defaultConfig
        is SearchQuery.Empty -> defaultConfig
    }
}

/**
 * Creates a filter that is used to query channels.
 *
 * If the [searchQuery] is empty, then returns the original [filter] provided by the user.
 * Otherwise, returns a wrapped [filter] that also checks that the channel name match the
 * [searchQuery].
 *
 * @param filter The filter that was passed by the user.
 * @param searchQuery The search query used to filter the channels.
 *
 * @return The filter that will be used to query channels.
 */
private fun createQueryChannelsFilter(filter: FilterObject, searchQuery: String): FilterObject {
    return if (searchQuery.isNotEmpty()) {
        Filters.and(
            filter,
            Filters.or(
                Filters.and(
                    Filters.autocomplete("member.user.name", searchQuery),
                    Filters.notExists("name"),
                ),
                Filters.autocomplete("name", searchQuery),
            ),
        )
    } else {
        filter
    }
}