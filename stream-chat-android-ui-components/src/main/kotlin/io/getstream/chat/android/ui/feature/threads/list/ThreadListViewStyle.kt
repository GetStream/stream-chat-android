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

package io.getstream.chat.android.ui.feature.threads.list

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.Px
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.feature.messages.preview.MessagePreviewStyle
import io.getstream.chat.android.ui.font.TextStyle
import io.getstream.chat.android.ui.helper.TransformStyle
import io.getstream.chat.android.ui.helper.ViewStyle
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.getDrawableCompat
import io.getstream.chat.android.ui.utils.extensions.use

/**
 * Class holding the customizable styling parameters for [ThreadListView].
 *
 * @param backgroundColor The background color of the thread list.
 * @param emptyStateDrawable Drawable shown when there are no threads.
 * @param emptyStateText Text shown when there are no threads.
 * @param emptyStateTextStyle Style for the empty state text.
 * @param threadIconDrawable Drawable for the thread icon.
 * @param threadTitleStyle Style for the thread title.
 * @param threadReplyToStyle Style for the thread reply to text.
 * @param latestReplyStyle Style for the latest reply message preview.
 * @param unreadCountBadgeTextStyle Style for the unread count badge.
 * @param unreadCountBadgeBackground Background for the unread count badge.
 * @param bannerTextStyle Style for the unread threads banner text.
 * @param bannerIcon Icon for the unread threads banner.
 * @param bannerBackground Background for the unread threads banner.
 * @param bannerPaddingLeft Left padding for the unread threads banner.
 * @param bannerPaddingTop Top padding for the unread threads banner.
 * @param bannerPaddingRight Right padding for the unread threads banner.
 * @param bannerPaddingBottom Bottom padding for the unread threads banner.
 * @param bannerMarginLeft Left margin for the unread threads banner.
 * @param bannerMarginTop Top margin for the unread threads banner.
 * @param bannerMarginRight Right margin for the unread threads banner.
 * @param bannerMarginBottom Bottom margin for the unread threads banner.
 */
public data class ThreadListViewStyle(
    // General
    @ColorInt public val backgroundColor: Int,
    // Empty state
    public val emptyStateDrawable: Drawable,
    public val emptyStateText: String,
    public val emptyStateTextStyle: TextStyle,
    // Results
    public val threadIconDrawable: Drawable,
    public val threadTitleStyle: TextStyle,
    public val threadReplyToStyle: TextStyle,
    public val latestReplyStyle: MessagePreviewStyle,
    public val unreadCountBadgeTextStyle: TextStyle,
    public val unreadCountBadgeBackground: Drawable,
    // Unread threads banner
    public val bannerTextStyle: TextStyle,
    public val bannerIcon: Drawable,
    public val bannerBackground: Drawable,
    @Px public val bannerPaddingLeft: Int,
    @Px public val bannerPaddingTop: Int,
    @Px public val bannerPaddingRight: Int,
    @Px public val bannerPaddingBottom: Int,
    @Px public val bannerMarginLeft: Int,
    @Px public val bannerMarginTop: Int,
    @Px public val bannerMarginRight: Int,
    @Px public val bannerMarginBottom: Int,
) : ViewStyle {

    @Suppress("TooManyFunctions")
    internal companion object {

        /**
         * Creates a [ThreadListViewStyle] from the declared XML properties.
         */
        operator fun invoke(context: Context, attrs: AttributeSet?): ThreadListViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.ThreadListView,
                R.attr.streamUiThreadListStyle,
                R.style.StreamUi_ThreadList,
            ).use { typedArray ->
                // General
                val backgroundColor = backgroundColor(context, typedArray)
                // Empty state
                val emptyStateDrawable = emptyStateDrawable(context, typedArray)
                val emptyStateText = emptyStateText(context, typedArray)
                val emptyStateTextStyle = emptyStateTextStyle(context, typedArray)
                // Results
                val threadIconDrawable = threadIconDrawable(context, typedArray)
                val threadTitleStyle = threadTitleStyle(context, typedArray)
                val threadReplyToStyle = threadReplyToStyle(context, typedArray)
                val latestReplySenderStyle = threadLatestReplySenderStyle(context, typedArray)
                val latestReplyMessageStyle = threadLatestReplyMessageStyle(context, typedArray)
                val latestReplyTimeStyle = threadLatestReplyTimeStyle(context, typedArray)
                val unreadCountBadgeTextStyle = unreadCountBadgeTextStyle(context, typedArray)
                val unreadCountBadgeBackground = unreadCountBadgeBackground(context, typedArray)
                // Unread threads banner
                val bannerTextStyle = bannerTextStyle(context, typedArray)
                val bannerIcon = bannerIcon(context, typedArray)
                val bannerBackground = bannerBackground(context, typedArray)
                val bannerPaddingLeft = bannerPaddingLeft(context, typedArray)
                val bannerPaddingTop = bannerPaddingTop(context, typedArray)
                val bannerPaddingRight = bannerPaddingRight(context, typedArray)
                val bannerPaddingBottom = bannerPaddingBottom(context, typedArray)
                val bannerMarginLeft = bannerMarginLeft(context, typedArray)
                val bannerMarginTop = bannerMarginTop(context, typedArray)
                val bannerMarginRight = bannerMarginRight(context, typedArray)
                val bannerMarginBottom = bannerMarginBottom(context, typedArray)
                return ThreadListViewStyle(
                    backgroundColor = backgroundColor,
                    emptyStateDrawable = emptyStateDrawable,
                    emptyStateText = emptyStateText,
                    emptyStateTextStyle = emptyStateTextStyle,
                    threadIconDrawable = threadIconDrawable,
                    threadTitleStyle = threadTitleStyle,
                    threadReplyToStyle = threadReplyToStyle,
                    latestReplyStyle = MessagePreviewStyle(
                        messageSenderTextStyle = latestReplySenderStyle,
                        messageTextStyle = latestReplyMessageStyle,
                        messageTimeTextStyle = latestReplyTimeStyle,
                    ),
                    unreadCountBadgeTextStyle = unreadCountBadgeTextStyle,
                    unreadCountBadgeBackground = unreadCountBadgeBackground,
                    bannerTextStyle = bannerTextStyle,
                    bannerIcon = bannerIcon,
                    bannerBackground = bannerBackground,
                    bannerPaddingLeft = bannerPaddingLeft,
                    bannerPaddingTop = bannerPaddingTop,
                    bannerPaddingRight = bannerPaddingRight,
                    bannerPaddingBottom = bannerPaddingBottom,
                    bannerMarginLeft = bannerMarginLeft,
                    bannerMarginTop = bannerMarginTop,
                    bannerMarginRight = bannerMarginRight,
                    bannerMarginBottom = bannerMarginBottom,
                ).let(TransformStyle.threadListViewStyle::transform)
            }
        }

        private fun backgroundColor(context: Context, typedArray: TypedArray) = typedArray.getColor(
            R.styleable.ThreadListView_streamUiThreadListBackground,
            context.getColorCompat(R.color.stream_ui_white_snow),
        )

        private fun emptyStateDrawable(context: Context, typedArray: TypedArray) =
            typedArray.getDrawable(R.styleable.ThreadListView_streamUiThreadListEmptyStateDrawable)
                ?: context.getDrawableCompat(R.drawable.stream_ui_ic_threads_empty)!!

        private fun emptyStateText(context: Context, typedArray: TypedArray) =
            typedArray.getString(R.styleable.ThreadListView_streamUiThreadListEmptyStateText)
                ?: context.getString(R.string.stream_ui_thread_list_empty_title)

        private fun emptyStateTextStyle(context: Context, typedArray: TypedArray) = TextStyle.Builder(typedArray)
            .size(
                R.styleable.ThreadListView_streamUiThreadListEmptyStateTextSize,
                context.getDimension(R.dimen.stream_ui_text_large),
            )
            .color(
                R.styleable.ThreadListView_streamUiThreadListEmptyStateTextColor,
                context.getColorCompat(R.color.stream_ui_text_color_secondary),
            )
            .font(
                R.styleable.ThreadListView_streamUiThreadListEmptyStateTextFontAssets,
                R.styleable.ThreadListView_streamUiThreadListEmptyStateTextFont,
            )
            .style(R.styleable.ThreadListView_streamUiThreadListEmptyStateTextStyle, Typeface.NORMAL)
            .build()

        private fun threadIconDrawable(context: Context, typedArray: TypedArray) =
            typedArray.getDrawable(R.styleable.ThreadListView_streamUiThreadListThreadIconDrawable)
                ?: context.getDrawableCompat(R.drawable.stream_ui_ic_thread)!!

        private fun threadTitleStyle(context: Context, typedArray: TypedArray) = TextStyle.Builder(typedArray)
            .size(
                R.styleable.ThreadListView_streamUiThreadListThreadTitleTextSize,
                context.getDimension(R.dimen.stream_ui_text_medium),
            )
            .color(
                R.styleable.ThreadListView_streamUiThreadListThreadTitleTextColor,
                context.getColorCompat(R.color.stream_ui_text_color_primary),
            )
            .font(
                R.styleable.ThreadListView_streamUiThreadListThreadTitleTextFontAssets,
                R.styleable.ThreadListView_streamUiThreadListThreadTitleTextFont,
            )
            .style(R.styleable.ThreadListView_streamUiThreadListThreadTitleTextStyle, Typeface.BOLD)
            .build()

        private fun threadReplyToStyle(context: Context, typedArray: TypedArray) = TextStyle.Builder(typedArray)
            .size(
                R.styleable.ThreadListView_streamUiThreadListThreadReplyToTextSize,
                context.getDimension(R.dimen.stream_ui_text_medium),
            )
            .color(
                R.styleable.ThreadListView_streamUiThreadListThreadReplyToTextColor,
                context.getColorCompat(R.color.stream_ui_text_color_secondary),
            )
            .font(
                R.styleable.ThreadListView_streamUiThreadListThreadReplyToTextFontAssets,
                R.styleable.ThreadListView_streamUiThreadListThreadReplyToTextFont,
            )
            .style(R.styleable.ThreadListView_streamUiThreadListThreadReplyToTextStyle, Typeface.NORMAL)
            .build()

        private fun threadLatestReplySenderStyle(context: Context, typedArray: TypedArray) =
            TextStyle.Builder(typedArray)
                .size(
                    R.styleable.ThreadListView_streamUiThreadListThreadLatestReplySenderTextSize,
                    context.getDimension(R.dimen.stream_ui_text_medium),
                )
                .color(
                    R.styleable.ThreadListView_streamUiThreadListThreadLatestReplySenderTextColor,
                    context.getColorCompat(R.color.stream_ui_text_color_primary),
                )
                .font(
                    R.styleable.ThreadListView_streamUiThreadListThreadLatestReplySenderTextFontAssets,
                    R.styleable.ThreadListView_streamUiThreadListThreadLatestReplySenderTextFont,
                )
                .style(
                    R.styleable.ThreadListView_streamUiThreadListThreadLatestReplySenderTextStyle,
                    Typeface.BOLD,
                )
                .build()

        private fun threadLatestReplyMessageStyle(context: Context, typedArray: TypedArray) =
            TextStyle.Builder(typedArray)
                .size(
                    R.styleable.ThreadListView_streamUiThreadListThreadLatestReplyMessageTextSize,
                    context.getDimension(R.dimen.stream_ui_text_medium),
                )
                .color(
                    R.styleable.ThreadListView_streamUiThreadListThreadLatestReplyMessageTextColor,
                    context.getColorCompat(R.color.stream_ui_text_color_secondary),
                )
                .font(
                    R.styleable.ThreadListView_streamUiThreadListThreadLatestReplyMessageTextFontAssets,
                    R.styleable.ThreadListView_streamUiThreadListThreadLatestReplyMessageTextFont,
                )
                .style(
                    R.styleable.ThreadListView_streamUiThreadListThreadLatestReplyMessageTextStyle,
                    Typeface.NORMAL,
                )
                .build()

        private fun threadLatestReplyTimeStyle(context: Context, typedArray: TypedArray) = TextStyle.Builder(typedArray)
            .size(
                R.styleable.ThreadListView_streamUiThreadListThreadLatestReplyTimeTextSize,
                context.getDimension(R.dimen.stream_ui_text_medium),
            )
            .color(
                R.styleable.ThreadListView_streamUiThreadListThreadLatestReplyTimeTextColor,
                context.getColorCompat(R.color.stream_ui_text_color_secondary),
            )
            .font(
                R.styleable.ThreadListView_streamUiThreadListThreadLatestReplyTimeTextFontAssets,
                R.styleable.ThreadListView_streamUiThreadListThreadLatestReplyTimeTextFont,
            )
            .style(
                R.styleable.ThreadListView_streamUiThreadListThreadLatestReplyTimeTextStyle,
                Typeface.NORMAL,
            )
            .build()

        private fun unreadCountBadgeTextStyle(context: Context, typedArray: TypedArray) = TextStyle.Builder(typedArray)
            .size(
                R.styleable.ThreadListView_streamUiThreadListThreadUnreadCountBadgeTextSize,
                context.getDimension(R.dimen.stream_ui_text_small),
            )
            .color(
                R.styleable.ThreadListView_streamUiThreadListThreadUnreadCountBadgeTextColor,
                context.getColorCompat(R.color.stream_ui_white),
            )
            .font(
                R.styleable.ThreadListView_streamUiThreadListThreadUnreadCountBadgeTextFontAssets,
                R.styleable.ThreadListView_streamUiThreadListThreadUnreadCountBadgeTextFont,
            )
            .style(
                R.styleable.ThreadListView_streamUiThreadListThreadUnreadCountBadgeTextStyle,
                Typeface.NORMAL,
            )
            .build()

        private fun unreadCountBadgeBackground(context: Context, typedArray: TypedArray) =
            typedArray.getDrawable(R.styleable.ThreadListView_streamUiThreadListThreadUnreadCountBadgeBackground)
                ?: context.getDrawableCompat(R.drawable.stream_ui_shape_badge_background)!!

        private fun bannerTextStyle(context: Context, typedArray: TypedArray) =
            TextStyle.Builder(typedArray)
                .size(
                    R.styleable.ThreadListView_streamUiThreadListUnreadThreadsBannerTextSize,
                    context.getDimension(R.dimen.stream_ui_text_large),
                )
                .color(
                    R.styleable.ThreadListView_streamUiThreadListUnreadThreadsBannerTextColor,
                    context.getColorCompat(R.color.stream_ui_white),
                )
                .font(
                    R.styleable.ThreadListView_streamUiThreadListUnreadThreadsBannerTextFontAssets,
                    R.styleable.ThreadListView_streamUiThreadListUnreadThreadsBannerTextFont,
                )
                .style(
                    R.styleable.ThreadListView_streamUiThreadListUnreadThreadsBannerTextStyle,
                    Typeface.NORMAL,
                )
                .build()

        private fun bannerIcon(context: Context, typedArray: TypedArray) =
            typedArray.getDrawable(R.styleable.ThreadListView_streamUiThreadListUnreadThreadsBannerIcon)
                ?: context.getDrawableCompat(R.drawable.stream_ui_ic_union)!!

        private fun bannerBackground(context: Context, typedArray: TypedArray) =
            typedArray.getDrawable(R.styleable.ThreadListView_streamUiThreadListUnreadThreadsBannerBackground)
                ?: context.getDrawableCompat(R.drawable.stream_ui_shape_unread_threads_banner)!!

        private fun bannerPaddingLeft(context: Context, typedArray: TypedArray) = typedArray.getDimensionPixelSize(
            R.styleable.ThreadListView_streamUiThreadListUnreadThreadsBannerPaddingLeft,
            context.getDimension(R.dimen.stream_ui_spacing_medium),
        )

        private fun bannerPaddingTop(context: Context, typedArray: TypedArray) = typedArray.getDimensionPixelSize(
            R.styleable.ThreadListView_streamUiThreadListUnreadThreadsBannerPaddingTop,
            context.getDimension(R.dimen.stream_ui_spacing_medium),
        )

        private fun bannerPaddingRight(context: Context, typedArray: TypedArray) = typedArray.getDimensionPixelSize(
            R.styleable.ThreadListView_streamUiThreadListUnreadThreadsBannerPaddingRight,
            context.getDimension(R.dimen.stream_ui_spacing_medium),
        )

        private fun bannerPaddingBottom(context: Context, typedArray: TypedArray) = typedArray.getDimensionPixelSize(
            R.styleable.ThreadListView_streamUiThreadListUnreadThreadsBannerPaddingBottom,
            context.getDimension(R.dimen.stream_ui_spacing_medium),
        )

        private fun bannerMarginLeft(context: Context, typedArray: TypedArray) = typedArray.getDimensionPixelSize(
            R.styleable.ThreadListView_streamUiThreadListUnreadThreadsBannerMarginLeft,
            context.getDimension(R.dimen.stream_ui_spacing_small),
        )

        private fun bannerMarginTop(context: Context, typedArray: TypedArray) = typedArray.getDimensionPixelSize(
            R.styleable.ThreadListView_streamUiThreadListUnreadThreadsBannerMarginTop,
            context.getDimension(R.dimen.stream_ui_spacing_small),
        )

        private fun bannerMarginRight(context: Context, typedArray: TypedArray) = typedArray.getDimensionPixelSize(
            R.styleable.ThreadListView_streamUiThreadListUnreadThreadsBannerMarginRight,
            context.getDimension(R.dimen.stream_ui_spacing_small),
        )

        private fun bannerMarginBottom(context: Context, typedArray: TypedArray) = typedArray.getDimensionPixelSize(
            R.styleable.ThreadListView_streamUiThreadListUnreadThreadsBannerMarginBottom,
            context.getDimension(R.dimen.stream_ui_spacing_small),
        )
    }
}
