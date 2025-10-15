/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.sample.ui.location

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ShareLocation
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import io.getstream.chat.android.compose.sample.vm.SharedLocationViewModelFactory
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentsPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.CustomPickerMode
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentPickerAction
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentPickerBack
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentsPickerTabFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData

internal class LocationPickerTabFactory(
    private val viewModelFactory: SharedLocationViewModelFactory,
) : AttachmentsPickerTabFactory {

    override val attachmentsPickerMode: AttachmentsPickerMode =
        CustomPickerMode()

    override fun isPickerTabEnabled(channel: Channel): Boolean =
        channel.config.sharedLocationsEnabled

    @Composable
    override fun PickerTabIcon(isEnabled: Boolean, isSelected: Boolean) {
        Icon(
            imageVector = Icons.Rounded.ShareLocation,
            contentDescription = "Share Location",
            tint = when {
                isSelected -> ChatTheme.colors.primaryAccent
                isEnabled -> ChatTheme.colors.textLowEmphasis
                else -> ChatTheme.colors.disabled
            },
        )
    }

    @Composable
    override fun PickerTabContent(
        onAttachmentPickerAction: (AttachmentPickerAction) -> Unit,
        attachments: List<AttachmentPickerItemState>,
        onAttachmentsChanged: (List<AttachmentPickerItemState>) -> Unit,
        onAttachmentItemSelected: (AttachmentPickerItemState) -> Unit,
        onAttachmentsSubmitted: (List<AttachmentMetaData>) -> Unit,
    ) {
        LocationPicker(
            viewModelFactory = viewModelFactory,
            onDismiss = { onAttachmentPickerAction(AttachmentPickerBack) },
        )
    }
}
