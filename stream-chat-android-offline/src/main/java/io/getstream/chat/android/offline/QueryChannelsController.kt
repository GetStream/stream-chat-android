package io.getstream.chat.android.offline

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.livedata.controller.QueryChannelsController
import kotlinx.coroutines.flow.StateFlow

/**
 * The QueryChannelsController is a small helper to show a list of channels
 *
 * - .channels a livedata object with the list of channels. this list
 * - .loading if we're currently loading
 * - .loadingMore if we're currently loading more channels
 *
 */
public interface QueryChannelsController {
    /**
     * The filter used for this query
     */
    public val filter: FilterObject

    /**
     * The sort used for this query
     */
    public val sort: QuerySort<Channel>

    /**
     * When the NotificationAddedToChannelEvent is triggered the newChannelEventFilter
     * determines if the channel should be added to the query or not.
     * Return true to add the channel, return false to ignore it.
     * By default it will simply add every channel for which this event is received
     */
    public var newChannelEventFilter: (Channel, FilterObject) -> Boolean

    /**
     * If the API call failed and we need to rerun this query
     */
    public val recoveryNeeded: Boolean

    /**
     * If we've reached the end of the channels
     */
    public val endOfChannels: StateFlow<Boolean>

    /**
     * The list of channels
     * Typically we recommend using [channelsState] instead, it's a bit more complex but ensures
     * that you're handling all edge cases
     *
     * @see channelsState
     */
    public val channels: StateFlow<List<Channel>>

    /**
     * Similar to the channels field, but returns the a ChannelsState object
     * This sealed class makes it easier to verify that you've implemented all possible error/no result states
     *
     * @see ChannelsState
     */
    public val channelsState: StateFlow<QueryChannelsController.ChannelsState>

    /**
     * If we are currently loading channels
     */
    public val loading: StateFlow<Boolean>

    /**
     * If we are currently loading more channels
     */
    public val loadingMore: StateFlow<Boolean>
}
