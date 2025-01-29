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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.animation.crossfade.CrossfadePlugin
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.placeholder.placeholder.PlaceholderPlugin
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.StreamImage
import io.getstream.chat.android.ui.common.images.resizing.applyStreamCdnImageResizingIfEnabled

/**
 * An avatar that renders an image from the provided image URL. In case the image URL
 * was empty or there was an error loading the image, it falls back to the initials avatar.
 *
 * @param imageUrl The URL of the image to load.
 * @param initials The fallback text.
 * @param modifier Modifier for styling.
 * @param shape The shape of the avatar.
 * @param textStyle The text style of the [initials] text.
 * @param placeholderPainter The placeholder to render while loading is in progress.
 * @param contentDescription Description of the image.
 * @param initialsAvatarOffset The initials offset to apply to the avatar.
 * @param onClick OnClick action, that can be nullable.
 */
@Composable
public fun Avatar(
    imageUrl: String,
    initials: String,
    modifier: Modifier = Modifier,
    shape: Shape = ChatTheme.shapes.avatar,
    textStyle: TextStyle = ChatTheme.typography.title3Bold,
    placeholderPainter: Painter? = null,
    contentDescription: String? = null,
    initialsAvatarOffset: DpOffset = DpOffset(0.dp, 0.dp),
    onClick: (() -> Unit)? = null,
) {
    if (imageUrl.isBlank()) {
        InitialsAvatar(
            modifier = modifier,
            initials = initials,
            shape = shape,
            textStyle = textStyle,
            onClick = onClick,
            avatarOffset = initialsAvatarOffset,
        )
        return
    }

    val cdnImageResizing = ChatTheme.streamCdnImageResizing
    val clickableModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }
    StreamImage(
        modifier = modifier
            .testTag("Stream_QuotedMessageAuthorAvatar")
            .clip(shape)
            .then(clickableModifier),
        data = { imageUrl.applyStreamCdnImageResizingIfEnabled(cdnImageResizing) },
        loading = {
            if (placeholderPainter != null) {
                ImageAvatar(
                    modifier = modifier,
                    shape = shape,
                    painter = placeholderPainter,
                    contentDescription = contentDescription,
                    onClick = onClick,
                )
            }
        },
        failure = {
            InitialsAvatar(
                modifier = modifier,
                initials = initials,
                shape = shape,
                textStyle = textStyle,
                onClick = onClick,
                avatarOffset = initialsAvatarOffset,
            )
        },
        component = rememberImageComponent {
            if (placeholderPainter == null) {
                +PlaceholderPlugin.Loading(painterResource(id = R.drawable.stream_compose_preview_avatar))
            }
            +CrossfadePlugin()
        },
        previewPlaceholder = painterResource(id = R.drawable.stream_compose_preview_avatar),
        imageOptions = ImageOptions(contentDescription = contentDescription),
    )
}

/**
 * Preview of [Avatar] for a valid image URL.
 *
 * Should show the provided image.
 */
@Preview(showBackground = true, name = "Avatar Preview (With image URL)")
@Composable
private fun AvatarWithImageUrlPreview() {
    AvatarPreview(
        imageUrl = "https://sample.com/image.png",
        initials = "JC",
    )
}

/**
 * Preview of [Avatar] for a user which is online.
 *
 * Should show a background gradient with fallback initials.
 */
@Preview(showBackground = true, name = "Avatar Preview (Without image URL)")
@Composable
private fun AvatarWithoutImageUrlPreview() {
    AvatarPreview(
        imageUrl = "",
        initials = "JC",
    )
}

/**
 * Shows [Avatar] preview for the provided parameters.
 *
 * @param imageUrl The image URL to load.
 * @param initials The fallback initials.
 */
@Composable
private fun AvatarPreview(
    imageUrl: String,
    initials: String,
) {
    ChatTheme {
        Avatar(
            modifier = Modifier.size(36.dp),
            imageUrl = imageUrl,
            initials = initials,
        )
    }
}
