@file:JvmName("MentionListViewModelBinding")

package io.getstream.chat.android.ui.mention.list.viewmodel

import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.livedata.utils.EventObserver
import io.getstream.chat.android.ui.mention.list.MentionListView

/**
 * Binds [MentionListView] with [MentionListViewModel], updating the view's state
 * based on data provided by the ViewModel.
 */
@JvmName("bind")
public fun MentionListViewModel.bindView(view: MentionListView, lifecycleOwner: LifecycleOwner) {
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
