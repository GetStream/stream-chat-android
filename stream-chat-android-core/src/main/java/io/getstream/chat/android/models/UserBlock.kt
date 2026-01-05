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

package io.getstream.chat.android.models

import java.util.Date

/**
 * Represents a user block.
 *
 * @param blockedBy who is the initiator of the block (usually will contain the ID of the current user)
 * @param userId the ID of the blocked user.
 * @param blockedAt when the action happened
 */
public data class UserBlock(
    val blockedBy: String,
    val userId: String,
    val blockedAt: Date,
)
