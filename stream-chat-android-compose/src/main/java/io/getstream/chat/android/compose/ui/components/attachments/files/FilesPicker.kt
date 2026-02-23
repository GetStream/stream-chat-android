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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.ui.theme.ChatTheme
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
                        modifier = Modifier.testTag("Stream_FindFilesButton"),
                        painter = painterResource(id = R.drawable.stream_compose_ic_more_files),
                        contentDescription = stringResource(id = R.string.stream_compose_send_attachment),
                        tint = ChatTheme.colors.primaryAccent,
                    )
                },
                onClick = { fileSelectContract.launch(allowMultipleSelection) },
            )
        }

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
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (allowMultipleSelection) {
                Checkbox(
                    checked = fileItem.isSelected,
                    onCheckedChange = null,
                    colors = CheckboxDefaults.colors(
                        checkedColor = ChatTheme.colors.primaryAccent,
                        uncheckedColor = ChatTheme.colors.disabled,
                        checkmarkColor = Color.White,
                        disabledCheckedColor = ChatTheme.colors.disabled,
                        disabledUncheckedColor = ChatTheme.colors.disabled,
                        disabledIndeterminateColor = ChatTheme.colors.disabled,
                    ),
                )
            } else {
                RadioButton(
                    selected = fileItem.isSelected,
                    onClick = null,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = ChatTheme.colors.primaryAccent,
                        unselectedColor = ChatTheme.colors.disabled,
                        disabledSelectedColor = ChatTheme.colors.disabled,
                        disabledUnselectedColor = ChatTheme.colors.disabled,
                    ),
                )
            }
        }

        FilesPickerItemImage(
            fileItem = fileItem,
            modifier = Modifier
                .padding(start = 16.dp)
                .size(size = 40.dp),
        )

        Column(
            modifier = Modifier.padding(start = 16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center,
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

@Preview(showBackground = true)
@Composable
private fun FilesPickerSingleSelectionPreview() {
    ChatTheme {
        FilesPickerSingleSelection()
    }
}

@Suppress("MagicNumber")
@Composable
internal fun FilesPickerSingleSelection() {
    FilesPicker(
        files = listOf(
            AttachmentPickerItemState(
                attachmentMetaData = AttachmentMetaData(mimeType = MimeType.MIME_TYPE_PDF).apply {
                    size = 10_000
                },
            ),
            AttachmentPickerItemState(
                attachmentMetaData = AttachmentMetaData(mimeType = MimeType.MIME_TYPE_DOC).apply {
                    size = 100_000
                },
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

@Suppress("MagicNumber")
@Composable
internal fun FilesPickerMultipleSelection() {
    FilesPicker(
        files = listOf(
            AttachmentPickerItemState(
                attachmentMetaData = AttachmentMetaData(mimeType = MimeType.MIME_TYPE_PDF).apply {
                    size = 10_000
                },
                isSelected = true,
            ),
            AttachmentPickerItemState(
                attachmentMetaData = AttachmentMetaData(mimeType = MimeType.MIME_TYPE_DOC).apply {
                    size = 100_000
                },
                isSelected = true,
            ),
        ),
        onItemSelected = {},
        onBrowseFilesResult = {},
        allowMultipleSelection = true,
    )
}
