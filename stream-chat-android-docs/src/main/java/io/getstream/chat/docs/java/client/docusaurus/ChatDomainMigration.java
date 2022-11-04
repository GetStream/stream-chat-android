package io.getstream.chat.docs.java.client.docusaurus;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.Collections;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.api.models.FilterObject;
import io.getstream.chat.android.client.api.models.QueryChannelsRequest;
import io.getstream.chat.android.client.api.models.querysort.QuerySortByField;
import io.getstream.chat.android.client.api.models.querysort.QuerySorter;
import io.getstream.chat.android.client.channel.state.ChannelState;
import io.getstream.chat.android.client.extensions.FlowExtensions;
import io.getstream.chat.android.client.models.Channel;
import io.getstream.chat.android.client.models.Filters;
import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.client.models.UploadAttachmentsNetworkType;
import io.getstream.chat.android.client.models.User;
import io.getstream.chat.android.offline.extensions.ChatClientExtensions;
import io.getstream.chat.android.offline.plugin.configuration.Config;
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory;
import io.getstream.chat.android.offline.plugin.state.channel.thread.ThreadState;
import io.getstream.chat.android.offline.plugin.state.global.GlobalState;
import io.getstream.chat.android.offline.plugin.state.querychannels.QueryChannelsState;
import kotlinx.coroutines.flow.StateFlow;

/**
 * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/chatdomain-migration/">ChatDomain Migration</a>
 */
public class ChatDomainMigration {
    private String apiKey = "api-key";

    ChatClient chatClient = ChatClient.instance();

    public void initialization(Context context) {
        // Enables background sync which is performed to sync user actions done while offline.
        boolean backgroundSyncEnabled = true;
        // Enables the ability to receive information about user activity such as last active date and if they are online right now.
        boolean userPresence = true;
        // Enables using the database as an internal caching mechanism.
        boolean persistenceEnabled = true;
        // An enumeration of various network types used as a constraint inside upload attachments worker.
        UploadAttachmentsNetworkType uploadAttachmentsNetworkType = UploadAttachmentsNetworkType.NOT_ROAMING;

        StreamOfflinePluginFactory offlinePluginFactory = new StreamOfflinePluginFactory(new Config(backgroundSyncEnabled, userPresence, persistenceEnabled), context);
        new ChatClient.Builder("apiKey", context).withPlugins(offlinePluginFactory).build();
    }

    public void requestingData() {
        String cid = "channel-cid";
        String messageText = "Hey there!";

        Message message = new Message();
        message.setCid(cid);
        message.setText(messageText);

        // Old approach
        /*
        chatDomain.sendMessage(message).enqueue(result -> {
            if (result.isSuccess()) {
                // Handle success
            } else {
                // Handle error
            }
        });
        */

        // New approach
        chatClient.channel(cid).sendMessage(message).enqueue(result -> {
            if (result.isSuccess()) {
                // Handle success
            } else {
                // Handle error
            }
        });
    }

    class State {
        FilterObject filter = Filters.eq("type", "messaging");
        QuerySorter<Channel> sort = QuerySortByField.descByName("lastUpdated");
        QueryChannelsRequest queryChannelsRequest = new QueryChannelsRequest(
                Filters.eq("type", "messaging"),
                0,
                30,
                new QuerySortByField<Channel>(),
                1,
                30
        );

        public void accessStates() {
            // Returns QueryChannelsState object based on filter and sort used to query channels
            QueryChannelsState queryChannelsState = ChatClientExtensions.getState(chatClient).queryChannels(filter, sort);

            // Returns ChannelState object for a given channel
            ChannelState channelState = ChatClientExtensions.getState(chatClient).channel("messaging", "sampleId");

            // Returns ThreadState object for a thread associated with a given parentMessageId
            ThreadState threadState = ChatClientExtensions.getState(chatClient).thread("parentMessageId");

            // Gives you access to GlobalState object
            GlobalState globalState = ChatClientExtensions.getGlobalState(chatClient);
        }

        public void requestChannelsAndObserveTheState() {
            // 1. Get the first 30 channels to which thierry belongs
            FilterObject filter = Filters.in("members", "thierry");
            QuerySorter<Channel> sort = QuerySortByField.descByName("lastUpdated");
            int limit = 30;
            int offset = 0;
            int messageLimit = 1;
            int memberLimit = 30;
            QueryChannelsRequest request = new QueryChannelsRequest(filter, offset, limit, sort, messageLimit, memberLimit);
            chatClient.queryChannels(request).enqueue(result -> {
                if (result.isSuccess()) {
                    // Request successful. Data will be propagated to the state object
                } else {
                    // Handle error
                }
            });
            // 2. Get the state object associated with the above API call
            QueryChannelsState queryChannelsState = ChatClientExtensions.getState(chatClient).queryChannels(filter, sort);
        }

        public void watchChannel() {
            // Old approach - returns ChannelController object and performs watchChannel request
            // ChatDomain.instance().watchChannel("messaging:sampleId", 30).enqueue(result -> {
            //            if (result.isSuccess()) {
            //                // Handle success
            //            } else {
            //                // Handle error
            //            }
            //        });

            // New approach - returns the LiveData<ChannelState> object and performs watchChannel request
            StateFlow<ChannelState> channelState = ChatClientExtensions.watchChannelAsState(chatClient, "messaging:sampleId", 30);
            LiveData<ChannelState> channelStateLiveData = FlowExtensions.asLiveData(channelState);
        }
    }

    public void otherChanges() {
        // Old approach of update message
        /*
        Message messageToUpdate = new Message();
        messageToUpdate.setText("Updated text");
        ChatDomain.instance().editMessage(messageToUpdate).enqueue { result ->
            if (result.isSuccess) {
                // Handle success
            } else {
                // Handle error
            }
        }*/

        // New approach of updating message
        Message messageToUpdate = new Message();
        messageToUpdate.setText("Updated text");
        chatClient.updateMessage(messageToUpdate).enqueue(result -> {
            if (result.isSuccess()) {
                // Handle success
            } else {
                // Handle error
            }
        });

        // Old approach of leaving channel
        /*ChatDomain.instance().leaveChannel(cid).enqueue(result -> {
                if (result.isSuccess()) {
                    // Handle success
                } else {
                    // Handle error
                }
            });
        }*/

        String cid = "cid";
        // New approach of leaving channel
        User currentUser = chatClient.getCurrentUser();
        if (currentUser != null) {
            chatClient.channel(cid).removeMembers(Collections.singletonList(currentUser.getId()), null).enqueue(result -> {
                if (result.isSuccess()) {
                    // Handle success
                } else {
                    // Handle error
                }
            });
        }
    }
}
