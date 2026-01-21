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

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.theme.animation.FadingVisibility

/**
 * A floating action button that allows the user to scroll to the bottom of the list.
 *
 * @param count The count shown on top of the button. Hidden if the count is below 1.
 * @param onClick The handler that's triggered when the user taps on the button.
 * @param modifier The modifier used for styling.
 */
@Composable
internal fun ScrollToBottomButton(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .semantics { role = Role.Button },
        contentAlignment = Alignment.Center,
    ) {
        FilledIconButton(
            modifier = Modifier
                .shadow(StreamTokens.spacing2xs, shape = CircleShape)
                .size(40.dp),
            onClick = onClick,
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = ChatTheme.colors.barsBackground,
                contentColor = ChatTheme.colors.textHighEmphasis,
            ),
        ) {
            Icon(
                painter = painterResource(R.drawable.stream_compose_ic_arrow_down),
                contentDescription = stringResource(R.string.stream_compose_scroll_to_bottom),
            )
        }
        FadingVisibility(
            modifier = Modifier.align(Alignment.TopEnd),
            visible = count > 0,
        ) {
            if (count > 0) {
                Surface(
                    modifier = Modifier.border(width = 1.dp, color = Color.White, shape = BadgeShape),
                    shape = BadgeShape,
                    color = ChatTheme.colors.primaryAccent,
                    contentColor = Color.White,
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = StreamTokens.spacing3xs),
                        text = count.toString(),
                        style = ChatTheme.typography.footnoteBold,
                    )
                }
            }
        }
    }
}

private val BadgeShape = RoundedCornerShape(StreamTokens.spacingMd)

@Composable
@Preview(showBackground = true)
private fun Preview() {
    ChatTheme {
        ScrollToBottomButton(
            count = 1,
            onClick = { },
        )
    }
}
