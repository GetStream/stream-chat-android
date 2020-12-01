package io.getstream.chat.ui.sample.feature.channel.add

import androidx.lifecycle.LifecycleOwner

fun AddChannelViewModel.bindView(view: AddChannelView, lifecycleOwner: LifecycleOwner) {
    state.observe(lifecycleOwner) { state ->
        when (state) {
            AddChannelViewModel.State.Loading -> {
                view.showLoadingView()
                view.hideUsersRecyclerView()
                view.hideEmptyStateView()
            }
            AddChannelViewModel.State.Empty -> {
                view.hideUsersRecyclerView()
                view.hideLoadingView()
                view.showEmptyStateView()
            }
            is AddChannelViewModel.State.Result -> {
                view.setUsers(state.users) {
                    view.hideLoadingView()
                    view.hideEmptyStateView()
                    view.showUsersRecyclerView()
                }
            }
            is AddChannelViewModel.State.ResultMoreUsers -> {
                view.addMoreUsers(state.users)
            }
            is AddChannelViewModel.State.ShowChannel,
            AddChannelViewModel.State.HideChannel,
            is AddChannelViewModel.State.NavigateToChannel -> Unit
        }
    }

    paginationState.observe(lifecycleOwner) { state ->
        view.setPaginationEnabled(!state.endReached && !state.loadingMore)
    }

    view.endReachedListener = AddChannelView.EndReachedListener {
        onEvent(AddChannelViewModel.Event.ReachedEndOfList)
    }
    view.setSearchInputChangedListener {
        onEvent(AddChannelViewModel.Event.SearchInputChanged(it))
    }
}
