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

package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Message

/**
 * A footer indicating the current upload progress - how many items have been uploaded and what the total number of
 * items is.
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
        horizontalAlignment = Alignment.End,
    ) {
        OwnedMessageVisibilityContent(message = message)

        Text(
            text = stringResource(
                id = R.string.stream_compose_upload_file_count,
                uploadedCount + 1,
                totalCount,
            ),
            style = ChatTheme.typography.body,
            textAlign = TextAlign.End,
        )
    }
}
