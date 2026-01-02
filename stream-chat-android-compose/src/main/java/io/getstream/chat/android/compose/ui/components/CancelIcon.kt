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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.clickable

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
    val style = ChatTheme.messageComposerTheme.attachmentCancelIcon
    Icon(
        modifier = modifier
            .then(
                if (style.border != null) {
                    Modifier.border(style.border, style.backgroundShape)
                } else {
                    Modifier
                },
            )
            .padding(2.dp)
            .background(
                shape = style.backgroundShape,
                color = style.backgroundColor,
            )
            .size(20.dp)
            .clickable(
                bounded = false,
                onClick = onClick,
            ),
        painter = style.painter,
        contentDescription = stringResource(id = R.string.stream_compose_cancel),
        tint = style.tint,
    )
}

@Preview
@Composable
private fun CancelIconPreview() {
    ChatTheme {
        CancelIcon {}
    }
}
