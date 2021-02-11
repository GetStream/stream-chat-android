package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomainImpl

public interface QueryMembers {

    @CheckResult
    public operator fun invoke(
        channelType: String,
        channelId: String,
        offset: Int = 0,
        limit: Int = 0,
        filter: FilterObject = FilterObject(),
        sort: QuerySort<Member> = QuerySort.desc(Member::createdAt),
        members: List<Member> = emptyList(),
    ): Call<List<Member>>
}

internal class QueryMembersImpl(private val domainImpl: ChatDomainImpl) : QueryMembers {
    override fun invoke(
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySort<Member>,
        members: List<Member>,
    ): Call<List<Member>> {
        return CoroutineCall(domainImpl.scope) {
            if (domainImpl.isOffline() && domainImpl.offlineEnabled) {
                val offlineMembers: List<Member> =
                    domainImpl.channel(channelType, channelId).members.value ?: emptyList()
                Result(offlineMembers)
            } else {
                domainImpl
                    .client
                    .channel(channelType, channelId)
                    .queryMembers(offset, limit, filter, sort, members)
                    .execute()
            }
        }
    }
}
