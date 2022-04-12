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

package io.getstream.chat.android.compose.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Contains all the dimens we provide for our components.
 *
 * @param channelItemVerticalPadding The vertical content padding inside channel list item.
 * @param channelItemHorizontalPadding The horizontal content padding inside channel list item.
 * @param channelAvatarSize The size of channel avatar.
 * @param selectedChannelMenuUserItemWidth The width of a member tile in the selected channel menu.
 * @param selectedChannelMenuUserItemHorizontalPadding The padding inside a member tile in the selected channel menu.
 * @param selectedChannelMenuUserItemAvatarSize The size of a member avatar in the selected channel menu.
 * @param attachmentsContentImageWidth The width of image attachments in the message list.
 * @param attachmentsContentImageGridSpacing The spacing between image tiles in the message list.
 * @param attachmentsContentImageHeight The height of image attachments in the message list.
 * @param attachmentsContentGiphyWidth The with of Giphy attachments in the message list.
 * @param attachmentsContentGiphyHeight The height of Giphy attachments in the message list.
 * @param attachmentsContentLinkWidth The with of link attachments in the message list.
 * @param attachmentsContentFileWidth The width of file attachments in the message list.
 * @param attachmentsContentFileUploadWidth The width of uploading file attachments in the message list.
 * @param threadSeparatorVerticalPadding The vertical content padding inside thread separator item.
 * @param threadSeparatorTextVerticalPadding The vertical padding inside thread separator text.
 * @param messageOptionsItemHeight The height of a message option item.
 * @param suggestionListMaxHeight The maximum height of the suggestion list popup.
 * @param suggestionListPadding The outer padding of the suggestion list popup.
 * @param suggestionListElevation THe elevation of the suggestion list popup.
 * @param mentionSuggestionItemHorizontalPadding The horizontal content padding inside mention list item.
 * @param mentionSuggestionItemVerticalPadding The vertical content padding inside mention list item.
 * @param mentionSuggestionItemAvatarSize The size of a channel avatar in the suggestion list popup.
 * @param commandSuggestionItemHorizontalPadding The horizontal content padding inside command list item.
 * @param commandSuggestionItemVerticalPadding The vertical content padding inside command list item.
 * @param commandSuggestionItemIconSize The size of a command icon in the suggestion list popup.
 * @param threadParticipantItemSize The size of thread participant avatar items.
 * @param userReactionsMaxHeight The max height of the message reactions section when we click on message reactions.
 * @param userReactionItemWidth The width of user reaction item.
 * @param userReactionItemAvatarSize The size of a user avatar in the user reaction item.
 * @param userReactionItemIconSize The size of a reaction icon in the user reaction item.
 * @param reactionOptionItemIconSize The size of a reaction option icon in the reaction options menu.
 * @param headerElevation The elevation of the headers, such as the ones appearing on the Channel or Message screens.
 */
@Immutable
public data class StreamDimens(
    public val channelItemVerticalPadding: Dp,
    public val channelItemHorizontalPadding: Dp,
    public val channelAvatarSize: Dp,
    public val selectedChannelMenuUserItemWidth: Dp,
    public val selectedChannelMenuUserItemHorizontalPadding: Dp,
    public val selectedChannelMenuUserItemAvatarSize: Dp,
    public val attachmentsContentImageWidth: Dp,
    public val attachmentsContentImageGridSpacing: Dp,
    public val attachmentsContentImageHeight: Dp,
    public val attachmentsContentGiphyWidth: Dp,
    public val attachmentsContentGiphyHeight: Dp,
    public val attachmentsContentLinkWidth: Dp,
    public val attachmentsContentFileWidth: Dp,
    public val attachmentsContentFileUploadWidth: Dp,
    public val threadSeparatorVerticalPadding: Dp,
    public val threadSeparatorTextVerticalPadding: Dp,
    public val messageOptionsItemHeight: Dp,
    public val suggestionListMaxHeight: Dp,
    public val suggestionListPadding: Dp,
    public val suggestionListElevation: Dp,
    public val mentionSuggestionItemHorizontalPadding: Dp,
    public val mentionSuggestionItemVerticalPadding: Dp,
    public val mentionSuggestionItemAvatarSize: Dp,
    public val commandSuggestionItemHorizontalPadding: Dp,
    public val commandSuggestionItemVerticalPadding: Dp,
    public val commandSuggestionItemIconSize: Dp,
    public val threadParticipantItemSize: Dp,
    public val userReactionsMaxHeight: Dp,
    public val userReactionItemWidth: Dp,
    public val userReactionItemAvatarSize: Dp,
    public val userReactionItemIconSize: Dp,
    public val reactionOptionItemIconSize: Dp,
    public val headerElevation: Dp,
) {
    public companion object {
        /**
         * Builds the default dimensions for our theme.
         *
         * @return A [StreamDimens] instance holding our default dimensions.
         */
        public fun defaultDimens(): StreamDimens = StreamDimens(
            channelItemVerticalPadding = 12.dp,
            channelItemHorizontalPadding = 8.dp,
            channelAvatarSize = 40.dp,
            selectedChannelMenuUserItemWidth = 80.dp,
            selectedChannelMenuUserItemHorizontalPadding = 8.dp,
            selectedChannelMenuUserItemAvatarSize = 64.dp,
            attachmentsContentImageWidth = 250.dp,
            attachmentsContentImageGridSpacing = 2.dp,
            attachmentsContentImageHeight = 200.dp,
            attachmentsContentGiphyWidth = 250.dp,
            attachmentsContentGiphyHeight = 200.dp,
            attachmentsContentLinkWidth = 250.dp,
            attachmentsContentFileWidth = 250.dp,
            attachmentsContentFileUploadWidth = 250.dp,
            threadSeparatorVerticalPadding = 8.dp,
            threadSeparatorTextVerticalPadding = 2.dp,
            messageOptionsItemHeight = 40.dp,
            suggestionListMaxHeight = 256.dp,
            suggestionListPadding = 8.dp,
            suggestionListElevation = 4.dp,
            mentionSuggestionItemHorizontalPadding = 16.dp,
            mentionSuggestionItemVerticalPadding = 8.dp,
            mentionSuggestionItemAvatarSize = 40.dp,
            commandSuggestionItemHorizontalPadding = 8.dp,
            commandSuggestionItemVerticalPadding = 8.dp,
            commandSuggestionItemIconSize = 24.dp,
            threadParticipantItemSize = 16.dp,
            userReactionsMaxHeight = 256.dp,
            userReactionItemWidth = 80.dp,
            userReactionItemIconSize = 24.dp,
            userReactionItemAvatarSize = 64.dp,
            reactionOptionItemIconSize = 24.dp,
            headerElevation = 4.dp,
        )
    }
}
