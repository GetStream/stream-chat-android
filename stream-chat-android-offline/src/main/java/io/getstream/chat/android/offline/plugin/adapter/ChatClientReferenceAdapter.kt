package io.getstream.chat.android.offline.plugin.adapter

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.offline.querychannels.QueryChannelsReference

public class ChatClientReferenceAdapter(private val chatClient: ChatClient) {
    public fun queryChannels(request: QueryChannelsRequest): QueryChannelsReference = QueryChannelsReference(request, chatClient)
}
