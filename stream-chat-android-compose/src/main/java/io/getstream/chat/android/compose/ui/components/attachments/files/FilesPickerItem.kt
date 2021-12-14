package io.getstream.chat.android.compose.ui.components.attachments.files

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.getstream.sdk.chat.utils.MediaStringUtil
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Represents a single item in the file picker list.
 *
 * @param fileItem File to render.
 * @param onItemSelected Handler when the item is selected.
 */
@Composable
internal fun FilesPickerItem(
    fileItem: AttachmentPickerItemState,
    onItemSelected: (AttachmentPickerItemState) -> Unit,
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

        FilesPickerItemImage(
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
