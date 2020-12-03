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

class Events(val client: ChatClient, val channelController: ChannelClient) {

    fun listenSpecificChannelEvents() {
        val disposable: Disposable = channelController
            .subscribeFor<NewMessageEvent> { newMessageEvent ->
                // to get the message
                val message = newMessageEvent.message
            }

        // Dispose when you want to stop receiving events
        disposable.dispose()
    }

    fun listenAllChannelEvents() {
        val disposable: Disposable = channelController
            .subscribe { event: ChatEvent ->
                when (event) {
                    is NewMessageEvent -> {
                        // to get the message
                        val message = event.message
                    }
                }
            }

        // Dispose when you want to stop receiving events
        disposable.dispose()
    }

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

    fun sendTypingEvents() {
        // sends a typing.start event if it's been more than 3000 ms since the last event
        channelController.keystroke().enqueue()

        // sends an event typing.stop to all channel participants
        channelController.stopTyping().enqueue()
    }

    fun notificationEvents() {
        // an example of how listen event when a user is added to a channel
        channelController.subscribeFor<NotificationAddedToChannelEvent> { notificationEvent ->
            // Handle event
        }
    }
}