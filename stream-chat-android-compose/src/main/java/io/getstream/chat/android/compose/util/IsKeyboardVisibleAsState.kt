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

package io.getstream.chat.android.compose.util

import android.graphics.Rect
import android.view.ViewTreeObserver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

/**
 * Produces a [State] indicating whether the software keyboard (IME) is currently visible.
 *
 * Uses [ViewTreeObserver.OnGlobalLayoutListener] with [android.view.View.getWindowVisibleDisplayFrame]
 * instead of [androidx.compose.foundation.layout.WindowInsets.Companion.ime], because the latter
 * depends on the host activity's window-inset configuration and may report incorrect values
 * on certain OEM ROMs (e.g. Xiaomi/MIUI).
 */
@Composable
internal fun isKeyboardVisibleAsState(): State<Boolean> {
    val view = LocalView.current
    val state = remember { mutableStateOf(false) }
    DisposableEffect(view) {
        val rect = Rect()
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val visibleDelta = screenHeight - rect.bottom
            state.value = visibleDelta > screenHeight * KeyboardVisibilityThreshold
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }
    return state
}

private const val KeyboardVisibilityThreshold = 0.15
