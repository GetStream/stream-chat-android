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

package io.getstream.chat.android.compose.ui.channels.list

import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.runtime.compositionLocalOf
import io.getstream.chat.android.ui.common.state.channels.actions.ChannelAction

/**
 * Coordinates swipe-to-reveal state across multiple channel list items,
 * ensuring only one item is open at a time.
 */
public class SwipeRevealCoordinator {

    private val registry = mutableMapOf<String, AnchoredDraggableState<SwipeRevealValue>>()

    /**
     * Registers a swipe state for the given channel CID.
     * Called from [SwipeableChannelItem]'s DisposableEffect.
     */
    public fun register(cid: String, state: AnchoredDraggableState<SwipeRevealValue>) {
        registry[cid] = state
    }

    /**
     * Unregisters the swipe state for the given channel CID.
     * Called when [SwipeableChannelItem] leaves composition.
     */
    public fun unregister(cid: String) {
        registry.remove(cid)
    }

    /**
     * Notifies the coordinator that an item has been opened.
     * All other open items will be animated to [SwipeRevealValue.Closed].
     */
    public suspend fun onItemOpened(cid: String) {
        registry.forEach { (key, state) ->
            if (key != cid && state.currentValue == SwipeRevealValue.Open) {
                state.animateTo(SwipeRevealValue.Closed)
            }
        }
    }

    /**
     * Closes all currently open items.
     */
    public suspend fun closeAll() {
        registry.forEach { (_, state) ->
            if (state.currentValue == SwipeRevealValue.Open) {
                state.animateTo(SwipeRevealValue.Closed)
            }
        }
    }
}

/**
 * Provides the [SwipeRevealCoordinator] to child composables.
 * `null` means swipe actions are not available in the current composition tree.
 */
internal val LocalSwipeRevealCoordinator = compositionLocalOf<SwipeRevealCoordinator?> { null }

/**
 * Provides a handler for swipe channel actions (pin, mute, delete, etc.).
 * `null` means no handler is available.
 */
internal val LocalSwipeActionHandler = compositionLocalOf<((ChannelAction) -> Unit)?> { null }
