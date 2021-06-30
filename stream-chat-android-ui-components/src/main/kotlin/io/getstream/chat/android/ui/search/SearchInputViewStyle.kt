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
 * @property textColor color value of the search input text
 * @property hintColor color value of the search input hint
 * @property searchIconDrawable drawable of search icon visible on the right side of the SearchInputView
 * @property clearInputDrawable drawable of clear input icon visible on the left side of the SearchInputView
 * @property backgroundDrawable drawable used as the view's background
 * @property hintText hint text
 */
public data class SearchInputViewStyle(
    @ColorInt val textColor: Int,
    @ColorInt val hintColor: Int,
    val searchIconDrawable: Drawable,
    val clearInputDrawable: Drawable,
    val backgroundDrawable: Drawable,
    val hintText: String,
    val textSize: Int,
) {
    internal companion object {
        operator fun invoke(context: Context, attrs: AttributeSet?): SearchInputViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.SearchInputView,
                R.attr.streamUiSearchInputViewStyle,
                R.style.StreamUi_SearchInputView
            ).use { a ->
                val searchIcon = a.getDrawable(R.styleable.SearchInputView_streamUiSearchInputViewSearchIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_search)!!

                val clearIcon = a.getDrawable(R.styleable.SearchInputView_streamUiSearchInputViewClearInputIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_clear)!!

                val backgroundDrawable = a.getDrawable(R.styleable.SearchInputView_streamUiSearchInputViewBackground)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_shape_search_view_background)!!

                val textColor = a.getColor(
                    R.styleable.SearchInputView_streamUiSearchInputViewTextColor,
                    context.getColorCompat(R.color.stream_ui_text_color_primary)
                )

                val hintColor = a.getColor(
                    R.styleable.SearchInputView_streamUiSearchInputViewHintColor,
                    context.getColorCompat(R.color.stream_ui_text_color_primary)
                )

                val hintText = a.getText(R.styleable.SearchInputView_streamUiSearchInputViewHintText)?.toString() ?: context.getString(R.string.stream_ui_search_input_hint)

                val textSize = a.getDimensionPixelSize(
                    R.styleable.SearchInputView_streamUiSearchInputViewTextSize,
                    context.getDimension(R.dimen.stream_ui_text_medium)
                )

                return SearchInputViewStyle(
                    searchIconDrawable = searchIcon,
                    clearInputDrawable = clearIcon,
                    backgroundDrawable = backgroundDrawable,
                    textColor = textColor,
                    hintColor = hintColor,
                    hintText = hintText,
                    textSize = textSize,
                ).let(TransformStyle.searchInputViewStyle::transform)
            }
        }
    }
}
