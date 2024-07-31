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

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentsPickerMode
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentPickerAction
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentPickerBack
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentPickerPollCreation
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentsPickerTabFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.mirrorRtl
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentsPickerViewModel
import io.getstream.chat.android.models.Attachment

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
 */
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
) {
    val defaultTabIndex = tabFactories.indexOfFirst { it.isPickerTabEnabled() }.takeIf { it >= 0 } ?: 0
    var selectedTabIndex by remember { mutableIntStateOf(defaultTabIndex) }
    var selectedAttachmentsPickerMode: AttachmentsPickerMode? by remember { mutableStateOf(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ChatTheme.attachmentPickerTheme.backgroundOverlay)
            .clickable(
                onClick = onDismiss,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ),
    ) {
        Card(
            modifier = modifier.clickable(
                indication = null,
                onClick = {},
                interactionSource = remember { MutableInteractionSource() },
            ),
            elevation = 4.dp,
            shape = if (selectedAttachmentsPickerMode?.isFullContent == true) {
                RoundedCornerShape(0.dp)
            } else {
                shape
            },
            backgroundColor = ChatTheme.attachmentPickerTheme.backgroundSecondary,
        ) {
            Column {
                if (selectedAttachmentsPickerMode == null || selectedAttachmentsPickerMode?.isFullContent == false) {
                    AttachmentPickerOptions(
                        hasPickedAttachments = attachmentsPickerViewModel.hasPickedAttachments,
                        tabFactories = tabFactories,
                        tabIndex = selectedTabIndex,
                        onTabClick = { index, attachmentPickerMode ->
                            onTabClick.invoke(index, attachmentPickerMode)
                            selectedTabIndex = index
                            selectedAttachmentsPickerMode = attachmentPickerMode
                            attachmentsPickerViewModel.changeAttachmentPickerMode(attachmentPickerMode) { false }
                        },
                        onSendAttachmentsClick = {
                            onAttachmentsSelected(attachmentsPickerViewModel.getSelectedAttachments())
                        },
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = if (selectedAttachmentsPickerMode?.isFullContent == true) {
                        RoundedCornerShape(0.dp)
                    } else {
                        shape
                    },
                    color = ChatTheme.attachmentPickerTheme.backgroundPrimary,
                ) {
                    AnimatedContent(targetState = selectedTabIndex, label = "") {
                        tabFactories.getOrNull(it)
                            ?.PickerTabContent(
                                onAttachmentPickerAction = { pickerAction ->
                                    when (pickerAction) {
                                        AttachmentPickerBack -> onDismiss.invoke()
                                        is AttachmentPickerPollCreation -> onAttachmentPickerAction.invoke(pickerAction)
                                    }
                                },
                                attachments = attachmentsPickerViewModel.attachments,
                                onAttachmentItemSelected = attachmentsPickerViewModel::changeSelectedAttachments,
                                onAttachmentsChanged = { attachmentsPickerViewModel.attachments = it },
                                onAttachmentsSubmitted = {
                                    onAttachmentsSelected(attachmentsPickerViewModel.getAttachmentsFromMetaData(it))
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
 * @param onTabClick Handler for clicking on any of the tabs, to change the shown attachments.
 * @param onSendAttachmentsClick Handler when confirming the picked attachments.
 */
@Composable
private fun AttachmentPickerOptions(
    hasPickedAttachments: Boolean,
    tabFactories: List<AttachmentsPickerTabFactory>,
    tabIndex: Int,
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
                val isEnabled = isSelected || (!hasPickedAttachments && tabFactory.isPickerTabEnabled())

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

        IconButton(
            enabled = hasPickedAttachments,
            onClick = onSendAttachmentsClick,
            content = {
                val layoutDirection = LocalLayoutDirection.current

                Icon(
                    modifier = Modifier
                        .weight(1f)
                        .mirrorRtl(layoutDirection = layoutDirection),
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
}
