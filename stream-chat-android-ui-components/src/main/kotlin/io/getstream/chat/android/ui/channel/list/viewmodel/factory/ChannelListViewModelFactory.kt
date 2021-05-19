package io.getstream.chat.android.ui.channel.list.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel

/**
 * Creates a channels view model factory
 *
 * @param filter how to filter the channels
 * @param sort how to sort the channels, defaults to last_updated
 * @param limit how many channels to return
 * @param messageLimit the number of messages to fetch for each channel
 *
 * @see Filters
 * @see QuerySort
 */
public class ChannelListViewModelFactory @JvmOverloads constructor(
    private val filter: FilterObject = Filters.and(
        Filters.eq("type", "messaging"),
        Filters.`in`("members", memberIdList(ChatDomain.instance().user.value?.id)),
        Filters.or(Filters.notExists("draft"), Filters.ne("draft", true)),
    ),
    private val sort: QuerySort<Channel> = ChannelListViewModel.DEFAULT_SORT,
    private val limit: Int = 30,
    private val messageLimit: Int = 1,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == ChannelListViewModel::class.java) {
            "ChannelListViewModelFactory can only create instances of ChannelListViewModel"
        }

        @Suppress("UNCHECKED_CAST")
        return ChannelListViewModel(ChatDomain.instance(), filter, sort, limit, messageLimit) as T
    }
}

private fun memberIdList(userId: String?): List<String> {
    return userId?.let(::listOf) ?: emptyList<String>().also {
        ChatLogger.get("User is not set in ChatDomain, default filter won't work.")
    }
}
