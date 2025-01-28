/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.sample.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.state.channels.list.ItemState
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResult
import io.getstream.chat.android.compose.ui.theme.ChatComponentFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.ReactionSorting
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.ui.common.state.messages.list.GiphyAction
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import io.getstream.chat.android.ui.common.state.messages.poll.PollSelectionType

class CustomChatComponentFactory : ChatComponentFactory {
    @Composable
    override fun RowScope.ChannelListHeaderLeadingContent(
        currentUser: User?,
        onAvatarClick: (User?) -> Unit,
    ) {
        Icon(
            imageVector = Icons.Rounded.Face,
            contentDescription = null,
        )
    }

    @Composable
    override fun RowScope.ChannelListHeaderCenterContent(
        connectionState: ConnectionState,
        title: String,
    ) {
        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .weight(1f),
            text = title,
            textAlign = TextAlign.Center,
            color = ChatTheme.colors.primaryAccent,
        )
    }

    @Composable
    override fun RowScope.ChannelListHeaderTrailingContent(
        onHeaderActionClick: () -> Unit,
    ) {
        Icon(
            imageVector = Icons.Rounded.ThumbUp,
            contentDescription = null,
        )
    }

    @Composable
    override fun RowScope.SearchInputLeadingIcon() {
        Icon(
            imageVector = Icons.Rounded.Build,
            contentDescription = null,
        )
    }

    @Composable
    override fun SearchInputLabel() {
        Text(
            text = "Search",
            color = ChatTheme.colors.textHighEmphasis,
        )
    }

    @Composable
    override fun ChannelListLoadingIndicator(modifier: Modifier) {
        Text(
            text = "Loading...",
            color = ChatTheme.colors.textHighEmphasis,
        )
    }

    @Composable
    override fun RowScope.ChannelItemLeadingContent(
        channelItem: ItemState.ChannelItemState,
        currentUser: User?,
    ) {
        Image(
            imageVector = Icons.Rounded.Face,
            contentDescription = null,
        )
    }

    @Composable
    override fun LazyItemScope.MessageListItemContent(
        messageItem: MessageItemState,
        reactionSorting: ReactionSorting,
        onPollUpdated: (Message, Poll) -> Unit,
        onCastVote: (Message, Poll, Option) -> Unit,
        onRemoveVote: (Message, Poll, Vote) -> Unit,
        selectPoll: (Message, Poll, PollSelectionType) -> Unit,
        onClosePoll: (String) -> Unit,
        onAddPollOption: (Poll, String) -> Unit,
        onLongItemClick: (Message) -> Unit,
        onThreadClick: (Message) -> Unit,
        onReactionsClick: (Message) -> Unit,
        onGiphyActionClick: (GiphyAction) -> Unit,
        onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit,
        onQuotedMessageClick: (Message) -> Unit,
        onUserAvatarClick: ((User) -> Unit)?,
        onMessageLinkClick: ((Message, String) -> Unit)?,
        onUserMentionClick: (User) -> Unit,
        onAddAnswer: (Message, Poll, String) -> Unit,
    ) {
        Text(text = messageItem.message.text)
    }
}
