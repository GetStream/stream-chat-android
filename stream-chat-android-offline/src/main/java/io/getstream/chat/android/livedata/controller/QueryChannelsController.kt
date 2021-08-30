package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.LiveData
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.offline.querychannels.QueryChannelsController

/**
 * The QueryChannelsController is a small helper to show a list of channels
 *
 * - .channels a livedata object with the list of channels. this list
 * - .loading if we're currently loading
 * - .loadingMore if we're currently loading more channels
 *
 */
public sealed interface QueryChannelsController {
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
     * When ChannelUpdatedEvent is triggered, if it is true a new query to the server is done to check if the update
     * on the channel match the filter to be added or deleted from the list of channels
     */
    public var checkFilterOnChannelUpdatedEvent: Boolean

    /**
     * If the API call failed and we need to rerun this query
     */
    public val recoveryNeeded: Boolean
    /**
     * If we've reached the end of the channels
     */
    public val endOfChannels: LiveData<Boolean>
    /**
     * The list of channels
     * Typically we recommend using [channelsState] instead, it's a bit more complex but ensures
     * that you're handling all edge cases
     *
     * @see channelsState
     */
    public val channels: LiveData<List<Channel>>
    /**
     * Similar to the channels field, but returns the a ChannelsState object
     * This sealed class makes it easier to verify that you've implemented all possible error/no result states
     *
     * @see ChannelsState
     */
    public val channelsState: LiveData<ChannelsState>

    /**
     * If we are currently loading channels
     */
    public val loading: LiveData<Boolean>
    /**
     * If we are currently loading more channels
     */
    public val loadingMore: LiveData<Boolean>

    public val mutedChannelIds: LiveData<List<String>>

    public sealed class ChannelsState {
        /** The QueryChannelsController is initialized but no query is currently running.
         * If you know that a query will be started you typically want to display a loading icon.
         * */
        public object NoQueryActive : ChannelsState()
        /** Indicates we are loading the first page of results.
         * We are in this state if QueryChannelsController.loading is true
         * For seeing if we're loading more results have a look at QueryChannelsController.loadingMore
         *
         * @see QueryChannelsController.loadingMore
         * @see QueryChannelsController.loading
         * */
        public object Loading : ChannelsState()
        /** If we are offline and don't have channels stored in offline storage, typically displayed as an error condition. */
        public object OfflineNoResults : ChannelsState()
        /** The list of channels, loaded either from offline storage or an API call.
         * Observe chatDomain.online to know if results are currently up to date
         * @see ChatDomainImpl.online
         * */
        public data class Result(val channels: List<Channel>) : ChannelsState()
    }
}
