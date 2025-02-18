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

package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatarRow
import io.getstream.chat.android.compose.ui.components.composer.InputField
import io.getstream.chat.android.compose.ui.components.poll.AddAnswerDialog
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.isErrorOrFailed
import io.getstream.chat.android.compose.util.extensions.toSet
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.models.VotingVisibility
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import io.getstream.chat.android.ui.common.state.messages.list.MessagePosition
import io.getstream.chat.android.ui.common.state.messages.poll.PollSelectionType

/**
 * Message content for the poll, which distinguishes the owner and users and allows them to interact.
 *
 * @param messageItem The message item to show the content for.
 * @param modifier Modifier for styling.
 * @param onCastVote Callback when a user cast a vote on an option.
 * @param onRemoveVote Callback when a user remove a vote on an option.
 * @param selectPoll Callback when a user selects a poll.
 * @param onAddAnswer Callback when a user adds a new answer to the poll.
 * @param onClosePoll Callback when a user closes a poll.
 * @param onAddPollOption Callback when a user adds a new option to the poll.
 * @param onLongItemClick Handler when the user selects a message, on long tap.
 */
@Suppress("LongParameterList", "LongMethod")
@Composable
public fun PollMessageContent(
    modifier: Modifier,
    messageItem: MessageItemState,
    onCastVote: (Message, Poll, Option) -> Unit,
    onRemoveVote: (Message, Poll, Vote) -> Unit,
    selectPoll: (Message, Poll, PollSelectionType) -> Unit,
    onAddAnswer: (message: Message, poll: Poll, answer: String) -> Unit,
    onClosePoll: (String) -> Unit,
    onAddPollOption: (poll: Poll, option: String) -> Unit,
    onLongItemClick: (Message) -> Unit = {},
) {
    val message = messageItem.message
    val position = messageItem.groupPosition
    val ownsMessage = messageItem.isMine

    val messageTheme = if (ownsMessage) ChatTheme.ownMessageTheme else ChatTheme.otherMessageTheme
    val messageBubbleShape = when {
        position.contains(MessagePosition.TOP) -> messageTheme.backgroundShapes.top
        position.contains(MessagePosition.MIDDLE) -> messageTheme.backgroundShapes.middle
        position.contains(MessagePosition.BOTTOM) -> messageTheme.backgroundShapes.bottom
        else -> messageTheme.backgroundShapes.none
    }

    val messageBubbleColor = when {
        message.isDeleted() -> messageTheme.deletedBackgroundColor
        else -> messageTheme.poll.backgroundColor
    }

    val poll = message.poll
    if (!messageItem.isErrorOrFailed() && poll != null) {
        MessageBubble(
            modifier = modifier,
            shape = messageBubbleShape,
            color = messageBubbleColor,
            border = if (messageItem.isMine) null else BorderStroke(1.dp, ChatTheme.colors.borders),
            content = {
                PollMessageContent(
                    message = message,
                    poll = poll,
                    isMine = ownsMessage,
                    onCastVote = { option ->
                        onCastVote.invoke(message, poll, option)
                    },
                    onRemoveVote = { vote ->
                        onRemoveVote.invoke(message, poll, vote)
                    },
                    selectPoll = selectPoll,
                    onAddAnswer = { answer ->
                        onAddAnswer.invoke(message, poll, answer)
                    },
                    onClosePoll = onClosePoll,
                    onAddPollOption = onAddPollOption,
                )
            },
        )
    } else {
        Box(modifier = modifier) {
            MessageBubble(
                modifier = Modifier.padding(end = 12.dp),
                shape = messageBubbleShape,
                color = messageBubbleColor,
                content = {
                    MessageContent(
                        message = message,
                        currentUser = messageItem.currentUser,
                        onLongItemClick = onLongItemClick,
                        onGiphyActionClick = {},
                        onMediaGalleryPreviewResult = {},
                        onQuotedMessageClick = {},
                    )
                },
            )

            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.BottomEnd),
                painter = painterResource(id = R.drawable.stream_compose_ic_error),
                contentDescription = null,
                tint = ChatTheme.colors.errorAccent,
            )
        }
    }
}

@Suppress("LongParameterList", "LongMethod")
@Composable
private fun PollMessageContent(
    message: Message,
    poll: Poll,
    isMine: Boolean,
    onClosePoll: (String) -> Unit,
    onCastVote: (Option) -> Unit,
    onAddAnswer: (answer: String) -> Unit,
    onRemoveVote: (Vote) -> Unit,
    onAddPollOption: (poll: Poll, option: String) -> Unit,
    selectPoll: (Message, Poll, PollSelectionType) -> Unit,
) {
    val showDialog = remember { mutableStateOf(false) }
    val heightMax = LocalConfiguration.current.screenHeightDp
    val isClosed = poll.closed
    val showAddAnswerDialog = remember { mutableStateOf(false) }

    if (showAddAnswerDialog.value) {
        AddAnswerDialog(
            initMessage = "",
            onDismiss = { showAddAnswerDialog.value = false },
            onNewAnswer = { newAnswer -> onAddAnswer.invoke(newAnswer) },
        )
    }

    if (showDialog.value) {
        NewOptionDialog(
            onDismiss = { showDialog.value = false },
            onNewOption = { newOption -> onAddPollOption.invoke(poll, newOption) },
        )
    }

    LazyColumn(
        modifier = Modifier
            .padding(
                horizontal = 10.dp,
                vertical = 12.dp,
            )
            .heightIn(max = heightMax.dp),
        userScrollEnabled = false,
    ) {
        item {
            Text(
                modifier = Modifier.padding(bottom = 4.dp),
                text = poll.name,
                color = ChatTheme.colors.textHighEmphasis,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
            )
        }

        item {
            Text(
                modifier = Modifier.padding(bottom = 8.dp),
                text = poll.description,
                color = ChatTheme.colors.textLowEmphasis,
                fontSize = 12.sp,
            )
        }

        items(
            items = poll.options.take(10),
            key = { it.id },
        ) { option ->
            val voteCount = poll.voteCountsByOption[option.id] ?: 0
            val isVotedByMine = poll.ownVotes.any { it.optionId == option.id }

            PollOptionItem(
                poll = poll,
                option = option,
                voteCount = voteCount,
                users = poll.votes.filter { it.optionId == option.id }.mapNotNull { it.user },
                totalVoteCount = poll.voteCountsByOption.values.sum(),
                checkedCount = poll.ownVotes.count { it.optionId == option.id },
                checked = isVotedByMine,
                onCastVote = { onCastVote.invoke(option) },
                onRemoveVote = {
                    val vote = poll.votes.firstOrNull { it.optionId == option.id } ?: return@PollOptionItem
                    onRemoveVote.invoke(vote)
                },
            )
        }

        if (poll.allowUserSuggestedOptions && !isClosed) {
            item {
                PollOptionButton(
                    text = stringResource(id = R.string.stream_compose_poll_suggest_option),
                    onButtonClicked = { showDialog.value = true },
                )
            }
        }

        if (poll.allowAnswers) {
            if (poll.answers.isNotEmpty()) {
                item {
                    PollOptionButton(
                        text = stringResource(R.string.stream_compose_view_answers),
                        onButtonClicked = { selectPoll.invoke(message, poll, PollSelectionType.ViewAnswers) },
                    )
                }
            } else if (!poll.closed) {
                item {
                    PollOptionButton(
                        text = stringResource(R.string.stream_compose_add_answer),
                        onButtonClicked = { showAddAnswerDialog.value = true },
                    )
                }
            }
        }

        if (poll.options.size > 10) {
            item {
                PollOptionButton(
                    text = stringResource(id = R.string.stream_compose_poll_see_more_options, poll.options.size),
                    onButtonClicked = { selectPoll.invoke(message, poll, PollSelectionType.MoreOption) },
                )
            }
        }

        item {
            PollOptionButton(
                text = stringResource(id = R.string.stream_compose_poll_view_result),
                onButtonClicked = { selectPoll.invoke(message, poll, PollSelectionType.ViewResult) },
            )
        }

        if (isMine && !isClosed) {
            item {
                PollOptionButton(
                    text = stringResource(id = R.string.stream_compose_poll_end_vote),
                    onButtonClicked = { onClosePoll.invoke(poll.id) },
                )
            }
        }
    }
}

@Composable
private fun NewOptionDialog(
    onDismiss: () -> Unit,
    onNewOption: (newOption: String) -> Unit,
) {
    val newOption = remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    AlertDialog(
        title = {
            Text(
                text = stringResource(R.string.stream_compose_suggest_an_option),
                color = ChatTheme.colors.textHighEmphasis,
            )
        },
        text = {
            InputField(
                value = newOption.value,
                onValueChange = { newOption.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                decorationBox = { innerTextField ->
                    Column {
                        innerTextField()
                    }
                },
            )
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        },
        onDismissRequest = { onDismiss.invoke() },
        confirmButton = {
            TextButton(
                enabled = newOption.value.isNotBlank(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = ChatTheme.colors.primaryAccent,
                    disabledContentColor = ChatTheme.colors.primaryAccent.copy(alpha = 0.5f),
                ),
                onClick = {
                    onNewOption.invoke(newOption.value)
                    onDismiss.invoke()
                },
            ) {
                Text(stringResource(R.string.stream_compose_confirm))
            }
        },
        dismissButton = {
            TextButton(
                colors = ButtonDefaults.textButtonColors(contentColor = ChatTheme.colors.primaryAccent),
                onClick = { onDismiss.invoke() },
            ) {
                Text(stringResource(R.string.stream_compose_dismiss))
            }
        },
        containerColor = ChatTheme.colors.barsBackground,
    )
}

@Composable
private fun PollOptionItem(
    modifier: Modifier = Modifier,
    poll: Poll,
    option: Option,
    voteCount: Int,
    totalVoteCount: Int,
    users: List<User>,
    checkedCount: Int,
    checked: Boolean,
    onCastVote: () -> Unit,
    onRemoveVote: () -> Unit,
) {
    val isVotedByMine = poll.ownVotes.any { it.optionId == option.id }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!poll.closed) {
                PollItemCheckBox(
                    enabled = checked,
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
                modifier = Modifier
                    .weight(0.5f)
                    .padding(start = 4.dp, bottom = 2.dp),
                text = option.text,
                color = ChatTheme.colors.textHighEmphasis,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 16.sp,
            )

            Row {
                if (voteCount > 0 && poll.votingVisibility != VotingVisibility.ANONYMOUS) {
                    UserAvatarRow(
                        modifier = Modifier.padding(end = 2.dp),
                        users = users,
                    )
                }

                Text(
                    modifier = Modifier.padding(bottom = 2.dp),
                    text = voteCount.toString(),
                    color = ChatTheme.colors.textHighEmphasis,
                    fontSize = 16.sp,
                )
            }
        }

        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = if (poll.closed) {
                        0.dp
                    } else {
                        22.dp
                    },
                )
                .clip(RoundedCornerShape(4.dp))
                .height(4.dp),
            progress = {
                if (voteCount == 0 || totalVoteCount == 0) {
                    0f
                } else {
                    voteCount / totalVoteCount.toFloat()
                }
            },
            color = if (isVotedByMine) {
                ChatTheme.colors.infoAccent
            } else {
                ChatTheme.colors.primaryAccent
            },
            trackColor = ChatTheme.colors.inputBackground,
            gapSize = 0.dp,
            strokeCap = StrokeCap.Square,
            drawStopIndicator = { /* Don't draw the stop indicator */ },
        )
    }
}

@Composable
internal fun PollItemCheckBox(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onCheckChanged: (Boolean) -> Unit,
) {
    Box(
        modifier = modifier
            .size(18.dp)
            .background(
                if (enabled) {
                    ChatTheme.colors.primaryAccent
                } else {
                    ChatTheme.colors.disabled
                },
                CircleShape,
            )
            .padding(1.dp)
            .background(
                if (enabled) {
                    ChatTheme.colors.primaryAccent
                } else {
                    ChatTheme.colors.inputBackground
                },
                CircleShape,
            )
            .clickable { onCheckChanged.invoke(!enabled) },
    ) {
        if (enabled) {
            Icon(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(3.dp),
                painter = painterResource(id = R.drawable.stream_compose_ic_checkmark),
                tint = Color.White,
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun PollOptionButton(
    text: String,
    onButtonClicked: () -> Unit,
) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 11.dp)
            .clickable { onButtonClicked.invoke() },
        textAlign = TextAlign.Center,
        text = text,
        color = ChatTheme.colors.primaryAccent,
        fontSize = 16.sp,
    )
}

@Preview
@Composable
private fun PollMessageContentPreview() {
    ChatTheme {
        Column(modifier = Modifier.background(ChatTheme.colors.appBackground)) {
            PollMessageContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                onCastVote = { _, _, _ -> },
                onRemoveVote = { _, _, _ -> },
                selectPoll = { _, _, _ -> },
                onAddAnswer = { _, _, _ -> },
                onClosePoll = {},
                onAddPollOption = { _, _ -> },
                messageItem = MessageItemState(
                    message = io.getstream.chat.android.previewdata.PreviewMessageData.messageWithPoll,
                    isMine = true,
                    ownCapabilities = ChannelCapabilities.toSet(),
                ),
            )

            PollMessageContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                onCastVote = { _, _, _ -> },
                onRemoveVote = { _, _, _ -> },
                selectPoll = { _, _, _ -> },
                onAddAnswer = { _, _, _ -> },
                onClosePoll = {},
                onAddPollOption = { _, _ -> },
                messageItem = MessageItemState(
                    message = io.getstream.chat.android.previewdata.PreviewMessageData.messageWithError,
                    isMine = true,
                    ownCapabilities = ChannelCapabilities.toSet(),
                ),
            )
        }
    }
}

@Preview
@Composable
private fun PollItemCheckBoxPreview() {
    ChatTheme {
        Row {
            PollItemCheckBox(
                enabled = false,
                onCheckChanged = {},
            )

            PollItemCheckBox(
                enabled = true,
                onCheckChanged = {},
            )
        }
    }
}

@Preview
@Composable
private fun PollOptionButtonPreview() {
    ChatTheme {
        Column {
            PollOptionButton("End Vote") {}
            PollOptionButton("View Result") {}
        }
    }
}
