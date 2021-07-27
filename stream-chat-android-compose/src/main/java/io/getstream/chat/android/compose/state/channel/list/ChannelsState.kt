package io.getstream.chat.android.compose.state.channel.list

import io.getstream.chat.android.client.models.Channel

/**
 * Represents the Channels screen state, used to render the required UI.
 *
 * @param isLoading - If we're currently loading data (initial load).
 * @param isLoadingMore - If we're loading more items (pagination).
 * @param endOfChannels - If we've reached the end of channels, to stop triggering pagination.
 * @param channels - The channels to render.
 * */
public data class ChannelsState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val endOfChannels: Boolean = false,
    val channels: List<Channel> = emptyList(),
)
