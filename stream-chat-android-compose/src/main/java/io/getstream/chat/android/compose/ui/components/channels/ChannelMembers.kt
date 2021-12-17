package io.getstream.chat.android.compose.ui.components.channels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Represents a list of members in the channel.
 *
 * @param members The list of channel members.
 * @param modifier Modifier for styling.
 */
@Composable
public fun ChannelMembers(
    members: List<Member>,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.Center,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp)
    ) {
        items(members) { member ->
            ChannelMembersUserItem(
                modifier = Modifier
                    .width(ChatTheme.dimens.channelInfoUserItemWidth)
                    .padding(horizontal = ChatTheme.dimens.channelInfoUserItemHorizontalPadding),
                member = member,
            )
        }
    }
}
