package io.getstream.chat.android.ui.utils

import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import com.getstream.sdk.chat.utils.Utils
import io.getstream.chat.android.ui.utils.LongClickFriendlyLinkMovementMethod.Companion.set

/**
 * A customized [LinkMovementMethod] implementation that lets you handle links being
 * clicked with [onLinkClicked], while also keeping long clicks as a separate event,
 * forwarded to [longClickTarget].
 *
 * This class is reattached to the target [textView] every time its content changes,
 * so you only need to call [set] one time.
 */
internal class LongClickFriendlyLinkMovementMethod private constructor(
    private val textView: TextView,
    private val longClickTarget: View,
    private val onLinkClicked: (url: String) -> Unit,
) : Utils.TextViewLinkHandler() {
    private var isLongClick = false

    init {
        textView.setOnLongClickListener {
            isLongClick = true
            longClickTarget.performLongClick()
            false
        }
        textView.doAfterTextChanged {
            textView.movementMethod = this
        }
    }

    override fun onLinkClick(url: String) {
        if (isLongClick) {
            isLongClick = false
            return
        }
        onLinkClicked(url)
    }

    companion object {
        fun set(
            textView: TextView,
            longClickTarget: View,
            onLinkClicked: (url: String) -> Unit,
        ) {
            LongClickFriendlyLinkMovementMethod(textView, longClickTarget, onLinkClicked)
        }
    }
}
