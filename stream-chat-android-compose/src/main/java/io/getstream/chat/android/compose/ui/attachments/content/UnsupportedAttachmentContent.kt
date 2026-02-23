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

package io.getstream.chat.android.compose.ui.attachments.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Represents fallback content for unsupported attachments.
 *
 * @param modifier Modifier for styling.
 */
@Composable
public fun UnsupportedAttachmentContent(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .padding(2.dp)
            .fillMaxWidth(),
        color = ChatTheme.colors.appBackground,
        shape = ChatTheme.shapes.attachment,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                modifier = Modifier.size(height = 40.dp, width = 35.dp),
                painter = painterResource(id = R.drawable.stream_compose_ic_file_generic),
                contentDescription = null,
                contentScale = ContentScale.Fit,
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp),
                text = stringResource(id = R.string.stream_compose_message_list_unsupported_attachment),
                style = ChatTheme.typography.bodyBold,
                overflow = TextOverflow.Ellipsis,
                color = ChatTheme.colors.textPrimary,
            )
        }
    }
}
