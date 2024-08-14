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

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Basic back button, that shows an icon and calls [onBackPressed] when tapped.
 *
 * @param painter The icon or image to show.
 * @param onBackPressed Handler for the back action.
 * @param modifier Modifier for styling.
 */
@Composable
public fun BackButton(
    painter: Painter,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        modifier = modifier,
        onClick = onBackPressed,
    ) {
        Icon(
            painter = painter,
            contentDescription = null,
            tint = ChatTheme.colors.textHighEmphasis,
        )
    }
}
