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
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.getstream.chat.android.compose.state.channels.list.ItemState
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
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User

/**
 * Factory for creating stateless components that are used by default throughout the Chat UI.
 *
 * Example of a custom [ChatComponentFactory] implementation that changes the default UI of the trailing content
 * of the channel list header element:
 *
 * ```kotlin
 * ChatTheme(
 *     componentFactory = ComponentFactory(
 *         channelListHeader = object : ChannelListHeader() {
 *             @Composable
 *             override fun RowScope.TrailingContent(
 *                 onHeaderActionClick: () -> Unit,
 *             ) {
 *                 IconButton(onClick = onHeaderActionClick) {
 *                     Icon(
 *                         imageVector = Icons.Default.Add,
 *                         contentDescription = "Add",
 *                     )
 *                 }
 *             }
 *         }
 *     )
 * ) {
 *     // Your Chat screens
 * }
 * ```
 *
 * [ChatComponentFactory] can also be extended in a separate class and passed to the [ChatTheme] as shown:
 *
 * ```kotlin
 * class MyComponentFactory : ComponentFactory(
 *     channelListHeader = object : ChannelListHeader() {
 *         @Composable
 *         override fun RowScope.TrailingContent(
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
 * )
 *
 * ChatTheme(
 *     componentFactory = MyComponentFactory()
 * ) {
 *     // Your Chat screens
 * }
 * ```
 *
 * @param channelListHeader The default UI elements for the header of the channel list component.
 * @param channelList The default UI elements for the channel list component.
 * @param searchInput The default UI elements for the search input component.
 */
public open class ChatComponentFactory(
    public val channelListHeader: ChannelListHeader = ChannelListHeader(),
    public val channelList: ChannelList = ChannelList(),
    public val searchInput: SearchInput = SearchInput(),
) {

    /**
     * The default UI elements for the header of the channel list component.
     */
    public open class ChannelListHeader {

        /**
         * The default leading content.
         */
        @Composable
        public open fun RowScope.LeadingContent(
            currentUser: User?,
            onAvatarClick: (User?) -> Unit,
        ) {
            DefaultChannelHeaderLeadingContent(
                currentUser = currentUser,
                onAvatarClick = onAvatarClick,
            )
        }

        /**
         * The default center content.
         */
        @Composable
        public open fun RowScope.CenterContent(
            connectionState: ConnectionState,
            title: String,
        ) {
            DefaultChannelListHeaderCenterContent(
                connectionState = connectionState,
                title = title,
            )
        }

        /**
         * The default trailing content.
         */
        @Composable
        public open fun RowScope.TrailingContent(
            onHeaderActionClick: () -> Unit,
        ) {
            DefaultChannelListHeaderTrailingContent(
                onHeaderActionClick = onHeaderActionClick,
            )
        }
    }

    /**
     * The default UI elements for the channel list component.
     */
    public open class ChannelList {

        /**
         * The default loading content, when the initial channel list is loading.
         */
        @Composable
        public open fun LoadingContent(modifier: Modifier) {
            DefaultChannelListLoadingIndicator(
                modifier = modifier,
            )
        }

        /**
         * The default empty content, when the channel list is empty.
         */
        @Composable
        public open fun EmptyContent(modifier: Modifier) {
            DefaultChannelListEmptyContent(
                modifier = modifier,
            )
        }

        /**
         * The default channel item content.
         */
        @Composable
        public open fun LazyItemScope.ChannelContent(
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
         * The default leading content for the channel item.
         * Usually the avatar that holds an image of the channel or its members.
         */
        @Composable
        public open fun RowScope.ChannelItemLeadingContent(
            channelItem: ItemState.ChannelItemState,
            currentUser: User?,
        ) {
            DefaultChannelItemLeadingContent(
                channelItem = channelItem,
                currentUser = currentUser,
            )
        }

        /**
         * The default center content for the channel item.
         * Usually the name of the channel and the last message.
         */
        @Composable
        public open fun RowScope.ChannelItemCenterContent(
            channelItem: ItemState.ChannelItemState,
            currentUser: User?,
        ) {
            DefaultChannelItemCenterContent(
                channelItemState = channelItem,
                currentUser = currentUser,
            )
        }

        /**
         * The default trailing content for the channel item.
         * Usually the last message and the number of unread messages.
         */
        @Composable
        public open fun RowScope.ChannelItemTrailingContent(
            channelItem: ItemState.ChannelItemState,
            currentUser: User?,
        ) {
            DefaultChannelItemTrailingContent(
                channel = channelItem.channel,
                currentUser = currentUser,
            )
        }

        /**
         * The default search result content.
         */
        @Composable
        public open fun LazyItemScope.SearchResultContent(
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
         * The default empty search content, when there are no matching search results.
         */
        @Composable
        public open fun EmptySearchContent(
            modifier: Modifier,
            searchQuery: String,
        ) {
            DefaultChannelSearchEmptyContent(
                modifier = modifier,
                searchQuery = searchQuery,
            )
        }

        /**
         * The default helper content rendered at the top of the channel list.
         * It's empty by default and can be used to implement a scroll to top feature.
         */
        @Composable
        public open fun BoxScope.HelperContent() {
        }

        /**
         * The default loading more content, when the next page of channels is loading.
         */
        @Composable
        public open fun LazyItemScope.LoadingMoreContent() {
            DefaultChannelsLoadingMoreIndicator()
        }

        /**
         * The default divider between channel items.
         */
        @Composable
        public open fun LazyItemScope.Divider() {
            DefaultChannelItemDivider()
        }
    }

    /**
     * The default UI elements for the search input component.
     */
    public open class SearchInput {

        /**
         * The default leading icon.
         */
        @Composable
        public open fun RowScope.LeadingIcon() {
            DefaultSearchLeadingIcon()
        }

        /**
         * The default label.
         */
        @Composable
        public open fun Label() {
            DefaultSearchLabel()
        }
    }
}
