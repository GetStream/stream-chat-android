package io.getstream.chat.android.compose.ui.attachments.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.getstream.sdk.chat.utils.MediaStringUtil
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.uploader.ProgressTrackerFactory
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.common.LoadingView
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Represents the content when files are being uploaded.
 *
 * @param attachmentState The state of this attachment.
 */
@Composable
public fun FileUploadContent(
    attachmentState: AttachmentState,
    modifier: Modifier = Modifier,
) {
    val message = attachmentState.message

    Column(
        modifier = modifier
            .wrapContentHeight()
            .width(FILE_ATTACHMENT_WIDTH)
    ) {
        for (attachment in message.attachments) {
            FileUploadItem(attachment = attachment)
        }
    }
}

/**
 * Represents each uploading item, with its upload progress.
 *
 * @param attachment The attachment that's being uploaded.
 */
@Composable
public fun FileUploadItem(attachment: Attachment) {
    Surface(
        modifier = Modifier
            .padding(2.dp)
            .fillMaxWidth(),
        color = ChatTheme.colors.appBackground,
        shape = ChatTheme.shapes.attachment
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

                val uploadId = requireNotNull(attachment.uploadId)

                val tracker = ProgressTrackerFactory.getOrCreate(uploadId)

                val uploadProgress by tracker.currentProgress().collectAsState()
                val isComplete by tracker.isComplete().collectAsState()
                val maxValue = attachment.upload?.length() ?: tracker.maxValue

                if (!isComplete) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        LoadingView(modifier = Modifier.size(12.dp))

                        Spacer(modifier = Modifier.size(8.dp))

                        val uploadedSize = (uploadProgress / 100F * maxValue).toLong()
                        Text(
                            text = stringResource(
                                id = R.string.stream_compose_upload_progress,
                                MediaStringUtil.convertFileSizeByteCount(uploadedSize),
                                MediaStringUtil.convertFileSizeByteCount(maxValue),
                            ),
                            style = ChatTheme.typography.footnote,
                            color = ChatTheme.colors.textLowEmphasis
                        )
                    }
                } else {
                    Text(
                        text = MediaStringUtil.convertFileSizeByteCount(attachment.upload?.length() ?: 0L),
                        style = ChatTheme.typography.footnote,
                        color = ChatTheme.colors.textLowEmphasis
                    )
                }
            }
        }
    }
}
