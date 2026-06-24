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

package io.getstream.chat.android.compose.ui.util

import android.view.View
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsOwner
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.core.content.getSystemService

/**
 * Observes [AccessibilityManager.isTouchExplorationEnabled] and recomposes when it toggles.
 *
 * Used to gate behaviour that should only apply when an explore-by-touch service (e.g. TalkBack)
 * is active.
 *
 * @return `true` when an explore-by-touch service is active, `false` otherwise.
 */
@Composable
internal fun rememberIsTouchExplorationEnabled(): Boolean {
    val context = LocalContext.current
    val manager = remember(context) { context.getSystemService<AccessibilityManager>() } ?: return false
    var enabled by remember(manager) { mutableStateOf(manager.isTouchExplorationEnabled) }
    DisposableEffect(manager) {
        val listener = AccessibilityManager.TouchExplorationStateChangeListener { enabled = it }
        manager.addTouchExplorationStateChangeListener(listener)
        enabled = manager.isTouchExplorationEnabled
        onDispose { manager.removeTouchExplorationStateChangeListener(listener) }
    }
    return enabled
}

/**
 * Moves the screen-reader (accessibility) cursor to the composable tagged with [testTag] by
 * dispatching [AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS]. It moves only the reading cursor,
 * not input focus, so no keyboard opens and the field does not become the input target.
 *
 * Compose exposes no public API for this, so the node id is resolved from the [SemanticsOwner]
 * (the only non-public step). Fails safe: returns `false` and leaves focus untouched if anything
 * cannot be resolved.
 *
 * @param testTag The test tag of the node to focus.
 * @return `true` if the accessibility focus action was dispatched, `false` otherwise.
 */
internal fun View.requestAccessibilityFocusForTestTag(testTag: String): Boolean = runCatching {
    val nodeId = semanticsNodeIdForTestTag(testTag) ?: return@runCatching false
    val provider = accessibilityNodeProvider ?: return@runCatching false
    provider.performAction(nodeId, AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null)
}.getOrDefault(false)

/**
 * Resolves the accessibility virtual-view id (the Compose semantics node id) of the node tagged with
 * [testTag] under this view's [SemanticsOwner]. Returns `null` if this view is not a Compose host,
 * the owner cannot be resolved, or no node carries the tag. Fails safe (never throws).
 *
 * @param testTag The test tag to look up.
 * @return The semantics node id, or `null` if it cannot be resolved.
 */
internal fun View.semanticsNodeIdForTestTag(testTag: String): Int? = runCatching {
    val owner = javaClass.getMethod("getSemanticsOwner").invoke(this) as? SemanticsOwner
        ?: return@runCatching null
    owner.unmergedRootSemanticsNode.findByTestTag(testTag)?.id
}.getOrNull()

private fun SemanticsNode.findByTestTag(testTag: String): SemanticsNode? =
    if (config.getOrNull(SemanticsProperties.TestTag) == testTag) {
        this
    } else {
        children.firstNotNullOfOrNull { it.findByTestTag(testTag) }
    }
