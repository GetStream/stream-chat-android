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

package io.getstream.chat.android.compose.ui.components.avatar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.StreamAsyncImage
import io.getstream.chat.android.compose.ui.util.applyIf
import io.getstream.chat.android.ui.common.images.resizing.applyStreamCdnImageResizingIfEnabled

@Composable
internal fun Avatar(
    imageUrl: String?,
    fallback: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    showBorder: Boolean = false,
) {
    val resizing = ChatTheme.streamCdnImageResizing
    val data = remember(imageUrl, resizing) { imageUrl?.applyStreamCdnImageResizingIfEnabled(resizing) }

    StreamAsyncImage(
        data = data,
        modifier = modifier
            .clip(CircleShape)
            .applyIf(showBorder) { border(1.dp, ChatTheme.colors.borderCoreOpacity10, CircleShape) }
            .background(ChatTheme.colors.backgroundElevationElevation0, CircleShape),
        contentScale = ContentScale.Crop,
        content = { state ->
            val painter = (state as? AsyncImagePainter.State.Success)?.painter

            if (painter == null) {
                fallback()
            } else {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    painter = painter,
                    contentDescription = null,
                )
            }
        },
    )
}

/**
 * Predefined set of avatar sizes. These are used in Stream designs, but also as threshold for scaling accessory
 * components, like the online indicator, placeholder icon, and text style.
 */
public object AvatarSize {
    public val ExtraSmall: Dp = 20.dp
    public val Small: Dp = 24.dp
    public val Medium: Dp = 32.dp
    public val Large: Dp = 40.dp
    public val ExtraLarge: Dp = 48.dp
    public val ExtraExtraLarge: Dp = 64.dp
}
