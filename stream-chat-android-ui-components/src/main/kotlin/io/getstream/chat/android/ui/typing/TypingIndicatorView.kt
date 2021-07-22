package io.getstream.chat.android.ui.typing

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.internal.dpToPx
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater

public class TypingIndicatorView : LinearLayout {

    public constructor(context: Context) : super(context.createStreamThemeWrapper()) {
        init()
    }

    public constructor(context: Context, attrs: AttributeSet?) : super(context.createStreamThemeWrapper(), attrs) {
        init()
    }

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private val userTypingTextView: TextView = TextView(context)

    private fun init() {
        val horizontalPadding = 8.dpToPx()
        setPadding(horizontalPadding, 0, horizontalPadding, 0)
        gravity = Gravity.CENTER_VERTICAL
        orientation = HORIZONTAL
        isVisible = false

        streamThemeInflater.inflate(R.layout.stream_ui_typing_indicator_animation_view, this)

        addView(
            userTypingTextView,
            LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                marginStart = 8.dpToPx()
            },
        )
    }

    public fun setTypingUsers(users: List<User>) {
        isVisible = if (users.isEmpty()) {
            false
        } else {
            userTypingTextView.text = resources.getQuantityString(
                R.plurals.stream_ui_message_list_header_typing_users,
                users.size,
                users.first().name,
                users.size - 1
            )
            true
        }
    }
}
