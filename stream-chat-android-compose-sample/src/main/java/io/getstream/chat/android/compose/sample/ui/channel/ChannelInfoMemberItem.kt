package io.getstream.chat.android.compose.sample.ui.channel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.getLastSeenText
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.User

/**
 * Component rendering a member of a group channel.
 *
 * @param member The [Member] whose information is shown.
 * @param createdBy The [User] who create the channel.
 */
@Composable
fun ChannelInfoMemberItem(
    member: Member,
    createdBy: User,
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChatTheme.colors.barsBackground)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(
            modifier = Modifier.size(40.dp),
            user = member.user
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = member.user.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 14.sp,
                color = ChatTheme.colors.textHighEmphasis
            )
            Text(
                text = member.user.getLastSeenText(context),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp,
                color = ChatTheme.colors.textLowEmphasis
            )
        }
        val isOwner = member.user.id == createdBy.id
        val channelRole = when {
            isOwner -> stringResource(id = R.string.channel_group_info_owner)
            member.channelRole == "channel_member" -> stringResource(id = R.string.channel_group_info_member)
            member.channelRole == "channel_moderator" -> stringResource(id = R.string.channel_group_info_moderator)
            else -> member.channelRole
        }
        channelRole?.let {
            Text(
                text = channelRole,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp,
                color = ChatTheme.colors.textLowEmphasis
            )
        }
    }
}
