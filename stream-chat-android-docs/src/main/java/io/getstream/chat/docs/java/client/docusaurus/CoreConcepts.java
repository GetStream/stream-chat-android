package io.getstream.chat.docs.java.client.docusaurus;

import io.getstream.chat.android.client.channel.ChannelClient;
import io.getstream.chat.android.models.Channel;
import io.getstream.chat.android.models.Message;
import io.getstream.chat.android.client.utils.Result;

/**
 * @see <a href="https://getstream.io/chat/docs/sdk/android/basics/core-concepts/#core-concepts">Core Concepts</a>
 */
public class CoreConcepts {

    public void calls(ChannelClient channelClient, Message message) {
        // Only call this from a background thread
        Result<Message> messageResult = channelClient.sendMessage(message).execute();
    }

    public void runningCallsAsynchronously(ChannelClient channelClient, Message message) {
        // Safe to call from the main thread
        channelClient.sendMessage(message).enqueue((result) -> {
            if (result.isSuccess()) {
                Message sentMessage = result.getOrNull();
            } else {
                // Handle error
            }
        });
    }

    public void errorHandling(Result<Channel> result) {
        Boolean isSuccess = result.isSuccess();
        Boolean isFailure = result.isFailure();

        if (result.isSuccess()) {
            // Handle success
        } else {
            // Handle error
        }
    }
}
