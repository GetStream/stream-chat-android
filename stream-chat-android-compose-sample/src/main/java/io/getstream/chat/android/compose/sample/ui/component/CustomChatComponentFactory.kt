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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.getstream.chat.android.client.extensions.isPinned
import io.getstream.chat.android.compose.sample.feature.reminders.MessageRemindersComponentFactory
import io.getstream.chat.android.client.utils.message.hasSharedLocation
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.compose.state.channels.list.ItemState
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResult
import io.getstream.chat.android.compose.ui.channels.list.ChannelItem
import io.getstream.chat.android.compose.ui.components.messages.factory.MessageContentFactory
import io.getstream.chat.android.compose.ui.messages.list.DefaultMessageItemCenterContent
import io.getstream.chat.android.compose.ui.theme.ChatComponentFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.ui.common.state.messages.list.GiphyAction
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import io.getstream.chat.android.ui.common.state.messages.poll.PollSelectionType

class CustomChatComponentFactory(
    private val delegate: ChatComponentFactory = MessageRemindersComponentFactory(),
) : ChatComponentFactory by delegate {

    @Composable
    override fun LazyItemScope.ChannelListItemContent(
        channelItem: ItemState.ChannelItemState,
        currentUser: User?,
        onChannelClick: (Channel) -> Unit,
        onChannelLongClick: (Channel) -> Unit,
    ) {
        ChannelItem(
            modifier = Modifier
                .animateItem()
                .run {
                    // Highlight the item background color if it is pinned
                    if (channelItem.channel.isPinned()) {
                        background(color = ChatTheme.colors.highlight)
                    } else {
                        this
                    }
                },
            channelItem = channelItem,
            currentUser = currentUser,
            onChannelClick = onChannelClick,
            onChannelLongClick = onChannelLongClick,
        )
    }

    @Composable
    override fun ColumnScope.MessageItemCenterContent(
        messageItem: MessageItemState,
        onLongItemClick: (Message) -> Unit,
        onPollUpdated: (Message, Poll) -> Unit,
        onCastVote: (Message, Poll, Option) -> Unit,
        onRemoveVote: (Message, Poll, Vote) -> Unit,
        selectPoll: (Message, Poll, PollSelectionType) -> Unit,
        onAddAnswer: (message: Message, poll: Poll, answer: String) -> Unit,
        onClosePoll: (String) -> Unit,
        onAddPollOption: (poll: Poll, option: String) -> Unit,
        onGiphyActionClick: (GiphyAction) -> Unit,
        onQuotedMessageClick: (Message) -> Unit,
        onLinkClick: ((Message, String) -> Unit)?,
        onUserMentionClick: (User) -> Unit,
        onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit,
    ) {
        val message = messageItem.message
        if (message.hasSharedLocation() && !message.isDeleted()) {
            val location = requireNotNull(message.sharedLocation)
            SharedLocationItem(
                modifier = Modifier.widthIn(max = ChatTheme.dimens.messageItemMaxWidth),
                message = message,
                location = location,
                onMapClick = { url -> onLinkClick?.invoke(message, url) },
            )
        } else {
            DefaultMessageItemCenterContent(
                messageItem = messageItem,
                messageContentFactory = MessageContentFactory.Deprecated,
                onLongItemClick = onLongItemClick,
                onGiphyActionClick = onGiphyActionClick,
                onQuotedMessageClick = onQuotedMessageClick,
                onLinkClick = onLinkClick,
                onUserMentionClick = onUserMentionClick,
                onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
                onPollUpdated = onPollUpdated,
                onCastVote = onCastVote,
                onRemoveVote = onRemoveVote,
                selectPoll = selectPoll,
                onAddAnswer = onAddAnswer,
                onClosePoll = onClosePoll,
                onAddPollOption = onAddPollOption,
            )
        }
    }
}
