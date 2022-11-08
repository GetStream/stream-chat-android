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

package io.getstream.chat.android.ui.feature.messages.list.internal

import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import io.getstream.chat.android.ui.common.utils.Utils
import io.getstream.chat.android.ui.feature.messages.list.internal.LongClickFriendlyLinkMovementMethod.Companion.set
import io.getstream.chat.android.ui.utils.shouldConsumeLongTap

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
        /** [shouldConsumeLongTap] check fixes issue https://github.com/GetStream/stream-chat-android/issues/3255
         * return false as before for other manufacturers
         */
        textView.setOnLongClickListener {
            isLongClick = true
            longClickTarget.performLongClick()
            shouldConsumeLongTap()
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
