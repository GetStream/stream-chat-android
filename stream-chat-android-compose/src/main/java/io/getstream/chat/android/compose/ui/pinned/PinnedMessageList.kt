/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.compose.ui.pinned

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.handlers.LoadMoreHandler
import io.getstream.chat.android.compose.ui.components.LoadingIndicator
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.parseBoldTags
import io.getstream.chat.android.compose.viewmodel.pinned.PinnedMessageListViewModel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewPinnedMessageData
import io.getstream.chat.android.ui.common.state.pinned.PinnedMessageListState
import kotlinx.coroutines.flow.collectLatest

/**
 * Default 'Pinned Messages List' component, which relies on [PinnedMessageListViewModel] to show and allow interactions
 * with the pinned messages from a given channel.
 *
 * @param viewModel The [PinnedMessageListViewModel] holding the data and the business logic for showing the pinned
 * messages from a given channel.
 * @param modifier [Modifier] instance for general styling.
 * @param currentUser The currently logged [User], used for formatting the message preview.
 * @param onPinnedMessageClick Action to be invoked when the user clicks on a message from the list.
 * @param itemContent Composable rendering each [Message] item in the list. Override this to provide custom component
 * for rendering the items.
 * @param itemDivider Composable rendering the divider between messages. Override this to provide (or remove) the
 * default divider.
 * @param emptyContent Composable shown when there are no pinned messages to display. Override this to provide custom
 * component for rendering the empty state.
 * @param loadingContent Composable shown during the initial loading of the pinned messages. Override this to provide a
 * custom initial loading state.
 * @param loadingMoreContent Composable shown at the bottom of the list during the loading of more pinned messages
 * (pagination). Override this to provide a custom loading component shown during the loading of more items.
 */
@Composable
public fun PinnedMessageList(
    viewModel: PinnedMessageListViewModel,
    modifier: Modifier = Modifier,
    currentUser: User? = ChatClient.instance().getCurrentUser(),
    onPinnedMessageClick: (Message) -> Unit = {},
    itemContent: @Composable (Message) -> Unit = {
        ChatTheme.componentFactory.PinnedMessageListItem(it, currentUser, onPinnedMessageClick)
    },
    itemDivider: @Composable (Int) -> Unit = {
        ChatTheme.componentFactory.PinnedMessageListItemDivider()
    },
    emptyContent: @Composable () -> Unit = {
        ChatTheme.componentFactory.PinnedMessageListEmptyContent(modifier)
    },
    loadingContent: @Composable () -> Unit = {
        ChatTheme.componentFactory.PinnedMessageListLoadingContent(modifier)
    },
    loadingMoreContent: @Composable () -> Unit = {
        ChatTheme.componentFactory.PinnedMessageListLoadingMoreContent()
    },
) {
    val state by viewModel.state.collectAsState()
    PinnedMessageList(
        state = state,
        modifier = modifier,
        currentUser = currentUser,
        onPinnedMessageClick = onPinnedMessageClick,
        onLoadMore = viewModel::loadMore,
        itemContent = itemContent,
        itemDivider = itemDivider,
        emptyContent = emptyContent,
        loadingContent = loadingContent,
        loadingMoreContent = loadingMoreContent,
    )

    // Error emissions
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.errorEvents.collectLatest {
            val errorMessage = context.getString(R.string.stream_compose_pinned_message_list_results_error)
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * Stateless 'Pinned Messages List' component whose content is rendered based on the provided [PinnedMessageListState].
 *
 * @param state The [PinnedMessageListState] instance holding the data to be displayed.
 * @param modifier [Modifier] instance for general styling.
 * @param currentUser The currently logged [User], used for formatting the message preview.
 * @param onPinnedMessageClick Action to be invoked when the user clicks on a message from the list.
 * @param onLoadMore Action to be invoked when the user scrolls to the end of the list and more pinned messages should
 * be loaded.
 * @param itemContent Composable rendering each [Message] item in the list. Override this to provide custom component
 * for rendering the items.
 * @param itemDivider Composable rendering the divider between messages. Override this to provide (or remove) the
 * default divider.
 * @param emptyContent Composable shown when there are no pinned messages to display. Override this to provide custom
 * component for rendering the empty state.
 * @param loadingContent Composable shown during the initial loading of the pinned messages. Override this to provide a
 * custom initial loading state.
 * @param loadingMoreContent Composable shown at the bottom of the list during the loading of more pinned messages
 * (pagination). Override this to provide a custom loading component shown during the loading of more items.
 */
@Composable
internal fun PinnedMessageList(
    state: PinnedMessageListState,
    modifier: Modifier = Modifier,
    currentUser: User? = ChatClient.instance().getCurrentUser(),
    onPinnedMessageClick: (Message) -> Unit,
    onLoadMore: () -> Unit,
    itemContent: @Composable (Message) -> Unit = {
        ChatTheme.componentFactory.PinnedMessageListItem(it, currentUser, onPinnedMessageClick)
    },
    itemDivider: @Composable (Int) -> Unit = {
        ChatTheme.componentFactory.PinnedMessageListItemDivider()
    },
    emptyContent: @Composable () -> Unit = {
        ChatTheme.componentFactory.PinnedMessageListEmptyContent(modifier)
    },
    loadingContent: @Composable () -> Unit = {
        ChatTheme.componentFactory.PinnedMessageListLoadingContent(modifier)
    },
    loadingMoreContent: @Composable () -> Unit = {
        ChatTheme.componentFactory.PinnedMessageListLoadingMoreContent()
    },
) {
    when {
        state.results.isEmpty() && state.isLoading -> loadingContent()
        state.results.isEmpty() && !state.isLoading -> emptyContent()
        else -> PinnedMessages(
            messages = state.results.map { it.message },
            modifier = modifier,
            itemContent = itemContent,
            itemDivider = itemDivider,
            loadingMoreContent = loadingMoreContent,
            onLoadMore = onLoadMore,
        )
    }
}

/**
 * Composable representing a non-empty list of pinned messages.
 *
 * @param messages The non-empty [List] of [Message]s to show.
 * @param modifier [Modifier] instance for general styling.
 * @param itemContent Composable rendering each [Message] item in the list.
 * @param itemDivider Composable rendering the divider between messages.
 * @param loadingMoreContent Composable shown at the bottom of the list during the loading of more pinned messages
 * (pagination).
 * @param onLoadMore Action executed when the scroll threshold was reached and a new page of messages should be loaded.
 */
@Suppress("LongParameterList")
@Composable
private fun PinnedMessages(
    messages: List<Message>,
    modifier: Modifier,
    itemContent: @Composable (Message) -> Unit,
    itemDivider: @Composable (Int) -> Unit,
    loadingMoreContent: @Composable () -> Unit,
    onLoadMore: () -> Unit,
) {
    val listState = rememberLazyListState()
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ChatTheme.colors.appBackground),
    ) {
        LazyColumn(state = listState) {
            itemsIndexed(messages) { index, pinnedMessage ->
                if (pinnedMessage.id.isNotEmpty()) {
                    itemContent(pinnedMessage)
                } else {
                    // Empty ID represents a 'loading more' item
                    loadingMoreContent()
                }
                if (index < messages.lastIndex) {
                    itemDivider(index)
                }
            }
        }
    }
    LoadMoreHandler(
        lazyListState = listState,
        loadMore = onLoadMore,
    )
}

/**
 * The default empty placeholder that is displayed when there are no pinned messages.
 *
 * @param modifier Modifier for styling.
 */
@Composable
internal fun DefaultPinnedMessageListEmptyContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.background(ChatTheme.colors.appBackground),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            modifier = Modifier.size(112.dp),
            painter = painterResource(R.drawable.stream_compose_ic_pinned_messages_empty),
            contentDescription = null,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.stream_compose_pinned_message_list_empty_title),
            textAlign = TextAlign.Center,
            style = ChatTheme.typography.captionBold,
            color = ChatTheme.colors.textHighEmphasis,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            text = stringResource(id = R.string.stream_compose_pinned_message_list_empty_description).parseBoldTags(),
            textAlign = TextAlign.Center,
            style = ChatTheme.typography.bodyBold,
            color = ChatTheme.colors.textLowEmphasis,
            fontSize = 16.sp,
        )
    }
}

/**
 * The default loading content that is displayed during the initial loading of the pinned messages.
 *
 * @param modifier Modifier for styling.
 */
@Composable
internal fun DefaultPinnedMessageListLoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(ChatTheme.colors.appBackground)) {
        LoadingIndicator(modifier)
    }
}

/**
 * The default content shown on the bottom of the list during the loading of more pinned messages.
 */
@Composable
internal fun DefaultPinnedMessageListLoadingMoreContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChatTheme.colors.appBackground)
            .padding(top = 8.dp, start = 8.dp, end = 8.dp, bottom = 40.dp),
        contentAlignment = Alignment.Center,
    ) {
        LoadingIndicator(modifier = Modifier.size(16.dp))
    }
}

@Composable
@Preview
private fun PinnedMessageItemPreview() {
    ChatTheme {
        Surface {
            PinnedMessageItem(
                message = PreviewPinnedMessageData.pinnedMessage1,
                currentUser = null,
                onPinnedMessageClick = {},
            )
        }
    }
}

@Composable
@Preview
private fun DefaultPinnedMessageListEmptyContentPreview() {
    ChatTheme {
        Surface {
            DefaultPinnedMessageListEmptyContent()
        }
    }
}

@Composable
@Preview
private fun DefaultPinnedMessageListLoadingContentPreview() {
    ChatTheme {
        Surface {
            DefaultPinnedMessageListLoadingContent()
        }
    }
}

@Composable
@Preview
private fun DefaultPinnedMessageListLoadingMoreContentPreview() {
    ChatTheme {
        Surface {
            DefaultPinnedMessageListLoadingMoreContent()
        }
    }
}
