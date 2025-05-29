package io.getstream.chat.android.compose.sample.feature.channel.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.User

@Composable
fun ChannelsScreenNavigationDrawer(
    currentUser: User?,
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
        currentUser?.let {
            LoggedInUserHeader(it)
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

@Composable
private fun LoggedInUserHeader(user: User) {
    Column {
        // Avatar and name
        Row(
            modifier = Modifier
                .height(64.dp)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UserAvatar(
                modifier = Modifier.size(40.dp),
                user = user,
            )
            Spacer(modifier = Modifier.size(12.dp))
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
                user.teams.forEach { team ->
                    builder.append(" - $team")
                    if (user.teamsRole[team] != null) {
                        builder.append(" (role = ${user.teamsRole[team]})")
                    }
                    builder.appendLine()
                }
                builder.toString()
            }
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                text = text,
                fontSize = 12.sp,
                color = ChatTheme.colors.textLowEmphasis,
            )
        } else {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
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
