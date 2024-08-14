package io.getstream.chat.docs.java.client.docusaurus;

import io.getstream.chat.android.client.channel.ChannelClient;
import io.getstream.chat.android.models.Channel;
import io.getstream.chat.android.models.Message;
import io.getstream.result.Result;
import io.getstream.result.Error;

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
        // Check if the call was successful
        Boolean isSuccess = result.isSuccess();
        // Check if the call had failed
        Boolean isFailure = result.isFailure();

        if (result.isSuccess()) {
            // Handle success
            Channel channel = result.getOrNull();
        } else {
            // Handle error
            Error error = result.errorOrNull();
        }
    }

    public void errorHandlingReactively(Result<Channel> result) {
        result.onSuccess(channel -> {
            // Handle success
            return null;
        }).onError(error -> {
            // Handle error
            return null;
        });
    }
}
