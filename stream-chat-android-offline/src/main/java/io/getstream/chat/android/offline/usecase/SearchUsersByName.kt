package io.getstream.chat.android.offline.usecase

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.ChatDomainImpl

/**
 * Use case for searching users by string-autocomplete filter. Performs online request if connected or local searching
 * in DB otherwise.
 */
internal class SearchUsersByName(private val chatDomainImpl: ChatDomainImpl) {

    @VisibleForTesting
    internal fun defaultUsersQueryFilter(userId: String): FilterObject {
        return Filters.and(
            Filters.ne(FIELD_NAME, ""), Filters.ne(FIELD_ID, userId)
        )
    }

    /**
     * Perform api request with a search string as autocomplete if in online state. Otherwise performs search by name
     * in local database.
     *
     * @param querySearch Search string used as autocomplete.
     * @param offset Offset for paginated requests.
     * @param userLimit The page size in the request.
     * @param userPresence Presence flag to obtain additional info such as last active date.
     */
    operator fun invoke(
        querySearch: String,
        offset: Int,
        userLimit: Int,
        userPresence: Boolean,
    ): Call<List<User>> {
        return CoroutineCall(chatDomainImpl.scope) {
            if (chatDomainImpl.isOnline()) {
                performOnlineSearch(querySearch, offset, userLimit, userPresence)
            } else {
                performOfflineSearch(querySearch, offset, userLimit)
            }
        }
    }

    private suspend fun performOfflineSearch(querySearch: String, offset: Int, userLimit: Int): Result<List<User>> {
        return if (querySearch.isEmpty()) {
            Result(chatDomainImpl.repos.selectAllUsers(userLimit, offset))
        } else {
            Result(chatDomainImpl.repos.selectUsersLikeName(querySearch, userLimit, offset))
        }
    }

    private suspend fun performOnlineSearch(
        querySearch: String,
        offset: Int,
        userLimit: Int,
        userPresence: Boolean,
    ): Result<List<User>> {
        val currentUser = chatDomainImpl.user.value

        return if (currentUser != null) {
            val filter = if (querySearch.isEmpty()) {
                defaultUsersQueryFilter(currentUser.id)
            } else {
                Filters.and(
                    Filters.autocomplete(FIELD_NAME, querySearch),
                    Filters.ne(FIELD_ID, currentUser.id)
                )
            }

            chatDomainImpl.client.queryUsers(
                QueryUsersRequest(
                    filter = filter,
                    offset = offset,
                    limit = userLimit,
                    querySort = USERS_QUERY_SORT,
                    presence = userPresence
                )
            ).execute().also { result ->
                if (result.isSuccess && result.data().isNotEmpty()) {
                    chatDomainImpl.repos.insertUsers(result.data())
                }
            }
        } else {
            Result(
                ChatError("User is not set in ChatDomain. It is not possible to perform online search")
            )
        }
    }

    internal companion object {
        private val USERS_QUERY_SORT = QuerySort.asc(User::name)

        private const val FIELD_NAME = "name"
        private const val FIELD_ID = "id"
    }
}
