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

package io.getstream.chat.android.compose.ui.messages.list

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.ui.unit.IntSize
import io.getstream.chat.android.client.models.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
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
    private val _parentSize: MutableStateFlow<IntSize> = MutableStateFlow(IntSize.Zero)

    /**
     * Tracks the state of the focused [Message]. Used to calculate [focusedMessageOffset].
     */
    private val _focusedMessageSize: MutableStateFlow<IntSize> = MutableStateFlow(IntSize.Zero)

    /**
     * The offset to be applied to a message after it has been marked for focus.
     */
    public val focusedMessageOffset: Flow<Int> =
        combine(_parentSize, _focusedMessageSize) { parentSize, focusedMessageSize ->
            messageOffsetHandler.calculateOffset(parentSize, focusedMessageSize)
        }.distinctUntilChanged()

    /**
     * Updates the list parent holder. By default will update the size of [Messages] composable.
     */
    public fun updateParentSize(parentSize: IntSize) {
        _parentSize.value = parentSize
    }

    /**
     * Updates the size of the focused message.
     */
    public fun updateFocusedMessageSize(focusedMessageSize: IntSize) {
        _focusedMessageSize.value = focusedMessageSize
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
         */
        public fun calculateOffset(parentSize: IntSize, focusedMessageSize: IntSize): Int
    }

    public companion object {
        private const val KeyFirstVisibleItem: String = "firstVisibleItem"
        private const val KeyFirstVisibleItemOffset: String = "firstVisibleItemOffset"
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
                    KeyFirstVisibleItem to it.lazyListState.firstVisibleItemIndex,
                    KeyFirstVisibleItemOffset to it.lazyListState.firstVisibleItemScrollOffset,
                    KeyMessageOffsetHandler to it.messageOffsetHandler
                )
            },
            restore = {
                MessagesLazyListState(
                    LazyListState(
                        firstVisibleItemIndex = (it[KeyFirstVisibleItem] as? Int) ?: 0,
                        firstVisibleItemScrollOffset = (it[KeyFirstVisibleItemOffset] as? Int) ?: 0
                    ),
                    messageOffsetHandler = (it[KeyMessageOffsetHandler] as? MessageOffsetHandler)
                        ?: defaultOffsetHandler
                )
            }
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
