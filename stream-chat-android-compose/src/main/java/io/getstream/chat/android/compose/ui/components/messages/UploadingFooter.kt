package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * A footer indicating the current upload progress - how many items have been uploaded and what the total number of items
 * is.
 *
 * @param message The message to show the content of.
 * @param modifier Modifier for styling.
 */
@Composable
public fun UploadingFooter(
    message: Message,
    modifier: Modifier = Modifier,
) {
    val uploadedCount = message.attachments.count { it.uploadState is Attachment.UploadState.Success }
    val totalCount = message.attachments.size

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        OwnedMessageVisibilityContent(message = message)

        Text(
            text = stringResource(id = R.string.stream_compose_upload_file_count, uploadedCount + 1, totalCount),
            style = ChatTheme.typography.body,
            textAlign = TextAlign.End
        )
    }
}
