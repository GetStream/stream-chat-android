package io.getstream.chat.android.offline.experimental.errorhandler.listener

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.ReturnOnErrorCall
import io.getstream.chat.android.client.call.onErrorReturn
import io.getstream.chat.android.client.experimental.errorhandler.ErrorHandler
import io.getstream.chat.android.client.experimental.errorhandler.listeners.QueryMembersErrorHandler
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.experimental.global.GlobalState
import io.getstream.chat.android.offline.repository.RepositoryFacade
import io.getstream.chat.android.offline.utils.toCid
import kotlinx.coroutines.CoroutineScope

/**
 * [QueryMembersErrorHandler] implementation for [io.getstream.chat.android.offline.experimental.errorhandler.OfflineErrorHandler].
 * Checks if the change was done offline and can be synced.
 *
 * @param scope [CoroutineScope]
 * @param globalState [GlobalState] provided by the [io.getstream.chat.android.offline.experimental.plugin.OfflinePlugin].
 * @param repos [RepositoryFacade] to access datasource.
 */
@ExperimentalStreamChatApi
internal class QueryMembersErrorHandlerImpl(
    private val scope: CoroutineScope,
    private val globalState: GlobalState,
    private val repos: RepositoryFacade,
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
                val membersFromDatabase = repos
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
