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
 * @param channelInfoUserItemWidth The width of a member tile in the channel info dialog.
 * @param channelInfoUserItemHorizontalPadding The padding inside a member tile in the channel info dialog.
 * @param channelInfoUserItemAvatarSize The size of a member avatar in the channel info dialog.
 * @param attachmentsContentImageWidth The width of image attachments in the message list.
 * @param attachmentsContentImageHeight The height of image attachments in the message list.
 * @param attachmentsContentGiphyWidth The with of Giphy attachments in the message list.
 * @param attachmentsContentGiphyHeight The height of Giphy attachments in the message list.
 * @param attachmentsContentLinkWidth The with of link attachments in the message list.
 * @param attachmentsContentFileWidth The width of file attachments in the message list.
 * @param attachmentsContentFileUploadWidth The width of uploading file attachments in the message list.
 * @param threadSeparatorVerticalPadding The vertical content padding inside thread separator item.
 * @param threadSeparatorTextVerticalPadding The vertical padding inside thread separator text.
 * @param messageOverlayActionItemHeight The height of an action item on the selected message overlay.
 * @param suggestionListMaxHeight The maximum height of the suggestion list popup.
 * @param suggestionListPadding The outer padding of the suggestion list popup.
 * @param suggestionListElevation THe elevation of the suggestion list popup.
 * @param mentionSuggestionItemHorizontalPadding The horizontal content padding inside mention list item.
 * @param mentionSuggestionItemVerticalPadding The vertical content padding inside mention list item.
 * @param mentionSuggestionItemAvatarSize The size of a channel avatar in the suggestion list popup.
 * @param commandSuggestionItemHorizontalPadding The horizontal content padding inside command list item.
 * @param commandSuggestionItemVerticalPadding The vertical content padding inside command list item.
 * @param commandSuggestionItemIconSize The size of a command icon in the suggestion list popup.
 * @param messageOptionsMaxHeight The max height of the message options section when we select a message in the list.
 * @param messageOptionsMaxWidth The max width of the message options section when we select a message in the list.
 * @param messageOptionsRoundedCorners The rounded corners size of the message options shape.
 * @param threadParticipantItemSize The size of thread participant avatar items.
 */
@Immutable
public data class StreamDimens(
    public val channelItemVerticalPadding: Dp,
    public val channelItemHorizontalPadding: Dp,
    public val channelAvatarSize: Dp,
    public val channelInfoUserItemWidth: Dp,
    public val channelInfoUserItemHorizontalPadding: Dp,
    public val channelInfoUserItemAvatarSize: Dp,
    public val attachmentsContentImageWidth: Dp,
    public val attachmentsContentImageHeight: Dp,
    public val attachmentsContentGiphyWidth: Dp,
    public val attachmentsContentGiphyHeight: Dp,
    public val attachmentsContentLinkWidth: Dp,
    public val attachmentsContentFileWidth: Dp,
    public val attachmentsContentFileUploadWidth: Dp,
    public val threadSeparatorVerticalPadding: Dp,
    public val threadSeparatorTextVerticalPadding: Dp,
    public val messageOverlayActionItemHeight: Dp,
    public val suggestionListMaxHeight: Dp,
    public val suggestionListPadding: Dp,
    public val suggestionListElevation: Dp,
    public val mentionSuggestionItemHorizontalPadding: Dp,
    public val mentionSuggestionItemVerticalPadding: Dp,
    public val mentionSuggestionItemAvatarSize: Dp,
    public val commandSuggestionItemHorizontalPadding: Dp,
    public val commandSuggestionItemVerticalPadding: Dp,
    public val commandSuggestionItemIconSize: Dp,
    public val messageOptionsMaxWidth: Dp,
    public val messageOptionsMaxHeight: Dp,
    public val messageOptionsRoundedCorners: Dp,
    public val threadParticipantItemSize: Dp,
) {
    public companion object {
        public fun defaultDimens(): StreamDimens = StreamDimens(
            channelItemVerticalPadding = 12.dp,
            channelItemHorizontalPadding = 8.dp,
            channelAvatarSize = 40.dp,
            channelInfoUserItemWidth = 80.dp,
            channelInfoUserItemHorizontalPadding = 8.dp,
            channelInfoUserItemAvatarSize = 64.dp,
            attachmentsContentImageWidth = 250.dp,
            attachmentsContentImageHeight = 200.dp,
            attachmentsContentGiphyWidth = 250.dp,
            attachmentsContentGiphyHeight = 200.dp,
            attachmentsContentLinkWidth = 250.dp,
            attachmentsContentFileWidth = 250.dp,
            attachmentsContentFileUploadWidth = 250.dp,
            threadSeparatorVerticalPadding = 8.dp,
            threadSeparatorTextVerticalPadding = 2.dp,
            messageOverlayActionItemHeight = 40.dp,
            suggestionListMaxHeight = 256.dp,
            suggestionListPadding = 8.dp,
            suggestionListElevation = 4.dp,
            mentionSuggestionItemHorizontalPadding = 16.dp,
            mentionSuggestionItemVerticalPadding = 8.dp,
            mentionSuggestionItemAvatarSize = 40.dp,
            commandSuggestionItemHorizontalPadding = 8.dp,
            commandSuggestionItemVerticalPadding = 8.dp,
            commandSuggestionItemIconSize = 24.dp,
            messageOptionsMaxWidth = 200.dp,
            messageOptionsMaxHeight = 300.dp,
            messageOptionsRoundedCorners = 16.dp,
            threadParticipantItemSize = 16.dp
        )
    }
}
