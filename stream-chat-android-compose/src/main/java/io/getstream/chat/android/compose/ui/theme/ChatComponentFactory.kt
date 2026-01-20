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

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Warning
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.extensions.getCreatedAtOrThrow
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.channels.list.ChannelOptionState
import io.getstream.chat.android.compose.state.channels.list.ItemState
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResult
import io.getstream.chat.android.compose.state.messageoptions.MessageOptionItemState
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.state.reactionoptions.ReactionOptionItemState
import io.getstream.chat.android.compose.state.userreactions.UserReactionItemState
import io.getstream.chat.android.compose.ui.attachments.preview.handler.AttachmentPreviewHandler
import io.getstream.chat.android.compose.ui.channel.info.ChannelInfoNavigationIcon
import io.getstream.chat.android.compose.ui.channels.header.DefaultChannelHeaderLeadingContent
import io.getstream.chat.android.compose.ui.channels.header.DefaultChannelListHeaderCenterContent
import io.getstream.chat.android.compose.ui.channels.header.DefaultChannelListHeaderTrailingContent
import io.getstream.chat.android.compose.ui.channels.info.DefaultSelectedChannelMenuHeaderContent
import io.getstream.chat.android.compose.ui.channels.info.SelectedChannelMenu
import io.getstream.chat.android.compose.ui.channels.list.ChannelItem
import io.getstream.chat.android.compose.ui.channels.list.DefaultChannelItemCenterContent
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
import io.getstream.chat.android.compose.ui.components.DefaultSearchClearButton
import io.getstream.chat.android.compose.ui.components.DefaultSearchLabel
import io.getstream.chat.android.compose.ui.components.DefaultSearchLeadingIcon
import io.getstream.chat.android.compose.ui.components.EmptyContent
import io.getstream.chat.android.compose.ui.components.LoadingFooter
import io.getstream.chat.android.compose.ui.components.LoadingIndicator
import io.getstream.chat.android.compose.ui.components.NetworkLoadingIndicator
import io.getstream.chat.android.compose.ui.components.SearchInput
import io.getstream.chat.android.compose.ui.components.StreamHorizontalDivider
import io.getstream.chat.android.compose.ui.components.avatar.InitialsAvatar
import io.getstream.chat.android.compose.ui.components.channels.ChannelOptions
import io.getstream.chat.android.compose.ui.components.channels.MessageReadStatusIcon
import io.getstream.chat.android.compose.ui.components.channels.UnreadCountIndicator
import io.getstream.chat.android.compose.ui.components.composer.ComposerLinkPreview
import io.getstream.chat.android.compose.ui.components.composer.CoolDownIndicator
import io.getstream.chat.android.compose.ui.components.composer.MessageInputOptions
import io.getstream.chat.android.compose.ui.components.messageoptions.MessageOptions
import io.getstream.chat.android.compose.ui.components.messages.DefaultMessageContent
import io.getstream.chat.android.compose.ui.components.messages.DefaultMessageDeletedContent
import io.getstream.chat.android.compose.ui.components.messages.DefaultMessageGiphyContent
import io.getstream.chat.android.compose.ui.components.messages.MessageComposerQuotedMessage
import io.getstream.chat.android.compose.ui.components.messages.MessageFooter
import io.getstream.chat.android.compose.ui.components.messages.MessageReactionItem
import io.getstream.chat.android.compose.ui.components.messages.MessageReactions
import io.getstream.chat.android.compose.ui.components.messages.MessageText
import io.getstream.chat.android.compose.ui.components.messages.MessageThreadFooter
import io.getstream.chat.android.compose.ui.components.messages.OwnedMessageVisibilityContent
import io.getstream.chat.android.compose.ui.components.messages.QuotedMessage
import io.getstream.chat.android.compose.ui.components.messages.ScrollToBottomButton
import io.getstream.chat.android.compose.ui.components.messages.UploadingFooter
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
import io.getstream.chat.android.compose.ui.messages.composer.actions.AudioRecordingActions
import io.getstream.chat.android.compose.ui.messages.composer.internal.CommandsButton
import io.getstream.chat.android.compose.ui.messages.composer.internal.DefaultAudioRecordButton
import io.getstream.chat.android.compose.ui.messages.composer.internal.DefaultComposerLabel
import io.getstream.chat.android.compose.ui.messages.composer.internal.DefaultMessageComposerFooterInThreadMode
import io.getstream.chat.android.compose.ui.messages.composer.internal.DefaultMessageComposerHeaderContent
import io.getstream.chat.android.compose.ui.messages.composer.internal.DefaultMessageComposerInput
import io.getstream.chat.android.compose.ui.messages.composer.internal.DefaultMessageComposerInputTrailingContent
import io.getstream.chat.android.compose.ui.messages.composer.internal.DefaultMessageComposerLeadingContent
import io.getstream.chat.android.compose.ui.messages.composer.internal.DefaultMessageComposerRecordingContent
import io.getstream.chat.android.compose.ui.messages.composer.internal.SendButton
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
import io.getstream.chat.android.compose.ui.pinned.DefaultPinnedMessageListLoadingContent
import io.getstream.chat.android.compose.ui.pinned.DefaultPinnedMessageListLoadingMoreContent
import io.getstream.chat.android.compose.ui.pinned.PinnedMessageItem
import io.getstream.chat.android.compose.ui.theme.animation.FadingVisibility
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
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.LinkPreview
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.ReactionSorting
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoMemberViewAction
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoMemberViewEvent
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewAction
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewEvent
import io.getstream.chat.android.ui.common.feature.messages.translations.MessageOriginalTranslationsStore
import io.getstream.chat.android.ui.common.model.MessageResult
import io.getstream.chat.android.ui.common.state.channel.attachments.ChannelAttachmentsViewState
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoMemberViewState
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoViewState
import io.getstream.chat.android.ui.common.state.channels.actions.ChannelAction
import io.getstream.chat.android.ui.common.state.messages.MessageAction
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.React
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState
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
import io.getstream.chat.android.compose.ui.channel.attachments.ChannelFilesAttachmentsItem as DefaultChannelFilesAttachmentsItem
import io.getstream.chat.android.compose.ui.channel.attachments.ChannelMediaAttachmentsItem as DefaultChannelMediaAttachmentsItem
import io.getstream.chat.android.compose.ui.channel.info.ChannelInfoOptionItem as DefaultChannelInfoOptionItem
import io.getstream.chat.android.ui.common.R as UiCommonR

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
            modifier = Modifier.animateItem(),
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
        StreamHorizontalDivider()
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
     * Usually information about the last message such as its read state, timestamp, and the number of unread messages.
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
     * The default unread count indicator in the channel item.
     */
    @Composable
    public fun ChannelItemUnreadCountIndicator(
        unreadCount: Int,
        modifier: Modifier,
    ) {
        UnreadCountIndicator(
            unreadCount = unreadCount,
            modifier = modifier,
        )
    }

    /**
     * The default read status indicator in the channel item, weather the last message is sent, pending or read.
     */
    @Composable
    public fun ChannelItemReadStatusIndicator(
        channel: Channel,
        message: Message,
        currentUser: User?,
        modifier: Modifier,
    ) {
        MessageReadStatusIcon(
            channel = channel,
            message = message,
            currentUser = currentUser,
            modifier = modifier,
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
     * The default clear button of the search input.
     *
     * Used by [ChannelListSearchInput].
     */
    @Composable
    public fun SearchInputClearButton(onClick: () -> Unit) {
        DefaultSearchClearButton(onClick = onClick)
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
        onChannelAvatarClick: (() -> Unit)?,
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
    @Deprecated(
        message = "Use the new version of MessageListHeaderCenterContent with a nullable onClick.",
        replaceWith = ReplaceWith(
            "MessageListHeaderCenterContent(\n" +
                "modifier = modifier,\n" +
                "channel = channel,\n" +
                "currentUser = currentUser,\n" +
                "connectionState = connectionState,\n" +
                "typingUsers = typingUsers,\n" +
                "messageMode = messageMode,\n" +
                "onClick = onHeaderTitleClick,\n" +
                ")",
        ),
        level = DeprecationLevel.WARNING,
    )
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
        MessageListHeaderCenterContent(
            modifier = modifier,
            channel = channel,
            currentUser = currentUser,
            connectionState = connectionState,
            typingUsers = typingUsers,
            messageMode = messageMode,
            onClick = onHeaderTitleClick,
        )
    }

    /**
     * The default center content of the message list header.
     * Usually shows the channel title in the top and
     * the channel information or the connection status in the bottom.
     */
    @Composable
    public fun RowScope.MessageListHeaderCenterContent(
        modifier: Modifier,
        channel: Channel,
        currentUser: User?,
        connectionState: ConnectionState,
        typingUsers: List<User>,
        messageMode: MessageMode,
        onClick: ((Channel) -> Unit)?,
    ) {
        DefaultMessageListHeaderCenterContent(
            modifier = modifier,
            channel = channel,
            currentUser = currentUser,
            connectionState = connectionState,
            typingUsers = typingUsers,
            messageMode = messageMode,
            onHeaderTitleClick = onClick,
        )
    }

    /**
     * The default trailing content of the message list header, which is the channel avatar.
     */
    @Composable
    public fun RowScope.MessageListHeaderTrailingContent(
        channel: Channel,
        currentUser: User?,
        onClick: (() -> Unit)?,
    ) {
        DefaultMessageListHeaderTrailingContent(
            channel = channel,
            currentUser = currentUser,
            onClick = onClick,
        )
    }

    /**
     * The default background of the message list.
     */
    @Composable
    public fun MessageListBackground() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ChatTheme.colors.appBackground),
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
     * It handles the scroll-to-bottom and scroll-to-focused message features.
     */
    @Composable
    public fun BoxScope.MessageListHelperContent(
        messageListState: MessageListState,
        messagesLazyListState: MessagesLazyListState,
        contentPadding: PaddingValues,
        onScrollToBottomClick: (() -> Unit) -> Unit,
    ) {
        DefaultMessagesHelperContent(
            messagesState = messageListState,
            messagesLazyListState = messagesLazyListState,
            contentPadding = contentPadding,
            scrollToBottom = onScrollToBottomClick,
        )
    }

    /**
     * The default scroll-to-bottom button shown when the user scrolls away from the bottom of the list.
     */
    @Composable
    public fun ScrollToBottomButton(
        modifier: Modifier,
        visible: Boolean,
        count: Int,
        onClick: () -> Unit,
    ) {
        // Disable animations in snapshot tests, at least until Paparazzi has a better support for animations.
        // This is due to the scroll to bottom tests, where the items are not visible in the snapshots.
        if (LocalInspectionMode.current) {
            if (visible) {
                ScrollToBottomButton(
                    modifier = modifier,
                    count = count,
                    onClick = onClick,
                )
            }
        } else {
            FadingVisibility(
                modifier = modifier,
                visible = visible,
            ) {
                ScrollToBottomButton(
                    count = count,
                    onClick = onClick,
                )
            }
        }
    }

    /**
     * The default message list item container, which renders each [MessageListItemState]'s subtype.
     */
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
        onReply: (Message) -> Unit,
    ) {
        DefaultMessageContainer(
            messageListItemState = messageListItem,
            reactionSorting = reactionSorting,
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
            onReply = onReply,
        )
    }

    /**
     * The default message list item modifier for styling.
     */
    @Composable
    public fun LazyItemScope.messageListItemModifier(): Modifier =
        // Disable animations in snapshot tests, at least until Paparazzi has a better support for animations.
        // This is due to the scroll to bottom tests, where the items are not visible in the snapshots.
        if (LocalInspectionMode.current) {
            Modifier
        } else {
            Modifier.animateItem()
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
        onReply: (Message) -> Unit,
    ) {
        DefaultMessageItem(
            messageItem = messageItem,
            reactionSorting = reactionSorting,
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
            onUserAvatarClick = onUserAvatarClick?.let { { it.invoke(messageItem.message.user) } },
            onLinkClick = onMessageLinkClick,
            onUserMentionClick = onUserMentionClick,
            onAddAnswer = onAddAnswer,
            onReply = onReply,
        )
    }

    /**
     * The default appearance of the message bubble.
     *
     * @param modifier Prepared [Modifier] for styling.
     * @param message The [Message] to be rendered inside the bubble.
     * @param color The color of the message bubble.
     * @param shape The shape of the message bubble.
     * @param border The border of the message bubble.
     * @param contentPadding The padding of the message bubble.
     * @param content The content shown inside the message bubble.
     */
    @Composable
    public fun MessageBubble(
        modifier: Modifier,
        message: Message,
        color: Color,
        shape: Shape,
        border: BorderStroke?,
        contentPadding: PaddingValues,
        content: @Composable () -> Unit,
    ) {
        io.getstream.chat.android.compose.ui.components.messages.MessageBubble(
            modifier = modifier,
            color = color,
            shape = shape,
            border = border,
            contentPadding = contentPadding,
            content = content,
        )
    }

    /**
     * Icon shown at the bottom-end of a [Message] when it failed to send.
     *
     * @param modifier Prepared [Modifier] for styling.
     * @param message The [Message] that failed to send.
     */
    @Composable
    public fun MessageFailedIcon(
        modifier: Modifier,
        message: Message,
    ) {
        Icon(
            modifier = modifier,
            painter = painterResource(id = R.drawable.stream_compose_ic_error),
            contentDescription = null,
            tint = ChatTheme.colors.errorAccent,
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
        DefaultMessageItemFooterContent(messageItem = messageItem)
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
     * The default list of reactions displayed above the message bubble and is part of [MessageItemHeaderContent].
     */
    @Composable
    public fun MessageReactionList(
        params: MessageReactionListParams,
    ) {
        MessageReactions(
            modifier = params.modifier
                .minimumInteractiveComponentSize()
                .clip(shape = RoundedCornerShape(16.dp))
                .run {
                    params.onClick?.let { onClick ->
                        clickable { onClick(params.message) }
                    } ?: this
                }
                .padding(horizontal = 4.dp, vertical = 2.dp),
            options = params.reactions,
        )
    }

    /**
     * The default individual reaction item shown inside [MessageReactionList].
     */
    @Composable
    public fun RowScope.MessageReactionItem(
        params: MessageReactionItemParams,
    ) {
        MessageReactionItem(
            modifier = params.modifier
                .semantics {
                    testTag = "Stream_MessageReaction_${params.state.type}"
                    contentDescription = params.state.type
                }
                .size(20.dp)
                .padding(2.dp)
                .align(Alignment.CenterVertically),
            option = params.state,
        )
    }

    /**
     * The default Giphy message content.
     */
    @Composable
    public fun MessageGiphyContent(
        message: Message,
        currentUser: User?,
        onGiphyActionClick: (GiphyAction) -> Unit,
    ) {
        DefaultMessageGiphyContent(
            message = message,
            currentUser = currentUser,
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
        MessageFooter(
            messageItem = messageItem,
            onToggleOriginalText = {
                // Important: This is a workaround to avoid a breaking change in the ChatComponentFactory API.
                // In the next major version, this callback should be passed as a parameter to the MessageFooterContent.
                val translationsStore = MessageOriginalTranslationsStore.forChannel(messageItem.message.cid)
                translationsStore.toggleOriginalText(messageItem.message.id)
            },
        )
    }

    /**
     * The default read status indicator in the message footer, weather the message is sent, pending or read.
     */
    @Deprecated(
        message = "Use the new version of MessageFooterStatusIndicator that takes MessageFooterStatusIndicatorParams.",
        replaceWith = ReplaceWith(
            "MessageFooterStatusIndicator(\n" +
                "    params = MessageFooterStatusIndicatorParams(\n" +
                "        modifier = modifier,\n" +
                "        messageItem = messageItem,\n" +
                "    ),\n" +
                ")",
        ),
        level = DeprecationLevel.WARNING,
    )
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

    @Composable
    public fun MessageFooterStatusIndicator(
        params: MessageFooterStatusIndicatorParams,
    ) {
        if (params.messageItem.isMessageDelivered) {
            MessageReadStatusIcon(
                modifier = params.modifier,
                message = params.messageItem.message,
                isMessageRead = params.messageItem.isMessageRead,
                isMessageDelivered = params.messageItem.isMessageDelivered,
                readCount = params.messageItem.messageReadBy.size,
            )
        } else {
            MessageFooterStatusIndicator(
                modifier = params.modifier,
                message = params.messageItem.message,
                isMessageRead = params.messageItem.isMessageRead,
                readCount = params.messageItem.messageReadBy.size,
            )
        }
    }

    /**
     * The default message composer that contains
     * the message input, attachments, commands, recording actions, integrations, and the send button.
     */
    @Composable
    public fun MessageComposer(
        messageComposerState: MessageComposerState,
        onSendMessage: (String, List<Attachment>) -> Unit,
        modifier: Modifier,
        onAttachmentsClick: () -> Unit,
        onCommandsClick: () -> Unit,
        onValueChange: (String) -> Unit,
        onAttachmentRemoved: (Attachment) -> Unit,
        onCancelAction: () -> Unit,
        onLinkPreviewClick: ((LinkPreview) -> Unit)?,
        onMentionSelected: (User) -> Unit,
        onCommandSelected: (Command) -> Unit,
        onAlsoSendToChannelSelected: (Boolean) -> Unit,
        recordingActions: AudioRecordingActions,
        headerContent: @Composable ColumnScope.(MessageComposerState) -> Unit,
        footerContent: @Composable ColumnScope.(MessageComposerState) -> Unit,
        mentionPopupContent: @Composable (List<User>) -> Unit,
        commandPopupContent: @Composable (List<Command>) -> Unit,
        leadingContent: @Composable RowScope.(MessageComposerState) -> Unit,
        label: @Composable (MessageComposerState) -> Unit,
        input: @Composable RowScope.(MessageComposerState) -> Unit,
        audioRecordingContent: @Composable RowScope.(MessageComposerState) -> Unit,
        trailingContent: @Composable (MessageComposerState) -> Unit,
    ) {
        io.getstream.chat.android.compose.ui.messages.composer.MessageComposer(
            messageComposerState = messageComposerState,
            onSendMessage = onSendMessage,
            modifier = modifier,
            onAttachmentsClick = onAttachmentsClick,
            onCommandsClick = onCommandsClick,
            onValueChange = onValueChange,
            onAttachmentRemoved = onAttachmentRemoved,
            onCancelAction = onCancelAction,
            onLinkPreviewClick = onLinkPreviewClick,
            onMentionSelected = onMentionSelected,
            onCommandSelected = onCommandSelected,
            onAlsoSendToChannelSelected = onAlsoSendToChannelSelected,
            recordingActions = recordingActions,
            headerContent = headerContent,
            footerContent = footerContent,
            mentionPopupContent = mentionPopupContent,
            commandPopupContent = commandPopupContent,
            leadingContent = leadingContent,
            label = label,
            input = input,
            audioRecordingContent = audioRecordingContent,
            trailingContent = trailingContent,
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
        Column(modifier = Modifier.animateContentSize()) {
            DefaultMessageComposerHeaderContent(state, onCancel, onLinkPreviewClick)
        }
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
     * @param onContentClick The handler called when the content is clicked.
     * @param onCancelClick The handler called when the cancel button is clicked.
     */
    @Composable
    public fun MessageComposerLinkPreview(
        modifier: Modifier,
        linkPreview: LinkPreview,
        onContentClick: ((LinkPreview) -> Unit)?,
        onCancelClick: (() -> Unit)?,
    ) {
        ComposerLinkPreview(
            modifier = modifier,
            linkPreview = linkPreview,
            onContentClick = onContentClick,
            onCancelClick = onCancelClick,
        )
    }

    /**
     * The default footer content of the message composer.
     * When replying to a thread, it provides the checkbox to also send the message to the channel.
     *
     * @param state The current state of the message composer.
     * @param onAlsoSendToChannelSelected The action to perform when the "Also send to channel" checkbox is selected.
     */
    @Composable
    public fun ColumnScope.MessageComposerFooterContent(
        state: MessageComposerState,
        onAlsoSendToChannelSelected: (Boolean) -> Unit,
    ) {
        Box(modifier = Modifier.animateContentSize()) {
            when (state.messageMode) {
                is MessageMode.Normal -> {
                    // no footer in normal mode
                }

                is MessageMode.MessageThread -> {
                    DefaultMessageComposerFooterInThreadMode(
                        alsoSendToChannel = state.alsoSendToChannel,
                        onAlsoSendToChannelChanged = onAlsoSendToChannelSelected,
                    )
                }
            }
        }
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
        DefaultCommandSuggestionItemLeadingContent(command)
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
     * The default leading content of the message composer, which includes an add attachment button by default.
     *
     * @param state The current state of the message composer.
     * @param onAttachmentsClick The action to perform when the attachments button is clicked.
     */
    @Composable
    public fun RowScope.MessageComposerLeadingContent(
        state: MessageComposerState,
        onAttachmentsClick: () -> Unit,
    ) {
        DefaultMessageComposerLeadingContent(
            messageInputState = state,
            onAttachmentsClick = onAttachmentsClick,
        )
    }

    /**
     * The default commands button of the message composer.
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
        DefaultComposerLabel(state)
    }

    /**
     * The default input of the message composer.
     *
     * @param state The current state of the message composer.
     * @param onInputChanged The action to perform when the input is changed.
     * @param onAttachmentRemoved The action to perform when an attachment is removed.
     * @param onLinkPreviewClick The action to perform when a link preview is clicked.
     * @param onCancelLinkPreviewClick The action to perform when the link preview cancel button is clicked.
     * @param label The label of the message composer.
     * @param onSendClick The action to perform when the send button is clicked.
     * @param recordingActions The actions to control the audio recording.
     * @param leadingContent The leading content of the message composer.
     * @param trailingContent The trailing content of the message composer.     *
     */
    @Composable
    public fun RowScope.MessageComposerInput(
        state: MessageComposerState,
        onInputChanged: (String) -> Unit,
        onAttachmentRemoved: (Attachment) -> Unit,
        onCancel: () -> Unit,
        onLinkPreviewClick: ((LinkPreview) -> Unit)?,
        onCancelLinkPreviewClick: (() -> Unit)?,
        label: @Composable (MessageComposerState) -> Unit,
        onSendClick: (String, List<Attachment>) -> Unit,
        recordingActions: AudioRecordingActions,
        leadingContent: @Composable RowScope.() -> Unit,
        trailingContent: @Composable RowScope.() -> Unit,
    ) {
        DefaultMessageComposerInput(
            messageComposerState = state,
            onValueChange = onInputChanged,
            onAttachmentRemoved = onAttachmentRemoved,
            onCancelAction = onCancel,
            onLinkPreviewClick = onLinkPreviewClick,
            onCancelLinkPreviewClick = onCancelLinkPreviewClick,
            label = label,
            onSendClick = onSendClick,
            recordingActions = recordingActions,
            leadingContent = leadingContent,
            trailingContent = trailingContent,
        )
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
        onCancelClick: () -> Unit,
    ) {
        MessageComposerQuotedMessage(
            modifier = modifier,
            message = quotedMessage,
            currentUser = state.currentUser,
            onCancelClick = onCancelClick,
        )
    }

    /**
     * The default leading content of the message composer.
     * Shown at the start of the composer input.
     *
     * Used as part of [MessageComposerInput].
     *
     * @param state The current state of the message composer.
     */
    @Composable
    public fun MessageComposerInputLeadingContent(
        state: MessageComposerState,
    ) {
    }

    /**
     * The default trailing content of the message composer.
     * Shown at the end of the composer input.
     *
     * Used as part of [MessageComposerInput].
     *
     * @param state The current state of the message composer.
     * @param onSendClick The action to perform when the send button is clicked.
     * @param recordingActions The actions to control the audio recording.
     */
    @Composable
    public fun MessageComposerInputTrailingContent(
        state: MessageComposerState,
        onSendClick: (String, List<Attachment>) -> Unit,
        recordingActions: AudioRecordingActions,
    ) {
        DefaultMessageComposerInputTrailingContent(
            messageComposerState = state,
            onSendMessage = onSendClick,
            recordingActions = recordingActions,
        )
    }

    /**
     * The default trailing content of the message composer.
     * Shown after the composer input.
     *
     * Used as part of [MessageComposer].
     *
     * @param state The current state of the message composer.
     */
    @Composable
    public fun MessageComposerTrailingContent(
        state: MessageComposerState,
    ) {
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
     * @param onClick The action to perform when the button is clicked.
     */
    @Composable
    public fun MessageComposerSendButton(
        onClick: () -> Unit,
    ) {
        SendButton(onClick = onClick)
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
    @Deprecated(
        message = "Use the new Avatar function instead.",
        level = DeprecationLevel.WARNING,
        replaceWith = ReplaceWith(
            expression = "Avatar(\n" +
                "modifier = modifier,\n " +
                "imageUrl = imageUrl,\n " +
                "initials = initials,\n " +
                "shape = shape,\n " +
                "textStyle = textStyle,\n " +
                "placeholderPainter = placeholderPainter,\n " +
                "errorPlaceholderPainter = errorPlaceholderPainter,\n " +
                "contentDescription = contentDescription,\n " +
                "initialsAvatarOffset = initialsAvatarOffset,\n " +
                "onClick = onClick,\n" +
                ")",
        ),
    )
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
        Avatar(
            modifier = modifier,
            imageUrl = imageUrl,
            initials = initials,
            shape = shape,
            textStyle = textStyle,
            placeholderPainter = placeholderPainter,
            errorPlaceholderPainter = null,
            contentDescription = contentDescription,
            initialsAvatarOffset = initialsAvatarOffset,
            onClick = onClick,
        )
    }

    /**
     * The default avatar, which renders an image from the provided image URL.
     * In case the image URL is empty or there is an error loading the image,
     * it falls back to an image with initials.
     */
    @Composable
    public fun Avatar(
        modifier: Modifier,
        imageUrl: String,
        initials: String,
        shape: Shape,
        textStyle: TextStyle,
        placeholderPainter: Painter?,
        errorPlaceholderPainter: Painter?,
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
            errorPlaceholderPainter = errorPlaceholderPainter,
            contentDescription = contentDescription,
            initialsAvatarOffset = initialsAvatarOffset,
            onClick = onClick,
        )
    }

    /**
     * The default fallback avatar, which renders initials in a circle.
     * It is used when the image URL is empty or there is an error loading the image.
     */
    @Composable
    public fun FallbackAvatar(
        imageUrl: String,
        initials: String,
        modifier: Modifier,
        shape: Shape,
        textStyle: TextStyle,
        avatarOffset: DpOffset,
        onClick: (() -> Unit)?,
    ) {
        InitialsAvatar(
            modifier = modifier,
            initials = initials,
            shape = shape,
            textStyle = textStyle,
            onClick = onClick,
            avatarOffset = avatarOffset,
        )
    }

    /**
     * The default user avatar content.
     * It renders the [User] avatar that's shown on the messages screen or in headers of direct messages.
     * If [showOnlineIndicator] is `true` and the user is online, it uses [Avatar] to shows an image or their initials.
     */
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
                        reaction = Reaction(messageId = message.id, type = it.type, emojiCode = it.emojiCode),
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
                .clickable(bounded = false) {
                    onReactionOptionSelected(option)
                },
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
            modifier = modifier.clickable(bounded = false) {
                onShowMoreReactionsSelected()
            },
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
                        reaction = Reaction(messageId = message.id, type = it.type, emojiCode = it.emojiCode),
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
        StreamHorizontalDivider()
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

    /**
     * The default content shown when swiping to reply to a message.
     */
    @Composable
    public fun RowScope.SwipeToReplyContent() {
        Box {
            Icon(
                painter = painterResource(id = R.drawable.stream_compose_ic_reply),
                contentDescription = "",
                tint = ChatTheme.colors.textLowEmphasis,
            )
        }
    }

    /**
     * The default content of a mention list item.
     */
    @Composable
    public fun LazyItemScope.MentionListItem(
        mention: MessageResult,
        modifier: Modifier,
        currentUser: User?,
        onClick: ((message: Message) -> Unit)?,
    ) {
        SearchResultItem(
            searchResultItemState = remember {
                ItemState.SearchResultItemState(
                    message = mention.message,
                    channel = mention.channel,
                )
            },
            currentUser = currentUser,
            modifier = modifier.animateItem(),
            onSearchResultClick = onClick,
        )
    }

    /**
     * The default loading indicator that is displayed during the initial loading of the mention list.
     *
     * @param modifier Modifier for styling.
     */
    @Composable
    public fun BoxScope.MentionListLoadingIndicator(
        modifier: Modifier,
    ) {
        LoadingIndicator(
            modifier = modifier,
        )
    }

    /**
     * The default empty placeholder that is displayed when the mention list is empty.
     *
     * @param modifier Modifier for styling.
     */
    @Composable
    public fun BoxScope.MentionListEmptyContent(modifier: Modifier) {
        EmptyContent(
            text = stringResource(UiCommonR.string.stream_ui_mention_list_empty),
            painter = painterResource(UiCommonR.drawable.stream_compose_ic_mentions),
            modifier = modifier,
        )
    }

    /**
     * The default loading indicator that is displayed on the bottom of the list when there are more mentions loading.
     */
    @Composable
    public fun LazyItemScope.MentionListLoadingItem(modifier: Modifier) {
        LoadingFooter(
            modifier = modifier.fillMaxWidth(),
        )
    }

    /**
     * The default pull-to-refresh indicator for the mention list.
     *
     * @param modifier Modifier for styling.
     * @param pullToRefreshState The state of the pull-to-refresh.
     * @param isRefreshing Whether the mention list is currently refreshing.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    public fun BoxScope.MentionListPullToRefreshIndicator(
        modifier: Modifier,
        pullToRefreshState: PullToRefreshState,
        isRefreshing: Boolean,
    ) {
        PullToRefreshDefaults.Indicator(
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            modifier = modifier.align(Alignment.TopCenter),
            containerColor = ChatTheme.colors.barsBackground,
            color = ChatTheme.colors.primaryAccent,
        )
    }

    /**
     * Factory method for creating the preview content of file attachments.
     *
     * @param modifier Modifier for styling.
     * @param attachments List of file attachments to preview.
     * @param onAttachmentRemoved Lambda invoked when an attachment is removed.
     */
    @Composable
    public fun FileAttachmentPreviewContent(
        modifier: Modifier,
        attachments: List<Attachment>,
        onAttachmentRemoved: (Attachment) -> Unit,
    ) {
        io.getstream.chat.android.compose.ui.attachments.content.FileAttachmentPreviewContent(
            modifier = modifier,
            attachments = attachments,
            onAttachmentRemoved = onAttachmentRemoved,
        )
    }

    /**
     * Factory method for creating the content of a file attachment.
     *
     * @param modifier Modifier for styling the composable.
     * @param attachmentState The state of the attachment, containing information about the file.
     * @param showFileSize A lambda function that determines whether the file size should be displayed.
     * @param onItemClick A lambda function invoked when the file attachment is clicked, providing a list of
     * [AttachmentPreviewHandler] and the clicked [Attachment].
     */
    @Composable
    public fun FileAttachmentContent(
        modifier: Modifier,
        attachmentState: AttachmentState,
        showFileSize: (Attachment) -> Boolean,
        onItemClick: (List<AttachmentPreviewHandler>, Attachment) -> Unit,
    ) {
        io.getstream.chat.android.compose.ui.attachments.content.FileAttachmentContent(
            modifier = modifier,
            attachmentState = attachmentState,
            showFileSize = showFileSize,
            onItemClick = onItemClick,
        )
    }

    /**
     * Factory method for creating a file attachment item.
     *
     * @param modifier Modifier for styling.
     * @param attachment The file attachment to show.
     * @param isMine Whether the message is sent by the current user or not.
     * @param showFileSize Whether to show the file size or not.
     */
    @Composable
    public fun FileAttachmentItem(
        modifier: Modifier,
        attachment: Attachment,
        isMine: Boolean,
        showFileSize: (Attachment) -> Boolean,
    ) {
        io.getstream.chat.android.compose.ui.attachments.content.FileAttachmentItem(
            modifier = modifier,
            attachment = attachment,
            isMine = isMine,
            showFileSize = showFileSize,
        )
    }

    /**
     * The default file upload content used for displaying uploading attachments.
     *
     * @param attachmentState The state of the attachment.
     * @param modifier Modifier for styling.
     * @param onItemClick Lambda called when an item gets clicked.
     */
    @Composable
    public fun FileUploadContent(
        attachmentState: AttachmentState,
        modifier: Modifier,
        onItemClick: (Attachment, List<AttachmentPreviewHandler>) -> Unit,
    ) {
        io.getstream.chat.android.compose.ui.attachments.content.FileUploadContent(
            attachmentState = attachmentState,
            modifier = modifier,
            onItemClick = onItemClick,
        )
    }

    /**
     * Factory method for creating a file upload item that shows an uploading attachment with progress.
     *
     * @param attachment The attachment that's being uploaded.
     * @param modifier Modifier for styling.
     */
    @Composable
    public fun FileUploadItem(
        attachment: Attachment,
        modifier: Modifier,
    ) {
        io.getstream.chat.android.compose.ui.attachments.content.FileUploadItem(
            attachment = attachment,
            modifier = modifier,
        )
    }

    /**
     * Factory method for creating the top bar of the channel info screen.
     *
     * @param headerState The state of the channel header.
     * @param listState The state of the lazy list.
     * @param onNavigationIconClick Callback invoked when the navigation icon is clicked.
     */
    @Composable
    public fun DirectChannelInfoTopBar(
        headerState: ChannelHeaderViewState,
        listState: LazyListState,
        onNavigationIconClick: () -> Unit,
    ) {
        io.getstream.chat.android.compose.ui.channel.info.DirectChannelInfoTopBar(
            onNavigationIconClick = onNavigationIconClick,
        )
    }

    /**
     * Factory method for creating the avatar container in the direct channel info screen.
     *
     * @param user The user whose avatar is displayed.
     */
    @Composable
    public fun DirectChannelInfoAvatarContainer(user: User) {
        io.getstream.chat.android.compose.ui.channel.info.DirectChannelInfoAvatarContainer(
            user = user,
        )
    }

    /**
     * Factory method for creating the top bar of the group channel info screen.
     *
     * @param headerState The state of the channel header.
     * @param infoState The state of the channel info.
     * @param listState The state of the lazy list.
     * @param onNavigationIconClick Callback invoked when the navigation icon is clicked.
     * @param onAddMembersClick Callback invoked when the "Add members" button is clicked.
     */
    @Composable
    public fun GroupChannelInfoTopBar(
        headerState: ChannelHeaderViewState,
        infoState: ChannelInfoViewState,
        listState: LazyListState,
        onNavigationIconClick: () -> Unit,
        onAddMembersClick: () -> Unit,
    ) {
        io.getstream.chat.android.compose.ui.channel.info.GroupChannelInfoTopBar(
            headerState = headerState,
            infoState = infoState,
            listState = listState,
            onNavigationIconClick = onNavigationIconClick,
            onAddMembersClick = onAddMembersClick,
        )
    }

    /**
     * Factory method for creating the "Add members" button of the group channel info screen.
     *
     * @param onClick Callback invoked when button is clicked.
     */
    @Composable
    public fun GroupChannelInfoAddMembersButton(
        onClick: () -> Unit,
    ) {
        IconButton(onClick = onClick) {
            Icon(
                painter = painterResource(R.drawable.stream_ic_member_add),
                contentDescription = stringResource(R.string.stream_ui_channel_info_member_add_button),
            )
        }
    }

    /**
     * Factory method for creating the channel info separator item.
     * This is used to visually separate different sections in the channel info screens.
     */
    @Composable
    public fun LazyItemScope.ChannelInfoSeparatorItem() {
        StreamHorizontalDivider(thickness = 8.dp)
    }

    /**
     * Factory method for creating the channel info option item used in direct and group channel info screens.
     *
     * @param option The channel info option to display.
     * @param isGroupChannel Whether the channel is a group channel.
     * @param onViewAction Callback invoked when a view action is triggered.
     */
    @Composable
    public fun LazyItemScope.ChannelInfoOptionItem(
        option: ChannelInfoViewState.Content.Option,
        isGroupChannel: Boolean,
        onViewAction: (ChannelInfoViewAction) -> Unit,
    ) {
        DefaultChannelInfoOptionItem(
            option = option,
            isGroupChannel = isGroupChannel,
            onViewAction = onViewAction,
        )
    }

    /**
     * Factory method for creating the member item in the group channel info screen.
     *
     * @param currentUser The currently logged-in user.
     * @param member The member to display.
     * @param isOwner Whether the member is the owner of the channel.
     * @param onClick Callback invoked when the user clicks on the member item.
     */
    @Composable
    public fun LazyItemScope.GroupChannelInfoMemberItem(
        currentUser: User?,
        member: Member,
        isOwner: Boolean,
        onClick: (() -> Unit)?,
    ) {
        io.getstream.chat.android.compose.ui.channel.info.GroupChannelInfoMemberItem(
            modifier = Modifier.animateItem(),
            currentUser = currentUser,
            member = member,
            isOwner = isOwner,
            onClick = onClick,
        )
    }

    /**
     * Factory method for creating the expand members item in the group channel info screen.
     *
     * @param collapsedCount The number of members that are currently collapsed.
     * @param onClick Callback invoked when the user clicks to expand the member list.
     */
    @Composable
    public fun LazyItemScope.GroupChannelInfoExpandMembersItem(
        collapsedCount: Int,
        onClick: () -> Unit,
    ) {
        io.getstream.chat.android.compose.ui.channel.info.GroupChannelInfoExpandMembersItem(
            collapsedCount = collapsedCount,
            onClick = onClick,
        )
    }

    /**
     * Factory method for creating the channel info screen modal.
     *
     * @param modal Which modal to display.
     * @param isGroupChannel Whether the channel is a group channel.
     * @param onViewAction Callback invoked when a view action is triggered.
     * Applicable for all modals except [ChannelInfoViewEvent.MemberInfoModal].
     * @param onMemberViewEvent Callback invoked when a member view event is triggered.
     * Only applicable for [ChannelInfoViewEvent.MemberInfoModal].
     */
    @Composable
    public fun ChannelInfoScreenModal(
        modal: ChannelInfoViewEvent.Modal?,
        isGroupChannel: Boolean,
        onViewAction: (action: ChannelInfoViewAction) -> Unit,
        onMemberViewEvent: (event: ChannelInfoMemberViewEvent) -> Unit,
        onDismiss: () -> Unit,
    ) {
        io.getstream.chat.android.compose.ui.channel.info.ChannelInfoScreenModal(
            modal = modal,
            isGroupChannel = isGroupChannel,
            onViewAction = onViewAction,
            onMemberViewEvent = onMemberViewEvent,
            onDismiss = onDismiss,
        )
    }

    /**
     * Factory method for creating the top bar of the member info modal sheet in the group channel info screen.
     *
     * @param member The member to display in the top bar.
     */
    @Composable
    public fun ChannelInfoMemberInfoModalSheetTopBar(member: Member) {
        io.getstream.chat.android.compose.ui.channel.info.ChannelInfoMemberInfoModalSheetTopBar(
            member = member,
        )
    }

    /**
     * Factory method for creating the channel info member option item.
     *
     * @param option The channel info member option to display.
     * @param onViewAction Callback invoked when a view action is triggered.
     */
    @Composable
    public fun LazyItemScope.ChannelInfoMemberOptionItem(
        option: ChannelInfoMemberViewState.Content.Option,
        onViewAction: (action: ChannelInfoMemberViewAction) -> Unit,
    ) {
        io.getstream.chat.android.compose.ui.channel.info.ChannelInfoMemberOptionItem(
            option = option,
            onViewAction = onViewAction,
        )
    }

    /**
     * Factory method for creating the top bar of the channel files attachments screen.
     *
     * @param listState The state of the lazy list.
     * @param onNavigationIconClick Callback invoked when the navigation icon is clicked.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    public fun ChannelFilesAttachmentsTopBar(
        listState: LazyListState,
        onNavigationIconClick: () -> Unit,
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.stream_ui_channel_attachments_files_title),
                    style = ChatTheme.typography.title3Bold,
                    maxLines = 1,
                )
            },
            navigationIcon = { ChannelInfoNavigationIcon(onClick = onNavigationIconClick) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = ChatTheme.colors.barsBackground,
                titleContentColor = ChatTheme.colors.textHighEmphasis,
            ),
        )
    }

    /**
     * Factory method for creating the loading indicator of the channel files attachments screen.
     */
    @Composable
    public fun ChannelFilesAttachmentsLoadingIndicator(modifier: Modifier) {
        LoadingIndicator(modifier = modifier)
    }

    /**
     * Factory method for creating the empty content of the channel files attachments screen.
     */
    @Composable
    public fun BoxScope.ChannelFilesAttachmentsEmptyContent(modifier: Modifier) {
        EmptyContent(
            modifier = modifier,
            title = stringResource(UiCommonR.string.stream_ui_channel_attachments_files_empty_title),
            text = stringResource(UiCommonR.string.stream_ui_channel_attachments_files_empty_text),
            painter = painterResource(UiCommonR.drawable.stream_ic_files),
        )
    }

    @Composable
    public fun BoxScope.ChannelFilesAttachmentsErrorContent(modifier: Modifier) {
        EmptyContent(
            modifier = modifier,
            text = stringResource(UiCommonR.string.stream_ui_channel_attachments_files_load_error),
            painter = rememberVectorPainter(Icons.TwoTone.Warning),
        )
    }

    /**
     * Factory method for creating the channel files attachments header item.
     * This is typically used to display the title of a group of attachments.
     *
     * @param modifier The modifier for styling the header item.
     * @param label The label for the header item.
     */
    @Composable
    public fun LazyItemScope.ChannelFilesAttachmentsHeaderItem(
        modifier: Modifier,
        label: String,
    ) {
        Text(
            modifier = modifier
                .fillMaxWidth()
                .background(ChatTheme.colors.borders.copy(alpha = 0.8f))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            text = label,
            style = ChatTheme.typography.bodyBold,
            color = ChatTheme.colors.textHighEmphasis,
        )
    }

    /**
     * Factory method for creating the channel files attachments item.
     *
     * @param modifier The modifier for styling the item.
     * @param index The index of the item in the list.
     * @param item The channel file attachment item to display.
     * @param currentUser The currently logged-in user.
     * @param onClick Callback invoked when the item is clicked.
     */
    @Composable
    public fun LazyItemScope.ChannelFilesAttachmentsItem(
        modifier: Modifier,
        index: Int,
        item: ChannelAttachmentsViewState.Content.Item,
        currentUser: User?,
        onClick: () -> Unit,
    ) {
        DefaultChannelFilesAttachmentsItem(
            modifier = modifier.animateItem(),
            item = item,
            currentUser = currentUser,
            onClick = onClick,
        )
    }

    /**
     * Factory method for creating a divider between channel files attachments items.
     *
     * @param modifier The modifier for styling the divider.
     * @param index The index of the item in the list.
     */
    @Composable
    public fun LazyItemScope.ChannelFilesAttachmentsItemDivider(
        modifier: Modifier,
        index: Int,
    ) {
        StreamHorizontalDivider()
    }

    /**
     * Factory method for creating the loading item in the channel files attachments list.
     *
     * This is typically shown at the end of the list when more items are being loaded.
     */
    @Composable
    public fun LazyItemScope.ChannelFilesAttachmentsLoadingItem(modifier: Modifier) {
        LoadingFooter(modifier = modifier.fillMaxWidth())
    }

    /**
     * Factory method for creating the top bar of the channel media attachments screen.
     *
     * @param gridState The state of the lazy grid.
     * @param onNavigationIconClick Callback invoked when the navigation icon is clicked.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    public fun ChannelMediaAttachmentsTopBar(
        gridState: LazyGridState,
        onNavigationIconClick: () -> Unit,
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.stream_ui_channel_attachments_media_title),
                    style = ChatTheme.typography.title3Bold,
                    maxLines = 1,
                )
            },
            navigationIcon = { ChannelInfoNavigationIcon(onClick = onNavigationIconClick) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = ChatTheme.colors.barsBackground,
                titleContentColor = ChatTheme.colors.textHighEmphasis,
            ),
        )
    }

    /**
     * Factory method for creating the loading indicator of the channel media attachments screen.
     */
    @Composable
    public fun ChannelMediaAttachmentsLoadingIndicator(modifier: Modifier) {
        LoadingIndicator(modifier = modifier)
    }

    /**
     * Factory method for creating the empty content of the channel media attachments screen.
     */
    @Composable
    public fun BoxScope.ChannelMediaAttachmentsEmptyContent(modifier: Modifier) {
        EmptyContent(
            modifier = modifier,
            title = stringResource(UiCommonR.string.stream_ui_channel_attachments_media_empty_title),
            text = stringResource(UiCommonR.string.stream_ui_channel_attachments_media_empty_text),
            painter = painterResource(UiCommonR.drawable.stream_ic_media),
        )
    }

    @Composable
    public fun BoxScope.ChannelMediaAttachmentsErrorContent(modifier: Modifier) {
        EmptyContent(
            modifier = modifier,
            text = stringResource(UiCommonR.string.stream_ui_channel_attachments_media_load_error),
            painter = rememberVectorPainter(Icons.TwoTone.Warning),
        )
    }

    /**
     * Factory method for creating the channel media attachments floating header.
     * This is typically used to display the title of a group of attachments.
     *
     * @param modifier The modifier for styling the floating header.
     * @param label The label for the floating item.
     */
    @Composable
    public fun BoxScope.ChannelMediaAttachmentsFloatingHeader(
        modifier: Modifier,
        label: String,
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .background(
                    color = ChatTheme.colors.textHighEmphasis.copy(alpha = 0.6f),
                    shape = ButtonDefaults.outlinedShape,
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            text = label,
            style = ChatTheme.typography.bodyBold,
            color = ChatTheme.colors.textHighEmphasisInverse,
        )
    }

    /**
     * Factory method for creating the channel media attachments item.
     *
     * @param modifier The modifier for styling the item.
     * @param index The index of the item in the list.
     * @param item The channel file attachment item to display.
     * @param onClick Callback invoked when the item is clicked.
     */
    @Composable
    public fun LazyGridItemScope.ChannelMediaAttachmentsItem(
        modifier: Modifier,
        index: Int,
        item: ChannelAttachmentsViewState.Content.Item,
        onClick: () -> Unit,
    ) {
        DefaultChannelMediaAttachmentsItem(
            modifier = modifier.animateItem(),
            item = item,
            onClick = onClick,
        )
    }

    /**
     * Factory method for creating the loading item in the channel media attachments list.
     *
     * This is typically shown at the end of the list when more items are being loaded.
     */
    @Composable
    public fun LazyGridItemScope.ChannelMediaAttachmentsLoadingItem(modifier: Modifier) {
        Box(
            modifier = modifier.aspectRatio(1f),
            contentAlignment = Alignment.Center,
        ) {
            @Suppress("MagicNumber")
            CircularProgressIndicator(
                modifier = modifier.fillMaxSize(.25f),
                strokeWidth = 2.dp,
                color = ChatTheme.colors.primaryAccent,
            )
        }
    }

    /**
     * Factory method for creating the top bar of the channel media attachments preview screen.
     *
     * @param item The item to display in the top bar.
     * @param onNavigationIconClick Callback invoked when the navigation icon is clicked.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    public fun ChannelMediaAttachmentsPreviewTopBar(
        item: ChannelAttachmentsViewState.Content.Item,
        onNavigationIconClick: () -> Unit,
    ) {
        CenterAlignedTopAppBar(
            title = { ChannelMediaAttachmentsPreviewTopBarTitle(item = item) },
            navigationIcon = {
                IconButton(onClick = onNavigationIconClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.stream_compose_ic_close),
                        contentDescription = stringResource(id = R.string.stream_compose_cancel),
                        tint = ChatTheme.colors.textHighEmphasis,
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = ChatTheme.colors.barsBackground,
                titleContentColor = ChatTheme.colors.textHighEmphasis,
            ),
        )
    }

    /**
     * Factory method for creating the title of the channel media attachments preview top bar.
     * This displays the message sender's name and the time when the message was sent.
     *
     * @param item The item containing the message to display in the top bar.
     */
    @Composable
    public fun ChannelMediaAttachmentsPreviewTopBarTitle(item: ChannelAttachmentsViewState.Content.Item) {
        val dateFormatter = ChatTheme.dateFormatter
        val title = item.message.user.name
        val subtitle = dateFormatter.formatRelativeTime(item.message.getCreatedAtOrThrow())
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = ChatTheme.typography.title3Bold,
                color = ChatTheme.colors.textHighEmphasis,
                maxLines = 1,
            )
            Text(
                text = subtitle,
                style = ChatTheme.typography.footnote,
                color = ChatTheme.colors.textLowEmphasis,
                maxLines = 1,
            )
        }
    }

    /**
     * Factory method for creating the bottom bar of the channel media attachments preview screen.
     *
     * @param text The text to display in the bottom bar.
     */
    @Deprecated(
        message = "Use ChannelMediaAttachmentsPreviewBottomBar(" +
            "params: ChannelMediaAttachmentsPreviewBottomBarParams) instead.",
        replaceWith = ReplaceWith(
            "ChannelMediaAttachmentsPreviewBottomBar(ChannelMediaAttachmentsPreviewBottomBarParams(" +
                "centerContent = { Text(text) }))",
        ),
    )
    @Composable
    public fun ChannelMediaAttachmentsPreviewBottomBar(text: String) {
        Row(
            modifier = Modifier
                .background(ChatTheme.colors.barsBackground)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = text,
                style = ChatTheme.typography.title3Bold,
                color = ChatTheme.colors.textHighEmphasis,
                maxLines = 1,
            )
        }
    }

    /**
     * Factory method for creating the bottom bar of the channel media attachments preview screen.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    public fun ChannelMediaAttachmentsPreviewBottomBar(
        params: ChannelMediaAttachmentsPreviewBottomBarParams,
    ) {
        CenterAlignedTopAppBar(
            title = { params.centerContent() },
            navigationIcon = { params.leadingContent() },
            actions = { params.trailingContent() },
            windowInsets = BottomAppBarDefaults.windowInsets,
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = ChatTheme.colors.barsBackground,
                titleContentColor = ChatTheme.colors.textHighEmphasis,
                navigationIconContentColor = ChatTheme.colors.textHighEmphasis,
                actionIconContentColor = ChatTheme.colors.textHighEmphasis,
            ),
        )
    }
}
