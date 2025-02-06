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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.channels.list.ChannelOptionState
import io.getstream.chat.android.compose.state.channels.list.ItemState
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResult
import io.getstream.chat.android.compose.state.messageoptions.MessageOptionItemState
import io.getstream.chat.android.compose.state.reactionoptions.ReactionOptionItemState
import io.getstream.chat.android.compose.state.userreactions.UserReactionItemState
import io.getstream.chat.android.compose.ui.channels.header.DefaultChannelHeaderLeadingContent
import io.getstream.chat.android.compose.ui.channels.header.DefaultChannelListHeaderCenterContent
import io.getstream.chat.android.compose.ui.channels.header.DefaultChannelListHeaderTrailingContent
import io.getstream.chat.android.compose.ui.channels.info.DefaultSelectedChannelMenuHeaderContent
import io.getstream.chat.android.compose.ui.channels.info.SelectedChannelMenu
import io.getstream.chat.android.compose.ui.channels.list.ChannelItem
import io.getstream.chat.android.compose.ui.channels.list.DefaultChannelItemCenterContent
import io.getstream.chat.android.compose.ui.channels.list.DefaultChannelItemDivider
import io.getstream.chat.android.compose.ui.channels.list.DefaultChannelItemLeadingContent
import io.getstream.chat.android.compose.ui.channels.list.DefaultChannelItemTrailingContent
import io.getstream.chat.android.compose.ui.channels.list.DefaultChannelListEmptyContent
import io.getstream.chat.android.compose.ui.channels.list.DefaultChannelListLoadingIndicator
import io.getstream.chat.android.compose.ui.channels.list.DefaultChannelSearchEmptyContent
import io.getstream.chat.android.compose.ui.channels.list.DefaultChannelsLoadingMoreIndicator
import io.getstream.chat.android.compose.ui.channels.list.DefaultSearchResultItemCenterContent
import io.getstream.chat.android.compose.ui.channels.list.DefaultSearchResultItemLeadingContent
import io.getstream.chat.android.compose.ui.channels.list.DefaultSearchResultItemTrailingContent
import io.getstream.chat.android.compose.ui.channels.list.SearchResultItem
import io.getstream.chat.android.compose.ui.components.DefaultSearchLabel
import io.getstream.chat.android.compose.ui.components.DefaultSearchLeadingIcon
import io.getstream.chat.android.compose.ui.components.NetworkLoadingIndicator
import io.getstream.chat.android.compose.ui.components.SearchInput
import io.getstream.chat.android.compose.ui.components.channels.ChannelOptions
import io.getstream.chat.android.compose.ui.components.channels.MessageReadStatusIcon
import io.getstream.chat.android.compose.ui.components.composer.ComposerLinkPreview
import io.getstream.chat.android.compose.ui.components.composer.CoolDownIndicator
import io.getstream.chat.android.compose.ui.components.composer.MessageInputOptions
import io.getstream.chat.android.compose.ui.components.messageoptions.MessageOptions
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
import io.getstream.chat.android.compose.ui.components.reactionoptions.ExtendedReactionsOptions
import io.getstream.chat.android.compose.ui.components.reactionoptions.ReactionOptionItem
import io.getstream.chat.android.compose.ui.components.reactionoptions.ReactionOptions
import io.getstream.chat.android.compose.ui.components.reactionpicker.ReactionsPicker
import io.getstream.chat.android.compose.ui.components.selectedmessage.SelectedMessageMenu
import io.getstream.chat.android.compose.ui.components.selectedmessage.SelectedReactionsMenu
import io.getstream.chat.android.compose.ui.components.suggestions.commands.CommandSuggestionItem
import io.getstream.chat.android.compose.ui.components.suggestions.commands.CommandSuggestionList
import io.getstream.chat.android.compose.ui.components.suggestions.commands.DefaultCommandSuggestionItemCenterContent
import io.getstream.chat.android.compose.ui.components.suggestions.commands.DefaultCommandSuggestionItemLeadingContent
import io.getstream.chat.android.compose.ui.components.suggestions.mentions.DefaultMentionSuggestionItemCenterContent
import io.getstream.chat.android.compose.ui.components.suggestions.mentions.DefaultMentionSuggestionItemLeadingContent
import io.getstream.chat.android.compose.ui.components.suggestions.mentions.DefaultMentionSuggestionItemTrailingContent
import io.getstream.chat.android.compose.ui.components.suggestions.mentions.MentionSuggestionItem
import io.getstream.chat.android.compose.ui.components.suggestions.mentions.MentionSuggestionList
import io.getstream.chat.android.compose.ui.components.userreactions.UserReactions
import io.getstream.chat.android.compose.ui.messages.attachments.DefaultAttachmentsPickerSendButton
import io.getstream.chat.android.compose.ui.messages.composer.AttachmentsButton
import io.getstream.chat.android.compose.ui.messages.composer.CommandsButton
import io.getstream.chat.android.compose.ui.messages.composer.DefaultComposerInputContent
import io.getstream.chat.android.compose.ui.messages.composer.DefaultComposerIntegrations
import io.getstream.chat.android.compose.ui.messages.composer.DefaultComposerLabel
import io.getstream.chat.android.compose.ui.messages.composer.DefaultMessageComposerFooterContent
import io.getstream.chat.android.compose.ui.messages.composer.DefaultMessageComposerHeaderContent
import io.getstream.chat.android.compose.ui.messages.composer.DefaultMessageComposerTrailingContent
import io.getstream.chat.android.compose.ui.messages.composer.SendButton
import io.getstream.chat.android.compose.ui.messages.composer.actions.AudioRecordingActions
import io.getstream.chat.android.compose.ui.messages.composer.internal.DefaultAudioRecordButton
import io.getstream.chat.android.compose.ui.messages.composer.internal.DefaultMessageComposerRecordingContent
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
import io.getstream.chat.android.compose.ui.messages.preview.internal.DefaultMessagePreviewItemCenterContent
import io.getstream.chat.android.compose.ui.messages.preview.internal.DefaultMessagePreviewItemLeadingContent
import io.getstream.chat.android.compose.ui.messages.preview.internal.DefaultMessagePreviewItemTrailingContent
import io.getstream.chat.android.compose.ui.pinned.DefaultPinnedMessageListEmptyContent
import io.getstream.chat.android.compose.ui.pinned.DefaultPinnedMessageListItemDivider
import io.getstream.chat.android.compose.ui.pinned.DefaultPinnedMessageListLoadingContent
import io.getstream.chat.android.compose.ui.pinned.DefaultPinnedMessageListLoadingMoreContent
import io.getstream.chat.android.compose.ui.pinned.PinnedMessageItem
import io.getstream.chat.android.compose.ui.threads.DefaultThreadListEmptyContent
import io.getstream.chat.android.compose.ui.threads.DefaultThreadListLoadingContent
import io.getstream.chat.android.compose.ui.threads.DefaultThreadListLoadingMoreContent
import io.getstream.chat.android.compose.ui.threads.ThreadItem
import io.getstream.chat.android.compose.ui.threads.ThreadItemLatestReplyContent
import io.getstream.chat.android.compose.ui.threads.ThreadItemReplyToContent
import io.getstream.chat.android.compose.ui.threads.ThreadItemTitle
import io.getstream.chat.android.compose.ui.threads.ThreadItemUnreadCountContent
import io.getstream.chat.android.compose.ui.threads.UnreadThreadsBanner
import io.getstream.chat.android.compose.ui.util.ReactionIcon
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.LinkPreview
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.ReactionSorting
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.ui.common.state.channels.actions.ChannelAction
import io.getstream.chat.android.ui.common.state.messages.MessageAction
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.React
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState
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

// Default values
private const val DefaultCellsCount: Int = 5

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
@Suppress("TooManyFunctions", "LargeClass", "LongParameterList")
public interface ChatComponentFactory {

    /**
     * The default header shown above the channel list.
     * Usually contains the current user's avatar, a title or the connected status, and an action button.
     */
    @Suppress("LongParameterList")
    @Composable
    public fun ChannelListHeader(
        modifier: Modifier,
        title: String,
        currentUser: User?,
        connectionState: ConnectionState,
        onAvatarClick: (User?) -> Unit,
        onHeaderActionClick: () -> Unit,
    ) {
        io.getstream.chat.android.compose.ui.channels.header.ChannelListHeader(
            modifier = modifier,
            title = title,
            currentUser = currentUser,
            connectionState = connectionState,
            onAvatarClick = onAvatarClick,
            onHeaderActionClick = onHeaderActionClick,
        )
    }

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
        ChannelItem(
            channelItem = channelItem,
            currentUser = currentUser,
            onChannelClick = onChannelClick,
            onChannelLongClick = onChannelLongClick,
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
     * The default search input of the channel list.
     *
     * @param modifier Modifier for styling.
     * @param query Current query string.
     * @param onSearchStarted Action invoked when the search starts.
     * @param onValueChange Action invoked when the query value changes.
     */
    @Composable
    public fun ChannelListSearchInput(
        modifier: Modifier,
        query: String,
        onSearchStarted: () -> Unit,
        onValueChange: (String) -> Unit,
    ) {
        SearchInput(
            modifier = modifier,
            query = query,
            onSearchStarted = onSearchStarted,
            onValueChange = onValueChange,
        )
    }

    /**
     * The default leading icon of the search input.
     *
     * Used by [ChannelListSearchInput].
     */
    @Composable
    public fun RowScope.SearchInputLeadingIcon() {
        DefaultSearchLeadingIcon()
    }

    /**
     * The default label of the search input.
     *
     * Used by [ChannelListSearchInput].
     */
    @Composable
    public fun SearchInputLabel() {
        DefaultSearchLabel()
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
     * The default search result item of the channel list.
     */
    @Composable
    public fun LazyItemScope.SearchResultItemContent(
        searchResultItem: ItemState.SearchResultItemState,
        currentUser: User?,
        onSearchResultClick: (Message) -> Unit,
    ) {
        SearchResultItem(
            searchResultItemState = searchResultItem,
            currentUser = currentUser,
            onSearchResultClick = onSearchResultClick,
        )
    }

    /**
     * The default leading content of a search result item. Shows the avatar of the user who sent the message.
     *
     * Used by [SearchResultItemContent].
     *
     * @param searchResultItem The search result item.
     * @param currentUser The currently logged in user.
     */
    @Composable
    public fun RowScope.SearchResultItemLeadingContent(
        searchResultItem: ItemState.SearchResultItemState,
        currentUser: User?,
    ) {
        DefaultSearchResultItemLeadingContent(searchResultItem, currentUser)
    }

    /**
     * The default center content of a search result item. Shows information about the message and by who and where
     * it was sent.
     *
     * Used by [SearchResultItemContent].
     *
     * @param searchResultItem The state of the search result item.
     * @param currentUser The currently logged in user.
     */
    @Composable
    public fun RowScope.SearchResultItemCenterContent(
        searchResultItem: ItemState.SearchResultItemState,
        currentUser: User?,
    ) {
        DefaultSearchResultItemCenterContent(searchResultItem, currentUser)
    }

    /**
     * The default trailing content of a search result item. Shows the message timestamp.
     *
     * Used by [SearchResultItemContent].
     *
     * @param searchResultItem The state of the search result item.
     */
    @Composable
    public fun RowScope.SearchResultItemTrailingContent(searchResultItem: ItemState.SearchResultItemState) {
        DefaultSearchResultItemTrailingContent(searchResultItem)
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
    @Suppress("LongParameterList")
    @Composable
    public fun MessageQuotedContent(
        modifier: Modifier,
        message: Message,
        currentUser: User?,
        replyMessage: Message,
        onLongItemClick: (Message) -> Unit,
        onQuotedMessageClick: (Message) -> Unit,
    ) {
        QuotedMessage(
            modifier = modifier,
            message = message,
            currentUser = currentUser,
            replyMessage = replyMessage,
            onLongItemClick = onLongItemClick,
            onQuotedMessageClick = onQuotedMessageClick,
        )
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
     * The default status indicator of the message footer, weather the message is sent, pending or read.
     */
    @Composable
    public fun MessageFooterStatusIndicator(
        modifier: Modifier,
        message: Message,
        isMessageRead: Boolean,
        readCount: Int,
    ) {
        MessageReadStatusIcon(
            modifier = modifier,
            message = message,
            isMessageRead = isMessageRead,
            readCount = readCount,
        )
    }

    /**
     * The default header content of the message composer.
     * Shown on top of the composer and contains additional info/context, and is shown during editing/replying to a
     * message, or when there is a link pasted in the composer.
     *
     * @param state The current state of the message composer.
     * @param onCancel The action to perform when the cancel button is clicked.
     * @param onLinkPreviewClick The action to perform when the link preview is clicked.
     */
    @Composable
    public fun ColumnScope.MessageComposerHeaderContent(
        state: MessageComposerState,
        onCancel: () -> Unit,
        onLinkPreviewClick: ((LinkPreview) -> Unit)?,
    ) {
        DefaultMessageComposerHeaderContent(state, onCancel, onLinkPreviewClick)
    }

    /**
     * The default options header for the message input component. It is based on the currently active
     * message action - [io.getstream.chat.android.ui.common.state.messages.Reply] or
     * [io.getstream.chat.android.ui.common.state.messages.Edit].
     * It shows a heading based on the action and a cancel button to cancel the action.
     *
     * Used as part of [MessageComposerHeaderContent].
     *
     * @param modifier The modifier to apply to the composable.
     * @param activeAction The currently active message action.
     * @param onCancel The action to perform when the cancel button is clicked.
     */
    @Composable
    public fun MessageComposerMessageInputOptions(
        modifier: Modifier,
        activeAction: MessageAction,
        onCancel: () -> Unit,
    ) {
        MessageInputOptions(activeAction, onCancel, modifier)
    }

    /**
     * Shows a preview of the link that the user has entered in the message composer.
     * Shows the link image preview, the title of the link and its description.
     *
     * Used as part of [MessageComposerHeaderContent].
     *
     * @param modifier The modifier to apply to the composable.
     * @param linkPreview The link preview to show.
     * @param onClick The action to perform when the link preview is clicked.
     */
    @Composable
    public fun MessageComposerLinkPreview(
        modifier: Modifier,
        linkPreview: LinkPreview,
        onClick: ((LinkPreview) -> Unit)?,
    ) {
        ComposerLinkPreview(modifier, linkPreview, onClick)
    }

    /**
     * The default footer content of the message composer.
     * Shown during replying to a thread, and it provides the checkbox to also send the message to the channel.
     *
     * @param state The current state of the message composer.
     * @param onAlsoSendToChannelSelected The action to perform when the "Also send to channel" checkbox is selected.
     */
    @Composable
    public fun ColumnScope.MessageComposerFooterContent(
        state: MessageComposerState,
        onAlsoSendToChannelSelected: (Boolean) -> Unit,
    ) {
        DefaultMessageComposerFooterContent(state, onAlsoSendToChannelSelected)
    }

    /**
     * The default mentions popup content of the message composer.
     * Shown when the user types '@' in the composer, and there are available users that can be mentioned.
     *
     * @param mentionSuggestions The list of mention suggestions to show.
     * @param onMentionSelected The action to perform when a mention is selected.
     */
    @Composable
    public fun MessageComposerMentionsPopupContent(
        mentionSuggestions: List<User>,
        onMentionSelected: (User) -> Unit,
    ) {
        MentionSuggestionList(users = mentionSuggestions, onMentionSelected = onMentionSelected)
    }

    /**
     * The default mention suggestion item of the message composer.
     *
     * Used in [MessageComposerMentionsPopupContent].
     *
     * @param user The user for which the suggestion is rendered.
     * @param onMentionSelected The action to perform when the mention is selected.
     */
    @Composable
    public fun MessageComposerMentionSuggestionItem(
        user: User,
        onMentionSelected: (User) -> Unit,
    ) {
        MentionSuggestionItem(user = user, onMentionSelected = onMentionSelected)
    }

    /**
     * The default leading content of the mention suggestion item of the message composer.
     *
     * Used as part of [MessageComposerMentionSuggestionItem].
     *
     * @param user The user for which the leading content is rendered.
     */
    @Composable
    public fun RowScope.MessageComposerMentionSuggestionItemLeadingContent(user: User) {
        DefaultMentionSuggestionItemLeadingContent(user)
    }

    /**
     * The default center content of the mention suggestion item of the message composer.
     *
     * Used as part of [MessageComposerMentionSuggestionItem].
     *
     * @param user The user for which the center content is rendered.
     */
    @Composable
    public fun RowScope.MessageComposerMentionSuggestionItemCenterContent(user: User) {
        DefaultMentionSuggestionItemCenterContent(user)
    }

    /**
     * The default trailing content of the mention suggestion item of the message composer.
     *
     * Used as part of [MessageComposerMentionSuggestionItem].
     *
     * @param user The user for which the trailing content is rendered.
     */
    @Composable
    public fun RowScope.MessageComposerMentionSuggestionItemTrailingContent(user: User) {
        DefaultMentionSuggestionItemTrailingContent()
    }

    /**
     * The default instant commands popup content of the message composer.
     * Shown when the user types '/' in the composer, and there are available commands that can be used, or when the
     * user click the "Commands" button.
     *
     * @param commandSuggestions The list of command suggestions to show.
     * @param onCommandSelected The action to perform when a command is selected.
     */
    @Composable
    public fun MessageComposerCommandsPopupContent(
        commandSuggestions: List<Command>,
        onCommandSelected: (Command) -> Unit,
    ) {
        CommandSuggestionList(commands = commandSuggestions, onCommandSelected = onCommandSelected)
    }

    /**
     * The default command suggestion item of the message composer.
     *
     * Used in [MessageComposerCommandsPopupContent].
     *
     * @param command The command for which the suggestion is rendered.
     * @param onCommandSelected The action to perform when the command is selected.
     */
    @Composable
    public fun MessageComposerCommandSuggestionItem(
        command: Command,
        onCommandSelected: (Command) -> Unit,
    ) {
        CommandSuggestionItem(command, onCommandSelected = onCommandSelected)
    }

    /**
     * The default leading content of the command suggestion item of the message composer.
     *
     * Used as part of [MessageComposerCommandSuggestionItem].
     *
     * @param command The command for which the leading content is rendered.
     */
    @Composable
    public fun RowScope.MessageComposerCommandSuggestionItemLeadingContent(command: Command) {
        DefaultCommandSuggestionItemLeadingContent()
    }

    /**
     * The default center content of the command suggestion item of the message composer.
     *
     * Used as part of [MessageComposerCommandSuggestionItem].
     *
     * @param modifier The modifier to apply to the composable.
     * @param command The command for which the center content is rendered.
     */
    @Composable
    public fun RowScope.MessageComposerCommandSuggestionItemCenterContent(
        modifier: Modifier,
        command: Command,
    ) {
        DefaultCommandSuggestionItemCenterContent(command, modifier)
    }

    /**
     * The default integrations of the message composer.
     * Provides the "Attachments" and "Commands" buttons shown before the composer.
     *
     * @param state The current state of the message composer.
     * @param onAttachmentsClick The action to perform when the "Attachments" button is clicked.
     * @param onCommandsClick The action to perform when the "Commands" button is clicked.
     */
    @Composable
    public fun RowScope.MessageComposerIntegrations(
        state: MessageComposerState,
        onAttachmentsClick: () -> Unit,
        onCommandsClick: () -> Unit,
    ) {
        DefaultComposerIntegrations(state, onAttachmentsClick, onCommandsClick, state.ownCapabilities)
    }

    /**
     * The default attachments button of the message composer.
     *
     * Used as part of [MessageComposerIntegrations].
     *
     * @param enabled Whether the button is enabled.
     * @param onClick The action to perform when the button is clicked.
     */
    @Composable
    public fun RowScope.MessageComposerAttachmentsButton(
        enabled: Boolean,
        onClick: () -> Unit,
    ) {
        AttachmentsButton(enabled, onClick)
    }

    /**
     * The default commands button of the message composer.
     *
     * Used as part of [MessageComposerIntegrations].
     *
     * @param hasCommandSuggestions Whether there are command suggestions available.
     * @param enabled Whether the button is enabled.
     * @param onClick The action to perform when the button is clicked.
     */
    @Composable
    public fun RowScope.MessageComposerCommandsButton(
        hasCommandSuggestions: Boolean,
        enabled: Boolean,
        onClick: () -> Unit,
    ) {
        CommandsButton(hasCommandSuggestions, enabled, onClick)
    }

    /**
     * The default label of the message composer.
     *
     * Used by [MessageComposerInput].
     *
     * @param state The current state of the message composer.
     */
    @Composable
    public fun MessageComposerLabel(state: MessageComposerState) {
        DefaultComposerLabel(state.ownCapabilities)
    }

    /**
     * The default input content of the message composer.
     *
     * @param state The current state of the message composer.
     * @param onInputChanged The action to perform when the input changes.
     * @param onAttachmentRemoved The action to perform when an attachment is removed.
     * @param label The label to show in the composer.
     */
    @Composable
    public fun RowScope.MessageComposerInput(
        state: MessageComposerState,
        onInputChanged: (String) -> Unit,
        onAttachmentRemoved: (Attachment) -> Unit,
        label: @Composable (MessageComposerState) -> Unit,
    ) {
        DefaultComposerInputContent(state, onInputChanged, onAttachmentRemoved, label)
    }

    /**
     * The default appearance of a quoted message in the message composer.
     * Shown when the user quotes (replies to) a message in the composer.
     *
     * Used as part of [MessageComposerInput].
     *
     * @param modifier The modifier to apply to the composable.
     * @param state The current state of the message composer.
     * @param quotedMessage The message that is being quoted (replied to).
     */
    @Composable
    public fun MessageComposerQuotedMessage(
        modifier: Modifier,
        state: MessageComposerState,
        quotedMessage: Message,
    ) {
        QuotedMessage(
            modifier = modifier,
            message = quotedMessage,
            currentUser = state.currentUser,
            replyMessage = null,
            onLongItemClick = {},
            onQuotedMessageClick = {},
        )
    }

    /**
     * The default trailing content of the message composer. Contains the "Send" button, and "Audio record" button (if
     * enabled).
     *
     * @param state The current state of the message composer.
     * @param onSendClick The action to perform when the "Send" button is clicked. Supply the message text and
     * attachments.
     * @param recordingActions The actions to control the audio recording.
     */
    @Composable
    public fun MessageComposerTrailingContent(
        state: MessageComposerState,
        onSendClick: (String, List<Attachment>) -> Unit,
        recordingActions: AudioRecordingActions,
    ) {
        DefaultMessageComposerTrailingContent(state, onSendClick, recordingActions)
    }

    /**
     * The default cooldown indicator of the message composer.
     * Shown when the user is prevented from sending messages due to a cooldown.
     *
     * Used as part of [MessageComposerTrailingContent].
     *
     * @param modifier The modifier to apply to the composable.
     * @param coolDownTime The remaining time until the user can send a message.
     */
    @Composable
    public fun MessageComposerCoolDownIndicator(
        modifier: Modifier,
        coolDownTime: Int,
    ) {
        CoolDownIndicator(coolDownTime, modifier)
    }

    /**
     * The default "Send" button of the message composer.
     *
     * Used as part of [MessageComposerTrailingContent].
     *
     * @param enabled Whether the button is enabled.
     * @param isInputValid Whether the current input in the message composer is valid.
     * @param onClick The action to perform when the button is clicked.
     */
    @Composable
    public fun MessageComposerSendButton(
        enabled: Boolean,
        isInputValid: Boolean,
        onClick: () -> Unit,
    ) {
        SendButton(enabled, isInputValid, onClick)
    }

    /**
     * The default "Audio record (voice message)" button of the message composer.
     *
     * Used as part of [MessageComposerTrailingContent].
     *
     * @param state The current state of the recording process.
     * @param recordingActions The actions to control the audio recording.
     */
    @Composable
    public fun MessageComposerAudioRecordButton(
        state: RecordingState,
        recordingActions: AudioRecordingActions,
    ) {
        DefaultAudioRecordButton(state, recordingActions)
    }

    /**
     * Default composable used for displaying audio recording information while audio recording is in progress.
     *
     * @param state The current state of the message composer.
     * @param recordingActions The actions to control the audio recording.
     */
    @Composable
    public fun RowScope.MessageComposerAudioRecordingContent(
        state: MessageComposerState,
        recordingActions: AudioRecordingActions,
    ) {
        DefaultMessageComposerRecordingContent(state, recordingActions)
    }

    /**
     * The default avatar, which renders an image from the provided image URL.
     * In case the image URL is empty or there is an error loading the image,
     * it falls back to an image with initials.
     */
    @Suppress("LongParameterList")
    @Composable
    public fun Avatar(
        modifier: Modifier,
        imageUrl: String,
        initials: String,
        shape: Shape,
        textStyle: TextStyle,
        placeholderPainter: Painter?,
        contentDescription: String?,
        initialsAvatarOffset: DpOffset,
        onClick: (() -> Unit)?,
    ) {
        io.getstream.chat.android.compose.ui.components.avatar.Avatar(
            modifier = modifier,
            imageUrl = imageUrl,
            initials = initials,
            shape = shape,
            textStyle = textStyle,
            placeholderPainter = placeholderPainter,
            contentDescription = contentDescription,
            initialsAvatarOffset = initialsAvatarOffset,
            onClick = onClick,
        )
    }

    /**
     * The default user avatar content.
     * It renders the [User] avatar that's shown on the messages screen or in headers of direct messages.
     * If [showOnlineIndicator] is `true` and the user is online, it uses [Avatar] to shows an image or their initials.
     */
    @Suppress("LongParameterList")
    @Composable
    public fun UserAvatar(
        modifier: Modifier,
        user: User,
        textStyle: TextStyle,
        showOnlineIndicator: Boolean,
        onlineIndicator: @Composable BoxScope.() -> Unit,
        onClick: (() -> Unit)?,
    ) {
        io.getstream.chat.android.compose.ui.components.avatar.UserAvatar(
            modifier = modifier,
            user = user,
            textStyle = textStyle,
            contentDescription = user.name,
            showOnlineIndicator = showOnlineIndicator,
            onlineIndicator = onlineIndicator,
            onClick = onClick,
        )
    }

    /**
     * The default group avatar, which renders a matrix of user images or initials.
     */
    @Composable
    public fun GroupAvatar(
        modifier: Modifier,
        users: List<User>,
        shape: Shape,
        textStyle: TextStyle,
        onClick: (() -> Unit)?,
    ) {
        io.getstream.chat.android.compose.ui.components.avatar.GroupAvatar(
            modifier = modifier,
            users = users,
            shape = shape,
            textStyle = textStyle,
            onClick = onClick,
        )
    }

    /**
     * The default avatar for a channel.
     * It renders the [Channel] avatar that's shown when browsing channels or when you open the messages screen.
     * Based on the state of the [Channel] and the number of members,
     * it might use [Avatar], [UserAvatar], or [GroupAvatar] to show different types of images.
     */
    @Composable
    public fun ChannelAvatar(
        modifier: Modifier,
        channel: Channel,
        currentUser: User?,
        onClick: (() -> Unit)?,
    ) {
        io.getstream.chat.android.compose.ui.components.avatar.ChannelAvatar(
            modifier = modifier,
            channel = channel,
            currentUser = currentUser,
            contentDescription = channel.name,
            onClick = onClick,
        )
    }

    // REGION: Channel menu
    /**
     * Factory method for creating the full content of the SelectedChannelMenu.
     *
     * @param modifier The modifier for the menu.
     * @param selectedChannel The selected channel.
     * @param isMuted Whether the channel is muted.
     * @param currentUser The current user.
     * @param onChannelOptionClick Callback for when a channel option is clicked.
     * @param onDismiss Callback for when the menu is dismissed.
     */
    @Composable
    public fun ChannelMenu(
        modifier: Modifier,
        selectedChannel: Channel,
        isMuted: Boolean,
        currentUser: User?,
        onChannelOptionClick: (ChannelAction) -> Unit,
        onDismiss: () -> Unit,
    ) {
        SelectedChannelMenu(
            modifier = modifier,
            selectedChannel = selectedChannel,
            isMuted = isMuted,
            currentUser = currentUser,
            onChannelOptionClick = onChannelOptionClick,
            onDismiss = onDismiss,
        )
    }

    /**
     * Factory method for creating the header content of the SelectedChannelMenu.
     *
     * @param selectedChannel The selected channel.
     * @param currentUser The current user.
     */
    @Composable
    public fun ChannelMenuHeaderContent(
        modifier: Modifier,
        selectedChannel: Channel,
        currentUser: User?,
    ) {
        DefaultSelectedChannelMenuHeaderContent(
            selectedChannel = selectedChannel,
            currentUser = currentUser,
        )
    }

    /**
     * Factory method for creating the center content of the SelectedChannelMenu.
     *
     * @param onChannelOptionClick Callback for when a channel option is clicked.
     * @param channelOptions List of channel options.
     */
    @Composable
    public fun ChannelMenuCenterContent(
        modifier: Modifier,
        onChannelOptionClick: (ChannelAction) -> Unit,
        channelOptions: List<ChannelOptionState>,
    ) {
        ChannelMenuOptions(
            channelOptions = channelOptions,
            onChannelOptionClick = onChannelOptionClick,
            modifier = modifier,
        )
    }

    /**
     * Factory method for creating the options content of the SelectedChannelMenu.
     *
     * @param onChannelOptionClick Callback for when a channel option is clicked.
     * @param channelOptions List of channel options.
     */
    @Composable
    public fun ChannelMenuOptions(
        modifier: Modifier,
        onChannelOptionClick: (ChannelAction) -> Unit,
        channelOptions: List<ChannelOptionState>,
    ) {
        ChannelOptions(
            options = channelOptions,
            onChannelOptionClick = onChannelOptionClick,
            modifier = modifier,
        )
    }

    /**
     * Factory method for creating the footer content of the SelectedChannelMenu.
     *
     * @param modifier The modifier for the footer.
     */
    @Composable
    public fun ChannelOptionsItem(
        modifier: Modifier,
        option: ChannelOptionState,
        onClick: () -> Unit,
    ) {
        MenuOptionItem(
            modifier = modifier,
            title = option.title,
            titleColor = option.titleColor,
            leadingIcon = {
                ChannelOptionsItemLeadingIcon(Modifier, option)
            },
            onClick = onClick,
            style = ChatTheme.typography.bodyBold,
            itemHeight = 56.dp,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        )
    }

    /**
     * Factory method for creating the leading icon of the Channel options menu item.
     *
     * @param option The channel option state.
     */
    @Composable
    public fun ChannelOptionsItemLeadingIcon(modifier: Modifier, option: ChannelOptionState) {
        Icon(
            modifier = modifier
                .size(56.dp)
                .padding(16.dp),
            painter = option.iconPainter,
            tint = option.iconColor,
            contentDescription = null,
        )
    }

    // REGION: Message menu
    /**
     * Factory method for creating the full content of the SelectedMessageMenu.
     * This is the menu that appears when a message is long-pressed.
     *
     * @param modifier The modifier for the menu.
     * @param message The selected message.
     * @param messageOptions List of message options.
     * @param ownCapabilities The capabilities of the current user.
     * @param onMessageAction Callback for when a message action is clicked.
     * @param onDismiss Callback for when the menu is dismissed.
     */
    @Composable
    public fun MessageMenu(
        modifier: Modifier,
        message: Message,
        messageOptions: List<MessageOptionItemState>,
        ownCapabilities: Set<String>,
        onMessageAction: (MessageAction) -> Unit,
        onShowMore: () -> Unit,
        onDismiss: () -> Unit,
    ) {
        SelectedMessageMenu(
            modifier = modifier,
            messageOptions = messageOptions,
            message = message,
            ownCapabilities = ownCapabilities,
            onMessageAction = onMessageAction,
            onShowMoreReactionsSelected = onShowMore,
            onDismiss = onDismiss,
        )
    }

    /**
     * Factory method for creating the center content of the SelectedMessageMenu.
     *
     * @param modifier The modifier for the center content.
     * @param message The selected message.
     * @param messageOptions List of message options.
     * @param ownCapabilities The capabilities of the current user.
     */
    @Composable
    public fun MessageMenuCenterContent(
        modifier: Modifier,
        message: Message,
        messageOptions: List<MessageOptionItemState>,
        onMessageAction: (MessageAction) -> Unit,
        ownCapabilities: Set<String>,
    ) {
        MessageMenuOptions(
            modifier = modifier,
            message = message,
            options = messageOptions,
            onMessageOptionSelected = { onMessageAction(it.action) },
        )
    }

    /**
     * Factory method for creating the header content of the SelectedMessageMenu.
     *
     * @param message The selected message.
     * @param messageOptions List of message options.
     * @param ownCapabilities The capabilities of the current user.
     * @param onShowMore Callback for when the show more reactions option is clicked.
     * @param onMessageAction Callback for when a message action is clicked.
     * @param reactionTypes The reaction types.
     * @param showMoreReactionsIcon The icon to show for the "Show more reactions" option.
     */
    @Composable
    public fun MessageMenuHeaderContent(
        modifier: Modifier,
        message: Message,
        messageOptions: List<MessageOptionItemState>,
        onMessageAction: (MessageAction) -> Unit,
        ownCapabilities: Set<String>,
        onShowMore: () -> Unit,
        reactionTypes: Map<String, ReactionIcon>,
        showMoreReactionsIcon: Int,
    ) {
        ReactionMenuOptions(
            modifier = modifier,
            message = message,
            reactionTypes = reactionTypes,
            onMessageAction = onMessageAction,
            onShowMoreReactionsSelected = onShowMore,
            showMoreReactionsIcon = showMoreReactionsIcon,
        )
    }

    /**
     * Shows the default message options.
     *
     * @param modifier The modifier for the message options.
     * @param options The list of message options.
     */
    @Composable
    public fun MessageMenuOptions(
        modifier: Modifier,
        message: Message,
        options: List<MessageOptionItemState>,
        onMessageOptionSelected: (MessageOptionItemState) -> Unit,
    ) {
        MessageOptions(
            modifier = modifier,
            onMessageOptionSelected = onMessageOptionSelected,
            options = options,
        )
    }

    /**
     * Factory method for creating the options content of the SelectedMessageMenu.
     */
    @Composable
    public fun MessageMenuOptionsItem(
        modifier: Modifier,
        option: MessageOptionItemState,
        onMessageOptionSelected: (MessageOptionItemState) -> Unit,
    ) {
        val title = stringResource(id = option.title)
        // Not using directly the [MessageOptionsItem] because
        // that one contains our default behavior which is not overridable.
        MenuOptionItem(
            modifier = modifier
                .fillMaxWidth()
                .height(ChatTheme.dimens.messageOptionsItemHeight),
            title = title,
            titleColor = option.titleColor,
            leadingIcon = {
                MessageMenuOptionsItemLeadingContent(modifier, option)
            },
            onClick = { onMessageOptionSelected(option) },
            style = ChatTheme.typography.body,
            itemHeight = 56.dp,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        )
    }

    /**
     * Factory method for creating the leading icon of the Message options menu item.
     * This is the icon that appears on the left side of the message option.
     *
     * @param option The message option state.
     */
    @Composable
    public fun MessageMenuOptionsItemLeadingContent(
        modifier: Modifier,
        option: MessageOptionItemState,
    ) {
        Icon(
            modifier = modifier.padding(horizontal = 16.dp),
            painter = option.iconPainter,
            tint = option.iconColor,
            contentDescription = null,
        )
    }

    // REGION: Reaction menu and items
    /**
     * Factory method for creating the full content of the SelectedReactionsMenu.
     *
     * @param modifier The modifier for the menu.
     * @param currentUser The current user.
     * @param message The selected message.
     * @param ownCapabilities The capabilities of the current user.
     * @param onMessageAction Callback for when a message action is clicked.
     * @param onDismiss Callback for when the menu is dismissed.
     */
    @Composable
    public fun ReactionsMenu(
        modifier: Modifier,
        currentUser: User?,
        message: Message,
        onMessageAction: (MessageAction) -> Unit,
        onShowMoreReactionsSelected: () -> Unit,
        ownCapabilities: Set<String>,
        onDismiss: () -> Unit,
    ) {
        SelectedReactionsMenu(
            modifier = modifier,
            currentUser = currentUser,
            message = message,
            onMessageAction = onMessageAction,
            onShowMoreReactionsSelected = onShowMoreReactionsSelected,
            onDismiss = onDismiss,
            ownCapabilities = ownCapabilities,
        )
    }

    /**
     * Factory method for creating the header content of the SelectedReactionsMenu.
     *
     * @param message The selected message.
     * @param reactionTypes The reaction types.
     * @param onMessageAction Callback for when a message action is clicked.
     * @param onShowMoreReactionsSelected Callback for when the show more reactions option is clicked.
     */
    @Composable
    public fun ReactionsMenuHeaderContent(
        modifier: Modifier,
        message: Message,
        reactionTypes: Map<String, ReactionIcon>,
        onMessageAction: (MessageAction) -> Unit,
        onShowMoreReactionsSelected: () -> Unit,
        showMoreReactionsIcon: Int,
    ) {
        ReactionMenuOptions(
            modifier = modifier,
            message = message,
            reactionTypes = reactionTypes,
            onMessageAction = onMessageAction,
            onShowMoreReactionsSelected = onShowMoreReactionsSelected,
            showMoreReactionsIcon = showMoreReactionsIcon,
        )
    }

    /**
     * Factory method for creating the center content of the reactions menu.
     *
     * @param modifier The modifier for the center content.
     * @param userReactions The user reactions.
     */
    @Composable
    public fun ReactionsMenuCenterContent(
        modifier: Modifier,
        userReactions: List<UserReactionItemState>,
    ) {
        UserReactions(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = ChatTheme.dimens.userReactionsMaxHeight)
                .padding(vertical = 16.dp),
            items = userReactions,
        )
    }

    /**
     * Factory method for the reaction options in the menu.
     *
     * @param message The selected message.
     * @param reactionTypes The reaction types.
     * @param onMessageAction Callback for when a message action is clicked.
     * @param onShowMoreReactionsSelected Callback for when the show more reactions option is clicked.
     */
    @Composable
    public fun ReactionMenuOptions(
        modifier: Modifier,
        message: Message,
        reactionTypes: Map<String, ReactionIcon>,
        onMessageAction: (MessageAction) -> Unit,
        onShowMoreReactionsSelected: () -> Unit,
        showMoreReactionsIcon: Int,
    ) {
        ReactionOptions(
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 20.dp),
            reactionTypes = reactionTypes,
            showMoreReactionsIcon = showMoreReactionsIcon,
            onReactionOptionSelected = {
                onMessageAction(
                    React(
                        reaction = Reaction(messageId = message.id, type = it.type),
                        message = message,
                    ),
                )
            },
            onShowMoreReactionsSelected = onShowMoreReactionsSelected,
            ownReactions = message.ownReactions,
        )
    }

    /**
     * Factory method for creating the header content of the SelectedReactionsMenu.
     *
     * @param modifier The modifier for the header.
     * @param option the reaction option.
     * @param onReactionOptionSelected Callback for when a reaction option is clicked.
     */
    @Composable
    public fun ReactionMenuOptionItem(
        modifier: Modifier,
        option: ReactionOptionItemState,
        onReactionOptionSelected: (ReactionOptionItemState) -> Unit,
    ) {
        ReactionOptionItem(
            modifier = modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = false),
                    onClick = { onReactionOptionSelected(option) },
                ),
            option = option,
        )
    }

    /**
     * Factory method for creating the reactions menu more option.
     *
     * @param onShowMoreReactionsSelected Callback for when the show more reactions option is clicked.
     * @param showMoreReactionsIcon The icon for the show more reactions option.
     */
    @Composable
    public fun ReactionMenuShowMore(
        modifier: Modifier,
        onShowMoreReactionsSelected: () -> Unit,
        showMoreReactionsIcon: Int,
    ) {
        Icon(
            modifier = modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false),
                onClick = { onShowMoreReactionsSelected() },
            ),
            painter = painterResource(id = showMoreReactionsIcon),
            contentDescription = LocalContext.current.getString(R.string.stream_compose_show_more_reactions),
            tint = ChatTheme.colors.textLowEmphasis,
        )
    }

    /**
     * Factory method for creating the center content of the reactions menu.
     *
     * @param modifier The modifier for the center content.
     * @param message The selected message.
     */
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    public fun ExtendedReactionsMenuOptions(
        modifier: Modifier,
        message: Message,
        reactionTypes: Map<String, ReactionIcon>,
        onMessageAction: (MessageAction) -> Unit,
    ) {
        ExtendedReactionsOptions(
            modifier = modifier
                .fillMaxWidth(),
            reactionTypes = reactionTypes,
            onReactionOptionSelected = {
                onMessageAction(
                    React(
                        reaction = Reaction(messageId = message.id, type = it.type),
                        message = message,
                    ),
                )
            },
            cells = GridCells.Fixed(DefaultCellsCount),
            ownReactions = message.ownReactions,
        )
    }

    /**
     * Factory method for creating the header content of the SelectedReactionsMenu.
     *
     * @param modifier The modifier for the header.
     * @param option the reaction option.
     * @param onReactionOptionSelected Callback for when a reaction option is clicked.
     */
    @Composable
    public fun ExtendedReactionMenuOptionItem(
        modifier: Modifier,
        option: ReactionOptionItemState,
        onReactionOptionSelected: (ReactionOptionItemState) -> Unit,
    ) {
        ReactionMenuOptionItem(
            modifier = modifier,
            onReactionOptionSelected = onReactionOptionSelected,
            option = option,
        )
    }

    /**
     * Factory method for creating the reactions menu more option.
     *
     * @param modifier The modifier
     * @param message The selected message.
     * @param onMessageAction Callback for when a message action is clicked.
     * @param onDismiss Callback for when the menu is dismissed.
     */
    @Composable
    public fun MessageReactionPicker(
        modifier: Modifier,
        message: Message,
        onMessageAction: (MessageAction) -> Unit,
        onDismiss: () -> Unit,
    ) {
        ReactionsPicker(
            modifier = modifier,
            message = message,
            onMessageAction = onMessageAction,
            onDismiss = onDismiss,
        )
    }

    /**
     * Factory method for creating the header content of the reaction picker.
     *
     * * @param modifier The modifier
     * @param message The selected message.
     * @param onMessageAction Callback for when a message action is clicked.
     * @param onDismiss Callback for when the menu is dismissed.
     */
    @Composable
    public fun MessageReactionPickerHeaderContent(
        modifier: Modifier,
        message: Message,
        onMessageAction: (MessageAction) -> Unit,
        onDismiss: () -> Unit,
    ) {
        // This composable is empty on purpose. By default we don't have a header to the picker.
    }

    /**
     * Factory method for creating the center content of the reaction picker.
     *
     * @param modifier The modifier
     * @param message The selected message.
     * @param onMessageAction Callback for when a message action is clicked.
     * @param reactionTypes The reaction types.
     * @param onDismiss Callback for when the menu is dismissed.
     */
    @Composable
    public fun MessageReactionPickerCenterContent(
        modifier: Modifier,
        message: Message,
        onMessageAction: (MessageAction) -> Unit,
        reactionTypes: Map<String, ReactionIcon>,
        onDismiss: () -> Unit,
    ) {
        ExtendedReactionsMenuOptions(
            modifier = modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            message = message,
            reactionTypes = reactionTypes,
            onMessageAction = onMessageAction,
        )
    }

    // REGION: Generic menu items
    /**
     * Factory method for creating a generic menu option item.
     *
     * @param modifier The modifier for the menu item.
     * @param title The title of the menu item.
     * @param titleColor The color of the title.
     * @param leadingIcon The leading icon of the menu item.
     * @param onClick Callback for when the menu item is clicked.
     */
    @Composable
    public fun MenuOptionItem(
        modifier: Modifier,
        onClick: () -> Unit,
        leadingIcon: @Composable RowScope.() -> Unit,
        title: String,
        titleColor: Color,
        style: TextStyle,
        itemHeight: Dp,
        verticalAlignment: Alignment.Vertical,
        horizontalArrangement: Arrangement.Horizontal,
    ) {
        io.getstream.chat.android.compose.ui.components.common.MenuOptionItem(
            modifier = modifier,
            onClick = onClick,
            leadingIcon = leadingIcon,
            title = title,
            titleColor = titleColor,
            style = style,
            itemHeight = itemHeight,
            verticalAlignment = verticalAlignment,
            horizontalArrangement = horizontalArrangement,
        )
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
     * @param currentUser The currently logged-in user.
     */
    @Composable
    public fun ThreadListItemLatestReplyContent(thread: Thread, currentUser: User?) {
        ThreadItemLatestReplyContent(thread, currentUser)
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

    /**
     * The default content of the pinned message list item.
     *
     * @param message The pinned message to display.
     * @param currentUser The current user.
     * @param onClick Action invoked when the user clicks on the pinned message.
     */
    @Composable
    public fun PinnedMessageListItem(
        message: Message,
        currentUser: User?,
        onClick: (Message) -> Unit,
    ) {
        PinnedMessageItem(message, currentUser, onClick)
    }

    /**
     * The default loading content of the pinned message list. Shows an avatar of the user who sent the pinned message.
     *
     * Used in the [PinnedMessageListItem].
     *
     * @param message The pinned message to display.
     * @param currentUser The currently logged-in user.
     */
    @Composable
    public fun RowScope.PinnedMessageListItemLeadingContent(message: Message, currentUser: User?) {
        DefaultMessagePreviewItemLeadingContent(message, currentUser)
    }

    /**
     * The default loading content of the pinned message list. Shows the message sender name and the message content.
     *
     * Used in the [PinnedMessageListItem].
     *
     * @param message The pinned message to display.
     * @param currentUser The current user.
     */
    @Composable
    public fun RowScope.PinnedMessageListItemCenterContent(
        message: Message,
        currentUser: User?,
    ) {
        DefaultMessagePreviewItemCenterContent(message, currentUser)
    }

    /**
     * The default loading content of the pinned message list. Shows the message timestamp.
     *
     * Used in the [PinnedMessageListItem].
     *
     * @param message The pinned message to display.
     */
    @Composable
    public fun RowScope.PinnedMessageListItemTrailingContent(message: Message) {
        DefaultMessagePreviewItemTrailingContent(message)
    }

    /**
     * The default divider appended after each pinned message.
     */
    @Composable
    public fun PinnedMessageListItemDivider() {
        DefaultPinnedMessageListItemDivider()
    }

    /**
     * The default empty placeholder that is displayed when there are no pinned messages.
     *
     * @param modifier Modifier for styling.
     */
    @Composable
    public fun PinnedMessageListEmptyContent(modifier: Modifier) {
        DefaultPinnedMessageListEmptyContent(modifier)
    }

    /**
     * The default loading content that is displayed during the initial loading of the pinned messages.
     *
     * @param modifier Modifier for styling.
     */
    @Composable
    public fun PinnedMessageListLoadingContent(modifier: Modifier) {
        DefaultPinnedMessageListLoadingContent(modifier)
    }

    /**
     * The default content shown on the bottom of the list during the loading of more pinned messages.
     */
    @Composable
    public fun PinnedMessageListLoadingMoreContent() {
        DefaultPinnedMessageListLoadingMoreContent()
    }

    /**
     * The default 'Send' button in the attachments picker.
     * Shown as ">" icon in the attachments picker header, enabled when there is at least one selected attachment.
     *
     * @param hasPickedAttachments If there are any attachments picked.
     * @param onClick The click handler for the button.
     */
    @Composable
    public fun AttachmentsPickerSendButton(
        hasPickedAttachments: Boolean,
        onClick: () -> Unit,
    ) {
        DefaultAttachmentsPickerSendButton(
            hasPickedAttachments = hasPickedAttachments,
            onClick = onClick,
        )
    }
}
