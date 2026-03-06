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

package io.getstream.chat.android.compose.ui.components.attachments.files

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.ui.components.StreamHorizontalDivider
import io.getstream.chat.android.compose.ui.components.common.RadioButton
import io.getstream.chat.android.compose.ui.components.common.RadioCheck
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.ui.common.contract.internal.SelectFilesContract
import io.getstream.chat.android.ui.common.model.MimeType
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import io.getstream.chat.android.ui.common.utils.MediaStringUtil

/**
 * Shows the UI for files the user can pick for message attachments. Exposes the logic of selecting
 * items and browsing for extra files.
 *
 * @param files The files the user can pick, to be rendered in a list.
 * @param onItemSelected Handler when the user clicks on any file item.
 * @param onBrowseFilesResult Handler when the user clicks on the browse more files action.
 * @param modifier Modifier for styling.
 * @param allowMultipleSelection When `true`, users can select multiple files. When `false`,
 * only single file selection is allowed. Defaults to `true`.
 * @param itemContent Composable for rendering individual file items.
 */
@Composable
public fun FilesPicker(
    files: List<AttachmentPickerItemState>,
    onItemSelected: (AttachmentPickerItemState) -> Unit,
    onBrowseFilesResult: (List<Uri>) -> Unit,
    modifier: Modifier = Modifier,
    allowMultipleSelection: Boolean = true,
    itemContent: @Composable (AttachmentPickerItemState) -> Unit = {
        DefaultFilesPickerItem(
            fileItem = it,
            onItemSelected = onItemSelected,
            allowMultipleSelection = allowMultipleSelection,
        )
    },
) {
    val fileSelectContract = rememberLauncherForActivityResult(
        contract = SelectFilesContract(),
    ) {
        onBrowseFilesResult(it)
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { fileSelectContract.launch(allowMultipleSelection) }
                .padding(StreamTokens.spacingSm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(id = R.string.stream_compose_recent_files),
                style = ChatTheme.typography.bodyDefault,
                color = ChatTheme.colors.textPrimary,
            )
            Icon(
                painter = painterResource(id = R.drawable.stream_compose_ic_chevron_right),
                contentDescription = stringResource(id = R.string.stream_compose_send_attachment),
                tint = ChatTheme.colors.textSecondary,
            )
        }

        StreamHorizontalDivider()

        LazyColumn(modifier) {
            items(files) { fileItem -> itemContent(fileItem) }
        }
    }
}

/**
 * Represents a single item in the file picker list.
 *
 * @param fileItem File to render.
 * @param onItemSelected Handler when the item is selected.
 * @param allowMultipleSelection When `true`, shows a checkbox for multi-select. When `false`,
 * shows a radio button for single-select. Defaults to `true`.
 */
@Composable
internal fun DefaultFilesPickerItem(
    fileItem: AttachmentPickerItemState,
    onItemSelected: (AttachmentPickerItemState) -> Unit,
    allowMultipleSelection: Boolean = true,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onItemSelected(fileItem) }
            .padding(StreamTokens.spacingSm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingSm),
    ) {
        FilesPickerItemImage(
            fileItem = fileItem,
            modifier = Modifier.size(size = 40.dp),
        )

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs),
        ) {
            Text(
                text = fileItem.attachmentMetaData.title ?: "",
                style = ChatTheme.typography.bodyDefault,
                color = ChatTheme.colors.textPrimary,
            )
            Text(
                text = MediaStringUtil.convertFileSizeByteCount(fileItem.attachmentMetaData.size),
                style = ChatTheme.typography.captionDefault,
                color = ChatTheme.colors.textTertiary,
            )
        }

        if (allowMultipleSelection) {
            RadioCheck(
                checked = fileItem.isSelected,
                onCheckedChange = null,
            )
        } else {
            RadioButton(
                checked = fileItem.isSelected,
                onCheckedChange = null,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FilesPickerSingleSelectionPreview() {
    ChatTheme {
        FilesPickerSingleSelection()
    }
}

@Composable
internal fun FilesPickerSingleSelection() {
    FilesPicker(
        files = listOf(
            AttachmentPickerItemState(
                attachmentMetaData = AttachmentMetaData1,
            ),
            AttachmentPickerItemState(
                attachmentMetaData = AttachmentMetaData2,
                isSelected = true,
            ),
        ),
        onItemSelected = {},
        onBrowseFilesResult = {},
        allowMultipleSelection = false,
    )
}

@Preview(showBackground = true)
@Composable
private fun FilesPickerMultipleSelectionPreview() {
    ChatTheme {
        FilesPickerMultipleSelection()
    }
}

@Composable
internal fun FilesPickerMultipleSelection() {
    FilesPicker(
        files = listOf(
            AttachmentPickerItemState(
                attachmentMetaData = AttachmentMetaData1,
                isSelected = true,
            ),
            AttachmentPickerItemState(
                attachmentMetaData = AttachmentMetaData2,
                isSelected = true,
            ),
        ),
        onItemSelected = {},
        onBrowseFilesResult = {},
        allowMultipleSelection = true,
    )
}

@Suppress("MagicNumber")
private val AttachmentMetaData1 = AttachmentMetaData(
    title = "PDF",
    mimeType = MimeType.MIME_TYPE_PDF,
).apply {
    size = 10_000
}

@Suppress("MagicNumber")
private val AttachmentMetaData2 = AttachmentMetaData(
    title = "DOC",
    mimeType = MimeType.MIME_TYPE_DOC,
).apply {
    size = 100_000
}
