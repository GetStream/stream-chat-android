package io.getstream.chat.android.compose.ui.messages.attachments

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.getstream.sdk.chat.SelectFilesContract
import com.getstream.sdk.chat.utils.MediaStringUtil
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentItem
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.MimeTypeIconProvider

/**
 * Shows the UI for files the user can pick for message attachments. Exposes the logic of selecting
 * items and browsing for extra files.
 *
 * @param files The files the user can pick, to be rendered in a list.
 * @param onItemSelected Handler when the user clicks on any file item.
 * @param onBrowseFilesResult Handler when the user clicks on the browse more files action.
 * @param modifier Modifier for styling.
 */
@Composable
internal fun FilesPicker(
    files: List<AttachmentItem>,
    onItemSelected: (AttachmentItem) -> Unit,
    onBrowseFilesResult: (List<Uri>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val fileSelectContract = rememberLauncherForActivityResult(contract = SelectFilesContract()) {
        onBrowseFilesResult(it)
    }

    Column(modifier = modifier) {
        Row(Modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = stringResource(id = R.string.stream_compose_recent_files),
                style = ChatTheme.typography.bodyBold,
                color = ChatTheme.colors.textHighEmphasis,
            )

            Spacer(modifier = Modifier.weight(6f))

            IconButton(
                content = {
                    Icon(
                        imageVector = Icons.Default.AddCircleOutline,
                        contentDescription = stringResource(id = R.string.stream_compose_send_attachment),
                        tint = ChatTheme.colors.primaryAccent,
                    )
                },
                onClick = { fileSelectContract.launch(Unit) }
            )
        }

        LazyColumn(modifier) {
            items(files) { fileItem -> FileListItem(fileItem = fileItem, onItemSelected) }
        }
    }
}

/**
 * Represents a single item in the file picker list.
 *
 * @param fileItem File to render.
 * @param onItemSelected Handler when the item is selected.
 */
@Composable
public fun FileListItem(
    fileItem: AttachmentItem,
    onItemSelected: (AttachmentItem) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(),
                onClick = { onItemSelected(fileItem) }
            )
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.Center) {
            // TODO use a Canvas maybe to draw this UI, instead of using a checkbox.
            Checkbox(
                checked = fileItem.isSelected,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(
                    checkedColor = ChatTheme.colors.primaryAccent,
                    uncheckedColor = ChatTheme.colors.disabled,
                    checkmarkColor = Color.White,
                    disabledColor = ChatTheme.colors.disabled,
                    disabledIndeterminateColor = ChatTheme.colors.disabled
                ),
            )

//            if (fileItem.isSelected && fileItem.attachmentMetaData.selectedPosition != Int.MIN_VALUE) {
//                Text(
//                    text = fileItem.attachmentMetaData.selectedPosition.toString(),
//                    color = Color.White
//                )
//            }
        }

        FileItemImage(
            fileItem = fileItem,
            modifier = Modifier
                .padding(start = 16.dp)
                .size(size = 40.dp)
        )

        Column(
            modifier = Modifier.padding(start = 16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = fileItem.attachmentMetaData.title ?: "",
                style = ChatTheme.typography.bodyBold,
                color = ChatTheme.colors.textHighEmphasis,
            )

            Text(
                text = MediaStringUtil.convertFileSizeByteCount(fileItem.attachmentMetaData.size),
                style = ChatTheme.typography.footnote,
                color = ChatTheme.colors.textLowEmphasis,
            )
        }
    }
}

/**
 * Represents the image that's shown in file picker items. This can be either an image/icon that represents the file type
 * or a thumbnail in case the file type is an image.
 *
 * @param fileItem - The item we use to show the image.
 */
@Composable
public fun FileItemImage(
    fileItem: AttachmentItem,
    modifier: Modifier = Modifier,
) {
    val attachment = fileItem.attachmentMetaData
    val isImage = fileItem.attachmentMetaData.type == "image"

    val painter = if (isImage) {
        val dataToLoad = attachment.uri ?: attachment.file

        rememberImagePainter(dataToLoad)
    } else {
        painterResource(id = MimeTypeIconProvider.getIconRes(attachment.mimeType))
    }

    val shape = if (isImage) ChatTheme.shapes.imageThumbnail else null

    val imageModifier = modifier.let { baseModifier ->
        if (shape != null) baseModifier.clip(shape) else baseModifier
    }

    Image(
        modifier = imageModifier,
        painter = painter,
        contentDescription = null,
        contentScale = if (isImage) ContentScale.Crop else ContentScale.Fit
    )
}
