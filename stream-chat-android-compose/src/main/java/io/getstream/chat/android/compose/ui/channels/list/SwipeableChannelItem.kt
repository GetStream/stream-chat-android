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
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.flow.filter
import kotlin.math.roundToInt

/**
 * A wrapper that adds swipe-to-reveal actions behind channel list item content.
 *
 * Uses [AnchoredDraggableState] to provide smooth swipe interaction with snap-to anchors.
 * Supports RTL layouts and coordinates with [SwipeRevealCoordinator] for single-open behavior.
 *
 * @param channelCid The channel CID used for coordinator registration.
 * @param modifier Modifier for styling.
 * @param enabled Whether swiping is enabled.
 * @param swipeActions Composable content for the action buttons revealed by swiping.
 * @param content The channel item content displayed on top.
 */
@Composable
public fun SwipeableChannelItem(
    channelCid: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    swipeActions: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val coordinator = LocalSwipeRevealCoordinator.current

    val anchoredDraggableState = remember {
        AnchoredDraggableState(initialValue = SwipeRevealValue.Closed)
    }

    var actionsWidthPx by remember { mutableIntStateOf(0) }

    // LaunchedEffect (not SideEffect) so the key triggers recomposition after onSizeChanged.
    LaunchedEffect(actionsWidthPx) {
        if (actionsWidthPx > 0) {
            val newAnchors = DraggableAnchors {
                SwipeRevealValue.Closed at 0f
                SwipeRevealValue.Open at -actionsWidthPx.toFloat()
            }
            anchoredDraggableState.updateAnchors(newAnchors, anchoredDraggableState.currentValue)
        }
    }

    // Register/unregister with coordinator
    DisposableEffect(channelCid, coordinator) {
        coordinator?.register(channelCid, anchoredDraggableState)
        onDispose {
            coordinator?.unregister(channelCid)
        }
    }

    // Notify coordinator when this item opens
    LaunchedEffect(anchoredDraggableState) {
        snapshotFlow { anchoredDraggableState.currentValue }
            .filter { it == SwipeRevealValue.Open }
            .collect { coordinator?.onItemOpened(channelCid) }
    }

    Box(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .clipToBounds(),
    ) {
        // Background: action buttons
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .align(if (isRtl) Alignment.CenterStart else Alignment.CenterEnd)
                .onSizeChanged { size -> actionsWidthPx = size.width },
        ) {
            swipeActions()
        }

        // Foreground: channel item content
        Box(
            modifier = Modifier
                .offset {
                    val x = anchoredDraggableState.offset.roundToInt()
                    IntOffset(x = if (isRtl) -x else x, y = 0)
                }
                .anchoredDraggable(
                    state = anchoredDraggableState,
                    orientation = Orientation.Horizontal,
                    enabled = enabled,
                ),
        ) {
            content()
        }
    }
}
