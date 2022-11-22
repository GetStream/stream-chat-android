package io.getstream.chat.docs.java.client.docusaurus;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.channel.ChannelClient;
import io.getstream.chat.android.client.events.ChatEvent;
import io.getstream.chat.android.client.events.NewMessageEvent;
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent;
import io.getstream.chat.android.models.Member;
import io.getstream.chat.android.models.Message;
import io.getstream.chat.android.client.utils.observable.Disposable;

/**
 * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/listening-for-events/">Listening for Events</a>
 */
public class ListeningForEvents {

    class ListeningForEventsSnippet1 {

        public void listeningForEvents() {
            ChatClient chatClient = ChatClient.instance();
            ChannelClient channelClient = chatClient.channel("messaging", "general");

            // Subscribe for new message events
            Disposable disposable = channelClient.subscribeFor(
                    new Class[]{NewMessageEvent.class},
                    (ChatEvent event) -> {
                        Message message = ((NewMessageEvent) event).getMessage();
                    }
            );

            // Dispose when you want to stop receiving events
            disposable.dispose();
        }
    }

    class ListeningForEventsSnippet2 extends Fragment {

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            // Setup chat screen components

            ChatClient chatClient = ChatClient.instance();
            ChannelClient channelClient = chatClient.channel("messaging", "general");
            channelClient.subscribeFor(
                    new Class[]{NotificationRemovedFromChannelEvent.class},
                    (ChatEvent event) -> {
                        Member member = ((NotificationRemovedFromChannelEvent) event).getMember();

                        String removedUserId = member.getUser().getId();
                        String currentUserId = chatClient.getCurrentUser().getId();
                        if (removedUserId.equals(currentUserId)) {
                            // Close the current chat screen as the current user has been removed
                            requireActivity().getOnBackPressedDispatcher().onBackPressed();
                        }
                    }
            );
        }
    }
}
