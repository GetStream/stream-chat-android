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

package io.getstream.chat.android.compose.ui.theme

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.state.channels.list.ItemState
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResult
import io.getstream.chat.android.compose.ui.channels.header.DefaultChannelHeaderLeadingContent
import io.getstream.chat.android.compose.ui.channels.header.DefaultChannelListHeaderCenterContent
import io.getstream.chat.android.compose.ui.channels.header.DefaultChannelListHeaderTrailingContent
import io.getstream.chat.android.compose.ui.channels.list.DefaultChannelItem
import io.getstream.chat.android.compose.ui.channels.list.DefaultChannelItemCenterContent
import io.getstream.chat.android.compose.ui.channels.list.DefaultChannelItemDivider
import io.getstream.chat.android.compose.ui.channels.list.DefaultChannelItemLeadingContent
import io.getstream.chat.android.compose.ui.channels.list.DefaultChannelItemTrailingContent
import io.getstream.chat.android.compose.ui.channels.list.DefaultChannelListEmptyContent
import io.getstream.chat.android.compose.ui.channels.list.DefaultChannelListLoadingIndicator
import io.getstream.chat.android.compose.ui.channels.list.DefaultChannelSearchEmptyContent
import io.getstream.chat.android.compose.ui.channels.list.DefaultChannelsLoadingMoreIndicator
import io.getstream.chat.android.compose.ui.channels.list.DefaultSearchResultItem
import io.getstream.chat.android.compose.ui.components.DefaultSearchLabel
import io.getstream.chat.android.compose.ui.components.DefaultSearchLeadingIcon
import io.getstream.chat.android.compose.ui.components.NetworkLoadingIndicator
import io.getstream.chat.android.compose.ui.components.messages.DefaultMessageContent
import io.getstream.chat.android.compose.ui.components.messages.DefaultMessageDeletedContent
import io.getstream.chat.android.compose.ui.components.messages.DefaultMessageGiphyContent
import io.getstream.chat.android.compose.ui.components.messages.MessageFooter
import io.getstream.chat.android.compose.ui.components.messages.MessageText
import io.getstream.chat.android.compose.ui.components.messages.MessageThreadFooter
import io.getstream.chat.android.compose.ui.components.messages.OwnedMessageVisibilityContent
import io.getstream.chat.android.compose.ui.components.messages.QuotedMessage
import io.getstream.chat.android.compose.ui.components.messages.UploadingFooter
import io.getstream.chat.android.compose.ui.components.messages.factory.MessageContentFactory
import io.getstream.chat.android.compose.ui.messages.header.DefaultMessageListHeaderCenterContent
import io.getstream.chat.android.compose.ui.messages.header.DefaultMessageListHeaderLeadingContent
import io.getstream.chat.android.compose.ui.messages.header.DefaultMessageListHeaderTrailingContent
import io.getstream.chat.android.compose.ui.messages.list.DefaultMessageContainer
import io.getstream.chat.android.compose.ui.messages.list.DefaultMessageDateSeparatorContent
import io.getstream.chat.android.compose.ui.messages.list.DefaultMessageItem
import io.getstream.chat.android.compose.ui.messages.list.DefaultMessageItemCenterContent
import io.getstream.chat.android.compose.ui.messages.list.DefaultMessageItemFooterContent
import io.getstream.chat.android.compose.ui.messages.list.DefaultMessageItemHeaderContent
import io.getstream.chat.android.compose.ui.messages.list.DefaultMessageItemLeadingContent
import io.getstream.chat.android.compose.ui.messages.list.DefaultMessageItemTrailingContent
import io.getstream.chat.android.compose.ui.messages.list.DefaultMessageListEmptyContent
import io.getstream.chat.android.compose.ui.messages.list.DefaultMessageListLoadingIndicator
import io.getstream.chat.android.compose.ui.messages.list.DefaultMessageModeratedContent
import io.getstream.chat.android.compose.ui.messages.list.DefaultMessageThreadSeparatorContent
import io.getstream.chat.android.compose.ui.messages.list.DefaultMessageUnreadSeparatorContent
import io.getstream.chat.android.compose.ui.messages.list.DefaultMessagesHelperContent
import io.getstream.chat.android.compose.ui.messages.list.DefaultMessagesLoadingMoreIndicator
import io.getstream.chat.android.compose.ui.messages.list.DefaultSystemMessageContent
import io.getstream.chat.android.compose.ui.messages.list.MessagesLazyListState
import io.getstream.chat.android.compose.ui.threads.DefaultThreadListEmptyContent
import io.getstream.chat.android.compose.ui.threads.DefaultThreadListLoadingContent
import io.getstream.chat.android.compose.ui.threads.DefaultThreadListLoadingMoreContent
import io.getstream.chat.android.compose.ui.threads.ThreadItem
import io.getstream.chat.android.compose.ui.threads.ThreadItemLatestReplyContent
import io.getstream.chat.android.compose.ui.threads.ThreadItemReplyToContent
import io.getstream.chat.android.compose.ui.threads.ThreadItemTitle
import io.getstream.chat.android.compose.ui.threads.ThreadItemUnreadCountContent
import io.getstream.chat.android.compose.ui.threads.UnreadThreadsBanner
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.ReactionSorting
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.ui.common.state.messages.MessageMode
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

/**
 * Default implementation of [ChatComponentFactory].
 */
internal class DefaultChatComponentFactory : ChatComponentFactory

/**
 * Factory for creating stateless components that are used by default throughout the Chat UI.
 *
 * Example of a custom [ChatComponentFactory] implementation that changes the default UI of the trailing content
 * of the channel list header element:
 *
 * ```kotlin
 * ChatTheme(
 *     componentFactory = object : ChatComponentFactory() {
 *         @Composable
 *         override fun RowScope.ChannelListHeaderTrailingContent(
 *             onHeaderActionClick: () -> Unit,
 *         ) {
 *             IconButton(onClick = onHeaderActionClick) {
 *                 Icon(
 *                     imageVector = Icons.Default.Add,
 *                     contentDescription = "Add",
 *                 )
 *             }
 *         }
 *     }
 * ) {
 *     // Your Chat screens
 * }
 * ```
 *
 * [ChatComponentFactory] can also be extended in a separate class and passed to the [ChatTheme] as shown:
 *
 * ```kotlin
 * class MyChatComponentFactory : ChatComponentFactory {
 *     @Composable
 *     override fun RowScope.ChannelListHeaderTrailingContent(onHeaderActionClick: () -> Unit) {
 *         IconButton(onClick = onHeaderActionClick) {
 *             Icon(
 *                 imageVector = Icons.Default.Add,
 *                 contentDescription = "Add",
 *             )
 *         }
 *     }
 * }
 *
 * ChatTheme(
 *     componentFactory = MyComponentFactory()
 * ) {
 *     // Your Chat screens
 * }
 * ```
 */
@Suppress("TooManyFunctions", "LargeClass")
public interface ChatComponentFactory {

    /**
     * The default leading content of the channel list header.
     * Usually the avatar of the current user if it's available.
     */
    @Composable
    public fun RowScope.ChannelListHeaderLeadingContent(
        currentUser: User?,
        onAvatarClick: (User?) -> Unit,
    ) {
        DefaultChannelHeaderLeadingContent(
            currentUser = currentUser,
            onAvatarClick = onAvatarClick,
        )
    }

    /**
     * The default center content of the channel list header.
     * Usually shows the [title] if [connectionState] is [ConnectionState.Connected],
     * a `Disconnected` text if [connectionState] is offline,
     * or [NetworkLoadingIndicator] otherwise.
     */
    @Composable
    public fun RowScope.ChannelListHeaderCenterContent(
        connectionState: ConnectionState,
        title: String,
    ) {
        DefaultChannelListHeaderCenterContent(
            connectionState = connectionState,
            title = title,
        )
    }

    /**
     * The default trailing content of the channel list header.
     * Usually an action button.
     */
    @Composable
    public fun RowScope.ChannelListHeaderTrailingContent(
        onHeaderActionClick: () -> Unit,
    ) {
        DefaultChannelListHeaderTrailingContent(
            onHeaderActionClick = onHeaderActionClick,
        )
    }

    /**
     * The default loading indicator of the channel list, when the initial data is loading.
     */
    @Composable
    public fun ChannelListLoadingIndicator(modifier: Modifier) {
        DefaultChannelListLoadingIndicator(
            modifier = modifier,
        )
    }

    /**
     * The default empty content of the channel list.
     */
    @Composable
    public fun ChannelListEmptyContent(modifier: Modifier) {
        DefaultChannelListEmptyContent(
            modifier = modifier,
        )
    }

    /**
     * The default channel list item content.
     */
    @Composable
    public fun LazyItemScope.ChannelListItemContent(
        channelItem: ItemState.ChannelItemState,
        currentUser: User?,
        onChannelClick: (Channel) -> Unit,
        onChannelLongClick: (Channel) -> Unit,
    ) {
        DefaultChannelItem(
            channelItem = channelItem,
            currentUser = currentUser,
            onChannelClick = onChannelClick,
            onChannelLongClick = onChannelLongClick,
        )
    }

    /**
     * The default search result item of the channel list.
     */
    @Composable
    public fun LazyItemScope.ChannelListSearchResultItemContent(
        searchResultItem: ItemState.SearchResultItemState,
        currentUser: User?,
        onSearchResultClick: (Message) -> Unit,
    ) {
        DefaultSearchResultItem(
            searchResultItemState = searchResultItem,
            currentUser = currentUser,
            onSearchResultClick = onSearchResultClick,
        )
    }

    /**
     * The default empty search content of the channel list, when there are no matching search results.
     */
    @Composable
    public fun ChannelListEmptySearchContent(
        modifier: Modifier,
        searchQuery: String,
    ) {
        DefaultChannelSearchEmptyContent(
            modifier = modifier,
            searchQuery = searchQuery,
        )
    }

    /**
     * The default helper content of the channel list.
     * It's empty by default and can be used to implement a scroll to top feature.
     */
    @Composable
    public fun BoxScope.ChannelListHelperContent() {
    }

    /**
     * The default loading more item, when the next page of the channel list is loading.
     */
    @Composable
    public fun LazyItemScope.ChannelListLoadingMoreItemContent() {
        DefaultChannelsLoadingMoreIndicator()
    }

    /**
     * The default divider between channel items.
     */
    @Composable
    public fun LazyItemScope.ChannelListDividerItem() {
        DefaultChannelItemDivider()
    }

    /**
     * The default leading content of the channel item.
     * Usually the avatar that holds an image of the channel or its members.
     */
    @Composable
    public fun RowScope.ChannelItemLeadingContent(
        channelItem: ItemState.ChannelItemState,
        currentUser: User?,
    ) {
        DefaultChannelItemLeadingContent(
            channelItem = channelItem,
            currentUser = currentUser,
        )
    }

    /**
     * The default center content of the channel item.
     * Usually the name of the channel and the last message.
     */
    @Composable
    public fun RowScope.ChannelItemCenterContent(
        channelItem: ItemState.ChannelItemState,
        currentUser: User?,
    ) {
        DefaultChannelItemCenterContent(
            channelItemState = channelItem,
            currentUser = currentUser,
        )
    }

    /**
     * The default trailing content of the channel item.
     * Usually the last message and the number of unread messages.
     */
    @Composable
    public fun RowScope.ChannelItemTrailingContent(
        channelItem: ItemState.ChannelItemState,
        currentUser: User?,
    ) {
        DefaultChannelItemTrailingContent(
            channel = channelItem.channel,
            currentUser = currentUser,
        )
    }

    /**
     * The default leading icon of the search input.
     */
    @Composable
    public fun RowScope.SearchInputLeadingIcon() {
        DefaultSearchLeadingIcon()
    }

    /**
     * The default label of the search input.
     */
    @Composable
    public fun SearchInputLabel() {
        DefaultSearchLabel()
    }

    /**
     * The default header of the message list.
     * Usually a back button as a leading content,
     * the channel title in the top center,
     * the channel information or the connection status in the bottom center,
     * and the channel avatar as the trailing content.
     */
    @Suppress("LongParameterList")
    @Composable
    public fun MessageListHeader(
        modifier: Modifier,
        channel: Channel,
        currentUser: User?,
        connectionState: ConnectionState,
        typingUsers: List<User>,
        messageMode: MessageMode,
        onBackPressed: () -> Unit,
        onHeaderTitleClick: (Channel) -> Unit,
        onChannelAvatarClick: () -> Unit,
    ) {
        io.getstream.chat.android.compose.ui.messages.header.MessageListHeader(
            channel = channel,
            currentUser = currentUser,
            connectionState = connectionState,
            modifier = modifier,
            typingUsers = typingUsers,
            messageMode = messageMode,
            onBackPressed = onBackPressed,
            onHeaderTitleClick = onHeaderTitleClick,
            onChannelAvatarClick = onChannelAvatarClick,
        )
    }

    /**
     * The default leading content of the message list header, which is the back button.
     */
    @Composable
    public fun RowScope.MessageListHeaderLeadingContent(
        onBackPressed: () -> Unit,
    ) {
        DefaultMessageListHeaderLeadingContent(onBackPressed = onBackPressed)
    }

    /**
     * The default center content of the message list header.
     * Usually shows the channel title in the top and
     * the channel information or the connection status in the bottom.
     */
    @Suppress("LongParameterList")
    @Composable
    public fun RowScope.MessageListHeaderCenterContent(
        modifier: Modifier,
        channel: Channel,
        currentUser: User?,
        typingUsers: List<User>,
        messageMode: MessageMode,
        onHeaderTitleClick: (Channel) -> Unit,
        connectionState: ConnectionState,
    ) {
        DefaultMessageListHeaderCenterContent(
            modifier = modifier,
            channel = channel,
            currentUser = currentUser,
            typingUsers = typingUsers,
            messageMode = messageMode,
            onHeaderTitleClick = onHeaderTitleClick,
            connectionState = connectionState,
        )
    }

    /**
     * The default trailing content of the message list header, which is the channel avatar.
     */
    @Composable
    public fun RowScope.MessageListHeaderTrailingContent(
        channel: Channel,
        currentUser: User?,
        onClick: () -> Unit,
    ) {
        DefaultMessageListHeaderTrailingContent(
            channel = channel,
            currentUser = currentUser,
            onClick = onClick,
        )
    }

    /**
     * The default loading indicator of the message list,
     * when the initial message list is loading.
     */
    @Composable
    public fun MessageListLoadingIndicator(modifier: Modifier) {
        DefaultMessageListLoadingIndicator(
            modifier = modifier,
        )
    }

    /**
     * The default empty content of the message list,
     * when the message list is empty.
     */
    @Composable
    public fun MessageListEmptyContent(modifier: Modifier) {
        DefaultMessageListEmptyContent(
            modifier = modifier,
        )
    }

    /**
     * The default helper content of the message list.
     * It shows the scroll to bottom button when the user scrolls up and it's away from the bottom.
     */
    @Composable
    public fun BoxScope.MessageListHelperContent(
        messageListState: MessageListState,
        messagesLazyListState: MessagesLazyListState,
        onScrollToBottomClick: (() -> Unit) -> Unit,
    ) {
        DefaultMessagesHelperContent(
            messagesState = messageListState,
            messagesLazyListState = messagesLazyListState,
            scrollToBottom = onScrollToBottomClick,
        )
    }

    /**
     * The default message list item container, which renders each [MessageListItemState]'s subtype.
     */
    @Suppress("LongParameterList")
    @Composable
    public fun LazyItemScope.MessageListItemContainer(
        messageListItem: MessageListItemState,
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
        DefaultMessageContainer(
            messageListItemState = messageListItem,
            reactionSorting = reactionSorting,
            messageContentFactory = MessageContentFactory.Deprecated,
            onPollUpdated = onPollUpdated,
            onCastVote = onCastVote,
            onRemoveVote = onRemoveVote,
            selectPoll = selectPoll,
            onClosePoll = onClosePoll,
            onAddPollOption = onAddPollOption,
            onLongItemClick = onLongItemClick,
            onThreadClick = onThreadClick,
            onReactionsClick = onReactionsClick,
            onGiphyActionClick = onGiphyActionClick,
            onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
            onQuotedMessageClick = onQuotedMessageClick,
            onUserAvatarClick = onUserAvatarClick,
            onLinkClick = onMessageLinkClick,
            onUserMentionClick = onUserMentionClick,
            onAddAnswer = onAddAnswer,
        )
    }

    /**
     * The default loading more item of the message list,
     * when the next page of messages is loading.
     */
    @Composable
    public fun LazyItemScope.MessageListLoadingMoreItemContent() {
        DefaultMessagesLoadingMoreIndicator()
    }

    /**
     * The default date separator item content of the message list.
     */
    @Composable
    public fun LazyItemScope.MessageListDateSeparatorItemContent(dateSeparatorItem: DateSeparatorItemState) {
        DefaultMessageDateSeparatorContent(dateSeparator = dateSeparatorItem)
    }

    /**
     * The default unread separator item content of the message list.
     */
    @Composable
    public fun LazyItemScope.MessageListUnreadSeparatorItemContent(unreadSeparatorItem: UnreadSeparatorItemState) {
        DefaultMessageUnreadSeparatorContent(unreadSeparatorItemState = unreadSeparatorItem)
    }

    /**
     * The default thread date separator item content of the message list.
     */
    @Composable
    public fun LazyItemScope.MessageListThreadDateSeparatorItemContent(
        threadDateSeparatorItem: ThreadDateSeparatorItemState,
    ) {
        DefaultMessageThreadSeparatorContent(threadSeparator = threadDateSeparatorItem)
    }

    /**
     * The default system message content of the message list.
     */
    @Composable
    public fun LazyItemScope.MessageListSystemItemContent(systemMessageItem: SystemMessageItemState) {
        DefaultSystemMessageContent(systemMessageState = systemMessageItem)
    }

    /**
     * The default moderated message content of the message list.
     */
    @Composable
    public fun LazyItemScope.MessageListModeratedItemContent(moderatedMessageItem: ModeratedMessageItemState) {
        DefaultMessageModeratedContent(moderatedMessageItemState = moderatedMessageItem)
    }

    /**
     * The default typing indicator content of the message list.
     */
    @Composable
    public fun LazyItemScope.MessageListTypingIndicatorItemContent(typingItem: TypingItemState) {
    }

    /**
     * The default empty thread placeholder item content of the message list.
     */
    @Composable
    public fun LazyItemScope.MessageListEmptyThreadPlaceholderItemContent(
        emptyThreadPlaceholderItem: EmptyThreadPlaceholderItemState,
    ) {
    }

    /**
     * The default start of the channel item content of the message list.
     */
    @Composable
    public fun LazyItemScope.MessageListStartOfTheChannelItemContent(
        startOfTheChannelItem: StartOfTheChannelItemState,
    ) {
    }

    /**
     * The default item content of a regular message.
     */
    @Suppress("LongParameterList")
    @Composable
    public fun LazyItemScope.MessageListItemContent(
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
        DefaultMessageItem(
            messageItem = messageItem,
            reactionSorting = reactionSorting,
            messageContentFactory = MessageContentFactory.Deprecated,
            onPollUpdated = onPollUpdated,
            onCastVote = onCastVote,
            onRemoveVote = onRemoveVote,
            selectPoll = selectPoll,
            onClosePoll = onClosePoll,
            onAddPollOption = onAddPollOption,
            onLongItemClick = onLongItemClick,
            onThreadClick = onThreadClick,
            onReactionsClick = onReactionsClick,
            onGiphyActionClick = onGiphyActionClick,
            onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
            onQuotedMessageClick = onQuotedMessageClick,
            onUserAvatarClick = { onUserAvatarClick?.invoke(messageItem.message.user) },
            onLinkClick = onMessageLinkClick,
            onUserMentionClick = onUserMentionClick,
            onAddAnswer = onAddAnswer,
        )
    }

    /**
     * The default header content of the message item.
     * Usually shown if the message is pinned and a list of reactions for the message.
     */
    @Composable
    public fun ColumnScope.MessageItemHeaderContent(
        messageItem: MessageItemState,
        reactionSorting: ReactionSorting,
        onReactionsClick: (Message) -> Unit,
    ) {
        DefaultMessageItemHeaderContent(
            messageItem = messageItem,
            reactionSorting = reactionSorting,
            onReactionsClick = onReactionsClick,
        )
    }

    /**
     * The default footer content of the message item.
     * Usually showing some of the following UI elements: upload status, thread participants, message timestamp.
     */
    @Composable
    public fun ColumnScope.MessageItemFooterContent(
        messageItem: MessageItemState,
    ) {
        DefaultMessageItemFooterContent(
            messageItem = messageItem,
            messageContentFactory = MessageContentFactory.Deprecated,
        )
    }

    /**
     * The default leading content of the message item.
     * Usually the avatar of the user if the message doesn't belong to the current user.
     */
    @Composable
    public fun RowScope.MessageItemLeadingContent(
        messageItem: MessageItemState,
        onUserAvatarClick: (() -> Unit)?,
    ) {
        DefaultMessageItemLeadingContent(
            messageItem = messageItem,
            onUserAvatarClick = onUserAvatarClick,
        )
    }

    /**
     * The default center content of the message item.
     * Usually a message bubble with attachments or emoji stickers if the message contains only emoji.
     */
    @Suppress("LongParameterList")
    @Composable
    public fun ColumnScope.MessageItemCenterContent(
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

    /**
     * The default trailing content of the message item.
     * Usually an extra spacing at the end of the message item if the author is the current user.
     */
    @Composable
    public fun RowScope.MessageItemTrailingContent(
        messageItem: MessageItemState,
    ) {
        DefaultMessageItemTrailingContent(messageItem = messageItem)
    }

    /**
     * The default Giphy message content.
     */
    @Composable
    public fun MessageGiphyContent(
        message: Message,
        onGiphyActionClick: (GiphyAction) -> Unit,
    ) {
        DefaultMessageGiphyContent(
            message = message,
            onGiphyActionClick = onGiphyActionClick,
        )
    }

    /**
     * The default content of a deleted message.
     */
    @Composable
    public fun MessageDeletedContent(
        modifier: Modifier,
    ) {
        DefaultMessageDeletedContent(modifier = modifier)
    }

    /**
     * The default content of a regular message that can contain attachments and text.
     */
    @Suppress("LongParameterList")
    @Composable
    public fun MessageRegularContent(
        message: Message,
        currentUser: User?,
        onLongItemClick: (Message) -> Unit,
        onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit,
        onQuotedMessageClick: (Message) -> Unit,
        onUserMentionClick: (User) -> Unit,
        onLinkClick: ((Message, String) -> Unit)?,
    ) {
        DefaultMessageContent(
            message = message,
            currentUser = currentUser,
            onLongItemClick = onLongItemClick,
            messageContentFactory = MessageContentFactory.Deprecated,
            onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
            onQuotedMessageClick = onQuotedMessageClick,
            onUserMentionClick = onUserMentionClick,
            onLinkClick = onLinkClick,
        )
    }

    /**
     * The default message text content.
     * Usually with extra styling and padding for the chat bubble.
     */
    @Composable
    public fun MessageTextContent(
        message: Message,
        currentUser: User?,
        onLongItemClick: (Message) -> Unit,
        onLinkClick: ((Message, String) -> Unit)?,
        onUserMentionClick: (User) -> Unit,
    ) {
        MessageText(
            message = message,
            currentUser = currentUser,
            onLongItemClick = onLongItemClick,
            onLinkClick = onLinkClick,
            onUserMentionClick = onUserMentionClick,
        )
    }

    /**
     * The default quoted message content.
     * Usually shows only the sender avatar, text and a single attachment preview.
     */
    @Composable
    public fun MessageQuotedContent(
        message: Message,
        currentUser: User?,
        onLongItemClick: (Message) -> Unit,
        onQuotedMessageClick: (Message) -> Unit,
    ) {
        val quotedMessage = message.replyTo
        if (quotedMessage != null) {
            QuotedMessage(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                message = quotedMessage,
                currentUser = currentUser,
                replyMessage = message,
                onLongItemClick = { onLongItemClick(message) },
                onQuotedMessageClick = onQuotedMessageClick,
            )
        }
    }

    /**
     * The default uploading content of the message footer.
     * Usually shows how many items have been uploaded and the total number of items.
     */
    @Composable
    public fun MessageFooterUploadingContent(
        modifier: Modifier,
        messageItem: MessageItemState,
    ) {
        UploadingFooter(
            modifier = modifier,
            message = messageItem.message,
        )
    }

    /**
     * The default content of the only-visible-to-you footer message.
     */
    @Composable
    public fun MessageFooterOnlyVisibleToYouContent(
        messageItem: MessageItemState,
    ) {
        OwnedMessageVisibilityContent(
            message = messageItem.message,
        )
    }

    /**
     * The default footer content.
     * Usually contains either [MessageThreadFooter] or the default footer,
     * which holds the sender name and the timestamp.
     */
    @Composable
    public fun MessageFooterContent(
        messageItem: MessageItemState,
    ) {
        MessageFooter(messageItem = messageItem)
    }

    /**
     * The default "Unread threads" banner.
     * Shows the number of unread threads in the "ThreadList".
     *
     * @param unreadThreads The number of unread threads.
     * @param onClick Action invoked when the user clicks on the banner.
     */
    @Composable
    public fun ThreadListUnreadThreadsBanner(
        unreadThreads: Int,
        onClick: () -> Unit,
    ) {
        UnreadThreadsBanner(
            unreadThreads = unreadThreads,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            onClick = onClick,
        )
    }

    /**
     * The default thread list item.
     * Shows information about the Thread title, parent message, last reply and number of unread
     * replies.
     *
     * @param thread The thread to display.
     * @param currentUser The current user.
     * @param onThreadClick Action invoked when the user clicks on the thread.
     */
    @Composable
    public fun ThreadListItem(
        thread: Thread,
        currentUser: User?,
        onThreadClick: (Thread) -> Unit,
    ) {
        ThreadItem(thread, currentUser, onThreadClick)
    }

    /**
     * Default representation of the thread title.
     *
     * Used in the [ThreadListItem] to display the title of the thread.
     *
     * @param thread The thread to display.
     * @param channel The channel the thread belongs to.
     * @param currentUser The current user.
     */
    @Composable
    public fun ThreadListItemTitle(
        thread: Thread,
        channel: Channel,
        currentUser: User?,
    ) {
        ThreadItemTitle(channel, currentUser)
    }

    /**
     * Default representation of the parent message preview in a thread.
     *
     * Used in the [ThreadListItem] to display the parent message of the thread.
     *
     * @param thread The thread to display.
     */
    @Composable
    public fun RowScope.ThreadListItemReplyToContent(thread: Thread) {
        ThreadItemReplyToContent(thread.parentMessage)
    }

    /**
     * Default representation of the unread count badge. Not shown if unreadCount == 0.
     *
     * Used in the [ThreadListItem] to display the number of unread replies in the thread.
     *
     * @param unreadCount The number of unread thread replies.
     */
    @Composable
    public fun RowScope.ThreadListItemUnreadCountContent(unreadCount: Int) {
        ThreadItemUnreadCountContent(unreadCount)
    }

    /**
     * Default representation of the latest reply content in a thread.
     * Shows a preview of the last message in the thread.
     *
     * Used in the [ThreadListItem] to display the latest reply in the thread.
     *
     * @param thread The thread to display.
     */
    @Composable
    public fun ThreadListItemLatestReplyContent(thread: Thread) {
        thread.latestReplies.lastOrNull()?.let { reply ->
            ThreadItemLatestReplyContent(reply)
        }
    }

    /**
     * The default empty placeholder that is displayed when there are no threads.
     *
     * @param modifier Modifier for styling.
     */
    @Composable
    public fun ThreadListEmptyContent(modifier: Modifier) {
        DefaultThreadListEmptyContent(modifier)
    }

    /**
     * The default loading content that is displayed during the initial loading of the threads.
     *
     * @param modifier Modifier for styling.
     */
    @Composable
    public fun ThreadListLoadingContent(modifier: Modifier) {
        DefaultThreadListLoadingContent(modifier)
    }

    /**
     * The default content shown on the bottom of the list during the loading of more threads.
     */
    @Composable
    public fun ThreadListLoadingMoreContent() {
        DefaultThreadListLoadingMoreContent()
    }
}
