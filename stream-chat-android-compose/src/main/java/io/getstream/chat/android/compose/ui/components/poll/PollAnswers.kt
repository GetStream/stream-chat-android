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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.StreamScreenBottomSheet
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun PollAnswersDialog(
    selectedPoll: SelectedPoll,
    listViewModel: MessageListViewModel,
    onDismissRequest: () -> Unit,
    onBackPressed: () -> Unit,
) {
    val user by listViewModel.user.collectAsState()
    val currentUserAnswer = selectedPoll.poll.answers.firstOrNull { it.user?.id == user?.id }
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
    StreamScreenBottomSheet(onDismissRequest = onDismissRequest) {
        Content(
            poll = selectedPoll.poll,
            currentUserAnswer = currentUserAnswer,
            onBackPressed = onBackPressed,
            onAddOrEditClick = { showAddAnswerDialog = true },
        )
    }
}

@Composable
private fun Content(
    poll: Poll,
    currentUserAnswer: Answer? = null,
    onBackPressed: () -> Unit = {},
    onAddOrEditClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .systemBarsPadding()
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
                            painter = painterResource(id = R.drawable.stream_design_ic_edit),
                            contentDescription = stringResource(id = R.string.stream_compose_add_answer),
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
            val showAvatar = poll.votingVisibility == VotingVisibility.PUBLIC

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
        Column(
            // No click handler; merge into one TalkBack stop.
            modifier = Modifier.semantics(mergeDescendants = true) {},
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
            AddAnswerDialogInput(
                newOption = newOption,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
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
        containerColor = ChatTheme.colors.backgroundCoreElevation1,
    )
}

@Composable
private fun AddAnswerDialogInput(newOption: MutableState<String>, modifier: Modifier) {
    InputField(
        value = newOption.value,
        onValueChange = { newOption.value = it },
        modifier = modifier,
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (newOption.value.isEmpty()) {
                    Text(
                        text = stringResource(R.string.stream_compose_add_answer_placeholder),
                        style = ChatTheme.typography.bodyDefault,
                        color = ChatTheme.colors.inputTextPlaceholder,
                    )
                }
                innerTextField()
            }
        },
    )
}

@Preview
@Composable
private fun PollAnswersContentPreview() {
    ChatTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            PollAnswersContent()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PollAnswersContent() {
    StreamScreenBottomSheet(onDismissRequest = {}) {
        Content(poll = previewPollWithAnswers())
    }
}

@Preview
@Composable
private fun PollAnswersWithCurrentUserContentPreview() {
    ChatTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            PollAnswersWithCurrentUserContent()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PollAnswersWithCurrentUserContent() {
    val poll = previewPollWithAnswers()
    val currentUserAnswer = poll.answers.first { it.user?.id == PreviewUserData.user1.id }
    StreamScreenBottomSheet(onDismissRequest = {}) {
        Content(poll = poll, currentUserAnswer = currentUserAnswer)
    }
}

@Preview
@Composable
private fun PollAnswersClosedAnonymousContentPreview() {
    ChatTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            PollAnswersClosedAnonymousContent()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PollAnswersClosedAnonymousContent() {
    StreamScreenBottomSheet(onDismissRequest = {}) {
        Content(
            poll = previewPollWithAnswers().copy(
                closed = true,
                votingVisibility = VotingVisibility.ANONYMOUS,
            ),
        )
    }
}

private fun previewPollWithAnswers(): Poll {
    val previewDate = Date(0)

    return PreviewPollData.poll1.copy(
        answers = listOf(
            Answer(
                id = "preview1",
                pollId = "",
                text = "I think we should go with option A, it makes the most sense.",
                createdAt = previewDate,
                updatedAt = previewDate,
                user = PreviewUserData.user1,
            ),
            Answer(
                id = "preview2",
                pollId = "",
                text = "This is my own comment on the poll.",
                createdAt = previewDate,
                updatedAt = previewDate,
                user = PreviewUserData.user2,
            ),
            Answer(
                id = "preview3",
                pollId = "",
                text = "Option B is clearly better!",
                createdAt = previewDate,
                updatedAt = previewDate,
                user = PreviewUserData.user3,
            ),
        ),
    )
}
