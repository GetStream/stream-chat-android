package io.getstream.chat.docs.java.ui.utility;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import io.getstream.chat.android.ui.feature.pinned.list.PinnedMessageListView;
import io.getstream.chat.android.ui.viewmodel.pinned.PinnedMessageListViewModel;
import io.getstream.chat.android.ui.viewmodel.pinned.PinnedMessageListViewModelBinding;
import io.getstream.chat.android.ui.viewmodel.pinned.PinnedMessageListViewModelFactory;

/**
 * [Pinned Message List View](https://getstream.io/chat/docs/sdk/android/ui/utility-components/pinned-message-list-view/)
 */
class PinnedMessageList extends Fragment {

    private PinnedMessageListView pinnedMessageListView;

    /**
     * [Usage](https://getstream.io/chat/docs/sdk/android/ui/utility-components/pinned-message-list-view/#usage)
     */
    public void usage() {
        ViewModelProvider.Factory factory = new PinnedMessageListViewModelFactory.Builder()
                .cid("messaging:123")
                .build();
        PinnedMessageListViewModel viewModel = new ViewModelProvider(this, factory).get(PinnedMessageListViewModel.class);

        PinnedMessageListViewModelBinding.bind(viewModel, pinnedMessageListView, getViewLifecycleOwner());
    }

    /**
     * [Handling Actions](https://getstream.io/chat/docs/sdk/android/ui/utility-components/pinned-message-list-view/#handling-actions)
     */
    public void handlingActions() {
        pinnedMessageListView.setPinnedMessageSelectedListener(message -> {
            // Handle a mention item being clicked
        });
    }
}
