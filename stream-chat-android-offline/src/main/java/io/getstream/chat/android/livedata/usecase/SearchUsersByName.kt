package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.usecase.SearchUsersByName as OfflineSearchUsersByName

/**
 * Use case for searching users by string-autocomplete filter. Performs online request if connected or local searching
 * in DB otherwise.
 */
public class SearchUsersByName internal constructor(private val offlineSearchUsersByName: OfflineSearchUsersByName) {
    /**
     * Perform api request with a search string as autocomplete if in online state. Otherwise performs search by name
     * in local database.
     *
     * @param querySearch Search string used as autocomplete.
     * @param offset Offset for paginated requests.
     * @param userLimit The page size in the request.
     * @param userPresence Presence flag to obtain additional info such as last active date.
     */
    public operator fun invoke(
        querySearch: String,
        offset: Int,
        userLimit: Int,
        userPresence: Boolean,
    ): Call<List<User>> = offlineSearchUsersByName.invoke(querySearch, offset, userLimit, userPresence)
}
