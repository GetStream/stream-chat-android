package io.getstream.chat.android.livedata.usecase

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomainImpl

public class SearchUsersByName internal constructor(private val chatDomainImpl: ChatDomainImpl) {

    @VisibleForTesting
    internal val defaultUsersQueryFilter by lazy {
        Filters.and(
            Filters.ne("name", ""),
            Filters.ne("id", chatDomainImpl.currentUser.id)
        )
    }

    /**
     * Perform api request with a search string as autocomplete if in online state. Otherwise performs search by name
     * in local database.
     *
     * @param querySearch Search string used as autocomplete.
     */
    public operator fun invoke(querySearch: String, offset: Int, userLimit: Int): Call<List<User>> {
        return CoroutineCall(chatDomainImpl.scope) {
            if (chatDomainImpl.isOnline()) {
                val filter = if (querySearch.isEmpty()) {
                    defaultUsersQueryFilter
                } else {
                    Filters.and(
                        Filters.autocomplete(FIELD_NAME, querySearch),
                        Filters.ne(FIELD_ID, chatDomainImpl.currentUser.id)
                    )
                }

                chatDomainImpl.client.queryUsers(
                    QueryUsersRequest(
                        filter = filter,
                        offset = offset,
                        limit = userLimit,
                        querySort = USERS_QUERY_SORT,
                        presence = true
                    )
                ).execute().also { result ->
                    if (result.isSuccess && result.data().isNotEmpty()) {
                        chatDomainImpl.repos.insertUsers(result.data())
                    }
                }
            } else {
                Result(emptyList())
            }
        }
    }

    internal companion object {
        private val USERS_QUERY_SORT = QuerySort.asc(User::name)

        @VisibleForTesting
        internal const val FIELD_NAME = "name"
        private const val FIELD_ID = "id"
    }
}
