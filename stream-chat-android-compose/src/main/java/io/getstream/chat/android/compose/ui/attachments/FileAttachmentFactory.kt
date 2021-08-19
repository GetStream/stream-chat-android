package io.getstream.chat.android.compose.ui.attachments

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.getstream.sdk.chat.utils.MediaStringUtil
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.attachments.components.FileAttachmentImage
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.offline.ChatDomain

/**
 * An extension of the [AttachmentFactory] that validates attachments as files and uses [FileAttachmentContent] to
 * build the UI for the message.
 * */
public class FileAttachmentFactory : AttachmentFactory(
    predicate = { attachments -> attachments.isNotEmpty() },
    content = @Composable { FileAttachmentContent(it) }
)

/**
 * Builds a file attachment message.
 *
 * @param attachmentState - The state of the attachment, holding the root modifier, the message
 * and the onLongItemClick handler.
 * */
@Composable
public fun FileAttachmentContent(attachmentState: AttachmentState) {
    val (modifier, messageItem, _) = attachmentState
    val (message, _) = messageItem

    Column(
        modifier = modifier
            .wrapContentHeight()
            .width(200.dp)
    ) {
        for (attachment in message.attachments) {
            Surface(
                modifier = Modifier
                    .padding(2.dp)
                    .fillMaxWidth(),
                color = ChatTheme.colors.appBackground, shape = ChatTheme.shapes.attachment
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FileAttachmentImage(attachment = attachment)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(start = 16.dp, end = 8.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = attachment.title ?: attachment.name ?: "",
                            style = ChatTheme.typography.bodyBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = ChatTheme.colors.textHighEmphasis
                        )

                        Text(
                            text = MediaStringUtil.convertFileSizeByteCount(attachment.fileSize.toLong()),
                            style = ChatTheme.typography.footnote,
                            color = ChatTheme.colors.textLowEmphasis
                        )
                    }

                    Icon(
                        modifier = Modifier
                            .align(Alignment.Top)
                            .padding(end = 2.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = rememberRipple(bounded = false)
                            ) {
                                ChatDomain
                                    .instance()
                                    .downloadAttachment(attachment)
                                    .enqueue()
                            },
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = stringResource(
                            id = R.string.stream_compose_download
                        ),
                        tint = ChatTheme.colors.textHighEmphasis
                    )
                }
            }
        }
    }
}
