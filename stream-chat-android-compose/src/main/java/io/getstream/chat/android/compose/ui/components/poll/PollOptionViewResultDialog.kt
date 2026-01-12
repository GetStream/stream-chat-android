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

package io.getstream.chat.android.compose.ui.components.poll

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.handlers.LoadMoreHandler
import io.getstream.chat.android.compose.ui.components.ContentBox
import io.getstream.chat.android.compose.ui.components.LoadingIndicator
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.ViewModelStore
import io.getstream.chat.android.compose.viewmodel.messages.PollOptionResultsViewModel
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.previewdata.PreviewPollData
import io.getstream.chat.android.ui.common.feature.messages.poll.PollOptionResultsViewAction
import io.getstream.chat.android.ui.common.feature.messages.poll.PollOptionResultsViewEvent
import io.getstream.chat.android.ui.common.state.messages.poll.PollOptionResultsViewState
import io.getstream.chat.android.ui.common.utils.extensions.initials
import kotlinx.coroutines.flow.collectLatest

/**
 * A dialog that displays all votes for a specific poll option.
 *
 * Shows a paginated list of all users who voted for the selected option, including their
 * avatars, names, and vote timestamps. Supports loading more votes as the user scrolls.
 * Displays loading states and error messages when vote data cannot be loaded.
 *
 * @param poll The poll containing the option for which results are displayed.
 * @param option The specific poll option for which vote results are displayed.
 * @param onDismissRequest Handler invoked when the dialog should be dismissed.
 * @param onBackPressed Handler invoked when the back button is pressed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PollOptionViewResultDialog(
    poll: Poll,
    option: Option,
    onDismissRequest: () -> Unit,
    onBackPressed: () -> Unit,
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        sheetMaxWidth = Dp.Unspecified,
        shape = RoundedCornerShape(0.dp),
        dragHandle = {},
        containerColor = ChatTheme.colors.barsBackground,
    ) {
        ViewModelStore {
            val viewModel = viewModel {
                PollOptionResultsViewModel(
                    poll = poll,
                    option = option,
                )
            }
            val state by viewModel.state.collectAsState()

            Content(
                state = state,
                onLoadMoreRequested = { viewModel.onViewAction(PollOptionResultsViewAction.LoadMoreRequested) },
                onBackPressed = onBackPressed,
            )

            LaunchedEffect(viewModel) {
                viewModel.events.collectLatest { event ->
                    when (event) {
                        is PollOptionResultsViewEvent.LoadError -> {
                            val errorMessage = context.getString(R.string.stream_compose_poll_option_results_error)
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Content(
    state: PollOptionResultsViewState,
    onLoadMoreRequested: () -> Unit = {},
    onBackPressed: () -> Unit = {},
) {
    val listState = rememberLazyListState()

    BackHandler(onBack = onBackPressed)

    LoadMoreHandler(
        lazyListState = listState,
        loadMore = onLoadMoreRequested,
    )

    Column(
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxSize(),
    ) {
        PollDialogHeader(
            title = state.option.text,
            onBackPressed = onBackPressed,
        )

        ContentBox(
            isLoading = state.isLoading,
            isEmpty = state.results.isEmpty(),
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clip(shape = ChatTheme.shapes.pollOptionInput)
                    .background(ChatTheme.colors.inputBackground)
                    .padding(16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        if (state.isWinner) {
                            Icon(
                                modifier = Modifier.padding(end = 8.dp),
                                painter = painterResource(id = R.drawable.stream_compose_ic_award),
                                tint = ChatTheme.colors.textHighEmphasis,
                                contentDescription = null,
                            )
                        }
                        Text(
                            text = stringResource(id = R.string.stream_compose_poll_vote_counts, state.voteCount),
                            color = ChatTheme.colors.textHighEmphasis,
                            fontSize = 16.sp,
                        )
                    }
                }

                items(
                    items = state.results,
                    key = { item -> item.id },
                ) { item ->
                    PollVoteItem(item)
                }

                if (state.isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            LoadingIndicator(
                                modifier = Modifier
                                    .size(16.dp)
                                    .align(Alignment.Center),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PollVoteItem(vote: Vote) {
    val user = vote.user ?: return

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ChatTheme.componentFactory.Avatar(
            modifier = Modifier.size(20.dp),
            imageUrl = user.image,
            initials = user.initials,
            shape = ChatTheme.shapes.avatar,
            textStyle = ChatTheme.typography.captionBold,
            placeholderPainter = null,
            errorPlaceholderPainter = null,
            contentDescription = user.name,
            initialsAvatarOffset = DpOffset.Zero,
            onClick = null,
        )

        Text(
            modifier = Modifier.weight(1f),
            text = user.name,
            color = ChatTheme.colors.textHighEmphasis,
            style = ChatTheme.typography.body,
        )

        Text(
            text = ChatTheme.dateFormatter.formatRelativeDate(vote.createdAt),
            color = ChatTheme.colors.textLowEmphasis,
            style = ChatTheme.typography.bodyBold,
        )

        Text(
            text = ChatTheme.dateFormatter.formatTime(vote.createdAt),
            color = ChatTheme.colors.textLowEmphasis,
            style = ChatTheme.typography.body,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PollOptionResultsLoadingPreview() {
    ChatTheme {
        PollOptionResultsLoading()
    }
}

@Composable
internal fun PollOptionResultsLoading() {
    val poll = PreviewPollData.poll1
    val option = poll.options.first()
    Content(
        state = PollOptionResultsViewState(
            option = option,
            voteCount = poll.voteCountsByOption[option.id] ?: 0,
            isWinner = true,
            isLoading = true,
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun PollOptionResultsContentPreview() {
    ChatTheme {
        PollOptionResultsContent()
    }
}

@Composable
internal fun PollOptionResultsContent() {
    val poll = PreviewPollData.poll1
    val option = poll.options.first()
    Content(
        state = PollOptionResultsViewState(
            option = option,
            voteCount = poll.voteCountsByOption[option.id] ?: 0,
            isWinner = true,
            isLoading = false,
            results = poll.getVotes(option),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun PollOptionResultsLoadingMorePreview() {
    ChatTheme {
        PollOptionResultsLoadingMore()
    }
}

@Composable
internal fun PollOptionResultsLoadingMore() {
    val poll = PreviewPollData.poll1
    val option = poll.options.first()
    Content(
        state = PollOptionResultsViewState(
            option = option,
            voteCount = poll.voteCountsByOption[option.id] ?: 0,
            isWinner = true,
            isLoading = false,
            results = poll.getVotes(option),
            isLoadingMore = true,
        ),
    )
}
