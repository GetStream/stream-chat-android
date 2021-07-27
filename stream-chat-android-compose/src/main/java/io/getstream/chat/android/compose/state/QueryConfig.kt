package io.getstream.chat.android.compose.state

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort

/**
 * The configuration for querying various bits of data. It's generic, so it can be used to query
 * Channels, Messages or something else.
 *
 * @param filters - The [FilterObject] to apply to the query.
 * @param querySort - The sorting option for the query results.
 * */
public data class QueryConfig<T : Any>(
    val filters: FilterObject,
    val querySort: QuerySort<T>,
)
