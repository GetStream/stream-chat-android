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
 * Model holding data about a user flag.
 *
 * @param user The user who created the flag.
 * @param targetUser The user who was flagged.
 * @param targetMessageId The ID of the message that was flagged.
 * @param reviewedBy The user who reviewed the flag.
 * @param createdByAutomod True if the flag was created by the automod.
 * @param createdAt The date when the flag was created.
 * @param updatedAt The date when the flag was last updated.
 * @param reviewedAt The date when the flag was reviewed.
 * @param approvedAt The date when the flag was approved.
 * @param rejectedAt The date when the flag was rejected.
 */
@Immutable
public data class Flag(
    val user: User,
    val targetUser: User?,
    val targetMessageId: String,
    val reviewedBy: String,
    val createdByAutomod: Boolean,
    val createdAt: Date?,
    val updatedAt: Date,
    val reviewedAt: Date?,
    val approvedAt: Date?,
    val rejectedAt: Date?,
)
