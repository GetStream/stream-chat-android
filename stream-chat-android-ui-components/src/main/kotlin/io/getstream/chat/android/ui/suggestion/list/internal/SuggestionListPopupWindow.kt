package io.getstream.chat.android.ui.suggestion.list.internal

import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import io.getstream.chat.android.ui.suggestion.Suggestions
import io.getstream.chat.android.ui.suggestion.list.SuggestionListView

internal class SuggestionListPopupWindow(
    private val suggestionListView: SuggestionListView,
    private val anchor: View,
    dismissListener: OnDismissListener,
) : PopupWindow(suggestionListView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT),
    SuggestionListUi {

    init {
        isOutsideTouchable = true
        setOnDismissListener(dismissListener)
        inputMethodMode = INPUT_METHOD_NEEDED
    }

    override fun renderSuggestions(suggestions: Suggestions) {
        suggestionListView.renderSuggestions(suggestions)

        if (suggestions.hasSuggestions()) {
            val displayWidth = Resources.getSystem().displayMetrics.widthPixels
            suggestionListView.measure(
                View.MeasureSpec.makeMeasureSpec(displayWidth, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            val popupWindowOffset: Int = suggestionListView.measuredHeight + anchor.height
            if (isShowing) {
                update(anchor, 0, -popupWindowOffset, -1, -1)
            } else {
                showAsDropDown(anchor, 0, -popupWindowOffset)
            }
        } else {
            dismiss()
        }
    }

    override fun dismiss() {
        super.dismiss()
        suggestionListView.renderSuggestions(Suggestions.EmptySuggestions)
    }

    override fun isSuggestionListVisible(): Boolean {
        return suggestionListView.isSuggestionListVisible()
    }
}
