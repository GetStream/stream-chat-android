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

package io.getstream.chat.android.ui.widgets.typing

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

public class TypingIndicatorView : LinearLayout {

    public constructor(context: Context) : super(context.createStreamThemeWrapper()) {
        init(null)
    }

    public constructor(context: Context, attrs: AttributeSet?) : super(context.createStreamThemeWrapper(), attrs) {
        init(attrs)
    }

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr,
    ) {
        init(attrs)
    }

    private val userTypingTextView: TextView = TextView(context)
    private lateinit var style: TypingIndicatorViewStyle

    private fun init(attrs: AttributeSet?) {
        val horizontalPadding = 8.dpToPx()
        setPadding(horizontalPadding, 0, horizontalPadding, 0)
        gravity = Gravity.CENTER_VERTICAL
        orientation = HORIZONTAL
        isVisible = false

        style = TypingIndicatorViewStyle(context, attrs)

        streamThemeInflater.inflate(style.typingIndicatorAnimationView, this)

        addView(
            userTypingTextView.apply { setTextStyle(style.typingIndicatorUsersTextStyle) },
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
                users.size - 1,
            )
            true
        }
    }
}
