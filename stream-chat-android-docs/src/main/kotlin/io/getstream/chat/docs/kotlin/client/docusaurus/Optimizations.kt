package io.getstream.chat.docs.kotlin.client.docusaurus

import android.content.Context
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.extensions.forceNewRequest

/**
 * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/optimizations/">Optimizations</a>
 */
class Optimizations {

    fun multipleCallsToAPI(context: Context, apiKey: String, queryChannelsRequest: QueryChannelsRequest) {
        ChatClient.Builder(apiKey, context).disableDistinctApiCalls().build()

        ChatClient.instance().queryChannels(queryChannelsRequest).forceNewRequest().enqueue { result ->
            if (result.isSuccess) {
                // Handle success
            } else {
                // Handle error
            }
        }
    }
}
