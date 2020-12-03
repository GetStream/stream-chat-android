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

    public void listenSpecificChannelEvents() {
        Disposable disposable = client.subscribeForSingle(NewMessageEvent.class,
                (NewMessageEvent event) -> {
                    // to get the message
                    Message message = event.getMessage();
                    return Unit.INSTANCE;
                });

        // Dispose when you want to stop receiving events
        disposable.dispose();
    }

    public void listenAllChannelEvents() {
        Disposable disposable = channelController.subscribe((ChatEvent event) -> {
            if (event instanceof NewMessageEvent) {
                // to get the message
                Message message = ((NewMessageEvent) event).getMessage();
            }
            return Unit.INSTANCE;
        });

        // Dispose when you want to stop receiving events
        disposable.dispose();
    }

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

    public void sendTypingEvent() {
        // sends a typing.start event if it's been more than 3000 ms since the last event
        channelController.keystroke().enqueue(result -> Unit.INSTANCE);

        // sends an event typing.stop to all channel participants
        channelController.stopTyping().enqueue(result -> Unit.INSTANCE);
    }

    public void notificationEvents() {
        // an example of how listen event when a user is added to a channel
        channelController.subscribeFor(
                new Class[]{NotificationAddedToChannelEvent.class},
                addedToChannelEvent -> {
                    // Handle event
                    return Unit.INSTANCE;
                }
        );
    }
}
