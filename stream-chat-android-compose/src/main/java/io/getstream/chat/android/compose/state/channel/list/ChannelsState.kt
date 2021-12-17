package io.getstream.chat.android.compose.state.channel.list

/**
 * Represents the Channels screen state, used to render the required UI.
 *
 * @param isLoading If we're currently loading data (initial load).
 * @param isLoadingMore If we're loading more items (pagination).
 * @param endOfChannels If we've reached the end of channels, to stop triggering pagination.
 * @param channelItems The channel items to represent in the list.
 * @param searchQuery The current search query.
 */
public data class ChannelsState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val endOfChannels: Boolean = false,
    val channelItems: List<ChannelItemState> = emptyList(),
    val searchQuery: String = "",
)
