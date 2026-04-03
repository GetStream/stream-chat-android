package io.getstream.chat.docs.java.ui.messages;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import io.getstream.chat.android.ui.feature.messages.ChannelActivity;
import io.getstream.chat.android.ui.feature.messages.ChannelFragment;
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerView;
import io.getstream.chat.android.ui.feature.messages.header.ChannelHeaderView;
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
            context.startActivity(ChannelActivity.createIntent(context, "messaging:123"));
        }

        public final class MyChannelActivity extends AppCompatActivity {

            public MyChannelActivity() {
                super(R.layout.stream_ui_fragment_container);
            }

            @Override
            protected void onCreate(@Nullable Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                if (savedInstanceState == null) {
                    ChannelFragment fragment = ChannelFragment.newInstance("messaging:123", builder -> {
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

        public final class MyChannelActivity extends AppCompatActivity implements ChannelFragment.BackPressListener {

            public MyChannelActivity() {
                super(R.layout.stream_ui_fragment_container);
            }

            @Override
            protected void onCreate(@Nullable Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                // Add ChannelFragment to the layout
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

        public final class CustomChannelFragment extends ChannelFragment {

            @Override
            protected void setupChannelHeader(@NonNull ChannelHeaderView channelHeaderView) {
                super.setupChannelHeader(channelHeaderView);
                // Customize message list header view

                // For example, set a custom listener for the back button
                channelHeaderView.setBackButtonClickListener(() -> {
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

        public final class CustomChannelActivity extends ChannelActivity {

            @NonNull
            @Override
            protected ChannelFragment createChannelFragment(@NonNull String cid, @Nullable String messageId) {
                return ChannelFragment.newInstance(cid, builder -> {
                    builder.setFragment(new CustomChannelFragment());
                    builder.customTheme(R.style.StreamUiTheme);
                    builder.showHeader(true);
                    builder.messageId(messageId);
                    return Unit.INSTANCE;
                });
            }
        }

        public void startActivity(Context context) {
            context.startActivity(ChannelActivity.createIntent(context, "messaging:123", null, CustomChannelActivity.class));
        }
    }
}
