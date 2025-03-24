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

package io.getstream.chat.android.compose.sample.ui.channel

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Composable representing an option (action) the the user can do with a channel.
 * Ex: "Mute user", "Delete conversation"...
 *
 * @param icon The icon resource to be shown on the start of the item.
 * @param text The text to be shown in the center of the item.
 * @param onClick Action invoked when the user clicks on the item.
 * @param iconTint The tint for the icon).
 * @param textColor The color of the text.
 * @param trailingContent Content shown at the end of the item (defaults to
 * [Icons.AutoMirrored.Filled.KeyboardArrowRight] icon).
 */
@Composable
fun ChannelInfoOptionItem(
    @DrawableRes icon: Int,
    text: String,
    onClick: () -> Unit,
    iconTint: Color = ChatTheme.colors.textLowEmphasis,
    textColor: Color = ChatTheme.colors.textHighEmphasis,
    trailingContent: @Composable RowScope.() -> Unit = {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = ChatTheme.colors.textLowEmphasis,
        )
    },
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChatTheme.colors.appBackground)
            .clickable(
                interactionSource = null,
                indication = ripple(),
                onClick = onClick,
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(icon),
            contentDescription = null,
            tint = iconTint,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            modifier = Modifier.weight(1f),
            text = text,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = textColor,
        )
        Spacer(modifier = Modifier.width(16.dp))
        trailingContent()
    }
}
