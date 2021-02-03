package io.getstream.chat.docs.java;

import io.getstream.chat.android.client.channel.ChannelClient;
import io.getstream.chat.android.client.events.TypingStartEvent;
import io.getstream.chat.android.client.events.TypingStopEvent;
import kotlin.Unit;

public class TypingIndicators {
    private ChannelClient channelClient;

    /**
     * @see <a href="https://getstream.io/chat/docs/typing_indicators/?language=java#sending-start-and-stop-typing-events">Sending Start and Stop Typing</a>
     */
    public void sendingStartAndStopTypingEvents() {
        // Sends a typing.start event
        channelClient.keystroke().enqueue();

        // Sends a typing.start event for a particular thread
        String threadId = "threadId";
        channelClient.keystroke(threadId).enqueue();

        // Sends the typing.stop event
        channelClient.stopTyping().enqueue();
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/typing_indicators/?language=java#receiving-typing-indicator-events">Receiving Typing Events</a>
     */
    public void receivingTypingEvents() {
        // Add typing start event handling
        channelClient.subscribeFor(
                new Class[]{TypingStartEvent.class},
                event -> {
                    // Handle change
                }
        );

        // Add typing stop event handling
        channelClient.subscribeFor(
                new Class[]{TypingStopEvent.class},
                event -> {
                    // Handle change
                }
        );
    }
}
