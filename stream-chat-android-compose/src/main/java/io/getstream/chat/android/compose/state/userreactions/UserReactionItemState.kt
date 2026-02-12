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

package io.getstream.chat.android.compose.state.userreactions

import io.getstream.chat.android.models.User

/**
 * UI representation of user reaction.
 *
 * @param user The user who left the reaction.
 * @param type The string representation of the reaction.
 * @param isMine Whether the reaction belongs to the current user.
 * @param emojiCode The optional emoji code to be shown for the reaction.
 */
public data class UserReactionItemState(
    public val user: User,
    public val type: String,
    public val isMine: Boolean = false,
    public val emojiCode: String? = null,
)
