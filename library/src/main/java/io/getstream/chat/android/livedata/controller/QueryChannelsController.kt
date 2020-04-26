package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.LiveData
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.livedata.entity.QueryChannelsEntity


/**
 * The QueryChannelsController is a small helper to show a list of channels
 *
 * - .channels a livedata object with the list of channels. this list
 * - .loading if we're currently loading
 * - .loadingMore if we're currently loading more channels
 *
 */
interface QueryChannelsController {
    var queryEntity: QueryChannelsEntity
    var recoveryNeeded: Boolean
    val endOfChannels: LiveData<Boolean>
    // Ensure we don't lose the sort in the channel
    var channels: LiveData<List<Channel>>
    val loading: LiveData<Boolean>
    val loadingMore: LiveData<Boolean>
}
