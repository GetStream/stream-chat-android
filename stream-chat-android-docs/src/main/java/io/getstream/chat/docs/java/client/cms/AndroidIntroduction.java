package io.getstream.chat.docs.java.client.cms;

import android.content.Context;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.api.models.FilterObject;
import io.getstream.chat.android.client.api.models.QueryChannelsRequest;
import io.getstream.chat.android.client.api.models.querysort.QuerySortByField;
import io.getstream.chat.android.client.api.models.querysort.QuerySorter;
import io.getstream.chat.android.client.channel.ChannelClient;
import io.getstream.chat.android.client.extensions.FlowExtensions;
import io.getstream.chat.android.client.logger.ChatLogLevel;
import io.getstream.chat.android.client.models.Channel;
import io.getstream.chat.android.client.models.Filters;
import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.client.models.User;
import io.getstream.chat.android.offline.extensions.ChatClientExtensions;
import io.getstream.chat.android.offline.model.message.attachments.UploadAttachmentsNetworkType;
import io.getstream.chat.android.offline.plugin.configuration.Config;
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory;
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState;
import kotlinx.coroutines.flow.StateFlow;

public class AndroidIntroduction {

    /**
     * @see <a href="https://getstream.io/chat/docs/android/?language=java#chat-client">Chat Client</a>
     */
    public void chatClient(Context applicationContext) {
        String apiKey = "{{ api_key }}";
        String token = "{{ chat_user_token }}";
        // Step 1 - Set up the OfflinePlugin for offline storage
        Config config = new Config(true, true, true, UploadAttachmentsNetworkType.NOT_ROAMING);
        StreamOfflinePluginFactory offlinePluginFactory = new StreamOfflinePluginFactory(config, applicationContext);

        // Step 2 - Set up the client, together with offline plugin, for API calls
        ChatClient client = new ChatClient.Builder(apiKey, applicationContext)
                // Change log level
                .logLevel(ChatLogLevel.ALL)
                .withPlugin(offlinePluginFactory)
                .build();

        // Step 3 - Authenticate and connect the user
        Map<String, Object> extraData = new HashMap<>();
        extraData.put("name", "Paranoid Android");
        extraData.put("image", "https://bit.ly/2TIt8NR");
        User user = new User();
        user.setId("summer-brook-2");
        user.setExtraData(extraData);

        client.connectUser(user, token).enqueue((result) -> {
            if (result.isSuccess()) {
                // Handle success
            } else {
                // Handler error
            }
        });
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/?language=java#channels">Channels</a>
     */
    public void channels(ChatClient client, LifecycleOwner lifecycleOwner) {
        ChannelClient channelClient = client.channel("messaging", "travel");

        Map<String, Object> extraData = new HashMap<>();
        extraData.put("name", "Awesome channel about traveling");
        List<String> memberIds = new ArrayList<String>();

        // Creating a channel with the low level client
        channelClient.create(memberIds, extraData).enqueue((result) -> {
            if (result.isSuccess()) {
                Channel channel = result.data();
                // Use channel by calling methods on channelClient
            } else {
                // Handle result.error()
            }
        });

        // Watching a channel's state using the offline library
        StateFlow<ChannelState> channelStateFlow = ChatClientExtensions.watchChannelAsState(client, "messaging:travel", 0);
        LiveData<ChannelState> channelStateLiveData = FlowExtensions.asLiveData(channelStateFlow);

        channelStateLiveData.observe(lifecycleOwner, channelState -> {
            if (channelState != null) {
                // StateFlow objects to observe. Use FlowExtensions.asLiveData(stateFlow); to LiveData conversion.
                channelState.getMessages();
                channelState.getReads();
                channelState.getTyping();
            } else {
                // User not connected yet.
            }
        });
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/?language=java#messages">Messages</a>
     */
    public void messages(ChannelClient channelClient) {
        Map<String, Object> extraData = new HashMap<>();
        extraData.put("customField", "123");

        Message message = new Message();
        message.setText("I’m mowing the air Rand, I’m mowing the air.");
        message.setCid("messaging:travel");
        message.setExtraData(extraData);


        channelClient.sendMessage(message).enqueue((result) -> {
            if (result.isSuccess()) {
                Message sentMessage = result.data();
            } else {
                // Handle result.error()
            }
        });
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/?language=java#querying-channels">Querying Channels</a>
     */
    public void queryingChannels(ChatClient client) {
        FilterObject filter = Filters.and(Filters.eq("type", "messaging"), Filters.in("members", "john"));
        QuerySorter<Channel> sort = QuerySortByField.descByName("lastMessageAt");

        int offset = 0;
        int limit = 10;
        int messageLimit = 0;
        int memberLimit = 0;
        QueryChannelsRequest request = new QueryChannelsRequest(filter, offset, limit, sort, messageLimit, memberLimit).withWatch().withState();

        client.queryChannels(request).enqueue((result) -> {
            if (result.isSuccess()) {
                List<Channel> channels = result.data();
            } else {
                // Handle result.error()
            }
        });
    }
}
