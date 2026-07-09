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

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imeAnimationSource
import androidx.compose.foundation.layout.imeAnimationTarget
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity

/**
 * Returns `true` while an IME animation is in progress; `false` when it's fully settled
 * (either open or closed).
 *
 * `imeAnimationSource` and `imeAnimationTarget` diverge for the whole duration of the
 * animation and converge again once it settles. Reading them synchronously in composition
 * means the value flips on the same frame the animation starts, avoiding a one-frame lag
 * that a coroutine-based debounce approach would incur at the transition edges.
 *
 * On some OEM ROMs (e.g. Xiaomi/MIUI) that do not dispatch IME inset animations,
 * `source` and `target` always report the same value, so this function always returns
 * `false` and the placement-animation suppression silently degrades to a no-op.
 */
@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun isImeAnimating(): Boolean {
    val density = LocalDensity.current
    val source = WindowInsets.imeAnimationSource.getBottom(density)
    val target = WindowInsets.imeAnimationTarget.getBottom(density)
    return source != target
}
