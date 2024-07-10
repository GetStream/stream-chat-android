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
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import io.getstream.chat.android.compose.previewdata.PreviewMessageData
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.isErrorOrFailed
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import io.getstream.chat.android.ui.common.state.messages.list.MessagePosition

/**
 * Message content for the poll, which distinguishes the owner and users and allows them to interact.
 *
 * @param messageItem The message item to show the content for.
 * @param modifier Modifier for styling.
 * @param onCastVote Callback when a user cast a vote on an option.
 * @param onRemoveVote Callback when a user remove a vote on an option.
 * @param onLongItemClick Handler when the user selects a message, on long tap.
 */
@Composable
public fun PollMessageContent(
    modifier: Modifier,
    messageItem: MessageItemState,
    onCastVote: (Message, Poll, Option) -> Unit,
    onRemoveVote: (Message, Poll, Vote) -> Unit,
    onClosePoll: (String) -> Unit,
    onLongItemClick: (Message) -> Unit = {},
) {
    val message = messageItem.message
    val position = messageItem.groupPosition
    val ownsMessage = messageItem.isMine

    val messageBubbleShape = when {
        position.contains(MessagePosition.TOP) || position.contains(MessagePosition.MIDDLE) -> RoundedCornerShape(16.dp)
        else -> {
            if (ownsMessage) ChatTheme.shapes.myMessageBubble else ChatTheme.shapes.otherMessageBubble
        }
    }

    val messageBubbleColor = when {
        message.isDeleted() -> when (ownsMessage) {
            true -> ChatTheme.ownMessageTheme.deletedBackgroundColor
            else -> ChatTheme.otherMessageTheme.deletedBackgroundColor
        }

        else -> when (ownsMessage) {
            true -> ChatTheme.colors.linkBackground
            else -> ChatTheme.otherMessageTheme.backgroundColor
        }
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
                    poll = poll,
                    isMine = ownsMessage,
                    onCastVote = { option ->
                        onCastVote.invoke(message, poll, option)
                    },
                    onRemoveVote = { vote ->
                        onRemoveVote.invoke(message, poll, vote)
                    },
                    onClosePoll = onClosePoll,
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

@Composable
private fun PollMessageContent(
    poll: Poll,
    isMine: Boolean,
    onClosePoll: (String) -> Unit,
    onCastVote: (Option) -> Unit,
    onRemoveVote: (Vote) -> Unit,
) {
    val heightMax = LocalConfiguration.current.screenHeightDp
    val isClosed = poll.closed

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
                text = poll.name,
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
                totalVoteCount = poll.votes.size,
                checkedCount = poll.ownVotes.count { it.optionId == option.id },
                checked = isVotedByMine,
                onCastVote = { onCastVote.invoke(option) },
                onRemoveVote = {
                    val vote = poll.votes.firstOrNull { it.optionId == option.id } ?: return@PollOptionItem
                    onRemoveVote.invoke(vote)
                },
            )
        }

        item { }

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
private fun PollOptionItem(
    poll: Poll,
    option: Option,
    voteCount: Int,
    totalVoteCount: Int,
    checkedCount: Int,
    checked: Boolean,
    onCastVote: () -> Unit,
    onRemoveVote: () -> Unit,
) {
    val isVotedByMine = poll.ownVotes.any { it.optionId == option.id }

    Column(
        modifier = Modifier
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
                    .weight(1f)
                    .padding(start = 4.dp, bottom = 2.dp),
                text = option.text,
                color = ChatTheme.colors.textHighEmphasis,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
            )

            Text(
                modifier = Modifier.padding(bottom = 2.dp),
                text = voteCount.toString(),
                color = ChatTheme.colors.textHighEmphasis,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
            )
        }

        val progress = if (voteCount == 0 || totalVoteCount == 0) {
            0f
        } else {
            voteCount / totalVoteCount.toFloat()
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
            progress = progress,
            color = if (isVotedByMine) {
                ChatTheme.colors.infoAccent
            } else {
                ChatTheme.colors.primaryAccent
            },
            backgroundColor = ChatTheme.colors.inputBackground,
        )
    }
}

@Composable
private fun PollItemCheckBox(
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
                onClosePoll = {},
                messageItem = MessageItemState(
                    message = PreviewMessageData.messageWithPoll,
                    isMine = true,
                ),
            )

            PollMessageContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                onCastVote = { _, _, _ -> },
                onRemoveVote = { _, _, _ -> },
                onClosePoll = {},
                messageItem = MessageItemState(
                    message = PreviewMessageData.messageWithError,
                    isMine = true,
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
