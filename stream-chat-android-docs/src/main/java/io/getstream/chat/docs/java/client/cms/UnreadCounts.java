package io.getstream.chat.docs.java.client.cms;

import java.util.List;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.api.models.QueryChannelRequest;
import io.getstream.chat.android.client.channel.ChannelClient;
import io.getstream.chat.android.client.events.MarkAllReadEvent;
import io.getstream.chat.android.client.events.NewMessageEvent;
import io.getstream.chat.android.client.events.NotificationMarkReadEvent;
import io.getstream.chat.android.client.events.NotificationMessageNewEvent;
import io.getstream.chat.android.client.extensions.ChannelExtensionKt;
import io.getstream.chat.android.models.Channel;
import io.getstream.chat.android.models.ChannelUserRead;
import io.getstream.chat.android.models.User;

public class UnreadCounts {
    private ChatClient client;
    private ChannelClient channelClient;

    /**
     * @see <a href="https://getstream.io/chat/docs/unread/?language=java">Unread</a>
     */
    class Unread {
        public void userConnect() {
            User user = new User.Builder()
                    .withId("user-id")
                    .build();
            client.connectUser(user, "{{ chat_user_token }}").enqueue(result -> {
                if (result.isSuccess()) {
                    User userRes = result.getOrNull().getUser();
                    int unreadChannels = userRes.getUnreadChannels();
                    int totalUnreadCount = userRes.getTotalUnreadCount();
                    int unreadThreads = userRes.getUnreadThreads();
                } else {
                    // Handle error
                }
            });
        }

        public void markRead() {
            channelClient.markRead().enqueue(result -> {
                if (result.isSuccess()) {
                    // Messages in the channel marked as read
                } else {
                    // Handle error
                }
            });
        }

        public void listenToReadEvents() {
            channelClient.subscribeFor(
                    new Class[]{
                            NewMessageEvent.class,
                            NotificationMessageNewEvent.class,
                            MarkAllReadEvent.class,
                            NotificationMarkReadEvent.class
                    },
                    event -> {
                        if (event instanceof NewMessageEvent) {
                            NewMessageEvent newMessageEvent = (NewMessageEvent) event;
                            Integer unreadChannels = newMessageEvent.getUnreadChannels();
                            Integer totalUnreadCount = newMessageEvent.getTotalUnreadCount();
                        } else if (event instanceof NotificationMessageNewEvent) {
                            NotificationMessageNewEvent notificationMessageNewEvent = (NotificationMessageNewEvent) event;
                            Integer unreadChannels = notificationMessageNewEvent.getUnreadChannels();
                            Integer totalUnreadCount = notificationMessageNewEvent.getTotalUnreadCount();
                        } else if (event instanceof MarkAllReadEvent) {
                            MarkAllReadEvent markAllReadEvent = (MarkAllReadEvent) event;
                            Integer unreadChannels = markAllReadEvent.getUnreadChannels();
                            Integer totalUnreadCount = markAllReadEvent.getTotalUnreadCount();
                        } else if (event instanceof NotificationMarkReadEvent) {
                            NotificationMarkReadEvent notificationMarkReadEvent = (NotificationMarkReadEvent) event;
                            Integer unreadChannels = notificationMarkReadEvent.getUnreadChannels();
                            Integer totalUnreadCount = notificationMarkReadEvent.getTotalUnreadCount();
                        }
                    }
            );
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/unread_channel/?language=java">Unread Channels</a>
     */
    class UnreadChannels {
        public void unreadChannels() {
            // Get channel
            QueryChannelRequest queryChannelRequest = new QueryChannelRequest().withState();

            client.queryChannel("channel-type", "channel-id", queryChannelRequest, false).enqueue((result) -> {
                if (result.isSuccess()) {
                    // readState is the list of read states for each user on the channel
                    List<ChannelUserRead> readState = result.getOrNull().getRead();
                } else {
                    // Handle error
                }
            });
        }

        public void unreadMessages() {
            // Get channel
            QueryChannelRequest queryChannelRequest = new QueryChannelRequest().withState();

            client.queryChannel("channel-type", "channel-id", queryChannelRequest, false).enqueue((result) -> {
                if (result.isSuccess()) {
                    // Unread count for the current user
                    Integer unreadCount = ChannelExtensionKt.currentUserUnreadCount(
                            result.getOrNull(),
                            client.getCurrentUser().getId()
                    );
                } else {
                    // Handle error
                }
            });
        }

        public void unreadMentions() {
            // Get channel
            QueryChannelRequest queryChannelRequest = new QueryChannelRequest().withState();
            User currentUser = client.getClientState().getUser().getValue();
            if (currentUser == null) {
                // Handle user not connected state
                return;
            }

            client.queryChannel("channel-type", "channel-id", queryChannelRequest, false).enqueue((result) -> {
                if (result.isSuccess()) {
                    // Unread mentions
                    Channel channel = result.getOrNull();
                    Integer unreadCount = ChannelExtensionKt.countUnreadMentionsForUser(channel, currentUser);
                } else {
                    // Handle error
                }
            });
        }

        public void markAllAsRead() {
            client.markAllRead().enqueue((result) -> {
                if (result.isSuccess()) {
                    // Handle success
                } else {
                    // Handle error
                }
            });
        }
    }
}
