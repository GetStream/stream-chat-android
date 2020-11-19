package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.LiveData
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.livedata.ChannelPreview

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
    public val endOfChannels: LiveData<Boolean>
    /**
     * The list of channels
     */
    public val channels: LiveData<List<Channel>>
    /**
     * If we are currently loading channels
     */
    public val loading: LiveData<Boolean>
    /**
     * If we are currently loading more channels
     */
    public val loadingMore: LiveData<Boolean>

    /**
     * A list of channel preview objects, in addition to the regular channel information it contains unread counts and typing information
     */
    public var channelPreviews: LiveData<List<ChannelPreview>>
}
