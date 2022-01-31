package io.getstream.chat.android.ui.message.composer

import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow

/**
 * Represents a popup window that is shown above the [MessageComposerView]. It is used
 * to display command or mention suggestions the user can tap to autocomplete the input.
 */
internal class MessageComposerSuggestionsPopup(
    private val suggestionView: View,
    private val anchorView: View,
    dismissListener: OnDismissListener,
) : PopupWindow(
    suggestionView,
    ViewGroup.LayoutParams.MATCH_PARENT,
    ViewGroup.LayoutParams.WRAP_CONTENT
) {
    init {
        isOutsideTouchable = true
        inputMethodMode = INPUT_METHOD_NEEDED
        setOnDismissListener(dismissListener)
    }

    /**
     * Shows the popup with the given [suggestionView] or updates the size and position
     * of the existing one.
     */
    fun showOrUpdate() {
        val displayWidth = Resources.getSystem().displayMetrics.widthPixels
        suggestionView.measure(
            View.MeasureSpec.makeMeasureSpec(displayWidth, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        val popupWindowOffset = -(suggestionView.measuredHeight + anchorView.height)
        if (isShowing) {
            update(anchorView, 0, popupWindowOffset, -1, -1)
        } else {
            showAsDropDown(anchorView, 0, popupWindowOffset)
        }
    }
}
