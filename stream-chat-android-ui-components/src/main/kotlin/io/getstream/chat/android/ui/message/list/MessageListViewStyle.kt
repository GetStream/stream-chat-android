package io.getstream.chat.android.ui.message.list

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.annotation.ColorInt
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getColorOrNull
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat
import io.getstream.chat.android.ui.common.extensions.internal.use
import io.getstream.chat.android.ui.common.style.TextStyle
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyViewHolder
import io.getstream.chat.android.ui.message.list.internal.ScrollButtonView

/**
 * Style for [MessageListView].
 * Use this class together with [TransformStyle.messageListStyleTransformer] to change [MessageListView] styles programmatically.
 *
 * @property scrollButtonViewStyle - style for [ScrollButtonView]
 * @property itemStyle - style for message list view holders
 * @property giphyViewHolderStyle - style for [GiphyViewHolder]
 * @property reactionsEnabled - enables/disables reactions feature. Enabled by default
 * @property backgroundColor - [MessageListView] background color. Default - [R.color.stream_ui_white_snow]
 * @property iconsTint - message options icon's tint. Default - [R.color.stream_ui_grey]
 * @property replyIcon - icon for reply option. Default - [R.drawable.stream_ui_ic_arrow_curve_left_grey]
 * @property replyEnabled - enables/disables reply feature. Enabled by default
 * @property threadReplyIcon - icon for thread option. Default - [R.drawable.stream_ui_ic_thread_reply]
 * @property threadsEnabled - enables/disables threads feature. Enabled by default
 * @property retryIcon - icon for retry option. Default - [R.drawable.stream_ui_ic_send]
 * @property copyIcon - icon for copy option. Default - [R.drawable.stream_ui_ic_copy]
 * @property editMessageEnabled - enables/disables edit message feature. Enabled by default
 * @property editIcon - icon for edit message option. Default - [R.drawable.stream_ui_ic_edit]
 * @property flagIcon - icon for flag message option. Default - [R.drawable.stream_ui_ic_flag]
 * @property flagEnabled - enables/disables "flag message" option
 * @property muteIcon - icon for mute option. Default - [R.drawable.stream_ui_ic_mute]
 * @property muteEnabled - enables/disables "mute user" option
 * @property blockIcon - icon for block option. Default - [R.drawable.stream_ui_ic_user_block]
 * @property blockEnabled - enables/disables "block user" option
 * @property deleteIcon - icon for delete message option. Default - [R.drawable.stream_ui_ic_delete]
 * @property deleteMessageEnabled - enables/disables delete message feature. Enabled by default
 * @property copyTextEnabled - enables/disables copy text feature. Enabled by default
 * @property deleteConfirmationEnabled - enables/disables showing confirmation dialog before deleting message. Enabled by default
 * @property flagMessageConfirmationEnabled - enables/disables showing confirmation dialog before flagging message. Disabled by default
 * @property warningActionsTintColor - color of dangerous option such as delete. Default - [R.color.stream_ui_accent_red].
 * @property messageOptionsText - text appearance of message option items
 * @property warningMessageOptionsText - text appearance of warning message option items
 * @property messageOptionsBackgroundColor - background color of message options. Default - [R.color.stream_ui_white]
 * @property userReactionsBackgroundColor - background color of user reactions card. Default - [R.color.stream_ui_white]
 * @property userReactionsTitleText - text appearance of of user reactions card title
 * @property optionsOverlayDimColor - overlay dim color. Default - [R.color.stream_ui_literal_transparent]
 */
public data class MessageListViewStyle(
    public val scrollButtonViewStyle: ScrollButtonViewStyle,
    public val itemStyle: MessageListItemStyle,
    public val giphyViewHolderStyle: GiphyViewHolderStyle,
    public val replyMessageStyle: MessageReplyStyle,
    public val reactionsEnabled: Boolean,
    @ColorInt public val backgroundColor: Int,
    @Deprecated(message = "Use custom icons instead", level = DeprecationLevel.WARNING)
    @ColorInt val iconsTint: Int?,
    val replyIcon: Int,
    val replyEnabled: Boolean,
    val threadReplyIcon: Int,
    val threadsEnabled: Boolean,
    val retryIcon: Int,
    val copyIcon: Int,
    val editMessageEnabled: Boolean,
    val editIcon: Int,
    val flagIcon: Int,
    val flagEnabled: Boolean,
    val muteIcon: Int,
    val unmuteIcon: Int,
    val muteEnabled: Boolean,
    val blockIcon: Int,
    val blockEnabled: Boolean,
    val deleteIcon: Int,
    val deleteMessageEnabled: Boolean,
    val copyTextEnabled: Boolean,
    val deleteConfirmationEnabled: Boolean,
    val flagMessageConfirmationEnabled: Boolean,
    @Deprecated(message = "Use deleteIcon instead", level = DeprecationLevel.WARNING)
    @ColorInt val warningActionsTintColor: Int?,
    val messageOptionsText: TextStyle,
    val warningMessageOptionsText: TextStyle,
    @ColorInt val messageOptionsBackgroundColor: Int,
    @ColorInt val userReactionsBackgroundColor: Int,
    val userReactionsTitleText: TextStyle,
    @ColorInt val optionsOverlayDimColor: Int,
) {

    internal companion object {
        private val DEFAULT_BACKGROUND_COLOR = R.color.stream_ui_white_snow

        operator fun invoke(context: Context, attrs: AttributeSet?): MessageListViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.MessageListView,
                0,
                0
            ).use { attributes ->
                val scrollButtonViewStyle = ScrollButtonViewStyle.Builder(context, attributes)
                    .scrollButtonEnabled(
                        R.styleable.MessageListView_streamUiScrollButtonEnabled,
                        true
                    )
                    .scrollButtonUnreadEnabled(
                        R.styleable.MessageListView_streamUiScrollButtonUnreadEnabled,
                        true
                    )
                    .scrollButtonColor(
                        R.styleable.MessageListView_streamUiScrollButtonColor,
                        context.getColorCompat(R.color.stream_ui_white)
                    )
                    .scrollButtonRippleColor(
                        R.styleable.MessageListView_streamUiScrollButtonRippleColor,
                        context.getColorCompat(R.color.stream_ui_white_smoke)
                    )
                    .scrollButtonBadgeColor(
                        R.styleable.MessageListView_streamUiScrollButtonBadgeColor,
                        context.getColorCompat(R.color.stream_ui_accent_blue)
                    )
                    .scrollButtonIcon(
                        R.styleable.MessageListView_streamUiScrollButtonIcon,
                        context.getDrawableCompat(R.drawable.stream_ui_ic_down)
                    ).build()

                val reactionsEnabled = attributes.getBoolean(
                    R.styleable.MessageListView_streamUiReactionsEnabled,
                    true
                )

                val backgroundColor = attributes.getColor(
                    R.styleable.MessageListView_streamUiBackgroundColor,
                    context.getColorCompat(DEFAULT_BACKGROUND_COLOR)
                )

                val itemStyle = MessageListItemStyle.Builder(attributes, context)
                    .messageBackgroundColorMine(R.styleable.MessageListView_streamUiMessageBackgroundColorMine)
                    .messageBackgroundColorTheirs(R.styleable.MessageListView_streamUiMessageBackgroundColorTheirs)
                    .messageLinkTextColorMine(R.styleable.MessageListView_streamUiMessageLinkColorMine)
                    .messageLinkTextColorTheirs(R.styleable.MessageListView_streamUiMessageLinkColorTheirs)
                    .reactionsEnabled(R.styleable.MessageListView_streamUiReactionsEnabled)
                    .threadsEnabled(R.styleable.MessageListView_streamUiThreadsEnabled)
                    .linkDescriptionMaxLines(R.styleable.MessageListView_streamUiLinkDescriptionMaxLines)
                    .build()

                val giphyViewHolderStyle = GiphyViewHolderStyle(context = context, attributes = attributes)
                val replyMessageStyle = MessageReplyStyle(context = context, attributes = attributes)

                val iconsTint = attributes.getColorOrNull(R.styleable.MessageListView_streamUiMessageOptionIconColor)

                val replyIcon = attributes.getResourceId(
                    R.styleable.MessageListView_streamUiReplyOptionIcon,
                    R.drawable.stream_ui_ic_arrow_curve_left_grey
                )

                val replyEnabled = attributes.getBoolean(R.styleable.MessageListView_streamUiReplyEnabled, true)

                val threadReplyIcon = attributes.getResourceId(
                    R.styleable.MessageListView_streamUiThreadReplyOptionIcon,
                    R.drawable.stream_ui_ic_thread_reply,
                )

                val retryIcon = attributes.getResourceId(
                    R.styleable.MessageListView_streamUiRetryOptionIcon,
                    R.drawable.stream_ui_ic_send,
                )

                val copyIcon = attributes.getResourceId(
                    R.styleable.MessageListView_streamUiCopyOptionIcon,
                    R.drawable.stream_ui_ic_copy,
                )

                val editIcon = attributes.getResourceId(
                    R.styleable.MessageListView_streamUiEditOptionIcon,
                    R.drawable.stream_ui_ic_edit,
                )

                val flagIcon = attributes.getResourceId(
                    R.styleable.MessageListView_streamUiFlagOptionIcon,
                    R.drawable.stream_ui_ic_flag,
                )

                val muteIcon = attributes.getResourceId(
                    R.styleable.MessageListView_streamUiMuteOptionIcon,
                    R.drawable.stream_ui_ic_mute
                )

                val unmuteIcon = attributes.getResourceId(
                    R.styleable.MessageListView_streamUiUnmuteOptionIcon,
                    R.drawable.stream_ui_ic_umnute,
                )

                val blockIcon = attributes.getResourceId(
                    R.styleable.MessageListView_streamUiBlockOptionIcon,
                    R.drawable.stream_ui_ic_user_block,
                )

                val deleteIcon = attributes.getResourceId(
                    R.styleable.MessageListView_streamUiDeleteOptionIcon,
                    R.drawable.stream_ui_ic_delete,
                )

                val flagEnabled = attributes.getBoolean(R.styleable.MessageListView_streamUiFlagMessageEnabled, true)

                val muteEnabled = attributes.getBoolean(R.styleable.MessageListView_streamUiMuteUserEnabled, true)

                val blockEnabled = attributes.getBoolean(R.styleable.MessageListView_streamUiBlockUserEnabled, true)

                val copyTextEnabled =
                    attributes.getBoolean(R.styleable.MessageListView_streamUiCopyMessageActionEnabled, true)

                val deleteConfirmationEnabled =
                    attributes.getBoolean(R.styleable.MessageListView_streamUiDeleteConfirmationEnabled, true)

                val flagMessageConfirmationEnabled =
                    attributes.getBoolean(R.styleable.MessageListView_streamUiFlagMessageConfirmationEnabled, false)
                val deleteMessageEnabled =
                    attributes.getBoolean(R.styleable.MessageListView_streamUiDeleteMessageEnabled, true)

                val editMessageEnabled =
                    attributes.getBoolean(R.styleable.MessageListView_streamUiEditMessageEnabled, true)

                val threadsEnabled = attributes.getBoolean(R.styleable.MessageListView_streamUiThreadsEnabled, true)

                val warningActionsTintColor =
                    attributes.getColorOrNull(R.styleable.MessageListView_streamUiWarningActionsTintColor)

                val messageOptionsText = TextStyle.Builder(attributes)
                    .size(
                        R.styleable.MessageListView_streamUiMessageOptionsTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium)
                    )
                    .color(
                        R.styleable.MessageListView_streamUiMessageOptionsTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary)
                    )
                    .font(
                        R.styleable.MessageListView_streamUiMessageOptionsTextFontAssets,
                        R.styleable.MessageListView_streamUiMessageOptionsTextFont
                    )
                    .style(
                        R.styleable.MessageListView_streamUiMessageOptionsTextStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val warningMessageOptionsText = TextStyle.Builder(attributes)
                    .size(
                        R.styleable.MessageListView_streamUiWarningMessageOptionsTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium)
                    )
                    .color(
                        R.styleable.MessageListView_streamUiWarningMessageOptionsTextColor,
                        context.getColorCompat(R.color.stream_ui_accent_red)
                    )
                    .font(
                        R.styleable.MessageListView_streamUiWarningMessageOptionsTextFontAssets,
                        R.styleable.MessageListView_streamUiWarningMessageOptionsTextFont
                    )
                    .style(
                        R.styleable.MessageListView_streamUiWarningMessageOptionsTextStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val messageOptionsBackgroundColor = attributes.getColor(
                    R.styleable.MessageListView_streamUiMessageOptionBackgroundColor,
                    context.getColorCompat(R.color.stream_ui_white)
                )

                val userReactionsBackgroundColor = attributes.getColor(
                    R.styleable.MessageListView_streamUiUserReactionsBackgroundColor,
                    context.getColorCompat(R.color.stream_ui_white)
                )

                val userReactionsTitleText = TextStyle.Builder(attributes)
                    .size(
                        R.styleable.MessageListView_streamUiUserReactionsTitleTextSize,
                        context.getDimension(R.dimen.stream_ui_text_large)
                    )
                    .color(
                        R.styleable.MessageListView_streamUiUserReactionsTitleTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary)
                    )
                    .font(
                        R.styleable.MessageListView_streamUiUserReactionsTitleTextFontAssets,
                        R.styleable.MessageListView_streamUiUserReactionsTitleTextFont
                    )
                    .style(
                        R.styleable.MessageListView_streamUiUserReactionsTitleTextStyle,
                        Typeface.BOLD
                    )
                    .build()

                val optionsOverlayDimColor = attributes.getColor(
                    R.styleable.MessageListView_streamUiOptionsOverlayDimColor,
                    context.getColorCompat(R.color.stream_ui_literal_transparent)
                )

                return MessageListViewStyle(
                    scrollButtonViewStyle = scrollButtonViewStyle,
                    reactionsEnabled = reactionsEnabled,
                    itemStyle = itemStyle,
                    giphyViewHolderStyle = giphyViewHolderStyle,
                    replyMessageStyle = replyMessageStyle,
                    backgroundColor = backgroundColor,
                    iconsTint = iconsTint,
                    replyIcon = replyIcon,
                    replyEnabled = replyEnabled,
                    threadReplyIcon = threadReplyIcon,
                    retryIcon = retryIcon,
                    copyIcon = copyIcon,
                    editIcon = editIcon,
                    flagIcon = flagIcon,
                    flagEnabled = flagEnabled,
                    muteIcon = muteIcon,
                    unmuteIcon = unmuteIcon,
                    muteEnabled = muteEnabled,
                    blockIcon = blockIcon,
                    blockEnabled = blockEnabled,
                    deleteIcon = deleteIcon,
                    copyTextEnabled = copyTextEnabled,
                    deleteConfirmationEnabled = deleteConfirmationEnabled,
                    flagMessageConfirmationEnabled = flagMessageConfirmationEnabled,
                    deleteMessageEnabled = deleteMessageEnabled,
                    editMessageEnabled = editMessageEnabled,
                    threadsEnabled = threadsEnabled,
                    warningActionsTintColor = warningActionsTintColor,
                    messageOptionsText = messageOptionsText,
                    warningMessageOptionsText = warningMessageOptionsText,
                    messageOptionsBackgroundColor = messageOptionsBackgroundColor,
                    userReactionsBackgroundColor = userReactionsBackgroundColor,
                    userReactionsTitleText = userReactionsTitleText,
                    optionsOverlayDimColor = optionsOverlayDimColor
                ).let(TransformStyle.messageListStyleTransformer::transform)
            }
        }
    }
}
