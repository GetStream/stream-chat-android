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

package io.getstream.chat.android.compose.ui.messages.list

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.ui.unit.IntSize
import io.getstream.chat.android.models.Message
import java.io.Serializable

/**
 * Provides a wrapper around lazy list state to be used with [Messages] composable. It is used to keep track of the
 * focused message offset needed to center the focused message in the scroll list.
 *
 * @param lazyListState State of the lazy list that represents the list of messages. Useful for controlling the
 * scroll state.
 * @param messageOffsetHandler The handler that calculates the focused message offset.
 */
public data class MessagesLazyListState(
    public val lazyListState: LazyListState,
    private val messageOffsetHandler: MessageOffsetHandler = defaultOffsetHandler,
) {

    /**
     * By default tracks the size of [Messages] composable. Used to calculate [focusedMessageOffset].
     */
    private val _parentSize: MutableState<IntSize> = mutableStateOf(IntSize.Zero)

    /**
     * Tracks the state of the focused [Message]. Used to calculate [focusedMessageOffset].
     */
    private val _focusedMessageSize: MutableState<IntSize> = mutableStateOf(IntSize.Zero)

    /**
     * The offset to be applied to a message after it has been marked for focus.
     */
    private val _focusedMessageOffset: MutableState<Int> = mutableStateOf(0)
    public val focusedMessageOffset: Int by _focusedMessageOffset

    /**
     * Updates the list parent holder. By default will update the size of [Messages] composable.
     *
     * @param parentSize The size of the parent view.
     */
    public fun updateParentSize(parentSize: IntSize) {
        if (parentSize == _parentSize.value) return
        _parentSize.value = parentSize
        calculateMessageOffset(parentSize, _focusedMessageSize.value)
    }

    /**
     * Updates the size of the focused message.
     *
     * @param focusedMessageSize The size of the message view we wish to focus to on the screen.
     */
    public fun updateFocusedMessageSize(focusedMessageSize: IntSize) {
        if (focusedMessageSize == _focusedMessageSize.value) return
        _focusedMessageSize.value = focusedMessageSize
        calculateMessageOffset(_parentSize.value, focusedMessageSize)
    }

    /**
     * Calculates the offset of the focused message on the screen.
     *
     * @param parentSize The size of the parent view.
     * @param focusedMessageSize The size of the message view we wish to focus to on the screen.
     */
    private fun calculateMessageOffset(parentSize: IntSize, focusedMessageSize: IntSize) {
        _focusedMessageOffset.value = messageOffsetHandler.calculateOffset(parentSize, focusedMessageSize)
    }

    /**
     * Handler that calculates the message offset that will be applied to the focused message. Implements [Serializable]
     * so that it can be saved using [Saver] to persist the custom implementation of the handler across compositions.
     */
    public fun interface MessageOffsetHandler : Serializable {
        /**
         * Calculates the focused message offset.
         *
         * @param parentSize The size of the parent containing the [Message] we wish to focus.
         * @param focusedMessageSize The size of the message we wish to focus.
         *
         * @return The offset that the focused message will have.
         */
        public fun calculateOffset(parentSize: IntSize, focusedMessageSize: IntSize): Int
    }

    public companion object {
        private const val KeyFirstVisibleItemIndex: String = "firstVisibleItemIndex"
        private const val KeyFirstVisibleItemScrollOffset: String = "firstVisibleItemScrollOffset"
        private const val KeyMessageOffsetHandler: String = "messageOffsetHandler"

        /**
         * The default [Saver] implementation for [MessagesLazyListState]. Used to persist the [MessagesLazyListState]
         * across compositions.
         *
         * @see LazyListState.Saver
         */
        public val Saver: Saver<MessagesLazyListState, *> = mapSaver(
            save = {
                mapOf(
                    KeyFirstVisibleItemIndex to it.lazyListState.firstVisibleItemIndex,
                    KeyFirstVisibleItemScrollOffset to it.lazyListState.firstVisibleItemScrollOffset,
                    KeyMessageOffsetHandler to it.messageOffsetHandler,
                )
            },
            restore = {
                MessagesLazyListState(
                    LazyListState(
                        firstVisibleItemIndex = (it[KeyFirstVisibleItemIndex] as? Int) ?: 0,
                        firstVisibleItemScrollOffset = (it[KeyFirstVisibleItemScrollOffset] as? Int) ?: 0,
                    ),
                    messageOffsetHandler = (it[KeyMessageOffsetHandler] as? MessageOffsetHandler)
                        ?: defaultOffsetHandler,
                )
            },
        )

        /**
         * Default implementation of [MessageOffsetHandler]. Will focus the message to the center of the screen.
         */
        internal val defaultOffsetHandler: MessageOffsetHandler =
            MessageOffsetHandler { parentSize, focusedMessageSize ->
                when {
                    parentSize.height == 0 && focusedMessageSize.height == 0 -> 0
                    parentSize.height != 0 && focusedMessageSize.height == 0 -> -parentSize.height / 2
                    else -> {
                        -parentSize.height / 2
                        val sizeDiff = parentSize.height - focusedMessageSize.height
                        if (sizeDiff > 0) {
                            -sizeDiff / 2
                        } else {
                            -sizeDiff
                        }
                    }
                }
            }
    }
}
