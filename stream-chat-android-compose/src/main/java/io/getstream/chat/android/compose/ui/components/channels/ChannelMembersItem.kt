/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.components.channels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.compose.state.OnlineIndicatorAlignment
import io.getstream.chat.android.compose.ui.components.avatar.DefaultOnlineIndicator
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.utils.extensions.shouldShowOnlineIndicator

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
    currentUser: User? = null,
) {
    val memberName = member.user.name

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ChatTheme.componentFactory.UserAvatar(
            modifier = Modifier.size(ChatTheme.dimens.selectedChannelMenuUserItemAvatarSize),
            user = member.user,
            textStyle = ChatTheme.typography.title3Bold,
            showOnlineIndicator = member.user.shouldShowOnlineIndicator(
                userPresence = ChatTheme.userPresence,
                currentUser = currentUser,
            ),
            onlineIndicator = { DefaultOnlineIndicator(onlineIndicatorAlignment = OnlineIndicatorAlignment.TopEnd) },
            onClick = null,
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
