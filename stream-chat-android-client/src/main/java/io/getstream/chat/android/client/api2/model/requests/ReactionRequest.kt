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

package io.getstream.chat.android.client.api2.model.requests

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.api2.model.dto.UpstreamReactionDto

/**
 * Request for adding a reaction to a message.
 *
 * @property reaction The reaction to be added.
 * @property enforce_unique If true, the reaction will be unique per user per message.
 * @property skip_push If true, the reaction addition will not trigger a push notification.
 */
@JsonClass(generateAdapter = true)
internal data class ReactionRequest(
    val reaction: UpstreamReactionDto,
    val enforce_unique: Boolean,
    val skip_push: Boolean,
)
