package io.getstream.chat.docs.java.client.docusaurus;

import android.content.Context;

import androidx.lifecycle.LiveData;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.api.models.FilterObject;
import io.getstream.chat.android.client.api.models.QueryChannelsRequest;
import io.getstream.chat.android.client.api.models.querysort.QuerySortByField;
import io.getstream.chat.android.client.api.models.querysort.QuerySorter;
import io.getstream.chat.android.client.channel.state.ChannelState;
import io.getstream.chat.android.client.extensions.FlowExtensions;
import io.getstream.chat.android.client.models.Channel;
import io.getstream.chat.android.client.models.Filters;
import io.getstream.chat.android.offline.extensions.ChatClientExtensions;
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory;
import io.getstream.chat.android.offline.plugin.state.channel.thread.ThreadState;
import io.getstream.chat.android.offline.plugin.state.global.GlobalState;
import io.getstream.chat.android.offline.plugin.state.querychannels.QueryChannelsState;
import kotlinx.coroutines.flow.StateFlow;

/**
 * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/offline-support">Offline Support</a>
 */
public class Offline {

    private String apiKey = "api-key";

    public void configureOfflinePlugin(Context context) {
        // Enables the ability to receive information about user activity such as last active date and if they are online right now.
        boolean userPresence = true;
        // Enables using the database as an internal caching mechanism.
        boolean persistenceEnabled = true;
        // An enumeration of various network types used as a constraint inside upload attachments worker.

        StreamOfflinePluginFactory offlinePluginFactory = new StreamOfflinePluginFactory(context);
        new ChatClient.Builder("apiKey", context).withPlugins(offlinePluginFactory).build();
    }

    public class State {
        ChatClient chatClient = ChatClient.instance();

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

        public void accessStateWithApiCall() {
            // Returns LiveData<QueryChannelsState> object and performs queryChannels request
            StateFlow<QueryChannelsState> queryChannelsStateStateFlow = ChatClientExtensions.queryChannelsAsState(chatClient, queryChannelsRequest);
            LiveData<QueryChannelsState> queryChannelsStateLiveData = FlowExtensions.asLiveData(queryChannelsStateStateFlow);

            // Returns LiveData<ChannelState> object and performs watchChannel request
            StateFlow<ChannelState> channelStateStateFlow = ChatClientExtensions.watchChannelAsState(chatClient, "messaging:sampleId", 30);
            LiveData<ChannelState> channelStateLiveData = FlowExtensions.asLiveData(channelStateStateFlow);

            // Returns ThreadState object for a thread associated with a given parentMessageId
            ThreadState threadState = ChatClientExtensions.getRepliesAsState(chatClient, "messaging:sampleId", 30);
        }
    }
}
