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

package io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator

import io.getstream.chat.android.ui.feature.messages.list.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem

/**
 * Decorator interface used to decorate the view holder.
 */
public interface Decorator {

    /**
     * The type of the decorator.
     */
    public val type: Type

    /**
     * Decorates the view holder.
     */
    public fun <T : MessageListItem> decorate(
        viewHolder: BaseMessageItemViewHolder<T>,
        data: T,
    )

    /**
     * The type of the decorator.
     */
    public interface Type {

        /**
         * The SDK built-in decorators.
         */
        public enum class BuiltIn : Type {

            /**
             * Decorates the avatar of the message.
             */
            AVATAR,

            /**
             * Decorates the background of the message.
             */
            BACKGROUND,

            /**
             * Decorates the failed indicator.
             */
            FAILED_INDICATOR,

            /**
             * Decorates the footer of the message.
             */
            FOOTNOTE,

            /**
             * Decorates the gap between messages.
             */
            GAP,

            /**
             * Defines max possible width for the message container.
             */
            MAX_POSSIBLE_WIDTH,

            /**
             * Defines the margins of the message container.
             */
            MESSAGE_CONTAINER_MARGIN,

            /**
             * Decorates the pinned messages.
             */
            PIN_INDICATOR,

            /**
             * Decorates the reactions attached to the message.
             */
            REACTIONS,

            /**
             * Decorates the replies to the message.
             */
            REPLY,

            /**
             * Decorates the the message text.
             */
            TEXT,
        }
    }
}
