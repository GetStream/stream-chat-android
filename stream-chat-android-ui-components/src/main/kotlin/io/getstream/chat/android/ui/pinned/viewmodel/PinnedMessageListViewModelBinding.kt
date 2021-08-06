@file:JvmName("PinnedMessageListViewModelBinding")

package io.getstream.chat.android.ui.pinned.viewmodel

import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.livedata.utils.EventObserver
import io.getstream.chat.android.ui.mention.list.MentionListView

/**
 * Binds [MentionListView] with [PinnedMessageListViewModel], updating the view's state based on
 * data provided by the ViewModel and propagating view events to the ViewModel as needed.
 *
 * This function sets listeners on the view and ViewModel. Call this method
 * before setting any additional listeners on these objects yourself.
 */
@JvmName("bind")
public fun PinnedMessageListViewModel.bindView(view: MentionListView, lifecycleOwner: LifecycleOwner) {
    state.observe(lifecycleOwner) { state ->
        when {
            state.isLoading -> {
                view.showLoading()
            }
            else -> {
                view.showMessages(state.results)
            }
        }
    }
    errorEvents.observe(
        lifecycleOwner,
        EventObserver {
            view.showError()
        }
    )
    view.setLoadMoreListener {
        this.loadMore()
    }
}
