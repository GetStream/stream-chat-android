package io.getstream.chat.docs.java;

import java.util.HashMap;
import java.util.Map;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.channel.ChannelClient;
import io.getstream.chat.android.client.events.ChatEvent;
import io.getstream.chat.android.client.events.ConnectedEvent;
import io.getstream.chat.android.client.events.ConnectingEvent;
import io.getstream.chat.android.client.events.DisconnectedEvent;
import io.getstream.chat.android.client.events.NewMessageEvent;
import io.getstream.chat.android.client.events.UnknownEvent;
import io.getstream.chat.android.client.events.UserPresenceChangedEvent;
import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.client.utils.observable.Disposable;

public class Events {
    private ChatClient client;
    private ChannelClient channelClient;

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
            ChannelClient channelClient = client.channel("messaging", "general");

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

        public void listenAllChannelEvents() {
            Disposable disposable = client.subscribe((ChatEvent event) -> {
                // Check for specific event types
                if (event instanceof NewMessageEvent) {
                    Message message = ((NewMessageEvent) event).getMessage();
                }
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
                    }
            );

            // Subscribe for just the first ConnectedEvent
            client.subscribeForSingle(
                    ConnectedEvent.class,
                    event -> {
                        // Use event data
                        int unreadCount = event.getMe().getTotalUnreadCount();
                        int unreadChannels = event.getMe().getUnreadChannels();
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
                    }
            );
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/event_listening/?language=java#stop-listening-for-events">Stop Listeing for Events</a>
         */
        public void stopListeningEvents() {
            final Disposable disposable = client.subscribe(chatEvent -> {
                /* ... */
            });
            disposable.dispose();
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/custom_events/?language=java">Custom Events</a>
     */
    class CustomEvents {

        /**
         * @see <a href="https://getstream.io/chat/docs/android/custom_events/?language=java#to-a-channel">Sending Custom Events</a>
         */
        public void sendingCustomEvents() {
            Map<Object, Object> extraData = new HashMap<>();
            extraData.put("text", "Hey there, long time no see!");

            // Send a custom event to all users watching the channel
            channelClient.sendEvent("friendship_request", extraData).enqueue(result -> {
                if (result.isSuccess()) {
                    ChatEvent chatEvent = result.data();
                } else {
                    // Handle result.error()
                }
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/android/custom_events/?language=java#to-a-channel">Listening for Custom Events</a>
         */
        public void listeningForCustomEvents() {
            ChannelClient channelClient = client.channel("messaging", "general");

            // Subscribe for custom events
            Disposable disposable = channelClient.subscribeFor(
                    new Class[]{UnknownEvent.class},
                    (ChatEvent event) -> {
                        UnknownEvent customEvent = (UnknownEvent) event;
                        String text = (String) customEvent.getRawData().get("text");
                    }
            );

            // Dispose when you want to stop receiving events
            disposable.dispose();
        }
    }
}
