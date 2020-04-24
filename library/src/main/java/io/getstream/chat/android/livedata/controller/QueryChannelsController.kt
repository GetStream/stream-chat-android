package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.LiveData
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.livedata.entity.QueryChannelsEntity

interface QueryChannelsController {
    var queryEntity: QueryChannelsEntity
    var recoveryNeeded: Boolean
    val endOfChannels: LiveData<Boolean>
    // Ensure we don't lose the sort in the channel
    var channels: LiveData<List<Channel>>
    val loading: LiveData<Boolean>
    val loadingMore: LiveData<Boolean>
}