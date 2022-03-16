package io.getstream.chat.android.offline.internal.errorhandler

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.ReturnOnErrorCall
import io.getstream.chat.android.client.call.onErrorReturn
import io.getstream.chat.android.client.experimental.errorhandler.ErrorHandler
import io.getstream.chat.android.client.experimental.errorhandler.QueryMembersErrorHandler
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.internal.extensions.toCid
import io.getstream.chat.android.offline.internal.repository.RepositoryFacade
import io.getstream.chat.android.offline.internal.repository.domain.channel.ChannelRepository
import io.getstream.chat.android.offline.plugin.state.global.GlobalState
import kotlinx.coroutines.CoroutineScope

/**
 * [QueryMembersErrorHandler] implementation for [io.getstream.chat.android.offline.plugin.internal.OfflinePlugin].
 * Checks if the change was done offline and can be synced.
 *
 * @param scope [CoroutineScope]
 * @param globalState [GlobalState] provided by the [io.getstream.chat.android.offline.plugin.internal.OfflinePlugin].
 * @param repos [RepositoryFacade] to access datasource.
 */
internal class QueryMembersErrorHandlerImpl(
    private val scope: CoroutineScope,
    private val globalState: GlobalState,
    private val channelRepository: ChannelRepository,
) : QueryMembersErrorHandler {

    override fun onQueryMembersError(
        originalCall: Call<List<Member>>,
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySort<Member>,
        members: List<Member>,
    ): ReturnOnErrorCall<List<Member>> {
        return originalCall.onErrorReturn(scope) { originalError ->
            if (globalState.isOnline()) {
                Result.error(originalError)
            } else {
                // retrieve from database
                val clampedOffset = offset.coerceAtLeast(0)
                val clampedLimit = limit.coerceAtLeast(0)
                val membersFromDatabase = channelRepository
                    .selectMembersForChannel(Pair(channelType, channelId).toCid())
                    .sortedWith(sort.comparator)
                    .drop(clampedOffset)
                    .let { members ->
                        if (clampedLimit > 0) {
                            members.take(clampedLimit)
                        } else members
                    }
                Result(membersFromDatabase)
            }
        }
    }

    override val name: String
        get() = "QueryMembersErrorHandlerImpl"

    override val priority: Int
        get() = ErrorHandler.DEFAULT_PRIORITY
}
