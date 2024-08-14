package io.getstream.chat.docs.java.client.docusaurus;

import static io.getstream.result.call.CallKt.forceNewRequest;

import android.content.Context;

import java.util.List;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.api.models.QueryChannelsRequest;
import io.getstream.result.call.Call;
import io.getstream.chat.android.models.Channel;

/**
 * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/optimizations/">Optimizations</a>
 */
public class Optimizations {

    public void multipleCallsToAPI(Context context, String apiKey, QueryChannelsRequest queryChannelsRequest) {
        new ChatClient.Builder(apiKey, context).disableDistinctApiCalls().build();

        Call<List<Channel>> queryChannelsCall = ChatClient.instance().queryChannels(queryChannelsRequest);
        forceNewRequest(queryChannelsCall);
        queryChannelsCall.enqueue(result -> {
            if (result.isSuccess()) {
                // Handle success
            } else {
                // Handle error
            }
        });
    }
}
