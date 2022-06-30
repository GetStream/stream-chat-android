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

package io.getstream.chat.android.compose.state.messages

import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Determines if the list is loading data to prepare for scroll to a certain part of the list or is currently scrolling
 * to it.
 */
public sealed class ScrollToPositionState

/**
 * State when the focused message is not in the current list and that the data is being loaded form the API.
 */
public object LoadFocusedMessageData : ScrollToPositionState()

/**
 * State when the focused message is inside the list and it should scroll to the focused message.
 *
 * @param scrollOffset The offset the list needs to apply so that the focused item is centered inside the screen
 */
public data class ScrollToFocusedMessage(
    private val scrollOffset: MutableStateFlow<Int?> = MutableStateFlow(null)
) : ScrollToPositionState() {

    public val focusedMessageOffset: StateFlow<Int?> = scrollOffset

    /**
     * Calculates the message offset needed for the message to center inside the list on scroll.
     *
     * @param parentSize The size of the list which contains the message.
     * @param focusedMessageSize The size of the message item we wish to bring to the center and focus.
     */
    public fun calculateMessageOffset(parentSize: IntSize, focusedMessageSize: IntSize) {
        if (parentSize.height == 0 || focusedMessageSize.height == 0) return

        val sizeDiff = parentSize.height - focusedMessageSize.height
        val offset = if (sizeDiff > 0) {
            -sizeDiff / 2
        } else {
            -sizeDiff
        }
        if (offset != scrollOffset.value) scrollOffset.value = offset
    }
}

/**
 * State when the loaded data does not contain the newest messages and it is loading data for it.
 */
public object LoadNewestMessages : ScrollToPositionState()

/**
 * State when the newest messages inside the list and should scroll to them.
 */
public object ScrollToNewestMessages : ScrollToPositionState()

/**
 * State when we do not do any automatic scrolling.
 */
public object Idle : ScrollToPositionState()
