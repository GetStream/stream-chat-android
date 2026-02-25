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

package io.getstream.chat.android.compose.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Generic dialog component that allows us to prompt the user.
 *
 * @param title Title for the dialog.
 * @param message Message for the dialog.
 * @param onPositiveAction Handler when the user confirms the dialog.
 * @param onDismiss Handler when the user dismisses the dialog.
 * @param modifier Modifier for styling.
 * @param showDismissButton If we should show the dismiss button or not.
 */
@Composable
public fun SimpleDialog(
    title: String,
    message: String,
    onPositiveAction: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    showDismissButton: Boolean = true,
) {
    SimpleDialog(
        title = title,
        text = {
            Text(
                text = message,
                style = ChatTheme.typography.bodyDefault,
            )
        },
        onConfirmClick = onPositiveAction,
        onDismiss = onDismiss,
        modifier = modifier,
        showDismissButton = showDismissButton,
    )
}

@Composable
internal fun SimpleDialog(
    title: String,
    text: @Composable () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    showDismissButton: Boolean = true,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = ChatTheme.typography.headingMedium,
            )
        },
        text = text,
        confirmButton = {
            TextButton(
                colors = ButtonDefaults.textButtonColors(contentColor = ChatTheme.colors.accentPrimary),
                onClick = onConfirmClick,
            ) {
                Text(text = stringResource(id = R.string.stream_compose_ok))
            }
        },
        dismissButton = if (showDismissButton) {
            {
                TextButton(
                    colors = ButtonDefaults.textButtonColors(contentColor = ChatTheme.colors.accentPrimary),
                    onClick = onDismiss,
                ) {
                    Text(text = stringResource(id = R.string.stream_compose_cancel))
                }
            }
        } else {
            null
        },
        titleContentColor = ChatTheme.colors.textPrimary,
        textContentColor = ChatTheme.colors.textPrimary,
        containerColor = ChatTheme.colors.backgroundElevationElevation1,
    )
}
