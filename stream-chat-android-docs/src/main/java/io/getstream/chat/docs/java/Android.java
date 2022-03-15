package io.getstream.chat.docs.java;


import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.api.models.QueryChannelRequest;
import io.getstream.chat.android.client.events.ChatEvent;
import io.getstream.chat.android.client.models.Channel;
import io.getstream.chat.android.client.models.ChannelUserRead;

public class Android {

    /**
     * @see <a href="hhttps://getstream.io/nessy/docs/chat_docs/events/event_listening/?language=java">Listening for events</a>
     */
    public class SyncHistory extends Fragment {

        public void getSyncHistory(ChatClient chatClient) {
            List<String> cidList = new ArrayList<>();
            cidList.add("messaging:123");

            Date lastSeenExample = new Date();

            chatClient.getSyncHistory(cidList, lastSeenExample).enqueue(result -> {
                if (result.isSuccess()) {
                    List<ChatEvent> events = result.data();
                } else {
                    // Handle result.error()
                }
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/unread_channel/?language=java">Channels</a>
     */
    public class UnreadCount extends Fragment {

        public void unreadCountInfo() {
            // Get channel
            QueryChannelRequest queryChannelRequest = new QueryChannelRequest().withState();

            Channel channel = ChatClient.instance().queryChannel(
                    "channel-type",
                    "channel-id",
                    queryChannelRequest
            )
                    .execute()
                    .data();

            // readState is the list of read states for each user on the channel
            List<ChannelUserRead> readState = channel.getRead();
        }

        public void getUnreadCountForCurrentUser() {
            // Get channel
            QueryChannelRequest queryChannelRequest = new QueryChannelRequest().withState();

            Channel channel = ChatClient.instance().queryChannel(
                    "channel-type",
                    "channel-id",
                    queryChannelRequest
            )
                    .execute()
                    .data();

            // Unread count for current user
            int unreadCount = channel.getUnreadCount();
        }

        public void markAllRead() {
            ChatClient.instance().markAllRead().enqueue(result -> {
                if (result.isSuccess()) {
                    //Handle success
                } else {
                    //Handle failure
                }
            });
        }
    }
}
