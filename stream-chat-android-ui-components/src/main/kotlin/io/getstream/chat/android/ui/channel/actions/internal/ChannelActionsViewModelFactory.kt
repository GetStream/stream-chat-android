package io.getstream.chat.android.ui.channel.actions.internal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Specialized factory class that produces [ChannelActionsViewModel].
 *
 * @param cid The full channel id, i.e. "messaging:123".
 * @param isGroup True if the Channel is a group channel, false otherwise.
 */
internal class ChannelActionsViewModelFactory(
    private val cid: String,
    private val isGroup: Boolean,
) : ViewModelProvider.Factory {

    /**
     * Creates an instance of [ChannelActionsViewModel].
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChannelActionsViewModel(cid, isGroup) as T
    }
}
