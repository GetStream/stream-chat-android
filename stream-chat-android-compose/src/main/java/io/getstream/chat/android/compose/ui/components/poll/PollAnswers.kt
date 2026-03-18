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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.avatar.AvatarSize
import io.getstream.chat.android.compose.ui.components.button.StreamButton
import io.getstream.chat.android.compose.ui.components.button.StreamButtonSize
import io.getstream.chat.android.compose.ui.components.button.StreamButtonStyleDefaults
import io.getstream.chat.android.compose.ui.components.button.StreamTextButton
import io.getstream.chat.android.compose.ui.components.composer.InputField
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.theme.UserAvatarParams
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.models.Answer
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.VotingVisibility
import io.getstream.chat.android.previewdata.PreviewPollData
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.state.messages.poll.SelectedPoll
import java.util.Date

@Suppress("LongMethod", "MagicNumber")
@Composable
public fun PollAnswersDialog(
    selectedPoll: SelectedPoll,
    showAnonymousAvatar: Boolean,
    listViewModel: MessageListViewModel,
    onDismissRequest: () -> Unit,
    onBackPressed: () -> Unit,
) {
    val user by listViewModel.user.collectAsState()
    val currentUserAnswer = selectedPoll.poll.answers.firstOrNull { it.user?.id == user?.id }
    val state = remember {
        MutableTransitionState(false).apply {
            // Start the animation immediately.
            targetState = true
        }
    }
    var showAddAnswerDialog by remember { mutableStateOf(false) }
    if (showAddAnswerDialog) {
        AddAnswerDialog(
            initMessage = currentUserAnswer?.text ?: "",
            onDismiss = { showAddAnswerDialog = false },
            onNewAnswer = { newAnswer ->
                listViewModel.castAnswer(selectedPoll.message, selectedPoll.poll, newAnswer)
            },
        )
    }
    Popup(
        alignment = Alignment.BottomCenter,
        onDismissRequest = onDismissRequest,
    ) {
        BackHandler(onBack = onBackPressed)

        @Suppress("MagicNumber")
        AnimatedVisibility(
            visibleState = state,
            enter = fadeIn() + slideInVertically(
                animationSpec = tween(400),
                initialOffsetY = { fullHeight -> fullHeight / 2 },
            ),
            exit = fadeOut(animationSpec = tween(200)) +
                slideOutVertically(animationSpec = tween(400)),
            label = "poll answers dialog",
        ) {
            Content(
                poll = selectedPoll.poll,
                currentUserAnswer = currentUserAnswer,
                showAnonymousAvatar = showAnonymousAvatar,
                onBackPressed = onBackPressed,
                onAddOrEditClick = { showAddAnswerDialog = true },
            )
        }
    }
}

@Composable
private fun Content(
    poll: Poll,
    currentUserAnswer: Answer? = null,
    showAnonymousAvatar: Boolean = false,
    onBackPressed: () -> Unit = {},
    onAddOrEditClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChatTheme.colors.backgroundCoreApp),
    ) {
        PollDialogHeader(
            title = stringResource(id = R.string.stream_compose_poll_answers),
            onBackPressed = onBackPressed,
            trailingContent = {
                if (!poll.closed && currentUserAnswer == null) {
                    StreamButton(
                        onClick = onAddOrEditClick,
                        style = StreamButtonStyleDefaults.primarySolid,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.stream_compose_ic_edit),
                            contentDescription = stringResource(id = R.string.stream_compose_add_answer),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = StreamTokens.spacingMd)
                .padding(top = StreamTokens.spacing2xl),
            verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingMd),
        ) {
            val showAvatar = (poll.votingVisibility == VotingVisibility.PUBLIC) || showAnonymousAvatar

            if (currentUserAnswer != null) {
                PollAnswersItem(
                    answer = currentUserAnswer,
                    showAvatar = showAvatar,
                    showUpdateButton = !poll.closed,
                    onUpdateClick = onAddOrEditClick,
                )
            }
            poll.answers.forEach { answer ->
                if (answer.id != currentUserAnswer?.id) {
                    PollAnswersItem(
                        answer = answer,
                        showAvatar = showAvatar,
                    )
                }
            }
        }
    }
}

@Composable
internal fun PollAnswersItem(
    answer: Answer,
    showAvatar: Boolean,
    showUpdateButton: Boolean = false,
    onUpdateClick: () -> Unit = {},
) {
    val colors = ChatTheme.colors
    val typography = ChatTheme.typography

    PollSection(
        contentPadding = PaddingValues(StreamTokens.spacingMd),
        verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
    ) {
        Text(
            text = answer.text,
            color = colors.textPrimary,
            style = typography.bodyDefault,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
        ) {
            val user = answer.user?.takeIf { showAvatar }
            if (user != null) {
                ChatTheme.componentFactory.UserAvatar(
                    params = UserAvatarParams(
                        modifier = Modifier.size(AvatarSize.ExtraSmall),
                        user = user,
                        showIndicator = false,
                        showBorder = false,
                    ),
                )

                Text(
                    modifier = Modifier.weight(1f),
                    text = user.name,
                    color = colors.chatTextUsername,
                    style = typography.captionDefault,
                )
            }

            Text(
                text = ChatTheme.dateFormatter.formatDate(answer.createdAt),
                color = colors.textTertiary,
                style = typography.captionDefault,
            )
        }

        if (showUpdateButton) {
            HorizontalDivider(
                modifier = Modifier.padding(top = StreamTokens.spacingXs),
                color = colors.borderCoreDefault,
            )

            StreamTextButton(
                onClick = onUpdateClick,
                text = stringResource(id = R.string.stream_compose_edit_answer),
                style = StreamButtonStyleDefaults.secondaryGhost,
                size = StreamButtonSize.Small,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
internal fun AddAnswerDialog(
    initMessage: String,
    onDismiss: () -> Unit,
    onNewAnswer: (newOption: String) -> Unit,
) {
    val newOption = remember { mutableStateOf(initMessage) }
    val focusRequester = remember { FocusRequester() }
    AlertDialog(
        title = {
            Text(
                text = stringResource(
                    when (initMessage.isBlank()) {
                        true -> R.string.stream_compose_add_answer
                        false -> R.string.stream_compose_edit_answer
                    },
                ),
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
                    onNewAnswer.invoke(newOption.value)
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

@Preview
@Composable
private fun PollAnswersDialogPreview() {
    ChatTheme {
        val now = Date()
        val pollWithAnswers = PreviewPollData.poll1.copy(
            answers = listOf(
                Answer(
                    id = "preview1",
                    pollId = "",
                    text = "I think we should go with option A, it makes the most sense.",
                    createdAt = now,
                    updatedAt = now,
                    user = PreviewUserData.user1,
                ),
                Answer(
                    id = "preview2",
                    pollId = "",
                    text = "This is my own comment on the poll.",
                    createdAt = now,
                    updatedAt = now,
                    user = PreviewUserData.user2,
                ),
                Answer(
                    id = "preview3",
                    pollId = "",
                    text = "Option B is clearly better!",
                    createdAt = now,
                    updatedAt = now,
                    user = PreviewUserData.user3,
                ),
            ),
        )
        Content(poll = pollWithAnswers)
    }
}
