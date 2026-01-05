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

package io.getstream.chat.android.ui.feature.search.list

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.content.res.ResourcesCompat
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
 * Style for [SearchResultListView].
 * Use this class together with [TransformStyle.searchResultListViewStyleTransformer] to change [SearchResultListView] styles programmatically.
 *
 * @property backgroundColor Background color for search results list. Default value is [R.color.stream_ui_white].
 * @property searchInfoBarBackground Background for search info bar. Default value is [R.drawable.stream_ui_bg_gradient].
 * @property searchInfoBarTextStyle Appearance for text displayed in search info bar.
 * @property emptyStateIcon Icon for empty state view. Default value is [R.drawable.stream_ui_ic_search_empty].
 * @property emptyStateTextStyle Appearance for empty state text.
 * @property progressBarIcon Animated progress drawable. Default value is [R.drawable.stream_ui_rotating_indeterminate_progress_gradient].
 * @property messagePreviewStyle Style for single search result item.
 * @property itemHeight Height of single search result item. Default value is [R.dimen.stream_ui_search_result_list_item_height].
 * @property itemMarginStart Start margin for single search result item. Default value is [R.dimen.stream_ui_search_result_item_margin_start].
 * @property itemMarginEnd End margin for single search result item. Default value is [R.dimen.stream_ui_search_result_item_margin_end].
 * @property itemTitleMarginStart Start margin for title in single search result item. Default value is [R.dimen.stream_ui_search_result_item_title_margin_start].
 * @property itemVerticalSpacerHeight Height of the single search result item vertical spacer. Default value is [R.dimen.stream_ui_search_result_item_vertical_spacer_height].
 * @property itemVerticalSpacerPosition Position of the single search result item vertical spacer. Default value is [R.dimen.stream_ui_search_result_item_vertical_spacer_position].
 */
public data class SearchResultListViewStyle(
    @ColorInt public val backgroundColor: Int,
    public val searchInfoBarBackground: Drawable,
    public val searchInfoBarTextStyle: TextStyle,
    public val emptyStateIcon: Drawable,
    public val emptyStateTextStyle: TextStyle,
    public val progressBarIcon: Drawable,
    public val messagePreviewStyle: MessagePreviewStyle,
    @Px public val itemHeight: Int,
    @Px public val itemMarginStart: Int,
    @Px public val itemMarginEnd: Int,
    @Px public val itemTitleMarginStart: Int,
    @Px public val itemVerticalSpacerHeight: Int,
    @Px public val itemVerticalSpacerPosition: Float,
    public val itemSeparator: Drawable,
) : ViewStyle {
    internal companion object {
        operator fun invoke(context: Context, attrs: AttributeSet?): SearchResultListViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.SearchResultListView,
                R.attr.streamUiSearchResultListViewStyle,
                R.style.StreamUi_SearchResultListView,
            ).use { a ->
                val backgroundColor =
                    a.getColor(
                        R.styleable.SearchResultListView_streamUiSearchResultListBackground,
                        context.getColorCompat(R.color.stream_ui_white),
                    )

                val searchInfoBarBackground =
                    a.getDrawable(R.styleable.SearchResultListView_streamUiSearchResultListSearchInfoBarBackground)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_bg_gradient)!!
                val searchInfoBarTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.SearchResultListView_streamUiSearchResultListSearchInfoBarTextSize,
                        context.getDimension(R.dimen.stream_ui_text_small),
                    )
                    .color(
                        R.styleable.SearchResultListView_streamUiSearchResultListSearchInfoBarTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary),
                    )
                    .font(
                        R.styleable.SearchResultListView_streamUiSearchResultListSearchInfoBarTextFontAssets,
                        R.styleable.SearchResultListView_streamUiSearchResultListSearchInfoBarTextFont,
                    )
                    .style(
                        R.styleable.SearchResultListView_streamUiSearchResultListSearchInfoBarTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()

                val emptyStateIcon =
                    a.getDrawable(R.styleable.SearchResultListView_streamUiSearchResultListEmptyStateIcon)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_ic_search_empty)!!
                val emptyStateTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.SearchResultListView_streamUiSearchResultListEmptyStateTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium),
                    )
                    .color(
                        R.styleable.SearchResultListView_streamUiSearchResultListEmptyStateTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary),
                    )
                    .font(
                        R.styleable.SearchResultListView_streamUiSearchResultListEmptyStateTextFontAssets,
                        R.styleable.SearchResultListView_streamUiSearchResultListEmptyStateTextFont,
                    )
                    .style(
                        R.styleable.SearchResultListView_streamUiSearchResultListEmptyStateTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()

                val progressBarIcon =
                    a.getDrawable(R.styleable.SearchResultListView_streamUiSearchResultListProgressBarIcon)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_rotating_indeterminate_progress_gradient)!!

                val senderTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.SearchResultListView_streamUiSearchResultListSenderNameTextSize,
                        context.getDimension(R.dimen.stream_ui_channel_item_title),
                    )
                    .color(
                        R.styleable.SearchResultListView_streamUiSearchResultListSenderNameTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary),
                    )
                    .font(
                        R.styleable.SearchResultListView_streamUiSearchResultListSenderNameTextFontAssets,
                        R.styleable.SearchResultListView_streamUiSearchResultListSenderNameTextFont,
                    )
                    .style(
                        R.styleable.SearchResultListView_streamUiSearchResultListSenderNameTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()

                val messageTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.SearchResultListView_streamUiSearchResultListMessageTextSize,
                        context.getDimension(R.dimen.stream_ui_channel_item_message),
                    )
                    .color(
                        R.styleable.SearchResultListView_streamUiSearchResultListMessageTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary),
                    )
                    .font(
                        R.styleable.SearchResultListView_streamUiSearchResultListMessageTextFontAssets,
                        R.styleable.SearchResultListView_streamUiSearchResultListMessageTextFont,
                    )
                    .style(
                        R.styleable.SearchResultListView_streamUiSearchResultListMessageTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()

                val messageTimeTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.SearchResultListView_streamUiSearchResultListMessageTimeTextSize,
                        context.getDimension(R.dimen.stream_ui_channel_item_message),
                    )
                    .color(
                        R.styleable.SearchResultListView_streamUiSearchResultListMessageTimeTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary),
                    )
                    .font(
                        R.styleable.SearchResultListView_streamUiSearchResultListMessageTimeTextFontAssets,
                        R.styleable.SearchResultListView_streamUiSearchResultListMessageTimeTextFont,
                    )
                    .style(
                        R.styleable.SearchResultListView_streamUiSearchResultListMessageTimeTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()

                val itemHeight = a.getDimensionPixelSize(
                    R.styleable.SearchResultListView_streamUiSearchResulListItemHeight,
                    context.getDimension(R.dimen.stream_ui_search_result_list_item_height),
                )

                val itemMarginStart = a.getDimensionPixelSize(
                    R.styleable.SearchResultListView_streamUiSearchResulListItemMarginStart,
                    context.getDimension(R.dimen.stream_ui_search_result_item_margin_start),
                )

                val itemMarginEnd = a.getDimensionPixelSize(
                    R.styleable.SearchResultListView_streamUiSearchResulListItemMarginEnd,
                    context.getDimension(R.dimen.stream_ui_search_result_item_margin_end),
                )

                val itemTitleMarginStart = a.getDimensionPixelSize(
                    R.styleable.SearchResultListView_streamUiSearchResulListItemTitleMarginStart,
                    context.getDimension(R.dimen.stream_ui_search_result_item_title_margin_start),
                )

                val itemVerticalSpacerHeight = a.getDimensionPixelSize(
                    R.styleable.SearchResultListView_streamUiSearchResulListItemVerticalSpacerHeight,
                    context.getDimension(R.dimen.stream_ui_search_result_item_vertical_spacer_height),
                )

                val itemVerticalSpacerPosition = a.getFloat(
                    R.styleable.SearchResultListView_streamUiSearchResulListItemVerticalSpacerPosition,
                    ResourcesCompat.getFloat(
                        context.resources,
                        R.dimen.stream_ui_search_result_item_vertical_spacer_position,
                    ),
                )

                val itemSeparator = a.getDrawable(
                    R.styleable.SearchResultListView_streamUiSearchResulListItemSeparatorDrawable,
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_divider)!!

                return SearchResultListViewStyle(
                    backgroundColor = backgroundColor,
                    searchInfoBarBackground = searchInfoBarBackground,
                    searchInfoBarTextStyle = searchInfoBarTextStyle,
                    emptyStateIcon = emptyStateIcon,
                    emptyStateTextStyle = emptyStateTextStyle,
                    progressBarIcon = progressBarIcon,
                    messagePreviewStyle = MessagePreviewStyle(
                        messageSenderTextStyle = senderTextStyle,
                        messageTextStyle = messageTextStyle,
                        messageTimeTextStyle = messageTimeTextStyle,
                    ),
                    itemHeight = itemHeight,
                    itemMarginStart = itemMarginStart,
                    itemMarginEnd = itemMarginEnd,
                    itemTitleMarginStart = itemTitleMarginStart,
                    itemVerticalSpacerHeight = itemVerticalSpacerHeight,
                    itemVerticalSpacerPosition = itemVerticalSpacerPosition,
                    itemSeparator = itemSeparator,
                ).let(TransformStyle.searchResultListViewStyleTransformer::transform)
            }
        }
    }
}
