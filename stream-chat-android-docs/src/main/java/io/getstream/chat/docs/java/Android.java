package io.getstream.chat.docs.java;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.getstream.sdk.chat.viewmodel.ChannelHeaderViewModel;

import io.getstream.chat.android.ui.channel.list.header.ChannelListHeaderView;
import io.getstream.chat.android.ui.channel.list.header.viewmodel.ChannelListHeaderViewModel;
import io.getstream.chat.android.ui.channel.list.header.viewmodel.ChannelListHeaderViewModelBinding;
import io.getstream.chat.android.ui.messages.header.ChannelHeaderViewModelBinding;
import io.getstream.chat.android.ui.messages.header.MessagesHeaderView;

public class Android {

    /**
     * @see <a href="https://getstream.io/chat/docs/android/channel_list_header_view">Channel List Header View</a>
     */
    public class ChannelListHeader extends Fragment {
        ChannelListHeaderView channelListHeaderView;

        public void bindingWithViewModel() {
            // Get ViewModel
            ChannelListHeaderViewModel viewModel = new ViewModelProvider(this).get(ChannelListHeaderViewModel.class);
            // Bind it with ChannelListHeaderView
            ChannelListHeaderViewModelBinding.bind(viewModel, channelListHeaderView, getViewLifecycleOwner());
        }
    }

    /**
     * * @see <a href="https://getstream.io/chat/docs/android/messages_header_view">Messages Header View</a>
     */
    public class MessagesHeader extends Fragment {
        MessagesHeaderView messagesHeaderView;

        public void bindingWithViewModel() {
            // Get ViewModel
            ChannelHeaderViewModel viewModel =
                    new ViewModelProvider(this).get(ChannelHeaderViewModel.class);
            // Bind it with MessagesHeaderView
            ChannelHeaderViewModelBinding
                    .bind(viewModel, messagesHeaderView, getViewLifecycleOwner());
        }
    }
}
