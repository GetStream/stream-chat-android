package io.getstream.chat.android.ui.typing

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiTypingIndicatorViewBinding

public class TypingIndicatorView : FrameLayout {

    private val binding: StreamUiTypingIndicatorViewBinding =
        StreamUiTypingIndicatorViewBinding.inflate(streamThemeInflater, this, true)

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

    private fun init() {
        isVisible = false
    }

    public fun setTypingUsers(users: List<User>) {
        isVisible = if (users.isEmpty()) {
            false
        } else {
            binding.tvUserTyping.text = resources.getQuantityString(
                R.plurals.stream_ui_message_list_header_typing_users,
                users.size,
                users.first().name,
                users.size - 1
            )
            true
        }
    }
}
