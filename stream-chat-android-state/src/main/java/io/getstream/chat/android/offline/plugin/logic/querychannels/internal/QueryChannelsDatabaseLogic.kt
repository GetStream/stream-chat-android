package io.getstream.chat.android.offline.plugin.logic.querychannels.internal

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelConfig
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest

public interface QueryChannelsDatabaseLogic {

    public suspend fun storeStateForChannels(
        configs: Collection<ChannelConfig>? = null,
        users: List<User>,
        channels: Collection<Channel>,
        messages: List<Message>,
        cacheForMessages: Boolean = false,
    )

    public suspend fun fetchChannelsFromCache(
        pagination: AnyChannelPaginationRequest,
        queryChannelsSpec: QueryChannelsSpec?
    ): List<Channel>

    public suspend fun selectChannelWithoutMessages(cid: String): Channel?

    public suspend fun insertQueryChannels(queryChannelsSpec: QueryChannelsSpec)

    public suspend fun insertChannelConfigs(configs: Collection<ChannelConfig>)
}
