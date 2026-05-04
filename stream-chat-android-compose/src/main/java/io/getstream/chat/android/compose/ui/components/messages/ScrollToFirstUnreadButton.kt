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

package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.clickable

private val IconSize = 16.dp

/**
 * A floating pill anchored at the top of the message list that lets the user jump to the first
 * unread message when it sits outside the viewport.
 *
 * The pill exposes two interactions: tapping the label area scrolls to the first unread message,
 * and tapping the trailing close (X) icon dismisses the pill without scrolling.
 *
 * @param unreadCount The number of unread messages to display in the label.
 * @param modifier The modifier used for styling.
 * @param onClick The handler triggered when the user taps the label area.
 * @param onDismiss The handler triggered when the user taps the close (X) icon.
 */
@Composable
internal fun ScrollToFirstUnreadButton(
    unreadCount: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    Surface(
        modifier = modifier
            .shadow(StreamTokens.spacing2xs, shape = CircleShape)
            .testTag("Stream_ScrollToFirstUnreadButton"),
        shape = CircleShape,
        color = ChatTheme.colors.backgroundCoreElevation1,
        contentColor = ChatTheme.colors.textPrimary,
        border = BorderStroke(StreamTokens.borderStrokeSubtle, ChatTheme.colors.borderCoreSubtle),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .clickable(role = Role.Button, onClick = onClick)
                    .padding(vertical = StreamTokens.spacingXs)
                    .padding(start = StreamTokens.spacingSm, end = StreamTokens.spacingXs),
                horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier.size(IconSize),
                    painter = painterResource(R.drawable.stream_design_ic_arrow_up),
                    contentDescription = stringResource(R.string.stream_compose_scroll_to_first_unread),
                )
                Text(
                    text = pluralStringResource(
                        id = R.plurals.stream_compose_scroll_to_first_unread_count,
                        count = unreadCount,
                        unreadCount,
                    ),
                    style = ChatTheme.typography.metadataEmphasis,
                )
            }
            VerticalDivider(
                thickness = StreamTokens.borderStrokeSubtle,
                color = ChatTheme.colors.borderCoreSubtle,
            )
            Icon(
                modifier = Modifier
                    .clickable(role = Role.Button, onClick = onDismiss)
                    .testTag("Stream_ScrollToFirstUnreadButton_Dismiss")
                    .padding(vertical = StreamTokens.spacingXs)
                    .padding(start = StreamTokens.spacingXs, end = StreamTokens.spacingSm)
                    .size(IconSize),
                painter = painterResource(R.drawable.stream_design_ic_xmark),
                contentDescription = stringResource(R.string.stream_compose_scroll_to_first_unread_dismiss),
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    ChatTheme {
        ScrollToFirstUnreadButton(
            unreadCount = 9,
        )
    }
}
