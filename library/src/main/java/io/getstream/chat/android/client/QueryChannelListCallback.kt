package io.getstream.chat.android.client

import io.getstream.chat.android.client.api.QueryChannelsResponse


interface QueryChannelListCallback {
    fun onSuccess(response: QueryChannelsResponse)
    fun onError(errMsg: String, errCode: Int)
}