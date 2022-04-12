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

package io.getstream.chat.android.ui.search

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat
import io.getstream.chat.android.ui.common.extensions.internal.use

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
 */
public data class SearchInputViewStyle(
    @ColorInt val textColor: Int,
    @ColorInt val hintColor: Int,
    val searchIconDrawable: Drawable,
    val clearInputDrawable: Drawable,
    val backgroundDrawable: Drawable,
    @ColorInt val containerBackgroundColor: Int,
    val hintText: String,
    val textSize: Int,
    val searchInputHeight: Int,
) {
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

                val containerBackground = a.getColor(
                    R.styleable.SearchInputView_streamUiSearchInputViewContainerBackground,
                    context.getColorCompat(R.color.stream_ui_white)
                )

                val textColor = a.getColor(
                    R.styleable.SearchInputView_streamUiSearchInputViewTextColor,
                    context.getColorCompat(R.color.stream_ui_text_color_primary)
                )

                val hintColor = a.getColor(
                    R.styleable.SearchInputView_streamUiSearchInputViewHintColor,
                    context.getColorCompat(R.color.stream_ui_text_color_primary)
                )

                val hintText = a.getText(R.styleable.SearchInputView_streamUiSearchInputViewHintText)?.toString()
                    ?: context.getString(R.string.stream_ui_search_input_hint)

                val textSize = a.getDimensionPixelSize(
                    R.styleable.SearchInputView_streamUiSearchInputViewTextSize,
                    context.getDimension(R.dimen.stream_ui_text_medium)
                )

                val searchInputHeight = a.getDimensionPixelSize(
                    R.styleable.SearchInputView_streamUiSearchInputViewHeight,
                    context.getDimension(R.dimen.stream_ui_search_input_height)
                )

                return SearchInputViewStyle(
                    searchIconDrawable = searchIcon,
                    clearInputDrawable = clearIcon,
                    backgroundDrawable = backgroundDrawable,
                    containerBackgroundColor = containerBackground,
                    textColor = textColor,
                    hintColor = hintColor,
                    hintText = hintText,
                    textSize = textSize,
                    searchInputHeight = searchInputHeight
                ).let(TransformStyle.searchInputViewStyleTransformer::transform)
            }
        }
    }
}
