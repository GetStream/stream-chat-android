/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.DownstreamModerationDto
import io.getstream.chat.android.models.Moderation
import io.getstream.chat.android.models.ModerationAction

/**
 * Maps the network [DownstreamModerationDto] to the domain model [Moderation].
 */
internal fun DownstreamModerationDto.toDomain() = Moderation(
    action = ModerationAction.fromValue(this.action),
    originalText = this.original_text,
    textHarms = this.text_harms.orEmpty(),
    imageHarms = this.image_harms.orEmpty(),
    blocklistMatched = this.blocklist_matched,
    semanticFilterMatched = this.semantic_filter_matched,
    platformCircumvented = this.platform_circumvented ?: false,
)
