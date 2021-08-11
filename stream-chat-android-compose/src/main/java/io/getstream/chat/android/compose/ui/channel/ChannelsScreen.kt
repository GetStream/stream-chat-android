package io.getstream.chat.android.compose.ui.channel

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.handlers.SystemBackPressedHandler
import io.getstream.chat.android.compose.state.channel.list.DeleteConversation
import io.getstream.chat.android.compose.state.channel.list.LeaveGroup
import io.getstream.chat.android.compose.state.channel.list.ViewInfo
import io.getstream.chat.android.compose.ui.channel.header.ChannelListHeader
import io.getstream.chat.android.compose.ui.channel.info.ChannelInfo
import io.getstream.chat.android.compose.ui.channel.list.ChannelList
import io.getstream.chat.android.compose.ui.common.SearchInput
import io.getstream.chat.android.compose.ui.common.SimpleDialog
import io.getstream.chat.android.compose.viewmodel.channel.ChannelListViewModel
import io.getstream.chat.android.compose.viewmodel.channel.ChannelViewModelFactory
import io.getstream.chat.android.offline.ChatDomain

/**
 * Default root Channel screen component, that provides the necessary ViewModel.
 *
 * It can be used without most parameters for default behavior, that can be tweaked if necessary.
 *
 * @param filters - Default filters for channels.
 * @param querySort - Default query sort for channels.
 * @param title - Header title.
 * @param isShowingHeader - If we show the header or hide it.
 * @param isShowingSearch - If we show the search input or hide it.
 * @param onHeaderClickAction - Handler for the default header action.
 * @param onItemClick - Handler for Channel item clicks.
 * @param onBackPressed - Handler for back press action.
 * */
@Composable
public fun ChannelsScreen(
    filters: FilterObject = Filters.and(
        Filters.eq("type", "messaging"),
        Filters.`in`("members", listOf(ChatClient.instance().getCurrentUser()?.id ?: ""))
    ),
    querySort: QuerySort<Channel> = QuerySort.desc("last_updated"),
    title: String = "Stream Chat",
    isShowingHeader: Boolean = true,
    isShowingSearch: Boolean = true,
    onHeaderClickAction: () -> Unit = {},
    onItemClick: (Channel) -> Unit = {},
    onViewChannelInfoAction: (Channel) -> Unit = {},
    onBackPressed: () -> Unit = {},
) {
    val listViewModel: ChannelListViewModel = viewModel(
        ChannelListViewModel::class.java,
        factory = ChannelViewModelFactory(
            ChatClient.instance(),
            ChatDomain.instance(),
            querySort,
            filters
        )
    )

    val selectedChannel = listViewModel.selectedChannel
    val user by listViewModel.user.collectAsState()
    val isNetworkAvailable by listViewModel.isOnline.collectAsState()

    SystemBackPressedHandler(isEnabled = true) {
        if (selectedChannel != null) {
            listViewModel.selectChannel(null)
        } else {
            onBackPressed()
        }
    }

    var searchQuery by rememberSaveable { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {

            if (isShowingHeader) {
                ChannelListHeader(
                    onHeaderActionClick = onHeaderClickAction,
                    currentUser = user,
                    title = title,
                    isNetworkAvailable = isNetworkAvailable
                )
            }

            if (isShowingSearch) {
                SearchInput(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    query = searchQuery,
                    onSearchStarted = {},
                    onValueChange = {
                        searchQuery = it
                        listViewModel.setSearchQuery(it)
                    },
                )
            }

            ChannelList(
                modifier = Modifier
                    .fillMaxSize(),
                viewModel = listViewModel,
                onChannelClick = onItemClick,
                onChannelLongClick = {
                    listViewModel.selectChannel(it)
                }
            )
        }

        if (selectedChannel != null) {
            ChannelInfo(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.BottomCenter),
                selectedChannel = selectedChannel,
                user = user,
                onChannelOptionClick = { action ->
                    when (action) {
                        is ViewInfo -> onViewChannelInfoAction(action.channel)
                        else -> listViewModel.performChannelAction(action)
                    }
                }
            )
        }

        val activeAction = listViewModel.activeChannelAction

        if (activeAction is LeaveGroup) {
            SimpleDialog(
                modifier = Modifier.padding(16.dp),
                title = stringResource(id = R.string.stream_compose_leave_group),
                message = stringResource(
                    id = R.string.stream_compose_leave_group_text,
                    activeAction.channel.id
                ),
                onPositiveAction = { listViewModel.leaveGroup(activeAction.channel) },
                onDismiss = { listViewModel.dismissChannelAction() }
            )
        } else if (activeAction is DeleteConversation) {
            SimpleDialog(
                modifier = Modifier.padding(16.dp),
                title = stringResource(id = R.string.stream_compose_delete_conversation),
                message = stringResource(
                    id = R.string.stream_compose_delete_conversation_text,
                    activeAction.channel.id
                ),
                onPositiveAction = { listViewModel.deleteConversation(activeAction.channel) },
                onDismiss = { listViewModel.dismissChannelAction() }
            )
        }
    }
}
