package io.getstream.chat.docs.kotlin

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.channel.subscribeFor
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.ConnectingEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.subscribeFor
import io.getstream.chat.android.client.subscribeForSingle
import io.getstream.chat.android.client.utils.observable.Disposable

class Events(val client: ChatClient, val channelClient: ChannelClient) {

    /**
     * @see <a href="https://getstream.io/chat/docs/event_object/?language=kotlin">Event Object</a>
     */
    inner class EventObject

    /**
     * @see <a href="https://getstream.io/chat/docs/event_listening/?language=kotlin">Listening For Events</a>
     */
    inner class ListeningForEvents {
        fun listenSpecificChannelEvents() {
            // Subscribe for new message events
            val disposable: Disposable = channelClient
                .subscribeFor<NewMessageEvent> { newMessageEvent ->
                    // To get the message
                    val message = newMessageEvent.message
                }

            // Dispose when you want to stop receiving events
            disposable.dispose()
        }

        fun listenAllChannelEvents() {
            val disposable: Disposable = channelClient
                .subscribe { event: ChatEvent ->
                    when (event) {
                        is NewMessageEvent -> {
                            // To get the message
                            val message = event.message
                        }
                    }
                }

            // Dispose when you want to stop receiving events
            disposable.dispose()
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/event_listening/?language=kotlin#client-events">Client Events</a>
         */
        fun listenClientEvents() {
            // Subscribe for User presence events
            client.subscribeFor<UserPresenceChangedEvent> { event ->
                // Handle change
            }

            // Subscribe for just the first ConnectedEvent
            client.subscribeForSingle<ConnectedEvent> { event ->
                // Use event data
                val unreadCount = event.me.totalUnreadCount
                val unreadChannels = event.me.unreadChannels
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/event_listening/?language=kotlin#connection-events">Connection Events</a>
         */
        fun listenConnectionEvents() {
            client.subscribeFor(ConnectedEvent::class, ConnectingEvent::class, DisconnectedEvent::class) { event ->
                when (event) {
                    is ConnectedEvent -> {
                        // Socket is connected
                    }
                    is ConnectingEvent -> {
                        // Socket is connecting
                    }
                    is DisconnectedEvent -> {
                        // Socket is disconnected
                    }
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/event_listening/?language=kotlin#stop-listening-for-events">Stop Listeing for Events</a>
         */
        fun stopListeningEvents() {
            val disposable: Disposable = client.subscribe { /* ... */ }
            disposable.dispose()
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/event_typing/?language=kotlin">Typing Events</a>
     */
    inner class TypingEvents {
        fun sendTypingEvents() {
            // Sends a typing.start event if it's been more than 3000 ms since the last event
            channelClient.keystroke().enqueue()

            // Sends an event typing.stop to all channel participants
            channelClient.stopTyping().enqueue()
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/notification_events/?language=kotlin">Typing Events</a>
     */
    inner class NotificationEvents {
        fun notificationEvents() {
            // An example of how listen event when a user is added to a channel
            channelClient.subscribeFor<NotificationAddedToChannelEvent> { notificationEvent ->
                // Handle event
            }
        }
    }
}
