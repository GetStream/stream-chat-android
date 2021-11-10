@file:JvmName("ChatClientExtensions")

package io.getstream.chat.android.offline.extensions

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.NeutralFilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.usecase.DownloadAttachment

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

/**
 * Perform api request with a search string as autocomplete if in online state. Otherwise performs search by name
 * in local database.
 *
 * @param querySearch Search string used as autocomplete.
 * @param offset Offset for paginated requests.
 * @param userLimit The page size in the request.
 * @param userPresence Presence flag to obtain additional info such as last active date.
 *
 * @return Executable async [Call] querying users.
 */
@CheckResult
public fun ChatClient.searchUsersByName(
    querySearch: String,
    offset: Int,
    userLimit: Int,
    userPresence: Boolean,
): Call<List<User>> = ChatDomain.instance().searchUsersByName(querySearch, offset, userLimit, userPresence)

/**
 * Downloads the selected attachment to the "Download" folder in the public external storage directory.
 *
 * @param attachment The attachment to download.
 *
 * @return Executable async [Call] downloading attachment.
 */
@CheckResult
public fun ChatClient.downloadAttachment(attachment: Attachment): Call<Unit> =
    DownloadAttachment(ChatDomain.instance() as ChatDomainImpl).invoke(attachment)
