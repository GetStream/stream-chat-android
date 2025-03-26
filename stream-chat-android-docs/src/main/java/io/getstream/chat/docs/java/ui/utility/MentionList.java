package io.getstream.chat.docs.java.ui.utility;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import io.getstream.chat.android.ui.feature.mentions.list.MentionListView;
import io.getstream.chat.android.ui.viewmodel.mentions.MentionListViewModel;
import io.getstream.chat.android.ui.viewmodel.mentions.MentionListViewModelBinding;
import io.getstream.chat.android.ui.viewmodel.mentions.MentionListViewModelFactory;

/**
 * [Mention List View](https://getstream.io/chat/docs/sdk/android/ui/utility-components/mention-list-view/)
 */
public class MentionList extends Fragment {

    private MentionListView mentionListView;

    /**
     * [Usage](https://getstream.io/chat/docs/sdk/android/ui/utility-components/mention-list-view/#usage)
     */
    public void usage() {
        ViewModelProvider.Factory factory = new MentionListViewModelFactory();
        MentionListViewModel viewModel = new ViewModelProvider(this, factory).get(MentionListViewModel.class);
        MentionListViewModelBinding.bind(viewModel, mentionListView, getViewLifecycleOwner());
    }

    /**
     * [Handling Actions](https://getstream.io/chat/docs/sdk/android/ui/utility-components/mention-list-view/#handling-actions)
     */
    public void handlingActions() {
        mentionListView.setMentionSelectedListener(message -> {
            // Handle a mention item being clicked
        });
    }
}
