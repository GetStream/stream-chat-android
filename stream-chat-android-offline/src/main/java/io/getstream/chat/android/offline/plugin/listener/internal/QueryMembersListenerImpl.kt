package io.getstream.chat.android.offline.plugin.listener.internal

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.experimental.plugin.listeners.QueryMembersListener
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.extensions.internal.toCid
import io.getstream.chat.android.offline.repository.builder.internal.RepositoryFacade

/**
 * [QueryMembersListener] implementation for [io.getstream.chat.android.offline.plugin.internal.OfflinePlugin].
 * Handles updating members in the database.
 *
 * @param repos [RepositoryFacade] to cache intermediate data and final result.
 */
internal class QueryMembersListenerImpl(
    private val repos: RepositoryFacade,
) : QueryMembersListener {

    override suspend fun onQueryMembersResult(
        result: Result<List<Member>>,
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySort<Member>,
        members: List<Member>,
    ) {
        if (result.isSuccess) {
            repos.updateMembersForChannel(Pair(channelType, channelId).toCid(), result.data())
        }
    }
}
