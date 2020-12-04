package io.getstream.chat.docs.kotlin

import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.channel.subscribeFor
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.TypingStopEvent

class TypingIndicators(val channelController: ChannelClient) {

    /**
     * @see <a href="https://getstream.io/chat/docs/typing_indicators/?language=kotlin#sending-start-and-stop-typing-events">Sending Start and Stop Typing</a>
     */
    fun sendingStartAndStopTypingEvents() {
        // Sends a typing.start event at most once every two seconds
        channelController.keystroke().enqueue()

        // Sends the typing.stop event
        channelController.stopTyping().enqueue()
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/typing_indicators/?language=kotlin#receiving-typing-indicator-events">Receiving Typing Events</a>
     */
    fun receivingTypingEvents() {
        // Add typing start event handling
        channelController.subscribeFor<TypingStartEvent> { typingStartEvent ->
            // Handle event
        }

        // Add typing top event handling
        channelController.subscribeFor<TypingStopEvent> { typingStopEvent ->
            // Handle event
        }
    }
}