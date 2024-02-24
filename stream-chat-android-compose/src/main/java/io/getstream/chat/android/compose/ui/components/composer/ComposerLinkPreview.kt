package io.getstream.chat.android.compose.ui.components.composer

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.rememberStreamImagePainter
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.LinkPreview
import io.getstream.chat.android.ui.common.utils.extensions.imagePreviewUrl
import io.getstream.chat.android.uiutils.extension.addSchemeToUrlIfNeeded

/**
 * Shows the link image preview, the title of the link
 * as well as its description.
 *
 * When clicking it, we open the preview link.
 *
 * @param linkPreview - The link preview to show.
 * @param linkDescriptionMaxLines - The limit of how many lines we show for the link description.
 * @param modifier Modifier for styling.
 * @param onItemClick Lambda called when an item gets clicked.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun ComposerLinkPreview(
    linkPreview: LinkPreview,
    linkDescriptionMaxLines: Int,
    modifier: Modifier = Modifier,
    onItemClick: (context: Context, url: String) -> Unit = ::onLinkPreviewClick,
) {
    
    var previewClosed by rememberSaveable { mutableStateOf(false) }

    if (previewClosed) {
        return
    }

    val context = LocalContext.current
    val attachment = linkPreview.attachment
    val previewUrl = attachment.titleLink ?: attachment.ogUrl
    val urlWithScheme = previewUrl?.addSchemeToUrlIfNeeded()

    checkNotNull(previewUrl) {
        "Missing preview URL."
    }

    val errorMessage = stringResource(
        id = R.string.stream_compose_message_list_error_cannot_open_link,
        previewUrl,
    )

    Row(
        modifier = modifier
            .combinedClickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    try {
                        if (urlWithScheme != null) {
                            onItemClick(context, urlWithScheme)
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
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val imagePreviewUrl = attachment.imagePreviewUrl
        if (imagePreviewUrl != null) {
            ComposerLinkImagePreview(attachment)
        }

        Spacer(modifier = Modifier.width(4.dp))

        Box(
            modifier = Modifier
                .height(48.dp)
                .width(2.dp)
                .background(ChatTheme.colors.primaryAccent)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            val title = attachment.title
            if (title != null) {
                ComposerLinkTitle(title)
            }
            Spacer(modifier = Modifier.height(4.dp))
            val description = attachment.text
            if (description != null) {
                ComposerLinkDescription(description, linkDescriptionMaxLines)
            }
        }
        
        IconButton(onClick = { previewClosed = true }) {
            Icon(
                modifier = modifier
                    .background(
                        shape = ChatTheme.messageComposerTheme.attachmentCancelIcon.backgroundShape,
                        color = ChatTheme.messageComposerTheme.attachmentCancelIcon.backgroundColor,
                    ),
                painter = ChatTheme.messageComposerTheme.attachmentCancelIcon.painter,
                contentDescription = stringResource(id = R.string.stream_compose_cancel),
                tint = ChatTheme.messageComposerTheme.attachmentCancelIcon.tint,
            )
        }
    }
}

@Composable
private fun ComposerLinkImagePreview(attachment: Attachment) {
    val painter = rememberStreamImagePainter(data = attachment.imagePreviewUrl)
    Box(modifier = Modifier.height(56.dp)
        .width(56.dp),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            modifier = Modifier
                .height(48.dp)
                .width(48.dp)
                /*.clip(ChatTheme.shapes.attachment)*/,
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
private fun ComposerLinkTitle(text: String) {
    Text(
        text = text,
        style = ChatTheme.typography.bodyBold,
        color = ChatTheme.colors.textHighEmphasis,
        maxLines = 1,
    )
}

@Composable
private fun ComposerLinkDescription(description: String, linkDescriptionMaxLines: Int) {
    Text(
        text = description,
        style = ChatTheme.typography.footnote,
        color = ChatTheme.colors.textHighEmphasis,
        maxLines = linkDescriptionMaxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

/**
 * Handles clicks on link attachment content.
 *
 * @param context Context needed to start the Activity.
 * @param url The url of the link attachment being clicked.
 */
internal fun onLinkPreviewClick(context: Context, url: String) {
    context.startActivity(
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse(url),
        ),
    )
}


@Preview
@Composable
private fun SampleComposerLinkPreview() {
    val attachment = Attachment(
        title = "Yahoo | Mail, Weather, Search, Politics, News, Finance, Sports & Videos",
        titleLink = "https://www.yahoo.com/",
        text = "Latest news coverage, email, free stock quotes, live scores and video are just the beginning. Discover more every day at Yahoo!",
        imageUrl = "https://s.yimg.com/cv/apiv2/social/images/yahoo_default_logo.png",
        thumbUrl = "https://s.yimg.com/cv/apiv2/social/images/yahoo_default_logo.png",
        ogUrl = "http://Yahoo.co",
    )

    ComposerLinkPreview(
        linkPreview = LinkPreview(
            originUrl = "https://www.yahoo.com/",
            attachment = attachment,
        ),
        linkDescriptionMaxLines = 2,
    )
}