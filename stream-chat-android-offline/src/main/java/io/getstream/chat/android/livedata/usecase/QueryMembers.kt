package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomainImpl

public class QueryMembers internal constructor(private val domainImpl: ChatDomainImpl) {

    public operator fun invoke(
        cid: String,
        offset: Int = 0,
        limit: Int = 0,
        filter: FilterObject = FilterObject(),
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
            .execute()

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
        val membersFromDatabase = domainImpl
            .repos
            .selectMembersForChannel(cid)
            .sortedWith(sort.comparator)
            .drop(offset.coerceAtLeast(0))
            .let { afterDrop ->
                limit.coerceAtLeast(0).let { clampedLimit ->
                    if (clampedLimit > 0) {
                        afterDrop.take(clampedLimit)
                    } else afterDrop
                }
            }

        return Result(membersFromDatabase)
    }
}
