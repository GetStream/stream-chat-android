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

package io.getstream.chat.android.compose.sample.ui.location

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.utils.message.hasSharedLocation
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.compose.sample.ui.component.SharedLocationItem
import io.getstream.chat.android.compose.sample.vm.SharedLocationViewModelFactory
import io.getstream.chat.android.compose.ui.theme.AttachmentPickerContentParams
import io.getstream.chat.android.compose.ui.theme.AttachmentTypePickerParams
import io.getstream.chat.android.compose.ui.theme.ChatComponentFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageContentParams

/**
 * Factory for creating components related to location sharing.
 */
class LocationComponentFactory(
    private val locationViewModelFactory: SharedLocationViewModelFactory?,
) : ChatComponentFactory {

    @Composable
    override fun AttachmentTypePicker(params: AttachmentTypePickerParams) {
        super.AttachmentTypePicker(
            params = AttachmentTypePickerParams(
                channel = params.channel,
                messageMode = params.messageMode,
                selectedMode = params.selectedMode,
                onModeSelected = params.onModeSelected,
                trailingContent = {
                    val isSelected = params.selectedMode is LocationPickerMode

                    FilledIconToggleButton(
                        modifier = Modifier.size(48.dp),
                        checked = isSelected,
                        onCheckedChange = { params.onModeSelected(LocationPickerMode) },
                        colors = IconButtonDefaults.filledIconToggleButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = ChatTheme.colors.buttonSecondaryText,
                            checkedContainerColor = ChatTheme.colors.backgroundUtilitySelected,
                            checkedContentColor = ChatTheme.colors.buttonSecondaryText,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = "Share Location",
                        )
                    }
                },
            ),
        )
    }

    @Composable
    override fun AttachmentPickerContent(params: AttachmentPickerContentParams) {
        if (params.pickerMode == LocationPickerMode && locationViewModelFactory != null) {
            LocationPicker(
                viewModelFactory = locationViewModelFactory,
                onDismiss = params.actions.onDismiss,
            )
        } else {
            super.AttachmentPickerContent(params)
        }
    }

    @Composable
    override fun MessageContent(params: MessageContentParams) {
        val message = params.messageItem.message
        if (message.hasSharedLocation() && !message.isDeleted()) {
            val location = requireNotNull(message.sharedLocation)
            SharedLocationItem(
                modifier = Modifier.widthIn(max = 250.dp),
                message = message,
                location = location,
                onMapClick = { url -> params.onLinkClick?.invoke(message, url) },
                onMapLongClick = { params.onLongItemClick(message) },
            )
        } else {
            super.MessageContent(params)
        }
    }
}
