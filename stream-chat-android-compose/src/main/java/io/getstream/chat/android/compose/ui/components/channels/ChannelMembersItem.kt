package io.getstream.chat.android.compose.ui.components.channels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.compose.previewdata.PreviewUserData
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * The UI component that shows a user avatar and user name, as a member of a channel.
 *
 * @param member The member data to show.
 * @param modifier Modifier for styling.
 */
@Composable
internal fun ChannelMembersItem(
    member: Member,
    modifier: Modifier = Modifier,
) {
    val memberName = member.user.name

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        UserAvatar(
            modifier = Modifier.size(ChatTheme.dimens.channelInfoUserItemAvatarSize),
            user = member.user,
            contentDescription = memberName
        )

        Text(
            text = memberName,
            style = ChatTheme.typography.footnoteBold,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            color = ChatTheme.colors.textHighEmphasis,
        )
    }
}

/**
 * Preview of [ChannelMembersItem].
 *
 * Should show user avatar and user name.
 */
@Preview(showBackground = true, name = "ChannelMembersItem Preview")
@Composable
private fun ChannelMemberItemPreview() {
    ChatTheme {
        ChannelMembersItem(Member(user = PreviewUserData.user1))
    }
}
