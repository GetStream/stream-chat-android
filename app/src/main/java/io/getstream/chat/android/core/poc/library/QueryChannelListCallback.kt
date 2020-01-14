package io.getstream.chat.android.core.poc.library

import io.getstream.chat.android.core.poc.library.api.QueryChannelsResponse


interface QueryChannelListCallback {
    fun onSuccess(response: QueryChannelsResponse)
    fun onError(errMsg: String, errCode: Int)
}