package io.getstream.chat.android.ui.channel.actions.internal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

internal class ChannelActionsViewModelFactory(
    private val cid: String,
    private val isGroup: Boolean
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChannelActionsViewModel(cid, isGroup) as T
    }
}
