package io.getstream.chat.android.offline.querychannels

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.utils.Result
import kotlinx.coroutines.flow.StateFlow

/* Default filter to include FilterObject in a channel by its cid
*
* @param client - ChatClient to perform the filter
* @param cid - The cid of the channel of the filter
* @param filter - the filter to be included with the cid.
*/
internal object ChannelFilterRequest {
    suspend fun filter(client: ChatClient, cid: String, filter: FilterObject): Result<List<Channel>> =
        client.queryChannelsInternal(
            QueryChannelsRequest(
                filter = Filters.and(
                    filter,
                    Filters.eq("cid", cid)
                ),
                offset = 0,
                limit = 1,
                messageLimit = 0,
                memberLimit = 0,
            )
        ).await()
}

/**
 * Checks if the channel collection contains a channel, if yes then it returns skip handling result, otherwise it
 * adds the channel.
 */
internal fun addIfChannelIsAbsent(channels: StateFlow<List<Channel>>, channel: Channel?): EventHandlingResult {
    return if (channel == null || channels.value.any { it.cid == channel.cid }) {
        EventHandlingResult.Skip
    } else {
        EventHandlingResult.Add(channel)
    }
}

/**
 * Checks if the channel collection contains a channel, if yes then it removes it. Otherwise it simply skips the event
 */
internal fun removeIfChannelIsPresent(channels: StateFlow<List<Channel>>, channel: Channel?): EventHandlingResult {
    return if (channel != null && channels.value.any { it.cid == channel.cid }) {
        EventHandlingResult.Remove(channel.cid)
    } else {
        EventHandlingResult.Skip
    }
}
