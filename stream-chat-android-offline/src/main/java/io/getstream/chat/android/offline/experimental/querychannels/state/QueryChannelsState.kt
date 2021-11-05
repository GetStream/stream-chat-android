package io.getstream.chat.android.offline.experimental.querychannels.state

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.offline.querychannels.ChatEventsHandler
import kotlinx.coroutines.flow.StateFlow

@InternalStreamChatApi
@ExperimentalStreamChatApi
public interface QueryChannelsState {
    /** The filter is associated with this query channels state. */
    public val filter: FilterObject
    /** The sort object which requested for this query channels state. */
    public val sort: QuerySort<Channel>
    /** The request for the current page. */
    public val currentRequest: StateFlow<QueryChannelsRequest?>
    /** The request for the next page, if there is a page. */
    public val nextPageRequest: StateFlow<QueryChannelsRequest?>
    /** If the current state is being loaded. */
    public val loading: StateFlow<Boolean>
    /** If the current state is loading more channels (a next page is being loaded). */
    public val loadingMore: StateFlow<Boolean>
    /** If the current state reached the final page. */
    public val endOfChannels: StateFlow<Boolean>
    /** The collection of channels loaded by the query channels request. */
    public val channels: StateFlow<List<Channel>>
    /** The collection of channel IDs that were muted. */
    public val mutedChannelIds: StateFlow<List<String>>
    /** The channels loaded state. See [ChannelsStateData]. */
    public val channelsStateData: StateFlow<ChannelsStateData>
    /** Instance of [ChatEventsHandler] that handles logic of event handling for this [QueryChannelsState]. */
    public var chatEventsHandler: ChatEventsHandler?
}
