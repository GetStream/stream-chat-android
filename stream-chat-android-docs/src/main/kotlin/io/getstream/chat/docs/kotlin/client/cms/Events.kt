package io.getstream.chat.docs.kotlin.client.cms

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.channel.subscribeFor
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.ConnectingEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.subscribeFor
import io.getstream.chat.android.client.subscribeForSingle
import io.getstream.result.Result
import io.getstream.chat.android.client.utils.observable.Disposable

class Events(val client: ChatClient, val channelClient: ChannelClient) {

    /**
     * @see <a href="https://getstream.io/chat/docs/event_listening/?language=kotlin">Listening For Events</a>
     */
    inner class ListeningForEvents {
        fun listenSpecificChannelEvents() {
            val channelClient = client.channel("messaging", "general")

            // Subscribe for new message events
            val disposable: Disposable = channelClient.subscribeFor<NewMessageEvent> { newMessageEvent ->
                val message = newMessageEvent.message
            }

            // Dispose when you want to stop receiving events
            disposable.dispose()
        }

        fun listenAllChannelEvents() {
            val disposable: Disposable = channelClient.subscribe { event: ChatEvent ->
                when (event) {
                    // Check for specific event types
                    is NewMessageEvent -> {
                        val message = event.message
                    }
                    else -> Unit
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
            client.subscribeFor(
                ConnectedEvent::class,
                ConnectingEvent::class,
                DisconnectedEvent::class,
            ) { event ->
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
                    else -> Unit
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/event_listening/?language=kotlin#stop-listening-for-events">Stop Listening for Events</a>
         */
        fun stopListeningEvents() {
            val disposable: Disposable = client.subscribe { /* ... */ }
            disposable.dispose()
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/custom_events/?language=kotlin">Custom Events</a>
     */
    inner class CustomEvents {

        /**
         * @see <a href="https://getstream.io/chat/docs/android/custom_events/?language=kotlin#to-a-channel">Sending Custom Events</a>
         */
        fun toAChannel() {
            // Send a custom event to all users watching the channel
            channelClient.sendEvent(
                eventType = "friendship_request",
                extraData = mapOf("text" to "Hey there, long time no see!")
            ).enqueue { result ->
                when (result) {
                    is Result.Success -> {
                        val chatEvent: ChatEvent = result.value
                    }
                    is Result.Failure -> {
                        // Handler error
                    }
                }
            }
        }
    }
}
