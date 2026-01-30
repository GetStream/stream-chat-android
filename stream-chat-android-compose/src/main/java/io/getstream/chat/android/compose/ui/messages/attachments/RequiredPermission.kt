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

package io.getstream.chat.android.compose.ui.messages.attachments

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.ui.common.utils.openSystemSettings

@Composable
internal fun RequiredStoragePermission(
    modifier: Modifier = Modifier,
    onGrantPermissionClick: () -> Unit,
) {
    RequiredPermission(
        modifier = modifier,
        icon = R.drawable.stream_compose_ic_attachment_media_picker,
        title = R.string.stream_ui_message_composer_permission_storage_title,
        message = R.string.stream_ui_message_composer_permission_storage_message,
        onGrantPermissionClick = onGrantPermissionClick,
    )
}

@Composable
internal fun RequiredCameraPermission(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    RequiredPermission(
        modifier = modifier,
        icon = R.drawable.stream_compose_ic_attachment_camera_picker,
        title = R.string.stream_ui_message_composer_permission_camera_title,
        message = R.string.stream_ui_message_composer_permission_camera_message,
        onGrantPermissionClick = context::openSystemSettings,
    )
}

@Composable
private fun RequiredPermission(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    @StringRes title: Int,
    @StringRes message: Int,
    onGrantPermissionClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .padding(
                start = StreamTokens.spacing2xl,
                end = StreamTokens.spacing2xl,
                bottom = StreamTokens.spacing3xl,
            )
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs, Alignment.CenterVertically),
        ) {
            Icon(
                modifier = Modifier.size(32.dp),
                painter = painterResource(icon),
                contentDescription = null,
                tint = ChatTheme.colors.textTertiary,
            )
            Text(
                text = stringResource(title),
                style = ChatTheme.typography.headingSm,
                color = ChatTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(message),
                style = ChatTheme.typography.captionDefault,
                color = ChatTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
        OutlinedButton(
            modifier = Modifier
                .height(48.dp)
                .fillMaxWidth(),
            onClick = onGrantPermissionClick,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = ChatTheme.colors.buttonSecondaryText,
            ),
        ) {
            Text(stringResource(id = R.string.stream_ui_message_composer_grant_permission_button))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RequiredStoragePermissionPreview() {
    ChatTheme {
        RequiredStoragePermission()
    }
}

@Composable
internal fun RequiredStoragePermission() {
    RequiredStoragePermission {}
}

@Preview(showBackground = true)
@Composable
private fun RequiredCameraPermissionPreview() {
    ChatTheme {
        RequiredCameraPermission()
    }
}

@Composable
internal fun RequiredCameraPermission() {
    RequiredCameraPermission(modifier = Modifier)
}
