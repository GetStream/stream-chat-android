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

package io.getstream.chat.android.compose.ui.components.channels

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Default component for selected channel menu options.
 *
 * @param title The text title of the action.
 * @param titleColor The color of the title.
 * @param leadingIcon The composable that defines the leading icon for the action.
 * @param onClick The action to perform once the user taps on any option.
 * @param modifier Modifier for styling.
 */
@Composable
internal fun ChannelOptionsItem(
    title: String,
    titleColor: Color,
    leadingIcon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(
                onClick = onClick,
                indication = ripple(),
                interactionSource = remember { MutableInteractionSource() },
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        leadingIcon()

        Text(
            text = title,
            style = ChatTheme.typography.bodyBold,
            color = titleColor,
        )
    }
}

/**
 * Preview of [ChannelOptionsItem].
 *
 * Should show a channel action item with an icon and a name.
 */
@Preview(showBackground = true, name = "ChannelOptionsItem Preview")
@Composable
private fun ChannelOptionsItemPreview() {
    ChatTheme {
        ChannelOptionsItem(
            title = stringResource(id = R.string.stream_compose_selected_channel_menu_view_info),
            titleColor = ChatTheme.colors.textHighEmphasis,
            leadingIcon = {
                Icon(
                    modifier = Modifier
                        .size(56.dp)
                        .padding(16.dp),
                    painter = painterResource(id = R.drawable.stream_compose_ic_person),
                    tint = ChatTheme.colors.textLowEmphasis,
                    contentDescription = null,
                )
            },
            onClick = {},
        )
    }
}
