package io.getstream.chat.android.ui.pinned.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

public class PinnedMessageListViewModelFactory(private val cid: String?) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(cid != null) {
            "Channel cid should not be null"
        }
        require(modelClass == PinnedMessageListViewModel::class.java) {
            "PinnedMessageListViewModelFactory can only create instances of PinnedMessageListViewModel"
        }

        @Suppress("UNCHECKED_CAST")
        return PinnedMessageListViewModel(cid) as T
    }
}
