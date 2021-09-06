package io.getstream.chat.android.offline.experimental.channel.state

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.offline.ChatDomainImpl

public sealed class MessagesState {
    /** The ChannelState is initialized but no query is currently running.
     * If you know that a query will be started you typically want to display a loading icon.
     */
    public object NoQueryActive : MessagesState()

    /** Indicates we are loading the first page of results.
     * We are in this state if ChannelState.loading is true
     * For seeing if we're loading more results have a look at loadingNewerMessages and loadingOlderMessages
     *
     * @see loading
     * @see loadingNewerMessages
     * @see loadingOlderMessages
     */
    public object Loading : MessagesState()

    /** If we are offline and don't have channels stored in offline storage, typically displayed as an error condition. */
    public object OfflineNoResults : MessagesState()

    /** The list of messages, loaded either from offline storage or an API call.
     * Observe chatDomain.online to know if results are currently up to date
     * @see ChatDomainImpl.online
     */
    public data class Result(val messages: List<Message>) : MessagesState()
}
