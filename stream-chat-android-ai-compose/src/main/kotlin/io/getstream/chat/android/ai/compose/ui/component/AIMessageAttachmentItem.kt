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

package io.getstream.chat.android.ai.compose.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.compose.R
import io.getstream.chat.android.ai.compose.conversation.AIMessageAttachment
import io.getstream.chat.android.ai.compose.ui.component.internal.AttachmentImage
import io.getstream.chat.android.ai.compose.ui.component.internal.FileAttachmentCard

/**
 * One of a message's attachments: an image as a square thumbnail, and any other kind as a file,
 * with its name, type and size, that opens when tapped.
 *
 * ```
 * AIMessageAttachmentItem(attachment = AIMessageAttachment.file(uri, name = "Itinerary.pdf"))
 * ```
 *
 * An image shows the bitmap the app holds, else the one at its URI, on this device or on the web,
 * loaded at the size it shows at. An image with neither shows as a file. A file opens in an app
 * for its type, and a page on the web in the browser.
 *
 * @param attachment What to show.
 * @param modifier The modifier to apply to the attachment.
 * @param imageSize The width and height of an image.
 * @param nameTextStyle The style of a file's name.
 * @param detailsTextStyle The style of a file's type and size.
 * @param colors The palette of the attachment.
 * @param icons The icons of the types of file.
 */
@Suppress("LongParameterList")
@Composable
public fun AIMessageAttachmentItem(
    attachment: AIMessageAttachment,
    modifier: Modifier = Modifier,
    imageSize: Dp = 180.dp,
    nameTextStyle: TextStyle = MaterialTheme.typography.labelLarge,
    detailsTextStyle: TextStyle = MaterialTheme.typography.bodySmall,
    colors: AIMessageAttachmentColors = AIMessageAttachmentDefaults.colors(),
    icons: AIMessageAttachmentIcons = AIMessageAttachmentDefaults.icons(),
) {
    val isImage = attachment.kind == AIMessageAttachment.Kind.Image
    if (isImage && (attachment.bitmap != null || attachment.uri != null)) {
        AttachmentImage(attachment = attachment, size = imageSize, colors = colors, modifier = modifier)
    } else {
        FileAttachmentCard(
            attachment = attachment,
            nameTextStyle = nameTextStyle,
            detailsTextStyle = detailsTextStyle,
            colors = colors,
            icons = icons,
            modifier = modifier,
        )
    }
}

/**
 * The palette of [AIMessageAttachmentItem].
 *
 * @param imagePlaceholder Color behind an image, which shows while it loads or when it can't be read.
 * @param background Color of a file's card.
 * @param border Color of the outline of a file's card.
 * @param iconBackground Color of the square behind the icon of a file's type.
 * @param icon Color of the icon of a file's type.
 * @param name Color of a file's name.
 * @param details Color of a file's type and size.
 */
@Immutable
public data class AIMessageAttachmentColors(
    val imagePlaceholder: Color,
    val background: Color,
    val border: Color,
    val iconBackground: Color,
    val icon: Color,
    val name: Color,
    val details: Color,
)

/**
 * The icons of [AIMessageAttachmentItem], one for each type of file. A file's type comes from its
 * media type, else from the extension of its name or URI.
 *
 * @param file Icon of a file of a type without one of its own.
 * @param pdf Icon of a PDF.
 * @param image Icon of an image shown as a file, such as one without a URI or bitmap.
 * @param audio Icon of an audio file.
 * @param video Icon of a video.
 * @param archive Icon of an archive, such as a ZIP, RAR, 7z, TAR or GZIP file.
 */
@Immutable
public data class AIMessageAttachmentIcons(
    val file: Painter,
    val pdf: Painter,
    val image: Painter,
    val audio: Painter,
    val video: Painter,
    val archive: Painter,
)

/** Defaults of [AIMessageAttachmentItem]. */
public object AIMessageAttachmentDefaults {

    /**
     * The default palette, from the current [MaterialTheme].
     *
     * @param imagePlaceholder Color behind an image, which shows while it loads or when it can't
     * be read.
     * @param background Color of a file's card.
     * @param border Color of the outline of a file's card.
     * @param iconBackground Color of the square behind the icon of a file's type.
     * @param icon Color of the icon of a file's type.
     * @param name Color of a file's name.
     * @param details Color of a file's type and size.
     */
    @Suppress("LongParameterList")
    @Composable
    public fun colors(
        imagePlaceholder: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
        background: Color = MaterialTheme.colorScheme.surfaceContainerLow,
        border: Color = MaterialTheme.colorScheme.outlineVariant,
        iconBackground: Color = MaterialTheme.colorScheme.primary,
        icon: Color = MaterialTheme.colorScheme.onPrimary,
        name: Color = MaterialTheme.colorScheme.onSurface,
        details: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    ): AIMessageAttachmentColors = AIMessageAttachmentColors(
        imagePlaceholder = imagePlaceholder,
        background = background,
        border = border,
        iconBackground = iconBackground,
        icon = icon,
        name = name,
        details = details,
    )

    /**
     * The default icons.
     *
     * @param file Icon of a file of a type without one of its own.
     * @param pdf Icon of a PDF.
     * @param image Icon of an image shown as a file.
     * @param audio Icon of an audio file.
     * @param video Icon of a video.
     * @param archive Icon of an archive.
     */
    @Suppress("LongParameterList")
    @Composable
    public fun icons(
        file: Painter = painterResource(R.drawable.stream_ai_compose_ic_conversation_file),
        pdf: Painter = painterResource(R.drawable.stream_ai_compose_ic_attachment_pdf),
        image: Painter = painterResource(R.drawable.stream_ai_compose_ic_image_placeholder),
        audio: Painter = painterResource(R.drawable.stream_ai_compose_ic_attachment_audio),
        video: Painter = painterResource(R.drawable.stream_ai_compose_ic_attachment_video),
        archive: Painter = painterResource(R.drawable.stream_ai_compose_ic_attachment_archive),
    ): AIMessageAttachmentIcons = AIMessageAttachmentIcons(
        file = file,
        pdf = pdf,
        image = image,
        audio = audio,
        video = video,
        archive = archive,
    )
}
