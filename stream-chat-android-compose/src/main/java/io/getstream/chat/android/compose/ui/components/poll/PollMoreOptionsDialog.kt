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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.messages.PollItemCheckBox
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
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
@Suppress("LongMethod")
@Composable
public fun PollMoreOptionsDialog(
    selectedPoll: SelectedPoll,
    listViewModel: MessageListViewModel,
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
            label = "poll more options dialog",
        ) {
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ChatTheme.colors.appBackground),
    ) {
        item {
            PollDialogHeader(
                title = stringResource(id = R.string.stream_compose_poll_options),
                onBackPressed = onBackPressed,
            )
        }

        item { PollMoreOptionsTitle(title = poll.name) }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        pollMoreOptionsContent(
            poll = poll,
            onCastVote = onCastVote,
            onRemoveVote = onRemoveVote,
        )

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
internal fun PollMoreOptionsTitle(title: String) {
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

private fun LazyListScope.pollMoreOptionsContent(
    poll: Poll,
    onCastVote: (Option) -> Unit,
    onRemoveVote: (Vote) -> Unit,
) {
    val options = poll.options
    itemsIndexed(
        items = options,
        key = { _, option -> option.id },
    ) { index, option ->
        val voteCount = poll.voteCountsByOption[option.id] ?: 0
        val isVotedByMine = poll.ownVotes.any { it.optionId == option.id }

        PollMoreOptionItem(
            index = index,
            poll = poll,
            option = option,
            voteCount = voteCount,
            checkedCount = poll.ownVotes.count { it.optionId == option.id },
            checked = isVotedByMine,
            onCastVote = { onCastVote.invoke(option) },
            onRemoveVote = {
                val vote = poll.votes.firstOrNull { it.optionId == option.id } ?: return@PollMoreOptionItem
                onRemoveVote.invoke(vote)
            },
        )
    }
}

@Composable
private fun PollMoreOptionItem(
    index: Int,
    poll: Poll,
    option: Option,
    voteCount: Int,
    checkedCount: Int,
    checked: Boolean,
    onCastVote: () -> Unit,
    onRemoveVote: () -> Unit,
) {
    val shape = if (index == 0) {
        RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
    } else if (index == poll.options.size - 1) {
        RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
    } else {
        RoundedCornerShape(0.dp)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(color = ChatTheme.colors.inputBackground, shape = shape)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!poll.closed) {
            PollItemCheckBox(
                modifier = Modifier.padding(end = 8.dp),
                checked = checked,
                onCheckChanged = { enabled ->
                    if (enabled && checkedCount < poll.maxVotesAllowed && !checked) {
                        onCastVote.invoke()
                    } else if (!enabled) {
                        onRemoveVote.invoke()
                    }
                },
            )
        }

        Text(
            modifier = Modifier.weight(1f),
            text = option.text,
            color = ChatTheme.colors.textHighEmphasis,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            fontSize = 16.sp,
        )

        Text(
            modifier = Modifier.padding(bottom = 2.dp),
            text = voteCount.toString(),
            color = ChatTheme.colors.textHighEmphasis,
            fontSize = 16.sp,
        )
    }
}

@Preview
@Composable
private fun PollMoreOptionsDialogPreview() {
    ChatTheme {
        PollMoreOptionsDialog()
    }
}

@Composable
internal fun PollMoreOptionsDialog() {
    Content(
        selectedPoll = SelectedPoll(
            poll = PreviewPollData.poll1,
            message = PreviewMessageData.message1,
            pollSelectionType = PollSelectionType.MoreOption,
        ),
    )
}
