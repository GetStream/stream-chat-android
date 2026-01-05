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

package io.getstream.chat.android.client.api2.model.response

import com.squareup.moshi.JsonClass
import java.util.Date

/**
 * Model representing the response from blocking a user.
 *
 * @param blocked_by_user_id The ID of the user who blocked the other user.
 * @param blocked_user_id The ID of the user who was blocked.
 * @param created_at The date when the block was created.
 */
@JsonClass(generateAdapter = true)
internal data class BlockUserResponse(
    val blocked_by_user_id: String,
    val blocked_user_id: String,
    val created_at: Date,
)
