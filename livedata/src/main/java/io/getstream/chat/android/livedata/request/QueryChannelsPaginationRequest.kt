package io.getstream.chat.android.livedata.request

/**
 * Paginate query channels on the queryChannels repo
 * Similar to QueryChannelsRequest but without the watch, filter and sort params
 * Since those are provided by the QueryChannelsRepo
 */
data class QueryChannelsPaginationRequest(
    val channelOffset: Int = 0,
    val channelLimit: Int = 30,
    val messageLimit: Int = 10
) {

    val isFirstPage: Boolean
        get() = channelOffset == 0
}
