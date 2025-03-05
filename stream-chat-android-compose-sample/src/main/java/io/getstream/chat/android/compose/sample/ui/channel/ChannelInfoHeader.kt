/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.sample.ui.channel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.getLastSeenText
import io.getstream.chat.android.models.Member

/**
 * Composable representing a header for the "Channel Info" screen. Shows an avatar, the username, and 'Last seen' info
 * the user. Additionally, shows a navigation button, which can be customized.
 *
 * @param member The [Member] for which the header is shown.
 * @param navigationIcon The icon to show for navigation.
 */
@Composable
fun ChannelInfoHeader(
    member: Member,
    navigationIcon: @Composable () -> Unit,
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChatTheme.colors.barsBackground),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            navigationIcon()
        }
        UserAvatar(
            modifier = Modifier.size(72.dp),
            user = member.user,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = member.user.name,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = ChatTheme.colors.textHighEmphasis,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = member.user.getLastSeenText(context),
            fontSize = 12.sp,
            color = ChatTheme.colors.textLowEmphasis,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        ChannelInfoContentDivider(height = 1.dp)
        UsernameSection(member = member)
    }
}

@Composable
private fun UsernameSection(member: Member) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "@${member.user.name.lowercase()}",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = ChatTheme.colors.textHighEmphasis,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(id = R.string.channel_info_user_name),
            fontSize = 14.sp,
            color = ChatTheme.colors.textLowEmphasis,
            textAlign = TextAlign.Center,
        )
    }
}
