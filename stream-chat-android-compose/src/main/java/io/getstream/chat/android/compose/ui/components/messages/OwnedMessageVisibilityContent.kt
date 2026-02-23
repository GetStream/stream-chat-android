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

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.Timestamp
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Message
import java.util.Date

/**
 * Shows the content that lets the user know that only they can see the message.
 *
 * @param message Message to show.
 * @param modifier Modifier for styling.
 */
@Composable
public fun OwnedMessageVisibilityContent(
    message: Message,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier
                .padding(end = 4.dp)
                .size(12.dp),
            painter = painterResource(id = R.drawable.stream_compose_ic_visible_to_you),
            contentDescription = null,
        )

        Text(
            text = stringResource(id = R.string.stream_compose_only_visible_to_you),
            style = ChatTheme.typography.footnote,
            color = ChatTheme.colors.textPrimary,
        )

        Timestamp(
            modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 4.dp),
            date = message.updatedAt ?: message.createdAt ?: Date(),
        )
    }
}
