package io.getstream.chat.android.ui.message.input.internal

import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import io.getstream.chat.android.ui.message.input.MessageInputView
import io.getstream.chat.android.ui.suggestion.internal.SuggestionListUi
import io.getstream.chat.android.ui.suggestion.list.SuggestionListView

internal class SuggestionListPopupWindow(
    private val suggestionListView: SuggestionListView,
    private val messageInputView: MessageInputView,
) : PopupWindow(suggestionListView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT),
    SuggestionListUi {

    override fun showSuggestionList(suggestions: SuggestionListView.Suggestions) {
        suggestionListView.showSuggestionList(suggestions)

        if (suggestions.hasSuggestions()) {
            suggestionListView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            val popupWindowOffset: Int = suggestionListView.measuredHeight + messageInputView.height
            if (isShowing) {
                update(messageInputView, 0, -popupWindowOffset, -1, -1)
            } else {
                showAsDropDown(messageInputView, 0, -popupWindowOffset)
            }
        } else {
            dismiss()
        }
    }

    override fun hideSuggestionList() {
        dismiss()
        suggestionListView.hideSuggestionList()
    }

    override fun isSuggestionListVisible(): Boolean {
        return suggestionListView.isSuggestionListVisible()
    }
}
