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

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Warning
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.extensions.getCreatedAtOrThrow
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.channels.list.ItemState
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.PollPickerMode
import io.getstream.chat.android.compose.ui.attachments.content.UnsupportedAttachmentContent
import io.getstream.chat.android.compose.ui.attachments.content.onFileAttachmentContentItemClick
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
import io.getstream.chat.android.compose.ui.channels.list.DefaultChannelSwipeActions
import io.getstream.chat.android.compose.ui.channels.list.DefaultChannelsLoadingMoreIndicator
import io.getstream.chat.android.compose.ui.channels.list.DefaultSearchResultItemCenterContent
import io.getstream.chat.android.compose.ui.channels.list.DefaultSearchResultItemLeadingContent
import io.getstream.chat.android.compose.ui.channels.list.DefaultSearchResultItemTrailingContent
import io.getstream.chat.android.compose.ui.channels.list.LocalSwipeRevealCoordinator
import io.getstream.chat.android.compose.ui.channels.list.SearchResultItem
import io.getstream.chat.android.compose.ui.channels.list.SwipeableChannelItem
import io.getstream.chat.android.compose.ui.components.DefaultSearchClearButton
import io.getstream.chat.android.compose.ui.components.DefaultSearchLabel
import io.getstream.chat.android.compose.ui.components.DefaultSearchLeadingIcon
import io.getstream.chat.android.compose.ui.components.EmptyContent
import io.getstream.chat.android.compose.ui.components.LoadingFooter
import io.getstream.chat.android.compose.ui.components.LoadingIndicator
import io.getstream.chat.android.compose.ui.components.NetworkLoadingIndicator
import io.getstream.chat.android.compose.ui.components.SearchInput
import io.getstream.chat.android.compose.ui.components.StreamHorizontalDivider
import io.getstream.chat.android.compose.ui.components.button.StreamButton
import io.getstream.chat.android.compose.ui.components.button.StreamButtonSize
import io.getstream.chat.android.compose.ui.components.button.StreamButtonStyleDefaults
import io.getstream.chat.android.compose.ui.components.channels.ChannelOptions
import io.getstream.chat.android.compose.ui.components.channels.MessageReadStatusIcon
import io.getstream.chat.android.compose.ui.components.channels.UnreadCountIndicator
import io.getstream.chat.android.compose.ui.components.common.CommandChip
import io.getstream.chat.android.compose.ui.components.common.ContextualMenuItem
import io.getstream.chat.android.compose.ui.components.composer.ComposerLinkPreview
import io.getstream.chat.android.compose.ui.components.composer.CoolDownIndicator
import io.getstream.chat.android.compose.ui.components.composer.MessageInput
import io.getstream.chat.android.compose.ui.components.messageoptions.MessageOptions
import io.getstream.chat.android.compose.ui.components.messages.ClusteredMessageReactions
import io.getstream.chat.android.compose.ui.components.messages.DefaultMessageContent
import io.getstream.chat.android.compose.ui.components.messages.DefaultMessageDeletedContent
import io.getstream.chat.android.compose.ui.components.messages.GiphyMessageContent
import io.getstream.chat.android.compose.ui.components.messages.MessageComposerQuotedMessage
import io.getstream.chat.android.compose.ui.components.messages.MessageFooter
import io.getstream.chat.android.compose.ui.components.messages.MessageText
import io.getstream.chat.android.compose.ui.components.messages.MessageThreadFooter
import io.getstream.chat.android.compose.ui.components.messages.OwnedMessageVisibilityContent
import io.getstream.chat.android.compose.ui.components.messages.QuotedMessage
import io.getstream.chat.android.compose.ui.components.messages.ScrollToBottomButton
import io.getstream.chat.android.compose.ui.components.messages.SegmentedMessageReactions
import io.getstream.chat.android.compose.ui.components.messages.SwipeToReplyIcon
import io.getstream.chat.android.compose.ui.components.reactionpicker.ReactionsPicker
import io.getstream.chat.android.compose.ui.components.reactions.ReactionToggleSize
import io.getstream.chat.android.compose.ui.components.selectedmessage.MessageMenuHeader
import io.getstream.chat.android.compose.ui.components.selectedmessage.SelectedMessageMenu
import io.getstream.chat.android.compose.ui.messages.composer.internal.AudioRecordingButton
import io.getstream.chat.android.compose.ui.messages.composer.internal.MessageComposerEditIndicator
import io.getstream.chat.android.compose.ui.messages.composer.internal.attachments.MessageComposerAttachmentAudioRecordItem
import io.getstream.chat.android.compose.ui.messages.composer.internal.attachments.MessageComposerAttachmentFileItem
import io.getstream.chat.android.compose.ui.messages.composer.internal.attachments.MessageComposerAttachmentMediaItem
import io.getstream.chat.android.compose.ui.messages.composer.internal.attachments.MessageComposerAttachments
import io.getstream.chat.android.compose.ui.messages.composer.internal.suggestions.CommandSuggestionItem
import io.getstream.chat.android.compose.ui.messages.composer.internal.suggestions.DefaultCommandSuggestionItemCenterContent
import io.getstream.chat.android.compose.ui.messages.composer.internal.suggestions.DefaultCommandSuggestionItemLeadingContent
import io.getstream.chat.android.compose.ui.messages.composer.internal.suggestions.DefaultUserSuggestionItemCenterContent
import io.getstream.chat.android.compose.ui.messages.composer.internal.suggestions.DefaultUserSuggestionItemLeadingContent
import io.getstream.chat.android.compose.ui.messages.composer.internal.suggestions.UserSuggestionItem
import io.getstream.chat.android.compose.ui.messages.header.DefaultMessageListHeaderCenterContent
import io.getstream.chat.android.compose.ui.messages.header.DefaultMessageListHeaderLeadingContent
import io.getstream.chat.android.compose.ui.messages.header.DefaultMessageListHeaderTrailingContent
import io.getstream.chat.android.compose.ui.messages.list.DefaultMessageAuthor
import io.getstream.chat.android.compose.ui.messages.list.DefaultMessageBottom
import io.getstream.chat.android.compose.ui.messages.list.DefaultMessageContent
import io.getstream.chat.android.compose.ui.messages.list.DefaultMessageDateSeparatorContent
import io.getstream.chat.android.compose.ui.messages.list.DefaultMessageItem
import io.getstream.chat.android.compose.ui.messages.list.DefaultMessageListEmptyContent
import io.getstream.chat.android.compose.ui.messages.list.DefaultMessageListLoadingIndicator
import io.getstream.chat.android.compose.ui.messages.list.DefaultMessageModeratedContent
import io.getstream.chat.android.compose.ui.messages.list.DefaultMessageThreadSeparatorContent
import io.getstream.chat.android.compose.ui.messages.list.DefaultMessageTop
import io.getstream.chat.android.compose.ui.messages.list.DefaultMessageTypingIndicatorContent
import io.getstream.chat.android.compose.ui.messages.list.DefaultMessageUnreadSeparatorContent
import io.getstream.chat.android.compose.ui.messages.list.DefaultMessagesHelperContent
import io.getstream.chat.android.compose.ui.messages.list.DefaultMessagesLoadingMoreIndicator
import io.getstream.chat.android.compose.ui.messages.list.DefaultSystemMessageContent
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
import io.getstream.chat.android.compose.ui.util.StreamSnackbar
import io.getstream.chat.android.compose.ui.util.bottomBorder
import io.getstream.chat.android.compose.ui.util.topBorder
import io.getstream.chat.android.compose.viewmodel.messages.AudioPlayerViewModelFactory
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.React
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState
import io.getstream.chat.android.ui.common.state.messages.list.MessageListItemState
import io.getstream.chat.android.compose.ui.channel.attachments.ChannelFilesAttachmentsItem as DefaultChannelFilesAttachmentsItem
import io.getstream.chat.android.compose.ui.channel.attachments.ChannelMediaAttachmentsItem as DefaultChannelMediaAttachmentsItem
import io.getstream.chat.android.compose.ui.channel.info.ChannelInfoOptionItem as DefaultChannelInfoOptionItem
import io.getstream.chat.android.ui.common.R as UiCommonR

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
 *             params: ChannelListHeaderTrailingContentParams,
 *         ) {
 *             IconButton(onClick = params.onHeaderActionClick) {
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
 *     override fun RowScope.ChannelListHeaderTrailingContent(
 *         params: ChannelListHeaderTrailingContentParams,
 *     ) {
 *         IconButton(onClick = params.onHeaderActionClick) {
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
     * The default header shown above the channel list.
     * Usually contains the current user's avatar, a title or the connected status, and an action button.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun ChannelListHeader(params: ChannelListHeaderParams) {
        io.getstream.chat.android.compose.ui.channels.header.ChannelListHeader(
            modifier = params.modifier,
            title = params.title,
            currentUser = params.currentUser,
            connectionState = params.connectionState,
            onAvatarClick = params.onAvatarClick,
            onHeaderActionClick = params.onHeaderActionClick,
        )
    }

    /**
     * The default leading content of the channel list header.
     * Usually the avatar of the current user if it's available.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun RowScope.ChannelListHeaderLeadingContent(params: ChannelListHeaderLeadingContentParams) {
        DefaultChannelHeaderLeadingContent(
            currentUser = params.currentUser,
            onAvatarClick = params.onAvatarClick,
        )
    }

    /**
     * The default center content of the channel list header.
     * Usually shows the title if connectionState is [ConnectionState.Connected],
     * a `Disconnected` text if connectionState is offline,
     * or [NetworkLoadingIndicator] otherwise.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun RowScope.ChannelListHeaderCenterContent(params: ChannelListHeaderCenterContentParams) {
        DefaultChannelListHeaderCenterContent(
            connectionState = params.connectionState,
            title = params.title,
        )
    }

    /**
     * The default trailing content of the channel list header.
     * Usually an action button.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun RowScope.ChannelListHeaderTrailingContent(params: ChannelListHeaderTrailingContentParams) {
        DefaultChannelListHeaderTrailingContent(
            onHeaderActionClick = params.onHeaderActionClick,
        )
    }

    /**
     * The default loading indicator of the channel list, when the initial data is loading.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun ChannelListLoadingIndicator(params: ChannelListLoadingIndicatorParams) {
        DefaultChannelListLoadingIndicator(
            modifier = params.modifier,
        )
    }

    /**
     * The default empty content of the channel list.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun ChannelListEmptyContent(params: ChannelListEmptyContentParams) {
        DefaultChannelListEmptyContent(
            modifier = params.modifier,
            onStartChatClick = params.onStartChatClick,
        )
    }

    /**
     * The default channel list item content.
     * When swipe actions are enabled and a [SwipeRevealCoordinator][LocalSwipeRevealCoordinator]
     * is provided, wraps the item in [SwipeableChannelItem].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun LazyItemScope.ChannelListItemContent(params: ChannelListItemContentParams) {
        val coordinator = LocalSwipeRevealCoordinator.current
        val swipeEnabled = ChatTheme.config.channelList.swipeActionsEnabled && coordinator != null

        if (swipeEnabled) {
            SwipeableChannelItem(
                modifier = Modifier.animateItem(),
                channelCid = params.channelItem.channel.cid,
                backgroundColor = ChatTheme.colors.backgroundCoreApp,
                swipeActions = { ChannelSwipeActions(ChannelSwipeActionsParams(params.channelItem)) },
            ) {
                ChannelItem(
                    channelItem = params.channelItem,
                    currentUser = params.currentUser,
                    onChannelClick = params.onChannelClick,
                    onChannelLongClick = params.onChannelLongClick,
                )
            }
        } else {
            ChannelItem(
                modifier = Modifier.animateItem(),
                channelItem = params.channelItem,
                currentUser = params.currentUser,
                onChannelClick = params.onChannelClick,
                onChannelLongClick = params.onChannelLongClick,
            )
        }
    }

    /**
     * The swipe actions revealed when swiping a channel list item.
     * Override this to provide custom swipe actions.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun ChannelSwipeActions(params: ChannelSwipeActionsParams) {
        DefaultChannelSwipeActions(params.channelItem)
    }

    /**
     * The default helper content of the channel list.
     * It's empty by default and can be used to implement a scroll to top feature.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun BoxScope.ChannelListHelperContent(params: ChannelListHelperContentParams) {
    }

    /**
     * The default loading more item, when the next page of the channel list is loading.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun LazyItemScope.ChannelListLoadingMoreItemContent(params: ChannelListLoadingMoreItemContentParams) {
        DefaultChannelsLoadingMoreIndicator()
    }

    /**
     * The default divider between channel items.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun LazyItemScope.ChannelListDividerItem(params: ChannelListDividerItemParams) {
        StreamHorizontalDivider()
    }

    /**
     * The default leading content of the channel item.
     * Usually the avatar that holds an image of the channel or its members.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun RowScope.ChannelItemLeadingContent(params: ChannelItemLeadingContentParams) {
        DefaultChannelItemLeadingContent(
            channelItem = params.channelItem,
            currentUser = params.currentUser,
        )
    }

    /**
     * The default center content of the channel item.
     * Usually the name of the channel and the last message.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun RowScope.ChannelItemCenterContent(params: ChannelItemCenterContentParams) {
        DefaultChannelItemCenterContent(
            channelItemState = params.channelItem,
            currentUser = params.currentUser,
        )
    }

    /**
     * The default trailing content of the channel item.
     * Usually information about the last message such as its read state, timestamp, and the number of unread messages.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun RowScope.ChannelItemTrailingContent(params: ChannelItemTrailingContentParams) {
        DefaultChannelItemTrailingContent(
            channel = params.channelItem.channel,
            currentUser = params.currentUser,
        )
    }

    /**
     * The default unread count indicator in the channel item.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun ChannelItemUnreadCountIndicator(params: ChannelItemUnreadCountIndicatorParams) {
        UnreadCountIndicator(
            unreadCount = params.unreadCount,
            modifier = params.modifier,
        )
    }

    /**
     * The default read status indicator in the channel item, whether the last message is sent, pending or read.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun ChannelItemReadStatusIndicator(params: ChannelItemReadStatusIndicatorParams) {
        MessageReadStatusIcon(
            channel = params.channel,
            message = params.message,
            currentUser = params.currentUser,
            modifier = params.modifier,
        )
    }

    /**
     * The default search input of the channel list.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun ChannelListSearchInput(params: ChannelListSearchInputParams) {
        SearchInput(
            modifier = params.modifier,
            query = params.query,
            onSearchStarted = params.onSearchStarted,
            onValueChange = params.onValueChange,
        )
    }

    /**
     * The default leading icon of the search input.
     *
     * Used by [ChannelListSearchInput].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun RowScope.SearchInputLeadingIcon(params: SearchInputLeadingIconParams) {
        DefaultSearchLeadingIcon()
    }

    /**
     * The default label of the search input.
     *
     * Used by [ChannelListSearchInput].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun SearchInputLabel(params: SearchInputLabelParams) {
        DefaultSearchLabel()
    }

    /**
     * The default clear button of the search input.
     *
     * Used by [ChannelListSearchInput].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun SearchInputClearButton(params: SearchInputClearButtonParams) {
        DefaultSearchClearButton(onClick = params.onClick)
    }

    /**
     * The default empty search content of the channel list, when there are no matching search results.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun ChannelListEmptySearchContent(params: ChannelListEmptySearchContentParams) {
        DefaultChannelSearchEmptyContent(
            modifier = params.modifier,
            searchQuery = params.searchQuery,
        )
    }

    /**
     * The default search result item of the channel list.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun LazyItemScope.SearchResultItemContent(params: SearchResultItemContentParams) {
        SearchResultItem(
            searchResultItemState = params.searchResultItem,
            currentUser = params.currentUser,
            onSearchResultClick = params.onSearchResultClick,
        )
    }

    /**
     * The default leading content of a search result item. Shows the avatar of the user who sent the message.
     *
     * Used by [SearchResultItemContent].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun RowScope.SearchResultItemLeadingContent(params: SearchResultItemLeadingContentParams) {
        DefaultSearchResultItemLeadingContent(params.searchResultItem, params.currentUser)
    }

    /**
     * The default center content of a search result item. Shows information about the message and by who and where
     * it was sent.
     *
     * Used by [SearchResultItemContent].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun RowScope.SearchResultItemCenterContent(params: SearchResultItemCenterContentParams) {
        DefaultSearchResultItemCenterContent(params.searchResultItem, params.currentUser)
    }

    /**
     * The default trailing content of a search result item. Shows the message timestamp.
     *
     * Used by [SearchResultItemContent].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun RowScope.SearchResultItemTrailingContent(params: SearchResultItemTrailingContentParams) {
        DefaultSearchResultItemTrailingContent(params.searchResultItem)
    }

    /**
     * The default header of the message list.
     * Usually a back button as a leading content,
     * the channel title in the top center,
     * the channel information or the connection status in the bottom center,
     * and the channel avatar as the trailing content.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageListHeader(params: MessageListHeaderParams) {
        io.getstream.chat.android.compose.ui.messages.header.MessageListHeader(
            channel = params.channel,
            currentUser = params.currentUser,
            connectionState = params.connectionState,
            modifier = params.modifier,
            typingUsers = params.typingUsers,
            messageMode = params.messageMode,
            onBackPressed = params.onBackPressed,
            onHeaderTitleClick = params.onHeaderTitleClick,
            onChannelAvatarClick = params.onChannelAvatarClick,
        )
    }

    /**
     * The default leading content of the message list header, which is the back button.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun RowScope.MessageListHeaderLeadingContent(params: MessageListHeaderLeadingContentParams) {
        DefaultMessageListHeaderLeadingContent(onBackPressed = params.onBackPressed)
    }

    /**
     * The default center content of the message list header.
     * Usually shows the channel title in the top and
     * the channel information or the connection status in the bottom.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun RowScope.MessageListHeaderCenterContent(params: MessageListHeaderCenterContentParams) {
        DefaultMessageListHeaderCenterContent(
            modifier = params.modifier,
            channel = params.channel,
            currentUser = params.currentUser,
            connectionState = params.connectionState,
            messageMode = params.messageMode,
            onHeaderTitleClick = params.onClick,
        )
    }

    /**
     * The default trailing content of the message list header, which is the channel avatar.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun RowScope.MessageListHeaderTrailingContent(params: MessageListHeaderTrailingContentParams) {
        DefaultMessageListHeaderTrailingContent(
            channel = params.channel,
            currentUser = params.currentUser,
            onClick = params.onClick,
        )
    }

    /**
     * The default background of the message list.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageListBackground(params: MessageListBackgroundParams) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ChatTheme.colors.backgroundCoreApp),
        )
    }

    /**
     * The default loading indicator of the message list,
     * when the initial message list is loading.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageListLoadingIndicator(params: MessageListLoadingIndicatorParams) {
        DefaultMessageListLoadingIndicator(
            modifier = params.modifier,
        )
    }

    /**
     * The default empty content of the message list,
     * when the message list is empty.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageListEmptyContent(params: MessageListEmptyContentParams) {
        DefaultMessageListEmptyContent(
            modifier = params.modifier,
        )
    }

    /**
     * The default helper content of the message list.
     * It handles the scroll-to-bottom and scroll-to-focused message features.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun BoxScope.MessageListHelperContent(params: MessageListHelperContentParams) {
        DefaultMessagesHelperContent(
            messagesState = params.messageListState,
            messagesLazyListState = params.messagesLazyListState,
            contentPadding = params.contentPadding,
            scrollToBottom = params.onScrollToBottomClick,
        )
    }

    /**
     * The default scroll-to-bottom button shown when the user scrolls away from the bottom of the list.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun ScrollToBottomButton(params: ScrollToBottomButtonParams) {
        // Disable animations in snapshot tests, at least until Paparazzi has a better support for animations.
        // This is due to the scroll to bottom tests, where the items are not visible in the snapshots.
        if (LocalInspectionMode.current) {
            if (params.visible) {
                ScrollToBottomButton(
                    modifier = params.modifier,
                    count = params.count,
                    onClick = params.onClick,
                )
            }
        } else {
            FadingVisibility(
                modifier = params.modifier,
                visible = params.visible,
            ) {
                ScrollToBottomButton(
                    count = params.count,
                    onClick = params.onClick,
                )
            }
        }
    }

    /**
     * The default message item component, which renders each [MessageListItemState]'s subtype.
     * This includes date separators, system messages, and regular messages.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun LazyItemScope.MessageItem(params: MessageItemParams) {
        DefaultMessageItem(
            messageListItemState = params.messageListItem,
            reactionSorting = params.reactionSorting,
            onPollUpdated = params.onPollUpdated,
            onCastVote = params.onCastVote,
            onRemoveVote = params.onRemoveVote,
            selectPoll = params.selectPoll,
            onClosePoll = params.onClosePoll,
            onAddPollOption = params.onAddPollOption,
            onLongItemClick = params.onLongItemClick,
            onThreadClick = params.onThreadClick,
            onReactionsClick = params.onReactionsClick,
            onGiphyActionClick = params.onGiphyActionClick,
            onMediaGalleryPreviewResult = params.onMediaGalleryPreviewResult,
            onQuotedMessageClick = params.onQuotedMessageClick,
            onUserAvatarClick = params.onUserAvatarClick,
            onLinkClick = params.onMessageLinkClick,
            onUserMentionClick = params.onUserMentionClick,
            onAddAnswer = params.onAddAnswer,
            onReply = params.onReply,
        )
    }

    /**
     * The default message list item modifier for styling.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun LazyItemScope.messageListItemModifier(params: MessageListItemModifierParams): Modifier =
        if (LocalInspectionMode.current) {
            Modifier
        } else {
            Modifier.animateItem()
        }

    /**
     * The default loading more item of the message list,
     * when the next page of messages is loading.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun LazyItemScope.MessageListLoadingMoreItemContent(params: MessageListLoadingMoreItemContentParams) {
        DefaultMessagesLoadingMoreIndicator()
    }

    /**
     * The default date separator item content of the message list.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun LazyItemScope.MessageListDateSeparatorItemContent(params: MessageListDateSeparatorItemContentParams) {
        DefaultMessageDateSeparatorContent(dateSeparator = params.dateSeparatorItem)
    }

    /**
     * The default unread separator item content of the message list.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun LazyItemScope.MessageListUnreadSeparatorItemContent(
        params: MessageListUnreadSeparatorItemContentParams,
    ) {
        DefaultMessageUnreadSeparatorContent(unreadSeparatorItemState = params.unreadSeparatorItem)
    }

    /**
     * The default thread date separator item content of the message list.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun LazyItemScope.MessageListThreadDateSeparatorItemContent(
        params: MessageListThreadDateSeparatorItemContentParams,
    ) {
        DefaultMessageThreadSeparatorContent(threadSeparator = params.threadDateSeparatorItem)
    }

    /**
     * The default system message content of the message list.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun LazyItemScope.MessageListSystemItemContent(params: MessageListSystemItemContentParams) {
        DefaultSystemMessageContent(systemMessageState = params.systemMessageItem)
    }

    /**
     * The default moderated message content of the message list.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun LazyItemScope.MessageListModeratedItemContent(params: MessageListModeratedItemContentParams) {
        DefaultMessageModeratedContent(moderatedMessageItemState = params.moderatedMessageItem)
    }

    /**
     * The default typing indicator content of the message list.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun LazyItemScope.MessageListTypingIndicatorItemContent(
        params: MessageListTypingIndicatorItemContentParams,
    ) {
        DefaultMessageTypingIndicatorContent(params.typingItem)
    }

    /**
     * The default empty thread placeholder item content of the message list.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun LazyItemScope.MessageListEmptyThreadPlaceholderItemContent(
        params: MessageListEmptyThreadPlaceholderItemContentParams,
    ) {
    }

    /**
     * The default start of the channel item content of the message list.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun LazyItemScope.MessageListStartOfTheChannelItemContent(
        params: MessageListStartOfTheChannelItemContentParams,
    ) {
    }

    /**
     * The default container for a regular message, which includes the author avatar,
     * message bubble, and reactions.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageContainer(params: MessageContainerParams) {
        io.getstream.chat.android.compose.ui.messages.list.MessageContainer(
            messageItem = params.messageItem,
            reactionSorting = params.reactionSorting,
            modifier = params.modifier,
            onPollUpdated = params.onPollUpdated,
            onCastVote = params.onCastVote,
            onRemoveVote = params.onRemoveVote,
            selectPoll = params.selectPoll,
            onClosePoll = params.onClosePoll,
            onAddPollOption = params.onAddPollOption,
            onLongItemClick = params.onLongItemClick,
            onThreadClick = params.onThreadClick,
            onReactionsClick = params.onReactionsClick,
            onGiphyActionClick = params.onGiphyActionClick,
            onMediaGalleryPreviewResult = params.onMediaGalleryPreviewResult,
            onQuotedMessageClick = params.onQuotedMessageClick,
            onUserAvatarClick = params.onUserAvatarClick?.let { { it.invoke(params.messageItem.message.user) } },
            onLinkClick = params.onMessageLinkClick,
            onUserMentionClick = params.onUserMentionClick,
            onAddAnswer = params.onAddAnswer,
            onReply = params.onReply,
        )
    }

    /**
     * The default appearance of the message bubble.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageBubble(params: MessageBubbleParams) {
        io.getstream.chat.android.compose.ui.components.messages.MessageBubble(
            modifier = params.modifier,
            color = params.color,
            shape = params.shape,
            border = params.border,
            content = params.content,
        )
    }

    /**
     * Icon shown at the bottom-end of a [Message] when it failed to send.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageFailedIcon(params: MessageFailedIconParams) {
        Icon(
            modifier = params.modifier,
            painter = painterResource(id = R.drawable.stream_compose_ic_error),
            contentDescription = null,
            tint = ChatTheme.colors.accentError,
        )
    }

    /**
     * The default top content inside the message bubble.
     * Usually shows pinned indicator and thread labels.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun ColumnScope.MessageTop(params: MessageTopParams) {
        DefaultMessageTop(
            messageItem = params.messageItem,
            onThreadClick = params.onThreadClick,
        )
    }

    /**
     * The default bottom content inside the message bubble.
     * Usually shows timestamp and delivery status.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun ColumnScope.MessageBottom(params: MessageBottomParams) {
        DefaultMessageBottom(messageItem = params.messageItem)
    }

    /**
     * The default author content for a message.
     * Usually shows the avatar of the user if the message doesn't belong to the current user.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun RowScope.MessageAuthor(params: MessageAuthorParams) {
        DefaultMessageAuthor(
            messageItem = params.messageItem,
            onUserAvatarClick = params.onUserAvatarClick,
        )
    }

    /**
     * The default content of the message bubble.
     * Usually contains attachments and text.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageContent(params: MessageContentParams) {
        DefaultMessageContent(
            messageItem = params.messageItem,
            onLongItemClick = params.onLongItemClick,
            onGiphyActionClick = params.onGiphyActionClick,
            onQuotedMessageClick = params.onQuotedMessageClick,
            onLinkClick = params.onLinkClick,
            onUserMentionClick = params.onUserMentionClick,
            onMediaGalleryPreviewResult = params.onMediaGalleryPreviewResult,
            onPollUpdated = params.onPollUpdated,
            onCastVote = params.onCastVote,
            onRemoveVote = params.onRemoveVote,
            selectPoll = params.selectPoll,
            onAddAnswer = params.onAddAnswer,
            onClosePoll = params.onClosePoll,
            onAddPollOption = params.onAddPollOption,
        )
    }

    /**
     * The empty space in the message item opposite to the message bubble.
     * For example, for outgoing messages, by default the spacer is placed before the bubble.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun RowScope.MessageSpacer(params: MessageSpacerParams) {
    }

    /**
     * The component displaying the reactions on a message. Defaults to [SegmentedMessageReactions],
     * but an equivalent implementation with a different visual style is available through
     * [ClusteredMessageReactions].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageReactions(
        params: MessageReactionsParams,
    ) {
        SegmentedMessageReactions(
            modifier = params.modifier,
            reactions = params.reactions,
            onClick = params.onClick?.let { onClick -> { onClick(params.message) } },
        )
    }

    /**
     * The default Giphy message content.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageGiphyContent(params: MessageGiphyContentParams) {
        GiphyMessageContent(
            message = params.message,
            currentUser = params.currentUser,
            onGiphyActionClick = params.onGiphyActionClick,
        )
    }

    /**
     * The default content of a deleted message.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageDeletedContent(params: MessageDeletedContentParams) {
        DefaultMessageDeletedContent(
            message = params.message,
            currentUser = params.currentUser,
            modifier = params.modifier,
        )
    }

    /**
     * The default content of a regular message that can contain attachments and text.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageRegularContent(params: MessageRegularContentParams) {
        DefaultMessageContent(
            message = params.message,
            currentUser = params.currentUser,
            onLongItemClick = params.onLongItemClick,
            onMediaGalleryPreviewResult = params.onMediaGalleryPreviewResult,
            onQuotedMessageClick = params.onQuotedMessageClick,
            onUserMentionClick = params.onUserMentionClick,
            onLinkClick = params.onLinkClick,
        )
    }

    /**
     * The default message text content.
     * Usually with extra styling and padding for the chat bubble.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageTextContent(params: MessageTextContentParams) {
        MessageText(
            modifier = params.modifier,
            message = params.message,
            currentUser = params.currentUser,
            onLongItemClick = params.onLongItemClick,
            onLinkClick = params.onLinkClick,
            onUserMentionClick = params.onUserMentionClick,
        )
    }

    /**
     * The default quoted message content.
     * Usually shows only the sender avatar, text and a single attachment preview.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageQuotedContent(params: MessageQuotedContentParams) {
        QuotedMessage(
            modifier = params.modifier.padding(MessageStyling.messageSectionPadding),
            message = params.message,
            currentUser = params.currentUser,
            replyMessage = params.replyMessage,
        )
    }

    /**
     * The message footer while uploading attachments. Empty by default.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageFooterUploadingContent(params: MessageFooterUploadingContentParams) {
    }

    /**
     * The default content of the only-visible-to-you footer message.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageFooterOnlyVisibleToYouContent(params: MessageFooterOnlyVisibleToYouContentParams) {
        OwnedMessageVisibilityContent(
            message = params.messageItem.message,
        )
    }

    /**
     * The default footer content.
     * Usually contains either [MessageThreadFooter] or the default footer,
     * which holds the sender name and the timestamp.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageFooterContent(params: MessageFooterContentParams) {
        MessageFooter(messageItem = params.messageItem)
    }

    /**
     * The default message footer status indicator.
     * Displays the delivery or read status icon of a message.
     *
     * @param params Parameters for this component.
     */
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
            MessageReadStatusIcon(
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
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageComposer(
        params: MessageComposerParams,
    ) {
        io.getstream.chat.android.compose.ui.messages.composer.MessageComposer(
            messageComposerState = params.messageComposerState,
            isAttachmentPickerVisible = params.isAttachmentPickerVisible,
            onSendMessage = params.onSendMessage,
            modifier = params.modifier,
            onAttachmentsClick = params.onAttachmentsClick,
            onValueChange = params.onValueChange,
            onAttachmentRemoved = params.onAttachmentRemoved,
            onCancelAction = params.onCancelAction,
            onLinkPreviewClick = params.onLinkPreviewClick,
            onCancelLinkPreviewClick = params.onCancelLinkPreviewClick,
            onUserSelected = params.onUserSelected,
            onCommandSelected = params.onCommandSelected,
            onAlsoSendToChannelChange = params.onAlsoSendToChannelSelected,
            onActiveCommandDismiss = params.onActiveCommandDismiss,
            recordingActions = params.recordingActions,
            input = params.input,
        )
    }

    /**
     * Shows a preview of the link that the user has entered in the message composer.
     * Shows the link image preview, the title of the link and its description.
     *
     * Used as part of [MessageComposerInput].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageComposerLinkPreview(
        params: MessageComposerLinkPreviewParams,
    ) {
        ComposerLinkPreview(
            modifier = params.modifier.padding(horizontal = StreamTokens.spacingSm),
            linkPreview = params.linkPreview,
            onContentClick = params.onContentClick,
            onCancelClick = params.onCancelClick,
        )
    }

    /**
     * The default user suggestion item of the message composer.
     *
     * Used in [io.getstream.chat.android.compose.ui.messages.composer.internal.suggestions.UserSuggestionList].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageComposerUserSuggestionItem(params: MessageComposerUserSuggestionItemParams) {
        UserSuggestionItem(
            user = params.user,
            currentUser = params.currentUser,
            onUserSelected = params.onUserSelected,
        )
    }

    /**
     * The default leading content of the user suggestion item of the message composer.
     *
     * Used as part of [MessageComposerUserSuggestionItem].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageComposerUserSuggestionItemLeadingContent(
        params: MessageComposerUserSuggestionItemLeadingContentParams,
    ) {
        DefaultUserSuggestionItemLeadingContent(
            modifier = params.modifier,
            user = params.user,
        )
    }

    /**
     * The default center content of the user suggestion item of the message composer.
     *
     * Used as part of [MessageComposerUserSuggestionItem].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageComposerUserSuggestionItemCenterContent(
        params: MessageComposerUserSuggestionItemCenterContentParams,
    ) {
        DefaultUserSuggestionItemCenterContent(
            modifier = params.modifier,
            user = params.user,
        )
    }

    /**
     * The default trailing content of the user suggestion item of the message composer.
     *
     * Used as part of [MessageComposerUserSuggestionItem].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageComposerUserSuggestionItemTrailingContent(
        params: MessageComposerUserSuggestionItemTrailingContentParams,
    ) {
    }

    /**
     * The default command suggestion item of the message composer.
     *
     * Used in [io.getstream.chat.android.compose.ui.messages.composer.internal.suggestions.CommandSuggestionList].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageComposerCommandSuggestionItem(params: MessageComposerCommandSuggestionItemParams) {
        CommandSuggestionItem(
            command = params.command,
            onCommandSelected = params.onCommandSelected,
        )
    }

    /**
     * The default leading content of the command suggestion item of the message composer.
     *
     * Used as part of [MessageComposerCommandSuggestionItem].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageComposerCommandSuggestionItemLeadingContent(
        params: MessageComposerCommandSuggestionItemLeadingContentParams,
    ) {
        DefaultCommandSuggestionItemLeadingContent(
            modifier = params.modifier,
            command = params.command,
        )
    }

    /**
     * The default center content of the command suggestion item of the message composer.
     *
     * Used as part of [MessageComposerCommandSuggestionItem].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageComposerCommandSuggestionItemCenterContent(
        params: MessageComposerCommandSuggestionItemCenterContentParams,
    ) {
        DefaultCommandSuggestionItemCenterContent(params.command, params.modifier)
    }

    /**
     * The default leading content of the message composer, which includes an add attachment button by default.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageComposerLeadingContent(params: MessageComposerLeadingContentParams) {
        io.getstream.chat.android.compose.ui.messages.composer.internal.MessageComposerLeadingContent(
            messageInputState = params.state,
            isAttachmentPickerVisible = params.isAttachmentPickerVisible,
            onAttachmentsClick = params.onAttachmentsClick,
        )
    }

    /**
     * The default input of the message composer.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageComposerInput(params: MessageComposerInputParams) {
        MessageInput(
            modifier = params.modifier,
            messageComposerState = params.state,
            onValueChange = params.onInputChanged,
            onAttachmentRemoved = params.onAttachmentRemoved,
            onCancelAction = params.onCancel,
            onLinkPreviewClick = params.onLinkPreviewClick,
            onCancelLinkPreviewClick = params.onCancelLinkPreviewClick,
            onSendClick = params.onSendClick,
            onAlsoSendToChannelChange = params.onAlsoSendToChannelChange,
            recordingActions = params.recordingActions,
            onActiveCommandDismiss = params.onActiveCommandDismiss,
        )
    }

    /**
     * The default appearance of a quoted message in the message composer.
     * Shown when the user quotes (replies to) a message in the composer.
     *
     * Used as part of [MessageComposerInput].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageComposerQuotedMessage(params: MessageComposerQuotedMessageParams) {
        MessageComposerQuotedMessage(
            modifier = params.modifier.padding(horizontal = StreamTokens.spacingSm),
            message = params.quotedMessage,
            currentUser = params.state.currentUser,
            onCancelClick = params.onCancelClick,
        )
    }

    /**
     * The edit indicator shown inside the composer's header when the user edits a message.
     * Previews the original message text and attachment type.
     *
     * Used as part of [MessageComposerInput].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageComposerEditIndicator(params: MessageComposerEditIndicatorParams) {
        MessageComposerEditIndicator(
            modifier = params.modifier.padding(horizontal = StreamTokens.spacingSm),
            message = params.editMessage,
            onCancelClick = params.onCancelClick,
        )
    }

    /**
     * The default leading content of the message composer input row.
     * Shown at the start of the composer input, before the text field.
     * When a command is active, renders a [CommandChip].
     *
     * Used as part of [MessageComposerInput].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageComposerInputLeadingContent(params: MessageComposerInputLeadingContentParams) {
        val activeCommand = params.state.activeCommand ?: return
        CommandChip(
            modifier = params.modifier.padding(
                start = StreamTokens.spacingSm,
                bottom = StreamTokens.spacingSm,
            ),
            command = activeCommand,
            onDismiss = params.onActiveCommandDismiss,
        )
    }

    /**
     * The default center content of the message composer input.
     * Contains the text input field (BasicTextField) with label overlay.
     *
     * Used as part of [MessageComposerInput].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageComposerInputCenterContent(params: MessageComposerInputCenterContentParams) {
        io.getstream.chat.android.compose.ui.messages.composer.internal.MessageComposerInputCenterContent(
            state = params.state,
            onValueChange = params.onValueChange,
            modifier = params.modifier,
        )
    }

    /**
     * The default center bottom content of the message composer input.
     * Shown at the bottom of the composer input.
     *
     * Used as part of [MessageComposerInput].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageComposerInputCenterBottomContent(params: MessageComposerInputCenterBottomContentParams) {
        val inThreadMode = params.state.messageMode is MessageMode.MessageThread
        AnimatedContent(targetState = inThreadMode) { visible ->
            if (visible) {
                io.getstream.chat.android.compose.ui.messages.composer.internal.MessageComposerInputCenterBottomContent(
                    alsoSendToChannel = params.state.alsoSendToChannel,
                    onAlsoSendToChannelChange = params.onAlsoSendToChannelChange,
                )
            }
        }
    }

    /**
     * The default trailing content of the message composer.
     * Shown at the end of the composer input.
     *
     * Used as part of [MessageComposerInput].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageComposerInputTrailingContent(params: MessageComposerInputTrailingContentParams) {
        io.getstream.chat.android.compose.ui.messages.composer.internal.MessageComposerInputTrailingContent(
            state = params.state,
            recordingActions = params.recordingActions,
            onSendClick = params.onSendClick,
        )
    }

    /**
     * The default trailing content of the message composer.
     * Shown after the composer input.
     *
     * Used as part of [MessageComposer].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageComposerTrailingContent(params: MessageComposerTrailingContentParams) {
    }

    /**
     * The default cooldown indicator of the message composer.
     * Shown when the user is prevented from sending messages due to a cooldown.
     *
     * Used as part of [MessageComposerTrailingContent].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageComposerCoolDownIndicator(params: MessageComposerCoolDownIndicatorParams) {
        CoolDownIndicator(params.coolDownTime, params.modifier)
    }

    /**
     * The default "Send" button of the message composer.
     *
     * Used as part of [MessageComposerTrailingContent].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageComposerSendButton(params: MessageComposerSendButtonParams) {
        io.getstream.chat.android.compose.ui.messages.composer.internal.MessageComposerSendButton(
            onClick = params.onClick,
        )
    }

    /**
     * The default "Save" button of the message composer, shown when editing a message.
     * Displays a checkmark icon in a filled circular button.
     *
     * Used as part of [MessageComposerTrailingContent].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageComposerSaveButton(params: MessageComposerSaveButtonParams) {
        io.getstream.chat.android.compose.ui.messages.composer.internal.MessageComposerSaveButton(
            enabled = params.enabled,
            onClick = params.onClick,
        )
    }

    /**
     * The default "Audio recording (voice message)" button of the message composer.
     *
     * Used as part of [MessageComposerTrailingContent].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageComposerAudioRecordingButton(params: MessageComposerAudioRecordingButtonParams) {
        AudioRecordingButton(
            recordingState = params.state,
            recordingActions = params.recordingActions,
        )
    }

    /**
     * The floating lock icon displayed above the recording content during [RecordingState.Hold]
     * and [RecordingState.Locked] states.
     *
     * Shows an open lock with a chevron while dragging, and a closed lock once locked.
     * The icon follows the vertical drag offset during Hold.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageComposerAudioRecordingFloatingLockIcon(
        params: MessageComposerAudioRecordingFloatingLockIconParams,
    ) {
        io.getstream.chat.android.compose.ui.messages.composer.internal
            .MessageComposerAudioRecordingFloatingLockIcon(
                isLocked = params.isLocked,
                dragOffsetY = params.dragOffsetY,
            )
    }

    /**
     * The permission rationale displayed as a snackbar when the audio recording permission
     * needs explanation. Shows a message and a "Settings" action button.
     *
     * Override this method to provide a custom permission rationale UI.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageComposerAudioRecordingPermissionRationale(
        params: MessageComposerAudioRecordingPermissionRationaleParams,
    ) {
        StreamSnackbar(snackbarData = params.data)
    }

    /**
     * The content displayed in the message composer while the user is holding to record audio.
     *
     * Shows a mic indicator icon, a live recording timer, and a "slide to cancel" hint that follows
     * the user's drag gesture.
     *
     * Override this method to provide a fully custom hold-to-record UI.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageComposerAudioRecordingHoldContent(params: MessageComposerAudioRecordingHoldContentParams) {
        io.getstream.chat.android.compose.ui.messages.composer.internal.MessageComposerAudioRecordingHoldContent(
            state = params.state,
            modifier = params.modifier,
        )
    }

    /**
     * The content displayed in the message composer when the recording is locked (finger released while
     * recording continues).
     *
     * Shows a mic indicator icon, a live recording timer, a growing waveform visualization, and
     * control buttons (delete, stop, complete) below.
     *
     * Override this method to provide a fully custom locked-recording UI.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageComposerAudioRecordingLockedContent(params: MessageComposerAudioRecordingLockedContentParams) {
        io.getstream.chat.android.compose.ui.messages.composer.internal.MessageComposerAudioRecordingLockedContent(
            state = params.state,
            recordingActions = params.recordingActions,
            modifier = params.modifier,
        )
    }

    /**
     * The content displayed in the message composer when the recording is stopped and the user
     * can scrub the waveform and play back before sending.
     *
     * Shows a play/pause button, a timer, an interactive waveform scrubber, and control buttons
     * (delete, complete) below.
     *
     * Override this method to provide a fully custom recording-overview UI.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageComposerAudioRecordingOverviewContent(
        params: MessageComposerAudioRecordingOverviewContentParams,
    ) {
        io.getstream.chat.android.compose.ui.messages.composer.internal.MessageComposerAudioRecordingOverviewContent(
            state = params.state,
            recordingActions = params.recordingActions,
            modifier = params.modifier,
        )
    }

    /**
     * The "Hold to record" instructional hint displayed as a snackbar when the user taps
     * the record button without holding.
     *
     * Override this method to provide a custom recording hint UI.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageComposerAudioRecordingHint(params: MessageComposerAudioRecordingHintParams) {
        StreamSnackbar(snackbarData = params.data)
    }

    /**
     * The default avatar component that displays an image from a URL or falls back to a placeholder.
     * This component serves as the foundational UI for all avatar types.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun Avatar(params: AvatarParams) {
        io.getstream.chat.android.compose.ui.components.avatar.Avatar(
            modifier = params.modifier,
            imageUrl = params.imageUrl,
            fallback = params.fallback,
            showBorder = params.showBorder,
        )
    }

    /**
     * The default user avatar content.
     *
     * This component displays the user's uploaded image or falls back to their initials if no
     * image is available. It is commonly used in message lists, headers, and user profiles.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun UserAvatar(params: UserAvatarParams) {
        io.getstream.chat.android.compose.ui.components.avatar.UserAvatar(
            modifier = params.modifier,
            user = params.user,
            showIndicator = params.showIndicator,
            showBorder = params.showBorder,
        )
    }

    /**
     * The default avatar for a channel.
     *
     * This component displays the channel image, the user avatar for direct messages, or a placeholder.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun ChannelAvatar(params: ChannelAvatarParams) {
        io.getstream.chat.android.compose.ui.components.avatar.ChannelAvatar(
            modifier = params.modifier,
            channel = params.channel,
            currentUser = params.currentUser,
            showIndicator = params.showIndicator,
            showBorder = params.showBorder,
        )
    }

    /**
     * Factory method for creating the full content of the SelectedChannelMenu.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun ChannelMenu(params: ChannelMenuParams) {
        SelectedChannelMenu(
            modifier = params.modifier,
            selectedChannel = params.selectedChannel,
            currentUser = params.currentUser,
            channelActions = params.channelActions,
            onChannelOptionConfirm = params.onChannelOptionConfirm,
            onDismiss = params.onDismiss,
        )
    }

    /**
     * Factory method for creating the header content of the SelectedChannelMenu.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun ChannelMenuHeaderContent(params: ChannelMenuHeaderContentParams) {
        DefaultSelectedChannelMenuHeaderContent(
            selectedChannel = params.selectedChannel,
            currentUser = params.currentUser,
        )
    }

    /**
     * Factory method for creating the center content of the SelectedChannelMenu.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun ChannelMenuCenterContent(params: ChannelMenuCenterContentParams) {
        ChannelMenuOptions(
            params = ChannelMenuOptionsParams(
                channelActions = params.channelActions,
                onChannelOptionConfirm = params.onChannelOptionConfirm,
                modifier = params.modifier,
            ),
        )
    }

    /**
     * Factory method for creating the options content of the SelectedChannelMenu.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun ChannelMenuOptions(params: ChannelMenuOptionsParams) {
        ChannelOptions(
            actions = params.channelActions,
            onChannelOptionConfirm = params.onChannelOptionConfirm,
            modifier = params.modifier,
        )
    }

    /**
     * Factory method for creating a single channel option item.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun ChannelOptionsItem(params: ChannelOptionsItemParams) {
        val titleColor = if (params.action.isDestructive) {
            ChatTheme.colors.accentError
        } else {
            ChatTheme.colors.textPrimary
        }
        MenuOptionItem(
            params = MenuOptionItemParams(
                modifier = params.modifier.padding(horizontal = StreamTokens.spacingMd),
                title = params.action.label,
                titleColor = titleColor,
                leadingIcon = {
                    ChannelOptionsItemLeadingIcon(ChannelOptionsItemLeadingIconParams(action = params.action))
                },
                onClick = params.onClick,
                style = ChatTheme.typography.bodyDefault,
                itemHeight = 44.dp,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
            ),
        )
    }

    /**
     * Factory method for creating the leading icon of the Channel options menu item.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun ChannelOptionsItemLeadingIcon(params: ChannelOptionsItemLeadingIconParams) {
        val iconColor = if (params.action.isDestructive) {
            ChatTheme.colors.accentError
        } else {
            ChatTheme.colors.textSecondary
        }
        Icon(
            modifier = params.modifier
                .padding(end = StreamTokens.spacingXs)
                .size(StreamTokens.spacingXl),
            painter = painterResource(id = params.action.icon),
            tint = iconColor,
            contentDescription = null,
        )
    }

    /**
     * Factory method for creating the full content of the SelectedMessageMenu.
     * This is the menu that appears when a message is long-pressed.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageMenu(params: MessageMenuParams) {
        SelectedMessageMenu(
            modifier = params.modifier,
            messageOptions = params.messageOptions,
            message = params.message,
            ownCapabilities = params.ownCapabilities,
            onMessageAction = params.onMessageAction,
            onShowMoreReactionsSelected = params.onShowMore,
            onDismiss = params.onDismiss,
            currentUser = params.currentUser,
        )
    }

    /**
     * Factory method for creating the header content of the SelectedMessageMenu.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageMenuHeaderContent(params: MessageMenuHeaderContentParams) {
        MessageMenuHeader(
            modifier = params.modifier,
            onReactionOptionSelected = {
                params.onMessageAction(
                    React(
                        reaction = Reaction(messageId = params.message.id, type = it.type, emojiCode = it.emojiCode),
                        message = params.message,
                    ),
                )
            },
            onShowMoreReactionsSelected = params.onShowMore,
            ownReactions = params.message.ownReactions,
        )
    }

    /**
     * Shows the default message options.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageMenuOptions(params: MessageMenuOptionsParams) {
        MessageOptions(
            modifier = params.modifier,
            onMessageOptionSelected = params.onMessageOptionSelected,
            options = params.options,
        )
    }

    /**
     * Factory method for creating an individual option item in the SelectedMessageMenu.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageMenuOptionsItem(params: MessageMenuOptionsItemParams) {
        ContextualMenuItem(
            label = stringResource(id = params.option.title),
            leadingIcon = params.option.iconPainter,
            destructive = params.option.destructive,
            onClick = { params.onMessageOptionSelected(params.option) },
        )
    }

    /**
     * Factory method for creating a reaction icon. By default, it only displays the emoji.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun ReactionIcon(params: ReactionIconParams) {
        io.getstream.chat.android.compose.ui.components.reactions.ReactionIcon(
            type = params.type,
            emoji = params.emoji,
            size = params.size,
            modifier = params.modifier,
        )
    }

    /**
     * Factory method for creating a reaction toggle. By default, it only displays the emoji.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun ReactionToggle(params: ReactionToggleParams) {
        io.getstream.chat.android.compose.ui.components.reactions.ReactionToggle(
            type = params.type,
            emoji = params.emoji,
            size = params.size,
            checked = params.checked,
            onCheckedChange = params.onCheckedChange,
            modifier = params.modifier,
        )
    }

    /**
     * Factory method for creating the menu displaying all the reactions on a message.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun ReactionsMenu(params: ReactionsMenuParams) {
        io.getstream.chat.android.compose.ui.components.selectedmessage.SelectedReactionsMenu(
            modifier = params.modifier,
            currentUser = params.currentUser,
            message = params.message,
            onMessageAction = params.onMessageAction,
            onShowMoreReactionsSelected = params.onShowMoreReactionsSelected,
            onDismiss = params.onDismiss,
            ownCapabilities = params.ownCapabilities,
        )
    }

    /**
     * Factory method for creating the content of the reactions menu.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun ReactionsMenuContent(params: ReactionsMenuContentParams) {
        io.getstream.chat.android.compose.ui.components.selectedmessage.ReactionsMenuContent(
            modifier = params.modifier,
            currentUser = params.currentUser,
            message = params.message,
            onMessageAction = params.onMessageAction,
            onShowMoreReactionsSelected = params.onShowMoreReactionsSelected,
            ownCapabilities = params.ownCapabilities,
        )
    }

    /**
     * Factory method for creating a single reaction option item in the reactions menu.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun ReactionMenuOptionItem(params: ReactionMenuOptionItemParams) {
        Box(
            modifier = params.modifier.testTag("Stream_Reaction_${params.option.type}"),
            contentAlignment = Alignment.Center,
        ) {
            ReactionToggle(
                params = ReactionToggleParams(
                    type = params.option.type,
                    emoji = params.option.emojiCode,
                    size = ReactionToggleSize.ExtraLarge,
                    checked = params.option.isSelected,
                    onCheckedChange = { params.onReactionOptionSelected(params.option) },
                ),
            )
        }
    }

    /**
     * Factory method for creating the reaction picker bottom sheet.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageReactionPicker(params: MessageReactionPickerParams) {
        ReactionsPicker(
            modifier = params.modifier,
            message = params.message,
            onMessageAction = params.onMessageAction,
            onDismiss = params.onDismiss,
        )
    }

    /**
     * Factory method for creating the content of the reaction picker bottom sheet.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageReactionsPickerContent(params: MessageReactionsPickerContentParams) {
        io.getstream.chat.android.compose.ui.components.reactionpicker.ReactionsPickerContent(
            modifier = params.modifier,
            message = params.message,
            onMessageAction = params.onMessageAction,
        )
    }

    /**
     * Factory method for creating a generic menu option item.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MenuOptionItem(params: MenuOptionItemParams) {
        io.getstream.chat.android.compose.ui.components.common.MenuOptionItem(
            modifier = params.modifier,
            onClick = params.onClick,
            leadingIcon = params.leadingIcon,
            title = params.title,
            titleColor = params.titleColor,
            style = params.style,
            itemHeight = params.itemHeight,
            verticalAlignment = params.verticalAlignment,
            horizontalArrangement = params.horizontalArrangement,
        )
    }

    /**
     * The default thread list banner.
     * Shows unread thread count, a loading indicator during refresh, or an error prompt.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun ThreadListBanner(params: ThreadListBannerParams) {
        io.getstream.chat.android.compose.ui.threads.ThreadListBanner(
            state = params.state,
            onClick = params.onClick,
        )
    }

    /**
     * The default thread list item.
     * Shows information about the Thread title, parent message, last reply and number of unread
     * replies.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun ThreadListItem(params: ThreadListItemParams) {
        ThreadItem(params.thread, params.currentUser, params.onThreadClick)
    }

    /**
     * The default empty placeholder that is displayed when there are no threads.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun ThreadListEmptyContent(params: ThreadListEmptyContentParams) {
        DefaultThreadListEmptyContent(params.modifier)
    }

    /**
     * The default loading content that is displayed during the initial loading of the threads.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun ThreadListLoadingContent(params: ThreadListLoadingContentParams) {
        DefaultThreadListLoadingContent(params.modifier)
    }

    /**
     * The default content shown on the bottom of the list during the loading of more threads.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun ThreadListLoadingMoreContent(params: ThreadListLoadingMoreContentParams) {
        DefaultThreadListLoadingMoreContent()
    }

    /**
     * The default content of the pinned message list item.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun PinnedMessageListItem(params: PinnedMessageListItemParams) {
        PinnedMessageItem(params.message, params.currentUser, params.onClick)
    }

    /**
     * The default leading content of the pinned message list item. Shows an avatar of the user who sent the pinned
     * message.
     *
     * Used in the [PinnedMessageListItem].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun RowScope.PinnedMessageListItemLeadingContent(params: PinnedMessageListItemLeadingContentParams) {
        DefaultMessagePreviewItemLeadingContent(params.message, params.currentUser)
    }

    /**
     * The default center content of the pinned message list item. Shows the message sender name and the message
     * content.
     *
     * Used in the [PinnedMessageListItem].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun RowScope.PinnedMessageListItemCenterContent(params: PinnedMessageListItemCenterContentParams) {
        DefaultMessagePreviewItemCenterContent(params.message, params.currentUser)
    }

    /**
     * The default trailing content of the pinned message list item. Shows the message timestamp.
     *
     * Used in the [PinnedMessageListItem].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun RowScope.PinnedMessageListItemTrailingContent(params: PinnedMessageListItemTrailingContentParams) {
        DefaultMessagePreviewItemTrailingContent(params.message)
    }

    /**
     * The default divider appended after each pinned message.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun PinnedMessageListItemDivider(params: PinnedMessageListItemDividerParams) {
        StreamHorizontalDivider()
    }

    /**
     * The default empty placeholder that is displayed when there are no pinned messages.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun PinnedMessageListEmptyContent(params: PinnedMessageListEmptyContentParams) {
        DefaultPinnedMessageListEmptyContent(params.modifier)
    }

    /**
     * The default loading content that is displayed during the initial loading of the pinned messages.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun PinnedMessageListLoadingContent(params: PinnedMessageListLoadingContentParams) {
        DefaultPinnedMessageListLoadingContent(params.modifier)
    }

    /**
     * The default content shown on the bottom of the list during the loading of more pinned messages.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun PinnedMessageListLoadingMoreContent(params: PinnedMessageListLoadingMoreContentParams) {
        DefaultPinnedMessageListLoadingMoreContent()
    }

    /**
     * The default content shown when swiping to reply to a message.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun RowScope.SwipeToReplyContent(params: SwipeToReplyContentParams) {
        SwipeToReplyIcon()
    }

    /**
     * The default content of a mention list item.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun LazyItemScope.MentionListItem(params: MentionListItemParams) {
        SearchResultItem(
            searchResultItemState = remember {
                ItemState.SearchResultItemState(
                    message = params.mention.message,
                    channel = params.mention.channel,
                )
            },
            currentUser = params.currentUser,
            modifier = params.modifier.animateItem(),
            onSearchResultClick = params.onClick,
        )
    }

    /**
     * The default loading indicator that is displayed during the initial loading of the mention list.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun BoxScope.MentionListLoadingIndicator(params: MentionListLoadingIndicatorParams) {
        LoadingIndicator(
            modifier = params.modifier,
        )
    }

    /**
     * The default empty placeholder that is displayed when the mention list is empty.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun BoxScope.MentionListEmptyContent(params: MentionListEmptyContentParams) {
        EmptyContent(
            text = stringResource(UiCommonR.string.stream_ui_mention_list_empty),
            painter = painterResource(UiCommonR.drawable.stream_compose_ic_mentions),
            modifier = params.modifier,
        )
    }

    /**
     * The default loading indicator that is displayed on the bottom of the list when there are more mentions loading.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun LazyItemScope.MentionListLoadingItem(params: MentionListLoadingItemParams) {
        LoadingFooter(
            modifier = params.modifier.fillMaxWidth(),
        )
    }

    /**
     * The default pull-to-refresh indicator for the mention list.
     *
     * @param params Parameters for this component.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    public fun BoxScope.MentionListPullToRefreshIndicator(params: MentionListPullToRefreshIndicatorParams) {
        PullToRefreshDefaults.Indicator(
            state = params.pullToRefreshState,
            isRefreshing = params.isRefreshing,
            modifier = params.modifier.align(Alignment.TopCenter),
            containerColor = ChatTheme.colors.backgroundCoreElevation1,
            color = ChatTheme.colors.accentPrimary,
        )
    }

    /**
     * Renders all selected attachments in the message composer as a single horizontal scrolling row.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageComposerAttachments(params: MessageComposerAttachmentsParams) {
        MessageComposerAttachments(
            modifier = params.modifier,
            attachments = params.attachments,
            onAttachmentRemoved = params.onAttachmentRemoved,
        )
    }

    /**
     * Renders a single audio recording attachment item in the message composer tray.
     *
     * Used as part of [MessageComposerAttachments].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageComposerAttachmentAudioRecordItem(params: MessageComposerAttachmentAudioRecordItemParams) {
        MessageComposerAttachmentAudioRecordItem(
            modifier = params.modifier,
            attachment = params.attachment,
            playerState = params.playerState,
            onPlayToggleClick = params.onPlayToggleClick,
            onPlaySpeedClick = params.onPlaySpeedClick,
            onThumbDragStart = params.onThumbDragStart,
            onThumbDragStop = params.onThumbDragStop,
            onAttachmentRemoved = params.onAttachmentRemoved,
        )
    }

    /**
     * Renders a single media (image or video) attachment item in the message composer tray.
     *
     * Used as part of [MessageComposerAttachments].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageComposerAttachmentMediaItem(params: MessageComposerAttachmentMediaItemParams) {
        MessageComposerAttachmentMediaItem(
            modifier = params.modifier,
            attachment = params.attachment,
            onAttachmentRemoved = params.onAttachmentRemoved,
        )
    }

    /**
     * Renders a single generic file attachment item in the message composer tray.
     *
     * Used as part of [MessageComposerAttachments].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MessageComposerAttachmentFileItem(params: MessageComposerAttachmentFileItemParams) {
        MessageComposerAttachmentFileItem(
            modifier = params.modifier,
            attachment = params.attachment,
            onAttachmentRemoved = params.onAttachmentRemoved,
        )
    }

    /**
     * Factory method for creating the content of audio recording attachments in a message.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun AudioRecordAttachmentContent(params: AudioRecordAttachmentContentParams) {
        val viewModelFactory = remember {
            AudioPlayerViewModelFactory(
                getAudioPlayer = { ChatClient.instance().audioPlayer },
                getRecordingUri = { it.assetUrl ?: it.upload?.toUri()?.toString() },
            )
        }
        io.getstream.chat.android.compose.ui.attachments.content.AudioRecordAttachmentContent(
            modifier = params.modifier,
            attachmentState = params.state,
            viewModelFactory = viewModelFactory,
        )
    }

    /**
     * Factory method for creating the content of file attachments in a message.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun FileAttachmentContent(params: FileAttachmentContentParams) {
        io.getstream.chat.android.compose.ui.attachments.content.FileAttachmentContent(
            modifier = params.modifier,
            attachmentState = params.state,
            showFileSize = { true },
            onItemClick = ::onFileAttachmentContentItemClick,
        )
    }

    /**
     * Factory method for creating the content of Giphy attachments in a message.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun GiphyAttachmentContent(params: GiphyAttachmentContentParams) {
        io.getstream.chat.android.compose.ui.attachments.content.GiphyAttachmentContent(
            state = params.state,
            modifier = params.modifier,
        )
    }

    /**
     * Factory method for creating the content of link attachments in a message.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun LinkAttachmentContent(params: LinkAttachmentContentParams) {
        io.getstream.chat.android.compose.ui.attachments.content.LinkAttachmentContent(
            state = params.state,
            modifier = params.modifier,
        )
    }

    /**
     * Factory method for creating the content of media attachments in a message.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun MediaAttachmentContent(params: MediaAttachmentContentParams) {
        io.getstream.chat.android.compose.ui.attachments.content.MediaAttachmentContent(
            state = params.state,
            modifier = params.modifier,
        )
    }

    /**
     * Factory method for creating the content of custom attachments in a message.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun CustomAttachmentContent(params: CustomAttachmentContentParams) {
        UnsupportedAttachmentContent(state = params.state, modifier = params.modifier)
    }

    /**
     * Factory method for creating a file attachment item.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun FileAttachmentItem(params: FileAttachmentItemParams) {
        io.getstream.chat.android.compose.ui.attachments.content.FileAttachmentItem(
            attachment = params.attachment,
            isMine = params.isMine,
            showFileSize = params.showFileSize,
            modifier = params.modifier,
        )
    }

    /**
     * Factory method for creating the top bar of the channel info screen.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun DirectChannelInfoTopBar(params: DirectChannelInfoTopBarParams) {
        io.getstream.chat.android.compose.ui.channel.info.DirectChannelInfoTopBar(
            onNavigationIconClick = params.onNavigationIconClick,
        )
    }

    /**
     * Factory method for creating the avatar container in the direct channel info screen.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun DirectChannelInfoAvatarContainer(params: DirectChannelInfoAvatarContainerParams) {
        io.getstream.chat.android.compose.ui.channel.info.DirectChannelInfoAvatarContainer(
            user = params.user,
        )
    }

    /**
     * Factory method for creating the avatar container in the group channel info screen.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun GroupChannelInfoAvatarContainer(params: GroupChannelInfoAvatarContainerParams) {
        io.getstream.chat.android.compose.ui.channel.info.GroupChannelInfoAvatarContainer(
            channel = params.channel,
            currentUser = params.currentUser,
            members = params.members,
        )
    }

    /**
     * Factory method for creating the member section card in the group channel info screen.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun GroupChannelInfoMemberSection(params: GroupChannelInfoMemberSectionParams) {
        io.getstream.chat.android.compose.ui.channel.info.GroupChannelInfoMemberSection(
            members = params.members,
            currentUser = params.currentUser,
            owner = params.owner,
            totalMemberCount = params.totalMemberCount,
            showAddButton = params.showAddButton,
            onAddMembersClick = params.onAddMembersClick,
            onViewAction = params.onViewAction,
        )
    }

    /**
     * Factory method for creating the top bar of the group channel info screen.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun GroupChannelInfoTopBar(params: GroupChannelInfoTopBarParams) {
        io.getstream.chat.android.compose.ui.channel.info.GroupChannelInfoTopBar(
            headerState = params.headerState,
            infoState = params.infoState,
            listState = params.listState,
            onNavigationIconClick = params.onNavigationIconClick,
            onAddMembersClick = params.onAddMembersClick,
        )
    }

    /**
     * Factory method for creating the "Add members" button of the group channel info screen.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun GroupChannelInfoAddMembersButton(params: GroupChannelInfoAddMembersButtonParams) {
        IconButton(onClick = params.onClick) {
            Icon(
                painter = painterResource(R.drawable.stream_ic_member_add),
                contentDescription = stringResource(R.string.stream_ui_channel_info_member_add_button),
            )
        }
    }

    /**
     * Factory method for creating the channel info separator item.
     * This is used to visually separate different sections in the channel info screens.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun LazyItemScope.ChannelInfoSeparatorItem(params: ChannelInfoSeparatorItemParams) {
        StreamHorizontalDivider(thickness = 8.dp)
    }

    /**
     * Factory method for creating the channel info option item used in direct and group channel info screens.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun LazyItemScope.ChannelInfoOptionItem(params: ChannelInfoOptionItemParams) {
        DefaultChannelInfoOptionItem(
            option = params.option,
            isGroupChannel = params.isGroupChannel,
            onViewAction = params.onViewAction,
        )
    }

    /**
     * Factory method for creating the member item in the group channel info screen.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun GroupChannelInfoMemberItem(params: GroupChannelInfoMemberItemParams) {
        io.getstream.chat.android.compose.ui.channel.info.GroupChannelInfoMemberItem(
            modifier = Modifier,
            currentUser = params.currentUser,
            member = params.member,
            isOwner = params.isOwner,
            onClick = params.onClick,
        )
    }

    /**
     * Factory method for creating the expand members item in the group channel info screen.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun GroupChannelInfoExpandMembersItem(params: GroupChannelInfoExpandMembersItemParams) {
        io.getstream.chat.android.compose.ui.channel.info.GroupChannelInfoExpandMembersItem(
            collapsedCount = params.collapsedCount,
            onClick = params.onClick,
        )
    }

    /**
     * Factory method for creating the channel info screen modal.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun ChannelInfoScreenModal(params: ChannelInfoScreenModalParams) {
        io.getstream.chat.android.compose.ui.channel.info.ChannelInfoScreenModal(
            modal = params.modal,
            isGroupChannel = params.isGroupChannel,
            onViewAction = params.onViewAction,
            onMemberViewEvent = params.onMemberViewEvent,
            onDismiss = params.onDismiss,
        )
    }

    /**
     * Factory method for creating the top bar of the member info modal sheet in the group channel info screen.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun ChannelInfoMemberInfoModalSheetTopBar(params: ChannelInfoMemberInfoModalSheetTopBarParams) {
        io.getstream.chat.android.compose.ui.channel.info.ChannelInfoMemberInfoModalSheetTopBar(
            member = params.member,
        )
    }

    /**
     * Factory method for creating a single member option item.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun ChannelInfoMemberOptionItem(params: ChannelInfoMemberOptionItemParams) {
        io.getstream.chat.android.compose.ui.channel.info.ChannelInfoMemberOptionItem(
            action = params.action,
        )
    }

    /**
     * Factory method for creating the top bar of the channel files attachments screen.
     *
     * @param params Parameters for this component.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    public fun ChannelFilesAttachmentsTopBar(params: ChannelFilesAttachmentsTopBarParams) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.stream_ui_channel_attachments_files_title),
                    style = ChatTheme.typography.headingSmall,
                    maxLines = 1,
                )
            },
            navigationIcon = { ChannelInfoNavigationIcon(onClick = params.onNavigationIconClick) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = ChatTheme.colors.backgroundCoreElevation1,
                titleContentColor = ChatTheme.colors.textSecondary,
            ),
        )
    }

    /**
     * Factory method for creating the loading indicator of the channel files attachments screen.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun ChannelFilesAttachmentsLoadingIndicator(params: ChannelFilesAttachmentsLoadingIndicatorParams) {
        LoadingIndicator(modifier = params.modifier)
    }

    /**
     * Factory method for creating the empty content of the channel files attachments screen.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun BoxScope.ChannelFilesAttachmentsEmptyContent(params: ChannelFilesAttachmentsEmptyContentParams) {
        EmptyContent(
            modifier = params.modifier,
            title = stringResource(UiCommonR.string.stream_ui_channel_attachments_files_empty_title),
            text = stringResource(UiCommonR.string.stream_ui_channel_attachments_files_empty_text),
            painter = painterResource(R.drawable.stream_compose_ic_files),
        )
    }

    /**
     * The default error content of the channel files attachments screen.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun BoxScope.ChannelFilesAttachmentsErrorContent(params: ChannelFilesAttachmentsErrorContentParams) {
        EmptyContent(
            modifier = params.modifier,
            text = stringResource(UiCommonR.string.stream_ui_channel_attachments_files_load_error),
            painter = rememberVectorPainter(Icons.TwoTone.Warning),
        )
    }

    /**
     * Factory method for creating the channel files attachments header item.
     * This is typically used to display the title of a group of attachments.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun LazyItemScope.ChannelFilesAttachmentsHeaderItem(params: ChannelFilesAttachmentsHeaderItemParams) {
        Text(
            modifier = params.modifier
                .fillMaxWidth()
                .padding(start = StreamTokens.spacing2xs)
                .background(ChatTheme.colors.backgroundCoreSurfaceSubtle)
                .topBorder(color = ChatTheme.colors.borderCoreSubtle)
                .bottomBorder(color = ChatTheme.colors.borderCoreSubtle)
                .padding(horizontal = StreamTokens.spacingMd, vertical = StreamTokens.spacingXs),
            text = params.label,
            style = ChatTheme.typography.captionEmphasis,
            color = ChatTheme.colors.chatTextSystem,
        )
    }

    /**
     * Factory method for creating the channel files attachments item.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun LazyItemScope.ChannelFilesAttachmentsItem(params: ChannelFilesAttachmentsItemParams) {
        DefaultChannelFilesAttachmentsItem(
            modifier = params.modifier.animateItem(),
            item = params.item,
            currentUser = params.currentUser,
            onClick = params.onClick,
        )
    }

    /**
     * Factory method for creating a divider between channel files attachments items.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun LazyItemScope.ChannelFilesAttachmentsItemDivider(params: ChannelFilesAttachmentsItemDividerParams) {
        StreamHorizontalDivider()
    }

    /**
     * Factory method for creating the loading item in the channel files attachments list.
     *
     * This is typically shown at the end of the list when more items are being loaded.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun LazyItemScope.ChannelFilesAttachmentsLoadingItem(params: ChannelFilesAttachmentsLoadingItemParams) {
        LoadingFooter(modifier = params.modifier.fillMaxWidth())
    }

    /**
     * Factory method for creating the top bar of the channel media attachments screen.
     *
     * @param params Parameters for this component.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    public fun ChannelMediaAttachmentsTopBar(params: ChannelMediaAttachmentsTopBarParams) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.stream_ui_channel_attachments_media_title),
                    style = ChatTheme.typography.headingSmall,
                    maxLines = 1,
                )
            },
            navigationIcon = { ChannelInfoNavigationIcon(onClick = params.onNavigationIconClick) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = ChatTheme.colors.backgroundCoreElevation1,
                titleContentColor = ChatTheme.colors.textSecondary,
            ),
        )
    }

    /**
     * Factory method for creating the loading indicator of the channel media attachments screen.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun ChannelMediaAttachmentsLoadingIndicator(params: ChannelMediaAttachmentsLoadingIndicatorParams) {
        LoadingIndicator(modifier = params.modifier)
    }

    /**
     * Factory method for creating the empty content of the channel media attachments screen.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun BoxScope.ChannelMediaAttachmentsEmptyContent(params: ChannelMediaAttachmentsEmptyContentParams) {
        EmptyContent(
            modifier = params.modifier,
            title = stringResource(UiCommonR.string.stream_ui_channel_attachments_media_empty_title),
            text = stringResource(UiCommonR.string.stream_ui_channel_attachments_media_empty_text),
            painter = painterResource(R.drawable.stream_compose_ic_media),
        )
    }

    /**
     * The default error content of the channel media attachments screen.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun BoxScope.ChannelMediaAttachmentsErrorContent(params: ChannelMediaAttachmentsErrorContentParams) {
        EmptyContent(
            modifier = params.modifier,
            text = stringResource(UiCommonR.string.stream_ui_channel_attachments_media_load_error),
            painter = rememberVectorPainter(Icons.TwoTone.Warning),
        )
    }

    /**
     * Factory method for creating the channel media attachments floating header.
     * This is typically used to display the title of a group of attachments.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun BoxScope.ChannelMediaAttachmentsFloatingHeader(params: ChannelMediaAttachmentsFloatingHeaderParams) {
        FadingVisibility(
            modifier = params.modifier.align(Alignment.TopCenter),
            visible = params.visible,
        ) {
            Text(
                modifier = Modifier
                    .padding(StreamTokens.spacingMd)
                    .background(
                        color = ChatTheme.colors.backgroundCoreInverse,
                        shape = RoundedCornerShape(StreamTokens.radiusFull),
                    )
                    .padding(horizontal = StreamTokens.spacingSm, vertical = StreamTokens.spacingXs),
                text = params.label,
                style = ChatTheme.typography.captionEmphasis,
                color = ChatTheme.colors.textInverse,
            )
        }
    }

    /**
     * Factory method for creating the channel media attachments item.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun LazyGridItemScope.ChannelMediaAttachmentsItem(params: ChannelMediaAttachmentsItemParams) {
        DefaultChannelMediaAttachmentsItem(
            modifier = params.modifier.animateItem(),
            item = params.item,
            onClick = params.onClick,
        )
    }

    /**
     * Factory method for creating the loading item in the channel media attachments list.
     *
     * This is typically shown at the end of the list when more items are being loaded.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun LazyGridItemScope.ChannelMediaAttachmentsLoadingItem(params: ChannelMediaAttachmentsLoadingItemParams) {
        Box(
            modifier = params.modifier.aspectRatio(1f),
            contentAlignment = Alignment.Center,
        ) {
            @Suppress("MagicNumber")
            CircularProgressIndicator(
                modifier = params.modifier.fillMaxSize(.25f),
                strokeWidth = 2.dp,
                color = ChatTheme.colors.accentPrimary,
            )
        }
    }

    /**
     * Factory method for creating the top bar of the channel media attachments preview screen.
     *
     * @param params Parameters for this component.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    public fun ChannelMediaAttachmentsPreviewTopBar(params: ChannelMediaAttachmentsPreviewTopBarParams) {
        CenterAlignedTopAppBar(
            modifier = Modifier.bottomBorder(ChatTheme.colors.borderCoreSubtle),
            title = {
                ChannelMediaAttachmentsPreviewTopBarTitle(
                    params = ChannelMediaAttachmentsPreviewTopBarTitleParams(item = params.item),
                )
            },
            navigationIcon = {
                StreamButton(
                    modifier = Modifier.minimumInteractiveComponentSize(),
                    onClick = params.onNavigationIconClick,
                    style = StreamButtonStyleDefaults.secondaryGhost,
                    size = StreamButtonSize.Medium,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.stream_compose_ic_arrow_back),
                        contentDescription = stringResource(id = R.string.stream_compose_cancel),
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = ChatTheme.colors.backgroundCoreElevation1,
                titleContentColor = ChatTheme.colors.textPrimary,
            ),
        )
    }

    /**
     * Factory method for creating the title of the channel media attachments preview top bar.
     * This displays the message sender's name and the time when the message was sent.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun ChannelMediaAttachmentsPreviewTopBarTitle(params: ChannelMediaAttachmentsPreviewTopBarTitleParams) {
        val dateFormatter = ChatTheme.dateFormatter
        val title = params.item.message.user.name
        val subtitle = dateFormatter.formatRelativeTime(params.item.message.getCreatedAtOrThrow())
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = ChatTheme.typography.headingSmall,
                color = ChatTheme.colors.textPrimary,
                maxLines = 1,
            )
            Text(
                text = subtitle,
                style = ChatTheme.typography.captionDefault,
                color = ChatTheme.colors.textSecondary,
                maxLines = 1,
            )
        }
    }

    /**
     * Factory method for creating the bottom bar of the channel media attachments preview screen.
     *
     * @param params Parameters for this component.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    public fun ChannelMediaAttachmentsPreviewBottomBar(
        params: ChannelMediaAttachmentsPreviewBottomBarParams,
    ) {
        Column(
            modifier = Modifier
                .background(ChatTheme.colors.backgroundCoreElevation1)
                .topBorder(ChatTheme.colors.borderCoreDefault),
        ) {
            params.topContent?.invoke()
            CenterAlignedTopAppBar(
                title = { params.centerContent() },
                navigationIcon = { params.leadingContent() },
                actions = { params.trailingContent() },
                windowInsets = BottomAppBarDefaults.windowInsets,
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = ChatTheme.colors.backgroundCoreElevation1,
                    titleContentColor = ChatTheme.colors.textPrimary,
                    navigationIconContentColor = ChatTheme.colors.textPrimary,
                    actionIconContentColor = ChatTheme.colors.textPrimary,
                ),
            )
        }
    }

    /**
     * Container component that manages the attachment picker's visibility and animations.
     *
     * Override this to customize the picker container behavior, including animations,
     * keyboard coordination, and composer integration.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun AttachmentPickerMenu(params: AttachmentPickerMenuParams) {
        io.getstream.chat.android.compose.ui.messages.attachments.AttachmentPickerMenu(
            attachmentsPickerViewModel = params.attachmentsPickerViewModel,
            composerViewModel = params.composerViewModel,
        )
    }

    /**
     * Main attachment picker component with mode tabs and content area.
     *
     * Override this to customize the overall picker layout. The picker automatically switches
     * between system picker and in-app picker based on [ChatTheme.attachmentPickerConfig].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun AttachmentPicker(params: AttachmentPickerParams) {
        io.getstream.chat.android.compose.ui.messages.attachments.AttachmentPicker(
            modifier = params.modifier,
            attachmentsPickerViewModel = params.attachmentsPickerViewModel,
            messageMode = params.messageMode,
            actions = params.actions,
        )
    }

    /**
     * Tab bar for the in-app attachment picker showing toggle buttons for each mode.
     *
     * Override this to customize the tab bar appearance or add custom tabs. The available
     * modes are determined by [ChatTheme.attachmentPickerConfig.modes].
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun AttachmentTypePicker(params: AttachmentTypePickerParams) {
        io.getstream.chat.android.compose.ui.messages.attachments.AttachmentTypePicker(
            channel = params.channel,
            messageMode = params.messageMode,
            selectedMode = params.selectedMode,
            onModeSelected = params.onModeSelected,
            trailingContent = params.trailingContent,
        )
    }

    /**
     * Button bar for the system attachment picker showing action buttons for each mode.
     *
     * Unlike [AttachmentTypePicker], each button directly launches the corresponding system picker
     * rather than switching to an in-app content view. No storage permissions are required.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun AttachmentTypeSystemPicker(params: AttachmentTypeSystemPickerParams) {
        io.getstream.chat.android.compose.ui.messages.attachments.AttachmentTypeSystemPicker(
            channel = params.channel,
            messageMode = params.messageMode,
            onModeSelected = params.onModeSelected,
            trailingContent = params.trailingContent,
        )
    }

    /**
     * Content router that displays the appropriate picker UI based on the current mode.
     *
     * Override this to add support for custom [AttachmentPickerMode] implementations or
     * to customize how modes are rendered.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun AttachmentPickerContent(params: AttachmentPickerContentParams) {
        io.getstream.chat.android.compose.ui.messages.attachments.AttachmentPickerContent(
            pickerMode = params.pickerMode,
            commands = params.commands,
            attachments = params.attachments,
            onLoadAttachments = params.onLoadAttachments,
            onUrisSelected = params.onUrisSelected,
            actions = params.actions,
            onAttachmentsSubmitted = params.onAttachmentsSubmitted,
        )
    }

    /**
     * Grid picker for selecting images and videos from device storage.
     *
     * Shows a scrollable grid of media thumbnails with selection badges. Requires storage
     * permissions to display content.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun AttachmentMediaPicker(params: AttachmentMediaPickerParams) {
        io.getstream.chat.android.compose.ui.messages.attachments.AttachmentMediaPicker(
            pickerMode = params.pickerMode,
            attachments = params.attachments,
            onLoadAttachments = params.onLoadAttachments,
            onAttachmentItemSelected = params.onAttachmentItemSelected,
        )
    }

    /**
     * Camera capture interface for taking photos or recording videos.
     *
     * Displays a button that launches the device camera. Captured media is automatically
     * submitted as an attachment.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun AttachmentCameraPicker(params: AttachmentCameraPickerParams) {
        io.getstream.chat.android.compose.ui.messages.attachments.AttachmentCameraPicker(
            pickerMode = params.pickerMode,
            onAttachmentsSubmitted = params.onAttachmentsSubmitted,
        )
    }

    /**
     * List picker for selecting files from device storage.
     *
     * Shows a scrollable list of files (documents, audio, etc.) with file type icons,
     * names, and sizes. Requires storage permissions to display content.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun AttachmentFilePicker(params: AttachmentFilePickerParams) {
        io.getstream.chat.android.compose.ui.messages.attachments.AttachmentFilePicker(
            pickerMode = params.pickerMode,
            attachments = params.attachments,
            onLoadAttachments = params.onLoadAttachments,
            onAttachmentItemSelected = params.onAttachmentItemSelected,
            onUrisSelected = params.onUrisSelected,
        )
    }

    /**
     * Poll creation entry point in the attachment picker.
     *
     * Shows a button or automatically opens the poll creation dialog based on [PollPickerMode.autoShowCreateDialog].
     * Poll creation is only available when the channel has the "polls" capability.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun AttachmentPollPicker(params: AttachmentPollPickerParams) {
        io.getstream.chat.android.compose.ui.messages.attachments.AttachmentPollPicker(
            pickerMode = params.pickerMode,
            onCreatePollClick = params.onCreatePollClick,
            onCreatePoll = params.onCreatePoll,
            onCreatePollDismissed = params.onCreatePollDismissed,
        )
    }

    /**
     * Slash command picker showing available commands.
     *
     * Displays a scrollable list of commands configured for the channel (e.g., /giphy, /mute).
     * Tapping a command inserts it into the message composer.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun AttachmentCommandPicker(params: AttachmentCommandPickerParams) {
        io.getstream.chat.android.compose.ui.messages.attachments.AttachmentCommandPicker(
            pickerMode = params.pickerMode,
            commands = params.commands,
            onCommandSelected = params.onCommandSelected,
        )
    }

    /**
     * System picker variant that uses native OS pickers instead of in-app UI.
     *
     * Shows a row of buttons that launch system pickers (photo picker, file browser, camera).
     * This variant does not require storage permissions since it uses system intents.
     * Used when [ChatTheme.config.attachmentPicker.useSystemPicker] is `true`.
     *
     * @param params Parameters for this component.
     */
    @Composable
    public fun AttachmentSystemPicker(params: AttachmentSystemPickerParams) {
        io.getstream.chat.android.compose.ui.messages.attachments.AttachmentSystemPicker(
            channel = params.channel,
            messageMode = params.messageMode,
            attachments = params.attachments,
            actions = params.actions,
            onUrisSelected = params.onUrisSelected,
            onAttachmentsSubmitted = params.onAttachmentsSubmitted,
        )
    }
}
