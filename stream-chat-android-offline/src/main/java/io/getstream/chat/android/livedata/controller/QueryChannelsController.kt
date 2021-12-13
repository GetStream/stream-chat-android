package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.LiveData
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.offline.querychannels.ChatEventHandler
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
     * Instance of [ChatEventHandler] that handles logic of event handling for this [QueryChannelsController].
     */
    public var chatEventHandler: ChatEventHandler?

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

    @Deprecated(
        message = "Use ChatDomain.mutedChannels instead",
        replaceWith = ReplaceWith("ChatDomain.instance().mutedChannels"),
        level = DeprecationLevel.ERROR,
    )
    public val mutedChannelIds: LiveData<List<String>>

    public sealed class ChannelsState {
        /** The QueryChannelsController is initialized but no query is currently running.
         * If you know that a query will be started you typically want to display a loading icon.
         */
        public object NoQueryActive : ChannelsState()

        /** Indicates we are loading the first page of results.
         * We are in this state if QueryChannelsController.loading is true
         * For seeing if we're loading more results have a look at QueryChannelsController.loadingMore
         *
         * @see QueryChannelsController.loadingMore
         * @see QueryChannelsController.loading
         */
        public object Loading : ChannelsState()

        /** If we are offline and don't have channels stored in offline storage, typically displayed as an error condition. */
        public object OfflineNoResults : ChannelsState()

        /** The list of channels, loaded either from offline storage or an API call.
         * Observe chatDomain.online to know if results are currently up to date
         * @see ChatDomainImpl.connectionState
         */
        public data class Result(val channels: List<Channel>) : ChannelsState()
    }
}
