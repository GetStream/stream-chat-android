package io.getstream.chat.android.compose.ui.channels.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.handlers.LoadMoreHandler
import io.getstream.chat.android.compose.state.channel.list.ChannelItemState
import io.getstream.chat.android.compose.state.channel.list.ChannelsState
import io.getstream.chat.android.compose.ui.components.LoadingFooter

/**
 * Builds a list of [ChannelItem] elements, based on [channelsState] and action handlers that it receives.
 *
 * @param channelsState Exposes if we're loading more items, reaches the end of the list and the
 * current list of channels to show.
 * @param onLastItemReached Handler for when the user reaches the end of the list.
 * @param modifier Modifier for styling.
 * @param itemContent Customizable UI component, that represents each item in the list.
 * @param divider Customizable UI component, that represents item dividers.
 */
@Composable
public fun Channels(
    channelsState: ChannelsState,
    onLastItemReached: () -> Unit,
    modifier: Modifier = Modifier,
    itemContent: @Composable (ChannelItemState) -> Unit,
    divider: @Composable () -> Unit,
) {
    val (_, isLoadingMore, endOfChannels, channelItems) = channelsState
    val listState = rememberLazyListState()

    LazyColumn(
        modifier = modifier,
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            DummyFirstChannelItem()
        }

        items(
            items = channelItems,
            key = { it.channel.cid }
        ) { item ->
            itemContent(item)

            divider()
        }

        if (isLoadingMore) {
            item {
                LoadingFooter(modifier = Modifier.fillMaxWidth())
            }
        }
    }

    if (!endOfChannels && channelItems.isNotEmpty()) {
        LoadMoreHandler(listState) {
            onLastItemReached()
        }
    }
}

/**
 * Represents an almost invisible dummy item to be added to the top of the list.
 *
 * If the list is scrolled to the top and a channel new item is added or moved
 * to the position above, then the list will automatically autoscroll to it.
 */
@Composable
private fun DummyFirstChannelItem() {
    Box(
        modifier = Modifier
            .height(1.dp)
            .fillMaxWidth()
    )
}
