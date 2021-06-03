@file:JvmName("ChannelListViewModelBinding")

package io.getstream.chat.android.ui.channel.list.viewmodel

import android.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.livedata.utils.EventObserver
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.channel.list.ChannelListView
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListItem

/**
 * Binds [ChannelListView] with [ChannelListViewModel], updating the view's state based on
 * data provided by the ViewModel, and propagating view events to the ViewModel as needed.
 *
 * This function sets listeners on the view and ViewModel. Call this method
 * before setting any additional listeners on these objects yourself.
 */
@JvmName("bind")
public fun ChannelListViewModel.bindView(
    view: ChannelListView,
    lifecycleOwner: LifecycleOwner,
) {
    state.observe(lifecycleOwner) { channelState ->
        when {
            channelState is ChannelListViewModel.State.Result && channelState.isLoading -> view.showLoadingView()

            channelState is ChannelListViewModel.State.Result && !channelState.isLoading -> {
                view.hideLoadingView()
                channelState
                    .channels
                    .map(ChannelListItem::ChannelItem)
                    .let(view::setChannels)
            }

            channelState is ChannelListViewModel.State.Error -> view.showErrorState("Error: No user is set")
        }
    }

    paginationState.observe(lifecycleOwner) { paginationState ->
        view.setPaginationEnabled(!paginationState.endOfChannels && !paginationState.loadingMore)

        if (paginationState.loadingMore) {
            view.showLoadingMore()
        } else {
            view.hideLoadingMore()
        }
    }

    view.setOnEndReachedListener {
        onAction(ChannelListViewModel.Action.ReachedEndOfList)
    }

    view.setChannelDeleteClickListener {
        AlertDialog.Builder(view.context)
            .setTitle(R.string.stream_ui_channel_option_delete_confirmation_title)
            .setMessage(R.string.stream_ui_channel_option_delete_confirmation_message)
            .setPositiveButton(R.string.stream_ui_channel_option_delete_positive_button) { dialog, _ ->
                dialog.dismiss()
                deleteChannel(it)
            }
            .setNegativeButton(R.string.stream_ui_channel_option_delete_negative_button) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    view.setChannelLeaveClickListener { channel ->
        leaveChannel(channel)
    }

    errorEvents.observe(
        lifecycleOwner,
        EventObserver {
            view.showError(it)
        }
    )
}
