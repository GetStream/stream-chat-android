package io.getstream.chat.android.client.api2.optimisation.hash

import io.getstream.chat.android.client.api2.model.requests.QueryChannelRequest

internal data class ChannelQueryKey(
    val channelType: String,
    val channelId: String,
    val queryKey: QueryChannelRequest,
) {

    companion object {
        fun from(
            channelType: String,
            channelId: String,
            query: io.getstream.chat.android.client.api.models.QueryChannelRequest,
        ): ChannelQueryKey {
            return ChannelQueryKey(
                channelType = channelType,
                channelId = channelId,
                queryKey = QueryChannelRequest(
                    state = query.state,
                    watch = query.watch,
                    presence = query.presence,
                    messages = query.messages,
                    watchers = query.watchers,
                    members = query.members,
                    data = query.data,
                )
            )
        }
    }
}
