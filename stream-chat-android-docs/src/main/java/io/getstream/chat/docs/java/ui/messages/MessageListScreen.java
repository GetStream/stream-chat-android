package io.getstream.chat.docs.java.ui.messages;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import io.getstream.chat.android.ui.message.MessageListActivity;
import io.getstream.chat.android.ui.message.MessageListFragment;
import io.getstream.chat.android.ui.message.composer.MessageComposerView;
import io.getstream.chat.android.ui.message.list.MessageListView;
import io.getstream.chat.android.ui.message.list.header.MessageListHeaderView;
import io.getstream.chat.docs.R;
import kotlin.Unit;

/**
 * [Message List Screen](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-list-screen/)
 */
public class MessageListScreen {

    public final class MyMessageListActivity extends AppCompatActivity {
        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.stream_ui_fragment_container);

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, MessageListFragment.newInstance("channelType:channelId"))
                        .commit();
            }
        }
    }

    public void startActivity(Context context) {
        context.startActivity(MessageListActivity.createIntent(context, "channelType:channelId"));
    }

    public final class MainActivity extends AppCompatActivity implements MessageListFragment.BackPressListener {

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

    public final class CustomMessageListFragment extends MessageListFragment {

        @Override
        protected void setupMessageListHeader(@NonNull MessageListHeaderView messageListHeaderView) {
            super.setupMessageListHeader(messageListHeaderView);
            // Customize message list header view. For example, set a custom back button click listener:
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
}
