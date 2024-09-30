package io.getstream.chat.android.compose.viewmodel.pinned

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.getstream.chat.android.ui.common.feature.pinned.PinnedMessageListController

/**
 * A ViewModel factory for creating a [PinnedMessageListViewModel].
 *
 * @param cid cid The full channel ID. Ex: "messaging:123".
 *
 * @see PinnedMessageListViewModel
 */
public class PinnedMessageListViewModelFactory(private val cid: String): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == PinnedMessageListViewModel::class.java) {
            "PinnedMessageListViewModelFactory can only create instances of PinnedMessageListViewModel"
        }
        @Suppress("UNCHECKED_CAST")
        return PinnedMessageListViewModel(controller = PinnedMessageListController(cid = cid)) as T
    }
}