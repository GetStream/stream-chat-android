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

package io.getstream.chat.android.compose.ui.messages.attachments.factory

import android.Manifest
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.uiutils.util.openSystemSettings

/**
 * Shows the UI if we're missing permissions to fetch data for attachments.
 * The UI explains to the user which permission is missing and why we need it.
 *
 * @param permissionState A state object to control and observe permission status changes.
 * @param modifier A [Modifier] for external customisation.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun MissingPermissionContent(
    permissionState: PermissionState,
    modifier: Modifier = Modifier,
) {
    val title = when (permissionState.permission) {
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_AUDIO,
        Manifest.permission.READ_MEDIA_VIDEO,
        -> R.string.stream_ui_message_composer_permission_storage_title
        else -> R.string.stream_ui_message_composer_permission_camera_title
    }

    val message = when (permissionState.permission) {
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_AUDIO,
        Manifest.permission.READ_MEDIA_VIDEO,
        -> R.string.stream_ui_message_composer_permission_storage_message
        else -> R.string.stream_ui_message_composer_permission_camera_message
    }

    val context = LocalContext.current

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            style = ChatTheme.typography.title3Bold,
            text = stringResource(id = title),
            color = ChatTheme.colors.textHighEmphasis,
        )

        Spacer(modifier = Modifier.size(16.dp))

        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            style = ChatTheme.typography.body,
            text = stringResource(id = message),
            textAlign = TextAlign.Center,
            color = ChatTheme.colors.textLowEmphasis,
        )

        Spacer(modifier = Modifier.size(16.dp))

        TextButton(
            colors = ButtonDefaults.textButtonColors(contentColor = ChatTheme.colors.primaryAccent),
            onClick = { context.openSystemSettings() },
        ) {
            Text(stringResource(id = R.string.stream_compose_grant_permission))
        }
    }
}
