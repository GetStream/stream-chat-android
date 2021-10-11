package io.getstream.chat.android.offline.experimental.plugin.adapter

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.offline.experimental.querychannels.QueryChannelsReference

@InternalStreamChatApi
@ExperimentalStreamChatApi
public class ChatClientReferenceAdapter(private val chatClient: ChatClient) {
    public fun queryChannels(request: QueryChannelsRequest): QueryChannelsReference = QueryChannelsReference(request, chatClient)
}
