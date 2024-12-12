/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * SnackBar shown when the user denies the access to the storage permanently, and requests the storage access
 * again.
 *
 * @param hostState The state of the snackbar.
 * @param onActionClick Action invoked when the user click the action on the snackbar.
 */
@Composable
internal fun PermissionPermanentlyDeniedSnackBar(
    hostState: SnackbarHostState,
    onActionClick: () -> Unit,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .wrapContentHeight(Alignment.Bottom),
    ) { data ->
        Snackbar(
            content = {
                Text(
                    text = data.message,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            },
            action = data.actionLabel?.let {
                {
                    TextButton(
                        colors = ButtonDefaults.textButtonColors(contentColor = ChatTheme.colors.primaryAccent),
                        onClick = onActionClick,
                    ) {
                        Text(text = it)
                    }
                }
            },
        )
    }
}
