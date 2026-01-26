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

package io.getstream.chat.android.compose.sample.feature.channel.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.sample.BuildConfig
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.ui.components.avatar.AvatarSize
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.User

@Suppress("LongParameterList")
@Composable
fun ChannelsScreenNavigationDrawer(
    currentUser: User?,
    onUserClick: () -> Unit,
    onNewDirectMessageClick: () -> Unit,
    onNewGroupClick: () -> Unit,
    onRemindersClick: () -> Unit,
    onSignOutClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .wrapContentWidth(),
    ) {
        // Currently logged in User
        currentUser?.let { user ->
            LoggedInUserHeader(
                user = user,
                onClick = onUserClick,
            )
        }
        // New Direct Message
        NavigationDrawerItem(
            icon = R.drawable.stream_compose_ic_new_chat,
            text = stringResource(R.string.navigation_drawer_new_direct_message),
            onClick = onNewDirectMessageClick,
        )
        // New Group
        NavigationDrawerItem(
            icon = R.drawable.ic_create_group,
            text = stringResource(R.string.navigation_drawer_new_group_chat),
            onClick = onNewGroupClick,
        )
        // Reminders
        NavigationDrawerItem(
            icon = R.drawable.ic_bookmark_24,
            text = stringResource(R.string.navigation_drawer_later),
            onClick = onRemindersClick,
        )
        // Fill content spacer
        Spacer(modifier = Modifier.weight(1f))
        // Sign Out
        NavigationDrawerItem(
            icon = R.drawable.stream_compose_ic_person,
            text = stringResource(R.string.navigation_drawer_sign_out),
            onClick = onSignOutClick,
        )
        // Version
        VersionFooter()
    }
}

@Suppress("LongMethod")
@Composable
private fun LoggedInUserHeader(
    user: User,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = null,
                indication = ripple(),
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Avatar and name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            UserAvatar(
                modifier = Modifier.size(AvatarSize.Large),
                user = user,
            )
            Text(
                text = user.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = ChatTheme.colors.textHighEmphasis,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        // Team membership
        if (user.teams.isNotEmpty()) {
            val text = remember(user.teams, user.teamsRole) {
                val builder = StringBuilder()
                builder.appendLine("Member of:")
                user.teams.forEachIndexed { index, team ->
                    builder.append(" - $team")
                    if (user.teamsRole[team] != null) {
                        builder.append(" (role = ${user.teamsRole[team]})")
                    }
                    if (index != user.teams.lastIndex) {
                        builder.appendLine()
                    }
                }
                builder.toString()
            }
            Text(
                text = text,
                fontSize = 12.sp,
                color = ChatTheme.colors.textLowEmphasis,
            )
        } else {
            Text(
                text = "No teams",
                fontSize = 12.sp,
                color = ChatTheme.colors.textLowEmphasis,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun NavigationDrawerItem(
    icon: Int,
    text: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = null,
                indication = ripple(),
                onClick = onClick,
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(icon),
            tint = ChatTheme.colors.textHighEmphasis,
            contentDescription = text,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            color = ChatTheme.colors.textHighEmphasis,
            fontSize = 14.sp,
            style = ChatTheme.typography.bodyBold,
        )
    }
}

@Composable
private fun VersionFooter() {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        textAlign = TextAlign.Center,
        text = BuildConfig.VERSION_NAME,
        color = ChatTheme.colors.textLowEmphasis,
        fontSize = 12.sp,
    )
}
