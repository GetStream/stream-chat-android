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
 * @param selectedChannelMenuUserItemHorizontalPadding The padding inside a member tile in the selected channel
 * menu.
 * @param selectedChannelMenuUserItemAvatarSize The size of a member avatar in the selected channel menu.
 * @param attachmentsContentImageWidth The width of image attachments in the message list.
 * @param attachmentsContentGiphyWidth The with of Giphy attachments in the message list.
 * @param attachmentsContentGiphyHeight The height of Giphy attachments in the message list.
 * @param attachmentsContentLinkWidth The with of link attachments in the message list.
 * @param attachmentsContentFileWidth The width of file attachments in the message list.
 * @param attachmentsContentFileUploadWidth The width of uploading file attachments in the message list.
 * @param attachmentsContentUnsupportedWidth The width of unsupported attachments in the message list.
 * @param threadSeparatorVerticalPadding The vertical content padding inside thread separator item.
 * @param threadSeparatorTextVerticalPadding The vertical padding inside thread separator text.
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
 * @param headerElevation The elevation of the headers, such as the ones appearing on the Channel or Message
 * screens.
 * @param messageItemMaxWidth The max width of message items inside message list.
 * @param quotedMessageTextVerticalPadding The vertical padding of text inside quoted message.
 * @param quotedMessageTextHorizontalPadding The horizontal padding of text inside quoted message.
 * @param quotedMessageAttachmentPreviewSize The size of the quoted message attachment preview.
 * @param quotedMessageAttachmentTopPadding The top padding of the quoted message attachment preview.
 * @param quotedMessageAttachmentBottomPadding The bottom padding of the quoted message attachment preview.
 * @param quotedMessageAttachmentStartPadding The start padding of the quoted message attachment preview.
 * @param quotedMessageAttachmentEndPadding The end padding of the quoted message attachment preview.
 * @param quotedMessageAttachmentSpacerHorizontal The horizontal spacing between quoted message attachment components.
 * @param quotedMessageAttachmentSpacerVertical The vertical spacing between quoted message attachment components.
 * @param groupAvatarInitialsXOffset The x offset of the user initials inside avatar when there are more than two
 * users.
 * @param groupAvatarInitialsYOffset The y offset of the user initials inside avatar when there are more than two
 * users.
 * @param attachmentsPickerHeight The height of the attachments picker.
 * @param attachmentsSystemPickerHeight The height of the system attachments picker.
 * @param attachmentsContentImageMaxHeight The maximum height an image attachment will expand to while automatically
 *  re-sizing itself in order to obey its aspect ratio.
 * re-sizing itself in order to obey its aspect ratio.
 * @param attachmentsContentGiphyMaxWidth The maximum width a Giphy attachment will expand to while automatically
 *  re-sizing itself in order to follow its aspect ratio.
 * @param attachmentsContentGiphyMaxHeight The maximum height a Giphy attachment will expand to while automatically
 *  re-sizing itself in order to follow its aspect ratio.
 *  re-sizing itself in order to obey its aspect ratio.
 * @param attachmentsContentVideoMaxHeight The maximum height video attachment will expand to while automatically
 *  re-sizing itself in order to obey its aspect ratio.
 * @param attachmentsContentMediaGridSpacing The spacing between media preview tiles in the message list.
 * @param attachmentsContentVideoWidth The width of media attachment previews in the message list.
 * @param attachmentsContentGroupPreviewWidth The width of the container displaying media previews tiled in
 * a group in the message list.
 * @param attachmentsContentGroupPreviewHeight The height of the container displaying media previews tiled in
 * a group in the message list.
 * @param pollOptionInputHeight The height of the poll option input field.
 * @param messageComposerShadowElevation The elevation of the message composer shadow.
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
    public val attachmentsContentGiphyWidth: Dp,
    public val attachmentsContentGiphyHeight: Dp,
    public val attachmentsContentLinkWidth: Dp,
    public val attachmentsContentFileWidth: Dp,
    public val attachmentsContentFileUploadWidth: Dp,
    public val attachmentsContentUnsupportedWidth: Dp,
    public val threadSeparatorVerticalPadding: Dp,
    public val threadSeparatorTextVerticalPadding: Dp,
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
    public val headerElevation: Dp,
    public val messageItemMaxWidth: Dp,
    public val quotedMessageTextVerticalPadding: Dp,
    public val quotedMessageTextHorizontalPadding: Dp,
    public val quotedMessageAttachmentPreviewSize: Dp,
    public val quotedMessageAttachmentTopPadding: Dp,
    public val quotedMessageAttachmentBottomPadding: Dp,
    public val quotedMessageAttachmentStartPadding: Dp,
    public val quotedMessageAttachmentEndPadding: Dp,
    public val quotedMessageAttachmentSpacerHorizontal: Dp,
    public val quotedMessageAttachmentSpacerVertical: Dp,
    public val groupAvatarInitialsXOffset: Dp,
    public val groupAvatarInitialsYOffset: Dp,
    public val attachmentsPickerHeight: Dp,
    public val attachmentsSystemPickerHeight: Dp,
    public val attachmentsContentImageMaxHeight: Dp,
    public val attachmentsContentGiphyMaxWidth: Dp = attachmentsContentGiphyWidth,
    public val attachmentsContentGiphyMaxHeight: Dp = attachmentsContentGiphyHeight,
    public val attachmentsContentVideoMaxHeight: Dp,
    public val attachmentsContentMediaGridSpacing: Dp,
    public val attachmentsContentVideoWidth: Dp,
    public val attachmentsContentGroupPreviewWidth: Dp,
    public val attachmentsContentGroupPreviewHeight: Dp,
    public val pollOptionInputHeight: Dp,
    public val messageComposerShadowElevation: Dp,
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
            attachmentsContentGiphyWidth = 250.dp,
            attachmentsContentGiphyHeight = 200.dp,
            attachmentsContentLinkWidth = 250.dp,
            attachmentsContentFileWidth = 250.dp,
            attachmentsContentFileUploadWidth = 250.dp,
            attachmentsContentUnsupportedWidth = 250.dp,
            threadSeparatorVerticalPadding = 8.dp,
            threadSeparatorTextVerticalPadding = 2.dp,
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
            headerElevation = 4.dp,
            messageItemMaxWidth = 250.dp,
            quotedMessageTextHorizontalPadding = 8.dp,
            quotedMessageTextVerticalPadding = 6.dp,
            quotedMessageAttachmentPreviewSize = 36.dp,
            quotedMessageAttachmentBottomPadding = 6.dp,
            quotedMessageAttachmentTopPadding = 6.dp,
            quotedMessageAttachmentStartPadding = 8.dp,
            quotedMessageAttachmentEndPadding = 0.dp,
            quotedMessageAttachmentSpacerHorizontal = 8.dp,
            quotedMessageAttachmentSpacerVertical = 2.dp,
            groupAvatarInitialsXOffset = 1.5.dp,
            groupAvatarInitialsYOffset = 2.5.dp,
            attachmentsPickerHeight = 350.dp,
            attachmentsSystemPickerHeight = 72.dp,
            attachmentsContentImageMaxHeight = 600.dp,
            attachmentsContentVideoMaxHeight = 400.dp,
            attachmentsContentMediaGridSpacing = 2.dp,
            attachmentsContentVideoWidth = 250.dp,
            attachmentsContentGroupPreviewWidth = 250.dp,
            attachmentsContentGroupPreviewHeight = 196.dp,
            pollOptionInputHeight = 56.dp,
            messageComposerShadowElevation = 24.dp,
        )
    }
}
