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

package io.getstream.chat.android.compose.ui.channel.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.animation.FadingVisibility
import io.getstream.chat.android.compose.ui.util.clickable

@Suppress("LongMethod")
@Composable
internal fun ChannelInfoNameField(
    name: String,
    readOnly: Boolean,
    onConfirmRenaming: (name: String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var value by remember { mutableStateOf(name) }
    var showEditingButtons by remember { mutableStateOf(false) }
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { showEditingButtons = it.isFocused && !readOnly },
        readOnly = readOnly,
        textStyle = ChatTheme.typography.body,
        prefix = {
            Text(
                modifier = Modifier.padding(end = 8.dp),
                text = stringResource(R.string.stream_ui_channel_info_name_field_label),
                style = ChatTheme.typography.bodyBold,
                color = ChatTheme.colors.textSecondary,
            )
        },
        placeholder = {
            Text(
                text = stringResource(R.string.stream_ui_channel_info_name_field_placeholder),
                style = ChatTheme.typography.body,
            )
        },
        value = value,
        trailingIcon = {
            FadingVisibility(visible = showEditingButtons) {
                Row(
                    modifier = Modifier.padding(end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        modifier = Modifier.clickable(bounded = false) {
                            value = name
                            focusManager.clearFocus()
                        },
                        imageVector = Icons.Rounded.Close,
                        tint = ChatTheme.colors.textSecondary,
                        contentDescription = null,
                    )
                    Icon(
                        modifier = Modifier.clickable(bounded = false) {
                            onConfirmRenaming(value)
                            focusManager.clearFocus()
                        },
                        imageVector = Icons.Rounded.Done,
                        tint = ChatTheme.colors.accentPrimary,
                        contentDescription = null,
                    )
                }
            }
        },
        singleLine = true,
        onValueChange = { value = it },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = ChatTheme.colors.textPrimary,
            unfocusedTextColor = ChatTheme.colors.textPrimary,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
        ),
    )
}
