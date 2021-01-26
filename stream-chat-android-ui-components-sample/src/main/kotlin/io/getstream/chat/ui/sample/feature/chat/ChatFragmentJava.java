package io.getstream.chat.ui.sample.feature.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import io.getstream.chat.android.ui.messages.customization.MessageListViewConfig;
import io.getstream.chat.android.ui.messages.customization.dsl.MessageListViewConfigBuilder;
import io.getstream.chat.android.ui.messages.customization.dsl.viewholder.PlainTextViewHolderConfigBuilder;
import io.getstream.chat.android.ui.messages.customization.dsl.viewholder.ViewHolderConfigBuilder;
import io.getstream.chat.android.ui.messages.customization.viewholder.PlainTextViewHolderConfig;
import io.getstream.chat.android.ui.messages.customization.viewholder.ViewHolderConfig;
import io.getstream.chat.android.ui.messages.view.MessageListView;
import io.getstream.chat.ui.sample.databinding.FragmentChatBinding;

public class ChatFragmentJava extends Fragment {

    private FragmentChatBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setupMessageListView(binding.messageListView);
    }

    private void setupMessageListView(MessageListView messageListView) {
        PlainTextViewHolderConfig plainTextViewHolderConfig = new PlainTextViewHolderConfigBuilder()
                .textSize(PlainTextViewHolderConfig.DEFAULT_TEXT_SIZE_SP)
                .textLineHeight(PlainTextViewHolderConfig.DEFAULT_TEXT_LINE_HEIGHT_SP)
                .build();

        ViewHolderConfig viewHolderConfig = new ViewHolderConfigBuilder()
                .plainText(plainTextViewHolderConfig)
                .build();

        MessageListViewConfig messageListViewConfig = new MessageListViewConfigBuilder()
                .viewHolders(viewHolderConfig)
                .build();

        messageListView.setMessageListViewConfig(messageListViewConfig);
    }
}
