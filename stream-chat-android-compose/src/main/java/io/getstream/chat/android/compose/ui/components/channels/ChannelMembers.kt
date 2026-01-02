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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewMembersData

/**
 * Represents a list of members in the channel.
 *
 * @param members The list of channel members.
 * @param modifier Modifier for styling.
 * @param currentUser The currently logged-in user.
 */
@Composable
public fun ChannelMembers(
    members: List<Member>,
    modifier: Modifier = Modifier,
    currentUser: User? = null,
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.Center,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
    ) {
        items(members) { member ->
            ChannelMembersItem(
                modifier = Modifier
                    .width(ChatTheme.dimens.selectedChannelMenuUserItemWidth)
                    .padding(horizontal = ChatTheme.dimens.selectedChannelMenuUserItemHorizontalPadding),
                member = member,
                currentUser = currentUser,
            )
        }
    }
}

/**
 * Preview of [ChannelMembers] with one channel member.
 */
@Preview(showBackground = true, name = "ChannelMembers Preview (One member)")
@Composable
private fun OneMemberChannelMembersPreview() {
    ChatTheme {
        ChannelMembers(members = PreviewMembersData.oneMember)
    }
}

/**
 * Preview of [ChannelMembers] with many channel members.
 */
@Preview(showBackground = true, name = "ChannelMembers Preview (Many members)")
@Composable
private fun ManyMembersChannelMembersPreview() {
    ChatTheme {
        ChannelMembers(members = PreviewMembersData.manyMembers)
    }
}
