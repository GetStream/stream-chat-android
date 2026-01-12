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

package io.getstream.chat.android.compose.ui.components.poll

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.client.extensions.internal.getVotesUnlessAnonymous
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.ViewModelStore
import io.getstream.chat.android.compose.viewmodel.messages.PollResultsViewModel
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.previewdata.PreviewPollData
import io.getstream.chat.android.ui.common.state.messages.poll.PollResultsViewState
import io.getstream.chat.android.ui.common.state.messages.poll.SelectedPoll
import io.getstream.chat.android.ui.common.utils.extensions.initials

/**
 * A dialog that displays poll results for all options in a poll.
 *
 * Shows each poll option with its vote count, winner indicator (if applicable), and a preview
 * of voters. Users can tap "Show All" on any option to navigate to [PollOptionViewResultDialog]
 * to see the complete list of votes for that option.
 *
 * @param selectedPoll The poll for which results are displayed, containing all poll state.
 * @param onDismissRequest Handler invoked when the dialog should be dismissed.
 * @param onBackPressed Handler invoked when the back button is pressed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun PollViewResultDialog(
    selectedPoll: SelectedPoll,
    onDismissRequest: () -> Unit,
    onBackPressed: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        sheetMaxWidth = Dp.Unspecified,
        shape = RoundedCornerShape(0.dp),
        dragHandle = {},
        containerColor = ChatTheme.colors.barsBackground,
    ) {
        var showAllOptionResults by rememberSaveable { mutableStateOf<Option?>(null) }

        ViewModelStore {
            Crossfade(
                modifier = Modifier.fillMaxSize(),
                targetState = showAllOptionResults,
            ) { showAll ->
                when (showAll) {
                    null -> {
                        val viewModel = viewModel { PollResultsViewModel(selectedPoll.poll) }
                        val state by viewModel.state.collectAsState()
                        Content(
                            state = state,
                            onBackPressed = onBackPressed,
                            onShowAllClick = { option -> showAllOptionResults = option },
                        )
                    }

                    else -> {
                        PollOptionViewResultDialog(
                            poll = selectedPoll.poll,
                            option = showAll,
                            onDismissRequest = { showAllOptionResults = null },
                            onBackPressed = { showAllOptionResults = null },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Content(
    state: PollResultsViewState,
    onBackPressed: () -> Unit = {},
    onShowAllClick: (option: Option) -> Unit = {},
) {
    BackHandler(onBack = onBackPressed)

    LazyColumn(
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxSize(),
    ) {
        item {
            PollDialogHeader(
                title = stringResource(id = R.string.stream_compose_poll_results),
                onBackPressed = onBackPressed,
            )
        }

        item {
            PollViewResultTitle(
                title = state.pollName,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }

        items(
            items = state.results,
            key = { item -> item.option.id },
        ) { item ->
            PollViewResultItem(
                item,
                onShowAllClick = onShowAllClick,
            )
        }
    }
}

@Composable
private fun PollViewResultItem(
    item: PollResultsViewState.ResultItem,
    onShowAllClick: (option: Option) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth()
            .clip(shape = ChatTheme.shapes.pollOptionInput)
            .background(ChatTheme.colors.inputBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                text = item.option.text,
                color = ChatTheme.colors.textHighEmphasis,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
            )

            if (item.isWinner) {
                Icon(
                    modifier = Modifier.padding(end = 8.dp),
                    painter = painterResource(id = R.drawable.stream_compose_ic_award),
                    tint = ChatTheme.colors.textHighEmphasis,
                    contentDescription = null,
                )
            }

            Text(
                text = stringResource(id = R.string.stream_compose_poll_vote_counts, item.voteCount),
                color = ChatTheme.colors.textHighEmphasis,
                fontSize = 16.sp,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        item.votes.forEach { vote ->
            PollVoteItem(vote = vote)
        }

        if (item.showAllButton) {
            TextButton(
                onClick = { onShowAllClick(item.option) },
                colors = ButtonDefaults.textButtonColors(contentColor = ChatTheme.colors.primaryAccent),
            ) {
                Text(text = stringResource(R.string.stream_compose_poll_show_all_votes))
            }
        }
    }
}

@Composable
private fun PollVoteItem(vote: Vote) {
    val user = vote.user ?: return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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

@Composable
private fun PollViewResultTitle(
    title: String,
    modifier: Modifier,
) {
    Box(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(shape = ChatTheme.shapes.pollOptionInput)
            .background(ChatTheme.colors.inputBackground)
            .padding(16.dp),
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterStart),
            text = title,
            color = ChatTheme.colors.textHighEmphasis,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PollResultsContentPreview() {
    ChatTheme {
        PollResultsContent()
    }
}

@Composable
internal fun PollResultsContent() {
    val poll = PreviewPollData.poll1
    Content(
        state = PollResultsViewState(
            pollName = poll.name,
            results = poll.options.mapIndexed { index, option ->
                PollResultsViewState.ResultItem(
                    option = option,
                    isWinner = option == poll.options.first(),
                    voteCount = poll.voteCountsByOption[option.id] ?: 0,
                    votes = poll.getVotesUnlessAnonymous(option),
                    showAllButton = index == 0,
                )
            },
        ),
    )
}
