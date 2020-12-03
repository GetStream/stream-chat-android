package io.getstream.chat.docs.java;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.channel.ChannelClient;
import io.getstream.chat.android.client.events.ChatEvent;
import io.getstream.chat.android.client.events.ConnectedEvent;
import io.getstream.chat.android.client.events.ConnectingEvent;
import io.getstream.chat.android.client.events.DisconnectedEvent;
import io.getstream.chat.android.client.events.NewMessageEvent;
import io.getstream.chat.android.client.events.UserPresenceChangedEvent;
import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.client.utils.observable.Disposable;
import kotlin.Unit;

public class Events {
    static ChatClient client;
    static ChannelClient channelController;

    public static void listenSpecificChannelEvents() {
        Disposable disposable = client.subscribeForSingle(NewMessageEvent.class,
                (NewMessageEvent event) -> {
                    // to get the message
                    Message message = event.getMessage();
                    return Unit.INSTANCE;
                });

        // Dispose when you want to stop receiving events
        disposable.dispose();
    }

    public static void listenAllChannelEvents() {
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

    public static void listenClientEvents() {
        // Subscribe for User presence events
        client.subscribeFor(
                new Class[]{UserPresenceChangedEvent.class},
                (ChatEvent chatEvent) -> {
                    // Handle change
                    return Unit.INSTANCE;
                }
        );

        // Subscribe for just the first ConnectedEvent
        client.subscribeForSingle(ConnectedEvent.class,
                (ConnectedEvent event) -> {
                    // Use event data
                    int unreadCount = event.getMe().getTotalUnreadCount();
                    int unreadChannels = event.getMe().getUnreadChannels();
                    return Unit.INSTANCE;
                });
    }

    public static void listenConnectionEvents() {
        client.subscribeFor(
                new Class[]{ConnectedEvent.class, ConnectingEvent.class, DisconnectedEvent.class},
                (ChatEvent chatEvent) -> {
                    if (chatEvent instanceof ConnectedEvent) {
                        // Socket is connected
                    } else if (chatEvent instanceof ConnectingEvent) {
                        // Socket is connecting
                    } else if (chatEvent instanceof DisconnectedEvent) {
                        // Socket is disconnected
                    }
                    return Unit.INSTANCE;
                }
        );
    }
}
