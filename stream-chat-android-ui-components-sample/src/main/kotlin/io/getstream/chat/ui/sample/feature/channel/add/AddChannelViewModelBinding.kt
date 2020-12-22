package io.getstream.chat.ui.sample.feature.channel.add

import androidx.lifecycle.LifecycleOwner

fun AddChannelViewModel.bindView(view: AddChannelView, lifecycleOwner: LifecycleOwner) {
    state.observe(lifecycleOwner) { state ->
        when (state) {
            AddChannelViewModel.State.Loading -> view.showLoadingView()
            is AddChannelViewModel.State.Result -> {
                view.setUsers(state.users)
            }
            is AddChannelViewModel.State.ResultMoreUsers -> {
                view.addMoreUsers(state.users)
            }
            is AddChannelViewModel.State.InitializeChannel,
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
