package io.getstream.chat.docs.java;

import android.content.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.api.models.FilterObject;
import io.getstream.chat.android.client.api.models.QueryChannelsRequest;
import io.getstream.chat.android.client.api.models.QuerySort;
import io.getstream.chat.android.client.channel.ChannelClient;
import io.getstream.chat.android.client.logger.ChatLogLevel;
import io.getstream.chat.android.client.models.Channel;
import io.getstream.chat.android.client.models.Filters;
import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.client.models.User;
import io.getstream.chat.android.livedata.ChatDomain;
import io.getstream.chat.android.livedata.controller.ChannelController;
import io.getstream.chat.android.livedata.controller.QueryChannelsController;

public class AndroidIntroduction {

    /**
     * @see <a href="https://getstream.io/chat/docs/android/?language=java#chat-client">Chat Client</a>
     */
    public void chatClient(Context applicationContext) {
        String apiKey = "{{ api_key }}";
        String token = "{{ chat_user_token }}";
        // Step 1 - Set up the client for API calls
        ChatClient client = new ChatClient.Builder(apiKey, applicationContext)
                // Change log level
                .logLevel(ChatLogLevel.ALL)
                .build();
        // Step 2 - Set up the domain for offline storage
        ChatDomain domain = new ChatDomain.Builder(applicationContext, client)
                // Enable offline support
                .offlineEnabled()
                .build();

        // Step 2 - Authenticate and connect the user
        User user = new User();
        user.setId("summer-brook-2");
        user.getExtraData().put("name", "Paranoid Android");
        user.getExtraData().put("image", "https://bit.ly/2TIt8NR");

        // You can use client.devToken(userId) if auth is disabled for your app
        client.connectUser(user, token).enqueue((result) -> {
            if (result.isSuccess()) {
                // Handle success
            } else {
                // Handler error
            }
        });
    }

    public void watchingAChannel(ChatClient client, ChatDomain chatDomain) {
        ChannelClient channelClient = client.channel("messaging", "travel");

        Map<String, Object> extraData = new HashMap<>();
        extraData.put("name", "Awesome channel about traveling");

        // Creating a channel with the low level client
        channelClient.create(extraData).enqueue(result -> {
            if (result.isSuccess()) {
                Channel channel = result.data();
                // Use channel by calling methods on channelClient
            } else {
                // Handle result.error()
            }
        });

        // Watching a channel's state using the offline library
        chatDomain.getUseCases()
                .getWatchChannel()
                .invoke("messaging:travel", 10)
                .enqueue(result -> {
                    if (result.isSuccess()) {
                        ChannelController channelController = result.data();

                        // LiveData objects to observe
                        channelController.getMessages();
                        channelController.getReads();
                        channelController.getTyping();
                    }
                });
    }

    public void sendFirstMessage(ChannelClient channelClient, ChatDomain chatDomain) {
        Message message = new Message();
        message.setText("Hello world");
        message.setCid("messaging:travel");
        message.putExtraValue("customField", "123");

        // Using the low level client
        channelClient.sendMessage(message).enqueue(result -> {
            if (result.isSuccess()) {
                Message sentMessage = result.data();
            } else {
                // Handle result.error()
            }
        });

        // Using the offline support library
        chatDomain.getUseCases().getSendMessage().invoke(message).enqueue(result -> {
            if (result.isSuccess()) {
                Message sentMessage = result.data();
            } else {
                // Handle result.error()
            }
        });
    }

    public void queryChannels(ChatClient client, ChatDomain chatDomain) {
        FilterObject filter = Filters.and(
                Filters.eq("type", "messaging"),
                Filters.in("members", "john")
        );
        QuerySort<Channel> sort = new QuerySort<Channel>().desc("last_message_at");
        int offset = 0;
        int limit = 10;
        int memberLimit = 0;
        int messageLimit = 0;

        // Using the low level client to query channels
        QueryChannelsRequest request = new QueryChannelsRequest(filter, offset, limit, sort, messageLimit, memberLimit).withWatch().withState();
        client.queryChannels(request).enqueue(result -> {
            if (result.isSuccess()) {
                List<Channel> channels = result.data();
            } else {
                // Handle result.error()
            }
        });

        // Using the offline library to query channels
        chatDomain.getUseCases().getQueryChannels()
                .invoke(filter, sort, limit, messageLimit)
                .enqueue(result -> {
                    if (result.isSuccess()) {
                        QueryChannelsController queryChannelsController = result.data();

                        // LiveData objects to observe
                        queryChannelsController.getChannels();
                        queryChannelsController.getLoading();
                        queryChannelsController.getEndOfChannels();
                    } else {
                        // Handle result.error()
                    }
                });
    }
}
