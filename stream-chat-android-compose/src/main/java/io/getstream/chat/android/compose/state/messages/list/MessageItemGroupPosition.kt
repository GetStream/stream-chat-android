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

package io.getstream.chat.android.compose.state.messages.list

/**
 * Represents the group position of a message, if the message is in a group. Otherwise represented as [None].
 *
 * Used to define the shape of the message as well as other UI styling.
 */
public sealed class MessageItemGroupPosition {

    /**
     * Message that is the first message in the group at the top.
     */
    public object Top : MessageItemGroupPosition() { override fun toString(): String = "Top" }

    /**
     * Message that has another message both at the top and bottom of it.
     */
    public object Middle : MessageItemGroupPosition() { override fun toString(): String = "Middle" }

    /**
     * Message that's the last message in the group, at the bottom.
     */
    public object Bottom : MessageItemGroupPosition() { override fun toString(): String = "Bottom" }

    /**
     * Message that is not in a group.
     */
    public object None : MessageItemGroupPosition() { override fun toString(): String = "None" }
}
