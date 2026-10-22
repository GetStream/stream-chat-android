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

package io.getstream.chat.android.ai.compose.ui.component.internal

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.compose.R
import io.getstream.chat.android.ai.compose.conversation.AIMessageAttachment
import io.getstream.chat.android.ai.compose.ui.component.AIMessageAttachmentColors

private val ImageShape = RoundedCornerShape(16.dp)
private val CardShape = RoundedCornerShape(16.dp)
private val IconShape = RoundedCornerShape(8.dp)
private val CardMaxWidth = 280.dp

/**
 * An image attachment as a square thumbnail, cropped to fill it: the bitmap the app holds, else
 * the image at its URI, with a placeholder color while it loads or when it can't be read.
 */
@Composable
internal fun AttachmentImage(
    attachment: AIMessageAttachment,
    size: Dp,
    colors: AIMessageAttachmentColors,
    modifier: Modifier = Modifier,
) {
    val description = rememberDisplayName(attachment)
    Box(
        modifier = modifier
            .size(size)
            .clip(ImageShape)
            .background(colors.imagePlaceholder)
            .semantics {
                contentDescription = description
                role = Role.Image
            },
    ) {
        val bitmap = attachment.bitmap
        val uri = attachment.uri
        when {
            bitmap != null -> FilledImage(bitmap)
            uri != null -> UriImage(uri, size)
        }
    }
}

@Composable
private fun BoxScope.UriImage(uri: Uri, size: Dp) {
    val context = LocalContext.current
    val targetSizePx = with(LocalDensity.current) { size.roundToPx() }
    var loaded by remember(uri, targetSizePx) {
        mutableStateOf<Bitmap?>(AttachmentImageLoader.cached(uri, targetSizePx))
    }
    // Previews and snapshots don't load: they show the placeholder.
    if (loaded == null && !LocalInspectionMode.current) {
        LaunchedEffect(uri, targetSizePx) {
            loaded = AttachmentImageLoader.load(context, uri, targetSizePx)
        }
    }
    loaded?.let { bitmap ->
        val image = remember(bitmap) { bitmap.asImageBitmap() }
        FilledImage(image)
    }
}

@Composable
private fun BoxScope.FilledImage(image: ImageBitmap) {
    Image(
        bitmap = image,
        contentDescription = null,
        modifier = Modifier.matchParentSize(),
        contentScale = ContentScale.Crop,
    )
}

/**
 * An attachment as a file: an icon of its type, its name, and its type and size, such as
 * "PDF · 2.4 MB". With a URI, tapping it opens the file in an app that shows it.
 */
@Composable
internal fun FileAttachmentCard(
    attachment: AIMessageAttachment,
    nameTextStyle: TextStyle,
    detailsTextStyle: TextStyle,
    colors: AIMessageAttachmentColors,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val name = rememberDisplayName(attachment)
    val details = remember(attachment, LocalConfiguration.current) { attachment.details(context) }
    val openLabel = stringResource(R.string.stream_ai_compose_conversation_attachment_open)
    val opens = attachment.uri != null
    Row(
        modifier = modifier
            .widthIn(max = CardMaxWidth)
            .clip(CardShape)
            .background(colors.background)
            .border(1.dp, colors.border, CardShape)
            .then(
                if (opens) {
                    Modifier.clickable(onClickLabel = openLabel, role = Role.Button) {
                        openAttachment(context, attachment)
                    }
                } else {
                    Modifier
                },
            )
            .semantics(mergeDescendants = true) {}
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FileTypeIcon(isImage = attachment.looksLikeImage(), colors = colors)
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(
                text = name,
                style = nameTextStyle,
                color = colors.name,
                maxLines = 1,
                overflow = TextOverflow.MiddleEllipsis,
            )
            if (details.isNotEmpty()) {
                Text(
                    text = details,
                    style = detailsTextStyle,
                    color = colors.details,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun FileTypeIcon(isImage: Boolean, colors: AIMessageAttachmentColors) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(colors.iconBackground, IconShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(
                if (isImage) {
                    R.drawable.stream_ai_compose_ic_image_placeholder
                } else {
                    R.drawable.stream_ai_compose_ic_conversation_file
                },
            ),
            contentDescription = null,
            tint = colors.icon,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun rememberDisplayName(attachment: AIMessageAttachment): String {
    val context = LocalContext.current
    return remember(attachment, LocalConfiguration.current) { attachment.displayName(context) }
}
