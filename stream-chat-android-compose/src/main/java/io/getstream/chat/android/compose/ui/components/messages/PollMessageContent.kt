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

package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.avatar.AvatarSize
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatarStack
import io.getstream.chat.android.compose.ui.components.button.StreamButtonStyle
import io.getstream.chat.android.compose.ui.components.button.StreamButtonStyleDefaults
import io.getstream.chat.android.compose.ui.components.button.StreamTextButton
import io.getstream.chat.android.compose.ui.components.common.RadioCheck
import io.getstream.chat.android.compose.ui.components.composer.InputField
import io.getstream.chat.android.compose.ui.components.poll.AddAnswerDialog
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageStyling
import io.getstream.chat.android.compose.ui.theme.MessageStyling.PollStyle
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.isErrorOrFailed
import io.getstream.chat.android.compose.util.extensions.toSet
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.models.VotingVisibility
import io.getstream.chat.android.previewdata.PreviewMessageData
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import io.getstream.chat.android.ui.common.state.messages.poll.PollSelectionType
import io.getstream.chat.android.ui.common.utils.PollsConstants
import io.getstream.chat.android.ui.common.utils.extensions.getSubtitle

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
    val ownsMessage = messageItem.isMine

    val messageAlignment = ChatTheme.messageAlignmentProvider.provideMessageAlignment(messageItem)
    val messageBubbleShape = MessageStyling.shape(messageItem.groupPosition, messageAlignment)
    val messageBubbleColor = MessageStyling.backgroundColor(messageItem.isMine)

    val poll = message.poll
    if (!messageItem.isErrorOrFailed() && poll != null) {
        ChatTheme.componentFactory.MessageBubble(
            modifier = modifier,
            message = message,
            shape = messageBubbleShape,
            color = messageBubbleColor,
            border = null,
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
            ChatTheme.componentFactory.MessageBubble(
                modifier = Modifier.padding(end = 12.dp),
                message = message,
                shape = messageBubbleShape,
                color = messageBubbleColor,
                border = BorderStroke(1.dp, ChatTheme.colors.borderCoreDefault),
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

            ChatTheme.componentFactory.MessageFailedIcon(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.BottomEnd),
                message = message,
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
    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }
    val showAddAnswerDialog = remember { mutableStateOf(false) }
    val typography = ChatTheme.typography
    val style = MessageStyling.pollStyle(outgoing = isMine)

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

    Column(modifier = Modifier.padding(StreamTokens.spacingMd)) {
        Text(
            text = poll.name,
            style = typography.bodyEmphasis,
            color = style.textColor,
        )

        Text(
            modifier = Modifier.padding(top = StreamTokens.spacing2xs),
            text = poll.getSubtitle(context),
            style = typography.captionDefault,
            color = style.textColor,
        )

        poll.options.take(PollsConstants.MAX_NUMBER_OF_VISIBLE_OPTIONS).forEachIndexed { index, option ->
            val padding = PaddingValues(
                top = if (index > 0) StreamTokens.spacingLg else StreamTokens.spacingMd,
                bottom = if (index == poll.options.size - 1) StreamTokens.spacingLg else 0.dp,
            )
            val voteCount = poll.voteCountsByOption[option.id] ?: 0

            PollOptionItem(
                modifier = Modifier.padding(padding),
                poll = poll,
                option = option,
                voteCount = voteCount,
                users = poll.getVotes(option).mapNotNull(Vote::user),
                totalVoteCount = poll.voteCountsByOption.values.sum(),
                checkedCount = poll.ownVotes.count { it.optionId == option.id },
                checked = poll.ownVotes.any { it.optionId == option.id },
                style = style,
                onCastVote = { onCastVote.invoke(option) },
                onRemoveVote = {
                    poll.ownVotes.firstOrNull { it.optionId == option.id }
                        ?.let(onRemoveVote)
                },
            )
        }

        PollButtons(
            poll = poll,
            style = style,
            selectPoll = selectPoll,
            message = message,
            showDialog = showDialog,
            showAddAnswerDialog = showAddAnswerDialog,
            isMine = isMine,
            onClosePoll = onClosePoll,
        )
    }
}

@Suppress("LongParameterList")
@Composable
private fun PollButtons(
    poll: Poll,
    style: PollStyle,
    selectPoll: (Message, Poll, PollSelectionType) -> Unit,
    message: Message,
    showDialog: MutableState<Boolean>,
    showAddAnswerDialog: MutableState<Boolean>,
    isMine: Boolean,
    onClosePoll: (String) -> Unit,
) {
    val outlinedButtonStyle = StreamButtonStyleDefaults.secondaryOutline.copy(borderColor = style.outlineColor)
    val ghostButtonStyle = StreamButtonStyleDefaults.secondaryGhost

    Column(verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs)) {
        if (poll.options.size > PollsConstants.MAX_NUMBER_OF_VISIBLE_OPTIONS) {
            PollOptionButton(
                text = stringResource(id = R.string.stream_ui_poll_action_see_all, poll.options.size),
                style = ghostButtonStyle,
                onButtonClicked = { selectPoll(message, poll, PollSelectionType.MoreOption) },
            )
        }

        PollOptionButton(
            text = stringResource(id = R.string.stream_compose_poll_view_result),
            style = outlinedButtonStyle,
            onButtonClicked = { selectPoll(message, poll, PollSelectionType.ViewResult) },
        )

        if (isMine && !poll.closed) {
            PollOptionButton(
                text = stringResource(id = R.string.stream_compose_poll_end_vote),
                style = outlinedButtonStyle,
                onButtonClicked = { onClosePoll.invoke(poll.id) },
            )
        }

        if (poll.allowUserSuggestedOptions && !poll.closed) {
            PollOptionButton(
                text = stringResource(id = R.string.stream_compose_poll_suggest_option),
                style = ghostButtonStyle,
                onButtonClicked = { showDialog.value = true },
            )
        }

        if (poll.allowAnswers) {
            if (poll.answers.isNotEmpty()) {
                PollOptionButton(
                    text = pluralStringResource(
                        R.plurals.stream_ui_poll_action_view_comments,
                        poll.answers.size,
                        poll.answers.size,
                    ),
                    style = ghostButtonStyle,
                    onButtonClicked = { selectPoll(message, poll, PollSelectionType.ViewAnswers) },
                )
            } else if (!poll.closed) {
                PollOptionButton(
                    text = stringResource(R.string.stream_compose_add_answer),
                    style = ghostButtonStyle,
                    onButtonClicked = { showAddAnswerDialog.value = true },
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
                color = ChatTheme.colors.textPrimary,
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
                    contentColor = ChatTheme.colors.accentPrimary,
                    disabledContentColor = ChatTheme.colors.accentPrimary.copy(alpha = 0.5f),
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
                colors = ButtonDefaults.textButtonColors(contentColor = ChatTheme.colors.accentPrimary),
                onClick = { onDismiss.invoke() },
            ) {
                Text(stringResource(R.string.stream_compose_dismiss))
            }
        },
        containerColor = ChatTheme.colors.backgroundElevationElevation1,
    )
}

@Suppress("LongParameterList", "LongMethod")
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
    style: PollStyle,
    onCastVote: () -> Unit,
    onRemoveVote: () -> Unit,
) {
    val typography = ChatTheme.typography

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingSm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!poll.closed) {
            RadioCheck(
                checked = checked,
                onCheckedChange = { enabled: Boolean ->
                    if (enabled && checkedCount < poll.maxVotesAllowed && !checked) {
                        onCastVote.invoke()
                    } else if (!enabled) {
                        onRemoveVote.invoke()
                    }
                },
                borderColor = style.outlineColor,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs)) {
            Row(Modifier.heightIn(min = AvatarSize.ExtraSmall)) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = option.text,
                    style = typography.captionDefault,
                    color = style.textColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (users.isNotEmpty() && poll.votingVisibility != VotingVisibility.ANONYMOUS) {
                    UserAvatarStack(
                        overlap = StreamTokens.spacingXs,
                        users = users.take(MaxStackedAvatars),
                        avatarSize = AvatarSize.ExtraSmall,
                        showBorder = true,
                        modifier = Modifier.padding(start = StreamTokens.spacingXs, end = StreamTokens.spacing2xs),
                    )
                }

                Text(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    text = voteCount.toString(),
                    style = typography.metadataDefault,
                    color = style.textColor,
                )
            }

            val progress by animateFloatAsState(
                targetValue = if (voteCount == 0 || totalVoteCount == 0) {
                    0f
                } else {
                    voteCount / totalVoteCount.toFloat()
                },
            )

            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                progress = { progress },
                color = style.progressColor,
                trackColor = style.trackColor,
                gapSize = 0.dp,
                strokeCap = StrokeCap.Square,
                drawStopIndicator = { /* Don't draw the stop indicator */ },
            )
        }
    }
}

private const val MaxStackedAvatars = 3

@Composable
private fun PollOptionButton(
    text: String,
    style: StreamButtonStyle,
    onButtonClicked: () -> Unit,
) {
    StreamTextButton(
        onClick = onButtonClicked,
        text = text,
        style = style,
        modifier = Modifier.fillMaxWidth(),
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
                    message = PreviewMessageData.messageWithPoll,
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
                    message = PreviewMessageData.messageWithError,
                    isMine = true,
                    ownCapabilities = ChannelCapabilities.toSet(),
                ),
            )
        }
    }
}

@Preview
@Composable
private fun PollOptionButtonPreview() {
    ChatTheme {
        val style = StreamButtonStyleDefaults.secondaryOutline
        Column {
            PollOptionButton("End Vote", style) {}
            PollOptionButton("View Result", style) {}
        }
    }
}
