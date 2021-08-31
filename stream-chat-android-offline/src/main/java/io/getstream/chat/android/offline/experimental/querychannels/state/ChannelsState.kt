package io.getstream.chat.android.offline.experimental.querychannels.state

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.offline.ChatDomainImpl

@InternalStreamChatApi
@ExperimentalStreamChatApi
public sealed class ChannelsState {
    /** No query is currently running.
     * If you know that a query will be started you typically want to display a loading icon.
     * */
    public object NoQueryActive : ChannelsState()

    /** Indicates we are loading the first page of results.
     * We are in this state if QueryChannelsState.loading is true
     * For seeing if we're loading more results have a look at QueryChannelsState.loadingMore
     *
     * @see QueryChannelsState.loadingMore
     * @see QueryChannelsState.loading
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
