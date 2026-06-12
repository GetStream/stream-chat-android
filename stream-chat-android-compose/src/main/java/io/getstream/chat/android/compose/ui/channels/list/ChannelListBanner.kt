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

package io.getstream.chat.android.compose.ui.channels.list

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.clickable

/**
 * Banner showing an error icon and a "Tap to retry" prompt, invoking [onClick] when tapped. Meant to be pinned below
 * the channel list header while a channel load is in error.
 *
 * @param modifier [Modifier] instance for general styling.
 * @param onClick Action invoked when the banner is tapped.
 */
@Composable
internal fun ChannelListBanner(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val color = ChatTheme.colors.chatTextSystem

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ChatTheme.colors.backgroundCoreSurfaceDefault)
            .clickable(role = Role.Button) { onClick() }
            .padding(StreamTokens.spacingSm),
        horizontalArrangement = Arrangement.spacedBy(
            StreamTokens.spacingXs,
            Alignment.CenterHorizontally,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            painter = painterResource(R.drawable.stream_design_ic_exclamation_circle),
            contentDescription = null,
            tint = color,
        )
        Text(
            text = stringResource(R.string.stream_compose_channel_list_banner_error),
            style = ChatTheme.typography.metadataEmphasis,
            color = color,
        )
    }
}

@Composable
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun ChannelListBannerErrorPreview() {
    ChatPreviewTheme {
        Surface {
            ChannelListBanner(onClick = {})
        }
    }
}
