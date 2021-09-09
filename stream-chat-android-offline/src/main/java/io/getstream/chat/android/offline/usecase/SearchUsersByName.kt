package io.getstream.chat.android.offline.usecase

import androidx.annotation.CheckResult
import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.ChatDomainImpl

/**
 * Use case for searching users by string-autocomplete filter. Performs online request if connected or local searching
 * in DB otherwise.
 */
internal class SearchUsersByName(private val chatDomainImpl: ChatDomainImpl) {

    @VisibleForTesting
    internal val defaultUsersQueryFilter by lazy {
        val currentUserId = chatDomainImpl.user.value?.id
        if (currentUserId != null) {
            Filters.ne(FIELD_ID, currentUserId)
        } else {
            Filters.neutral()
        }
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
    @CheckResult
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
        val filter = if (querySearch.isEmpty()) {
            defaultUsersQueryFilter
        } else {
            createFilter(
                Filters.autocomplete(FIELD_NAME, querySearch),
                chatDomainImpl.user.value?.id?.let { id -> Filters.ne(FIELD_ID, id) }
            )
        }

        return chatDomainImpl.client.queryUsers(
            QueryUsersRequest(
                filter = filter,
                offset = offset,
                limit = userLimit,
                querySort = USERS_QUERY_SORT,
                presence = userPresence
            )
        ).await().also { result ->
            if (result.isSuccess && result.data().isNotEmpty()) {
                chatDomainImpl.repos.insertUsers(result.data())
            }
        }
    }

    internal companion object {
        private val USERS_QUERY_SORT = QuerySort.asc(User::name)

        private const val FIELD_NAME = "name"
        private const val FIELD_ID = "id"
    }

    private fun createFilter(defaultFilter: FilterObject, optionalFilter: FilterObject?): FilterObject {
        return if (optionalFilter != null) {
            Filters.and(defaultFilter, optionalFilter)
        } else {
            defaultFilter
        }
    }
}
