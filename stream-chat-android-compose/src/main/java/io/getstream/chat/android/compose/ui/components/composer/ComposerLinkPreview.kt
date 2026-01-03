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
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import coil3.ColorImage
import coil3.compose.LocalAsyncImagePreviewHandler
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
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
    val previewUrl = attachment.titleLink ?: attachment.ogUrl

    checkNotNull(previewUrl) {
        "Missing preview URL."
    }

    val errorMessage = stringResource(id = R.string.stream_compose_message_list_error_cannot_open_link, previewUrl)

    Row(
        modifier = modifier
            .combinedClickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    try {
                        onClick?.invoke(linkPreview) ?: onLinkPreviewClick(context, linkPreview)
                    } catch (e: ActivityNotFoundException) {
                        StreamLog.e(TAG, e) { "[onLinkPreviewClick] failed: $e" }
                        Toast
                            .makeText(context, errorMessage, Toast.LENGTH_LONG)
                            .show()
                    }
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val theme = ChatTheme.messageComposerTheme.linkPreview
        ComposerLinkImagePreview(attachment)
        ComposerVerticalSeparator()
        Column(
            modifier = Modifier.weight(1f),
        ) {
            ComposerLinkTitle(attachment.title)
            Spacer(modifier = Modifier.height(theme.titleToSubtitle))
            ComposerLinkDescription(attachment.text)
        }
        ComposerLinkCancelIcon { previewClosed = true }
    }
}

@Composable
private fun ComposerLinkImagePreview(attachment: Attachment) {
    val imagePreviewUrl = attachment.imagePreviewUrl ?: return
    val theme = ChatTheme.messageComposerTheme.linkPreview
    Box(
        modifier = Modifier.padding(theme.imagePadding),
        contentAlignment = Alignment.Center,
    ) {
        StreamAsyncImage(
            data = imagePreviewUrl,
            modifier = Modifier
                .height(theme.imageSize.height)
                .width(theme.imageSize.width)
                .clip(theme.imageShape)
                .testTag("Stream_LinkPreviewImage"),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
private fun ComposerVerticalSeparator() {
    val theme = ChatTheme.messageComposerTheme.linkPreview
    Box(
        modifier = Modifier.padding(
            start = theme.separatorMarginStart,
            end = theme.separatorMarginEnd,
        ),
    ) {
        Box(
            modifier = Modifier
                .height(theme.separatorSize.height)
                .width(theme.separatorSize.width)
                .background(ChatTheme.colors.primaryAccent),
        )
    }
}

@Composable
private fun ComposerLinkTitle(title: String?) {
    title ?: return
    val textStyle = ChatTheme.messageComposerTheme.linkPreview.title
    Text(
        modifier = Modifier.testTag("Stream_LinkPreviewTitle"),
        text = title,
        style = textStyle.style,
        color = textStyle.color,
        maxLines = textStyle.maxLines,
        overflow = textStyle.overflow,
    )
}

@Composable
private fun ComposerLinkDescription(description: String?) {
    description ?: return
    val textStyle = ChatTheme.messageComposerTheme.linkPreview.subtitle
    Text(
        modifier = Modifier.testTag("Stream_LinkPreviewDescription"),
        text = description,
        style = textStyle.style,
        color = textStyle.color,
        maxLines = textStyle.maxLines,
        overflow = textStyle.overflow,
    )
}

@Composable
private fun ComposerLinkCancelIcon(
    onClick: () -> Unit,
) {
    val theme = ChatTheme.messageComposerTheme.linkPreview
    IconButton(onClick = onClick) {
        Icon(
            modifier = Modifier
                .background(
                    shape = theme.cancelIcon.backgroundShape,
                    color = theme.cancelIcon.backgroundColor,
                )
                .testTag("Stream_AttachmentCancelIcon"),
            painter = theme.cancelIcon.painter,
            contentDescription = stringResource(id = R.string.stream_compose_cancel),
            tint = theme.cancelIcon.tint,
        )
    }
}

/**
 * Handles clicks on link attachment content.
 *
 * @param context Context needed to start the Activity.
 * @param preview The preview of the link attachment being clicked.
 */
private fun onLinkPreviewClick(context: Context, preview: LinkPreview) {
    val previewUrl = preview.attachment.titleLink ?: preview.attachment.ogUrl
    checkNotNull(previewUrl) {
        "Missing preview URL."
    }
    val urlWithScheme = previewUrl.addSchemeToUrlIfNeeded()
    context.startActivity(
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse(urlWithScheme),
        ),
    )
}

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
            titleLink = "Link",
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
