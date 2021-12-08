package io.getstream.chat.android.compose.ui.components.attachments.files

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.getstream.sdk.chat.SelectFilesContract
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.ui.theme.ChatTheme

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
public fun FilesPicker(
    files: List<AttachmentPickerItemState>,
    onItemSelected: (AttachmentPickerItemState) -> Unit,
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
                        painter = painterResource(id = R.drawable.stream_compose_ic_more_files),
                        contentDescription = stringResource(id = R.string.stream_compose_send_attachment),
                        tint = ChatTheme.colors.primaryAccent,
                    )
                },
                onClick = { fileSelectContract.launch(Unit) }
            )
        }

        LazyColumn(modifier) {
            items(files) { fileItem -> FilesPickerItem(fileItem = fileItem, onItemSelected) }
        }
    }
}
