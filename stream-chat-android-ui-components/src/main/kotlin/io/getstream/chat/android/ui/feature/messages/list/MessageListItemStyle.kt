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

package io.getstream.chat.android.ui.feature.messages.list

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.Gravity
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.annotation.Px
import androidx.annotation.StyleableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.feature.messages.list.MessageListItemStyle.Companion.MESSAGE_STROKE_COLOR_MINE
import io.getstream.chat.android.ui.feature.messages.list.MessageListItemStyle.Companion.MESSAGE_STROKE_COLOR_THEIRS
import io.getstream.chat.android.ui.feature.messages.list.MessageListItemStyle.Companion.MESSAGE_STROKE_WIDTH_MINE
import io.getstream.chat.android.ui.feature.messages.list.MessageListItemStyle.Companion.MESSAGE_STROKE_WIDTH_THEIRS
import io.getstream.chat.android.ui.feature.messages.list.reactions.edit.EditReactionsViewStyle
import io.getstream.chat.android.ui.feature.messages.list.reactions.edit.internal.EditReactionsView
import io.getstream.chat.android.ui.feature.messages.list.reactions.view.ViewReactionsViewStyle
import io.getstream.chat.android.ui.feature.messages.list.reactions.view.internal.ViewReactionsView
import io.getstream.chat.android.ui.font.TextStyle
import io.getstream.chat.android.ui.helper.TransformStyle
import io.getstream.chat.android.ui.helper.ViewStyle
import io.getstream.chat.android.ui.utils.extensions.dpToPxPrecise
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.getDrawableCompat

/**
 * Style for view holders used inside [MessageListView].
 * Use this class together with [TransformStyle.messageListItemStyleTransformer] to change styles programmatically.
 *
 * @property messageBackgroundColorMine Background color for message sent by the current user. Default value is [R.color.stream_ui_grey_gainsboro].
 * @property messageBackgroundColorTheirs Background color for message sent by other user. Default value is [R.color.stream_ui_white].
 * @property messageLinkTextColorMine Color for links sent by the current user. Default value is [R.color.stream_ui_accent_blue].
 * @property messageLinkTextColorTheirs Color for links sent by other user. Default value is [R.color.stream_ui_accent_blue].
 * @property messageLinkBackgroundColorMine Background color for message with link, sent by the current user. Default value is [R.color.stream_ui_blue_alice].
 * @property messageLinkBackgroundColorTheirs Background color for message with link, sent by other user. Default value is [R.color.stream_ui_blue_alice].
 * @property linkDescriptionMaxLines Max lines for link's description. Default value is 5.
 * @property textStyleMine Appearance for message text sent by the current user.
 * @property textStyleTheirs Appearance for message text sent by other user.
 * @property textStyleUserName Appearance for user name text.
 * @property textStyleMessageDate Appearance for message date text.
 * @property textStyleMessageLanguage Appearance for message language text.
 * @property textStyleThreadCounter Appearance for thread counter text.
 * @property textStyleReadCounter Appearance for read counter text.
 * @property textStyleLinkTitle Appearance for link.
 * @property textStyleLinkDescription Appearance for link's description text.
 * @property dateSeparatorBackgroundColor Background color for data separator. Default value is [R.color.stream_ui_overlay_dark].
 * @property textStyleDateSeparator Appearance for date separator text.
 * @property reactionsViewStyle Style for [ViewReactionsView].
 * @property editReactionsViewStyle Style for [EditReactionsView].
 * @property iconIndicatorSent Icon for message's sent status. Default value is [R.drawable.stream_ui_ic_check_single].
 * @property iconIndicatorRead Icon for message's read status. Default value is [R.drawable.stream_ui_ic_check_double].
 * @property iconIndicatorPendingSync Icon for message's pending status. Default value is [R.drawable.stream_ui_ic_clock].
 * @property iconOnlyVisibleToYou Icon for message's pending status. Default value is [R.drawable.stream_ui_ic_icon_eye_off].
 * @property textStyleMessageDeleted Appearance for message deleted text.
 * @property messageDeletedBackground Background color for deleted message. Default value is [R.color.stream_ui_grey_whisper].
 * @property textStyleMessageDeletedMine Appearance for mine message deleted text. Default value is [textStyleMessageDeleted].
 * @property messageDeletedBackgroundMine Background color for mine deleted message. Default value is [messageDeletedBackground].
 * @property textStyleMessageDeletedTheirs Appearance for theirs message deleted text. Default value is [textStyleMessageDeleted].
 * @property messageDeletedBackgroundTheirs Background color for theirs deleted message. Default value is [messageDeletedBackground].
 * @property messageStrokeColorMine Stroke color for message sent by the current user. Default value is [MESSAGE_STROKE_COLOR_MINE].
 * @property messageStrokeWidthMine Stroke width for message sent by the current user. Default value is [MESSAGE_STROKE_WIDTH_MINE].
 * @property messageStrokeColorTheirs Stroke color for message sent by other user. Default value is [MESSAGE_STROKE_COLOR_THEIRS].
 * @property messageStrokeWidthTheirs Stroke width for message sent by other user. Default value is [MESSAGE_STROKE_WIDTH_THEIRS].
 * @property textStyleSystemMessage Appearance for system message text.
 * @property textStyleErrorMessage Appearance for error message text.
 * @property messageStartMargin Margin for messages in the left side. Default value is 48dp.
 * @property messageEndMargin Margin for messages in the right side. Default value is 0dp.
 * @property messageMaxWidthFactorMine Factor used to compute max width for message sent by the current user. Should be in <0.75, 1> range.
 * @property messageMaxWidthFactorTheirs Factor used to compute max width for message sent by other user. Should be in <0.75, 1> range.
 * @property showMessageDeliveryStatusIndicator Flag if we need to show the delivery indicator or not.
 * @property iconFailedMessage Icon for message failed status. Default value is [R.drawable.stream_ui_ic_warning].
 * @property iconBannedMessage Icon for message when the current user is banned. Default value is [R.drawable.stream_ui_ic_warning].
 * @property systemMessageAlignment Changes the alignment of system messages.
 * @property loadingMoreView Loading more view. Default value is [R.layout.stream_ui_message_list_loading_more_view].
 */
public data class MessageListItemStyle(
    @ColorInt public val messageBackgroundColorMine: Int?,
    @ColorInt public val messageBackgroundColorTheirs: Int?,
    @ColorInt public val messageLinkTextColorMine: Int?,
    @ColorInt public val messageLinkTextColorTheirs: Int?,
    @ColorInt public val messageLinkBackgroundColorMine: Int,
    @ColorInt public val messageLinkBackgroundColorTheirs: Int,
    public val linkDescriptionMaxLines: Int,
    public val textStyleMine: TextStyle,
    public val textStyleTheirs: TextStyle,
    public val textStyleUserName: TextStyle,
    public val textStyleMessageDate: TextStyle,
    public val textStyleMessageLanguage: TextStyle,
    public val textStyleThreadCounter: TextStyle,
    public val textStyleReadCounter: TextStyle,
    public val threadSeparatorTextStyle: TextStyle,
    public val textStyleLinkLabel: TextStyle,
    public val textStyleLinkTitle: TextStyle,
    public val textStyleLinkDescription: TextStyle,
    @ColorInt public val dateSeparatorBackgroundColor: Int,
    public val textStyleDateSeparator: TextStyle,
    public val reactionsViewStyle: ViewReactionsViewStyle,
    public val editReactionsViewStyle: EditReactionsViewStyle,
    public val iconIndicatorSent: Drawable,
    public val iconIndicatorRead: Drawable,
    public val iconIndicatorPendingSync: Drawable,
    public val iconOnlyVisibleToYou: Drawable,
    @Deprecated(
        message = "Use textStyleMessageDeletedMine and textStyleMessageDeletedTheirs instead.",
        replaceWith = ReplaceWith("textStyleMessageDeletedMine and textStyleMessageDeletedTheirs"),
        level = DeprecationLevel.WARNING,
    )
    public val textStyleMessageDeleted: TextStyle,
    @Deprecated(
        message = "Use messageDeletedBackgroundMine and messageDeletedBackgroundTheirs instead.",
        replaceWith = ReplaceWith("messageDeletedBackgroundMine and messageDeletedBackgroundTheirs"),
        level = DeprecationLevel.WARNING,
    )
    @ColorInt public val messageDeletedBackground: Int,
    public val textStyleMessageDeletedMine: TextStyle?,
    @ColorInt public val messageDeletedBackgroundMine: Int?,
    public val textStyleMessageDeletedTheirs: TextStyle?,
    @ColorInt public val messageDeletedBackgroundTheirs: Int?,
    @ColorInt public val messageStrokeColorMine: Int,
    @Px public val messageStrokeWidthMine: Float,
    @ColorInt public val messageStrokeColorTheirs: Int,
    @Px public val messageStrokeWidthTheirs: Float,
    public val textStyleSystemMessage: TextStyle,
    public val textStyleErrorMessage: TextStyle,
    public val pinnedMessageIndicatorTextStyle: TextStyle,
    public val pinnedMessageIndicatorIcon: Drawable,
    @ColorInt public val pinnedMessageBackgroundColor: Int,
    @Px public val messageStartMargin: Int,
    @Px public val messageEndMargin: Int,
    public val messageMaxWidthFactorMine: Float,
    public val messageMaxWidthFactorTheirs: Float,
    public val showMessageDeliveryStatusIndicator: Boolean,
    public val iconFailedMessage: Drawable,
    public val iconBannedMessage: Drawable,
    public val systemMessageAlignment: Int,
    @LayoutRes public val loadingMoreView: Int,
    @ColorInt public val unreadSeparatorBackgroundColor: Int,
    public val unreadSeparatorTextStyle: TextStyle,
) : ViewStyle {

    @ColorInt
    public fun getStyleTextColor(isMine: Boolean): Int? {
        return if (isMine) textStyleMine.colorOrNull() else textStyleTheirs.colorOrNull()
    }

    @ColorInt
    public fun getStyleLinkTextColor(isMine: Boolean): Int? {
        return if (isMine) messageLinkTextColorMine else messageLinkTextColorTheirs
    }

    internal companion object {
        internal const val VALUE_NOT_SET = Integer.MAX_VALUE

        internal val DEFAULT_LINK_BACKGROUND_COLOR = R.color.stream_ui_blue_alice

        internal val DEFAULT_TEXT_COLOR = R.color.stream_ui_text_color_primary
        internal val DEFAULT_TEXT_SIZE = R.dimen.stream_ui_text_medium
        internal const val DEFAULT_TEXT_STYLE = Typeface.NORMAL

        internal val DEFAULT_TEXT_COLOR_USER_NAME = R.color.stream_ui_text_color_secondary
        internal val DEFAULT_TEXT_SIZE_USER_NAME = R.dimen.stream_ui_text_small

        internal val DEFAULT_TEXT_COLOR_DATE = R.color.stream_ui_text_color_secondary
        internal val DEFAULT_TEXT_SIZE_DATE = R.dimen.stream_ui_text_small

        internal val DEFAULT_TEXT_COLOR_THREAD_COUNTER = R.color.stream_ui_accent_blue
        internal val DEFAULT_TEXT_SIZE_THREAD_COUNTER = R.dimen.stream_ui_text_small

        internal val DEFAULT_TEXT_COLOR_READ_COUNTER = R.color.stream_ui_text_color_secondary
        internal val DEFAULT_TEXT_SIZE_READ_COUNTER = R.dimen.stream_ui_text_small

        internal val DEFAULT_TEXT_COLOR_LINK_DESCRIPTION = R.color.stream_ui_text_color_secondary
        internal val DEFAULT_TEXT_SIZE_LINK_DESCRIPTION = R.dimen.stream_ui_text_small

        internal val DEFAULT_TEXT_COLOR_DATE_SEPARATOR = R.color.stream_ui_white
        internal val DEFAULT_TEXT_SIZE_DATE_SEPARATOR = R.dimen.stream_ui_text_small

        internal val MESSAGE_STROKE_COLOR_MINE = R.color.stream_ui_literal_transparent
        internal const val MESSAGE_STROKE_WIDTH_MINE: Float = 0f
        internal val MESSAGE_STROKE_COLOR_THEIRS = R.color.stream_ui_grey_whisper
        internal val MESSAGE_STROKE_WIDTH_THEIRS: Float by lazy { 1.dpToPxPrecise() }

        private const val BASE_MESSAGE_MAX_WIDTH_FACTOR = 1
        private const val DEFAULT_MESSAGE_MAX_WIDTH_FACTOR = 0.75f
    }

    internal class Builder(private val attributes: TypedArray, private val context: Context) {
        @ColorInt
        private var messageBackgroundColorMine: Int = VALUE_NOT_SET

        @ColorInt
        private var messageBackgroundColorTheirs: Int = VALUE_NOT_SET

        @ColorInt
        private var messageLinkTextColorMine: Int = VALUE_NOT_SET

        @ColorInt
        private var messageLinkTextColorTheirs: Int = VALUE_NOT_SET

        private var reactionsEnabled: Boolean = true

        private var linkDescriptionMaxLines: Int = 5

        private var systemMessageGravity: Int = Gravity.CENTER

        fun messageBackgroundColorMine(
            @StyleableRes messageBackgroundColorMineStyleableId: Int,
            @ColorInt defaultValue: Int = VALUE_NOT_SET,
        ) = apply {
            messageBackgroundColorMine = attributes.getColor(messageBackgroundColorMineStyleableId, defaultValue)
        }

        fun messageBackgroundColorTheirs(
            @StyleableRes messageBackgroundColorTheirsId: Int,
            @ColorInt defaultValue: Int = VALUE_NOT_SET,
        ) = apply {
            messageBackgroundColorTheirs = attributes.getColor(messageBackgroundColorTheirsId, defaultValue)
        }

        fun messageLinkTextColorMine(
            @StyleableRes messageLinkTextColorMineId: Int,
            @ColorInt defaultValue: Int = VALUE_NOT_SET,
        ) = apply {
            messageLinkTextColorMine = attributes.getColor(messageLinkTextColorMineId, defaultValue)
        }

        fun messageLinkTextColorTheirs(
            @StyleableRes messageLinkTextColorTheirsId: Int,
            @ColorInt defaultValue: Int = VALUE_NOT_SET,
        ) = apply {
            messageLinkTextColorTheirs = attributes.getColor(messageLinkTextColorTheirsId, defaultValue)
        }

        fun reactionsEnabled(
            @StyleableRes reactionsEnabled: Int,
            defaultValue: Boolean = true,
        ) = apply {
            this.reactionsEnabled = attributes.getBoolean(reactionsEnabled, defaultValue)
        }

        fun linkDescriptionMaxLines(
            maxLines: Int,
            defaultValue: Int = 5,
        ) = apply {
            this.linkDescriptionMaxLines = attributes.getInt(maxLines, defaultValue)
        }

        fun systemMessageGravity(
            @StyleableRes systemMessageGravity: Int,
            defaultGravity: Int = Gravity.CENTER,
        ) = apply {
            this.systemMessageGravity = attributes.getInt(systemMessageGravity, defaultGravity)
        }

        fun build(): MessageListItemStyle {
            val linkBackgroundColorMine =
                attributes.getColor(
                    R.styleable.MessageListView_streamUiMessageLinkBackgroundColorMine,
                    context.getColorCompat(DEFAULT_LINK_BACKGROUND_COLOR),
                )
            val linkBackgroundColorTheirs =
                attributes.getColor(
                    R.styleable.MessageListView_streamUiMessageLinkBackgroundColorTheirs,
                    context.getColorCompat(DEFAULT_LINK_BACKGROUND_COLOR),
                )

            val mediumTypeface = ResourcesCompat.getFont(context, R.font.stream_roboto_medium) ?: Typeface.DEFAULT
            val boldTypeface = ResourcesCompat.getFont(context, R.font.stream_roboto_bold) ?: Typeface.DEFAULT_BOLD

            val textStyleMine = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiMessageTextSizeMine,
                    context.getDimension(DEFAULT_TEXT_SIZE),
                )
                .color(
                    R.styleable.MessageListView_streamUiMessageTextColorMine,
                    context.getColorCompat(DEFAULT_TEXT_COLOR),
                )
                .font(
                    R.styleable.MessageListView_streamUiMessageTextFontAssetsMine,
                    R.styleable.MessageListView_streamUiMessageTextFontMine,
                    mediumTypeface,
                )
                .style(R.styleable.MessageListView_streamUiMessageTextStyleMine, DEFAULT_TEXT_STYLE)
                .build()

            val textStyleTheirs = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiMessageTextSizeTheirs,
                    context.getDimension(DEFAULT_TEXT_SIZE),
                )
                .color(
                    R.styleable.MessageListView_streamUiMessageTextColorTheirs,
                    context.getColorCompat(DEFAULT_TEXT_COLOR),
                )
                .font(
                    R.styleable.MessageListView_streamUiMessageTextFontAssetsTheirs,
                    R.styleable.MessageListView_streamUiMessageTextFontTheirs,
                    mediumTypeface,
                )
                .style(R.styleable.MessageListView_streamUiMessageTextStyleTheirs, DEFAULT_TEXT_STYLE)
                .build()

            val textStyleUserName = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiMessageTextSizeUserName,
                    context.getDimension(DEFAULT_TEXT_SIZE_USER_NAME),
                )
                .color(
                    R.styleable.MessageListView_streamUiMessageTextColorUserName,
                    context.getColorCompat(DEFAULT_TEXT_COLOR_USER_NAME),
                )
                .font(
                    R.styleable.MessageListView_streamUiMessageTextFontAssetsUserName,
                    R.styleable.MessageListView_streamUiMessageTextFontUserName,
                )
                .style(R.styleable.MessageListView_streamUiMessageTextStyleUserName, DEFAULT_TEXT_STYLE)
                .build()

            val textStyleMessageDate = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiMessageTextSizeDate,
                    context.getDimension(DEFAULT_TEXT_SIZE_DATE),
                )
                .color(
                    R.styleable.MessageListView_streamUiMessageTextColorDate,
                    context.getColorCompat(DEFAULT_TEXT_COLOR_DATE),
                )
                .font(
                    R.styleable.MessageListView_streamUiMessageTextFontAssetsDate,
                    R.styleable.MessageListView_streamUiMessageTextFontDate,
                )
                .style(R.styleable.MessageListView_streamUiMessageTextStyleDate, DEFAULT_TEXT_STYLE)
                .build()

            val textStyleMessageLanguage = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiMessageTextSizeLanguage,
                    context.getDimension(DEFAULT_TEXT_SIZE_DATE),
                )
                .color(
                    R.styleable.MessageListView_streamUiMessageTextColorLanguage,
                    context.getColorCompat(DEFAULT_TEXT_COLOR_DATE),
                )
                .font(
                    R.styleable.MessageListView_streamUiMessageTextFontAssetsLanguage,
                    R.styleable.MessageListView_streamUiMessageTextFontLanguage,
                )
                .style(R.styleable.MessageListView_streamUiMessageTextStyleLanguage, DEFAULT_TEXT_STYLE)
                .build()

            val textStyleThreadCounter = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiMessageTextSizeThreadCounter,
                    context.getDimension(DEFAULT_TEXT_SIZE_THREAD_COUNTER),
                )
                .color(
                    R.styleable.MessageListView_streamUiMessageTextColorThreadCounter,
                    context.getColorCompat(DEFAULT_TEXT_COLOR_THREAD_COUNTER),
                )
                .font(
                    R.styleable.MessageListView_streamUiMessageTextFontAssetsThreadCounter,
                    R.styleable.MessageListView_streamUiMessageTextFontThreadCounter,
                    mediumTypeface,
                )
                .style(R.styleable.MessageListView_streamUiMessageTextStyleThreadCounter, DEFAULT_TEXT_STYLE)
                .build()

            val textStyleReadCounter = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiMessageTextSizeReadCounter,
                    context.getDimension(DEFAULT_TEXT_SIZE_READ_COUNTER),
                )
                .color(
                    R.styleable.MessageListView_streamUiMessageTextColorReadCounter,
                    context.getColorCompat(DEFAULT_TEXT_COLOR_READ_COUNTER),
                )
                .font(
                    R.styleable.MessageListView_streamUiMessageTextFontAssetsReadCounter,
                    R.styleable.MessageListView_streamUiMessageTextFontReadCounter,
                    mediumTypeface,
                )
                .style(R.styleable.MessageListView_streamUiMessageTextStyleReadCounter, DEFAULT_TEXT_STYLE)
                .build()

            val textStyleThreadSeparator = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiMessageTextSizeThreadSeparator,
                    context.getDimension(DEFAULT_TEXT_SIZE_THREAD_COUNTER),
                )
                .color(
                    R.styleable.MessageListView_streamUiMessageTextColorThreadSeparator,
                    context.getColorCompat(DEFAULT_TEXT_COLOR_THREAD_COUNTER),
                )
                .font(
                    R.styleable.MessageListView_streamUiMessageTextFontAssetsThreadSeparator,
                    R.styleable.MessageListView_streamUiMessageTextFontThreadSeparator,
                    mediumTypeface,
                )
                .style(R.styleable.MessageListView_streamUiMessageTextStyleThreadSeparator, DEFAULT_TEXT_STYLE)
                .build()

            val textStyleLinkTitle = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiMessageTextSizeLinkTitle,
                    context.getDimension(DEFAULT_TEXT_SIZE),
                )
                .color(
                    R.styleable.MessageListView_streamUiMessageTextColorLinkTitle,
                    context.getColorCompat(DEFAULT_TEXT_COLOR),
                )
                .font(
                    R.styleable.MessageListView_streamUiMessageTextFontAssetsLinkTitle,
                    R.styleable.MessageListView_streamUiMessageTextFontLinkTitle,
                    boldTypeface,
                )
                .style(R.styleable.MessageListView_streamUiMessageTextStyleLinkTitle, DEFAULT_TEXT_STYLE)
                .build()

            val textStyleLinkDescription = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiMessageTextSizeLinkDescription,
                    context.getDimension(DEFAULT_TEXT_SIZE_LINK_DESCRIPTION),
                )
                .color(
                    R.styleable.MessageListView_streamUiMessageTextColorLinkDescription,
                    context.getColorCompat(DEFAULT_TEXT_COLOR_LINK_DESCRIPTION),
                )
                .font(
                    R.styleable.MessageListView_streamUiMessageTextFontAssetsLinkDescription,
                    R.styleable.MessageListView_streamUiMessageTextFontLinkDescription,
                )
                .style(R.styleable.MessageListView_streamUiMessageTextStyleLinkDescription, DEFAULT_TEXT_STYLE)
                .build()

            val textStyleLinkLabel = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiMessageTextSizeLinkLabel,
                    context.getDimension(DEFAULT_TEXT_SIZE_LINK_DESCRIPTION),
                )
                .color(
                    R.styleable.MessageListView_streamUiMessageTextColorLinkLabel,
                    context.getColorCompat(DEFAULT_TEXT_COLOR_LINK_DESCRIPTION),
                )
                .font(
                    R.styleable.MessageListView_streamUiMessageTextFontAssetsLinkLabel,
                    R.styleable.MessageListView_streamUiMessageTextFontLinkLabel,
                )
                .style(R.styleable.MessageListView_streamUiMessageTextStyleLinkLabel, DEFAULT_TEXT_STYLE)
                .build()

            val dateSeparatorBackgroundColor =
                attributes.getColor(
                    R.styleable.MessageListView_streamUiDateSeparatorBackgroundColor,
                    context.getColorCompat(R.color.stream_ui_overlay_dark),
                )

            val textStyleDateSeparator = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiMessageTextSizeDateSeparator,
                    context.getDimension(DEFAULT_TEXT_SIZE_DATE_SEPARATOR),
                )
                .color(
                    R.styleable.MessageListView_streamUiMessageTextColorDateSeparator,
                    context.getColorCompat(DEFAULT_TEXT_COLOR_DATE_SEPARATOR),
                )
                .font(
                    R.styleable.MessageListView_streamUiMessageTextFontAssetsDateSeparator,
                    R.styleable.MessageListView_streamUiMessageTextFontDateSeparator,
                )
                .style(R.styleable.MessageListView_streamUiMessageTextStyleDateSeparator, DEFAULT_TEXT_STYLE)
                .build()

            val reactionsViewStyle = ViewReactionsViewStyle.Companion.Builder(attributes, context)
                .bubbleBorderColorMine(R.styleable.MessageListView_streamUiMessageReactionsBubbleBorderColorMine)
                .bubbleBorderColorTheirs(R.styleable.MessageListView_streamUiMessageReactionsBubbleBorderColorTheirs)
                .bubbleBorderWidthMine(R.styleable.MessageListView_streamUiMessageReactionsBubbleBorderWidthMine)
                .bubbleBorderWidthTheirs(R.styleable.MessageListView_streamUiMessageReactionsBubbleBorderWidthTheirs)
                .bubbleColorMine(R.styleable.MessageListView_streamUiMessageReactionsBubbleColorMine)
                .bubbleColorTheirs(R.styleable.MessageListView_streamUiMessageReactionsBubbleColorTheirs)
                .messageOptionsReactionSorting(
                    R.styleable.MessageListView_streamUiMessageOptionsReactionSorting,
                )
                .build()

            val editReactionsViewStyle = EditReactionsViewStyle.Builder(attributes, context)
                .bubbleColorMine(R.styleable.MessageListView_streamUiEditReactionsBubbleColorMine)
                .bubbleColorTheirs(R.styleable.MessageListView_streamUiEditReactionsBubbleColorTheirs)
                .reactionsColumns(R.styleable.MessageListView_streamUiEditReactionsColumns)
                .build()

            val showMessageDeliveryStatusIndicator = attributes.getBoolean(
                R.styleable.MessageListView_streamUiShowMessageDeliveryStatusIndicator,
                true,
            )

            val iconIndicatorSent = attributes.getDrawable(
                R.styleable.MessageListView_streamUiIconIndicatorSent,
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_check_single)!!
            val iconIndicatorRead = attributes.getDrawable(
                R.styleable.MessageListView_streamUiIconIndicatorRead,
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_check_double)!!
            val iconIndicatorPendingSync = attributes.getDrawableCompat(
                context,
                R.styleable.MessageListView_streamUiIconIndicatorPendingSync,
            ) ?: AppCompatResources.getDrawable(context, R.drawable.stream_ui_ic_clock)!!

            val iconOnlyVisibleToYou = attributes.getDrawable(
                R.styleable.MessageListView_streamUiIconOnlyVisibleToYou,
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_icon_eye_off)!!

            val messageDeletedBackground =
                attributes.getColor(
                    R.styleable.MessageListView_streamUiDeletedMessageBackgroundColor,
                    context.getColorCompat(R.color.stream_ui_grey_whisper),
                )

            val textStyleMessageDeleted = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiMessageTextSizeMessageDeleted,
                    context.getDimension(DEFAULT_TEXT_SIZE),
                )
                .color(
                    R.styleable.MessageListView_streamUiMessageTextColorMessageDeleted,
                    context.getColorCompat(R.color.stream_ui_text_color_secondary),
                )
                .font(
                    R.styleable.MessageListView_streamUiMessageTextFontAssetsMessageDeleted,
                    R.styleable.MessageListView_streamUiMessageTextFontMessageDeleted,
                )
                .style(R.styleable.MessageListView_streamUiMessageTextStyleMessageDeleted, Typeface.ITALIC)
                .build()

            val messageDeletedBackgroundMine =
                attributes.getColor(
                    R.styleable.MessageListView_streamUiDeletedMessageBackgroundColorMine,
                    VALUE_NOT_SET,
                )

            val textStyleMessageDeletedMine = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiMessageTextSizeMessageDeletedMine,
                    textStyleMessageDeleted.size,
                )
                .color(
                    R.styleable.MessageListView_streamUiMessageTextColorMessageDeletedMine,
                    textStyleMessageDeleted.color,
                )
                .fontAssetsPath(
                    R.styleable.MessageListView_streamUiMessageTextFontAssetsMessageDeletedMine,
                    textStyleMessageDeleted.fontAssetsPath,
                )
                .fontResource(
                    R.styleable.MessageListView_streamUiMessageTextFontMessageDeletedMine,
                    textStyleMessageDeleted.fontResource,
                )
                .style(
                    R.styleable.MessageListView_streamUiMessageTextStyleMessageDeletedMine,
                    textStyleMessageDeleted.style,
                )
                .build()

            val messageDeletedBackgroundTheirs =
                attributes.getColor(
                    R.styleable.MessageListView_streamUiDeletedMessageBackgroundColorTheirs,
                    VALUE_NOT_SET,
                )

            val textStyleMessageDeletedTheirs = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiMessageTextSizeMessageDeletedTheirs,
                    textStyleMessageDeleted.size,
                )
                .color(
                    R.styleable.MessageListView_streamUiMessageTextColorMessageDeletedTheirs,
                    textStyleMessageDeleted.color,
                )
                .fontAssetsPath(
                    R.styleable.MessageListView_streamUiMessageTextFontAssetsMessageDeletedTheirs,
                    textStyleMessageDeleted.fontAssetsPath,
                )
                .fontResource(
                    R.styleable.MessageListView_streamUiMessageTextFontMessageDeletedTheirs,
                    textStyleMessageDeleted.fontResource,
                )
                .style(
                    R.styleable.MessageListView_streamUiMessageTextStyleMessageDeletedTheirs,
                    textStyleMessageDeleted.style,
                )
                .build()

            val messageStrokeColorMine = attributes.getColor(
                R.styleable.MessageListView_streamUiMessageStrokeColorMine,
                context.getColorCompat(MESSAGE_STROKE_COLOR_MINE),
            )
            val messageStrokeWidthMine =
                attributes.getDimension(
                    R.styleable.MessageListView_streamUiMessageStrokeWidthMine,
                    MESSAGE_STROKE_WIDTH_MINE,
                )
            val messageStrokeColorTheirs =
                attributes.getColor(
                    R.styleable.MessageListView_streamUiMessageStrokeColorTheirs,
                    context.getColorCompat(
                        MESSAGE_STROKE_COLOR_THEIRS,
                    ),
                )
            val messageStrokeWidthTheirs =
                attributes.getDimension(
                    R.styleable.MessageListView_streamUiMessageStrokeWidthTheirs,
                    MESSAGE_STROKE_WIDTH_THEIRS,
                )

            val textStyleSystemMessage = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiSystemMessageTextSize,
                    context.getDimension(R.dimen.stream_ui_text_small),
                )
                .color(
                    R.styleable.MessageListView_streamUiSystemMessageTextColor,
                    context.getColorCompat(R.color.stream_ui_text_color_secondary),
                )
                .font(
                    R.styleable.MessageListView_streamUiSystemMessageTextFontAssets,
                    R.styleable.MessageListView_streamUiSystemMessageTextFont,
                )
                .style(R.styleable.MessageListView_streamUiSystemMessageTextStyle, Typeface.BOLD)
                .build()

            val textStyleErrorMessage = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiErrorMessageTextSize,
                    context.getDimension(R.dimen.stream_ui_text_small),
                )
                .color(
                    R.styleable.MessageListView_streamUiErrorMessageTextColor,
                    context.getColorCompat(R.color.stream_ui_text_color_secondary),
                )
                .font(
                    R.styleable.MessageListView_streamUiErrorMessageTextFontAssets,
                    R.styleable.MessageListView_streamUiErrorMessageTextFont,
                )
                .style(R.styleable.MessageListView_streamUiErrorMessageTextStyle, Typeface.BOLD)
                .build()

            val pinnedMessageIndicatorTextStyle = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiPinnedMessageIndicatorTextSize,
                    context.getDimension(R.dimen.stream_ui_text_small),
                )
                .color(
                    R.styleable.MessageListView_streamUiPinnedMessageIndicatorTextColor,
                    context.getColorCompat(R.color.stream_ui_text_color_secondary),
                )
                .font(
                    R.styleable.MessageListView_streamUiPinnedMessageIndicatorTextFontAssets,
                    R.styleable.MessageListView_streamUiPinnedMessageIndicatorTextFont,
                )
                .style(R.styleable.MessageListView_streamUiPinnedMessageIndicatorTextStyle, Typeface.NORMAL)
                .build()

            val pinnedMessageIndicatorIcon = attributes.getDrawable(
                R.styleable.MessageListView_streamUiPinnedMessageIndicatorIcon,
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_pin)!!

            val pinnedMessageBackgroundColor = attributes.getColor(
                R.styleable.MessageListView_streamUiPinnedMessageBackgroundColor,
                context.getColorCompat(R.color.stream_ui_highlight),
            )

            val messageStartMargin = attributes.getDimension(
                R.styleable.MessageListView_streamUiMessageStartMargin,
                context.getDimension(R.dimen.stream_ui_message_viewholder_avatar_missing_margin).toFloat(),
            ).toInt()

            val messageEndMargin = attributes.getDimension(
                R.styleable.MessageListView_streamUiMessageEndMargin,
                context.getDimension(R.dimen.stream_ui_message_viewholder_avatar_missing_margin).toFloat(),
            ).toInt()

            val messageMaxWidthFactorMine = attributes.getFraction(
                R.styleable.MessageListView_streamUiMessageMaxWidthFactorMine,
                BASE_MESSAGE_MAX_WIDTH_FACTOR,
                BASE_MESSAGE_MAX_WIDTH_FACTOR,
                DEFAULT_MESSAGE_MAX_WIDTH_FACTOR,
            )

            val messageMaxWidthFactorTheirs = attributes.getFraction(
                R.styleable.MessageListView_streamUiMessageMaxWidthFactorTheirs,
                BASE_MESSAGE_MAX_WIDTH_FACTOR,
                BASE_MESSAGE_MAX_WIDTH_FACTOR,
                DEFAULT_MESSAGE_MAX_WIDTH_FACTOR,
            )

            val iconFailedMessage = attributes.getDrawable(R.styleable.MessageListView_streamUiIconFailedIndicator)
                ?: ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_warning)!!
            val iconBannedMessage = attributes.getDrawable(R.styleable.MessageListView_streamUiIconBannedIndicator)
                ?: ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_warning)!!

            val loadingMoreView = attributes.getResourceId(
                R.styleable.MessageListView_streamUiMessageListLoadingMoreView,
                R.layout.stream_ui_message_list_loading_more_view,
            )

            val unreadSeparatorBackgroundColor = attributes.getColor(
                R.styleable.MessageListView_streamUiUnreadSeparatorBackgroundColor,
                context.getColorCompat(R.color.stream_ui_unread_label_background_color),
            )

            val unreadSeparatorTextStyle = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiUnreadSeparatorTextSize,
                    context.getDimension(R.dimen.stream_ui_text_small),
                )
                .color(
                    R.styleable.MessageListView_streamUiUnreadSeparatorTextColor,
                    context.getColorCompat(R.color.stream_ui_unread_label_text_color),
                )
                .font(
                    R.styleable.MessageListView_streamUiUnreadSeparatorTextFontAssets,
                    R.styleable.MessageListView_streamUiUnreadSeparatorTextFont,
                )
                .style(R.styleable.MessageListView_streamUiUnreadSeparatorTextStyle, Typeface.BOLD)
                .build()

            return MessageListItemStyle(
                messageBackgroundColorMine = messageBackgroundColorMine.nullIfNotSet(),
                messageBackgroundColorTheirs = messageBackgroundColorTheirs.nullIfNotSet(),
                messageLinkTextColorMine = messageLinkTextColorMine.nullIfNotSet(),
                messageLinkTextColorTheirs = messageLinkTextColorTheirs.nullIfNotSet(),
                messageLinkBackgroundColorMine = linkBackgroundColorMine,
                messageLinkBackgroundColorTheirs = linkBackgroundColorTheirs,
                linkDescriptionMaxLines = linkDescriptionMaxLines,
                textStyleMine = textStyleMine,
                textStyleTheirs = textStyleTheirs,
                textStyleUserName = textStyleUserName,
                textStyleMessageDate = textStyleMessageDate,
                textStyleMessageLanguage = textStyleMessageLanguage,
                textStyleThreadCounter = textStyleThreadCounter,
                textStyleReadCounter = textStyleReadCounter,
                threadSeparatorTextStyle = textStyleThreadSeparator,
                textStyleLinkTitle = textStyleLinkTitle,
                textStyleLinkDescription = textStyleLinkDescription,
                textStyleLinkLabel = textStyleLinkLabel,
                dateSeparatorBackgroundColor = dateSeparatorBackgroundColor,
                textStyleDateSeparator = textStyleDateSeparator,
                reactionsViewStyle = reactionsViewStyle,
                editReactionsViewStyle = editReactionsViewStyle,
                iconIndicatorSent = iconIndicatorSent,
                iconIndicatorRead = iconIndicatorRead,
                iconIndicatorPendingSync = iconIndicatorPendingSync,
                iconOnlyVisibleToYou = iconOnlyVisibleToYou,
                messageDeletedBackground = messageDeletedBackground,
                textStyleMessageDeleted = textStyleMessageDeleted,
                messageDeletedBackgroundMine = messageDeletedBackgroundMine.nullIfNotSet(),
                textStyleMessageDeletedMine = textStyleMessageDeletedMine.nullIfEqualsTo(textStyleMessageDeleted),
                messageDeletedBackgroundTheirs = messageDeletedBackgroundTheirs.nullIfNotSet(),
                textStyleMessageDeletedTheirs = textStyleMessageDeletedTheirs.nullIfEqualsTo(textStyleMessageDeleted),
                messageStrokeColorMine = messageStrokeColorMine,
                messageStrokeWidthMine = messageStrokeWidthMine,
                messageStrokeColorTheirs = messageStrokeColorTheirs,
                messageStrokeWidthTheirs = messageStrokeWidthTheirs,
                textStyleSystemMessage = textStyleSystemMessage,
                textStyleErrorMessage = textStyleErrorMessage,
                pinnedMessageIndicatorTextStyle = pinnedMessageIndicatorTextStyle,
                pinnedMessageIndicatorIcon = pinnedMessageIndicatorIcon,
                pinnedMessageBackgroundColor = pinnedMessageBackgroundColor,
                messageStartMargin = messageStartMargin,
                messageEndMargin = messageEndMargin,
                messageMaxWidthFactorMine = messageMaxWidthFactorMine,
                messageMaxWidthFactorTheirs = messageMaxWidthFactorTheirs,
                showMessageDeliveryStatusIndicator = showMessageDeliveryStatusIndicator,
                iconFailedMessage = iconFailedMessage,
                iconBannedMessage = iconBannedMessage,
                systemMessageAlignment = systemMessageGravity,
                loadingMoreView = loadingMoreView,
                unreadSeparatorBackgroundColor = unreadSeparatorBackgroundColor,
                unreadSeparatorTextStyle = unreadSeparatorTextStyle,
            ).let(TransformStyle.messageListItemStyleTransformer::transform)
                .also { style -> style.checkMessageMaxWidthFactorsRange() }
        }

        private fun Int.nullIfNotSet(): Int? {
            return if (this == VALUE_NOT_SET) null else this
        }

        private fun TextStyle.nullIfEqualsTo(defaultValue: TextStyle): TextStyle? {
            return if (this == defaultValue) null else this
        }

        private fun MessageListItemStyle.checkMessageMaxWidthFactorsRange() {
            require(messageMaxWidthFactorMine in 0.75..1.0) { "messageMaxWidthFactorMine cannot be lower than 0.75 and greater than 1! Current value: $messageMaxWidthFactorMine" }
            require(messageMaxWidthFactorTheirs in 0.75..1.0) { "messageMaxWidthFactorTheirs cannot be lower than 0.75 and greater than 1! Current value: $messageMaxWidthFactorTheirs" }
        }
    }
}
