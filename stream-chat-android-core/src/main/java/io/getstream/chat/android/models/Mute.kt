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

package io.getstream.chat.android.models

import androidx.compose.runtime.Immutable
import java.util.Date

/**
 * Model holding data about a user mute.
 *
 * @param user The user who muted the target user.
 * @param target The muted user.
 * @param createdAt The date when the mute was created.
 * @param updatedAt The date when the mute was last updated.
 * @param expires The date when the mute expires.
 */
@Immutable
public data class Mute(
    val user: User?,
    val target: User?,
    val createdAt: Date,
    val updatedAt: Date,
    val expires: Date?,
)
