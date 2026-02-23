/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.threads

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.handlers.LoadMoreHandler
import io.getstream.chat.android.compose.ui.components.LoadingIndicator
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.threads.ThreadListViewModel
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.state.threads.ThreadListState

/**
 * Composable rendering a paginated list of threads.
 * Optionally, it renders a banner informing about new threads/thread messages outside of the loaded pages of threads.
 *
 * @param viewModel The [ThreadListViewModel] handling the loading of the threads.
 * @param modifier [Modifier] instance for general styling.
 * @param currentUser The currently logged [User], used for formatting the message in the thread preview.
 * @param onUnreadThreadsBannerClick Action invoked when the user clicks on the "Unread threads" banner. By default, it
 * calls [ThreadListViewModel.load] to force reload the list of threads, loading the newly created/updated threads.
 * @param onThreadClick Action invoked when the usr clicks on a thread item in the list. No-op by default.
 * @param onLoadMore Action invoked when the current thread page was scrolled to the end, and a next page should be
 * loaded. By default, it calls [ThreadListViewModel.loadNextPage] to load the next page of threads.
 * @param unreadThreadsBanner Composable rendering the "Unread threads" banner on the top of the list. Override it to
 * provide a custom component to be rendered for displaying the number of new unread threads.
 * @param itemContent Composable rendering each [Thread] item in the list. Override this to provide a custom component
 * for rendering the items.
 * @param emptyContent Composable shown when there are no threads to display. Override this to provide custom component
 * for rendering the empty state.
 * @param loadingContent Composable shown during the initial loading of the threads. Override this to provide a custom
 * initial loading state.
 * @param loadingMoreContent Composable shown at the bottom of the list during the loading of more threads (pagination).
 * Override this to provide a custom loading component shown during the loading of more items.
 */
@Composable
public fun ThreadList(
    viewModel: ThreadListViewModel,
    modifier: Modifier = Modifier,
    currentUser: User? = ChatClient.instance().getCurrentUser(),
    onUnreadThreadsBannerClick: () -> Unit = { viewModel.load() },
    onThreadClick: (Thread) -> Unit = {},
    onLoadMore: () -> Unit = { viewModel.loadNextPage() },
    unreadThreadsBanner: @Composable (Int) -> Unit = {
        ChatTheme.componentFactory.ThreadListUnreadThreadsBanner(it, onUnreadThreadsBannerClick)
    },
    itemContent: @Composable (Thread) -> Unit = {
        ChatTheme.componentFactory.ThreadListItem(it, currentUser, onThreadClick)
    },
    emptyContent: @Composable () -> Unit = {
        ChatTheme.componentFactory.ThreadListEmptyContent(modifier)
    },
    loadingContent: @Composable () -> Unit = {
        ChatTheme.componentFactory.ThreadListLoadingContent(modifier)
    },
    loadingMoreContent: @Composable () -> Unit = {
        ChatTheme.componentFactory.ThreadListLoadingMoreContent()
    },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ThreadList(
        state = state,
        modifier = modifier,
        currentUser = currentUser,
        onUnreadThreadsBannerClick = onUnreadThreadsBannerClick,
        onThreadClick = onThreadClick,
        onLoadMore = onLoadMore,
        unreadThreadsBanner = unreadThreadsBanner,
        itemContent = itemContent,
        emptyContent = emptyContent,
        loadingContent = loadingContent,
        loadingMoreContent = loadingMoreContent,
    )
}

/**
 * Composable rendering a paginated list of threads.
 * Optionally, it renders a banner informing about new threads/thread messages outside of the loaded pages of threads.
 *
 * @param state The [ThreadListState] holding the current thread list state.
 * @param modifier [Modifier] instance for general styling.
 * @param currentUser The currently logged [User], used for formatting the message in the thread preview.
 * @param onUnreadThreadsBannerClick Action invoked when the user clicks on the "Unread threads" banner.
 * @param onThreadClick Action invoked when the usr clicks on a thread item in the list.
 * @param onLoadMore Action invoked when the current thread page was scrolled to the end, and a next page should be
 * loaded.
 * @param unreadThreadsBanner Composable rendering the "Unread threads" banner on the top of the list. Override it to
 * provide a custom component to be rendered for displaying the number of new unread threads.
 * @param itemContent Composable rendering each [Thread] item in the list. Override this to provide a custom component
 * for rendering the items.
 * @param emptyContent Composable shown when there are no threads to display. Override this to provide custom component
 * for rendering the empty state.
 * @param loadingContent Composable shown during the initial loading of the threads. Override this to provide a custom
 * initial loading state.
 * @param loadingMoreContent Composable shown at the bottom of the list during the loading of more threads (pagination).
 * Override this to provide a custom loading component shown during the loading of more items.
 */
@Composable
public fun ThreadList(
    state: ThreadListState,
    modifier: Modifier = Modifier,
    currentUser: User? = ChatClient.instance().getCurrentUser(),
    onUnreadThreadsBannerClick: () -> Unit,
    onThreadClick: (Thread) -> Unit,
    onLoadMore: () -> Unit,
    unreadThreadsBanner: @Composable (Int) -> Unit = {
        ChatTheme.componentFactory.ThreadListUnreadThreadsBanner(it, onUnreadThreadsBannerClick)
    },
    itemContent: @Composable (Thread) -> Unit = {
        ChatTheme.componentFactory.ThreadListItem(it, currentUser, onThreadClick)
    },
    emptyContent: @Composable () -> Unit = {
        ChatTheme.componentFactory.ThreadListEmptyContent(modifier)
    },
    loadingContent: @Composable () -> Unit = {
        ChatTheme.componentFactory.ThreadListLoadingContent(modifier)
    },
    loadingMoreContent: @Composable () -> Unit = {
        ChatTheme.componentFactory.ThreadListLoadingMoreContent()
    },
) {
    Scaffold(
        containerColor = ChatTheme.colors.appBackground,
        topBar = {
            unreadThreadsBanner(state.unseenThreadsCount)
        },
        content = { padding ->
            Box(modifier = modifier.padding(padding)) {
                when {
                    state.threads.isEmpty() && state.isLoading -> loadingContent()
                    state.threads.isEmpty() -> emptyContent()
                    else -> Threads(
                        threads = state.threads,
                        isLoadingMore = state.isLoadingMore,
                        modifier = modifier,
                        onLoadMore = onLoadMore,
                        itemContent = itemContent,
                        loadingMoreContent = loadingMoreContent,
                    )
                }
            }
        },
    )
}

/**
 * Composable representing a non-empty list of threads.
 *
 * @param threads The non-empty [List] of [Thread]s to show.
 * @param isLoadingMore Indicator if there is loading of the next page of threads in progress.
 * @param modifier [Modifier] instance for general styling.
 * @param onLoadMore Action invoked when the current thread page was scrolled to the end, and a next page should be
 * loaded.
 * @param itemContent Composable rendering each [Thread] item in the list.
 * @param loadingMoreContent Composable shown at the bottom of the list during the loading of more threads (pagination).
 */
@Suppress("LongParameterList")
@Composable
private fun Threads(
    threads: List<Thread>,
    isLoadingMore: Boolean,
    modifier: Modifier,
    onLoadMore: () -> Unit,
    itemContent: @Composable (Thread) -> Unit,
    loadingMoreContent: @Composable () -> Unit,
) {
    val listState = rememberLazyListState()
    Box(modifier = modifier) {
        LazyColumn(state = listState) {
            items(
                items = threads,
                key = Thread::parentMessageId,
            ) { thread ->
                itemContent(thread)
            }
            if (isLoadingMore) {
                item {
                    loadingMoreContent()
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
 * The default empty placeholder that is displayed when there are no threads.
 *
 * @param modifier Modifier for styling.
 */
@Composable
internal fun DefaultThreadListEmptyContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            modifier = Modifier.size(112.dp),
            painter = painterResource(R.drawable.stream_compose_ic_threads_empty),
            contentDescription = null,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.stream_compose_thread_list_empty_title),
            textAlign = TextAlign.Center,
            color = ChatTheme.colors.textSecondary,
            fontSize = 20.sp,
            lineHeight = 25.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * The default loading content that is displayed during the initial loading of the threads.
 *
 * @param modifier Modifier for styling.
 */
@Composable
internal fun DefaultThreadListLoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(ChatTheme.colors.appBackground)) {
        LoadingIndicator(modifier)
    }
}

/**
 * The default content shown on the bottom of the list during the loading of more threads.
 */
@Composable
internal fun DefaultThreadListLoadingMoreContent() {
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

@Preview
@Composable
private fun DefaultThreadListEmptyContentPreview() {
    ChatTheme {
        Surface {
            DefaultThreadListEmptyContent()
        }
    }
}

@Preview
@Composable
private fun DefaultThreadListLoadingContentPreview() {
    ChatTheme {
        Surface {
            DefaultThreadListLoadingContent()
        }
    }
}

@Preview
@Composable
private fun DefaultThreadListLoadingMoreContentPreview() {
    ChatTheme {
        Surface {
            DefaultThreadListLoadingMoreContent()
        }
    }
}
