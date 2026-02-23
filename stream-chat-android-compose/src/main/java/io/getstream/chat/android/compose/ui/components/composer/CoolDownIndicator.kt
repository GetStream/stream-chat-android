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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Represent a timer that show the remaining time until the user is allowed to send the next message.
 *
 * @param coolDownTime The amount of time left until the user is allowed to sent the next message.
 * @param modifier Modifier for styling.
 */
@Composable
public fun CoolDownIndicator(
    coolDownTime: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .padding(8.dp)
            .background(shape = RoundedCornerShape(24.dp), color = ChatTheme.colors.textDisabled),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = coolDownTime.toString(),
            color = Color.White,
            textAlign = TextAlign.Center,
            style = ChatTheme.typography.bodyBold,
        )
    }
}
