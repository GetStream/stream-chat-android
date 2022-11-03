package io.getstream.chat.docs.java.ui.utility;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import io.getstream.chat.android.ui.channel.list.header.viewmodel.ChannelListHeaderViewModel;
import io.getstream.chat.android.ui.mention.list.MentionListView;
import io.getstream.chat.android.ui.mention.list.viewmodel.MentionListViewModel;
import io.getstream.chat.android.ui.mention.list.viewmodel.MentionListViewModelBinding;

/**
 * [Mention List View](https://getstream.io/chat/docs/sdk/android/ui/utility-components/mention-list-view/)
 */
public class MentionListViewSnippets extends Fragment {

    private MentionListView mentionListView;

    public void usage() {
        MentionListViewModel viewModel = new ViewModelProvider(this).get(MentionListViewModel.class);
        MentionListViewModelBinding.bind(viewModel, mentionListView, getViewLifecycleOwner());
    }

    public void handlingActions() {
        mentionListView.setMentionSelectedListener(message -> {
            // Handle a mention item being clicked
        });
    }
}
