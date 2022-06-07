package io.getstream.chat.android.offline.plugin.logic.channel.internal

import android.util.Log
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.offline.plugin.state.channel.internal.ChannelMutableState

internal class SearchLogic(private val mutableState: ChannelMutableState) {

    private var isInsideSearch = false

    fun handleMessageBounds(request: QueryChannelRequest, noMoreMessages: Boolean) {
        when {
            !isInsideSearch && request.isFilteringAroundIdMessages() -> {
                updateSearchState(true)
            }

            isInsideSearch && request.isFilteringNewerMessages() && noMoreMessages -> {
                updateSearchState(false)
            }
        }
    }

    private fun updateSearchState(isInsideSearch: Boolean) {
        this.isInsideSearch = isInsideSearch
        mutableState._insideSearch.value = isInsideSearch
    }
}
