package io.getstream.chat.android.compose.ui.channel.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.state.channel.list.ChannelsState
import io.getstream.chat.android.compose.ui.common.EmptyView
import io.getstream.chat.android.compose.ui.common.LoadingFooter
import io.getstream.chat.android.compose.ui.common.LoadingView
import io.getstream.chat.android.compose.viewmodel.channel.ChannelListViewModel
import io.getstream.chat.android.compose.viewmodel.channel.ChannelViewModelFactory
import io.getstream.chat.android.offline.ChatDomain

/**
 * Default ChannelList component, that relies on the [ChannelListViewModel] to load the data and
 * show it on the UI.
 *
 * @param modifier - Modifier for styling.
 * @param viewModel - The ViewModel that loads all the data and connects it to the UI. We provide a
 * factory that builds the default ViewModel in case the user doesn't want to provide their own.
 * @param onLastItemReached - Handler for pagination, when the user reaches the last item in the list.
 * @param onChannelClick - Handler for a single item tap.
 * @param onChannelLongClick - Handler for a long item tap.
 * @param itemContent - UI lambda function that allows the user to completely customize the item UI.
 * It shows [DefaultChannelItem] if left unchanged, with the actions provided by [onChannelClick] and
 * [onChannelLongClick].
 * */
@Composable
public fun ChannelList(
    modifier: Modifier = Modifier,
    viewModel: ChannelListViewModel = viewModel(
        factory =
        ChannelViewModelFactory(
            ChatClient.instance(),
            ChatDomain.instance(),
            QuerySort.desc("id"),
            Filters.and(
                Filters.eq("type", "messaging"),
                Filters.`in`("members", listOf(ChatClient.instance().getCurrentUser()?.id ?: ""))
            )
        )
    ),
    onLastItemReached: () -> Unit = { viewModel.loadMore() },
    onChannelClick: (Channel) -> Unit = {},
    onChannelLongClick: (Channel) -> Unit = { viewModel.onChannelSelected(it) },
    itemContent: @Composable (Channel) -> Unit = {
        DefaultChannelItem(
            item = it,
            viewModel.user.value,
            onChannelClick = onChannelClick,
            onChannelLongClick = onChannelLongClick
        )
    },
) {

    LaunchedEffect(Unit) {
        viewModel.start()
    }

    ChannelList(
        modifier = modifier,
        channelsState = viewModel.channelsState,
        currentUser = viewModel.user.value,
        onLastItemReached = onLastItemReached,
        onChannelClick = onChannelClick,
        onChannelLongClick = onChannelLongClick,
        itemContent = itemContent
    )
}

/**
 * Root Channel list component, that represents different UI, based on the current channel state.
 *
 * This is decoupled from ViewModels, so the user can provide manual and custom data handling,
 * as well as define a completely custom UI component for the channel item.
 *
 * If there is no state, no query active or the data is being loaded, we show the [LoadingView].
 *
 * If there are no results or we're offline, usually due to an error in the API or network, we show an [EmptyView].
 *
 * If there is data available and it is not empty, we show [Channels].
 *
 * @param modifier - Modifier for styling.
 * @param currentUser - The data of the current user, used various states.
 * @param channelsState - Current state of the Channel list, represented by [ChannelsState].
 * @param onLastItemReached - Handler for pagination, when the user reaches the end of the list.
 * @param onChannelClick - Handler for a single item tap.
 * @param onChannelLongClick - Handler for a long item tap.
 * @param itemContent - UI lambda function that allows the user to completely customize the item UI.
 * It shows [DefaultChannelItem] if left unchanged, with the actions provided by [onChannelClick] and
 * [onChannelLongClick].
 * */
@Composable
public fun ChannelList(
    channelsState: ChannelsState,
    currentUser: User?,
    modifier: Modifier = Modifier,
    onLastItemReached: () -> Unit = {},
    onChannelClick: (Channel) -> Unit = {},
    onChannelLongClick: (Channel) -> Unit = {},
    itemContent: @Composable (Channel) -> Unit = {
        DefaultChannelItem(
            item = it,
            currentUser = currentUser,
            onChannelClick = onChannelClick,
            onChannelLongClick = onChannelLongClick
        )
    },
) {
    val (isLoading, _, _, channels) = channelsState

    when {
        isLoading -> LoadingView(Modifier.fillMaxSize())
        !isLoading && channels.isNotEmpty() -> Channels(
            modifier = modifier,
            channelsState = channelsState,
            onLastItemReached = onLastItemReached,
            itemContent = itemContent
        )
        else -> EmptyView(modifier = Modifier.fillMaxSize())
    }
}

/**
 * Builds a list of [DefaultChannelItem] elements, based on [channelsState] and action handlers that it receives.
 *
 * @param channelsState - exposes if we're loading more items, reaches the end of the list and the
 * current list of channels to show.
 * @param onLastItemReached - Handler for when the user reaches the end of the list.
 * @param itemContent - Customizable UI component, that represents each item in the list.
 * @param modifier - Modifier for styling.
 * */
@Composable
private fun Channels(
    channelsState: ChannelsState,
    onLastItemReached: () -> Unit,
    itemContent: @Composable (Channel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val (_, isLoadingMore, endOfChannels, channels) = channelsState
    val state = rememberLazyListState()

    LazyColumn(
        modifier,
        state = state,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        itemsIndexed(channels) { index, item ->
            itemContent(item)

            if (!endOfChannels && channels.isNotEmpty() && state.isScrollInProgress && index == channels.lastIndex) {
                onLastItemReached()
            }
        }

        if (isLoadingMore) {
            item { LoadingFooter() }
        }
    }
}
