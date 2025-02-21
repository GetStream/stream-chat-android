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

package io.getstream.chat.android.compose.ui.components.avatar

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.clickable

/**
 * Renders an image the [painter] provides. It allows for customization,
 * uses the 'avatar' shape from [ChatTheme.shapes] for the clipping and exposes an [onClick] action.
 *
 * @param painter The painter for the image.
 * @param modifier Modifier for styling.
 * @param shape The shape of the avatar.
 * @param contentDescription Description of the image.
 * @param onClick OnClick action, that can be nullable.
 */
@Composable
public fun ImageAvatar(
    painter: Painter,
    modifier: Modifier = Modifier,
    shape: Shape = ChatTheme.shapes.avatar,
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val clickableModifier: Modifier = if (onClick != null) {
        modifier.clickable(
            onClick = onClick,
            bounded = false,
        )
    } else {
        modifier
    }

    Image(
        modifier = clickableModifier.clip(shape),
        contentScale = ContentScale.Crop,
        painter = painter,
        contentDescription = contentDescription,
    )
}
