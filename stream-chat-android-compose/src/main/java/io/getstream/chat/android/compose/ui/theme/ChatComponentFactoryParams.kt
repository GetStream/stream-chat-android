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

package io.getstream.chat.android.compose.ui.theme

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.state.channels.list.ItemState
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResult
import io.getstream.chat.android.compose.state.messageoptions.MessageOptionItemState
import io.getstream.chat.android.compose.state.messages.MessageReactionItemState
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.state.messages.attachments.CameraPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.CommandPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.FilePickerMode
import io.getstream.chat.android.compose.state.messages.attachments.GalleryPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.PollPickerMode
import io.getstream.chat.android.compose.state.reactionoptions.ReactionOptionItemState
import io.getstream.chat.android.compose.ui.components.reactions.ReactionIconSize
import io.getstream.chat.android.compose.ui.components.reactions.ReactionToggleSize
import io.getstream.chat.android.compose.ui.messages.attachments.AttachmentPickerActions
import io.getstream.chat.android.compose.ui.messages.composer.actions.AudioRecordingActions
import io.getstream.chat.android.compose.ui.messages.list.MessagesLazyListState
import io.getstream.chat.android.compose.ui.threads.ThreadListBannerState
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentsPickerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.LinkPreview
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.PollConfig
import io.getstream.chat.android.models.ReactionSorting
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoMemberViewEvent
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewAction
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewEvent
import io.getstream.chat.android.ui.common.model.MessageResult
import io.getstream.chat.android.ui.common.state.channel.attachments.ChannelAttachmentsViewState
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoViewState
import io.getstream.chat.android.ui.common.state.channel.info.MemberAction
import io.getstream.chat.android.ui.common.state.channels.actions.ChannelAction
import io.getstream.chat.android.ui.common.state.messages.MessageAction
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState
import io.getstream.chat.android.ui.common.state.messages.list.AudioPlayerState
import io.getstream.chat.android.ui.common.state.messages.list.ChannelHeaderViewState
import io.getstream.chat.android.ui.common.state.messages.list.DateSeparatorItemState
import io.getstream.chat.android.ui.common.state.messages.list.EmptyThreadPlaceholderItemState
import io.getstream.chat.android.ui.common.state.messages.list.GiphyAction
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import io.getstream.chat.android.ui.common.state.messages.list.MessageListItemState
import io.getstream.chat.android.ui.common.state.messages.list.MessageListState
import io.getstream.chat.android.ui.common.state.messages.list.ModeratedMessageItemState
import io.getstream.chat.android.ui.common.state.messages.list.StartOfTheChannelItemState
import io.getstream.chat.android.ui.common.state.messages.list.SystemMessageItemState
import io.getstream.chat.android.ui.common.state.messages.list.ThreadDateSeparatorItemState
import io.getstream.chat.android.ui.common.state.messages.list.TypingItemState
import io.getstream.chat.android.ui.common.state.messages.list.UnreadSeparatorItemState
import io.getstream.chat.android.ui.common.state.messages.poll.PollSelectionType
import io.getstream.chat.android.ui.common.utils.ExpandableList

/**
 * Parameters for [ChatComponentFactory.ChannelListHeader].
 *
 * @param connectionState The current connection state.
 * @param modifier Modifier for styling.
 * @param title The title to display in the header.
 * @param currentUser The currently logged in user.
 * @param onAvatarClick Action invoked when the avatar is clicked.
 * @param onHeaderActionClick Action invoked when the header action is clicked.
 */
public data class ChannelListHeaderParams(
    val connectionState: ConnectionState,
    val modifier: Modifier = Modifier,
    val title: String = "",
    val currentUser: User? = null,
    val onAvatarClick: (User?) -> Unit = {},
    val onHeaderActionClick: () -> Unit = {},
)

/**
 * Parameters for [ChatComponentFactory.ChannelListHeaderLeadingContent].
 *
 * @param currentUser The currently logged in user.
 * @param onAvatarClick Action invoked when the avatar is clicked.
 */
public data class ChannelListHeaderLeadingContentParams(
    val currentUser: User?,
    val onAvatarClick: (User?) -> Unit,
)

/**
 * Parameters for [ChatComponentFactory.ChannelListHeaderCenterContent].
 *
 * @param connectionState The current connection state.
 * @param title The title to display.
 */
public data class ChannelListHeaderCenterContentParams(
    val connectionState: ConnectionState,
    val title: String,
)

/**
 * Parameters for [ChatComponentFactory.ChannelListHeaderTrailingContent].
 *
 * @param onHeaderActionClick Action invoked when the header action is clicked.
 */
public data class ChannelListHeaderTrailingContentParams(
    val onHeaderActionClick: () -> Unit,
)

/**
 * Parameters for [ChatComponentFactory.ChannelListLoadingIndicator].
 *
 * @param modifier Modifier for styling.
 */
public data class ChannelListLoadingIndicatorParams(
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.ChannelListEmptyContent].
 *
 * @param modifier Modifier for styling.
 * @param onStartChatClick Optional callback for the "Start a chat" button.
 */
public data class ChannelListEmptyContentParams(
    val modifier: Modifier = Modifier,
    val onStartChatClick: (() -> Unit)? = null,
)

/**
 * Parameters for [ChatComponentFactory.ChannelListItemContent].
 *
 * @param channelItem The channel item state.
 * @param currentUser The currently logged in user.
 * @param onChannelClick Action invoked when the channel is clicked.
 * @param onChannelLongClick Action invoked when the channel is long-clicked.
 */
public data class ChannelListItemContentParams(
    val channelItem: ItemState.ChannelItemState,
    val currentUser: User?,
    val onChannelClick: (Channel) -> Unit,
    val onChannelLongClick: (Channel) -> Unit,
)

/**
 * Parameters for [ChatComponentFactory.ChannelSwipeActions].
 *
 * @param channelItem The channel item to build actions for.
 */
public data class ChannelSwipeActionsParams(
    val channelItem: ItemState.ChannelItemState,
)

/**
 * Parameters for [ChatComponentFactory.ChannelListHelperContent].
 */
public class ChannelListHelperContentParams

/**
 * Parameters for [ChatComponentFactory.ChannelListLoadingMoreItemContent].
 */
public class ChannelListLoadingMoreItemContentParams

/**
 * Parameters for [ChatComponentFactory.ChannelListDividerItem].
 */
public class ChannelListDividerItemParams

/**
 * Parameters for [ChatComponentFactory.ChannelItemLeadingContent].
 *
 * @param channelItem The channel item state.
 * @param currentUser The currently logged in user.
 */
public data class ChannelItemLeadingContentParams(
    val channelItem: ItemState.ChannelItemState,
    val currentUser: User?,
)

/**
 * Parameters for [ChatComponentFactory.ChannelItemCenterContent].
 *
 * @param channelItem The channel item state.
 * @param currentUser The currently logged in user.
 */
public data class ChannelItemCenterContentParams(
    val channelItem: ItemState.ChannelItemState,
    val currentUser: User?,
)

/**
 * Parameters for [ChatComponentFactory.ChannelItemTrailingContent].
 *
 * @param channelItem The channel item state.
 * @param currentUser The currently logged in user.
 */
public data class ChannelItemTrailingContentParams(
    val channelItem: ItemState.ChannelItemState,
    val currentUser: User?,
)

/**
 * Parameters for [ChatComponentFactory.ChannelItemUnreadCountIndicator].
 *
 * @param unreadCount The number of unread messages.
 * @param modifier Modifier for styling.
 */
public data class ChannelItemUnreadCountIndicatorParams(
    val unreadCount: Int,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.ChannelItemReadStatusIndicator].
 *
 * @param channel The channel containing the message.
 * @param message The message to check read status for.
 * @param currentUser The currently logged in user.
 * @param modifier Modifier for styling.
 */
public data class ChannelItemReadStatusIndicatorParams(
    val channel: Channel,
    val message: Message,
    val currentUser: User?,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.ChannelListSearchInput].
 *
 * @param query Current query string.
 * @param onValueChange Action invoked when the query value changes.
 * @param modifier Modifier for styling.
 * @param onSearchStarted Action invoked when the search starts.
 */
public data class ChannelListSearchInputParams(
    val query: String,
    val onValueChange: (String) -> Unit,
    val modifier: Modifier = Modifier,
    val onSearchStarted: () -> Unit = {},
)

/**
 * Parameters for [ChatComponentFactory.SearchInputLeadingIcon].
 */
public class SearchInputLeadingIconParams

/**
 * Parameters for [ChatComponentFactory.SearchInputLabel].
 */
public class SearchInputLabelParams

/**
 * Parameters for [ChatComponentFactory.SearchInputClearButton].
 *
 * @param onClick Action invoked when the clear button is clicked.
 */
public data class SearchInputClearButtonParams(
    val onClick: () -> Unit,
)

/**
 * Parameters for [ChatComponentFactory.ChannelListEmptySearchContent].
 *
 * @param searchQuery The current search query.
 * @param modifier Modifier for styling.
 */
public data class ChannelListEmptySearchContentParams(
    val searchQuery: String,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.SearchResultItemContent].
 *
 * @param searchResultItem The search result item state.
 * @param currentUser The currently logged in user.
 * @param onSearchResultClick Action invoked when the search result is clicked.
 */
public data class SearchResultItemContentParams(
    val searchResultItem: ItemState.SearchResultItemState,
    val currentUser: User?,
    val onSearchResultClick: (Message) -> Unit,
)

/**
 * Parameters for [ChatComponentFactory.SearchResultItemLeadingContent].
 *
 * @param searchResultItem The search result item state.
 * @param currentUser The currently logged in user.
 */
public data class SearchResultItemLeadingContentParams(
    val searchResultItem: ItemState.SearchResultItemState,
    val currentUser: User?,
)

/**
 * Parameters for [ChatComponentFactory.SearchResultItemCenterContent].
 *
 * @param searchResultItem The search result item state.
 * @param currentUser The currently logged in user.
 */
public data class SearchResultItemCenterContentParams(
    val searchResultItem: ItemState.SearchResultItemState,
    val currentUser: User?,
)

/**
 * Parameters for [ChatComponentFactory.SearchResultItemTrailingContent].
 *
 * @param searchResultItem The search result item state.
 */
public data class SearchResultItemTrailingContentParams(
    val searchResultItem: ItemState.SearchResultItemState,
)

/**
 * Parameters for [ChatComponentFactory.MessageListHeader].
 *
 * @param channel The channel to display header for.
 * @param connectionState The current connection state.
 * @param modifier Modifier for styling.
 * @param currentUser The currently logged in user.
 * @param typingUsers The list of users currently typing.
 * @param messageMode The current message mode.
 * @param onBackPressed Action invoked when the back button is pressed.
 * @param onHeaderTitleClick Action invoked when the header title is clicked.
 * @param onChannelAvatarClick Action invoked when the channel avatar is clicked.
 */
public data class MessageListHeaderParams(
    val channel: Channel,
    val connectionState: ConnectionState,
    val modifier: Modifier = Modifier,
    val currentUser: User? = null,
    val typingUsers: List<User> = emptyList(),
    val messageMode: MessageMode = MessageMode.Normal,
    val onBackPressed: () -> Unit = {},
    val onHeaderTitleClick: ((Channel) -> Unit)? = null,
    val onChannelAvatarClick: ((Channel) -> Unit)? = null,
)

/**
 * Parameters for [ChatComponentFactory.MessageListHeaderLeadingContent].
 *
 * @param onBackPressed Action invoked when the back button is pressed.
 */
public data class MessageListHeaderLeadingContentParams(
    val onBackPressed: () -> Unit,
)

/**
 * Parameters for [ChatComponentFactory.MessageListHeaderCenterContent].
 *
 * @param channel The channel to display.
 * @param connectionState The current connection state.
 * @param modifier Modifier for styling.
 * @param currentUser The currently logged in user.
 * @param typingUsers The list of users currently typing.
 * @param messageMode The current message mode.
 * @param onClick Action invoked when the header is clicked.
 */
public data class MessageListHeaderCenterContentParams(
    val channel: Channel,
    val connectionState: ConnectionState,
    val modifier: Modifier = Modifier,
    val currentUser: User? = null,
    val typingUsers: List<User> = emptyList(),
    val messageMode: MessageMode = MessageMode.Normal,
    val onClick: ((Channel) -> Unit)? = null,
)

/**
 * Parameters for [ChatComponentFactory.MessageListHeaderTrailingContent].
 *
 * @param channel The channel to display.
 * @param currentUser The currently logged in user.
 * @param onClick Action invoked when the trailing content is clicked.
 */
public data class MessageListHeaderTrailingContentParams(
    val channel: Channel,
    val currentUser: User?,
    val onClick: ((Channel) -> Unit)? = null,
)

/**
 * Parameters for [ChatComponentFactory.MessageListBackground].
 */
public class MessageListBackgroundParams

/**
 * Parameters for [ChatComponentFactory.MessageListLoadingIndicator].
 *
 * @param modifier Modifier for styling.
 */
public data class MessageListLoadingIndicatorParams(
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.MessageListEmptyContent].
 *
 * @param modifier Modifier for styling.
 */
public data class MessageListEmptyContentParams(
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.MessageListHelperContent].
 *
 * @param messageListState The current state of the message list.
 * @param messagesLazyListState The lazy list state for scrolling.
 * @param contentPadding The content padding of the message list.
 * @param onScrollToBottomClick Action invoked when the scroll to bottom button is clicked.
 */
public data class MessageListHelperContentParams(
    val messageListState: MessageListState,
    val messagesLazyListState: MessagesLazyListState,
    val contentPadding: PaddingValues,
    val onScrollToBottomClick: (() -> Unit) -> Unit,
)

/**
 * Parameters for [ChatComponentFactory.ScrollToBottomButton].
 *
 * @param visible Whether the button is visible.
 * @param count The unread message count to display.
 * @param onClick Action invoked when the button is clicked.
 * @param modifier Modifier for styling.
 */
public data class ScrollToBottomButtonParams(
    val visible: Boolean,
    val count: Int,
    val onClick: () -> Unit,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.MessageItem].
 *
 * @param messageListItem The message list item state.
 * @param reactionSorting The sorting for reactions.
 * @param onPollUpdated Action invoked when a poll is updated.
 * @param onCastVote Action invoked when a vote is cast.
 * @param onRemoveVote Action invoked when a vote is removed.
 * @param selectPoll Action invoked when a poll selection is made.
 * @param onClosePoll Action invoked when a poll is closed.
 * @param onAddPollOption Action invoked when a poll option is added.
 * @param onLongItemClick Action invoked when a message is long-clicked.
 * @param onThreadClick Action invoked when the thread is clicked.
 * @param onReactionsClick Action invoked when reactions are clicked.
 * @param onGiphyActionClick Action invoked when a Giphy action is clicked.
 * @param onMediaGalleryPreviewResult Action invoked with the media gallery preview result.
 * @param onQuotedMessageClick Action invoked when a quoted message is clicked.
 * @param onUserMentionClick Action invoked when a user mention is clicked.
 * @param onAddAnswer Action invoked when an answer is added to a poll.
 * @param onReply Action invoked when the reply button is clicked.
 * @param onUserAvatarClick Action invoked when a user avatar is clicked.
 * @param onMessageLinkClick Action invoked when a link in a message is clicked.
 */
public data class MessageItemParams(
    val messageListItem: MessageListItemState,
    val reactionSorting: ReactionSorting,
    val onPollUpdated: (Message, Poll) -> Unit = { _, _ -> },
    val onCastVote: (Message, Poll, Option) -> Unit = { _, _, _ -> },
    val onRemoveVote: (Message, Poll, Vote) -> Unit = { _, _, _ -> },
    val selectPoll: (Message, Poll, PollSelectionType) -> Unit = { _, _, _ -> },
    val onClosePoll: (String) -> Unit = {},
    val onAddPollOption: (Poll, String) -> Unit = { _, _ -> },
    val onLongItemClick: (Message) -> Unit = {},
    val onThreadClick: (Message) -> Unit = {},
    val onReactionsClick: (Message) -> Unit = {},
    val onGiphyActionClick: (GiphyAction) -> Unit = {},
    val onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
    val onQuotedMessageClick: (Message) -> Unit = {},
    val onUserMentionClick: (User) -> Unit = {},
    val onAddAnswer: (Message, Poll, String) -> Unit = { _, _, _ -> },
    val onReply: (Message) -> Unit = {},
    val onUserAvatarClick: ((User) -> Unit)? = null,
    val onMessageLinkClick: ((Message, String) -> Unit)? = null,
)

/**
 * Parameters for [ChatComponentFactory.messageListItemModifier].
 */
public class MessageListItemModifierParams

/**
 * Parameters for [ChatComponentFactory.MessageListLoadingMoreItemContent].
 */
public class MessageListLoadingMoreItemContentParams

/**
 * Parameters for [ChatComponentFactory.MessageListDateSeparatorItemContent].
 *
 * @param dateSeparatorItem The date separator item state.
 */
public data class MessageListDateSeparatorItemContentParams(
    val dateSeparatorItem: DateSeparatorItemState,
)

/**
 * Parameters for [ChatComponentFactory.MessageListUnreadSeparatorItemContent].
 *
 * @param unreadSeparatorItem The unread separator item state.
 */
public data class MessageListUnreadSeparatorItemContentParams(
    val unreadSeparatorItem: UnreadSeparatorItemState,
)

/**
 * Parameters for [ChatComponentFactory.MessageListThreadDateSeparatorItemContent].
 *
 * @param threadDateSeparatorItem The thread date separator item state.
 */
public data class MessageListThreadDateSeparatorItemContentParams(
    val threadDateSeparatorItem: ThreadDateSeparatorItemState,
)

/**
 * Parameters for [ChatComponentFactory.MessageListSystemItemContent].
 *
 * @param systemMessageItem The system message item state.
 */
public data class MessageListSystemItemContentParams(
    val systemMessageItem: SystemMessageItemState,
)

/**
 * Parameters for [ChatComponentFactory.MessageListModeratedItemContent].
 *
 * @param moderatedMessageItem The moderated message item state.
 */
public data class MessageListModeratedItemContentParams(
    val moderatedMessageItem: ModeratedMessageItemState,
)

/**
 * Parameters for [ChatComponentFactory.MessageListTypingIndicatorItemContent].
 *
 * @param typingItem The typing item state.
 */
public data class MessageListTypingIndicatorItemContentParams(
    val typingItem: TypingItemState,
)

/**
 * Parameters for [ChatComponentFactory.MessageListEmptyThreadPlaceholderItemContent].
 *
 * @param emptyThreadPlaceholderItem The empty thread placeholder item state.
 */
public data class MessageListEmptyThreadPlaceholderItemContentParams(
    val emptyThreadPlaceholderItem: EmptyThreadPlaceholderItemState,
)

/**
 * Parameters for [ChatComponentFactory.MessageListStartOfTheChannelItemContent].
 *
 * @param startOfTheChannelItem The start of the channel item state.
 */
public data class MessageListStartOfTheChannelItemContentParams(
    val startOfTheChannelItem: StartOfTheChannelItemState,
)

/**
 * Parameters for [ChatComponentFactory.MessageContainer].
 *
 * @param messageItem The message item state.
 * @param reactionSorting The sorting for reactions.
 * @param onPollUpdated Action invoked when a poll is updated.
 * @param onCastVote Action invoked when a vote is cast.
 * @param onRemoveVote Action invoked when a vote is removed.
 * @param selectPoll Action invoked when a poll selection is made.
 * @param onClosePoll Action invoked when a poll is closed.
 * @param onAddPollOption Action invoked when a poll option is added.
 * @param onLongItemClick Action invoked when a message is long-clicked.
 * @param onThreadClick Action invoked when the thread is clicked.
 * @param onReactionsClick Action invoked when reactions are clicked.
 * @param onGiphyActionClick Action invoked when a Giphy action is clicked.
 * @param onMediaGalleryPreviewResult Action invoked with the media gallery preview result.
 * @param onQuotedMessageClick Action invoked when a quoted message is clicked.
 * @param onUserMentionClick Action invoked when a user mention is clicked.
 * @param onAddAnswer Action invoked when an answer is added to a poll.
 * @param modifier Modifier for styling.
 * @param onPollUpdated Action invoked when a poll is updated.
 * @param onCastVote Action invoked when a vote is cast.
 * @param onRemoveVote Action invoked when a vote is removed.
 * @param selectPoll Action invoked when a poll selection is made.
 * @param onClosePoll Action invoked when a poll is closed.
 * @param onAddPollOption Action invoked when a poll option is added.
 * @param onLongItemClick Action invoked when a message is long-clicked.
 * @param onThreadClick Action invoked when the thread is clicked.
 * @param onReactionsClick Action invoked when reactions are clicked.
 * @param onGiphyActionClick Action invoked when a Giphy action is clicked.
 * @param onMediaGalleryPreviewResult Action invoked with the media gallery preview result.
 * @param onQuotedMessageClick Action invoked when a quoted message is clicked.
 * @param onUserMentionClick Action invoked when a user mention is clicked.
 * @param onAddAnswer Action invoked when an answer is added to a poll.
 * @param onReply Action invoked when the reply button is clicked.
 * @param onUserAvatarClick Action invoked when a user avatar is clicked.
 * @param onMessageLinkClick Action invoked when a link in a message is clicked.
 */
public data class MessageContainerParams(
    val messageItem: MessageItemState,
    val reactionSorting: ReactionSorting,
    val modifier: Modifier = Modifier,
    val onPollUpdated: (Message, Poll) -> Unit = { _, _ -> },
    val onCastVote: (Message, Poll, Option) -> Unit = { _, _, _ -> },
    val onRemoveVote: (Message, Poll, Vote) -> Unit = { _, _, _ -> },
    val selectPoll: (Message, Poll, PollSelectionType) -> Unit = { _, _, _ -> },
    val onClosePoll: (String) -> Unit = {},
    val onAddPollOption: (Poll, String) -> Unit = { _, _ -> },
    val onLongItemClick: (Message) -> Unit = {},
    val onThreadClick: (Message) -> Unit = {},
    val onReactionsClick: (Message) -> Unit = {},
    val onGiphyActionClick: (GiphyAction) -> Unit = {},
    val onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
    val onQuotedMessageClick: (Message) -> Unit = {},
    val onUserMentionClick: (User) -> Unit = {},
    val onAddAnswer: (Message, Poll, String) -> Unit = { _, _, _ -> },
    val onReply: (Message) -> Unit = {},
    val onUserAvatarClick: ((User) -> Unit)? = null,
    val onMessageLinkClick: ((Message, String) -> Unit)? = null,
)

/**
 * Parameters for [ChatComponentFactory.MessageBubble].
 *
 * @param message The message to render inside the bubble.
 * @param color The color of the message bubble.
 * @param shape The shape of the message bubble.
 * @param content The content shown inside the message bubble.
 * @param modifier Modifier for styling.
 * @param border The border of the message bubble.
 */
public data class MessageBubbleParams(
    val message: Message,
    val color: Color,
    val shape: Shape,
    val content: @Composable () -> Unit,
    val modifier: Modifier = Modifier,
    val border: BorderStroke? = null,
)

/**
 * Parameters for [ChatComponentFactory.MessageFailedIcon].
 *
 * @param message The message that failed to send.
 * @param modifier Modifier for styling.
 */
public data class MessageFailedIconParams(
    val message: Message,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.MessageTop].
 *
 * @param messageItem The message item state.
 * @param onThreadClick Action invoked when the thread is clicked.
 */
public data class MessageTopParams(
    val messageItem: MessageItemState,
    val onThreadClick: (Message) -> Unit,
)

/**
 * Parameters for [ChatComponentFactory.MessageBottom].
 *
 * @param messageItem The message item state.
 */
public data class MessageBottomParams(
    val messageItem: MessageItemState,
)

/**
 * Parameters for [ChatComponentFactory.MessageAuthor].
 *
 * @param messageItem The message item state.
 * @param onUserAvatarClick Action invoked when the user avatar is clicked.
 */
public data class MessageAuthorParams(
    val messageItem: MessageItemState,
    val onUserAvatarClick: (() -> Unit)? = null,
)

/**
 * Parameters for [ChatComponentFactory.MessageContent].
 *
 * @param messageItem The message item state.
 * @param onLongItemClick Action invoked when a message is long-clicked.
 * @param onPollUpdated Action invoked when a poll is updated.
 * @param onCastVote Action invoked when a vote is cast.
 * @param onRemoveVote Action invoked when a vote is removed.
 * @param selectPoll Action invoked when a poll selection is made.
 * @param onAddAnswer Action invoked when an answer is added to a poll.
 * @param onClosePoll Action invoked when a poll is closed.
 * @param onAddPollOption Action invoked when a poll option is added.
 * @param onGiphyActionClick Action invoked when a Giphy action is clicked.
 * @param onQuotedMessageClick Action invoked when a quoted message is clicked.
 * @param onUserMentionClick Action invoked when a user mention is clicked.
 * @param onMediaGalleryPreviewResult Action invoked with the media gallery preview result.
 * @param onLinkClick Action invoked when a link in a message is clicked.
 */
public data class MessageContentParams(
    val messageItem: MessageItemState,
    val onLongItemClick: (Message) -> Unit = {},
    val onPollUpdated: (Message, Poll) -> Unit = { _, _ -> },
    val onCastVote: (Message, Poll, Option) -> Unit = { _, _, _ -> },
    val onRemoveVote: (Message, Poll, Vote) -> Unit = { _, _, _ -> },
    val selectPoll: (Message, Poll, PollSelectionType) -> Unit = { _, _, _ -> },
    val onAddAnswer: (Message, Poll, String) -> Unit = { _, _, _ -> },
    val onClosePoll: (String) -> Unit = {},
    val onAddPollOption: (Poll, String) -> Unit = { _, _ -> },
    val onGiphyActionClick: (GiphyAction) -> Unit = {},
    val onQuotedMessageClick: (Message) -> Unit = {},
    val onUserMentionClick: (User) -> Unit = {},
    val onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
    val onLinkClick: ((Message, String) -> Unit)? = null,
)

/**
 * Parameters for [ChatComponentFactory.MessageSpacer].
 *
 * @param messageItem The message item state.
 */
public data class MessageSpacerParams(
    val messageItem: MessageItemState,
)

/**
 * Parameters for [ChatComponentFactory.MessageGiphyContent].
 *
 * @param message The message containing the Giphy attachment.
 * @param currentUser The currently logged in user.
 * @param onGiphyActionClick Action invoked when a Giphy action is clicked.
 */
public data class MessageGiphyContentParams(
    val message: Message,
    val currentUser: User?,
    val onGiphyActionClick: (GiphyAction) -> Unit,
)

/**
 * Parameters for [ChatComponentFactory.MessageDeletedContent].
 *
 * @param message The deleted message.
 * @param currentUser The currently logged in user.
 * @param modifier Modifier for styling.
 */
public data class MessageDeletedContentParams(
    val message: Message,
    val currentUser: User?,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.MessageRegularContent].
 *
 * @param message The message to display.
 * @param currentUser The currently logged in user.
 * @param onLongItemClick Action invoked when a message is long-clicked.
 * @param onMediaGalleryPreviewResult Action invoked with the media gallery preview result.
 * @param onQuotedMessageClick Action invoked when a quoted message is clicked.
 * @param onUserMentionClick Action invoked when a user mention is clicked.
 * @param onLinkClick Action invoked when a link in a message is clicked.
 */
public data class MessageRegularContentParams(
    val message: Message,
    val currentUser: User?,
    val onLongItemClick: (Message) -> Unit,
    val onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit,
    val onQuotedMessageClick: (Message) -> Unit,
    val onUserMentionClick: (User) -> Unit,
    val onLinkClick: ((Message, String) -> Unit)? = null,
)

/**
 * Parameters for [ChatComponentFactory.MessageTextContent].
 *
 * @param message The message containing the text.
 * @param currentUser The currently logged in user.
 * @param onLongItemClick Action invoked when a message is long-clicked.
 * @param onUserMentionClick Action invoked when a user mention is clicked.
 * @param modifier Modifier for styling.
 * @param onLinkClick Action invoked when a link in a message is clicked.
 */
public data class MessageTextContentParams(
    val message: Message,
    val currentUser: User?,
    val onLongItemClick: (Message) -> Unit,
    val onUserMentionClick: (User) -> Unit,
    val modifier: Modifier = Modifier,
    val onLinkClick: ((Message, String) -> Unit)? = null,
)

/**
 * Parameters for [ChatComponentFactory.MessageQuotedContent].
 *
 * @param message The message that contains the quote.
 * @param currentUser The currently logged in user.
 * @param replyMessage The quoted message.
 * @param onLongItemClick Action invoked when a message is long-clicked.
 * @param onQuotedMessageClick Action invoked when the quoted message is clicked.
 * @param modifier Modifier for styling.
 */
public data class MessageQuotedContentParams(
    val message: Message,
    val currentUser: User?,
    val replyMessage: Message,
    val onLongItemClick: (Message) -> Unit,
    val onQuotedMessageClick: (Message) -> Unit,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.MessageFooterUploadingContent].
 *
 * @param messageItem The message item state.
 * @param modifier Modifier for styling.
 */
public data class MessageFooterUploadingContentParams(
    val messageItem: MessageItemState,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.MessageFooterOnlyVisibleToYouContent].
 *
 * @param messageItem The message item state.
 */
public data class MessageFooterOnlyVisibleToYouContentParams(
    val messageItem: MessageItemState,
)

/**
 * Parameters for [ChatComponentFactory.MessageFooterContent].
 *
 * @param messageItem The message item state.
 */
public data class MessageFooterContentParams(
    val messageItem: MessageItemState,
)

/**
 * Parameters for [ChatComponentFactory.SwipeToReplyContent].
 */
public class SwipeToReplyContentParams

/**
 * Parameters for [ChatComponentFactory.MessageComposer].
 *
 * @param messageComposerState The current state of the message composer.
 * @param input The composable input content.
 * @param modifier Modifier for styling.
 * @param isAttachmentPickerVisible Whether the attachment picker is visible.
 * @param onSendMessage Action invoked when a message is sent.
 * @param onAttachmentsClick Action invoked when attachments button is clicked.
 * @param onValueChange Action invoked when the input value changes.
 * @param onAttachmentRemoved Action invoked when an attachment is removed.
 * @param onCancelAction Action invoked when the cancel button is clicked.
 * @param onUserSelected Action invoked when a user is selected.
 * @param onCommandSelected Action invoked when a command is selected.
 * @param onAlsoSendToChannelSelected Action invoked when also-send-to-channel is changed.
 * @param recordingActions The actions to control the audio recording.
 * @param onLinkPreviewClick Action invoked when a link preview is clicked.
 */
public data class MessageComposerParams(
    val messageComposerState: MessageComposerState,
    val input: @Composable RowScope.(MessageComposerState) -> Unit,
    val modifier: Modifier = Modifier,
    val isAttachmentPickerVisible: Boolean = false,
    val onSendMessage: (String, List<Attachment>) -> Unit = { _, _ -> },
    val onAttachmentsClick: () -> Unit = {},
    val onValueChange: (String) -> Unit = {},
    val onAttachmentRemoved: (Attachment) -> Unit = {},
    val onCancelAction: () -> Unit = {},
    val onUserSelected: (User) -> Unit = {},
    val onCommandSelected: (Command) -> Unit = {},
    val onAlsoSendToChannelSelected: (Boolean) -> Unit = {},
    val recordingActions: AudioRecordingActions = AudioRecordingActions.None,
    val onLinkPreviewClick: ((LinkPreview) -> Unit)? = null,
)

/**
 * Parameters for [ChatComponentFactory.MessageComposerLinkPreview].
 *
 * @param linkPreview The link preview to show.
 * @param modifier Modifier for styling.
 * @param onContentClick Action invoked when the content is clicked.
 * @param onCancelClick Action invoked when the cancel button is clicked.
 */
public data class MessageComposerLinkPreviewParams(
    val linkPreview: LinkPreview,
    val modifier: Modifier = Modifier,
    val onContentClick: ((LinkPreview) -> Unit)? = null,
    val onCancelClick: (() -> Unit)? = null,
)

/**
 * Parameters for [ChatComponentFactory.MessageComposerUserSuggestionItem].
 *
 * @param user The user for which the suggestion is rendered.
 * @param currentUser The currently logged in user.
 * @param onUserSelected Action invoked when the user is selected.
 */
public data class MessageComposerUserSuggestionItemParams(
    val user: User,
    val currentUser: User?,
    val onUserSelected: (User) -> Unit,
)

/**
 * Parameters for [ChatComponentFactory.MessageComposerUserSuggestionItemLeadingContent].
 *
 * @param user The user for which the leading content is rendered.
 * @param currentUser The currently logged in user.
 * @param modifier Modifier for styling.
 */
public data class MessageComposerUserSuggestionItemLeadingContentParams(
    val user: User,
    val currentUser: User?,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.MessageComposerUserSuggestionItemCenterContent].
 *
 * @param user The user for which the center content is rendered.
 * @param modifier Modifier for styling.
 */
public data class MessageComposerUserSuggestionItemCenterContentParams(
    val user: User,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.MessageComposerUserSuggestionItemTrailingContent].
 *
 * @param user The user for which the trailing content is rendered.
 * @param modifier Modifier for styling.
 */
public data class MessageComposerUserSuggestionItemTrailingContentParams(
    val user: User,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.MessageComposerCommandSuggestionItem].
 *
 * @param command The command for which the suggestion is rendered.
 * @param onCommandSelected Action invoked when the command is selected.
 */
public data class MessageComposerCommandSuggestionItemParams(
    val command: Command,
    val onCommandSelected: (Command) -> Unit,
)

/**
 * Parameters for [ChatComponentFactory.MessageComposerCommandSuggestionItemLeadingContent].
 *
 * @param command The command for which the leading content is rendered.
 * @param modifier Modifier for styling.
 */
public data class MessageComposerCommandSuggestionItemLeadingContentParams(
    val command: Command,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.MessageComposerCommandSuggestionItemCenterContent].
 *
 * @param command The command for which the center content is rendered.
 * @param modifier Modifier for styling.
 */
public data class MessageComposerCommandSuggestionItemCenterContentParams(
    val command: Command,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.MessageComposerLeadingContent].
 *
 * @param state The current state of the message composer.
 * @param isAttachmentPickerVisible Whether the attachment picker is visible.
 * @param onAttachmentsClick Action invoked when attachments button is clicked.
 * @param modifier Modifier for styling.
 */
public data class MessageComposerLeadingContentParams(
    val state: MessageComposerState,
    val isAttachmentPickerVisible: Boolean,
    val onAttachmentsClick: () -> Unit,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.MessageComposerInput].
 *
 * @param state The current state of the message composer.
 * @param onInputChanged Action invoked when the input is changed.
 * @param onAttachmentRemoved Action invoked when an attachment is removed.
 * @param onCancel Action invoked when the cancel button is clicked.
 * @param onSendClick Action invoked when the send button is clicked.
 * @param onAlsoSendToChannelChange Action invoked when also-send-to-channel is changed.
 * @param recordingActions The actions to control the audio recording.
 * @param modifier Modifier for styling.
 * @param onInputChanged Action invoked when the input is changed.
 * @param onAttachmentRemoved Action invoked when an attachment is removed.
 * @param onCancel Action invoked when the cancel button is clicked.
 * @param onSendClick Action invoked when the send button is clicked.
 * @param onAlsoSendToChannelChange Action invoked when also-send-to-channel is changed.
 * @param recordingActions The actions to control the audio recording.
 * @param onActiveCommandDismiss Action invoked when the active command is dismissed.
 * @param onLinkPreviewClick Action invoked when a link preview is clicked.
 * @param onCancelLinkPreviewClick Action invoked when link preview cancel is clicked.
 */
public data class MessageComposerInputParams(
    val state: MessageComposerState,
    val modifier: Modifier = Modifier,
    val onInputChanged: (String) -> Unit = {},
    val onAttachmentRemoved: (Attachment) -> Unit = {},
    val onCancel: () -> Unit = {},
    val onSendClick: (String, List<Attachment>) -> Unit = { _, _ -> },
    val onAlsoSendToChannelChange: (Boolean) -> Unit = {},
    val recordingActions: AudioRecordingActions = AudioRecordingActions.None,
    val onActiveCommandDismiss: () -> Unit = {},
    val onLinkPreviewClick: ((LinkPreview) -> Unit)? = null,
    val onCancelLinkPreviewClick: (() -> Unit)? = null,
)

/**
 * Parameters for [ChatComponentFactory.MessageComposerQuotedMessage].
 *
 * @param state The current state of the message composer.
 * @param quotedMessage The message being quoted.
 * @param onCancelClick Action invoked when the cancel button is clicked.
 * @param modifier Modifier for styling.
 */
public data class MessageComposerQuotedMessageParams(
    val state: MessageComposerState,
    val quotedMessage: Message,
    val onCancelClick: () -> Unit,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.MessageComposerEditIndicator].
 *
 * @param state The current state of the message composer.
 * @param editMessage The message being edited.
 * @param onCancelClick Action invoked when the cancel button is clicked.
 * @param modifier Modifier for styling.
 */
public data class MessageComposerEditIndicatorParams(
    val state: MessageComposerState,
    val editMessage: Message,
    val onCancelClick: () -> Unit,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.MessageComposerInputCenterContent].
 *
 * @param state The current state of the message composer.
 * @param onValueChange Action invoked when the input value changes.
 * @param modifier Modifier for styling.
 */
public data class MessageComposerInputCenterContentParams(
    val state: MessageComposerState,
    val onValueChange: (String) -> Unit,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.MessageComposerInputCenterBottomContent].
 *
 * @param state The current state of the message composer.
 * @param onAlsoSendToChannelChange Action invoked when also-send-to-channel is changed.
 * @param modifier Modifier for styling.
 */
public data class MessageComposerInputCenterBottomContentParams(
    val state: MessageComposerState,
    val onAlsoSendToChannelChange: (Boolean) -> Unit,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.MessageComposerInputTrailingContent].
 *
 * @param state The current state of the message composer.
 * @param recordingActions The actions to control the audio recording.
 * @param onSendClick Action invoked when the send button is clicked.
 */
public data class MessageComposerInputTrailingContentParams(
    val state: MessageComposerState,
    val recordingActions: AudioRecordingActions,
    val onSendClick: (String, List<Attachment>) -> Unit,
)

/**
 * Parameters for [ChatComponentFactory.MessageComposerTrailingContent].
 *
 * @param state The current state of the message composer.
 */
public data class MessageComposerTrailingContentParams(
    val state: MessageComposerState,
)

/**
 * Parameters for [ChatComponentFactory.MessageComposerCoolDownIndicator].
 *
 * @param coolDownTime The remaining cool down time.
 * @param modifier Modifier for styling.
 */
public data class MessageComposerCoolDownIndicatorParams(
    val coolDownTime: Int,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.MessageComposerSendButton].
 *
 * @param onClick Action invoked when the button is clicked.
 */
public data class MessageComposerSendButtonParams(
    val onClick: () -> Unit,
)

/**
 * Parameters for [ChatComponentFactory.MessageComposerSaveButton].
 *
 * @param enabled Whether the save button is enabled.
 * @param onClick Action invoked when the button is clicked.
 */
public data class MessageComposerSaveButtonParams(
    val enabled: Boolean,
    val onClick: () -> Unit,
)

/**
 * Parameters for [ChatComponentFactory.MessageComposerAudioRecordingButton].
 *
 * @param state The current state of the recording process.
 * @param recordingActions The actions to control the audio recording.
 */
public data class MessageComposerAudioRecordingButtonParams(
    val state: RecordingState,
    val recordingActions: AudioRecordingActions,
)

/**
 * Parameters for [ChatComponentFactory.MessageComposerAudioRecordingFloatingLockIcon].
 *
 * @param isLocked Whether the recording is currently locked.
 * @param dragOffsetY The vertical drag offset in pixels.
 */
public data class MessageComposerAudioRecordingFloatingLockIconParams(
    val isLocked: Boolean,
    val dragOffsetY: Int,
)

/**
 * Parameters for [ChatComponentFactory.MessageComposerAudioRecordingPermissionRationale].
 *
 * @param data The snackbar data containing the rationale message and action.
 */
public data class MessageComposerAudioRecordingPermissionRationaleParams(
    val data: SnackbarData,
)

/**
 * Parameters for [ChatComponentFactory.MessageComposerAudioRecordingHoldContent].
 *
 * @param state The current hold recording state.
 * @param modifier Modifier for styling.
 */
public data class MessageComposerAudioRecordingHoldContentParams(
    val state: RecordingState.Hold,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.MessageComposerAudioRecordingLockedContent].
 *
 * @param state The current locked recording state.
 * @param recordingActions The actions to control the recording.
 * @param modifier Modifier for styling.
 */
public data class MessageComposerAudioRecordingLockedContentParams(
    val state: RecordingState.Locked,
    val recordingActions: AudioRecordingActions,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.MessageComposerAudioRecordingOverviewContent].
 *
 * @param state The current overview recording state.
 * @param recordingActions The actions to control playback.
 * @param modifier Modifier for styling.
 */
public data class MessageComposerAudioRecordingOverviewContentParams(
    val state: RecordingState.Overview,
    val recordingActions: AudioRecordingActions,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.MessageComposerAudioRecordingHint].
 *
 * @param data The snackbar data containing the hint message and dismiss action.
 */
public data class MessageComposerAudioRecordingHintParams(
    val data: SnackbarData,
)

/**
 * Parameters for [ChatComponentFactory.Avatar].
 *
 * @param imageUrl The URL of the image to display.
 * @param showBorder Whether to draw a border around the avatar.
 * @param fallback The fallback content to display if the image is unavailable.
 * @param modifier Modifier for styling.
 */
public data class AvatarParams(
    val imageUrl: String?,
    val showBorder: Boolean,
    val fallback: @Composable () -> Unit,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.UserAvatar].
 *
 * @param user The user whose avatar will be displayed.
 * @param modifier Modifier for styling.
 * @param showIndicator Whether to overlay an online status indicator.
 * @param showBorder Whether to draw a border around the avatar.
 */
public data class UserAvatarParams(
    val user: User,
    val modifier: Modifier = Modifier,
    val showIndicator: Boolean = false,
    val showBorder: Boolean = false,
)

/**
 * Parameters for [ChatComponentFactory.ChannelAvatar].
 *
 * @param channel The channel whose avatar will be displayed.
 * @param modifier Modifier for styling.
 * @param currentUser The user currently logged in.
 * @param showIndicator Whether to overlay an online status indicator.
 * @param showBorder Whether to draw a border around the avatar.
 */
public data class ChannelAvatarParams(
    val channel: Channel,
    val modifier: Modifier = Modifier,
    val currentUser: User? = null,
    val showIndicator: Boolean = false,
    val showBorder: Boolean = false,
)

/**
 * Parameters for [ChatComponentFactory.ChannelMenu].
 *
 * @param selectedChannel The selected channel.
 * @param currentUser The currently logged in user.
 * @param channelActions The list of channel actions.
 * @param onChannelOptionConfirm Action invoked when a channel option is confirmed.
 * @param onDismiss Action invoked when the menu is dismissed.
 * @param modifier Modifier for styling.
 */
public data class ChannelMenuParams(
    val selectedChannel: Channel,
    val currentUser: User?,
    val channelActions: List<ChannelAction>,
    val onChannelOptionConfirm: (ChannelAction) -> Unit,
    val onDismiss: () -> Unit,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.ChannelMenuHeaderContent].
 *
 * @param selectedChannel The selected channel.
 * @param currentUser The currently logged in user.
 * @param modifier Modifier for styling.
 */
public data class ChannelMenuHeaderContentParams(
    val selectedChannel: Channel,
    val currentUser: User?,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.ChannelMenuCenterContent].
 *
 * @param channelActions The list of channel actions.
 * @param onChannelOptionConfirm Action invoked when a channel option is confirmed.
 * @param modifier Modifier for styling.
 */
public data class ChannelMenuCenterContentParams(
    val channelActions: List<ChannelAction>,
    val onChannelOptionConfirm: (ChannelAction) -> Unit,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.ChannelMenuOptions].
 *
 * @param channelActions The list of channel actions.
 * @param onChannelOptionConfirm Action invoked when a channel option is confirmed.
 * @param modifier Modifier for styling.
 */
public data class ChannelMenuOptionsParams(
    val channelActions: List<ChannelAction>,
    val onChannelOptionConfirm: (ChannelAction) -> Unit,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.ChannelOptionsItem].
 *
 * @param action The channel action to render.
 * @param onClick Action invoked when the item is clicked.
 * @param modifier Modifier for styling.
 */
public data class ChannelOptionsItemParams(
    val action: ChannelAction,
    val onClick: () -> Unit,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.ChannelOptionsItemLeadingIcon].
 *
 * @param action The channel action.
 * @param modifier Modifier for styling.
 */
public data class ChannelOptionsItemLeadingIconParams(
    val action: ChannelAction,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.MessageMenu].
 *
 * @param message The selected message.
 * @param messageOptions The list of message options.
 * @param ownCapabilities The capabilities of the current user.
 * @param onMessageAction Action invoked when a message action is clicked.
 * @param onShowMore Action invoked when "show more" is clicked.
 * @param modifier Modifier for styling.
 * @param onDismiss Action invoked when the menu is dismissed.
 * @param currentUser The currently logged in user.
 */
public data class MessageMenuParams(
    val message: Message,
    val messageOptions: List<MessageOptionItemState>,
    val ownCapabilities: Set<String>,
    val onMessageAction: (MessageAction) -> Unit,
    val onShowMore: () -> Unit,
    val modifier: Modifier = Modifier,
    val onDismiss: () -> Unit = {},
    val currentUser: User? = null,
)

/**
 * Parameters for [ChatComponentFactory.MessageMenuHeaderContent].
 *
 * @param message The selected message.
 * @param messageOptions The list of message options.
 * @param ownCapabilities The capabilities of the current user.
 * @param onMessageAction Action invoked when a message action is clicked.
 * @param onShowMore Action invoked when "show more" is clicked.
 * @param modifier Modifier for styling.
 */
public data class MessageMenuHeaderContentParams(
    val message: Message,
    val messageOptions: List<MessageOptionItemState>,
    val ownCapabilities: Set<String>,
    val onMessageAction: (MessageAction) -> Unit,
    val onShowMore: () -> Unit,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.MessageMenuOptions].
 *
 * @param message The selected message.
 * @param options The list of message options.
 * @param onMessageOptionSelected Action invoked when a message option is selected.
 * @param modifier Modifier for styling.
 */
public data class MessageMenuOptionsParams(
    val message: Message,
    val options: List<MessageOptionItemState>,
    val onMessageOptionSelected: (MessageOptionItemState) -> Unit,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.MessageMenuOptionsItem].
 *
 * @param option The message option state.
 * @param onMessageOptionSelected Action invoked when a message option is selected.
 */
public data class MessageMenuOptionsItemParams(
    val option: MessageOptionItemState,
    val onMessageOptionSelected: (MessageOptionItemState) -> Unit,
)

/**
 * Parameters for the [ChatComponentFactory.MessageReactions] component.
 *
 * @param message The message for which the reactions are displayed.
 * @param reactions The list of reaction options to display.
 * @param modifier Modifier for styling.
 * @param onClick Handler when the reaction list is clicked.
 */
public data class MessageReactionsParams(
    val message: Message,
    val reactions: List<MessageReactionItemState>,
    val modifier: Modifier = Modifier,
    val onClick: ((message: Message) -> Unit)? = null,
)

/**
 * Parameters for [ChatComponentFactory.ReactionIcon].
 *
 * @param type The string representation of the reaction.
 * @param emoji The emoji character the type maps to, if any.
 * @param size The size of the reaction icon.
 * @param modifier Modifier for styling.
 */
public data class ReactionIconParams(
    val type: String,
    val emoji: String?,
    val size: ReactionIconSize,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.ReactionToggle].
 *
 * @param type The string representation of the reaction.
 * @param emoji The emoji character the type maps to, if any.
 * @param size The size of the reaction toggle.
 * @param checked Whether the toggle is checked.
 * @param modifier Modifier for styling.
 * @param onCheckedChange Callback when the checked state changes.
 */
public data class ReactionToggleParams(
    val type: String,
    val emoji: String?,
    val size: ReactionToggleSize,
    val checked: Boolean,
    val modifier: Modifier = Modifier,
    val onCheckedChange: ((Boolean) -> Unit)? = null,
)

/**
 * Parameters for [ChatComponentFactory.ReactionsMenu].
 *
 * @param currentUser The currently logged in user.
 * @param message The selected message.
 * @param onMessageAction Action invoked when a message action is clicked.
 * @param onShowMoreReactionsSelected Action invoked when "show more" is clicked.
 * @param ownCapabilities The capabilities of the current user.
 * @param onDismiss Action invoked when the menu is dismissed.
 * @param modifier Modifier for styling.
 */
public data class ReactionsMenuParams(
    val currentUser: User?,
    val message: Message,
    val onMessageAction: (MessageAction) -> Unit,
    val onShowMoreReactionsSelected: () -> Unit,
    val ownCapabilities: Set<String>,
    val onDismiss: () -> Unit,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.ReactionsMenuContent].
 *
 * @param currentUser The currently logged in user.
 * @param message The selected message.
 * @param onMessageAction Action invoked when a message action is clicked.
 * @param onShowMoreReactionsSelected Action invoked when "show more" is clicked.
 * @param ownCapabilities The capabilities of the current user.
 * @param modifier Modifier for styling.
 */
public data class ReactionsMenuContentParams(
    val currentUser: User?,
    val message: Message,
    val onMessageAction: (MessageAction) -> Unit,
    val onShowMoreReactionsSelected: () -> Unit,
    val ownCapabilities: Set<String>,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.ReactionMenuOptionItem].
 *
 * @param option The reaction option state.
 * @param onReactionOptionSelected Action invoked when a reaction option is selected.
 * @param modifier Modifier for styling.
 */
public data class ReactionMenuOptionItemParams(
    val option: ReactionOptionItemState,
    val onReactionOptionSelected: (ReactionOptionItemState) -> Unit,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.MessageReactionPicker].
 *
 * @param message The selected message.
 * @param onMessageAction Action invoked when a message action is clicked.
 * @param onDismiss Action invoked when the picker is dismissed.
 * @param modifier Modifier for styling.
 */
public data class MessageReactionPickerParams(
    val message: Message,
    val onMessageAction: (MessageAction) -> Unit,
    val onDismiss: () -> Unit = {},
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.MessageReactionsPickerContent].
 *
 * @param message The selected message.
 * @param onMessageAction Action invoked when a message action is clicked.
 * @param modifier Modifier for styling.
 */
public data class MessageReactionsPickerContentParams(
    val message: Message,
    val onMessageAction: (MessageAction) -> Unit,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.MenuOptionItem].
 *
 * @param onClick Action invoked when the item is clicked.
 * @param title The title of the menu item.
 * @param titleColor The color of the title.
 * @param style The text style.
 * @param leadingIcon The leading icon composable.
 * @param modifier Modifier for styling.
 * @param itemHeight The height of the item.
 * @param verticalAlignment The vertical alignment.
 * @param horizontalArrangement The horizontal arrangement.
 */
public data class MenuOptionItemParams(
    val onClick: () -> Unit,
    val title: String,
    val titleColor: Color,
    val style: TextStyle,
    val leadingIcon: @Composable RowScope.() -> Unit,
    val modifier: Modifier = Modifier,
    val itemHeight: Dp = 56.dp,
    val verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    val horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
)

/**
 * Parameters for [ChatComponentFactory.ThreadListBanner].
 *
 * @param state The current thread list banner state.
 * @param onClick Action invoked when the banner is clicked.
 */
public data class ThreadListBannerParams(
    val state: ThreadListBannerState,
    val onClick: () -> Unit,
)

/**
 * Parameters for [ChatComponentFactory.ThreadListItem].
 *
 * @param thread The thread to display.
 * @param currentUser The currently logged in user.
 * @param onThreadClick Action invoked when the thread is clicked.
 */
public data class ThreadListItemParams(
    val thread: Thread,
    val currentUser: User?,
    val onThreadClick: (Thread) -> Unit,
)

/**
 * Parameters for [ChatComponentFactory.ThreadListEmptyContent].
 *
 * @param modifier Modifier for styling.
 */
public data class ThreadListEmptyContentParams(
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.ThreadListLoadingContent].
 *
 * @param modifier Modifier for styling.
 */
public data class ThreadListLoadingContentParams(
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.ThreadListLoadingMoreContent].
 */
public class ThreadListLoadingMoreContentParams

/**
 * Parameters for [ChatComponentFactory.PinnedMessageListItem].
 *
 * @param message The pinned message to display.
 * @param currentUser The currently logged in user.
 * @param onClick Action invoked when the pinned message is clicked.
 */
public data class PinnedMessageListItemParams(
    val message: Message,
    val currentUser: User?,
    val onClick: (Message) -> Unit,
)

/**
 * Parameters for [ChatComponentFactory.PinnedMessageListItemLeadingContent].
 *
 * @param message The pinned message.
 * @param currentUser The currently logged in user.
 */
public data class PinnedMessageListItemLeadingContentParams(
    val message: Message,
    val currentUser: User?,
)

/**
 * Parameters for [ChatComponentFactory.PinnedMessageListItemCenterContent].
 *
 * @param message The pinned message.
 * @param currentUser The currently logged in user.
 */
public data class PinnedMessageListItemCenterContentParams(
    val message: Message,
    val currentUser: User?,
)

/**
 * Parameters for [ChatComponentFactory.PinnedMessageListItemTrailingContent].
 *
 * @param message The pinned message.
 */
public data class PinnedMessageListItemTrailingContentParams(
    val message: Message,
)

/**
 * Parameters for [ChatComponentFactory.PinnedMessageListItemDivider].
 */
public class PinnedMessageListItemDividerParams

/**
 * Parameters for [ChatComponentFactory.PinnedMessageListEmptyContent].
 *
 * @param modifier Modifier for styling.
 */
public data class PinnedMessageListEmptyContentParams(
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.PinnedMessageListLoadingContent].
 *
 * @param modifier Modifier for styling.
 */
public data class PinnedMessageListLoadingContentParams(
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.PinnedMessageListLoadingMoreContent].
 */
public class PinnedMessageListLoadingMoreContentParams

/**
 * Parameters for [ChatComponentFactory.MentionListItem].
 *
 * @param mention The mention result to display.
 * @param currentUser The currently logged in user.
 * @param modifier Modifier for styling.
 * @param onClick Action invoked when the mention is clicked.
 */
public data class MentionListItemParams(
    val mention: MessageResult,
    val currentUser: User?,
    val modifier: Modifier = Modifier,
    val onClick: ((message: Message) -> Unit)? = null,
)

/**
 * Parameters for [ChatComponentFactory.MentionListLoadingIndicator].
 *
 * @param modifier Modifier for styling.
 */
public data class MentionListLoadingIndicatorParams(
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.MentionListEmptyContent].
 *
 * @param modifier Modifier for styling.
 */
public data class MentionListEmptyContentParams(
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.MentionListLoadingItem].
 *
 * @param modifier Modifier for styling.
 */
public data class MentionListLoadingItemParams(
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.MentionListPullToRefreshIndicator].
 *
 * @param pullToRefreshState The state of the pull-to-refresh.
 * @param isRefreshing Whether the list is currently refreshing.
 * @param modifier Modifier for styling.
 */
@OptIn(ExperimentalMaterial3Api::class)
public data class MentionListPullToRefreshIndicatorParams(
    val pullToRefreshState: PullToRefreshState,
    val isRefreshing: Boolean,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.DirectChannelInfoTopBar].
 *
 * @param headerState The state of the channel header.
 * @param listState The state of the lazy list.
 * @param onNavigationIconClick Action invoked when the navigation icon is clicked.
 */
public data class DirectChannelInfoTopBarParams(
    val headerState: ChannelHeaderViewState,
    val listState: LazyListState,
    val onNavigationIconClick: () -> Unit,
)

/**
 * Parameters for [ChatComponentFactory.DirectChannelInfoAvatarContainer].
 *
 * @param user The user whose avatar is displayed.
 */
public data class DirectChannelInfoAvatarContainerParams(
    val user: User,
)

/**
 * Parameters for [ChatComponentFactory.GroupChannelInfoAvatarContainer].
 *
 * @param channel The channel to display the avatar for.
 * @param currentUser The currently logged in user.
 * @param members The members list of the channel.
 */
public data class GroupChannelInfoAvatarContainerParams(
    val channel: Channel,
    val currentUser: User?,
    val members: ExpandableList<Member>,
)

/**
 * Parameters for [ChatComponentFactory.GroupChannelInfoMemberSection].
 *
 * @param members The expandable list of members.
 * @param currentUser The currently logged in user.
 * @param owner The owner of the channel.
 * @param totalMemberCount The total number of members.
 * @param showAddButton Whether to show the "Add" button.
 * @param onAddMembersClick Action invoked when the "Add" button is clicked.
 * @param onViewAction Action invoked when a view action is triggered.
 */
public data class GroupChannelInfoMemberSectionParams(
    val members: ExpandableList<Member>,
    val currentUser: User?,
    val owner: User,
    val totalMemberCount: Int,
    val showAddButton: Boolean,
    val onAddMembersClick: () -> Unit,
    val onViewAction: (ChannelInfoViewAction) -> Unit,
)

/**
 * Parameters for [ChatComponentFactory.GroupChannelInfoTopBar].
 *
 * @param headerState The state of the channel header.
 * @param infoState The state of the channel info.
 * @param listState The state of the lazy list.
 * @param onNavigationIconClick Action invoked when the navigation icon is clicked.
 * @param onAddMembersClick Action invoked when the "Add members" button is clicked.
 */
public data class GroupChannelInfoTopBarParams(
    val headerState: ChannelHeaderViewState,
    val infoState: ChannelInfoViewState,
    val listState: LazyListState,
    val onNavigationIconClick: () -> Unit,
    val onAddMembersClick: () -> Unit,
)

/**
 * Parameters for [ChatComponentFactory.GroupChannelInfoAddMembersButton].
 *
 * @param onClick Action invoked when the button is clicked.
 */
public data class GroupChannelInfoAddMembersButtonParams(
    val onClick: () -> Unit,
)

/**
 * Parameters for [ChatComponentFactory.ChannelInfoSeparatorItem].
 */
public class ChannelInfoSeparatorItemParams

/**
 * Parameters for [ChatComponentFactory.ChannelInfoOptionItem].
 *
 * @param option The channel info option to display.
 * @param isGroupChannel Whether the channel is a group channel.
 * @param onViewAction Action invoked when a view action is triggered.
 */
public data class ChannelInfoOptionItemParams(
    val option: ChannelInfoViewState.Content.Option,
    val isGroupChannel: Boolean,
    val onViewAction: (ChannelInfoViewAction) -> Unit,
)

/**
 * Parameters for [ChatComponentFactory.GroupChannelInfoMemberItem].
 *
 * @param currentUser The currently logged in user.
 * @param member The member to display.
 * @param isOwner Whether the member is the owner.
 * @param onClick Action invoked when the member item is clicked.
 */
public data class GroupChannelInfoMemberItemParams(
    val currentUser: User?,
    val member: Member,
    val isOwner: Boolean,
    val onClick: (() -> Unit)? = null,
)

/**
 * Parameters for [ChatComponentFactory.GroupChannelInfoExpandMembersItem].
 *
 * @param collapsedCount The number of collapsed members.
 * @param onClick Action invoked when the expand button is clicked.
 */
public data class GroupChannelInfoExpandMembersItemParams(
    val collapsedCount: Int,
    val onClick: () -> Unit,
)

/**
 * Parameters for [ChatComponentFactory.ChannelInfoScreenModal].
 *
 * @param modal Which modal to display.
 * @param isGroupChannel Whether the channel is a group channel.
 * @param onViewAction Action invoked when a view action is triggered.
 * @param onMemberViewEvent Action invoked when a member view event is triggered.
 * @param onDismiss Action invoked when the modal is dismissed.
 */
public data class ChannelInfoScreenModalParams(
    val modal: ChannelInfoViewEvent.Modal?,
    val isGroupChannel: Boolean,
    val onViewAction: (ChannelInfoViewAction) -> Unit,
    val onMemberViewEvent: (ChannelInfoMemberViewEvent) -> Unit,
    val onDismiss: () -> Unit,
)

/**
 * Parameters for [ChatComponentFactory.ChannelInfoMemberInfoModalSheetTopBar].
 *
 * @param member The member to display.
 */
public data class ChannelInfoMemberInfoModalSheetTopBarParams(
    val member: Member,
)

/**
 * Parameters for [ChatComponentFactory.ChannelInfoMemberOptionItem].
 *
 * @param action The member action to render.
 */
public data class ChannelInfoMemberOptionItemParams(
    val action: MemberAction,
)

/**
 * Parameters for [ChatComponentFactory.ChannelFilesAttachmentsTopBar].
 *
 * @param listState The state of the lazy list.
 * @param onNavigationIconClick Action invoked when the navigation icon is clicked.
 */
public data class ChannelFilesAttachmentsTopBarParams(
    val listState: LazyListState,
    val onNavigationIconClick: () -> Unit,
)

/**
 * Parameters for [ChatComponentFactory.ChannelFilesAttachmentsLoadingIndicator].
 *
 * @param modifier Modifier for styling.
 */
public data class ChannelFilesAttachmentsLoadingIndicatorParams(
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.ChannelFilesAttachmentsEmptyContent].
 *
 * @param modifier Modifier for styling.
 */
public data class ChannelFilesAttachmentsEmptyContentParams(
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.ChannelFilesAttachmentsErrorContent].
 *
 * @param modifier Modifier for styling.
 */
public data class ChannelFilesAttachmentsErrorContentParams(
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.ChannelFilesAttachmentsHeaderItem].
 *
 * @param label The label for the header item.
 * @param modifier Modifier for styling.
 */
public data class ChannelFilesAttachmentsHeaderItemParams(
    val label: String,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.ChannelFilesAttachmentsItem].
 *
 * @param index The index of the item.
 * @param item The channel file attachment item.
 * @param currentUser The currently logged in user.
 * @param onClick Action invoked when the item is clicked.
 * @param modifier Modifier for styling.
 */
public data class ChannelFilesAttachmentsItemParams(
    val index: Int,
    val item: ChannelAttachmentsViewState.Content.Item,
    val currentUser: User?,
    val onClick: () -> Unit,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.ChannelFilesAttachmentsItemDivider].
 *
 * @param index The index of the item.
 * @param modifier Modifier for styling.
 */
public data class ChannelFilesAttachmentsItemDividerParams(
    val index: Int,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.ChannelFilesAttachmentsLoadingItem].
 *
 * @param modifier Modifier for styling.
 */
public data class ChannelFilesAttachmentsLoadingItemParams(
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.ChannelMediaAttachmentsTopBar].
 *
 * @param gridState The state of the lazy grid.
 * @param onNavigationIconClick Action invoked when the navigation icon is clicked.
 */
public data class ChannelMediaAttachmentsTopBarParams(
    val gridState: LazyGridState,
    val onNavigationIconClick: () -> Unit,
)

/**
 * Parameters for [ChatComponentFactory.ChannelMediaAttachmentsLoadingIndicator].
 *
 * @param modifier Modifier for styling.
 */
public data class ChannelMediaAttachmentsLoadingIndicatorParams(
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.ChannelMediaAttachmentsEmptyContent].
 *
 * @param modifier Modifier for styling.
 */
public data class ChannelMediaAttachmentsEmptyContentParams(
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.ChannelMediaAttachmentsErrorContent].
 *
 * @param modifier Modifier for styling.
 */
public data class ChannelMediaAttachmentsErrorContentParams(
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.ChannelMediaAttachmentsFloatingHeader].
 *
 * @param label The label for the floating header.
 * @param modifier Modifier for styling.
 */
public data class ChannelMediaAttachmentsFloatingHeaderParams(
    val label: String,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.ChannelMediaAttachmentsItem].
 *
 * @param index The index of the item.
 * @param item The channel media attachment item.
 * @param onClick Action invoked when the item is clicked.
 * @param modifier Modifier for styling.
 */
public data class ChannelMediaAttachmentsItemParams(
    val index: Int,
    val item: ChannelAttachmentsViewState.Content.Item,
    val onClick: () -> Unit,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.ChannelMediaAttachmentsLoadingItem].
 *
 * @param modifier Modifier for styling.
 */
public data class ChannelMediaAttachmentsLoadingItemParams(
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.ChannelMediaAttachmentsPreviewTopBar].
 *
 * @param item The item to display in the top bar.
 * @param onNavigationIconClick Action invoked when the navigation icon is clicked.
 */
public data class ChannelMediaAttachmentsPreviewTopBarParams(
    val item: ChannelAttachmentsViewState.Content.Item,
    val onNavigationIconClick: () -> Unit,
)

/**
 * Parameters for [ChatComponentFactory.ChannelMediaAttachmentsPreviewTopBarTitle].
 *
 * @param item The item containing the message to display.
 */
public data class ChannelMediaAttachmentsPreviewTopBarTitleParams(
    val item: ChannelAttachmentsViewState.Content.Item,
)

/**
 * Parameters for the [ChatComponentFactory.ChannelMediaAttachmentsPreviewBottomBar] component.
 *
 * @param centerContent Composable lambda for center content in the bottom bar.
 * @param leadingContent Composable lambda for leading content in the bottom bar.
 * @param trailingContent Composable lambda for trailing content in the bottom bar.
 * @param topContent Composable lambda for content above the bottom bar (e.g. video playback controls).
 */
public data class ChannelMediaAttachmentsPreviewBottomBarParams(
    val centerContent: @Composable () -> Unit,
    val leadingContent: @Composable () -> Unit = {},
    val trailingContent: @Composable () -> Unit = {},
    val topContent: @Composable (() -> Unit)? = null,
)

/**
 * Parameters for [ChatComponentFactory.AudioRecordAttachmentContent].
 *
 * @param state State providing context for the attachment.
 * @param modifier Modifier for styling.
 */
public data class AudioRecordAttachmentContentParams(
    val state: AttachmentState,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.FileAttachmentContent].
 *
 * @param state State providing context for the attachment.
 * @param modifier Modifier for styling.
 */
public data class FileAttachmentContentParams(
    val state: AttachmentState,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.GiphyAttachmentContent].
 *
 * @param state State providing context for the attachment.
 * @param modifier Modifier for styling.
 */
public data class GiphyAttachmentContentParams(
    val state: AttachmentState,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.LinkAttachmentContent].
 *
 * @param state State providing context for the attachment.
 * @param modifier Modifier for styling.
 */
public data class LinkAttachmentContentParams(
    val state: AttachmentState,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.MediaAttachmentContent].
 *
 * @param state State providing context for the attachment.
 * @param modifier Modifier for styling.
 */
public data class MediaAttachmentContentParams(
    val state: AttachmentState,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.CustomAttachmentContent].
 *
 * @param state State providing context for the attachment.
 * @param modifier Modifier for styling.
 */
public data class CustomAttachmentContentParams(
    val state: AttachmentState,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.FileAttachmentItem].
 *
 * @param attachment The file attachment to show.
 * @param isMine Whether the message is sent by the current user.
 * @param showFileSize Whether to show the file size.
 * @param modifier Modifier for styling.
 */
public data class FileAttachmentItemParams(
    val attachment: Attachment,
    val isMine: Boolean,
    val showFileSize: (Attachment) -> Boolean,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for the [ChatComponentFactory.MessageFooterStatusIndicator] component.
 *
 * @param messageItem The message item state.
 * @param modifier Modifier for styling.
 */
public data class MessageFooterStatusIndicatorParams(
    val messageItem: MessageItemState,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for the [ChatComponentFactory.MessageComposerInputLeadingContent] component.
 *
 * @param state The message composer state.
 * @param onActiveCommandDismiss Handler when the active command is dismissed.
 * @param modifier Modifier for styling.
 */
public data class MessageComposerInputLeadingContentParams(
    val state: MessageComposerState,
    val onActiveCommandDismiss: () -> Unit,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for the [ChatComponentFactory.MessageComposerAttachments] component.
 *
 * @param attachments The attachments currently selected in the composer.
 * @param onAttachmentRemoved Lambda invoked when an attachment is removed by the user.
 * @param modifier Modifier for styling.
 */
public data class MessageComposerAttachmentsParams(
    val attachments: List<Attachment>,
    val onAttachmentRemoved: (Attachment) -> Unit,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for the [ChatComponentFactory.MessageComposerAttachmentAudioRecordItem] component.
 *
 * @param attachment The audio recording attachment to render.
 * @param playerState Current state of the audio player.
 * @param modifier Modifier for styling.
 * @param onPlayToggleClick Called when the play/pause button is tapped.
 * @param onPlaySpeedClick Called when the playback speed button is tapped.
 * @param onThumbDragStart Called when the user starts dragging the waveform thumb.
 * @param onThumbDragStop Called when the user stops dragging, with the target seek fraction.
 * @param onAttachmentRemoved Called when the attachment is removed by the user.
 */
public data class MessageComposerAttachmentAudioRecordItemParams(
    val attachment: Attachment,
    val playerState: AudioPlayerState,
    val modifier: Modifier = Modifier,
    val onPlayToggleClick: (Attachment) -> Unit = {},
    val onPlaySpeedClick: (Attachment) -> Unit = {},
    val onThumbDragStart: (Attachment) -> Unit = {},
    val onThumbDragStop: (Attachment, Float) -> Unit = { _, _ -> },
    val onAttachmentRemoved: (Attachment) -> Unit = {},
)

/**
 * Parameters for the [ChatComponentFactory.MessageComposerAttachmentMediaItem] component.
 *
 * @param attachment The image or video attachment to render.
 * @param onAttachmentRemoved Called when the attachment is removed by the user.
 * @param modifier Modifier for styling.
 */
public data class MessageComposerAttachmentMediaItemParams(
    val attachment: Attachment,
    val onAttachmentRemoved: (Attachment) -> Unit,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for the [ChatComponentFactory.MessageComposerAttachmentFileItem] component.
 *
 * @param attachment The file attachment to render.
 * @param onAttachmentRemoved Called when the attachment is removed by the user.
 * @param modifier Modifier for styling.
 */
public data class MessageComposerAttachmentFileItemParams(
    val attachment: Attachment,
    val onAttachmentRemoved: (Attachment) -> Unit,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.AttachmentPickerMenu].
 *
 * @param attachmentsPickerViewModel Controls picker visibility and manages attachment state.
 * @param composerViewModel Receives selected attachments.
 */
public data class AttachmentPickerMenuParams(
    val attachmentsPickerViewModel: AttachmentsPickerViewModel,
    val composerViewModel: MessageComposerViewModel,
)

/**
 * Parameters for [ChatComponentFactory.AttachmentPicker].
 *
 * @param attachmentsPickerViewModel Manages picker state.
 * @param messageMode Current message mode.
 * @param actions The attachment picker actions.
 * @param modifier Modifier for styling.
 */
public data class AttachmentPickerParams(
    val attachmentsPickerViewModel: AttachmentsPickerViewModel,
    val messageMode: MessageMode,
    val actions: AttachmentPickerActions,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatComponentFactory.AttachmentTypePicker].
 *
 * @param channel Used to check channel capabilities.
 * @param messageMode Used to filter modes.
 * @param selectedMode The currently active mode.
 * @param onModeSelected Called when user taps a tab.
 * @param trailingContent Slot for custom content after the mode buttons.
 */
public data class AttachmentTypePickerParams(
    val channel: Channel,
    val messageMode: MessageMode,
    val selectedMode: AttachmentPickerMode?,
    val onModeSelected: (AttachmentPickerMode) -> Unit,
    val trailingContent: @Composable RowScope.() -> Unit = {},
)

/**
 * Parameters for [ChatComponentFactory.AttachmentTypeSystemPicker].
 *
 * @param channel Used to check channel capabilities.
 * @param messageMode Used to filter modes.
 * @param onModeSelected Called when user taps a button.
 * @param trailingContent Slot for custom content after the mode buttons.
 */
public data class AttachmentTypeSystemPickerParams(
    val channel: Channel,
    val messageMode: MessageMode,
    val onModeSelected: (AttachmentPickerMode) -> Unit,
    val trailingContent: @Composable RowScope.() -> Unit = {},
)

/**
 * Parameters for [ChatComponentFactory.AttachmentPickerContent].
 *
 * @param pickerMode The currently active mode.
 * @param commands Available slash commands.
 * @param attachments Current attachment items.
 * @param onLoadAttachments Called to trigger loading attachment metadata.
 * @param onUrisSelected Called with URIs from a system picker.
 * @param actions The attachment picker actions.
 * @param onAttachmentsSubmitted Called when attachments are ready.
 */
public data class AttachmentPickerContentParams(
    val pickerMode: AttachmentPickerMode?,
    val commands: List<Command>,
    val attachments: List<AttachmentPickerItemState>,
    val onLoadAttachments: () -> Unit,
    val onUrisSelected: (List<Uri>) -> Unit,
    val actions: AttachmentPickerActions,
    val onAttachmentsSubmitted: (List<AttachmentMetaData>) -> Unit,
)

/**
 * Parameters for [ChatComponentFactory.AttachmentMediaPicker].
 *
 * @param pickerMode Configuration for the gallery picker.
 * @param attachments Media items to display.
 * @param onLoadAttachments Called to trigger loading media metadata.
 * @param onAttachmentItemSelected Called when user taps an item.
 */
public data class AttachmentMediaPickerParams(
    val pickerMode: GalleryPickerMode,
    val attachments: List<AttachmentPickerItemState>,
    val onLoadAttachments: () -> Unit,
    val onAttachmentItemSelected: (AttachmentPickerItemState) -> Unit,
)

/**
 * Parameters for [ChatComponentFactory.AttachmentCameraPicker].
 *
 * @param pickerMode Configuration for camera capture.
 * @param onAttachmentsSubmitted Called with the captured media metadata.
 */
public data class AttachmentCameraPickerParams(
    val pickerMode: CameraPickerMode,
    val onAttachmentsSubmitted: (List<AttachmentMetaData>) -> Unit,
)

/**
 * Parameters for [ChatComponentFactory.AttachmentFilePicker].
 *
 * @param pickerMode Configuration for the file picker.
 * @param attachments File items to display.
 * @param onLoadAttachments Called to trigger loading file metadata.
 * @param onAttachmentItemSelected Called when user taps an item.
 * @param onUrisSelected Called with URIs picked via the system file browser.
 */
public data class AttachmentFilePickerParams(
    val pickerMode: FilePickerMode,
    val attachments: List<AttachmentPickerItemState>,
    val onLoadAttachments: () -> Unit,
    val onAttachmentItemSelected: (AttachmentPickerItemState) -> Unit,
    val onUrisSelected: (List<Uri>) -> Unit,
)

/**
 * Parameters for [ChatComponentFactory.AttachmentPollPicker].
 *
 * @param pickerMode Configuration for poll picker behavior.
 * @param onCreatePollClick Called when the user taps "Create Poll".
 * @param onCreatePoll Called when the user submits a new poll.
 * @param onCreatePollDismissed Called when the poll creation dialog is closed.
 */
public data class AttachmentPollPickerParams(
    val pickerMode: PollPickerMode,
    val onCreatePollClick: () -> Unit,
    val onCreatePoll: (PollConfig) -> Unit,
    val onCreatePollDismissed: () -> Unit,
)

/**
 * Parameters for [ChatComponentFactory.AttachmentCommandPicker].
 *
 * @param pickerMode The command picker mode.
 * @param commands Available commands.
 * @param onCommandSelected Called when a slash command is selected.
 */
public data class AttachmentCommandPickerParams(
    val pickerMode: CommandPickerMode,
    val commands: List<Command>,
    val onCommandSelected: (Command) -> Unit,
)

/**
 * Parameters for [ChatComponentFactory.AttachmentSystemPicker].
 *
 * @param channel Used to check channel capabilities.
 * @param messageMode Used to filter modes.
 * @param attachments Current attachment state.
 * @param actions The attachment picker actions.
 * @param onUrisSelected Called with URIs from system pickers.
 * @param onAttachmentsSubmitted Called when camera-captured media is ready.
 */
public data class AttachmentSystemPickerParams(
    val channel: Channel,
    val messageMode: MessageMode,
    val attachments: List<AttachmentPickerItemState>,
    val actions: AttachmentPickerActions,
    val onUrisSelected: (List<Uri>) -> Unit,
    val onAttachmentsSubmitted: (List<AttachmentMetaData>) -> Unit,
)
