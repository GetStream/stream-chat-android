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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.composer.InputField
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.models.Answer
import io.getstream.chat.android.models.VotingVisibility
import io.getstream.chat.android.ui.common.state.messages.poll.SelectedPoll
import io.getstream.chat.android.ui.common.utils.extensions.initials

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
    val showAddAnswerDialog = remember { mutableStateOf(false) }
    if (showAddAnswerDialog.value) {
        AddAnswerDialog(
            initMessage = currentUserAnswer?.text ?: "",
            onDismiss = { showAddAnswerDialog.value = false },
            onNewAnswer = { newAnswer ->
                listViewModel.castAnswer(selectedPoll.message, selectedPoll.poll, newAnswer)
            },
        )
    }
    Popup(
        alignment = Alignment.BottomCenter,
        onDismissRequest = onDismissRequest,
    ) {
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
            val poll = selectedPoll.poll

            BackHandler { onBackPressed.invoke() }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ChatTheme.colors.appBackground),
            ) {
                item {
                    PollDialogHeader(
                        title = stringResource(id = R.string.stream_compose_poll_answers),
                        onBackPressed = onBackPressed,
                    )
                }

                items(
                    items = poll.answers,
                    key = { answer -> answer.id },
                ) { answer ->
                    PollAnswersItem(
                        answer = answer,
                        showAvatar = (poll.votingVisibility == VotingVisibility.PUBLIC) || showAnonymousAvatar,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                if (!poll.closed) {
                    item {
                        Box(
                            modifier = Modifier
                                .clickable { showAddAnswerDialog.value = true }
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .background(
                                    color = ChatTheme.colors.inputBackground,
                                    shape = RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 12.dp,
                                        bottomStart = 12.dp,
                                        bottomEnd = 12.dp,
                                    ),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 11.dp),
                                text = stringResource(
                                    id = when (currentUserAnswer == null) {
                                        true -> R.string.stream_compose_add_answer
                                        false -> R.string.stream_compose_edit_answer
                                    },
                                ),
                                textAlign = TextAlign.Center,
                                color = ChatTheme.colors.primaryAccent,
                                fontSize = 16.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun PollAnswersItem(
    answer: Answer,
    showAvatar: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(
                color = ChatTheme.colors.inputBackground,
                shape = RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp,
                    bottomStart = 12.dp,
                    bottomEnd = 12.dp,
                ),
            )
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                text = answer.text,
                color = ChatTheme.colors.textHighEmphasis,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            val user = answer.user?.takeIf { showAvatar }
            if (user != null) {
                ChatTheme.componentFactory.Avatar(
                    modifier = Modifier.size(20.dp),
                    imageUrl = user.image,
                    initials = user.initials,
                    shape = ChatTheme.shapes.avatar,
                    textStyle = ChatTheme.typography.title3Bold,
                    placeholderPainter = null,
                    contentDescription = user.name,
                    initialsAvatarOffset = DpOffset.Zero,
                    onClick = null,
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

            Text(
                modifier = Modifier.padding(bottom = 2.dp),
                text = ChatTheme.dateFormatter.formatDate(answer.createdAt),
                color = ChatTheme.colors.textLowEmphasis,
                fontSize = 14.sp,
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
                    onNewAnswer.invoke(newOption.value)
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
