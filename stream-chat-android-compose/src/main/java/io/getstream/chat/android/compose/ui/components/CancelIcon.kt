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

package io.getstream.chat.android.compose.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Represents a simple cancel icon that is used primarily for attachments.
 *
 * @param modifier Modifier for styling.
 * @param onClick Handler when the user clicks on the icon.
 */
@Composable
public fun CancelIcon(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Icon(
        modifier = modifier
            .background(
                shape = ChatTheme.messageComposerTheme.attachmentCancelIcon.backgroundShape,
                color = ChatTheme.messageComposerTheme.attachmentCancelIcon.backgroundColor,
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            ),
        painter = ChatTheme.messageComposerTheme.attachmentCancelIcon.painter,
        contentDescription = stringResource(id = R.string.stream_compose_cancel),
        tint = ChatTheme.messageComposerTheme.attachmentCancelIcon.tint,
    )
}
