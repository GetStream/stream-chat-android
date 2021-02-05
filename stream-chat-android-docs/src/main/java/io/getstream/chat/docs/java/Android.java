package io.getstream.chat.docs.java;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.getstream.sdk.chat.viewmodel.MessagesHeaderViewModel;

import io.getstream.chat.android.ui.channel.list.header.ChannelListHeaderView;
import io.getstream.chat.android.ui.channel.list.header.viewmodel.ChannelListHeaderViewModel;
import io.getstream.chat.android.ui.channel.list.header.viewmodel.ChannelListHeaderViewModelBinding;
import io.getstream.chat.android.ui.messages.header.MessagesHeaderView;
import io.getstream.chat.android.ui.messages.header.MessagesHeaderViewModelBinding;

public class Android {

    /**
     * @see <a href="https://getstream.io/chat/docs/android/channel_list_header_view">Channel List Header View</a>
     */
    class ChannelListHeader extends Fragment {
        ChannelListHeaderView channelListHeaderView;

        public void bindingWithViewModel() {
            // Get ViewModel
            ChannelListHeaderViewModel viewModel = new ViewModelProvider(this).get(ChannelListHeaderViewModel.class);
            // Bind it with ChannelListHeaderView
            ChannelListHeaderViewModelBinding.bind(viewModel, channelListHeaderView, getViewLifecycleOwner());
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/channel_list_header_view">Channel List Header View</a>
     */
    class MessagesHeader extends Fragment {
        MessagesHeaderView messagesHeaderView;

        public void bindingWithViewModel() {
            // Get ViewModel
            MessagesHeaderViewModel viewModel =
                    new ViewModelProvider(this).get(MessagesHeaderViewModel.class);
            // Bind it with MessagesHeaderView
            MessagesHeaderViewModelBinding
                    .bind(viewModel, messagesHeaderView, getViewLifecycleOwner());
        }
    }
}
