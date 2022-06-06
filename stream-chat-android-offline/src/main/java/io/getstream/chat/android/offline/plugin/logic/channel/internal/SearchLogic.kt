package io.getstream.chat.android.offline.plugin.logic.channel.internal

import android.util.Log
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.offline.plugin.state.channel.internal.ChannelMutableState

internal class SearchLogic(private val mutableState: ChannelMutableState) {

    private var isInsideSearch = false

    fun handleMessageBounds(request: QueryChannelRequest, hasMoreMessages: Boolean) {
        Log.d("SearchLogic", "Handling search logic. is filtering: ${request.isFilteringMessages()} " +
            "more messages: $hasMoreMessages")

        when {
            !isInsideSearch && request.isFilteringNewerMessages() && hasMoreMessages  -> {
                updateSearchState(true)
            }

            isInsideSearch && request.isFilteringNewerMessages() && !hasMoreMessages -> {
                updateSearchState(false)
            }
        }
    }

    private fun updateSearchState(isInsideSearch: Boolean) {
        this.isInsideSearch = isInsideSearch
        mutableState._insideSearch.value = isInsideSearch
    }
}

private fun QueryChannelRequest.isFilteringMessages(): Boolean {
    return this.isFilteringNewerMessages() || this.filteringOlderMessages()
}
