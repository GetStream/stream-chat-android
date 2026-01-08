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

package io.getstream.chat.android.ui.feature.messages.composer.internal

import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerView

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
    ViewGroup.LayoutParams.WRAP_CONTENT,
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
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        )

        val popupWindowOffset = suggestionView.measuredHeight + anchorView.height
        if (isShowing) {
            update(anchorView, 0, -popupWindowOffset, -1, -1)
        } else {
            showAsDropDown(anchorView, 0, -popupWindowOffset)
        }
    }
}
