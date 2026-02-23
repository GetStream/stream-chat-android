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

package io.getstream.chat.android.compose.ui.components.composer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.MessageAction

/**
 * Shows the options "header" for the message input component. This renders the currently active
 * message action if it's [Edit].
 *
 * @param modifier Modifier for styling.
 * @param activeAction Currently active [MessageAction].
 * @param onCancelAction Handler when the user cancels the current action.
 */
@Composable
public fun MessageInputOptions(
    activeAction: MessageAction,
    onCancelAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (activeAction !is Edit) return

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.stream_compose_ic_edit),
            contentDescription = null,
            tint = ChatTheme.colors.textSecondary,
        )

        Text(
            text = stringResource(id = R.string.stream_compose_edit_message),
            style = ChatTheme.typography.bodyBold,
            color = ChatTheme.colors.textPrimary,
        )

        IconButton(
            modifier = Modifier
                .testTag("Stream_ComposerCancelButton"),
            onClick = onCancelAction,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.stream_compose_ic_close),
                contentDescription = stringResource(id = R.string.stream_compose_cancel),
                tint = ChatTheme.colors.textSecondary,
            )
        }
    }
}
