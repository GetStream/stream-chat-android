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

package io.getstream.chat.android.ui.utils.extensions

import android.content.Context
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public fun EditText.showKeyboardIfFocused() {
    if (isFocused) {
        post {
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }
    }
}

@InternalStreamChatApi
public fun EditText.focusAndShowKeyboard() {
    requestFocus()

    if (hasWindowFocus()) {
        showKeyboardIfFocused()
    } else {
        viewTreeObserver.addOnWindowFocusChangeListener(
            object : ViewTreeObserver.OnWindowFocusChangeListener {
                override fun onWindowFocusChanged(hasFocus: Boolean) {
                    if (hasFocus) {
                        this@focusAndShowKeyboard.showKeyboardIfFocused()
                        viewTreeObserver.removeOnWindowFocusChangeListener(this)
                    }
                }
            },
        )
    }
}
