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

package io.getstream.chat.android.compose.ui.messages.attachments

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentsPickerMode
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentPickerAction
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentPickerBack
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentPickerPollCreation
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentsPickerTabFactory
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentsPickerTabFactoryFilter
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.mirrorRtl
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentsPickerViewModel
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import kotlinx.coroutines.flow.collectLatest

/**
 * Represents the bottom bar UI that allows users to pick attachments. The picker renders its
 * tabs based on the [tabFactories] parameter. Out of the box we provide factories for images,
 * files and media capture tabs.
 *
 * @param attachmentsPickerViewModel ViewModel that loads the images or files and persists which
 * items have been selected.
 * @param onAttachmentPickerAction A lambda that will be invoked when an action is happened.
 * @param onAttachmentsSelected Handler when attachments are selected and confirmed by the user.
 * @param onDismiss Handler when the user dismisses the UI.
 * @param modifier Modifier for styling.
 * @param tabFactories The list of attachment picker tab factories.
 * @param shape The shape of the dialog.
 * @param messageMode The message mode, used to determine if the default "Polls" tab is enabled.
 */
@Suppress("LongMethod")
@Composable
public fun AttachmentsPicker(
    attachmentsPickerViewModel: AttachmentsPickerViewModel,
    onAttachmentsSelected: (List<Attachment>) -> Unit,
    onTabClick: (Int, AttachmentsPickerMode) -> Unit,
    onAttachmentPickerAction: (AttachmentPickerAction) -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    tabFactories: List<AttachmentsPickerTabFactory> = ChatTheme.attachmentsPickerTabFactories,
    shape: Shape = ChatTheme.shapes.bottomSheet,
    messageMode: MessageMode = MessageMode.Normal,
) {
    val saveAttachmentsOnDismiss = ChatTheme.attachmentPickerTheme.saveAttachmentsOnDismiss
    val dismissAction = {
        if (saveAttachmentsOnDismiss) {
            onAttachmentsSelected(attachmentsPickerViewModel.getSelectedAttachments())
        }
        onDismiss()
    }
    BackHandler(onBack = dismissAction)
    // Cross-validate requested tabFactories with the allowed ones from BE
    val filter = remember { AttachmentsPickerTabFactoryFilter() }
    val allowedFactories = filter.filterAllowedFactories(tabFactories, attachmentsPickerViewModel.channel, messageMode)
    val defaultTabIndex = allowedFactories
        .indexOfFirst { it.isPickerTabEnabled(attachmentsPickerViewModel.channel) }
        .takeIf { it >= 0 }
        ?: 0
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(defaultTabIndex) }

    Box(
        modifier = Modifier
            .background(ChatTheme.attachmentPickerTheme.backgroundOverlay)
            .safeDrawingPadding()
            .fillMaxSize()
            .clickable(
                onClick = dismissAction,
                indication = null,
                interactionSource = null,
            ),
    ) {
        Card(
            modifier = modifier.clickable(
                indication = null,
                onClick = {},
                interactionSource = null,
            ),
            shape = if (attachmentsPickerViewModel.attachmentsPickerMode.isFullContent) {
                RoundedCornerShape(0.dp)
            } else {
                shape
            },
            colors = CardDefaults.cardColors(containerColor = ChatTheme.attachmentPickerTheme.backgroundSecondary),
        ) {
            Column {
                if (!attachmentsPickerViewModel.attachmentsPickerMode.isFullContent) {
                    AttachmentPickerOptions(
                        hasPickedAttachments = attachmentsPickerViewModel.hasPickedAttachments,
                        tabFactories = allowedFactories,
                        tabIndex = selectedTabIndex,
                        channel = attachmentsPickerViewModel.channel,
                        onTabClick = { index, attachmentPickerMode ->
                            onTabClick.invoke(index, attachmentPickerMode)
                            selectedTabIndex = index
                            attachmentsPickerViewModel.changeAttachmentPickerMode(attachmentPickerMode) { false }
                        },
                        onSendAttachmentsClick = {
                            onAttachmentsSelected(attachmentsPickerViewModel.getSelectedAttachments())
                        },
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = if (attachmentsPickerViewModel.attachmentsPickerMode.isFullContent) {
                        RoundedCornerShape(0.dp)
                    } else {
                        shape
                    },
                    color = ChatTheme.attachmentPickerTheme.backgroundPrimary,
                ) {
                    // Listen for attachments to be ready for upload
                    LaunchedEffect(attachmentsPickerViewModel) {
                        attachmentsPickerViewModel.attachmentsForUpload.collectLatest {
                            onAttachmentsSelected(it)
                        }
                    }
                    // Tab content
                    AnimatedContent(targetState = selectedTabIndex, label = "") {
                        allowedFactories.getOrNull(it)
                            ?.PickerTabContent(
                                onAttachmentPickerAction = { pickerAction ->
                                    when (pickerAction) {
                                        AttachmentPickerBack -> dismissAction()
                                        is AttachmentPickerPollCreation -> onAttachmentPickerAction.invoke(pickerAction)
                                    }
                                },
                                attachments = attachmentsPickerViewModel.attachments,
                                onAttachmentItemSelected = attachmentsPickerViewModel::changeSelectedAttachments,
                                onAttachmentsChanged = { attachmentsPickerViewModel.attachments = it },
                                onAttachmentsSubmitted = {
                                    attachmentsPickerViewModel.getAttachmentsFromMetadataAsync(it)
                                },
                            )
                    }
                }
            }
        }
    }
}

/**
 * The options for the Attachment picker. Shows tabs based on the provided list of [tabFactories]
 * and a button to submit the selected attachments.
 *
 * @param hasPickedAttachments If we selected any attachments in the currently selected tab.
 * @param tabFactories The list of factories to build tab icons.
 * @param tabIndex The index of the tab that we selected.
 * @param channel The channel where the attachments picker is being used.
 * @param onTabClick Handler for clicking on any of the tabs, to change the shown attachments.
 * @param onSendAttachmentsClick Handler when confirming the picked attachments.
 */
@Suppress("LongParameterList")
@Composable
internal fun AttachmentPickerOptions(
    hasPickedAttachments: Boolean,
    tabFactories: List<AttachmentsPickerTabFactory>,
    tabIndex: Int,
    channel: Channel,
    onTabClick: (Int, AttachmentsPickerMode) -> Unit,
    onSendAttachmentsClick: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.SpaceEvenly) {
            tabFactories.forEachIndexed { index, tabFactory ->

                val isSelected = index == tabIndex
                val isEnabled = isSelected || (!hasPickedAttachments && tabFactory.isPickerTabEnabled(channel))

                IconButton(
                    enabled = isEnabled,
                    content = {
                        tabFactory.PickerTabIcon(
                            isEnabled = isEnabled,
                            isSelected = isSelected,
                        )
                    },
                    onClick = { onTabClick(index, tabFactory.attachmentsPickerMode) },
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        ChatTheme.componentFactory.AttachmentsPickerSendButton(
            hasPickedAttachments = hasPickedAttachments,
            onClick = onSendAttachmentsClick,
        )
    }
}

/**
 * The default "Send" button in the attachments picker heading.
 *
 * @param hasPickedAttachments Indicator if there are selected attachments.
 * @param onClick The action to be taken when the button is clicked.
 */
@Composable
internal fun DefaultAttachmentsPickerSendButton(
    hasPickedAttachments: Boolean,
    onClick: () -> Unit,
) {
    IconButton(
        enabled = hasPickedAttachments,
        onClick = onClick,
        content = {
            val layoutDirection = LocalLayoutDirection.current
            Icon(
                modifier = Modifier
                    .mirrorRtl(layoutDirection = layoutDirection)
                    .testTag("Stream_AttachmentPickerSendButton"),
                painter = painterResource(id = R.drawable.stream_compose_ic_left),
                contentDescription = stringResource(id = R.string.stream_compose_send_attachment),
                tint = if (hasPickedAttachments) {
                    ChatTheme.colors.primaryAccent
                } else {
                    ChatTheme.colors.textLowEmphasis
                },
            )
        },
    )
}
