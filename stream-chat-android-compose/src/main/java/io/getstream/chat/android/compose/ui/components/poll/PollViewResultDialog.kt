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

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.client.extensions.internal.getVotesUnlessAnonymous
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.R.plurals.stream_compose_poll_vote_counts
import io.getstream.chat.android.compose.ui.components.button.StreamButtonStyleDefaults
import io.getstream.chat.android.compose.ui.components.button.StreamTextButton
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.ViewModelStore
import io.getstream.chat.android.compose.viewmodel.messages.PollResultsViewModel
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.previewdata.PreviewPollData
import io.getstream.chat.android.ui.common.state.messages.poll.PollResultsViewState
import io.getstream.chat.android.ui.common.state.messages.poll.PollResultsViewState.ResultItem
import io.getstream.chat.android.ui.common.state.messages.poll.SelectedPoll

/**
 * A dialog that displays poll results for all options in a poll.
 *
 * Shows each poll option with its vote count, winner indicator (if applicable), and a preview
 * of voters. Users can tap "Show All" on any option to navigate to [PollOptionVotesDialog]
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
        shape = RectangleShape,
        dragHandle = {},
        containerColor = ChatTheme.colors.backgroundCoreApp,
    ) {
        var showAllOptionVotes by rememberSaveable(stateSaver = NullableOptionSaver) { mutableStateOf(null) }

        ViewModelStore {
            Crossfade(
                modifier = Modifier.fillMaxSize(),
                targetState = showAllOptionVotes,
            ) { option ->
                when (option) {
                    null -> {
                        val viewModel = viewModel { PollResultsViewModel(selectedPoll.poll) }
                        val state by viewModel.state.collectAsState()
                        Content(
                            state = state,
                            onBackPressed = onBackPressed,
                            onShowAllClick = { option -> showAllOptionVotes = option },
                        )
                    }

                    else -> {
                        PollOptionVotesDialog(
                            poll = selectedPoll.poll,
                            option = option,
                            onDismissRequest = { showAllOptionVotes = null },
                            onBackPressed = { showAllOptionVotes = null },
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

    Column(
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxSize(),
    ) {
        PollDialogHeader(
            title = stringResource(id = R.string.stream_compose_poll_results),
            onBackPressed = onBackPressed,
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = StreamTokens.spacingMd),
        ) {
            item {
                PollViewResultTitle(
                    title = state.pollName,
                    modifier = Modifier.padding(bottom = StreamTokens.spacing2xl),
                )
            }

            itemsIndexed(
                items = state.results,
                key = { _, item -> item.option.id },
            ) { index, item ->
                PollViewResultItem(
                    item = item,
                    index = index,
                    onShowAllClick = onShowAllClick,
                )
            }

            item {
                val totalVotes = state.results.sumOf(ResultItem::voteCount)
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = StreamTokens.spacing2xl),
                    text = pluralStringResource(
                        R.plurals.stream_compose_poll_total_vote_counts,
                        totalVotes,
                        totalVotes,
                    ),
                    color = ChatTheme.colors.textPrimary,
                    style = ChatTheme.typography.bodyDefault,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun PollViewResultItem(
    item: ResultItem,
    index: Int,
    onShowAllClick: (option: Option) -> Unit,
) {
    PollResultSection(modifier = Modifier.padding(top = StreamTokens.spacingMd)) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = StreamTokens.spacing2xs),
            text = stringResource(R.string.stream_compose_poll_option_label, index + 1),
            color = ChatTheme.colors.textTertiary,
            style = ChatTheme.typography.headingExtraSmall,
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                text = item.option.text,
                style = ChatTheme.typography.headingMedium,
                color = ChatTheme.colors.textPrimary,
            )

            if (item.isWinner) {
                Icon(
                    modifier = Modifier.padding(end = StreamTokens.spacingXs),
                    painter = painterResource(id = R.drawable.stream_compose_ic_trophy),
                    tint = ChatTheme.colors.textPrimary,
                    contentDescription = null,
                )
            }

            Text(
                text = pluralStringResource(stream_compose_poll_vote_counts, item.voteCount, item.voteCount),
                color = ChatTheme.colors.textPrimary,
                style = ChatTheme.typography.bodyEmphasis,
            )
        }

        item.votes.forEach { vote ->
            PollVoteItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = StreamTokens.spacingMd),
                vote = vote,
            )
        }

        if (item.showAllButton) {
            StreamTextButton(
                onClick = { onShowAllClick(item.option) },
                style = StreamButtonStyleDefaults.secondaryOutline,
                text = stringResource(R.string.stream_compose_poll_view_more),
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}

@Composable
private fun PollViewResultTitle(
    title: String,
    modifier: Modifier,
) {
    PollResultSection(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.stream_compose_poll_question_label),
            color = ChatTheme.colors.textTertiary,
            style = ChatTheme.typography.headingExtraSmall,
        )

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = title,
            style = ChatTheme.typography.headingMedium,
            color = ChatTheme.colors.textPrimary,
        )
    }
}

@Composable
private fun PollResultSection(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(StreamTokens.radiusXl))
            .background(ChatTheme.colors.backgroundCoreSurfaceSubtle)
            .padding(StreamTokens.spacingMd),
        verticalArrangement = verticalArrangement,
        content = content,
    )
}

private val NullableOptionSaver: Saver<Option?, Bundle> = Saver(
    save = { option ->
        option?.let {
            Bundle().apply {
                putString("id", it.id)
                putString("text", it.text)
            }
        }
    },
    restore = { bundle ->
        val id = bundle.getString("id") ?: return@Saver null
        val text = bundle.getString("text") ?: return@Saver null
        Option(id = id, text = text)
    },
)

@Preview
@Composable
private fun PollResultsContentPreview() {
    ChatTheme {
        Box(Modifier.background(ChatTheme.colors.backgroundCoreApp)) {
            PollResultsContent()
        }
    }
}

@Composable
internal fun PollResultsContent() {
    val poll = PreviewPollData.poll1
    Content(
        state = PollResultsViewState(
            pollName = poll.name,
            results = poll.options.mapIndexed { index, option ->
                ResultItem(
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
