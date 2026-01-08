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

package io.getstream.chat.android.compose.ui.messages.attachments.poll

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentPickerAction
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentPickerBack
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentPickerPollCreation
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import kotlinx.coroutines.launch

/**
 * Screen for creating a new poll with options and configuration switches.
 *
 * @param onAttachmentPickerAction Callback invoked when an attachment picker action occurs.
 */
@Suppress("LongMethod")
@Composable
public fun CreatePollScreen(
    onAttachmentPickerAction: (AttachmentPickerAction) -> Unit,
) {
    val viewModel: CreatePollViewModel = viewModel(
        factory = CreatePollViewModelFactory(ChatTheme.pollSwitchitemFactory.providePollSwitchItemList()),
    )
    val backAction = {
        // Invoke reset() - important to clear the ViewModel state. (the view model can be persisted across opening and
        // closing the attachment picker, because it is not a modal one).
        viewModel.reset()
        onAttachmentPickerAction(AttachmentPickerBack)
    }
    val state by viewModel.state.collectAsState()
    var isShowingDiscardDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val questionListLazyState = rememberLazyListState()

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = -available.y
                coroutineScope.launch {
                    questionListLazyState.scrollBy(delta)
                }
                return Offset.Zero
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .nestedScroll(nestedScrollConnection)
            .verticalScroll(rememberScrollState())
            .background(ChatTheme.colors.appBackground),
    ) {
        // Header
        PollCreationHeader(
            modifier = Modifier.fillMaxWidth(),
            enabledCreation = state.isCreationEnabled,
            onPollCreateClicked = {
                onAttachmentPickerAction(
                    AttachmentPickerPollCreation(
                        pollConfigFrom(
                            pollQuestion = state.question,
                            pollOptions = state.optionItemList,
                            pollSwitches = state.switchItemList,
                        ),
                    ),
                )
                backAction()
            },
            onBackPressed = {
                if (!state.hasChanges) {
                    backAction()
                } else {
                    isShowingDiscardDialog = true
                }
            },
        )

        // Poll question input
        PollQuestionInput(
            question = state.question,
            onQuestionChanged = viewModel::updateQuestion,
        )

        // Options list
        PollOptionList(
            lazyListState = questionListLazyState,
            optionItems = state.optionItemList,
            onQuestionsChanged = viewModel::updateOptions,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Poll configuration switches
        PollSwitchList(
            pollSwitchItems = state.switchItemList,
            onSwitchesChanged = viewModel::updateSwitches,
        )

        // Back handler
        BackHandler(enabled = state.hasChanges) {
            isShowingDiscardDialog = true
        }

        // Discard dialog
        if (isShowingDiscardDialog) {
            PollCreationDiscardDialog(
                onCancelClicked = { isShowingDiscardDialog = false },
                onDiscardClicked = {
                    isShowingDiscardDialog = false
                    backAction()
                },
            )
        }
    }
}
