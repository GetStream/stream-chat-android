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

package io.getstream.chat.android.ui.feature.search

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.Px
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.helper.TransformStyle
import io.getstream.chat.android.ui.helper.ViewStyle
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getColorOrNull
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.getDimensionOrNull
import io.getstream.chat.android.ui.utils.extensions.getDrawableCompat
import io.getstream.chat.android.ui.utils.extensions.use
import kotlin.math.roundToInt

/**
 * @property textColor Color value of the search input text.
 * @property hintColor Color value of the search input hint.
 * @property searchIconDrawable Drawable of search icon visible on the right side of the SearchInputView.
 * @property clearInputDrawable Drawable of clear input icon visible on the left side of the SearchInputView.
 * @property backgroundDrawable Drawable used as the view's background.
 * @property containerBackgroundColor Color of the container background.
 * @property hintText Hint text.
 * @property textSize The size of the text in the input.
 * @property searchInputHeight The height of the root container.
 * @property searchIconWidth The width of the search icon.
 * @property searchIconHeight The height of the search icon.
 * @property searchIconMarginStart The start margin of the search icon.
 * @property clearIconWidth The width of the clear icon.
 * @property clearIconHeight The height of the clear icon.
 * @property clearIconMarginEnd The end margin of the clear icon.
 * @property textMarginStart The start margin of the input text.
 * @property textMarginEnd The end margin of the input text.
 */
public data class SearchInputViewStyle(
    @ColorInt val textColor: Int,
    @ColorInt val hintColor: Int,
    val searchIconDrawable: Drawable,
    val clearInputDrawable: Drawable,
    val backgroundDrawable: Drawable,
    val backgroundDrawableOutline: DrawableOutline?,
    @ColorInt val containerBackgroundColor: Int,
    val hintText: String,
    val textSize: Int,
    @Px val textMarginStart: Int,
    @Px val textMarginEnd: Int,
    val searchInputHeight: Int,
    @Px val searchIconWidth: Int,
    @Px val searchIconHeight: Int,
    @Px val searchIconMarginStart: Int,
    @Px val clearIconWidth: Int,
    @Px val clearIconHeight: Int,
    @Px val clearIconMarginEnd: Int,
) : ViewStyle {
    internal companion object {
        operator fun invoke(context: Context, attrs: AttributeSet?): SearchInputViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.SearchInputView,
                R.attr.streamUiSearchInputViewStyle,
                R.style.StreamUi_SearchInputView,
            ).use { a ->
                val searchIcon = a.getDrawable(R.styleable.SearchInputView_streamUiSearchInputViewSearchIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_search)!!

                val clearIcon = a.getDrawable(R.styleable.SearchInputView_streamUiSearchInputViewClearInputIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_clear)!!

                val backgroundDrawable = a.getDrawable(R.styleable.SearchInputView_streamUiSearchInputViewBackground)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_shape_search_view_background)!!

                var backgroundDrawableOutline: DrawableOutline? = null

                val backgroundDrawableOutlineColor = a.getColorOrNull(
                    R.styleable.SearchInputView_streamUiSearchInputViewBackgroundOutlineColor,
                )
                val backgroundDrawableOutlineWidth = a.getDimensionOrNull(
                    R.styleable.SearchInputView_streamUiSearchInputViewBackgroundOutlineWidth,
                )?.roundToInt()
                if (backgroundDrawableOutlineColor != null && backgroundDrawableOutlineWidth != null) {
                    backgroundDrawableOutline = DrawableOutline(
                        width = backgroundDrawableOutlineWidth,
                        color = backgroundDrawableOutlineColor,
                    )
                }

                val containerBackground = a.getColor(
                    R.styleable.SearchInputView_streamUiSearchInputViewContainerBackground,
                    context.getColorCompat(R.color.stream_ui_white),
                )

                val textColor = a.getColor(
                    R.styleable.SearchInputView_streamUiSearchInputViewTextColor,
                    context.getColorCompat(R.color.stream_ui_text_color_primary),
                )

                val hintColor = a.getColor(
                    R.styleable.SearchInputView_streamUiSearchInputViewHintColor,
                    context.getColorCompat(R.color.stream_ui_text_color_primary),
                )

                val hintText = a.getText(R.styleable.SearchInputView_streamUiSearchInputViewHintText)?.toString()
                    ?: context.getString(R.string.stream_ui_search_input_hint)

                val textSize = a.getDimensionPixelSize(
                    R.styleable.SearchInputView_streamUiSearchInputViewTextSize,
                    context.getDimension(R.dimen.stream_ui_text_medium),
                )

                val textMarginStart = a.getDimensionPixelSize(
                    R.styleable.SearchInputView_streamUiSearchInputViewTextMarginStart,
                    context.getDimension(R.dimen.stream_ui_search_input_text_margin_start),
                )

                val textMarginEnd = a.getDimensionPixelSize(
                    R.styleable.SearchInputView_streamUiSearchInputViewTextMarginEnd,
                    context.getDimension(R.dimen.stream_ui_search_input_text_margin_end),
                )

                val searchInputHeight = a.getDimensionPixelSize(
                    R.styleable.SearchInputView_streamUiSearchInputViewHeight,
                    context.getDimension(R.dimen.stream_ui_search_input_height),
                )

                val searchIconWidth = a.getDimensionPixelSize(
                    R.styleable.SearchInputView_streamUiSearchInputViewIconSearchWidth,
                    context.getDimension(R.dimen.stream_ui_search_input_icon_search_width),
                )

                val searchIconHeight = a.getDimensionPixelSize(
                    R.styleable.SearchInputView_streamUiSearchInputViewIconSearchHeight,
                    context.getDimension(R.dimen.stream_ui_search_input_icon_search_height),
                )

                val searchIconMarginStart = a.getDimensionPixelSize(
                    R.styleable.SearchInputView_streamUiSearchInputViewIconSearchMarginStart,
                    context.getDimension(R.dimen.stream_ui_search_input_icon_search_margin_start),
                )

                val clearIconWidth = a.getDimensionPixelSize(
                    R.styleable.SearchInputView_streamUiSearchInputViewIconClearWidth,
                    context.getDimension(R.dimen.stream_ui_search_input_icon_clear_width),
                )

                val clearIconHeight = a.getDimensionPixelSize(
                    R.styleable.SearchInputView_streamUiSearchInputViewIconClearHeight,
                    context.getDimension(R.dimen.stream_ui_search_input_icon_clear_height),
                )

                val clearIconMarginEnd = a.getDimensionPixelSize(
                    R.styleable.SearchInputView_streamUiSearchInputViewIconClearMarginEnd,
                    context.getDimension(R.dimen.stream_ui_search_input_icon_clear_margin_end),
                )

                return SearchInputViewStyle(
                    searchIconDrawable = searchIcon,
                    clearInputDrawable = clearIcon,
                    backgroundDrawable = backgroundDrawable,
                    backgroundDrawableOutline = backgroundDrawableOutline,
                    containerBackgroundColor = containerBackground,
                    textColor = textColor,
                    hintColor = hintColor,
                    hintText = hintText,
                    textSize = textSize,
                    textMarginStart = textMarginStart,
                    textMarginEnd = textMarginEnd,
                    searchInputHeight = searchInputHeight,
                    searchIconWidth = searchIconWidth,
                    searchIconHeight = searchIconHeight,
                    searchIconMarginStart = searchIconMarginStart,
                    clearIconWidth = clearIconWidth,
                    clearIconHeight = clearIconHeight,
                    clearIconMarginEnd = clearIconMarginEnd,
                ).let(TransformStyle.searchInputViewStyleTransformer::transform)
            }
        }
    }

    /**
     * Represents the outline of the drawable.
     *
     * @property color Color of the drawable outline.
     * @property width Width of the drawable outline.
     */
    public data class DrawableOutline(
        @Px val width: Int,
        @ColorInt val color: Int,
    )
}
