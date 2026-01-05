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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import io.getstream.chat.android.client.extensions.internal.getVotesUnlessAnonymous
import io.getstream.chat.android.client.extensions.internal.getWinner
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.previewdata.PreviewMessageData
import io.getstream.chat.android.previewdata.PreviewPollData
import io.getstream.chat.android.ui.common.state.messages.poll.PollSelectionType
import io.getstream.chat.android.ui.common.state.messages.poll.SelectedPoll
import io.getstream.chat.android.ui.common.utils.extensions.initials

/**
 * A dialog that should be shown if a user taps the seeing result of the votes.
 *
 * @param selectedPoll The current poll that contains all the states.
 * @param onDismissRequest Handler for dismissing the dialog.
 * @param onBackPressed Handler for pressing a back button.
 */
@Composable
public fun PollViewResultDialog(
    selectedPoll: SelectedPoll,
    onDismissRequest: () -> Unit,
    onBackPressed: () -> Unit,
) {
    val state = remember {
        MutableTransitionState(false).apply {
            // Start the animation immediately.
            targetState = true
        }
    }
    Popup(
        alignment = Alignment.BottomCenter,
        onDismissRequest = onDismissRequest,
    ) {
        AnimatedVisibility(
            visibleState = state,
            enter = fadeIn() + slideInVertically(
                animationSpec = tween(400),
                initialOffsetY = { fullHeight -> fullHeight / 2 },
            ),
            exit = fadeOut(animationSpec = tween(200)) +
                slideOutVertically(animationSpec = tween(400)),
            label = "poll view result dialog",
        ) {
            Content(
                selectedPoll = selectedPoll,
                onBackPressed = onBackPressed,
            )
        }
    }
}

@Composable
private fun Content(
    selectedPoll: SelectedPoll,
    onBackPressed: () -> Unit = {},
) {
    val poll = selectedPoll.poll

    BackHandler { onBackPressed.invoke() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ChatTheme.colors.appBackground),
    ) {
        item {
            PollDialogHeader(
                title = stringResource(id = R.string.stream_compose_poll_results),
                onBackPressed = onBackPressed,
            )
        }

        item { PollViewResultTitle(title = poll.name) }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        pollViewResultContent(poll = poll)

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

private fun LazyListScope.pollViewResultContent(
    poll: Poll,
) {
    val options = poll.options.sortedByDescending { option -> poll.voteCountsByOption[option.id] ?: 0 }
    val winner = poll.getWinner()

    items(
        items = options,
        key = Option::id,
    ) { option ->
        val votes = poll.getVotesUnlessAnonymous(option)
        PollViewResultItem(
            option = option,
            isWinner = winner == option,
            votesCount = poll.voteCountsByOption[option.id] ?: 0,
            votes = votes,
        )
    }
}

@Composable
private fun PollViewResultItem(
    option: Option,
    isWinner: Boolean,
    votesCount: Int,
    votes: List<Vote>,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth()
            .clip(shape = ChatTheme.shapes.pollOptionInput)
            .background(ChatTheme.colors.inputBackground)
            .padding(16.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                text = option.text,
                color = ChatTheme.colors.textHighEmphasis,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
            )

            if (isWinner) {
                Icon(
                    modifier = Modifier.padding(end = 8.dp),
                    painter = painterResource(id = R.drawable.stream_compose_ic_award),
                    tint = ChatTheme.colors.textHighEmphasis,
                    contentDescription = null,
                )
            }

            Text(
                text = stringResource(id = R.string.stream_compose_poll_vote_counts, votesCount),
                color = ChatTheme.colors.textHighEmphasis,
                fontSize = 16.sp,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        votes.forEach { vote ->
            PollVoteItem(vote = vote)
        }
    }
}

@Composable
private fun PollVoteItem(vote: Vote) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        val user = vote.user
        if (user != null) {
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
}

@Composable
private fun PollViewResultTitle(title: String) {
    Box(
        modifier = Modifier
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

@Preview
@Composable
private fun PollViewResultDialogPreview() {
    ChatTheme {
        PollViewResultDialog()
    }
}

@Composable
internal fun PollViewResultDialog() {
    Content(
        selectedPoll = SelectedPoll(
            poll = PreviewPollData.poll1,
            message = PreviewMessageData.message1,
            pollSelectionType = PollSelectionType.ViewResult,
        ),
    )
}
