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
class PinnedMessageListViewSnippets extends Fragment {

    private PinnedMessageListView pinnedMessageListView;

    public void usage() {
        ViewModelProvider.Factory factory = new PinnedMessageListViewModelFactory.Builder()
                .cid("channelType:channelId")
                .build();
        PinnedMessageListViewModel viewModel = new ViewModelProvider(this, factory).get(PinnedMessageListViewModel.class);

        PinnedMessageListViewModelBinding.bind(viewModel, pinnedMessageListView, getViewLifecycleOwner());
    }

    public void handlingActions() {
        pinnedMessageListView.setPinnedMessageSelectedListener(message -> {
            // Handle a mention item being clicked
        });
    }
}
