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

package io.getstream.chat.android.ui.feature.messages.composer

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.InputType
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.font.TextStyle
import io.getstream.chat.android.ui.helper.TransformStyle
import io.getstream.chat.android.ui.utils.extensions.dpToPxPrecise
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getColorOrNull
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.getDimensionOrNull
import io.getstream.chat.android.ui.utils.extensions.getDrawableCompat
import io.getstream.chat.android.ui.utils.extensions.use

/**
 * Style for [MessageComposerView].
 *
 * @param backgroundColor The background color of the message composer.
 * @param buttonIconDrawableTintColor The tint applied to attachments, commands and send buttons.
 * @param dividerBackgroundDrawable The background of the divider at the top.
 * @param commandSuggestionsTitleText The text for the title at the top of the command suggestions dialog.
 * @param commandSuggestionsTitleTextStyle The text style for the title at the top of the command suggestions dialog.
 * @param commandSuggestionsTitleIconDrawable The icon for the title at the top of the command suggestions dialog.
 * @param commandSuggestionsBackgroundColor The background color of the command suggestions dialog.
 * @param commandSuggestionItemCommandNameTextStyle The text style for the command name.
 * @param commandSuggestionItemCommandDescriptionText The command description template with two placeholders.
 * @param commandSuggestionItemCommandDescriptionTextStyle The text style for the command description.
 * @param mentionSuggestionsBackgroundColor The background color of the mention suggestions dialog.
 * @param mentionSuggestionItemIconDrawable The icon for each command icon in the suggestion list.
 * @param mentionSuggestionItemUsernameTextStyle The text style that will be used for the user name.
 * @param mentionSuggestionItemMentionText The mention template that will be used for the mention preview.
 * @param mentionSuggestionItemMentionTextStyle The text style that will be used for the mention preview.
 * @param messageInputCommandsHandlingEnabled If command suggestions are shown based on user input.
 * @param messageInputMentionsHandlingEnabled If mention suggestions are shown based on user input.
 * @param messageInputTextStyle The text style of the text input field.
 * @param messageInputBackgroundDrawable The background drawable of the text input field.
 * @param messageInputCursorDrawable The drawable for the cursor in the text input field.
 * @param messageInputScrollbarEnabled If the vertical scrollbar should be drawn or not.
 * @param messageInputScrollbarFadingEnabled If the vertical edges should be faded on scroll or not.
 * @param messageInputMaxLines The maximum number of message input lines.
 * @param messageInputCannotSendHintText The input hint text in case we can't send messages in this channel.
 * @param messageInputInputType The [InputType] to be applied to the message input edit text.
 * @param messageInputVideoAttachmentIconDrawable Overlays a drawable above video attachments.
 * By default, used to display a play button.
 * @param messageInputVideoAttachmentIconDrawableTint Sets the tint of the drawable overlaid above video attachments.
 * @param messageInputVideoAttachmentIconBackgroundColor Sets the background color of the icon overlaid above video
 * attachments.
 * @param messageInputVideoAttachmentIconCornerRadius Sets the corner radius of the parent card containing the video
 * attachment icon.
 * @param messageInputVideoAttachmentIconElevation Sets the elevation of the icon overlaid above video attachments.
 * @param messageInputVideoAttachmentIconDrawablePaddingTop Sets the top padding between the video attachment drawable
 * and its parent card.
 * @param messageInputVideoAttachmentIconDrawablePaddingBottom Sets the bottom padding between the video attachment
 * drawable and its parent card.
 * @param messageInputVideoAttachmentIconDrawablePaddingStart Sets the start padding between the video attachment
 * drawable and its parent card.
 * @param messageInputVideoAttachmentIconDrawablePaddingEnd Sets the end padding between the video attachment drawable
 * and its parent card.
 * @param attachmentsButtonVisible If the button to pick attachments is displayed.
 * @param attachmentsButtonIconDrawable The icon for the attachments button.
 * @param attachmentsButtonRippleColor Ripple color of the attachments button.
 * @param commandsButtonVisible If the button to select commands is displayed.
 * @param commandsButtonIconDrawable The icon for the commands button.
 * @param commandsButtonRippleColor Ripple color of the commands button.
 * @param alsoSendToChannelCheckboxVisible If the checkbox to send a message to the channel is displayed.
 * @param alsoSendToChannelCheckboxDrawable The drawable that will be used for the checkbox.
 * @param alsoSendToChannelCheckboxText The text that will be displayed next to the checkbox.
 * @param alsoSendToChannelCheckboxTextStyle The text style that will be used for the checkbox text.
 * @param editModeText The text for edit mode title.
 * @param editModeIconDrawable The icon displayed in top left corner when the user edits a message.
 * @param replyModeText The text for reply mode title.
 * @param replyModeIconDrawable The icon displayed in top left corner when the user replies to a message.
 * @param dismissModeIconDrawable The icon for the button that dismisses edit or reply mode.
 * @param sendMessageButtonEnabled If the button to send message is enabled.
 * @param sendMessageButtonIconDrawable The icon for the button to send message.
 * @param cooldownTimerTextStyle The text style that will be used for cooldown timer.
 * @param cooldownTimerBackgroundDrawable Background drawable for cooldown timer.
 */
public data class MessageComposerViewStyle(
    @ColorInt public val backgroundColor: Int,
    @ColorInt public val buttonIconDrawableTintColor: Int?,
    public val dividerBackgroundDrawable: Drawable,
    // Command suggestions content
    public val commandSuggestionsTitleText: String,
    public val commandSuggestionsTitleTextStyle: TextStyle,
    public val commandSuggestionsTitleIconDrawable: Drawable,
    @ColorInt public val commandSuggestionsBackgroundColor: Int,
    public val commandSuggestionItemCommandNameTextStyle: TextStyle,
    public val commandSuggestionItemCommandDescriptionText: String,
    public val commandSuggestionItemCommandDescriptionTextStyle: TextStyle,
    // Mention suggestions content
    @ColorInt public val mentionSuggestionsBackgroundColor: Int,
    public val mentionSuggestionItemIconDrawable: Drawable,
    public val mentionSuggestionItemUsernameTextStyle: TextStyle,
    public val mentionSuggestionItemMentionText: String,
    public val mentionSuggestionItemMentionTextStyle: TextStyle,
    // Center content
    public val messageInputCommandsHandlingEnabled: Boolean,
    public val messageInputMentionsHandlingEnabled: Boolean,
    public val messageInputTextStyle: TextStyle,
    public val messageInputBackgroundDrawable: Drawable,
    public val messageInputCursorDrawable: Drawable?,
    public val messageInputScrollbarEnabled: Boolean,
    public val messageInputScrollbarFadingEnabled: Boolean,
    public val messageInputMaxLines: Int,
    public val messageInputCannotSendHintText: String,
    public val messageInputInputType: Int,
    public val messageInputVideoAttachmentIconDrawable: Drawable,
    @ColorInt public val messageInputVideoAttachmentIconDrawableTint: Int?,
    @ColorInt public val messageInputVideoAttachmentIconBackgroundColor: Int?,
    public val messageInputVideoAttachmentIconCornerRadius: Float,
    public val messageInputVideoAttachmentIconElevation: Float,
    public val messageInputVideoAttachmentIconDrawablePaddingTop: Int,
    public val messageInputVideoAttachmentIconDrawablePaddingBottom: Int,
    public val messageInputVideoAttachmentIconDrawablePaddingStart: Int,
    public val messageInputVideoAttachmentIconDrawablePaddingEnd: Int,
    // Leading content
    public val attachmentsButtonVisible: Boolean,
    public val attachmentsButtonIconDrawable: Drawable,
    @ColorInt public val attachmentsButtonRippleColor: Int?,
    public val commandsButtonVisible: Boolean,
    public val commandsButtonIconDrawable: Drawable,
    @ColorInt public val commandsButtonRippleColor: Int?,
    // Footer content
    public val alsoSendToChannelCheckboxVisible: Boolean,
    public val alsoSendToChannelCheckboxDrawable: Drawable?,
    public val alsoSendToChannelCheckboxText: String,
    public val alsoSendToChannelCheckboxTextStyle: TextStyle,
    // Header content
    public val editModeText: String,
    public val editModeIconDrawable: Drawable,
    public val replyModeText: String,
    public val replyModeIconDrawable: Drawable,
    public val dismissModeIconDrawable: Drawable,
    // Trailing content
    public val sendMessageButtonEnabled: Boolean,
    public val sendMessageButtonIconDrawable: Drawable,
    public val cooldownTimerTextStyle: TextStyle,
    public val cooldownTimerBackgroundDrawable: Drawable,
) {
    public companion object {
        internal operator fun invoke(context: Context, attrs: AttributeSet?): MessageComposerViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.MessageComposerView,
                R.attr.streamUiMessageComposerViewStyle,
                R.style.StreamUi_MessageComposerView,
            ).use { a ->
                var backgroundColor: Int
                context.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.background)).use {
                    backgroundColor = it.getColor(0, context.getColorCompat(R.color.stream_ui_white))
                }

                val buttonIconDrawableTintColor = a.getColorOrNull(
                    R.styleable.MessageComposerView_streamUiMessageComposerIconDrawableTintColor
                )

                val dividerBackgroundDrawable = a.getDrawable(
                    R.styleable.MessageComposerView_streamUiMessageComposerDividerBackgroundDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_divider)!!

                /**
                 * Command suggestions content
                 */
                val commandSuggestionsTitleText = a.getString(
                    R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionsTitleText
                ) ?: context.getString(R.string.stream_ui_message_composer_instant_commands)

                val commandSuggestionsTitleTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionsTitleTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium)
                    )
                    .color(
                        R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionsTitleTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary)
                    )
                    .font(
                        R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionsTitleTextFontAssets,
                        R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionsTitleTextFont
                    )
                    .style(
                        R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionsTitleTextStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val commandSuggestionsTitleIconDrawable = a.getDrawable(
                    R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionsTitleIconDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_command_blue)!!

                val commandSuggestionsBackgroundColor = a.getColor(
                    R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionsBackgroundColor,
                    context.getColorCompat(R.color.stream_ui_white)
                )

                val commandSuggestionItemCommandNameTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionItemCommandNameTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium)
                    )
                    .color(
                        R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionItemCommandNameTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary)
                    )
                    .font(
                        R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionItemCommandNameTextFontAssets,
                        R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionItemCommandNameTextFont
                    )
                    .style(
                        R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionItemCommandNameTextStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val commandSuggestionItemCommandDescriptionText = a.getString(
                    R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionItemCommandDescriptionText
                ) ?: context.getString(R.string.stream_ui_message_composer_command_template)

                val commandSuggestionItemCommandDescriptionTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionItemCommandDescriptionTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium)
                    )
                    .color(
                        R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionItemCommandDescriptionTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary)
                    )
                    .font(
                        R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionItemCommandDescriptionTextFontAssets,
                        R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionItemCommandDescriptionTextFont
                    )
                    .style(
                        R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionItemCommandDescriptionTextStyle,
                        Typeface.NORMAL
                    )
                    .build()

                /**
                 * Mention suggestions content
                 */
                val mentionSuggestionsBackgroundColor = a.getColor(
                    R.styleable.MessageComposerView_streamUiMessageComposerMentionSuggestionsBackgroundColor,
                    context.getColorCompat(R.color.stream_ui_white)
                )

                val mentionSuggestionItemIconDrawable: Drawable = a.getDrawable(
                    R.styleable.MessageComposerView_streamUiMessageComposerMentionSuggestionItemIconDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_mention)!!

                val mentionSuggestionItemUsernameTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageComposerView_streamUiMessageComposerMentionSuggestionItemUsernameTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium)
                    )
                    .color(
                        R.styleable.MessageComposerView_streamUiMessageComposerMentionSuggestionItemUsernameTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary)
                    )
                    .font(
                        R.styleable.MessageComposerView_streamUiMessageComposerMentionSuggestionItemUsernameTextFontAssets,
                        R.styleable.MessageComposerView_streamUiMessageComposerMentionSuggestionItemUsernameTextFont
                    )
                    .style(
                        R.styleable.MessageComposerView_streamUiMessageComposerMentionSuggestionItemUsernameTextStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val mentionSuggestionItemMentionText = a.getString(
                    R.styleable.MessageComposerView_streamUiMessageComposerMentionSuggestionItemMentionText
                ) ?: context.getString(R.string.stream_ui_message_composer_mention_template)

                val mentionSuggestionItemMentionTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageComposerView_streamUiMessageComposerMentionSuggestionItemMentionTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium)
                    )
                    .color(
                        R.styleable.MessageComposerView_streamUiMessageComposerMentionSuggestionItemMentionTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary)
                    )
                    .font(
                        R.styleable.MessageComposerView_streamUiMessageComposerMentionSuggestionItemMentionTextFontAssets,
                        R.styleable.MessageComposerView_streamUiMessageComposerMentionSuggestionItemMentionTextFont
                    )
                    .style(
                        R.styleable.MessageComposerView_streamUiMessageComposerMentionSuggestionItemMentionTextStyle,
                        Typeface.NORMAL
                    )
                    .build()

                /**
                 * Center content
                 */
                val messageInputCommandsHandlingEnabled = a.getBoolean(
                    R.styleable.MessageComposerView_streamUiMessageComposerCommandsHandlingEnabled,
                    true
                )

                val messageInputMentionsHandlingEnabled = a.getBoolean(
                    R.styleable.MessageComposerView_streamUiMessageComposerMentionsHandlingEnabled,
                    true
                )

                val messageInputTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageInputTextSize,
                        context.resources.getDimensionPixelSize(R.dimen.stream_ui_text_size_input)
                    )
                    .color(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageInputTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary)
                    )
                    .font(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageInputTextFontAssets,
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageInputTextFont
                    )
                    .style(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageInputTextStyle,
                        Typeface.NORMAL
                    )
                    .hint(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageInputHintText,
                        context.getString(R.string.stream_ui_message_composer_hint_normal)
                    )
                    .hintColor(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageInputHintColor,
                        context.getColorCompat(R.color.stream_ui_text_color_hint)
                    )
                    .build()

                val messageInputBackgroundDrawable = a.getDrawable(
                    R.styleable.MessageComposerView_streamUiMessageComposerMessageInputBackgroundDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_shape_edit_text_round)!!

                val messageInputCursorDrawable: Drawable? = a.getDrawable(
                    R.styleable.MessageComposerView_streamUiMessageComposerMessageInputCursorDrawable
                )

                val messageInputScrollbarEnabled = a.getBoolean(
                    R.styleable.MessageComposerView_streamUiMessageComposerScrollbarEnabled,
                    false
                )
                val messageInputScrollbarFadingEnabled = a.getBoolean(
                    R.styleable.MessageComposerView_streamUiMessageComposerScrollbarFadingEnabled,
                    false
                )

                val messageInputMaxLines = a.getInt(
                    R.styleable.MessageComposerView_streamUiMessageComposerMessageInputMaxLines,
                    7
                )

                val messageInputCannotSendHintText = a.getString(
                    R.styleable.MessageComposerView_streamUiMessageComposerMessageInputCannotSendHintText
                ) ?: context.getString(R.string.stream_ui_message_composer_hint_cannot_send_message)

                /**
                 * Leading content
                 */
                val attachmentsButtonVisible = a.getBoolean(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsButtonVisible,
                    true
                )

                val attachmentsButtonIconDrawable = a.getDrawableCompat(
                    context,
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsButtonIconDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_attach)!!

                val attachmentsButtonRippleColor = a.getColorOrNull(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsButtonRippleColor
                )

                val commandsButtonVisible = a.getBoolean(
                    R.styleable.MessageComposerView_streamUiMessageComposerCommandsButtonVisible,
                    true
                )

                val commandsButtonIconDrawable = a.getDrawableCompat(
                    context,
                    R.styleable.MessageComposerView_streamUiMessageComposerCommandsButtonIconDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_command)!!

                val commandsButtonRippleColor = a.getColorOrNull(
                    R.styleable.MessageComposerView_streamUiMessageComposerCommandsButtonRippleColor
                )

                /**
                 * Footer content
                 */
                val alsoSendToChannelCheckboxVisible = a.getBoolean(
                    R.styleable.MessageComposerView_streamUiMessageComposerAlsoSendToChannelCheckboxVisible,
                    true
                )

                val alsoSendToChannelCheckboxDrawable = a.getDrawable(
                    R.styleable.MessageComposerView_streamUiMessageComposerAlsoSendToChannelCheckboxDrawable
                )

                val alsoSendToChannelCheckboxText: String = a.getText(
                    R.styleable.MessageComposerView_streamUiMessageComposerAlsoSendToChannelCheckboxText
                )?.toString() ?: context.getString(R.string.stream_ui_message_composer_send_to_channel)

                val alsoSendToChannelCheckboxTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageComposerView_streamUiMessageComposerAlsoSendToChannelCheckboxTextSize,
                        context.getDimension(R.dimen.stream_ui_text_small)
                    )
                    .color(
                        R.styleable.MessageComposerView_streamUiMessageComposerAlsoSendToChannelCheckboxTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary)
                    )
                    .font(
                        R.styleable.MessageComposerView_streamUiMessageComposerAlsoSendToChannelCheckboxTextFontAssets,
                        R.styleable.MessageComposerView_streamUiMessageComposerAlsoSendToChannelCheckboxTextFont
                    )
                    .style(
                        R.styleable.MessageComposerView_streamUiMessageComposerAlsoSendToChannelCheckboxTextStyle,
                        Typeface.NORMAL
                    )
                    .build()

                /**
                 * Header content
                 */
                val editModeText: String = a.getText(
                    R.styleable.MessageComposerView_streamUiMessageComposerEditModeText
                )?.toString() ?: context.getString(R.string.stream_ui_message_composer_mode_edit)

                val editModeIconDrawable = a.getDrawable(
                    R.styleable.MessageComposerView_streamUiMessageComposerEditModeIconDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_edit)!!

                val replyModeText: String = a.getText(
                    R.styleable.MessageComposerView_streamUiMessageComposerReplyModeText
                )?.toString() ?: context.getString(R.string.stream_ui_message_composer_mode_reply)

                val replyModeIconDrawable = a.getDrawable(
                    R.styleable.MessageComposerView_streamUiMessageComposerReplyModeIconDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_arrow_curve_left_grey)!!

                val dismissModeIconDrawable = a.getDrawable(
                    R.styleable.MessageComposerView_streamUiMessageComposerDismissModeIconDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_clear)!!

                /**
                 * Trailing content
                 */
                val sendMessageButtonEnabled = a.getBoolean(
                    R.styleable.MessageComposerView_streamUiMessageComposerSendMessageButtonEnabled,
                    true
                )

                val sendMessageButtonIconDrawable = a.getDrawableCompat(
                    context,
                    R.styleable.MessageComposerView_streamUiMessageComposerSendMessageButtonIconDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_send_message)!!

                val cooldownTimerTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageComposerView_streamUiMessageComposerCooldownTimerTextSize,
                        context.getDimension(R.dimen.stream_ui_text_large)
                    )
                    .color(
                        R.styleable.MessageComposerView_streamUiMessageComposerCooldownTimerTextColor,
                        context.getColorCompat(R.color.stream_ui_literal_white)
                    )
                    .font(
                        R.styleable.MessageComposerView_streamUiMessageComposerCooldownTimerTextFontAssets,
                        R.styleable.MessageComposerView_streamUiMessageComposerCooldownTimerTextFont
                    )
                    .style(
                        R.styleable.MessageComposerView_streamUiMessageComposerCooldownTimerTextStyle,
                        Typeface.BOLD
                    )
                    .build()

                val cooldownTimerBackgroundDrawable = a.getDrawable(
                    R.styleable.MessageComposerView_streamUiMessageComposerCooldownTimerBackgroundDrawable,
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_cooldown_badge_background)!!

                val messageInputInputType = a.getInt(
                    R.styleable.MessageComposerView_android_inputType,
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                        InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                )

                val messageInputVideoAttachmentIconDrawable =
                    a.getDrawable(R.styleable.MessageComposerView_streamUiMessageComposerMessageInputVideoAttachmentIconDrawable)
                        ?: ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_play)!!

                val messageInputVideoAttachmentIconDrawableTint =
                    a.getColorOrNull(R.styleable.MessageComposerView_streamUiMessageComposerMessageInputVideoAttachmentIconDrawableTint)

                val messageInputVideoAttachmentIconBackgroundColor =
                    a.getColorOrNull(R.styleable.MessageComposerView_streamUiMessageComposerMessageInputVideoAttachmentIconBackgroundColor)

                val messageInputVideoAttachmentIconCornerRadius =
                    a.getDimension(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageInputVideoCornerRadius,
                        20.dpToPxPrecise()
                    )

                val messageInputVideoAttachmentIconElevation =
                    a.getDimension(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageInputVideoAttachmentIconElevation,
                        0f
                    )

                val messageInputVideoAttachmentIconDrawablePaddingTop =
                    a.getDimensionPixelSize(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageInputVideoAttachmentIconDrawablePaddingTop,
                        0
                    )

                val messageInputVideoAttachmentIconDrawablePaddingBottom =
                    a.getDimensionPixelSize(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageInputVideoAttachmentIconDrawablePaddingBottom,
                        0
                    )

                val messageInputVideoAttachmentIconDrawablePaddingStart =
                    a.getDimensionPixelSize(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageInputVideoAttachmentIconDrawablePaddingStart,
                        0
                    )

                val messageInputVideoAttachmentIconDrawablePaddingEnd =
                    a.getDimensionPixelSize(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageInputVideoAttachmentIconDrawablePaddingEnd,
                        0
                    )

                val messageInputVideoAttachmentIconDrawablePadding =
                    a.getDimensionOrNull(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageInputVideoAttachmentIconDrawablePadding,
                    )?.toInt()

                return MessageComposerViewStyle(
                    backgroundColor = backgroundColor,
                    buttonIconDrawableTintColor = buttonIconDrawableTintColor,
                    dividerBackgroundDrawable = dividerBackgroundDrawable,
                    // Command suggestions content
                    commandSuggestionsTitleText = commandSuggestionsTitleText,
                    commandSuggestionsTitleTextStyle = commandSuggestionsTitleTextStyle,
                    commandSuggestionsTitleIconDrawable = commandSuggestionsTitleIconDrawable,
                    commandSuggestionsBackgroundColor = commandSuggestionsBackgroundColor,
                    commandSuggestionItemCommandNameTextStyle = commandSuggestionItemCommandNameTextStyle,
                    commandSuggestionItemCommandDescriptionText = commandSuggestionItemCommandDescriptionText,
                    commandSuggestionItemCommandDescriptionTextStyle = commandSuggestionItemCommandDescriptionTextStyle,
                    // Mention suggestions content
                    mentionSuggestionsBackgroundColor = mentionSuggestionsBackgroundColor,
                    mentionSuggestionItemIconDrawable = mentionSuggestionItemIconDrawable,
                    mentionSuggestionItemUsernameTextStyle = mentionSuggestionItemUsernameTextStyle,
                    mentionSuggestionItemMentionText = mentionSuggestionItemMentionText,
                    mentionSuggestionItemMentionTextStyle = mentionSuggestionItemMentionTextStyle,
                    // Center content
                    messageInputCommandsHandlingEnabled = messageInputCommandsHandlingEnabled,
                    messageInputMentionsHandlingEnabled = messageInputMentionsHandlingEnabled,
                    messageInputTextStyle = messageInputTextStyle,
                    messageInputBackgroundDrawable = messageInputBackgroundDrawable,
                    messageInputCursorDrawable = messageInputCursorDrawable,
                    messageInputScrollbarEnabled = messageInputScrollbarEnabled,
                    messageInputScrollbarFadingEnabled = messageInputScrollbarFadingEnabled,
                    messageInputMaxLines = messageInputMaxLines,
                    messageInputCannotSendHintText = messageInputCannotSendHintText,
                    messageInputInputType = messageInputInputType,
                    messageInputVideoAttachmentIconDrawable = messageInputVideoAttachmentIconDrawable,
                    messageInputVideoAttachmentIconDrawableTint = messageInputVideoAttachmentIconDrawableTint,
                    messageInputVideoAttachmentIconBackgroundColor = messageInputVideoAttachmentIconBackgroundColor,
                    messageInputVideoAttachmentIconCornerRadius = messageInputVideoAttachmentIconCornerRadius,
                    messageInputVideoAttachmentIconElevation = messageInputVideoAttachmentIconElevation,
                    messageInputVideoAttachmentIconDrawablePaddingTop = messageInputVideoAttachmentIconDrawablePadding
                        ?: messageInputVideoAttachmentIconDrawablePaddingTop,
                    messageInputVideoAttachmentIconDrawablePaddingBottom = messageInputVideoAttachmentIconDrawablePadding
                        ?: messageInputVideoAttachmentIconDrawablePaddingBottom,
                    messageInputVideoAttachmentIconDrawablePaddingStart = messageInputVideoAttachmentIconDrawablePadding
                        ?: messageInputVideoAttachmentIconDrawablePaddingStart,
                    messageInputVideoAttachmentIconDrawablePaddingEnd = messageInputVideoAttachmentIconDrawablePadding
                        ?: messageInputVideoAttachmentIconDrawablePaddingEnd,
                    // Leading content
                    attachmentsButtonVisible = attachmentsButtonVisible,
                    attachmentsButtonIconDrawable = attachmentsButtonIconDrawable,
                    attachmentsButtonRippleColor = attachmentsButtonRippleColor,
                    commandsButtonVisible = commandsButtonVisible,
                    commandsButtonIconDrawable = commandsButtonIconDrawable,
                    commandsButtonRippleColor = commandsButtonRippleColor,
                    // Footer content
                    alsoSendToChannelCheckboxVisible = alsoSendToChannelCheckboxVisible,
                    alsoSendToChannelCheckboxDrawable = alsoSendToChannelCheckboxDrawable,
                    alsoSendToChannelCheckboxText = alsoSendToChannelCheckboxText,
                    alsoSendToChannelCheckboxTextStyle = alsoSendToChannelCheckboxTextStyle,
                    // Header content
                    editModeText = editModeText,
                    editModeIconDrawable = editModeIconDrawable,
                    replyModeText = replyModeText,
                    replyModeIconDrawable = replyModeIconDrawable,
                    dismissModeIconDrawable = dismissModeIconDrawable,
                    // Trailing content
                    sendMessageButtonEnabled = sendMessageButtonEnabled,
                    sendMessageButtonIconDrawable = sendMessageButtonIconDrawable,
                    cooldownTimerTextStyle = cooldownTimerTextStyle,
                    cooldownTimerBackgroundDrawable = cooldownTimerBackgroundDrawable,
                ).let(TransformStyle.messageComposerStyleTransformer::transform)
            }
        }
    }
}
