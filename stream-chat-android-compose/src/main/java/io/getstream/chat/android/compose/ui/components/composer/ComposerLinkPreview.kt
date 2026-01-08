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

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.ColorImage
import coil3.compose.LocalAsyncImagePreviewHandler
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.ComposerCancelIcon
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamColors
import io.getstream.chat.android.compose.ui.util.AsyncImagePreviewHandler
import io.getstream.chat.android.compose.ui.util.StreamAsyncImage
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.LinkPreview
import io.getstream.chat.android.ui.common.utils.extensions.imagePreviewUrl
import io.getstream.chat.android.uiutils.extension.addSchemeToUrlIfNeeded
import io.getstream.log.StreamLog

private const val TAG = "ComposerLinkPreview"

/**
 * Shows the link image preview, the title of the link
 * as well as its description.
 *
 * When clicking it, we open the preview link.
 *
 * @param linkPreview - The link preview to show.
 * @param modifier Modifier for styling.
 * @param onClick Lambda called when an item gets clicked.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun ComposerLinkPreview(
    modifier: Modifier = Modifier,
    linkPreview: LinkPreview,
    onClick: ((linkPreview: LinkPreview) -> Unit)? = null,
) {
    var previewClosed by rememberSaveable { mutableStateOf(false) }

    if (previewClosed) {
        return
    }

    val context = LocalContext.current
    val attachment = linkPreview.attachment
    val colors = ChatTheme.colors
    val typography = ChatTheme.typography
    val textColor = colors.textHighEmphasis

    Box {
        Row(
            modifier = modifier
                .combinedClickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { handleLinkPreviewClick(onClick, context, linkPreview) },
                )
                .background(colors.chatBgOutgoing, ChatTheme.shapes.attachment)
                // TODO [G.] point to spacings
                .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ComposerLinkImagePreview(attachment, colors)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically),
            ) {
                ComposerLinkPreviewText(
                    text = attachment.title,
                    style = typography.footnoteBold.copy(color = textColor),
                    testTag = "Stream_LinkPreviewTitle",
                )
                ComposerLinkPreviewText(
                    text = attachment.text,
                    style = typography.footnote.copy(color = textColor),
                    testTag = "Stream_LinkPreviewDescription",
                )
                ComposerLinkPreviewText(
                    text = linkPreview.resolveUrl(),
                    style = typography.footnote.copy(color = textColor),
                    testTag = "Stream_LinkPreviewUrl",
                )
            }
        }
        ComposerCancelIcon(
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = 4.dp, y = (-4).dp),
        ) { previewClosed = true }
    }
}

@Composable
private fun ComposerLinkImagePreview(attachment: Attachment, colors: StreamColors) {
    val imagePreviewUrl = attachment.imagePreviewUrl ?: return
    // TODO [G.] point to radii
    val shape = RoundedCornerShape(8.dp)
    StreamAsyncImage(
        data = imagePreviewUrl,
        modifier = Modifier
            .size(width = 40.dp, height = 40.dp)
            .clip(shape)
            .border(1.dp, colors.borderCoreImage, shape)
            .testTag("Stream_LinkPreviewImage"),
        contentDescription = null,
        contentScale = ContentScale.Crop,
    )
}

@Composable
private fun ComposerLinkPreviewText(text: String?, style: TextStyle, testTag: String) {
    text ?: return
    Text(
        modifier = Modifier.testTag(testTag),
        text = text,
        style = style,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/**
 * Handles clicks on link attachment content.
 *
 * @param onClick Optional custom click handler.
 * @param context Context needed to start the Activity.
 * @param preview The preview of the link attachment being clicked.
 */
@VisibleForTesting
internal fun handleLinkPreviewClick(
    onClick: ((linkPreview: LinkPreview) -> Unit)?,
    context: Context,
    preview: LinkPreview,
) {
    val previewUrl = preview.resolveUrl()

    checkNotNull(previewUrl) {
        "Missing preview URL."
    }

    try {
        onClick?.invoke(preview) ?: context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                previewUrl.addSchemeToUrlIfNeeded().toUri(),
            ),
        )
    } catch (e: ActivityNotFoundException) {
        StreamLog.e(TAG, e) { "[handleLinkPreviewClick] failed: $e" }
        val errorMessage = context.getString(R.string.stream_compose_message_list_error_cannot_open_link, previewUrl)
        Toast
            .makeText(context, errorMessage, Toast.LENGTH_LONG)
            .show()
    }
}

private fun LinkPreview.resolveUrl() = attachment.run { titleLink ?: ogUrl }

@Preview(showBackground = true)
@Composable
private fun ComposerLinkContentPreview() {
    ChatTheme {
        ComposerLinkPreview()
    }
}

@Composable
internal fun ComposerLinkPreview() {
    val previewHandler = AsyncImagePreviewHandler {
        ColorImage(color = Color.Magenta.toArgb(), width = 200, height = 150)
    }
    CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
        val attachment = Attachment(
            titleLink = "https://loremipsumdolor.sit",
            title = "Title",
            text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt.",
            imageUrl = "Image",
        )
        ComposerLinkPreview(
            linkPreview = LinkPreview(
                originUrl = "Url",
                attachment = attachment,
            ),
        )
    }
}
