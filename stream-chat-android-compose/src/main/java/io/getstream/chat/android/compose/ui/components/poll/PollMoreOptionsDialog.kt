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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.StreamScreenBottomSheet
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageStyling.PollStyle
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.previewdata.PreviewMessageData
import io.getstream.chat.android.previewdata.PreviewPollData
import io.getstream.chat.android.ui.common.state.messages.poll.PollSelectionType
import io.getstream.chat.android.ui.common.state.messages.poll.SelectedPoll

/**
 * A dialog that should be shown if a user taps the seeing more options on the poll message.
 *
 * @param selectedPoll The current poll that contains all the states.
 * @param listViewModel The [MessageListViewModel] used to read state from.
 * @param onDismissRequest Handler for dismissing the dialog.
 * @param onBackPressed Handler for pressing a back button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun PollMoreOptionsDialog(
    selectedPoll: SelectedPoll,
    listViewModel: MessageListViewModel,
    onDismissRequest: () -> Unit,
    onBackPressed: () -> Unit,
) {
    StreamScreenBottomSheet(onDismissRequest = onDismissRequest) {
        Content(
            selectedPoll = selectedPoll,
            onBackPressed = onBackPressed,
            onCastVote = { option ->
                listViewModel.castVote(
                    message = selectedPoll.message,
                    poll = selectedPoll.poll,
                    option = option,
                )
            },
            onRemoveVote = { vote ->
                listViewModel.removeVote(
                    message = selectedPoll.message,
                    poll = selectedPoll.poll,
                    vote = vote,
                )
            },
        )
    }
}

@Composable
private fun Content(
    selectedPoll: SelectedPoll,
    onBackPressed: () -> Unit = {},
    onCastVote: (Option) -> Unit = {},
    onRemoveVote: (Vote) -> Unit = {},
) {
    val poll = selectedPoll.poll

    BackHandler { onBackPressed.invoke() }

    Column(
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxSize()
            .background(ChatTheme.colors.backgroundCoreApp),
    ) {
        PollDialogHeader(
            title = stringResource(id = R.string.stream_compose_poll_options),
            onBackPressed = onBackPressed,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = StreamTokens.spacingMd),
        ) {
            PollMoreOptionsTitle(title = poll.name)

            val totalVoteCount = remember(poll.voteCountsByOption) { poll.voteCountsByOption.values.sum() }

            PollMoreOptionsItemList(
                poll = poll,
                totalVoteCount = totalVoteCount,
                onCastVote = onCastVote,
                onRemoveVote = onRemoveVote,
            )

            val context = LocalContext.current
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = StreamTokens.spacing2xl),
                text = remember(totalVoteCount) {
                    context.resources.getQuantityString(
                        R.plurals.stream_compose_poll_total_vote_counts,
                        totalVoteCount,
                        totalVoteCount,
                    )
                },
                style = ChatTheme.typography.bodyDefault,
                color = ChatTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
internal fun PollMoreOptionsTitle(title: String) {
    PollSection(
        modifier = Modifier
            .padding(vertical = StreamTokens.spacing2xl)
            .semantics(mergeDescendants = true) { heading() },
        verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
        contentPadding = PaddingValues(StreamTokens.spacingMd),
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
private fun PollMoreOptionsItemList(
    poll: Poll,
    totalVoteCount: Int,
    onCastVote: (Option) -> Unit,
    onRemoveVote: (Vote) -> Unit,
) {
    PollSection(
        contentPadding = PaddingValues(
            horizontal = StreamTokens.spacingXs,
            vertical = StreamTokens.spacingMd,
        ),
        verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
    ) {
        poll.options.forEach { option ->
            val voteCount = poll.voteCountsByOption[option.id] ?: 0
            val isVotedByMine = poll.ownVotes.any { it.optionId == option.id }
            val users = remember(poll.votes, option) { poll.getVotes(option).mapNotNull(Vote::user) }

            PollMoreOptionItem(
                poll = poll,
                option = option,
                voteCount = voteCount,
                totalVoteCount = totalVoteCount,
                users = users,
                checked = isVotedByMine,
                onCastVote = { onCastVote(option) },
                onRemoveVote = { poll.votes.find { it.optionId == option.id }?.let(onRemoveVote) },
            )
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun PollMoreOptionItem(
    poll: Poll,
    option: Option,
    voteCount: Int,
    totalVoteCount: Int,
    users: List<User>,
    checked: Boolean,
    onCastVote: () -> Unit,
    onRemoveVote: () -> Unit,
) {
    val colors = ChatTheme.colors
    PollOptionVotingRow(
        modifier = Modifier.padding(StreamTokens.spacingXs),
        poll = poll,
        option = option,
        voteCount = voteCount,
        totalVoteCount = totalVoteCount,
        users = users,
        checked = checked,
        style = PollStyle(
            textColor = colors.chatTextIncoming,
            outlineColor = colors.chatBorderOnChatIncoming,
            progressColor = colors.chatPollProgressFillIncoming,
            trackColor = colors.chatPollProgressTrackIncoming,
        ),
        onCastVote = onCastVote,
        onRemoveVote = onRemoveVote,
    )
}

@Preview
@Composable
private fun PollMoreOptionsDialogPreview() {
    ChatTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            PollMoreOptionsDialog()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PollMoreOptionsDialog() {
    StreamScreenBottomSheet(onDismissRequest = {}) {
        Content(
            selectedPoll = SelectedPoll(
                poll = PreviewPollData.poll1,
                message = PreviewMessageData.message1,
                pollSelectionType = PollSelectionType.MoreOption,
            ),
        )
    }
}
