package io.getstream.chat.docs.java.ui.messages;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import io.getstream.chat.android.ui.feature.messages.MessageListActivity;
import io.getstream.chat.android.ui.feature.messages.MessageListFragment;
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerView;
import io.getstream.chat.android.ui.feature.messages.header.MessageListHeaderView;
import io.getstream.chat.android.ui.feature.messages.list.MessageListView;
import io.getstream.chat.docs.R;
import kotlin.Unit;

/**
 * [Message List Screen](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-list-screen/)
 */
public class MessageListScreen {

    /**
     * [Usage](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-list-screen/#usage)
     */
    class Usage {

        public void startActivity(Context context) {
            context.startActivity(MessageListActivity.createIntent(context, "messaging:123"));
        }

        public final class MyMessageListActivity extends AppCompatActivity {

            public MyMessageListActivity() {
                super(R.layout.stream_ui_fragment_container);
            }

            @Override
            protected void onCreate(@Nullable Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                if (savedInstanceState == null) {
                    MessageListFragment fragment = MessageListFragment.newInstance("messaging:123", builder -> {
                        builder.showHeader(true);
                        return Unit.INSTANCE;
                    });
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, fragment)
                            .commit();
                }
            }
        }
    }

    /**
     * [Handling Actions](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-list-screen/#handling-actions)
     */
    class HandlingActions {

        public final class MyMessageListActivity extends AppCompatActivity implements MessageListFragment.BackPressListener {

            public MyMessageListActivity() {
                super(R.layout.stream_ui_fragment_container);
            }

            @Override
            protected void onCreate(@Nullable Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                // Add MessageListFragment to the layout
            }

            @Override
            public void onBackPress() {
                // Handle back press
            }
        }
    }

    /**
     * [Customization](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-list-screen/#customization)
     */
    class Customization {

        public final class CustomMessageListFragment extends MessageListFragment {

            @Override
            protected void setupMessageListHeader(@NonNull MessageListHeaderView messageListHeaderView) {
                super.setupMessageListHeader(messageListHeaderView);
                // Customize message list header view

                // For example, set a custom listener for the back button
                messageListHeaderView.setBackButtonClickListener(() -> {
                    // Handle back press
                });
            }

            @Override
            protected void setupMessageList(@NonNull MessageListView messageListView) {
                super.setupMessageList(messageListView);
                // Customize message list view
            }

            @Override
            protected void setupMessageComposer(@NonNull MessageComposerView messageComposerView) {
                super.setupMessageComposer(messageComposerView);
                // Customize message composer view
            }
        }

        public final class CustomMessageListActivity extends MessageListActivity {

            @NonNull
            @Override
            protected MessageListFragment createMessageListFragment(@NonNull String cid, @Nullable String messageId) {
                return MessageListFragment.newInstance(cid, builder -> {
                    builder.setFragment(new CustomMessageListFragment());
                    builder.customTheme(R.style.StreamUiTheme);
                    builder.showHeader(true);
                    builder.messageId(messageId);
                    return Unit.INSTANCE;
                });
            }
        }

        public void startActivity(Context context) {
            context.startActivity(MessageListActivity.createIntent(context, "messaging:123", null, CustomMessageListActivity.class));
        }
    }
}
