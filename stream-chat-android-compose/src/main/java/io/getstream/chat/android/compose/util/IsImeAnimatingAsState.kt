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
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalDensity

/**
 * `true` while an IME (keyboard) animation is in progress; `false` when it's fully settled
 * (either open or closed).
 *
 * Consumers gate `Modifier.animateItem`'s placement animation on this so items don't slide
 * during the keyboard open/close window (which reflows the LazyColumn's viewport).
 *
 * Providers typically compute the value with [rememberIsImeAnimating] and inject it via a
 * `CompositionLocalProvider`.
 */
internal val LocalIsImeAnimating: ProvidableCompositionLocal<Boolean> =
    compositionLocalOf { false }

/**
 * Returns `true` while an IME animation is in progress.
 *
 * `imeAnimationSource` and `imeAnimationTarget` diverge for the whole duration of the
 * animation and converge again once it settles. Reading them synchronously in composition
 * means the value flips on the same frame the animation starts, avoiding a one-frame lag
 * that a coroutine-based debounce approach would incur at the transition edges.
 */
@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun rememberIsImeAnimating(): Boolean {
    val density = LocalDensity.current
    val source = WindowInsets.imeAnimationSource.getBottom(density)
    val target = WindowInsets.imeAnimationTarget.getBottom(density)
    return source != target
}
