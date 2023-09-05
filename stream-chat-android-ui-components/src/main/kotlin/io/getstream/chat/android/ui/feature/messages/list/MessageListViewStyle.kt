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
import android.util.AttributeSet
import android.view.Gravity
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.state.messages.list.MessageOptionsUserReactionAlignment
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.internal.GiphyViewHolder
import io.getstream.chat.android.ui.feature.messages.list.internal.ScrollButtonView
import io.getstream.chat.android.ui.font.TextStyle
import io.getstream.chat.android.ui.helper.TransformStyle
import io.getstream.chat.android.ui.helper.ViewStyle
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.getDrawableCompat
import io.getstream.chat.android.ui.utils.extensions.use

/**
 * Style for [MessageListView].
 * Use this class together with [TransformStyle.messageListStyleTransformer] to change [MessageListView] styles programmatically.
 *
 * @property scrollButtonViewStyle Style for [ScrollButtonView].
 * @property scrollButtonBehaviour - On new messages always scroll to bottom or count new messages. Default - Count messages.
 * @property itemStyle Style for message list view holders.
 * @property giphyViewHolderStyle Style for [GiphyViewHolder].
 * @property replyMessageStyle Styles messages that are replies.
 * @property reactionsEnabled Enables/disables reactions feature. Enabled by default.
 * @property backgroundColor [MessageListView] background color. Default value is [R.color.stream_ui_white_snow].
 * @property replyIcon Icon for reply option. Default value is [R.drawable.stream_ui_ic_arrow_curve_left_grey].
 * @property replyEnabled Enables/disables reply feature. Enabled by default.
 * @property threadReplyIcon Icon for thread option. Default value is [R.drawable.stream_ui_ic_thread_reply].
 * @property threadsEnabled Enables/disables threads feature. Enabled by default.
 * @property retryIcon Icon for retry option. Default value is [R.drawable.stream_ui_ic_send].
 * @property copyIcon Icon for copy option. Default value is [R.drawable.stream_ui_ic_copy].
 * @property editMessageEnabled Enables/disables edit message feature. Enabled by default.
 * @property editIcon Icon for edit message option. Default value is [R.drawable.stream_ui_ic_edit].
 * @property flagIcon Icon for flag message option. Default value is [R.drawable.stream_ui_ic_flag].
 * @property flagEnabled Enables/disables "flag message" option.
 * @property pinIcon Icon for pin message option. Default value is [R.drawable.stream_ui_ic_pin].
 * @property unpinIcon Icon for unpin message option. Default value is [R.drawable.stream_ui_ic_unpin].
 * @property pinMessageEnabled Enables/disables pin message feature. Disabled by default.
 * @property deleteIcon Icon for delete message option. Default value is [R.drawable.stream_ui_ic_delete].
 * @property deleteMessageEnabled Enables/disables delete message feature. Enabled by default.
 * @property copyTextEnabled Enables/disables copy text feature. Enabled by default.
 * @property retryMessageEnabled Enables/disables retry failed message feature. Enabled by default.
 * @property deleteConfirmationEnabled Enables/disables showing confirmation dialog before deleting message. Enabled by default.
 * @property flagMessageConfirmationEnabled Enables/disables showing confirmation dialog before flagging message. Disabled by default.
 * @property messageOptionsText Text appearance of message option items.
 * @property warningMessageOptionsText Text appearance of warning message option items.
 * @property messageOptionsBackgroundColor Background color of message options. Default value is [R.color.stream_ui_white].
 * @property userReactionsBackgroundColor Background color of user reactions card. Default value is [R.color.stream_ui_white].
 * @property userReactionsTitleText Text appearance of of user reactions card title.
 * @property optionsOverlayDimColor Overlay dim color. Default value is [R.color.stream_ui_literal_transparent].
 * @property emptyViewTextStyle Style for the text displayed in the empty view when no data is present.
 * @property loadingView Layout for the loading view. Default value is [R.layout.stream_ui_default_loading_view].
 * @property messagesStart Messages start at the bottom or top of the screen. Default: bottom.
 * @property threadMessagesStart Thread messages start at the bottom or top of the screen. Default: bottom.
 * @property messageOptionsUserReactionAlignment Alignment of the message options user reaction bubble. Default value is [MessageOptionsUserReactionAlignment.BY_USER].
 * @property scrollButtonBottomMargin Defines the bottom margin of the scroll button.
 * @property scrollButtonEndMargin Defines the end margin of the scroll button.
 * @property disableScrollWhenShowingDialog Enables/disables scroll while a dialog is shown over the message list.
 * @property optionsOverlayEditReactionsMarginTop Defines the top margin between the edit reactions bubble on the options overlay and the parent.
 * @property optionsOverlayEditReactionsMarginBottom Defines the margin between the selected message and the edit reactions bubble on the options overlay.
 * @property optionsOverlayEditReactionsMarginStart Defines the start margin between the edit reactions bubble on the options overlay and the parent.
 * @property optionsOverlayEditReactionsMarginEnd Defines the end margin between the edit reactions bubble on the options overlay and the parent.
 * @property optionsOverlayUserReactionsMarginTop Defines the margin between the selected message and the user reaction list on the options overlay.
 * @property optionsOverlayUserReactionsMarginBottom Defines the bottom margin between the user reaction list on the options overlay and the parent.
 * @property optionsOverlayUserReactionsMarginStart Defines the start margin between the user reaction list on the options overlay and the parent.
 * @property optionsOverlayUserReactionsMarginEnd Defines the end margin between the user reaction list on the options overlay and the parent.
 * @property optionsOverlayMessageOptionsMarginTop Defines the margin between the selected message and the message option list on the options overlay.
 * @property optionsOverlayMessageOptionsMarginBottom Defines the bottom margin between the message option list on the options overlay and the user reactions view.
 * @property optionsOverlayMessageOptionsMarginStart Defines the start margin between the message option list on the options overlay and the parent.
 * @property optionsOverlayMessageOptionsMarginEnd Defines the end margin between the message option list on the options overlay and the parent.
 * @property showReactionsForUnsentMessages If we need to show the edit reactions bubble for unsent messages on the options overlay.
 * @property readCountEnabled Enables/disables read count. Enabled by default.
 */
public data class MessageListViewStyle(
    public val scrollButtonViewStyle: ScrollButtonViewStyle,
    public val scrollButtonBehaviour: MessageListView.NewMessagesBehaviour,
    public val itemStyle: MessageListItemStyle,
    public val giphyViewHolderStyle: GiphyViewHolderStyle,
    public val replyMessageStyle: MessageReplyStyle,
    public val reactionsEnabled: Boolean,
    @ColorInt public val backgroundColor: Int,
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
    val pinIcon: Int,
    val unpinIcon: Int,
    val pinMessageEnabled: Boolean,
    val deleteIcon: Int,
    val deleteMessageEnabled: Boolean,
    val copyTextEnabled: Boolean,
    val retryMessageEnabled: Boolean,
    val deleteConfirmationEnabled: Boolean,
    val flagMessageConfirmationEnabled: Boolean,
    val messageOptionsText: TextStyle,
    val warningMessageOptionsText: TextStyle,
    @ColorInt val messageOptionsBackgroundColor: Int,
    @ColorInt val userReactionsBackgroundColor: Int,
    val userReactionsTitleText: TextStyle,
    @ColorInt val optionsOverlayDimColor: Int,
    val emptyViewTextStyle: TextStyle,
    @LayoutRes public val loadingView: Int,
    public val messagesStart: Int,
    public val threadMessagesStart: Int,
    public val messageOptionsUserReactionAlignment: Int,
    public val scrollButtonBottomMargin: Int,
    public val scrollButtonEndMargin: Int,
    public val disableScrollWhenShowingDialog: Boolean,
    public val optionsOverlayEditReactionsMarginTop: Int,
    public val optionsOverlayEditReactionsMarginBottom: Int,
    public val optionsOverlayEditReactionsMarginStart: Int,
    public val optionsOverlayEditReactionsMarginEnd: Int,
    public val optionsOverlayUserReactionsMarginTop: Int,
    public val optionsOverlayUserReactionsMarginBottom: Int,
    public val optionsOverlayUserReactionsMarginStart: Int,
    public val optionsOverlayUserReactionsMarginEnd: Int,
    public val optionsOverlayMessageOptionsMarginTop: Int,
    public val optionsOverlayMessageOptionsMarginBottom: Int,
    public val optionsOverlayMessageOptionsMarginStart: Int,
    public val optionsOverlayMessageOptionsMarginEnd: Int,
    public val showReactionsForUnsentMessages: Boolean,
    public val readCountEnabled: Boolean,
) : ViewStyle {
    public companion object {
        private val DEFAULT_BACKGROUND_COLOR = R.color.stream_ui_white_snow
        private val DEFAULT_SCROLL_BUTTON_ELEVATION = 3.dpToPx().toFloat()
        private val DEFAULT_SCROLL_BUTTON_MARGIN = 6.dpToPx()
        private val DEFAULT_SCROLL_BUTTON_INTERNAL_MARGIN = 2.dpToPx()
        private val DEFAULT_SCROLL_BUTTON_BADGE_ELEVATION = DEFAULT_SCROLL_BUTTON_ELEVATION

        private val DEFAULT_EDIT_REACTIONS_MARGIN_TOP = 0.dpToPx()
        private val DEFAULT_EDIT_REACTIONS_MARGIN_BOTTOM = 0.dpToPx()
        private val DEFAULT_EDIT_REACTIONS_MARGIN_START = 50.dpToPx()
        private val DEFAULT_EDIT_REACTIONS_MARGIN_END = 8.dpToPx()

        private val DEFAULT_USER_REACTIONS_MARGIN_TOP = 8.dpToPx()
        private val DEFAULT_USER_REACTIONS_MARGIN_BOTTOM = 0.dpToPx()
        private val DEFAULT_USER_REACTIONS_MARGIN_START = 8.dpToPx()
        private val DEFAULT_USER_REACTIONS_MARGIN_END = 8.dpToPx()

        private val DEFAULT_MESSAGE_OPTIONS_MARGIN_TOP = 24.dpToPx()
        private val DEFAULT_MESSAGE_OPTIONS_MARGIN_BOTTOM = 0.dpToPx()
        private val DEFAULT_MESSAGE_OPTIONS_MARGIN_START = 50.dpToPx()
        private val DEFAULT_MESSAGE_OPTIONS_MARGIN_END = 8.dpToPx()

        /**
         * Creates an [MessageListViewStyle] instance with the default values.
         *
         * @param context The context to load resources.
         */
        public fun createDefault(context: Context): MessageListViewStyle = invoke(context, null)

        internal operator fun invoke(context: Context, attrs: AttributeSet?): MessageListViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.MessageListView,
                R.attr.streamUiMessageListStyle,
                R.style.StreamUi_MessageList,
            ).use { attributes ->
                val scrollButtonViewStyle = ScrollButtonViewStyle.Builder(context, attributes)
                    .scrollButtonEnabled(
                        scrollButtonEnabledStyleableId = R.styleable.MessageListView_streamUiScrollButtonEnabled,
                        defaultValue = true,
                    )
                    .scrollButtonUnreadEnabled(
                        scrollButtonUnreadEnabledStyleableId =
                        R.styleable.MessageListView_streamUiScrollButtonUnreadEnabled,
                        defaultValue = true,
                    )
                    .scrollButtonColor(
                        scrollButtonColorStyleableId = R.styleable.MessageListView_streamUiScrollButtonColor,
                        defaultValue = context.getColorCompat(R.color.stream_ui_white),
                    )
                    .scrollButtonRippleColor(
                        scrollButtonRippleColorStyleableId =
                        R.styleable.MessageListView_streamUiScrollButtonRippleColor,
                        defaultColor = context.getColorCompat(R.color.stream_ui_white_smoke),
                    )
                    .scrollButtonBadgeColor(
                        R.styleable.MessageListView_streamUiScrollButtonBadgeColor,
                    )
                    .scrollButtonElevation(
                        scrollButtonElevation = R.styleable.MessageListView_streamUiScrollButtonElevation,
                        defaultElevation = DEFAULT_SCROLL_BUTTON_ELEVATION,
                    )
                    .scrollButtonIcon(
                        scrollButtonIconStyleableId = R.styleable.MessageListView_streamUiScrollButtonIcon,
                        defaultIcon = context.getDrawableCompat(R.drawable.stream_ui_ic_down),
                    ).scrollButtonBadgeGravity(
                        scrollButtonBadgeGravity = R.styleable.MessageListView_streamUiScrollButtonBadgeGravity,
                        defaultGravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP,
                    ).scrollButtonBadgeIcon(
                        scrollButtonBadgeIcon = R.styleable.MessageListView_streamUiScrollButtonBadgeIcon,
                        defaultIcon = context.getDrawableCompat(R.drawable.stream_ui_shape_scroll_button_badge),
                    ).scrollButtonBadgeElevation(
                        scrollButtonBadgeElevation = R.styleable.MessageListView_streamUiScrollButtonBadgeElevation,
                        defaultElevation = DEFAULT_SCROLL_BUTTON_BADGE_ELEVATION,
                    ).scrollButtonBadgeInternalMargin(
                        scrollButtonInternalMargin = R.styleable.MessageListView_streamUIScrollButtonInternalMargin,
                        defaultMargin = DEFAULT_SCROLL_BUTTON_INTERNAL_MARGIN,
                    ).build()

                val scrollButtonBehaviour = MessageListView.NewMessagesBehaviour.parseValue(
                    attributes.getInt(
                        R.styleable.MessageListView_streamUiNewMessagesBehaviour,
                        MessageListView.NewMessagesBehaviour.COUNT_UPDATE.value,
                    ),
                )

                val scrollButtonMarginBottom =
                    attributes.getDimensionPixelSize(
                        R.styleable.MessageListView_streamUiScrollButtonBottomMargin,
                        DEFAULT_SCROLL_BUTTON_MARGIN,
                    )

                val scrollButtonMarginEnd =
                    attributes.getDimensionPixelSize(
                        R.styleable.MessageListView_streamUiScrollButtonEndMargin,
                        DEFAULT_SCROLL_BUTTON_MARGIN,
                    )

                val reactionsEnabled = attributes.getBoolean(
                    R.styleable.MessageListView_streamUiReactionsEnabled,
                    true,
                )

                val backgroundColor = attributes.getColor(
                    R.styleable.MessageListView_streamUiBackgroundColor,
                    context.getColorCompat(DEFAULT_BACKGROUND_COLOR),
                )

                val itemStyle = MessageListItemStyle.Builder(attributes, context)
                    .messageBackgroundColorMine(R.styleable.MessageListView_streamUiMessageBackgroundColorMine)
                    .messageBackgroundColorTheirs(R.styleable.MessageListView_streamUiMessageBackgroundColorTheirs)
                    .messageLinkTextColorMine(R.styleable.MessageListView_streamUiMessageLinkColorMine)
                    .messageLinkTextColorTheirs(R.styleable.MessageListView_streamUiMessageLinkColorTheirs)
                    .reactionsEnabled(R.styleable.MessageListView_streamUiReactionsEnabled)
                    .linkDescriptionMaxLines(R.styleable.MessageListView_streamUiLinkDescriptionMaxLines)
                    .systemMessageGravity(R.styleable.MessageListView_streamUiSystemMessageAlignment)
                    .build()

                val giphyViewHolderStyle = GiphyViewHolderStyle(context = context, attributes = attributes)
                val replyMessageStyle = MessageReplyStyle(context = context, attributes = attributes)

                val replyIcon = attributes.getResourceId(
                    R.styleable.MessageListView_streamUiReplyOptionIcon,
                    R.drawable.stream_ui_ic_arrow_curve_left_grey,
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

                val deleteIcon = attributes.getResourceId(
                    R.styleable.MessageListView_streamUiDeleteOptionIcon,
                    R.drawable.stream_ui_ic_delete,
                )

                val flagEnabled = attributes.getBoolean(R.styleable.MessageListView_streamUiFlagMessageEnabled, true)

                val pinIcon = attributes.getResourceId(
                    R.styleable.MessageListView_streamUiPinOptionIcon,
                    R.drawable.stream_ui_ic_pin,
                )

                val unpinIcon = attributes.getResourceId(
                    R.styleable.MessageListView_streamUiUnpinOptionIcon,
                    R.drawable.stream_ui_ic_unpin,
                )

                val pinMessageEnabled =
                    attributes.getBoolean(R.styleable.MessageListView_streamUiPinMessageEnabled, false)

                val copyTextEnabled =
                    attributes.getBoolean(R.styleable.MessageListView_streamUiCopyMessageActionEnabled, true)

                val retryMessageEnabled =
                    attributes.getBoolean(R.styleable.MessageListView_streamUiRetryMessageEnabled, true)

                val deleteConfirmationEnabled =
                    attributes.getBoolean(R.styleable.MessageListView_streamUiDeleteConfirmationEnabled, true)

                val flagMessageConfirmationEnabled =
                    attributes.getBoolean(R.styleable.MessageListView_streamUiFlagMessageConfirmationEnabled, false)
                val deleteMessageEnabled =
                    attributes.getBoolean(R.styleable.MessageListView_streamUiDeleteMessageEnabled, true)

                val editMessageEnabled =
                    attributes.getBoolean(R.styleable.MessageListView_streamUiEditMessageEnabled, true)

                val threadsEnabled = attributes.getBoolean(R.styleable.MessageListView_streamUiThreadsEnabled, true)

                val messageOptionsText = TextStyle.Builder(attributes)
                    .size(
                        R.styleable.MessageListView_streamUiMessageOptionsTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium),
                    )
                    .color(
                        R.styleable.MessageListView_streamUiMessageOptionsTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary),
                    )
                    .font(
                        R.styleable.MessageListView_streamUiMessageOptionsTextFontAssets,
                        R.styleable.MessageListView_streamUiMessageOptionsTextFont,
                    )
                    .style(
                        R.styleable.MessageListView_streamUiMessageOptionsTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()

                val warningMessageOptionsText = TextStyle.Builder(attributes)
                    .size(
                        R.styleable.MessageListView_streamUiWarningMessageOptionsTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium),
                    )
                    .color(
                        R.styleable.MessageListView_streamUiWarningMessageOptionsTextColor,
                        context.getColorCompat(R.color.stream_ui_accent_red),
                    )
                    .font(
                        R.styleable.MessageListView_streamUiWarningMessageOptionsTextFontAssets,
                        R.styleable.MessageListView_streamUiWarningMessageOptionsTextFont,
                    )
                    .style(
                        R.styleable.MessageListView_streamUiWarningMessageOptionsTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()

                val messageOptionsBackgroundColor = attributes.getColor(
                    R.styleable.MessageListView_streamUiMessageOptionBackgroundColor,
                    context.getColorCompat(R.color.stream_ui_white),
                )

                val userReactionsBackgroundColor = attributes.getColor(
                    R.styleable.MessageListView_streamUiUserReactionsBackgroundColor,
                    context.getColorCompat(R.color.stream_ui_white),
                )

                val userReactionsTitleText = TextStyle.Builder(attributes)
                    .size(
                        R.styleable.MessageListView_streamUiUserReactionsTitleTextSize,
                        context.getDimension(R.dimen.stream_ui_text_large),
                    )
                    .color(
                        R.styleable.MessageListView_streamUiUserReactionsTitleTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary),
                    )
                    .font(
                        R.styleable.MessageListView_streamUiUserReactionsTitleTextFontAssets,
                        R.styleable.MessageListView_streamUiUserReactionsTitleTextFont,
                    )
                    .style(
                        R.styleable.MessageListView_streamUiUserReactionsTitleTextStyle,
                        Typeface.BOLD,
                    )
                    .build()

                val optionsOverlayDimColor = attributes.getColor(
                    R.styleable.MessageListView_streamUiOptionsOverlayDimColor,
                    context.getColorCompat(R.color.stream_ui_literal_transparent),
                )

                val emptyViewTextStyle = emptyViewStyle(context, attributes)

                val loadingView = attributes.getResourceId(
                    R.styleable.MessageListView_streamUiMessageListLoadingView,
                    R.layout.stream_ui_default_loading_view,
                )

                val messagesStart = attributes.getInt(
                    R.styleable.MessageListView_streamUiMessagesStart,
                    MessageListView.MessagesStart.BOTTOM.value,
                )

                val threadMessagesStart = attributes.getInt(
                    R.styleable.MessageListView_streamUiThreadMessagesStart,
                    MessageListView.MessagesStart.BOTTOM.value,
                )

                val messageOptionsUserReactionAlignment = attributes.getInt(
                    R.styleable.MessageListView_streamUiMessageOptionsUserReactionAlignment,
                    MessageOptionsUserReactionAlignment.BY_USER.value,
                )

                val disableScrollWhenShowingDialog = attributes.getBoolean(
                    R.styleable.MessageListView_streamUiDisableScrollWhenShowingDialog,
                    true,
                )

                val optionsOverlayEditReactionsMarginBottom =
                    attributes.getDimensionPixelSize(
                        R.styleable.MessageListView_streamUiOptionsOverlayEditReactionsMarginBottom,
                        DEFAULT_EDIT_REACTIONS_MARGIN_BOTTOM,
                    )

                val optionsOverlayEditReactionsMarginTop =
                    attributes.getDimensionPixelSize(
                        R.styleable.MessageListView_streamUiOptionsOverlayEditReactionsMarginTop,
                        DEFAULT_EDIT_REACTIONS_MARGIN_TOP,
                    )

                val optionsOverlayEditReactionsMarginStart =
                    attributes.getDimensionPixelSize(
                        R.styleable.MessageListView_streamUiOptionsOverlayEditReactionsMarginStart,
                        DEFAULT_EDIT_REACTIONS_MARGIN_START,
                    )

                val optionsOverlayEditReactionsMarginEnd =
                    attributes.getDimensionPixelSize(
                        R.styleable.MessageListView_streamUiOptionsOverlayEditReactionsMarginEnd,
                        DEFAULT_EDIT_REACTIONS_MARGIN_END,
                    )

                val optionsOverlayUserReactionsMarginTop =
                    attributes.getDimensionPixelSize(
                        R.styleable.MessageListView_streamUiOptionsOverlayUserReactionsMarginTop,
                        DEFAULT_USER_REACTIONS_MARGIN_TOP,
                    )

                val optionsOverlayUserReactionsMarginBottom =
                    attributes.getDimensionPixelSize(
                        R.styleable.MessageListView_streamUiOptionsOverlayUserReactionsMarginBottom,
                        DEFAULT_USER_REACTIONS_MARGIN_BOTTOM,
                    )

                val optionsOverlayUserReactionsMarginStart =
                    attributes.getDimensionPixelSize(
                        R.styleable.MessageListView_streamUiOptionsOverlayUserReactionsMarginStart,
                        DEFAULT_USER_REACTIONS_MARGIN_START,
                    )

                val optionsOverlayUserReactionsMarginEnd =
                    attributes.getDimensionPixelSize(
                        R.styleable.MessageListView_streamUiOptionsOverlayUserReactionsMarginEnd,
                        DEFAULT_USER_REACTIONS_MARGIN_END,
                    )

                val optionsOverlayMessageOptionsMarginTop =
                    attributes.getDimensionPixelSize(
                        R.styleable.MessageListView_streamUiOptionsOverlayMessageOptionsMarginTop,
                        DEFAULT_MESSAGE_OPTIONS_MARGIN_TOP,
                    )

                val optionsOverlayMessageOptionsMarginBottom =
                    attributes.getDimensionPixelSize(
                        R.styleable.MessageListView_streamUiOptionsOverlayMessageOptionsMarginBottom,
                        DEFAULT_MESSAGE_OPTIONS_MARGIN_BOTTOM,
                    )

                val optionsOverlayMessageOptionsMarginStart =
                    attributes.getDimensionPixelSize(
                        R.styleable.MessageListView_streamUiOptionsOverlayMessageOptionsMarginStart,
                        DEFAULT_MESSAGE_OPTIONS_MARGIN_START,
                    )

                val optionsOverlayMessageOptionsMarginEnd =
                    attributes.getDimensionPixelSize(
                        R.styleable.MessageListView_streamUiOptionsOverlayMessageOptionsMarginEnd,
                        DEFAULT_MESSAGE_OPTIONS_MARGIN_END,
                    )

                val showReactionsForUnsentMessages = attributes.getBoolean(
                    R.styleable.MessageListView_streamUiShowReactionsForUnsentMessages,
                    false,
                )

                val readCountEnabled = attributes.getBoolean(
                    R.styleable.MessageListView_streamUiReadCount,
                    true,
                )

                return MessageListViewStyle(
                    scrollButtonViewStyle = scrollButtonViewStyle,
                    scrollButtonBehaviour = scrollButtonBehaviour,
                    scrollButtonBottomMargin = scrollButtonMarginBottom,
                    scrollButtonEndMargin = scrollButtonMarginEnd,
                    reactionsEnabled = reactionsEnabled,
                    itemStyle = itemStyle,
                    giphyViewHolderStyle = giphyViewHolderStyle,
                    replyMessageStyle = replyMessageStyle,
                    backgroundColor = backgroundColor,
                    replyIcon = replyIcon,
                    replyEnabled = replyEnabled,
                    threadReplyIcon = threadReplyIcon,
                    retryIcon = retryIcon,
                    copyIcon = copyIcon,
                    editIcon = editIcon,
                    flagIcon = flagIcon,
                    flagEnabled = flagEnabled,
                    pinIcon = pinIcon,
                    unpinIcon = unpinIcon,
                    pinMessageEnabled = pinMessageEnabled,
                    deleteIcon = deleteIcon,
                    copyTextEnabled = copyTextEnabled,
                    retryMessageEnabled = retryMessageEnabled,
                    deleteConfirmationEnabled = deleteConfirmationEnabled,
                    flagMessageConfirmationEnabled = flagMessageConfirmationEnabled,
                    deleteMessageEnabled = deleteMessageEnabled,
                    editMessageEnabled = editMessageEnabled,
                    threadsEnabled = threadsEnabled,
                    messageOptionsText = messageOptionsText,
                    warningMessageOptionsText = warningMessageOptionsText,
                    messageOptionsBackgroundColor = messageOptionsBackgroundColor,
                    userReactionsBackgroundColor = userReactionsBackgroundColor,
                    userReactionsTitleText = userReactionsTitleText,
                    optionsOverlayDimColor = optionsOverlayDimColor,
                    emptyViewTextStyle = emptyViewTextStyle,
                    loadingView = loadingView,
                    messagesStart = messagesStart,
                    threadMessagesStart = threadMessagesStart,
                    messageOptionsUserReactionAlignment = messageOptionsUserReactionAlignment,
                    disableScrollWhenShowingDialog = disableScrollWhenShowingDialog,
                    optionsOverlayEditReactionsMarginBottom = optionsOverlayEditReactionsMarginBottom,
                    optionsOverlayEditReactionsMarginTop = optionsOverlayEditReactionsMarginTop,
                    optionsOverlayEditReactionsMarginStart = optionsOverlayEditReactionsMarginStart,
                    optionsOverlayEditReactionsMarginEnd = optionsOverlayEditReactionsMarginEnd,
                    optionsOverlayUserReactionsMarginTop = optionsOverlayUserReactionsMarginTop,
                    optionsOverlayUserReactionsMarginBottom = optionsOverlayUserReactionsMarginBottom,
                    optionsOverlayUserReactionsMarginStart = optionsOverlayUserReactionsMarginStart,
                    optionsOverlayUserReactionsMarginEnd = optionsOverlayUserReactionsMarginEnd,
                    optionsOverlayMessageOptionsMarginTop = optionsOverlayMessageOptionsMarginTop,
                    optionsOverlayMessageOptionsMarginBottom = optionsOverlayMessageOptionsMarginBottom,
                    optionsOverlayMessageOptionsMarginStart = optionsOverlayMessageOptionsMarginStart,
                    optionsOverlayMessageOptionsMarginEnd = optionsOverlayMessageOptionsMarginEnd,
                    showReactionsForUnsentMessages = showReactionsForUnsentMessages,
                    readCountEnabled = readCountEnabled,
                ).let(TransformStyle.messageListStyleTransformer::transform)
            }
        }

        private fun emptyViewStyle(context: Context, typedArray: TypedArray): TextStyle {
            return TextStyle.Builder(typedArray)
                .color(
                    R.styleable.MessageListView_streamUiEmptyStateTextColor,
                    context.getColorCompat(R.color.stream_ui_text_color_primary),
                )
                .size(
                    R.styleable.MessageListView_streamUiEmptyStateTextSize,
                    context.getDimension(R.dimen.stream_ui_text_medium),
                )
                .font(
                    R.styleable.MessageListView_streamUiEmptyStateTextFontAssets,
                    R.styleable.MessageListView_streamUiEmptyStateTextFont,
                )
                .style(
                    R.styleable.MessageListView_streamUiEmptyStateTextStyle,
                    Typeface.NORMAL,
                )
                .build()
        }
    }
}
