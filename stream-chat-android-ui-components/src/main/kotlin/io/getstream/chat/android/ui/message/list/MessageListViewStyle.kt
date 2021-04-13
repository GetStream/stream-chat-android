package io.getstream.chat.android.ui.message.list

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat
import io.getstream.chat.android.ui.common.extensions.internal.use
import io.getstream.chat.android.ui.message.list.internal.ScrollButtonView
import java.io.Serializable

/**
 * Style for [MessageListView].
 * Use this class together with [TransformStyle.messageListStyleTransformer] to change [MessageListView] styles programmatically.
 *
 * @property scrollButtonViewStyle - style for [ScrollButtonView]
 * @property itemStyle - style for message list view holders
 * @property reactionsEnabled - enables/disables reactions feature. Enabled by default
 * @property backgroundColor - [MessageListView] background color. Default - [R.color.stream_ui_white_snow]
 * @property iconsTint - message options icon's tint. Default - [R.color.stream_ui_grey]
 * @property replyIcon - icon for reply option. Default - [R.drawable.stream_ui_ic_arrow_curve_left]
 * @property replyEnabled - enables/disables reply feature. Enabled by default
 * @property threadReplyIcon - icon for thread option. Default - [R.drawable.stream_ui_ic_thread_reply]
 * @property threadsEnabled - enables/disables threads feature. Enabled by default
 * @property retryIcon - icon for retry option. Default - [R.drawable.stream_ui_ic_send]
 * @property copyIcon - icon for copy option. Default - [R.drawable.stream_ui_ic_copy]
 * @property editMessageEnabled - enables/disables edit message feature. Enabled by default
 * @property editIcon - icon for edit message option. Default - [R.drawable.stream_ui_ic_edit]
 * @property flagIcon - icon for flag message option. Default - [R.drawable.stream_ui_ic_flag]
 * @property muteIcon - icon for mute option. Default - [R.drawable.stream_ui_ic_mute]
 * @property blockIcon - icon for block option. Default - [R.drawable.stream_ui_ic_user_block]
 * @property deleteIcon - icon for delete message option. Default - [R.drawable.stream_ui_ic_delete]
 * @property deleteMessageEnabled - enables/disables delete message feature. Enabled by default
 * @property copyTextEnabled - enables/disables copy text feature. Enabled by default
 * @property deleteConfirmationEnabled - enables/disables showing confirmation dialog before deleting message. Enabled by default
 */
public data class MessageListViewStyle(
    public val scrollButtonViewStyle: ScrollButtonViewStyle,
    public val itemStyle: MessageListItemStyle,
    public val reactionsEnabled: Boolean,
    @ColorInt public val backgroundColor: Int,
    @ColorInt val iconsTint: Int,
    val replyIcon: Int,
    val replyEnabled: Boolean,
    val threadReplyIcon: Int,
    val threadsEnabled: Boolean,
    val retryIcon: Int,
    val copyIcon: Int,
    val editMessageEnabled: Boolean,
    val editIcon: Int,
    val flagIcon: Int,
    val muteIcon: Int,
    val blockIcon: Int,
    val deleteIcon: Int,
    val deleteMessageEnabled: Boolean,
    val copyTextEnabled: Boolean,
    val deleteConfirmationEnabled: Boolean,
) : Serializable {

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

                val iconsTint = attributes.getColor(
                    R.styleable.MessageListView_streamUiMessageOptionIconColor,
                    ContextCompat.getColor(context, R.color.stream_ui_grey)
                )

                val replyIcon = attributes.getResourceId(
                    R.styleable.MessageListView_streamUiReplyOptionIcon,
                    R.drawable.stream_ui_ic_arrow_curve_left
                )

                val replyEnabled = attributes.getBoolean(R.styleable.MessageListView_streamUiReplyEnabled, true)

                val threadReplyIcon = attributes.getResourceId(
                    R.styleable.MessageListView_streamUiThreadReplyOptionIcon,
                    R.drawable.stream_ui_ic_thread_reply
                )

                val retryIcon = attributes.getResourceId(
                    R.styleable.MessageListView_streamUiRetryOptionIcon,
                    R.drawable.stream_ui_ic_send
                )

                val copyIcon = attributes.getResourceId(
                    R.styleable.MessageListView_streamUiCopyOptionIcon,
                    R.drawable.stream_ui_ic_copy
                )

                val editIcon = attributes.getResourceId(
                    R.styleable.MessageListView_streamUiEditOptionIcon,
                    R.drawable.stream_ui_ic_edit
                )

                val flagIcon = attributes.getResourceId(
                    R.styleable.MessageListView_streamUiFlagOptionIcon,
                    R.drawable.stream_ui_ic_flag
                )

                val muteIcon = attributes.getResourceId(
                    R.styleable.MessageListView_streamUiMuteOptionIcon,
                    R.drawable.stream_ui_ic_mute
                )

                val blockIcon = attributes.getResourceId(
                    R.styleable.MessageListView_streamUiBlockOptionIcon,
                    R.drawable.stream_ui_ic_user_block
                )

                val deleteIcon = attributes.getResourceId(
                    R.styleable.MessageListView_streamUiDeleteOptionIcon,
                    R.drawable.stream_ui_ic_delete
                )

                val copyTextEnabled = attributes.getBoolean(R.styleable.MessageListView_streamUiCopyMessageActionEnabled, true)

                val deleteConfirmationEnabled =
                    attributes.getBoolean(R.styleable.MessageListView_streamUiDeleteConfirmationEnabled, true)

                val deleteMessageEnabled =
                    attributes.getBoolean(R.styleable.MessageListView_streamUiDeleteMessageEnabled, true)

                val editMessageEnabled = attributes.getBoolean(R.styleable.MessageListView_streamUiEditMessageEnabled, true)

                val threadsEnabled = attributes.getBoolean(R.styleable.MessageListView_streamUiThreadsEnabled, true)

                return MessageListViewStyle(
                    scrollButtonViewStyle = scrollButtonViewStyle,
                    reactionsEnabled = reactionsEnabled,
                    itemStyle = itemStyle,
                    backgroundColor = backgroundColor,
                    iconsTint = iconsTint,
                    replyIcon = replyIcon,
                    replyEnabled = replyEnabled,
                    threadReplyIcon = threadReplyIcon,
                    retryIcon = retryIcon,
                    copyIcon = copyIcon,
                    editIcon = editIcon,
                    flagIcon = flagIcon,
                    muteIcon = muteIcon,
                    blockIcon = blockIcon,
                    deleteIcon = deleteIcon,
                    copyTextEnabled = copyTextEnabled,
                    deleteConfirmationEnabled = deleteConfirmationEnabled,
                    deleteMessageEnabled = deleteMessageEnabled,
                    editMessageEnabled = editMessageEnabled,
                    threadsEnabled = threadsEnabled,
                ).let(TransformStyle.messageListStyleTransformer::transform)
            }
        }
    }
}
