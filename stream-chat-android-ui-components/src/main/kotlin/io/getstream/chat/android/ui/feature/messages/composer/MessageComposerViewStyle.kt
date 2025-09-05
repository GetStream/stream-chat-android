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
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.InputType
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.permissions.VisualMediaType
import io.getstream.chat.android.ui.feature.messages.common.AudioRecordPlayerViewStyle
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.AttachmentsPickerDialogStyle
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.PickerMediaMode
import io.getstream.chat.android.ui.feature.messages.list.MessageReplyStyle
import io.getstream.chat.android.ui.font.TextStyle
import io.getstream.chat.android.ui.helper.TransformStyle
import io.getstream.chat.android.ui.helper.ViewStyle
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import io.getstream.chat.android.ui.utils.extensions.dpToPxPrecise
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getColorOrNull
import io.getstream.chat.android.ui.utils.extensions.getColorStateListCompat
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.getDimensionOrNull
import io.getstream.chat.android.ui.utils.extensions.getDrawableCompat
import io.getstream.chat.android.ui.utils.extensions.getEnum
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
 * @param commandSuggestionsTitleIconDrawableTintColor The tint applied to the icon for the title at the top
 * of the command suggestions dialog.
 * @param commandSuggestionsBackgroundColor The background color of the command suggestions dialog.
 * @param commandSuggestionItemCommandNameTextStyle The text style for the command name.
 * @param commandSuggestionItemCommandDescriptionText The command description template with two placeholders.
 * @param commandSuggestionItemCommandDescriptionTextStyle The text style for the command description.
 * @param mentionSuggestionsBackgroundColor The background color of the mention suggestions dialog.
 * @param mentionSuggestionItemIconDrawable The icon for each command icon in the suggestion list.
 * @param mentionSuggestionItemIconDrawableTintColor The tint applied to the icon for each command icon
 * in the suggestion list.
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
 * @param messageInputShowReplyView Whether to show the default reply view inside the message input or not.
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
 * @param audioRecordingButtonVisible If the button to record audio is displayed.
 * @param audioRecordingButtonEnabled If the button to record audio is enabled.
 * @param audioRecordingButtonPreferred If the button to record audio is displayed over send button while input is
 * empty.
 * @param audioRecordingButtonIconDrawable The icon for the button to record audio.
 * @param audioRecordingButtonIconTintList The tint list for the button to record audio.
 * @param audioRecordingButtonWidth The width of the button to record audio.
 * @param audioRecordingButtonHeight The height of the button to record audio.
 * @param audioRecordingButtonPadding The padding of the button to record audio.
 * @param audioRecordingHoldToRecordText The info text that will be shown if touch event on audio button was too short.
 * @param audioRecordingHoldToRecordTextStyle The text style that will be used for the "hold to record" text.
 * @param audioRecordingHoldToRecordBackgroundDrawable The drawable will be used as a background for the "hold to
 * record" text.
 * @param audioRecordingHoldToRecordBackgroundDrawableTint The tint color will be used for background drawable of
 * the "hold to record" text.
 * @param audioRecordingSlideToCancelText The info text that will be shown while holding mic button.
 * @param audioRecordingSlideToCancelTextStyle The text style that will be used for the "slide to cancel" text.
 * @param audioRecordingSlideToCancelStartDrawable The icon that will be displayed in front of the
 * "slide to cancel" text.
 * @param audioRecordingSlideToCancelStartDrawableTint The tint color that will be used for the icon in front of the
 * "slide to cancel" text.
 * @param audioRecordingFloatingButtonIconDrawable The icon that will be displayed in inside the floating draggable
 * button while recording.
 * @param audioRecordingFloatingButtonIconDrawableTint The tint color that will be used for the the mic icon inside
 * the floating draggable button.
 * @param audioRecordingFloatingButtonBackgroundDrawable The background drawable that will be applied to the floating
 * draggable button while recording.
 * @param audioRecordingFloatingButtonBackgroundDrawableTint The tint color that will be used for the background
 * drawable in the floating draggable button.
 * @param audioRecordingFloatingLockIconDrawable The floating icon that will be displayed above floating button
 * while unlocked.
 * @param audioRecordingFloatingLockIconDrawableTint The tint color that will be used for the the lock icon.
 * @param audioRecordingFloatingLockedIconDrawable The floating icon that will be displayed above recording view
 * when locked.
 * @param audioRecordingFloatingLockedIconDrawableTint The tint color that will be used for the the locked icon.
 * @param audioRecordingWaveformColor The color of the waveform.
 * @param attachmentsButtonVisible If the button to pick attachments is displayed.
 * @param attachmentsButtonIconDrawable The icon for the attachments button.
 * @param attachmentsButtonIconTintList The tint list for the attachments button.
 * @param attachmentsButtonRippleColor Ripple color of the attachments button.
 * @param commandsButtonVisible If the button to select commands is displayed.
 * @param commandsButtonIconDrawable The icon for the commands button.
 * @param commandsButtonIconTintList The tint list for the commands button.
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
 * @param sendMessageButtonIconTintList The tint list for the button to send message.
 * @param cooldownTimerTextStyle The text style that will be used for cooldown timer.
 * @param cooldownTimerBackgroundDrawable Background drawable for cooldown timer.
 * @param messageReplyShowUserAvatar Whether to show user avatar in the reply view. Default value is `true`.
 * @param messageReplyBackgroundColor Sets the background color of the quoted message bubble visible in the composer
 * when replying to a message.
 * @param messageReplyTextStyleMine  Sets the style of the text inside the quoted message bubble visible in the composer
 * when replying to a message. Applied to messages sent by the current user.
 * @param messageReplyMessageBackgroundStrokeColorMine Sets the color of the stroke of the quoted message bubble visible
 * in the composer when replying to a message. Applied to messages sent by the current user.
 * @param messageReplyMessageBackgroundStrokeWidthMine Sets the width of the stroke of the quoted message bubble visible
 * in the composer when replying to a message. Applied to messages sent by the current user.
 * @param messageReplyTextStyleTheirs  Sets the style of the text inside the quoted message bubble visible in the
 * composer when replying to a message. Applied to messages sent by users other than the currently logged in one.
 * @param messageReplyMessageBackgroundStrokeColorTheirs Sets the color of the stroke of the quoted message bubble
 * visible in the composer when replying to a message. Applied to messages sent by users other than the currently
 * logged in one.
 * @param messageReplyMessageBackgroundStrokeWidthTheirs Sets the width of the stroke of the quoted message bubble
 * visible in the composer when replying to a message. Applied to messages sent by users other than the currently
 * logged in one.
 */
public data class MessageComposerViewStyle(
    @ColorInt public val backgroundColor: Int,
    @Deprecated(
        message = "Use the " +
            "commandSuggestionsTitleIconDrawableTintColor" +
            "/mentionSuggestionItemIconDrawableTintColor " +
            "/attachmentsButtonIconTintList " +
            "/commandsButtonIconTintList " +
            "/sendMessageButtonIconTintList " +
            "property instead.",
        replaceWith = ReplaceWith("proper button tint property"),
        level = DeprecationLevel.WARNING,
    )
    @ColorInt public val buttonIconDrawableTintColor: Int?,
    public val dividerBackgroundDrawable: Drawable,
    // Command suggestions content
    public val commandSuggestionsTitleText: String,
    public val commandSuggestionsTitleTextStyle: TextStyle,
    public val commandSuggestionsTitleIconDrawable: Drawable,
    public val commandSuggestionsTitleIconDrawableTintColor: Int?,
    @ColorInt public val commandSuggestionsBackgroundColor: Int,
    public val commandSuggestionItemCommandNameTextStyle: TextStyle,
    public val commandSuggestionItemCommandDescriptionText: String,
    public val commandSuggestionItemCommandDescriptionTextStyle: TextStyle,
    // Mention suggestions content
    @ColorInt public val mentionSuggestionsBackgroundColor: Int,
    public val mentionSuggestionItemIconDrawable: Drawable,
    public val mentionSuggestionItemIconDrawableTintColor: Int?,
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
    public val messageInputShowReplyView: Boolean,
    public val messageInputVideoAttachmentIconDrawable: Drawable,
    @ColorInt public val messageInputVideoAttachmentIconDrawableTint: Int?,
    @ColorInt public val messageInputVideoAttachmentIconBackgroundColor: Int?,
    public val messageInputVideoAttachmentIconCornerRadius: Float,
    public val messageInputVideoAttachmentIconElevation: Float,
    public val messageInputVideoAttachmentIconDrawablePaddingTop: Int,
    public val messageInputVideoAttachmentIconDrawablePaddingBottom: Int,
    public val messageInputVideoAttachmentIconDrawablePaddingStart: Int,
    public val messageInputVideoAttachmentIconDrawablePaddingEnd: Int,
    // Center overlap content
    public val audioRecordingHoldToRecordText: String,
    public val audioRecordingHoldToRecordTextStyle: TextStyle,
    public val audioRecordingHoldToRecordBackgroundDrawable: Drawable,
    @ColorInt public val audioRecordingHoldToRecordBackgroundDrawableTint: Int?,
    public val audioRecordingSlideToCancelText: String,
    public val audioRecordingSlideToCancelTextStyle: TextStyle,
    public val audioRecordingSlideToCancelStartDrawable: Drawable,
    @ColorInt public val audioRecordingSlideToCancelStartDrawableTint: Int?,
    public val audioRecordingFloatingButtonIconDrawable: Drawable,
    @ColorInt public val audioRecordingFloatingButtonIconDrawableTint: Int?,
    public val audioRecordingFloatingButtonBackgroundDrawable: Drawable,
    @ColorInt public val audioRecordingFloatingButtonBackgroundDrawableTint: Int?,
    public val audioRecordingFloatingLockIconDrawable: Drawable,
    @ColorInt public val audioRecordingFloatingLockIconDrawableTint: Int?,
    public val audioRecordingFloatingLockedIconDrawable: Drawable,
    @ColorInt public val audioRecordingFloatingLockedIconDrawableTint: Int?,
    @ColorInt public val audioRecordingWaveformColor: Int?,
    // Leading content
    public val attachmentsButtonVisible: Boolean,
    public val attachmentsButtonIconDrawable: Drawable,
    public val attachmentsButtonIconTintList: ColorStateList?,
    @ColorInt public val attachmentsButtonRippleColor: Int?,
    public val commandsButtonVisible: Boolean,
    public val commandsButtonIconDrawable: Drawable,
    public val commandsButtonIconTintList: ColorStateList?,
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
    public val sendMessageButtonIconTintList: ColorStateList?,
    @Px public val sendMessageButtonWidth: Int,
    @Px public val sendMessageButtonHeight: Int,
    @Px public val sendMessageButtonPadding: Int,
    public val audioRecordingButtonVisible: Boolean,
    public val audioRecordingButtonEnabled: Boolean,
    public val audioRecordingButtonPreferred: Boolean,
    public val audioRecordingButtonIconDrawable: Drawable,
    public val audioRecordingButtonIconTintList: ColorStateList?,
    @Px public val audioRecordingButtonWidth: Int,
    @Px public val audioRecordingButtonHeight: Int,
    @Px public val audioRecordingButtonPadding: Int,
    public val cooldownTimerTextStyle: TextStyle,
    public val cooldownTimerBackgroundDrawable: Drawable,
    // Message reply customization, by default belongs to center content as well
    public val messageReplyShowUserAvatar: Boolean = true,
    @ColorInt public val messageReplyBackgroundColor: Int,
    public val messageReplyTextStyleMine: TextStyle,
    @ColorInt public val messageReplyMessageBackgroundStrokeColorMine: Int,
    @Px public val messageReplyMessageBackgroundStrokeWidthMine: Float,
    public val messageReplyTextStyleTheirs: TextStyle,
    @ColorInt public val messageReplyMessageBackgroundStrokeColorTheirs: Int,
    @Px public val messageReplyMessageBackgroundStrokeWidthTheirs: Float,
    public val attachmentsPickerDialogStyle: AttachmentsPickerDialogStyle,
    public val audioRecordPlayerViewStyle: AudioRecordPlayerViewStyle?,
) : ViewStyle {

    /**
     * Creates an instance of [MessageReplyStyle] from the parameters provided by [MessageComposerViewStyle].
     *
     * @return an instance of [MessageReplyStyle].
     */
    public fun toMessageReplyStyle(): MessageReplyStyle = MessageReplyStyle(
        showUserAvatar = messageReplyShowUserAvatar,
        messageBackgroundColorMine = messageReplyBackgroundColor,
        messageBackgroundColorTheirs = messageReplyBackgroundColor,
        linkBackgroundColorMine = messageReplyBackgroundColor,
        linkBackgroundColorTheirs = messageReplyBackgroundColor,
        textStyleMine = messageReplyTextStyleMine,
        textStyleTheirs = messageReplyTextStyleTheirs,
        linkStyleMine = messageReplyTextStyleMine,
        linkStyleTheirs = messageReplyTextStyleTheirs,
        messageStrokeColorMine = messageReplyMessageBackgroundStrokeColorMine,
        messageStrokeColorTheirs = messageReplyMessageBackgroundStrokeColorTheirs,
        messageStrokeWidthMine = messageReplyMessageBackgroundStrokeWidthMine,
        messageStrokeWidthTheirs = messageReplyMessageBackgroundStrokeWidthTheirs,
    )

    public companion object {

        @Suppress("MaxLineLength", "ComplexMethod", "LongMethod")
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
                    R.styleable.MessageComposerView_streamUiMessageComposerIconDrawableTintColor,
                )

                val dividerBackgroundDrawable = a.getDrawable(
                    R.styleable.MessageComposerView_streamUiMessageComposerDividerBackgroundDrawable,
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_divider)!!

                /**
                 * Command suggestions content
                 */
                val commandSuggestionsTitleText = a.getString(
                    R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionsTitleText,
                ) ?: context.getString(R.string.stream_ui_message_composer_instant_commands)

                val commandSuggestionsTitleTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionsTitleTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium),
                    )
                    .color(
                        R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionsTitleTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary),
                    )
                    .font(
                        R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionsTitleTextFontAssets,
                        R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionsTitleTextFont,
                    )
                    .style(
                        R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionsTitleTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()

                val commandSuggestionsTitleIconDrawable = a.getDrawable(
                    R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionsTitleIconDrawable,
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_command_blue)!!

                val commandSuggestionsTitleIconDrawableTintColor = a.getColorOrNull(
                    R.styleable.MessageComposerView_streamUiMessageComposerMentionSuggestionItemIconDrawableTintColor,
                )

                val commandSuggestionsBackgroundColor = a.getColor(
                    R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionsBackgroundColor,
                    context.getColorCompat(R.color.stream_ui_white),
                )

                val commandSuggestionItemCommandNameTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionItemCommandNameTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium),
                    )
                    .color(
                        R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionItemCommandNameTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary),
                    )
                    .font(
                        R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionItemCommandNameTextFontAssets,
                        R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionItemCommandNameTextFont,
                    )
                    .style(
                        R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionItemCommandNameTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()

                val commandSuggestionItemCommandDescriptionText = a.getString(
                    R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionItemCommandDescriptionText,
                ) ?: context.getString(R.string.stream_ui_message_composer_command_template)

                val commandSuggestionItemCommandDescriptionTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionItemCommandDescriptionTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium),
                    )
                    .color(
                        R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionItemCommandDescriptionTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary),
                    )
                    .font(
                        R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionItemCommandDescriptionTextFontAssets,
                        R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionItemCommandDescriptionTextFont,
                    )
                    .style(
                        R.styleable.MessageComposerView_streamUiMessageComposerCommandSuggestionItemCommandDescriptionTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()

                /**
                 * Mention suggestions content
                 */
                val mentionSuggestionsBackgroundColor = a.getColor(
                    R.styleable.MessageComposerView_streamUiMessageComposerMentionSuggestionsBackgroundColor,
                    context.getColorCompat(R.color.stream_ui_white),
                )

                val mentionSuggestionItemIconDrawable: Drawable = a.getDrawable(
                    R.styleable.MessageComposerView_streamUiMessageComposerMentionSuggestionItemIconDrawable,
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_mention)!!

                val mentionSuggestionItemIconDrawableTintColor = a.getColorOrNull(
                    R.styleable.MessageComposerView_streamUiMessageComposerMentionSuggestionItemIconDrawableTintColor,
                )

                val mentionSuggestionItemUsernameTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageComposerView_streamUiMessageComposerMentionSuggestionItemUsernameTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium),
                    )
                    .color(
                        R.styleable.MessageComposerView_streamUiMessageComposerMentionSuggestionItemUsernameTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary),
                    )
                    .font(
                        R.styleable
                            .MessageComposerView_streamUiMessageComposerMentionSuggestionItemUsernameTextFontAssets,
                        R.styleable.MessageComposerView_streamUiMessageComposerMentionSuggestionItemUsernameTextFont,
                    )
                    .style(
                        R.styleable.MessageComposerView_streamUiMessageComposerMentionSuggestionItemUsernameTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()

                val mentionSuggestionItemMentionText = a.getString(
                    R.styleable.MessageComposerView_streamUiMessageComposerMentionSuggestionItemMentionText,
                ) ?: context.getString(R.string.stream_ui_message_composer_mention_template)

                val mentionSuggestionItemMentionTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageComposerView_streamUiMessageComposerMentionSuggestionItemMentionTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium),
                    )
                    .color(
                        R.styleable.MessageComposerView_streamUiMessageComposerMentionSuggestionItemMentionTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary),
                    )
                    .font(
                        R.styleable.MessageComposerView_streamUiMessageComposerMentionSuggestionItemMentionTextFontAssets,
                        R.styleable.MessageComposerView_streamUiMessageComposerMentionSuggestionItemMentionTextFont,
                    )
                    .style(
                        R.styleable.MessageComposerView_streamUiMessageComposerMentionSuggestionItemMentionTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()

                /**
                 * Center content
                 */
                val messageInputCommandsHandlingEnabled = a.getBoolean(
                    R.styleable.MessageComposerView_streamUiMessageComposerCommandsHandlingEnabled,
                    true,
                )

                val messageInputMentionsHandlingEnabled = a.getBoolean(
                    R.styleable.MessageComposerView_streamUiMessageComposerMentionsHandlingEnabled,
                    true,
                )

                val messageInputTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageInputTextSize,
                        context.resources.getDimensionPixelSize(R.dimen.stream_ui_text_size_input),
                    )
                    .color(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageInputTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary),
                    )
                    .font(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageInputTextFontAssets,
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageInputTextFont,
                    )
                    .style(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageInputTextStyle,
                        Typeface.NORMAL,
                    )
                    .hint(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageInputHintText,
                        context.getString(R.string.stream_ui_message_composer_hint_normal),
                    )
                    .hintColor(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageInputHintColor,
                        context.getColorCompat(R.color.stream_ui_text_color_hint),
                    )
                    .build()

                val messageInputBackgroundDrawable = a.getDrawable(
                    R.styleable.MessageComposerView_streamUiMessageComposerMessageInputBackgroundDrawable,
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_shape_edit_text_round)!!

                val messageInputCursorDrawable: Drawable? = a.getDrawable(
                    R.styleable.MessageComposerView_streamUiMessageComposerMessageInputCursorDrawable,
                )

                val messageInputScrollbarEnabled = a.getBoolean(
                    R.styleable.MessageComposerView_streamUiMessageComposerScrollbarEnabled,
                    false,
                )
                val messageInputScrollbarFadingEnabled = a.getBoolean(
                    R.styleable.MessageComposerView_streamUiMessageComposerScrollbarFadingEnabled,
                    false,
                )

                val messageInputMaxLines = a.getInt(
                    R.styleable.MessageComposerView_streamUiMessageComposerMessageInputMaxLines,
                    7,
                )

                val messageInputCannotSendHintText = a.getString(
                    R.styleable.MessageComposerView_streamUiMessageComposerMessageInputCannotSendHintText,
                ) ?: context.getString(R.string.stream_ui_message_composer_hint_cannot_send_message)

                /**
                 * Center overlap content
                 */
                val audioRecordingHoldToRecordText = a.getString(
                    R.styleable.MessageComposerView_streamUiMessageComposerAudioRecordingHoldToRecordText,
                ) ?: context.getString(R.string.stream_ui_message_composer_hold_to_record)
                val audioRecordingHoldToRecordTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageComposerView_streamUiMessageComposerAudioRecordingHoldToRecordTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium),
                    )
                    .color(
                        R.styleable.MessageComposerView_streamUiMessageComposerAudioRecordingHoldToRecordTextColor,
                        context.getColorCompat(R.color.stream_ui_white_snow),
                    )
                    .font(
                        R.styleable.MessageComposerView_streamUiMessageComposerAudioRecordingHoldToRecordTextFontAssets,
                        R.styleable.MessageComposerView_streamUiMessageComposerAudioRecordingHoldToRecordTextFont,
                    )
                    .style(
                        R.styleable.MessageComposerView_streamUiMessageComposerAudioRecordingHoldToRecordTextStyle,
                        Typeface.BOLD,
                    )
                    .build()
                val audioRecordingHoldToRecordBackgroundDrawable = a.getDrawableCompat(
                    context,
                    R.styleable.MessageComposerView_streamUiMessageComposerAudioRecordingHoldToRecordBackgroundDrawable,
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_message_composer_audio_record_hold_background)!!

                val audioRecordingHoldToRecordBackgroundDrawableTint = a.getColorOrNull(
                    R.styleable.MessageComposerView_streamUiMessageComposerAudioRecordingHoldToRecordBackgroundDrawableTint,
                )

                val audioRecordingSlideToCancelText = a.getString(
                    R.styleable.MessageComposerView_streamUiMessageComposerAudioRecordingSlideToCancelText,
                ) ?: context.getString(R.string.stream_ui_message_composer_slide_to_cancel)
                val audioRecordingSlideToCancelTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageComposerView_streamUiMessageComposerAudioRecordingSlideToCancelTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium),
                    )
                    .color(
                        R.styleable.MessageComposerView_streamUiMessageComposerAudioRecordingSlideToCancelTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary),
                    )
                    .font(
                        R.styleable.MessageComposerView_streamUiMessageComposerAudioRecordingSlideToCancelTextFontAssets,
                        R.styleable.MessageComposerView_streamUiMessageComposerAudioRecordingSlideToCancelTextFont,
                    )
                    .style(
                        R.styleable.MessageComposerView_streamUiMessageComposerAudioRecordingSlideToCancelTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()
                val audioRecordingSlideToCancelStartDrawable = a.getDrawableCompat(
                    context,
                    R.styleable.MessageComposerView_streamUiMessageComposerAudioRecordingSlideToCancelStartDrawable,
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_arrow_left)!!

                val audioRecordingSlideToCancelStartDrawableTint = a.getColorOrNull(
                    R.styleable.MessageComposerView_streamUiMessageComposerAudioRecordingSlideToCancelStartDrawableTint,
                )
                val audioRecordingFloatingButtonIconDrawable = a.getDrawableCompat(
                    context,
                    R.styleable.MessageComposerView_streamUiMessageComposerAudioRecordingFloatingButtonIconDrawable,
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_mic)!!
                val audioRecordingFloatingButtonIconDrawableTint = a.getColorOrNull(
                    R.styleable.MessageComposerView_streamUiMessageComposerAudioRecordingFloatingButtonIconDrawableTint,
                )

                val audioRecordingFloatingButtonBackgroundDrawable = a.getDrawableCompat(
                    context,
                    R.styleable.MessageComposerView_streamUiMessageComposerAudioRecordingFloatingButtonBackgroundDrawable,
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_message_composer_audio_record_mic_background)!!
                val audioRecordingFloatingButtonBackgroundDrawableTint = a.getColorOrNull(
                    R.styleable.MessageComposerView_streamUiMessageComposerAudioRecordingFloatingButtonBackgroundDrawableTint,
                )

                val audioRecordingFloatingLockIconDrawable = a.getDrawableCompat(
                    context,
                    R.styleable.MessageComposerView_streamUiMessageComposerAudioRecordingFloatingLockIconDrawable,
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_mic_lock)!!
                val audioRecordingFloatingLockIconDrawableTint = a.getColorOrNull(
                    R.styleable.MessageComposerView_streamUiMessageComposerAudioRecordingFloatingLockIconDrawableTint,
                )

                val audioRecordingFloatingLockedIconDrawable = a.getDrawableCompat(
                    context,
                    R.styleable.MessageComposerView_streamUiMessageComposerAudioRecordingFloatingLockedIconDrawable,
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_mic_locked)!!
                val audioRecordingFloatingLockedIconDrawableTint = a.getColorOrNull(
                    R.styleable.MessageComposerView_streamUiMessageComposerAudioRecordingFloatingLockedIconDrawableTint,
                )
                val audioRecordingWaveformColor = a.getColorOrNull(
                    R.styleable.MessageComposerView_streamUiMessageComposerAudioRecordingWaveformColor,
                )

                /**
                 * Leading content
                 */
                val attachmentsButtonVisible = a.getBoolean(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsButtonVisible,
                    true,
                )

                val attachmentsButtonIconDrawable = a.getDrawableCompat(
                    context,
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsButtonIconDrawable,
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_attach)!!

                val attachmentsButtonIconTintList = a.getColorStateListCompat(
                    context,
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsButtonIconTintList,
                )

                val attachmentsButtonRippleColor = a.getColorOrNull(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsButtonRippleColor,
                )

                val commandsButtonVisible = a.getBoolean(
                    R.styleable.MessageComposerView_streamUiMessageComposerCommandsButtonVisible,
                    true,
                )

                val commandsButtonIconDrawable = a.getDrawableCompat(
                    context,
                    R.styleable.MessageComposerView_streamUiMessageComposerCommandsButtonIconDrawable,
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_command)!!

                val commandsButtonIconTintList = a.getColorStateListCompat(
                    context,
                    R.styleable.MessageComposerView_streamUiMessageComposerCommandsButtonIconTintList,
                )

                val commandsButtonRippleColor = a.getColorOrNull(
                    R.styleable.MessageComposerView_streamUiMessageComposerCommandsButtonRippleColor,
                )

                /**
                 * Footer content
                 */
                val alsoSendToChannelCheckboxVisible = a.getBoolean(
                    R.styleable.MessageComposerView_streamUiMessageComposerAlsoSendToChannelCheckboxVisible,
                    true,
                )

                val alsoSendToChannelCheckboxDrawable = a.getDrawable(
                    R.styleable.MessageComposerView_streamUiMessageComposerAlsoSendToChannelCheckboxDrawable,
                )

                val alsoSendToChannelCheckboxText: String = a.getText(
                    R.styleable.MessageComposerView_streamUiMessageComposerAlsoSendToChannelCheckboxText,
                )?.toString() ?: context.getString(R.string.stream_ui_message_composer_send_to_channel)

                val alsoSendToChannelCheckboxTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageComposerView_streamUiMessageComposerAlsoSendToChannelCheckboxTextSize,
                        context.getDimension(R.dimen.stream_ui_text_small),
                    )
                    .color(
                        R.styleable.MessageComposerView_streamUiMessageComposerAlsoSendToChannelCheckboxTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary),
                    )
                    .font(
                        R.styleable.MessageComposerView_streamUiMessageComposerAlsoSendToChannelCheckboxTextFontAssets,
                        R.styleable.MessageComposerView_streamUiMessageComposerAlsoSendToChannelCheckboxTextFont,
                    )
                    .style(
                        R.styleable.MessageComposerView_streamUiMessageComposerAlsoSendToChannelCheckboxTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()

                /**
                 * Header content
                 */
                val editModeText: String = a.getText(
                    R.styleable.MessageComposerView_streamUiMessageComposerEditModeText,
                )?.toString() ?: context.getString(R.string.stream_ui_message_composer_mode_edit)

                val editModeIconDrawable = a.getDrawable(
                    R.styleable.MessageComposerView_streamUiMessageComposerEditModeIconDrawable,
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_edit)!!

                val replyModeText: String = a.getText(
                    R.styleable.MessageComposerView_streamUiMessageComposerReplyModeText,
                )?.toString() ?: context.getString(R.string.stream_ui_message_composer_mode_reply)

                val replyModeIconDrawable = a.getDrawable(
                    R.styleable.MessageComposerView_streamUiMessageComposerReplyModeIconDrawable,
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_arrow_curve_left_grey)!!

                val dismissModeIconDrawable = a.getDrawable(
                    R.styleable.MessageComposerView_streamUiMessageComposerDismissModeIconDrawable,
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_clear)!!

                /**
                 * Trailing content
                 */
                val sendMessageButtonEnabled = a.getBoolean(
                    R.styleable.MessageComposerView_streamUiMessageComposerSendMessageButtonEnabled,
                    true,
                )

                val sendMessageButtonIconDrawable = a.getDrawableCompat(
                    context,
                    R.styleable.MessageComposerView_streamUiMessageComposerSendMessageButtonIconDrawable,
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_send_message)!!

                val sendMessageButtonIconTintList = a.getColorStateListCompat(
                    context,
                    R.styleable.MessageComposerView_streamUiMessageComposerSendMessageButtonIconTintList,
                )

                val sendMessageButtonWidth: Int =
                    a.getDimensionPixelSize(
                        R.styleable.MessageComposerView_streamUiMessageComposerSendMessageButtonWidth,
                        DEFAULT_TRAILING_BUTTON_SIZE.dpToPx(),
                    )
                val sendMessageButtonHeight: Int =
                    a.getDimensionPixelSize(
                        R.styleable.MessageComposerView_streamUiMessageComposerSendMessageButtonHeight,
                        DEFAULT_TRAILING_BUTTON_SIZE.dpToPx(),
                    )
                val sendMessageButtonPadding: Int =
                    a.getDimensionPixelSize(
                        R.styleable.MessageComposerView_streamUiMessageComposerSendMessageButtonPadding,
                        DEFAULT_TRAILING_BUTTON_PADDING.dpToPx(),
                    )

                val audioRecordingButtonEnabled = a.getBoolean(
                    R.styleable.MessageComposerView_streamUiMessageComposerAudioRecordingButtonEnabled,
                    true,
                )
                val audioRecordingButtonVisible = a.getBoolean(
                    R.styleable.MessageComposerView_streamUiMessageComposerAudioRecordingButtonVisible,
                    false,
                )
                val audioRecordingButtonPreferred = a.getBoolean(
                    R.styleable.MessageComposerView_streamUiMessageComposerAudioRecordingButtonPreferred,
                    false,
                )
                val audioRecordingButtonIconDrawable = a.getDrawableCompat(
                    context,
                    R.styleable.MessageComposerView_streamUiMessageComposerAudioRecordingButtonIconDrawable,
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_mic)!!

                val audioRecordingButtonIconTintList = a.getColorStateListCompat(
                    context,
                    R.styleable.MessageComposerView_streamUiMessageComposerAudioRecordingButtonIconTintList,
                )

                val audioRecordingButtonWidth: Int =
                    a.getDimensionPixelSize(
                        R.styleable.MessageComposerView_streamUiMessageComposerAudioRecordingButtonWidth,
                        DEFAULT_TRAILING_BUTTON_SIZE.dpToPx(),
                    )
                val audioRecordingButtonHeight: Int =
                    a.getDimensionPixelSize(
                        R.styleable.MessageComposerView_streamUiMessageComposerAudioRecordingButtonHeight,
                        DEFAULT_TRAILING_BUTTON_SIZE.dpToPx(),
                    )
                val audioRecordingButtonPadding: Int =
                    a.getDimensionPixelSize(
                        R.styleable.MessageComposerView_streamUiMessageComposerAudioRecordingButtonPadding,
                        DEFAULT_TRAILING_BUTTON_PADDING.dpToPx(),
                    )

                val cooldownTimerTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageComposerView_streamUiMessageComposerCooldownTimerTextSize,
                        context.getDimension(R.dimen.stream_ui_text_large),
                    )
                    .color(
                        R.styleable.MessageComposerView_streamUiMessageComposerCooldownTimerTextColor,
                        context.getColorCompat(R.color.stream_ui_literal_white),
                    )
                    .font(
                        R.styleable.MessageComposerView_streamUiMessageComposerCooldownTimerTextFontAssets,
                        R.styleable.MessageComposerView_streamUiMessageComposerCooldownTimerTextFont,
                    )
                    .style(
                        R.styleable.MessageComposerView_streamUiMessageComposerCooldownTimerTextStyle,
                        Typeface.BOLD,
                    )
                    .build()

                val cooldownTimerBackgroundDrawable = a.getDrawable(
                    R.styleable.MessageComposerView_streamUiMessageComposerCooldownTimerBackgroundDrawable,
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_cooldown_badge_background)!!

                val messageInputInputType = a.getInt(
                    R.styleable.MessageComposerView_android_inputType,
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                        InputType.TYPE_TEXT_FLAG_CAP_SENTENCES,
                )

                val messageInputShowReplyView = a.getBoolean(
                    R.styleable.MessageComposerView_streamUiMessageComposerShowMessageReplyView,
                    true,
                )

                val messageInputVideoAttachmentIconDrawable =
                    a.getDrawable(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageInputVideoAttachmentIconDrawable,
                    )
                        ?: ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_play)!!

                val messageInputVideoAttachmentIconDrawableTint =
                    a.getColorOrNull(R.styleable.MessageComposerView_streamUiMessageComposerMessageInputVideoAttachmentIconDrawableTint)

                val messageInputVideoAttachmentIconBackgroundColor =
                    a.getColorOrNull(R.styleable.MessageComposerView_streamUiMessageComposerMessageInputVideoAttachmentIconBackgroundColor)

                val messageInputVideoAttachmentIconCornerRadius =
                    a.getDimension(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageInputVideoCornerRadius,
                        20.dpToPxPrecise(),
                    )

                val messageInputVideoAttachmentIconElevation =
                    a.getDimension(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageInputVideoAttachmentIconElevation,
                        0f,
                    )

                val messageInputVideoAttachmentIconDrawablePaddingTop =
                    a.getDimensionPixelSize(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageInputVideoAttachmentIconDrawablePaddingTop,
                        0,
                    )

                val messageInputVideoAttachmentIconDrawablePaddingBottom =
                    a.getDimensionPixelSize(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageInputVideoAttachmentIconDrawablePaddingBottom,
                        0,
                    )

                val messageInputVideoAttachmentIconDrawablePaddingStart =
                    a.getDimensionPixelSize(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageInputVideoAttachmentIconDrawablePaddingStart,
                        0,
                    )

                val messageInputVideoAttachmentIconDrawablePaddingEnd =
                    a.getDimensionPixelSize(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageInputVideoAttachmentIconDrawablePaddingEnd,
                        0,
                    )

                val messageInputVideoAttachmentIconDrawablePadding =
                    a.getDimensionOrNull(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageInputVideoAttachmentIconDrawablePadding,
                    )?.toInt()

                val mediumTypeface = ResourcesCompat.getFont(context, R.font.stream_roboto_medium) ?: Typeface.DEFAULT

                val messageReplyShowUserAvatar = a.getBoolean(
                    R.styleable.MessageComposerView_streamUiMessageComposerMessageReplyShowUserAvatar,
                    true,
                )

                val messageReplyBackgroundColor: Int =
                    a.getColor(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageReplyBackgroundColor,
                        context.getColorCompat(R.color.stream_ui_white),
                    )

                val messageReplyTextStyleMine: TextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageReplyTextSizeMine,
                        context.getDimension(MessageReplyStyle.DEFAULT_TEXT_SIZE),
                    )
                    .color(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageReplyTextColorMine,
                        context.getColorCompat(MessageReplyStyle.DEFAULT_TEXT_COLOR),
                    )
                    .font(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageReplyTextFontAssetsMine,
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageReplyTextStyleMine,
                        mediumTypeface,
                    )
                    .style(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageReplyTextStyleMine,
                        MessageReplyStyle.DEFAULT_TEXT_STYLE,
                    )
                    .build()

                val messageReplyMessageBackgroundStrokeColorMine: Int =
                    a.getColor(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageReplyStrokeColorMine,
                        context.getColorCompat(R.color.stream_ui_grey_gainsboro),
                    )

                val messageReplyMessageBackgroundStrokeWidthMine: Float =
                    a.getDimension(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageReplyStrokeWidthMine,
                        DEFAULT_MESSAGE_REPLY_BACKGROUND_STROKE_WIDTH,
                    )

                val messageReplyTextStyleTheirs: TextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageReplyTextSizeTheirs,
                        context.getDimension(MessageReplyStyle.DEFAULT_TEXT_SIZE),
                    )
                    .color(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageReplyTextColorTheirs,
                        context.getColorCompat(MessageReplyStyle.DEFAULT_TEXT_COLOR),
                    )
                    .font(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageReplyTextFontAssetsTheirs,
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageReplyTextStyleTheirs,
                        mediumTypeface,
                    )
                    .style(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageReplyTextStyleTheirs,
                        MessageReplyStyle.DEFAULT_TEXT_STYLE,
                    )
                    .build()

                val messageReplyMessageBackgroundStrokeColorTheirs: Int =
                    a.getColor(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageReplyStrokeColorTheirs,
                        context.getColorCompat(R.color.stream_ui_grey_gainsboro),
                    )

                val messageReplyMessageBackgroundStrokeWidthTheirs: Float =
                    a.getDimension(
                        R.styleable.MessageComposerView_streamUiMessageComposerMessageReplyStrokeWidthTheirs,
                        DEFAULT_MESSAGE_REPLY_BACKGROUND_STROKE_WIDTH,
                    )

                var playerViewStyle: AudioRecordPlayerViewStyle? = null
                val playerViewStyleResId: Int = a.getResourceId(
                    R.styleable.MessageComposerView_streamUiMessageComposerAudioRecordPlayerViewStyle,
                    R.style.StreamUi_AudioRecordPlayerView,
                )
                if (playerViewStyleResId != R.style.StreamUi_AudioRecordPlayerView) {
                    context.obtainStyledAttributes(playerViewStyleResId, R.styleable.AudioRecordPlayerView).use {
                        playerViewStyle = AudioRecordPlayerViewStyle(
                            context = context, attributes = it,
                        )
                    }
                }

                return MessageComposerViewStyle(
                    backgroundColor = backgroundColor,
                    buttonIconDrawableTintColor = buttonIconDrawableTintColor,
                    dividerBackgroundDrawable = dividerBackgroundDrawable,
                    // Command suggestions content
                    commandSuggestionsTitleText = commandSuggestionsTitleText,
                    commandSuggestionsTitleTextStyle = commandSuggestionsTitleTextStyle,
                    commandSuggestionsTitleIconDrawable = commandSuggestionsTitleIconDrawable,
                    commandSuggestionsTitleIconDrawableTintColor = commandSuggestionsTitleIconDrawableTintColor,
                    commandSuggestionsBackgroundColor = commandSuggestionsBackgroundColor,
                    commandSuggestionItemCommandNameTextStyle = commandSuggestionItemCommandNameTextStyle,
                    commandSuggestionItemCommandDescriptionText = commandSuggestionItemCommandDescriptionText,
                    commandSuggestionItemCommandDescriptionTextStyle = commandSuggestionItemCommandDescriptionTextStyle,
                    // Mention suggestions content
                    mentionSuggestionsBackgroundColor = mentionSuggestionsBackgroundColor,
                    mentionSuggestionItemIconDrawable = mentionSuggestionItemIconDrawable,
                    mentionSuggestionItemIconDrawableTintColor = mentionSuggestionItemIconDrawableTintColor,
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
                    messageInputShowReplyView = messageInputShowReplyView,
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
                    // Center overlap content
                    audioRecordingHoldToRecordText = audioRecordingHoldToRecordText,
                    audioRecordingHoldToRecordTextStyle = audioRecordingHoldToRecordTextStyle,
                    audioRecordingHoldToRecordBackgroundDrawable = audioRecordingHoldToRecordBackgroundDrawable,
                    audioRecordingHoldToRecordBackgroundDrawableTint = audioRecordingHoldToRecordBackgroundDrawableTint,
                    audioRecordingSlideToCancelText = audioRecordingSlideToCancelText,
                    audioRecordingSlideToCancelTextStyle = audioRecordingSlideToCancelTextStyle,
                    audioRecordingSlideToCancelStartDrawable = audioRecordingSlideToCancelStartDrawable,
                    audioRecordingSlideToCancelStartDrawableTint = audioRecordingSlideToCancelStartDrawableTint,
                    audioRecordingFloatingButtonIconDrawable = audioRecordingFloatingButtonIconDrawable,
                    audioRecordingFloatingButtonIconDrawableTint = audioRecordingFloatingButtonIconDrawableTint,
                    audioRecordingFloatingButtonBackgroundDrawable = audioRecordingFloatingButtonBackgroundDrawable,
                    audioRecordingFloatingButtonBackgroundDrawableTint = audioRecordingFloatingButtonBackgroundDrawableTint,
                    audioRecordingFloatingLockIconDrawable = audioRecordingFloatingLockIconDrawable,
                    audioRecordingFloatingLockIconDrawableTint = audioRecordingFloatingLockIconDrawableTint,
                    audioRecordingFloatingLockedIconDrawable = audioRecordingFloatingLockedIconDrawable,
                    audioRecordingFloatingLockedIconDrawableTint = audioRecordingFloatingLockedIconDrawableTint,
                    audioRecordingWaveformColor = audioRecordingWaveformColor,
                    // Leading content
                    attachmentsButtonVisible = attachmentsButtonVisible,
                    attachmentsButtonIconDrawable = attachmentsButtonIconDrawable,
                    attachmentsButtonIconTintList = attachmentsButtonIconTintList,
                    attachmentsButtonRippleColor = attachmentsButtonRippleColor,
                    commandsButtonVisible = commandsButtonVisible,
                    commandsButtonIconDrawable = commandsButtonIconDrawable,
                    commandsButtonIconTintList = commandsButtonIconTintList,
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
                    sendMessageButtonIconTintList = sendMessageButtonIconTintList,
                    sendMessageButtonWidth = sendMessageButtonWidth,
                    sendMessageButtonHeight = sendMessageButtonHeight,
                    sendMessageButtonPadding = sendMessageButtonPadding,
                    audioRecordingButtonEnabled = audioRecordingButtonEnabled,
                    audioRecordingButtonVisible = audioRecordingButtonVisible,
                    audioRecordingButtonPreferred = audioRecordingButtonPreferred,
                    audioRecordingButtonIconDrawable = audioRecordingButtonIconDrawable,
                    audioRecordingButtonIconTintList = audioRecordingButtonIconTintList,
                    audioRecordingButtonWidth = audioRecordingButtonWidth,
                    audioRecordingButtonHeight = audioRecordingButtonHeight,
                    audioRecordingButtonPadding = audioRecordingButtonPadding,
                    cooldownTimerTextStyle = cooldownTimerTextStyle,
                    cooldownTimerBackgroundDrawable = cooldownTimerBackgroundDrawable,
                    messageReplyShowUserAvatar = messageReplyShowUserAvatar,
                    messageReplyBackgroundColor = messageReplyBackgroundColor,
                    messageReplyTextStyleMine = messageReplyTextStyleMine,
                    messageReplyMessageBackgroundStrokeColorMine = messageReplyMessageBackgroundStrokeColorMine,
                    messageReplyMessageBackgroundStrokeWidthMine = messageReplyMessageBackgroundStrokeWidthMine,
                    messageReplyTextStyleTheirs = messageReplyTextStyleTheirs,
                    messageReplyMessageBackgroundStrokeColorTheirs = messageReplyMessageBackgroundStrokeColorTheirs,
                    messageReplyMessageBackgroundStrokeWidthTheirs = messageReplyMessageBackgroundStrokeWidthTheirs,
                    attachmentsPickerDialogStyle = createAttachmentPickerDialogStyle(context, a),
                    audioRecordPlayerViewStyle = playerViewStyle,
                ).let(TransformStyle.messageComposerStyleTransformer::transform)
            }
        }

        @Suppress("MaxLineLength", "LongMethod", "ComplexMethod")
        private fun createAttachmentPickerDialogStyle(
            context: Context,
            a: TypedArray,
        ): AttachmentsPickerDialogStyle {
            val saveAttachmentsOnDismiss = a.getBoolean(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerSaveAttachmentsOnDismiss,
                false,
            )

            val attachmentsPickerBackgroundColor = a.getColor(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerBackgroundColor,
                context.getColorCompat(R.color.stream_ui_white_smoke),
            )

            val allowAccessButtonTextStyle = TextStyle.Builder(a)
                .size(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerAllowAccessButtonTextSize,
                    context.getDimension(R.dimen.stream_ui_text_large),
                )
                .color(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerAllowAccessButtonTextColor,
                    context.getColorCompat(R.color.stream_ui_accent_blue),
                )
                .font(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerAllowAccessButtonTextFontAssets,
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerAllowAccessButtonTextFont,
                )
                .style(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerAllowAccessButtonTextStyle,
                    Typeface.BOLD,
                )
                .build()

            val submitAttachmentsButtonIconDrawable = a.getDrawableCompat(
                context,
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerSubmitAttachmentsButtonIconDrawable,
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_next)!!

            val attachmentTabToggleButtonStateList = a.getColorStateList(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerAttachmentTabToggleButtonStateList,
            ) ?: context.getColorStateListCompat(R.color.stream_ui_attachment_tab_button)

            /**
             * Media attachments tab
             */
            val mediaAttachmentsTabEnabled = a.getBoolean(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerMediaAttachmentsTabEnabled,
                true,
            )

            val mediaAttachmentsTabIconDrawable = a.getDrawable(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerMediaAttachmentsTabIconDrawable,
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_attachment_permission_media)!!

            val allowAccessToMediaButtonText = a.getText(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerAllowAccessToMediaButtonText,
            )?.toString() ?: context.getString(R.string.stream_ui_message_composer_gallery_access)

            val allowAccessToMediaIconDrawable = a.getDrawable(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerAllowAccessToMediaIconDrawable,
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_attachment_permission_media)!!

            val videoLengthTextVisible = a.getBoolean(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerVideoLengthTextVisible,
                true,
            )

            val videoLengthTextStyle = TextStyle.Builder(a)
                .size(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerVideoLengthTextSize,
                    context.getDimension(R.dimen.stream_ui_text_small),
                )
                .color(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerVideoLengthTextColor,
                    context.getColorCompat(R.color.stream_ui_black),
                )
                .font(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerVideoLengthTextFontAssets,
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerVideoLengthTextFont,
                )
                .style(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerVideoLengthTextStyle,
                    Typeface.NORMAL,
                )
                .build()

            val videoIconVisible = a.getBoolean(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerVideoIconVisible,
                true,
            )

            val videoIconDrawable = a.getDrawable(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerVideoIconDrawable,
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_video)!!

            val videoIconDrawableTint =
                a.getColorOrNull(R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerVideoIconDrawableTint)

            val mediaAttachmentNoMediaText = a.getString(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerMediaAttachmentNoMediaText,
            ) ?: context.getString(R.string.stream_ui_message_composer_no_files)

            val mediaAttachmentNoMediaTextStyle = TextStyle.Builder(a)
                .size(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerMediaAttachmentNoMediaTextSize,
                    context.getDimension(R.dimen.stream_ui_text_large),
                )
                .color(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerMediaAttachmentNoMediaTextColor,
                    context.getColorCompat(R.color.stream_ui_text_color_primary),
                )
                .font(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerMediaAttachmentNoMediaTextFontAssets,
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerMediaAttachmentNoMediaTextFont,
                )
                .style(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerMediaAttachmentNoMediaTextStyle,
                    Typeface.NORMAL,
                )
                .build()

            /**
             * File attachments tab
             */
            val fileAttachmentsTabEnabled = a.getBoolean(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerFileAttachmentsTabEnabled,
                true,
            )

            val fileAttachmentsTabIconDrawable = a.getDrawable(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerFileAttachmentsTabIconDrawable,
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_attachment_permission_file)!!

            val allowAccessToFilesButtonText = a.getText(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerAllowAccessToFilesButtonText,
            )?.toString() ?: context.getString(R.string.stream_ui_message_composer_files_access)

            val allowAccessToFilesIconDrawable = a.getDrawable(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerAllowAccessToFilesIconDrawable,
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_attachment_permission_file)!!

            val allowAccessToAudioText = a.getText(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerAllowAccessToAudioText,
            )?.toString() ?: context.getString(R.string.stream_ui_message_composer_files_allow_audio_access)

            val allowAccessToAudioTextStyle = TextStyle.Builder(a)
                .size(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerAllowAccessToAudioTextSize,
                    context.getDimension(R.dimen.stream_ui_text_large),
                )
                .color(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerAllowAccessToAudioTextColor,
                    context.getColorCompat(R.color.stream_ui_black),
                )
                .font(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerAllowAccessToAudioTextFontAssets,
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerAllowAccessToAudioTextFont,
                )
                .style(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerAllowAccessToAudioTextStyle,
                    Typeface.BOLD,
                )
                .build()

            val allowAccessToAudioIconDrawable = a.getDrawable(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerAllowAccessToAudioIconDrawable,
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_file_manager)!!

            val allowAccessToVisualMediaText = a.getText(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerAllowAccessToVisualMediaText,
            )?.toString() ?: context.getString(R.string.stream_ui_message_composer_files_allow_visual_media_access)

            val allowAccessToMoreVisualMediaText = a.getText(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerAllowAccessToMoreVisualMediaText,
            )?.toString() ?: context.getString(R.string.stream_ui_message_composer_files_allow_more_visual_media)

            val allowAccessToVisualMediaTextStyle = TextStyle.Builder(a)
                .size(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerAllowAccessToVisualMediaTextSize,
                    context.getDimension(R.dimen.stream_ui_text_large),
                )
                .color(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerAllowAccessToVisualMediaTextColor,
                    context.getColorCompat(R.color.stream_ui_black),
                )
                .font(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerAllowAccessToVisualMediaTextFontAssets,
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerAllowAccessToVisualMediaTextFont,
                )
                .style(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerAllowAccessToVisualMediaTextStyle,
                    Typeface.BOLD,
                )
                .build()

            val allowAccessToVisualMediaIconDrawable = a.getDrawable(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerAllowAccessToVisualMediaIconDrawable,
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_file_manager)!!

            val recentFilesText = a.getText(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerRecentFilesText,
            )?.toString() ?: context.getString(R.string.stream_ui_message_composer_recent_files)

            val recentFilesTextStyle = TextStyle.Builder(a)
                .size(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerRecentFilesTextSize,
                    context.getDimension(R.dimen.stream_ui_spacing_medium),
                )
                .color(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerRecentFilesTextColor,
                    context.getColorCompat(R.color.stream_ui_black),
                )
                .font(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerRecentFilesTextFontAssets,
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerRecentFilesTextFont,
                )
                .style(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerRecentFilesTextStyle,
                    Typeface.BOLD,
                )
                .build()

            val fileManagerIconDrawable = a.getDrawable(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerFileManagerIconDrawable,
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_file_manager)!!

            val fileAttachmentsNoFilesText = a.getString(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerFileAttachmentsNoFilesText,
            ) ?: context.getString(R.string.stream_ui_message_composer_no_files)

            val fileAttachmentsNoFilesTextStyle = TextStyle.Builder(a)
                .size(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerFileAttachmentsNoFilesTextSize,
                    context.getDimension(R.dimen.stream_ui_text_large),
                )
                .color(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerFileAttachmentsNoFilesTextColor,
                    context.getColorCompat(R.color.stream_ui_text_color_primary),
                )
                .font(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerFileAttachmentsNoFilesTextFontAssets,
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerFileAttachmentsNoFilesTextFont,
                )
                .style(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerFileAttachmentsNoFilesTextStyle,
                    Typeface.NORMAL,
                )
                .build()

            val fileAttachmentItemNameTextStyle = TextStyle.Builder(a)
                .size(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerFileAttachmentItemNameTextSize,
                    context.getDimension(R.dimen.stream_ui_text_medium),
                )
                .color(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerFileAttachmentItemNameTextColor,
                    context.getColorCompat(R.color.stream_ui_black),
                )
                .font(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerFileAttachmentItemNameTextFontAssets,
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerFileAttachmentItemNameTextFont,
                )
                .style(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerFileAttachmentItemNameTextStyle,
                    Typeface.BOLD,
                )
                .build()

            val fileAttachmentItemSizeTextStyle = TextStyle.Builder(a)
                .size(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerFileAttachmentItemSizeTextSize,
                    context.getDimension(R.dimen.stream_ui_text_small),
                )
                .color(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerFileAttachmentItemSizeTextColor,
                    context.getColorCompat(R.color.stream_ui_text_color_secondary),
                )
                .font(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerFileAttachmentItemSizeTextFontAssets,
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerFileAttachmentItemSizeTextFont,
                )
                .style(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerFileAttachmentItemSizeTextStyle,
                    Typeface.BOLD,
                )
                .build()

            val fileAttachmentItemCheckboxSelectedDrawable = a.getDrawable(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerFileAttachmentItemCheckboxSelectedDrawable,
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_circle_blue)!!

            val fileAttachmentItemCheckboxDeselectedDrawable = a.getDrawable(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerFileAttachmentItemCheckboxDeselectedDrawable,
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_file_manager)!!

            val fileAttachmentItemCheckboxTextColor = a.getColor(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerFileAttachmentItemCheckboxTextColor,
                context.getColorCompat(R.color.stream_ui_literal_white),
            )

            /**
             * Camera attachments tab
             */
            val cameraAttachmentsTabEnabled = a.getBoolean(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerCameraAttachmentsTabEnabled,
                true,
            )

            val cameraAttachmentsTabIconDrawable = a.getDrawable(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerCameraAttachmentsTabIconDrawable,
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_attachment_permission_camera)!!

            val pollAttachmentsTabEnabled = a.getBoolean(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerPollAttachmentsTabEnabled,
                true,
            )

            val pollAttachmentsTabIconDrawable = a.getDrawable(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerPollAttachmentsTabIconDrawable,
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_attachment_poll)!!

            val allowAccessToCameraButtonText = a.getText(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerAllowAccessToCameraButtonText,
            )?.toString() ?: context.getString(R.string.stream_ui_message_composer_camera_access)

            val allowAccessToCameraIconDrawable = a.getDrawable(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerAllowAccessToCameraIconDrawable,
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_attachment_permission_camera)!!

            val pickerMediaMode = a.getEnum(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerMediaMode,
                PickerMediaMode.PHOTO_AND_VIDEO,
            )

            val useDefaultSystemMediaPicker = a.getBoolean(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerSystemPickerEnabled,
                false,
            )
            val systemMediaPickerVisualMediaAllowMultiple = a.getBoolean(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerSystemPickerVisualMediaAllowMultiple,
                false,
            )
            val systemMediaPickerVisualMediaType = a.getEnum(
                R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsPickerSystemPickerVisualMediaType,
                VisualMediaType.IMAGE_AND_VIDEO,
            )

            return AttachmentsPickerDialogStyle(
                useDefaultSystemMediaPicker = useDefaultSystemMediaPicker,
                saveAttachmentsOnDismiss = saveAttachmentsOnDismiss,
                attachmentsPickerBackgroundColor = attachmentsPickerBackgroundColor,
                allowAccessButtonTextStyle = allowAccessButtonTextStyle,
                submitAttachmentsButtonIconDrawable = submitAttachmentsButtonIconDrawable,
                attachmentTabToggleButtonStateList = attachmentTabToggleButtonStateList,
                // Media attachments tab
                mediaAttachmentsTabEnabled = mediaAttachmentsTabEnabled,
                mediaAttachmentsTabIconDrawable = mediaAttachmentsTabIconDrawable,
                allowAccessToMediaButtonText = allowAccessToMediaButtonText,
                allowAccessToMediaIconDrawable = allowAccessToMediaIconDrawable,
                videoLengthTextVisible = videoLengthTextVisible,
                videoLengthTextStyle = videoLengthTextStyle,
                videoIconVisible = videoIconVisible,
                videoIconDrawable = videoIconDrawable,
                videoIconDrawableTint = videoIconDrawableTint,
                mediaAttachmentNoMediaText = mediaAttachmentNoMediaText,
                mediaAttachmentNoMediaTextStyle = mediaAttachmentNoMediaTextStyle,
                // File attachments tab
                fileAttachmentsTabEnabled = fileAttachmentsTabEnabled,
                fileAttachmentsTabIconDrawable = fileAttachmentsTabIconDrawable,
                allowAccessToFilesButtonText = allowAccessToFilesButtonText,
                allowAccessToFilesIconDrawable = allowAccessToFilesIconDrawable,
                allowAccessToAudioText = allowAccessToAudioText,
                allowAccessToAudioTextStyle = allowAccessToAudioTextStyle,
                allowAccessToAudioIconDrawable = allowAccessToAudioIconDrawable,
                allowAccessToVisualMediaText = allowAccessToVisualMediaText,
                allowAccessToMoreVisualMediaText = allowAccessToMoreVisualMediaText,
                allowAccessToVisualMediaTextStyle = allowAccessToVisualMediaTextStyle,
                allowAccessToVisualMediaIconDrawable = allowAccessToVisualMediaIconDrawable,
                recentFilesText = recentFilesText,
                recentFilesTextStyle = recentFilesTextStyle,
                fileManagerIconDrawable = fileManagerIconDrawable,
                fileAttachmentsNoFilesText = fileAttachmentsNoFilesText,
                fileAttachmentsNoFilesTextStyle = fileAttachmentsNoFilesTextStyle,
                fileAttachmentItemNameTextStyle = fileAttachmentItemNameTextStyle,
                fileAttachmentItemSizeTextStyle = fileAttachmentItemSizeTextStyle,
                fileAttachmentItemCheckboxSelectedDrawable = fileAttachmentItemCheckboxSelectedDrawable,
                fileAttachmentItemCheckboxDeselectedDrawable = fileAttachmentItemCheckboxDeselectedDrawable,
                fileAttachmentItemCheckboxTextColor = fileAttachmentItemCheckboxTextColor,
                // Camera attachments tab
                cameraAttachmentsTabEnabled = cameraAttachmentsTabEnabled,
                cameraAttachmentsTabIconDrawable = cameraAttachmentsTabIconDrawable,
                allowAccessToCameraButtonText = allowAccessToCameraButtonText,
                allowAccessToCameraIconDrawable = allowAccessToCameraIconDrawable,
                pollAttachmentsTabEnabled = pollAttachmentsTabEnabled,
                pollAttachmentsTabIconDrawable = pollAttachmentsTabIconDrawable,
                pickerMediaMode = pickerMediaMode,
                // System media picker
                systemMediaPickerVisualMediaAllowMultiple = systemMediaPickerVisualMediaAllowMultiple,
                systemMediaPickerVisualMediaType = systemMediaPickerVisualMediaType,
            ).let(TransformStyle.attachmentsPickerStyleTransformer::transform)
        }

        private const val DEFAULT_MESSAGE_REPLY_BACKGROUND_STROKE_WIDTH = 4F
        private const val DEFAULT_TRAILING_BUTTON_SIZE = 32
        private const val DEFAULT_TRAILING_BUTTON_PADDING = 4
    }
}
