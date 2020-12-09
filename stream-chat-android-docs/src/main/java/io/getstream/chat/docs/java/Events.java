package io.getstream.chat.docs.java;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.channel.ChannelClient;
import io.getstream.chat.android.client.events.ChatEvent;
import io.getstream.chat.android.client.events.ConnectedEvent;
import io.getstream.chat.android.client.events.ConnectingEvent;
import io.getstream.chat.android.client.events.DisconnectedEvent;
import io.getstream.chat.android.client.events.NewMessageEvent;
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent;
import io.getstream.chat.android.client.events.UserPresenceChangedEvent;
import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.client.utils.observable.Disposable;
import kotlin.Unit;

public class Events {
    private ChatClient client;
    private ChannelClient channelController;

    /**
     * @see <a href="https://getstream.io/chat/docs/event_object/?language=java">Event Object</a>
     */
    class EventObject {

    }

    /**
     * @see <a href="https://getstream.io/chat/docs/event_listening/?language=java">Listening For Events</a>
     */
    class ListeningForEvents {

        public void listenSpecificChannelEvents() {
            // Subscribe for new message events
            Disposable disposable = client.subscribeForSingle(
                    NewMessageEvent.class,
                    (NewMessageEvent event) -> {
                        // To get the message
                        Message message = event.getMessage();
                        return Unit.INSTANCE;
                    }
            );

            // Dispose when you want to stop receiving events
            disposable.dispose();
        }

        public void listenAllChannelEvents() {
            Disposable disposable = channelController.subscribe((ChatEvent event) -> {
                if (event instanceof NewMessageEvent) {
                    // To get the message
                    Message message = ((NewMessageEvent) event).getMessage();
                }
                return Unit.INSTANCE;
            });

            // Dispose when you want to stop receiving events
            disposable.dispose();
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/event_listening/?language=java#client-events">Client Events</a>
         */
        public void listenClientEvents() {
            // Subscribe for User presence events
            client.subscribeFor(
                    new Class[]{UserPresenceChangedEvent.class},
                    event -> {
                        // Handle change
                        return Unit.INSTANCE;
                    }
            );

            // Subscribe for just the first ConnectedEvent
            client.subscribeForSingle(
                    ConnectedEvent.class,
                    event -> {
                        // Use event data
                        int unreadCount = event.getMe().getTotalUnreadCount();
                        int unreadChannels = event.getMe().getUnreadChannels();
                        return Unit.INSTANCE;
                    }
            );
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/event_listening/?language=java#connection-events">Connection Events</a>
         */
        public void listenConnectionEvents() {
            client.subscribeFor(
                    new Class[]{ConnectedEvent.class, ConnectingEvent.class, DisconnectedEvent.class},
                    event -> {
                        if (event instanceof ConnectedEvent) {
                            // Socket is connected
                        } else if (event instanceof ConnectingEvent) {
                            // Socket is connecting
                        } else if (event instanceof DisconnectedEvent) {
                            // Socket is disconnected
                        }
                        return Unit.INSTANCE;
                    }
            );
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/event_listening/?language=java#stop-listening-for-events">Stop Listeing for Events</a>
         */
        public void stopListeningEvents() {
            final Disposable disposable = client.subscribe(chatEvent -> {
                /* ... */
                return null;
            });
            disposable.dispose();
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/event_typing/?language=java">Typing Events</a>
     */
    class TypingEvents {
        public void sendTypingEvent() {
            // Sends a typing.start event if it's been more than 3000 ms since the last event
            channelController.keystroke().enqueue();

            // Sends an event typing.stop to all channel participants
            channelController.stopTyping().enqueue();
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/notification_events/?language=java">Typing Events</a>
     */
    class NotificationEvents {
        public void notificationEvents() {
            // An example of how listen event when a user is added to a channel
            channelController.subscribeFor(
                    new Class[]{NotificationAddedToChannelEvent.class},
                    addedToChannelEvent -> {
                        // Handle event
                        return Unit.INSTANCE;
                    }
            );
        }
    }
}
