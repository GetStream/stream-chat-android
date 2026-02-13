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

package io.getstream.chat.android.compose.state.messages

/**
 * Representation of a message reaction, holding all information required to render the reaction.
 *
 * @param type The string representation of the reaction.
 * @param emoji The emoji character the [type] maps to, if any.
 * @param count The number of reactions of this type in the message.
 */
public data class MessageReactionItemState(
    public val type: String,
    public val emoji: String?,
    public val count: Int,
)
