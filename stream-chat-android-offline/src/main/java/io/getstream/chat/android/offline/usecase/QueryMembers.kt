package io.getstream.chat.android.offline.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.NeutralFilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.ChatDomainImpl

/**
 * UseCase for querying members of a channel.
 *
 * @property domainImpl Instance of a ChatDomain.
 */
internal class QueryMembers(private val domainImpl: ChatDomainImpl) {

    /**
     * Obtains an executable coroutine call for querying members
     *
     * @param cid CID of the Channel whose members we are querying.
     * @param offset Indicates how many items to exclude from the start of the result.
     * @param limit Indicates the maximum allowed number of items in the result.
     * @param filter Applied to online queries for advanced selection criteria.
     * @param sort The sort criteria applied to the result.
     * @param members
     * @return
     */
    @CheckResult
    operator fun invoke(
        cid: String,
        offset: Int = 0,
        limit: Int = 0,
        filter: FilterObject = NeutralFilterObject,
        sort: QuerySort<Member> = QuerySort.desc(Member::createdAt),
        members: List<Member> = emptyList(),
    ): Call<List<Member>> {
        return CoroutineCall(domainImpl.scope) {
            if (domainImpl.isOffline()) {
                queryMembersOffline(cid, sort, offset, limit)
            } else {
                queryMembersOnline(cid, offset, limit, filter, sort, members)
            }
        }
    }

    private suspend fun queryMembersOnline(
        cid: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySort<Member>,
        members: List<Member>,
    ): Result<List<Member>> {

        val result = domainImpl.client
            .channel(cid)
            .queryMembers(offset, limit, filter, sort, members)
            .await()

        if (result.isSuccess) {
            domainImpl.repos.updateMembersForChannel(cid, result.data())
        }

        return result
    }

    private suspend fun queryMembersOffline(
        cid: String,
        sort: QuerySort<Member>,
        offset: Int,
        limit: Int,
    ): Result<List<Member>> {
        // retrieve from database
        val clampedOffset = offset.coerceAtLeast(0)
        val clampedLimit = limit.coerceAtLeast(0)
        val membersFromDatabase = domainImpl
            .repos
            .selectMembersForChannel(cid)
            .sortedWith(sort.comparator)
            .drop(clampedOffset)
            .let { members ->
                if (clampedLimit > 0) {
                    members.take(clampedLimit)
                } else members
            }

        return Result(membersFromDatabase)
    }
}
