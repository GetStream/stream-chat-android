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

@file:OptIn(ExperimentalAnimationApi::class)

package io.getstream.chat.android.compose.ui.components.poll

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.previewdata.PreviewPollData
import io.getstream.chat.android.ui.common.state.messages.poll.SelectedPoll

/**
 * A dialog that should be shown if a user taps the seeing result of the votes.
 *
 * @param selectedPoll The current poll that contains all the states.
 * @param onDismissRequest Handler for dismissing the dialog.
 * @param onBackPressed Handler for pressing a back button.
 */
@Composable
public fun PollViewResultDialog(
    selectedPoll: SelectedPoll?,
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
            if (selectedPoll != null) {
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
        }
    }
}

internal fun LazyListScope.pollViewResultContent(
    poll: Poll,
) {
    val votes = poll.votes
    val options = poll.options.sortedByDescending { option -> votes.count { it.optionId == option.id } }

    itemsIndexed(
        items = options,
        key = { _, option -> option.id },
    ) { index, option ->
        PollViewResultItem(
            index = index,
            option = option,
            votes = votes.filter { it.optionId == option.id },
        )
    }
}

@Composable
private fun PollViewResultItem(
    index: Int,
    option: Option,
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

            if (index == 0) {
                Icon(
                    modifier = Modifier.padding(end = 8.dp),
                    painter = painterResource(id = R.drawable.stream_compose_ic_award),
                    tint = ChatTheme.colors.textHighEmphasis,
                    contentDescription = null,
                )
            }

            Text(
                text = stringResource(id = R.string.stream_compose_poll_vote_counts, votes.size),
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
    ) {
        val user = vote.user
        if (user != null) {
            UserAvatar(
                modifier = Modifier.size(20.dp),
                user = user,
                showOnlineIndicator = false,
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
                    .weight(1f),
                text = user.name,
                color = ChatTheme.colors.textHighEmphasis,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
internal fun PollViewResultTitle(title: String) {
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
internal fun PollViewResultDialogPreview() {
    val poll = io.getstream.chat.android.previewdata.PreviewPollData.poll1

    ChatTheme {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(ChatTheme.colors.appBackground),
        ) {
            item {
                PollDialogHeader(
                    title = stringResource(id = R.string.stream_compose_poll_results),
                    onBackPressed = {},
                )
            }

            item { PollViewResultTitle(title = poll.name) }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            pollViewResultContent(poll = poll)

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
