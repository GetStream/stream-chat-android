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
 * Model holding data related to a banned user.
 *
 * @param user The banned user.
 * @param bannedBy The user who banned the user.
 * @param channel The channel where the user was banned.
 * @param createdAt The date when the user was banned.
 * @param expires The date when the ban expires.
 * @param shadow If the ban is shadow.
 * @param reason The reason for the ban.
 */
@Immutable
public data class BannedUser(
    val user: User,
    val bannedBy: User?,
    val channel: Channel?,
    val createdAt: Date?,
    val expires: Date?,
    val shadow: Boolean,
    val reason: String?,
)
