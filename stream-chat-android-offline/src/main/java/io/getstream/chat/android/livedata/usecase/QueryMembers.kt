package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.NeutralFilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.offline.usecase.QueryMembers as OfflineQueryMembers

/**
 * UseCase for querying members of a channel
 *
 * @property domainImpl instance of a ChatDomain
 */
public class QueryMembers internal constructor(private val offlineQueryMember: OfflineQueryMembers) {

    /**
     * Obtains an executable coroutine call for querying members
     *
     * @param cid CID of the Channel whose members we are querying
     * @param offset indicates how many items to exclude from the start of the result
     * @param limit indicates the maximum allowed number of items in the result
     * @param filter applied to online queries for advanced selection criteria
     * @param sort the sort criteria applied to the result
     * @param members
     * @return
     */
    public operator fun invoke(
        cid: String,
        offset: Int = 0,
        limit: Int = 0,
        filter: FilterObject = NeutralFilterObject,
        sort: QuerySort<Member> = QuerySort.desc(Member::createdAt),
        members: List<Member> = emptyList(),
    ): Call<List<Member>> = offlineQueryMember.invoke(cid, offset, limit, filter, sort, members)
}
