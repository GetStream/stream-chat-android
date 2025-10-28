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

package io.getstream.chat.android.compose.ui.attachments.content

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.LocalAsyncImagePreviewHandler
import io.getstream.chat.android.client.utils.attachment.isGiphy
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.components.ShimmerProgressIndicator
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.AsyncImagePreviewHandler
import io.getstream.chat.android.compose.ui.util.StreamAsyncImage
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.utils.extensions.imagePreviewUrl
import io.getstream.chat.android.uiutils.extension.addSchemeToUrlIfNeeded
import io.getstream.chat.android.uiutils.extension.hasLink

/**
 * Builds a link attachment message, which shows the link image preview, the title of the link
 * as well as its description.
 *
 * When clicking it, we open the preview link.
 *
 * @param attachmentState - The state of the attachment, holding the root modifier, the message
 * and the onLongItemClick handler.
 * @param linkDescriptionMaxLines - The limit of how many lines we show for the link description.
 * @param modifier Modifier for styling.
 * @param onItemClick Lambda called when an item gets clicked.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
@Deprecated(
    message = "Use the version with the `onItemClick` parameter that accepts a LinkAttachmentClickData.",
    replaceWith = ReplaceWith(
        expression = "LinkAttachmentContent(attachmentState, linkDescriptionMaxLines, modifier, onItemClick)",
    ),
)
public fun LinkAttachmentContent(
    attachmentState: AttachmentState,
    linkDescriptionMaxLines: Int,
    modifier: Modifier = Modifier,
    onItemClick: (context: Context, url: String) -> Unit,
) {
    LinkAttachmentContent(
        state = attachmentState,
        linkDescriptionMaxLines = linkDescriptionMaxLines,
        modifier = modifier,
        onItemClick = { onItemClick(it.context, it.url) },
    )
}

/**
 * Builds a link attachment message, which shows the link image preview, the title of the link
 * as well as its description.
 *
 * When clicking it, we open the preview link.
 *
 * @param state - The state of the attachment, holding the root modifier, the message
 * and the onLongItemClick handler.
 * @param linkDescriptionMaxLines - The limit of how many lines we show for the link description.
 * @param modifier Modifier for styling.
 * @param onItemClick Lambda called when an item gets clicked.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
@Suppress("LongMethod")
public fun LinkAttachmentContent(
    state: AttachmentState,
    linkDescriptionMaxLines: Int,
    modifier: Modifier = Modifier,
    onItemClick: (LinkAttachmentClickData) -> Unit = {
        onLinkAttachmentContentClick(it.context, it.url)
    },
) {
    val (message, isMine, onLongItemClick) = state

    val context = LocalContext.current
    val attachment = message.attachments.firstOrNull { it.hasLink() && !it.isGiphy() }

    checkNotNull(attachment) {
        "Missing link attachment."
    }

    val previewUrl = attachment.titleLink ?: attachment.ogUrl
    val urlWithScheme = previewUrl?.addSchemeToUrlIfNeeded()

    checkNotNull(previewUrl) {
        "Missing preview URL."
    }

    val errorMessage = stringResource(R.string.stream_compose_message_list_error_cannot_open_link, previewUrl)

    Column(
        modifier = modifier
            .clip(ChatTheme.shapes.attachment)
            .background(getLinkBackgroundColor(isMine))
            .combinedClickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    try {
                        if (urlWithScheme != null) {
                            onItemClick(
                                LinkAttachmentClickData(
                                    context = context,
                                    url = urlWithScheme,
                                    attachment = attachment,
                                    message = message,
                                ),
                            )
                        } else {
                            Toast
                                .makeText(context, errorMessage, Toast.LENGTH_LONG)
                                .show()
                        }
                    } catch (e: ActivityNotFoundException) {
                        e.printStackTrace()
                        Toast
                            .makeText(context, errorMessage, Toast.LENGTH_LONG)
                            .show()
                    }
                },
                onLongClick = { onLongItemClick(message) },
            ),
    ) {
        val imagePreviewUrl = attachment.imagePreviewUrl
        if (imagePreviewUrl != null) {
            LinkAttachmentImagePreview(attachment, isMine)
        }

        val title = attachment.title
        if (title != null) {
            LinkAttachmentTitle(title)
        }

        val description = attachment.text
        if (description != null) {
            LinkAttachmentDescription(description, linkDescriptionMaxLines)
        }
    }
}

@Composable
private fun LinkAttachmentImagePreview(attachment: Attachment, isMine: Boolean) {
    val data = attachment.imagePreviewUrl
    var maxWidth by remember { mutableStateOf(0.dp) }

    Box(
        modifier = Modifier.onSizeChanged { size -> maxWidth = size.width.dp },
    ) {
        val contentScale = ContentScale.FillWidth
        StreamAsyncImage(
            modifier = Modifier
                .heightIn(max = 250.dp)
                .clip(ChatTheme.shapes.attachment)
                .testTag("Stream_LinkAttachmentPreview"),
            data = data,
            contentScale = contentScale,
        ) { state ->
            val painter = state.painter

            if (painter == null) {
                ShimmerProgressIndicator(
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                val intrinsicSize = painter.intrinsicSize
                val aspectRatio = intrinsicSize.width / intrinsicSize.height

                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(aspectRatio),
                    painter = painter,
                    contentDescription = null,
                    contentScale = contentScale,
                )
            }
        }

        val authorName = attachment.authorName

        if (authorName != null) {
            Text(
                text = authorName,
                color = ChatTheme.colors.primaryAccent,
                maxLines = 1,
                style = ChatTheme.typography.bodyBold,
                fontSize = 16.sp,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .widthIn(max = maxWidth / 2)
                    .background(
                        color = getLinkBackgroundColor(isMine),
                        shape = ChatTheme.shapes.attachmentSiteLabel,
                    )
                    .padding(vertical = 6.dp, horizontal = 12.dp)
                    .align(Alignment.BottomStart),
            )
        }
    }
}

@Composable
private fun LinkAttachmentTitle(text: String) {
    Text(
        modifier = Modifier
            .padding(start = 8.dp, end = 8.dp)
            .testTag("Stream_LinkAttachmentTitle"),
        text = text,
        style = ChatTheme.typography.bodyBold,
        color = ChatTheme.colors.textHighEmphasis,
    )
}

@Composable
private fun LinkAttachmentDescription(description: String, linkDescriptionMaxLines: Int) {
    Text(
        modifier = Modifier
            .padding(
                start = 8.dp,
                end = 8.dp,
                bottom = 4.dp,
                top = 2.dp,
            )
            .testTag("Stream_LinkAttachmentDescription"),
        text = description,
        style = ChatTheme.typography.footnote,
        color = ChatTheme.colors.textHighEmphasis,
        maxLines = linkDescriptionMaxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun getLinkBackgroundColor(isMine: Boolean): Color = if (isMine) {
    ChatTheme.ownMessageTheme.linkBackgroundColor
} else {
    ChatTheme.otherMessageTheme.linkBackgroundColor
}

/**
 * Handles clicks on link attachment content.
 *
 * @param context Context needed to start the Activity.
 * @param url The url of the link attachment being clicked.
 */
internal fun onLinkAttachmentContentClick(context: Context, url: String) {
    context.startActivity(
        Intent(
            Intent.ACTION_VIEW,
            url.toUri(),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun LinkAttachmentContentPreview() {
    ChatTheme {
        LinkAttachmentContent()
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
internal fun LinkAttachmentContent() {
    val previewHandler = AsyncImagePreviewHandler {
        ColorImage(color = Color.Cyan.toArgb(), width = 200, height = 150)
    }
    CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
        val attachment = Attachment(
            titleLink = "Link",
            title = "Title",
            text = LongDescription,
            imageUrl = "Image",
            authorName = "Author",
        )
        LinkAttachmentContent(
            state = AttachmentState(
                message = Message(attachments = listOf(attachment)),
            ),
            linkDescriptionMaxLines = 5,
        )
    }
}

/**
 * Data class that holds information about a link attachment click event.
 *
 * @param context The context in which the click event occurred.
 * @param url The URL of the link attachment that was clicked.
 * @param message The message containing the link attachment.
 */
@ConsistentCopyVisibility
public data class LinkAttachmentClickData internal constructor(
    val context: Context,
    val url: String,
    val attachment: Attachment,
    val message: Message,
)

@Suppress("MagicNumber")
private val LongDescription = (0..50).joinToString { "Lorem ipsum dolor sit amet" }
