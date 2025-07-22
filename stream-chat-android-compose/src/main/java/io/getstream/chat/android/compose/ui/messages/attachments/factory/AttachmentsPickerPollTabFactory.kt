/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.messages.attachments.factory

import android.content.res.Configuration
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
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentsPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.Poll
import io.getstream.chat.android.compose.ui.messages.attachments.poll.PollCreationDiscardDialog
import io.getstream.chat.android.compose.ui.messages.attachments.poll.PollCreationHeader
import io.getstream.chat.android.compose.ui.messages.attachments.poll.PollOptionItem
import io.getstream.chat.android.compose.ui.messages.attachments.poll.PollOptionList
import io.getstream.chat.android.compose.ui.messages.attachments.poll.PollQuestionInput
import io.getstream.chat.android.compose.ui.messages.attachments.poll.PollSwitchItem
import io.getstream.chat.android.compose.ui.messages.attachments.poll.PollSwitchList
import io.getstream.chat.android.compose.ui.messages.attachments.poll.pollConfigFrom
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.util.extensions.isPollEnabled
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import kotlinx.coroutines.launch

/**
 * Holds the information required to add support for "poll" tab in the attachment picker.
 */
public class AttachmentsPickerPollTabFactory : AttachmentsPickerTabFactory {

    /**
     * The attachment picker mode that this factory handles.
     */
    override val attachmentsPickerMode: AttachmentsPickerMode
        get() = Poll

    override fun isPickerTabEnabled(channel: Channel): Boolean = channel.isPollEnabled()

    /**
     * Emits a file icon for this tab.
     *
     * @param isEnabled If the tab is enabled.
     * @param isSelected If the tab is selected.
     */
    @Composable
    override fun PickerTabIcon(isEnabled: Boolean, isSelected: Boolean) {
        Icon(
            modifier = Modifier.testTag("Stream_AttachmentPickerPollsTab"),
            painter = painterResource(id = R.drawable.stream_compose_ic_poll),
            contentDescription = stringResource(id = R.string.stream_compose_poll_option),
            tint = when {
                isEnabled -> ChatTheme.colors.textLowEmphasis
                else -> ChatTheme.colors.disabled
            },
        )
    }

    /**
     * Emits content that allows users to create a poll in this tab.
     *
     * @param onAttachmentPickerAction A lambda that will be invoked when an action is happened.
     * @param attachments The list of attachments to display.
     * @param onAttachmentsChanged Handler to set the loaded list of attachments to display.
     * @param onAttachmentItemSelected Handler when the item selection state changes.
     * @param onAttachmentsSubmitted Handler to submit the selected attachments to the message composer.
     */
    @Composable
    override fun PickerTabContent(
        onAttachmentPickerAction: (AttachmentPickerAction) -> Unit,
        attachments: List<AttachmentPickerItemState>,
        onAttachmentsChanged: (List<AttachmentPickerItemState>) -> Unit,
        onAttachmentItemSelected: (AttachmentPickerItemState) -> Unit,
        onAttachmentsSubmitted: (List<AttachmentMetaData>) -> Unit,
    ) {
        val coroutineScope = rememberCoroutineScope()
        val questionListLazyState = rememberLazyListState()
        val pollSwitchItemFactory = ChatTheme.pollSwitchitemFactory
        var optionItemList by remember { mutableStateOf(emptyList<PollOptionItem>()) }
        var switchItemList: List<PollSwitchItem> by remember { mutableStateOf(pollSwitchItemFactory.providePollSwitchItemList()) }
        var hasError by remember { mutableStateOf(false) }
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
            val (question, onQuestionChanged) = rememberSaveable { mutableStateOf("") }
            val isEnabled = question.isNotBlank() && optionItemList.any { it.title.isNotBlank() } && !hasError
            val hasChanges = question.isNotBlank() || optionItemList.any { it.title.isNotBlank() }
            var isShowingDiscardDialog by remember { mutableStateOf(false) }

            PollCreationHeader(
                modifier = Modifier.fillMaxWidth(),
                enabledCreation = isEnabled,
                onPollCreateClicked = {
                    onAttachmentPickerAction.invoke(
                        AttachmentPickerPollCreation(
                            pollConfigFrom(
                                pollQuestion = question,
                                pollOptions = optionItemList,
                                pollSwitches = switchItemList,
                            ),
                        ),
                    )
                    onAttachmentPickerAction.invoke(AttachmentPickerBack)
                },
                onBackPressed = {
                    if (!hasChanges) {
                        onAttachmentPickerAction.invoke(AttachmentPickerBack)
                    } else {
                        isShowingDiscardDialog = true
                    }
                },
            )

            PollQuestionInput(
                question = question,
                onQuestionChanged = onQuestionChanged,
            )

            PollOptionList(
                lazyListState = questionListLazyState,
                onQuestionsChanged = {
                    optionItemList = it
                    hasError = hasError(optionItemList, switchItemList)
                },
            )

            Spacer(modifier = Modifier.height(32.dp))

            PollSwitchList(
                pollSwitchItems = switchItemList,
                onSwitchesChanged = {
                    switchItemList = it
                    hasError = hasError(optionItemList, switchItemList)
                },
            )

            BackHandler(enabled = hasChanges) { isShowingDiscardDialog = true }

            if (isShowingDiscardDialog) {
                PollCreationDiscardDialog(
                    onCancelClicked = { isShowingDiscardDialog = false },
                    onDiscardClicked = {
                        isShowingDiscardDialog = false
                        onAttachmentPickerAction.invoke(AttachmentPickerBack)
                    },
                )
            }
        }
    }
}

/**
 * Checks if there are any errors in the 'options' list, or any errors or missing fields in the 'switches' list.
 */
private fun hasError(
    options: List<PollOptionItem>,
    switches: List<PollSwitchItem>,
): Boolean {
    // Check errors in options
    val hasErrorInOptions = options.fastAny { item ->
        item.pollOptionError != null
    }
    // Check errors or missing fields in switches
    val hasErrorInSwitches = switches.fastAny { item ->
        val hasError = item.pollOptionError != null
        val isMissingMandatoryInput = item.enabled &&
            item.pollSwitchInput != null &&
            item.pollSwitchInput.value.toString().isEmpty()
        hasError || isMissingMandatoryInput
    }
    return hasErrorInOptions || hasErrorInSwitches
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AttachmentsPickerPollTabFactoryContentPreview() {
    ChatTheme {
        AttachmentsPickerPollTabFactory().PickerTabContent(
            onAttachmentPickerAction = {},
            attachments = emptyList(),
            onAttachmentsChanged = {},
            onAttachmentItemSelected = {},
        ) {}
    }
}
