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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Represents the default Swipe-to-reply icon.
 */
@Composable
internal fun SwipeToReplyIcon() {
    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .size(32.dp)
            .clip(CircleShape)
            .background(ChatTheme.colors.buttonSecondaryBg, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.stream_compose_ic_reply),
            contentDescription = "",
            tint = ChatTheme.colors.buttonSecondaryTextOnAccent,
        )
    }
}

@Preview
@Composable
private fun SwipeToReplyIconPreview() {
    ChatPreviewTheme {
        SwipeToReplyIcon()
    }
}
