@file:JvmName("ChatClientExtensions")

package io.getstream.chat.android.offline.extensions

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.ChatDomain

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
