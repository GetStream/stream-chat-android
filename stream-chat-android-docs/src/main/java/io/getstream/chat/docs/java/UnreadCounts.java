package io.getstream.chat.docs.java;

import android.util.Log;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.channel.ChannelClient;
import io.getstream.chat.android.client.events.MarkAllReadEvent;
import io.getstream.chat.android.client.events.NewMessageEvent;
import io.getstream.chat.android.client.events.NotificationMarkReadEvent;
import io.getstream.chat.android.client.events.NotificationMessageNewEvent;
import io.getstream.chat.android.client.models.User;

import static io.getstream.chat.docs.StaticInstances.TAG;

public class UnreadCounts {
    private ChatClient client;
    private ChannelClient channelClient;

    /**
     * @see <a href="https://getstream.io/chat/docs/unread/?language=java">Unread</a>
     */
    class Unread {
        public void getUnreadCount() {
            User user = new User();
            user.setId("user-id");
            client.connectUser(user, "{{ chat_user_token }}").enqueue(result -> {
                if (result.isSuccess()) {
                    User userRes = result.data().getUser();
                    int unreadChannels = userRes.getUnreadChannels();
                    int totalUnreadCount = userRes.getTotalUnreadCount();
                }
            });
        }

        public void markRead() {
            channelClient.markRead().enqueue(result -> {
                if (result.isSuccess()) {
                    // Messages in the channel marked as read
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
            });
        }

        public void listeningReadEvents() {
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
}
