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

package io.getstream.chat.android.compose.ui.messages.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.EmptyContent
import io.getstream.chat.android.compose.ui.components.button.StreamButtonStyleDefaults
import io.getstream.chat.android.compose.ui.components.button.StreamTextButton
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens

/**
 * Shown in place of the message list when the channel is deleted while open.
 *
 * @param modifier Modifier for styling.
 * @param onBackClick Action for the back button, or `null` to hide it.
 */
@Composable
internal fun DefaultChannelDeletedContent(
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.background(color = ChatTheme.colors.backgroundCoreApp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        EmptyContent(
            text = stringResource(R.string.stream_compose_message_list_channel_deleted),
            painter = painterResource(R.drawable.stream_design_ic_message_bubble),
        )
        if (onBackClick != null) {
            Spacer(Modifier.size(StreamTokens.spacingLg))
            StreamTextButton(
                onClick = onBackClick,
                text = stringResource(R.string.stream_compose_message_list_channel_deleted_back),
                style = StreamButtonStyleDefaults.secondaryOutline,
            )
        }
    }
}

@Preview
@Composable
private fun ChannelDeletedContentWithBackButtonPreview() {
    ChatTheme {
        ChannelDeletedContentWithBackButton()
    }
}

@Composable
internal fun ChannelDeletedContentWithBackButton() {
    DefaultChannelDeletedContent(
        modifier = Modifier.fillMaxSize(),
        onBackClick = {},
    )
}

@Preview
@Composable
private fun ChannelDeletedContentWithoutBackButtonPreview() {
    ChatTheme {
        ChannelDeletedContentWithoutBackButton()
    }
}

@Composable
internal fun ChannelDeletedContentWithoutBackButton() {
    DefaultChannelDeletedContent(modifier = Modifier.fillMaxSize())
}
