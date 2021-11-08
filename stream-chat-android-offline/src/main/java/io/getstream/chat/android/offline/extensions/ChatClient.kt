package io.getstream.chat.android.offline.extensions

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.NeutralFilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.offline.ChatDomain

/**
 * Query members of a channel.
 *
 * @param cid CID of the Channel whose members we are querying.
 * @param offset Indicates how many items to exclude from the start of the result.
 * @param limit Indicates the maximum allowed number of items in the result.
 * @param filter Filter applied to online queries for advanced selection criteria.
 * @param sort The sort criteria applied to the result.
 * @param members
 *
 * @return Executable async [Call] querying members.
 */
@JvmOverloads
@CheckResult
public fun ChatClient.requestMembers(
    cid: String,
    offset: Int = 0,
    limit: Int = 0,
    filter: FilterObject = NeutralFilterObject,
    sort: QuerySort<Member> = QuerySort.desc(Member::createdAt),
    members: List<Member> = emptyList(),
): Call<List<Member>> = ChatDomain.instance().queryMembers(cid, offset, limit, filter, sort, members)
