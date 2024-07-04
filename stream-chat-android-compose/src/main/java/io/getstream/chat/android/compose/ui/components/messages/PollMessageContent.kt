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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import io.getstream.chat.android.ui.common.state.messages.list.MessagePosition

/**
 * Message content for the poll, which distinguishes the owner and users and allows them to interact.
 *
 * @param messageItem The message item to show the content for.
 * @param modifier Modifier for styling.
 * @param onCastVote Callback when a user cast a vote on an option.
 * @param onLongItemClick Handler when the user selects a message, on long tap.
 */
@Composable
public fun PollMessageContent(
    modifier: Modifier,
    messageItem: MessageItemState,
    onCastVote: (Message, Poll, Option) -> Unit,
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
            true -> ChatTheme.ownMessageTheme.backgroundColor
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
                PollMessageContent(poll = poll, onCastVote = { option ->
                    onCastVote.invoke(message, poll, option)
                })
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
    onCastVote: (Option) -> Unit,
) {
    val checkedMap = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(key1 = poll) {
        poll.options.forEach { option ->
            checkedMap[option.id] = false
        }
    }

    val heightMax = LocalConfiguration.current.screenHeightDp

    LazyColumn(
        modifier = Modifier.padding(
            horizontal = 10.dp,
            vertical = 12.dp,
        ).heightIn(max = heightMax.dp),
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
            items = poll.options,
            key = { it.id },
        ) { option ->
            val voteCount = poll.voteCountsByOption[option.id] ?: 0
            PollOptionItem(
                poll = poll,
                option = option,
                voteCount = voteCount,
                totalVoteCount = poll.votes.size,
                checkedCount = checkedMap.count { it.value },
                checked = checkedMap[option.id] ?: false,
                onCheckChanged = { checked ->
                    checkedMap[option.id] = checked
                    onCastVote.invoke(option)
                },
            )
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
    onCheckChanged: (Boolean) -> Unit,
) {
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
                Checkbox(
                    checked = checked,
                    onCheckedChange = { changed ->
                        if (!changed) {
                            onCheckChanged.invoke(false)
                        } else if (checkedCount < poll.maxVotesAllowed) {
                            onCheckChanged.invoke(true)
                        }
                    },
                )
            }

            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 2.dp),
                text = option.text,
                color = ChatTheme.colors.textHighEmphasis,
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
                .clip(RoundedCornerShape(4.dp))
                .height(4.dp),
            progress = progress,
            color = ChatTheme.colors.primaryAccent,
            backgroundColor = ChatTheme.colors.inputBackground,
        )
    }
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
                messageItem = MessageItemState(
                    message = PreviewMessageData.messageWithError,
                    isMine = true,
                ),
            )
        }
    }
}
