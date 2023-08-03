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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * The view that's shown when there's no data available. Consists of an icon and a caption below it.
 *
 * @param text The text to be displayed.
 * @param painter The painter for the icon.
 * @param modifier Modifier for styling.
 */
@Composable
public fun EmptyContent(
    text: String,
    painter: Painter,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.background(color = ChatTheme.colors.appBackground),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painter,
            contentDescription = null,
            tint = ChatTheme.colors.disabled,
            modifier = Modifier.size(96.dp),
        )

        Spacer(Modifier.size(16.dp))

        Text(
            text = text,
            style = ChatTheme.typography.title3,
            color = ChatTheme.colors.textLowEmphasis,
            textAlign = TextAlign.Center,
        )
    }
}
