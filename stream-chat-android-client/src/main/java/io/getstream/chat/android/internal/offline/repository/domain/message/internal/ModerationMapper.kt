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

package io.getstream.chat.android.internal.offline.repository.domain.message.internal

import io.getstream.chat.android.models.Moderation
import io.getstream.chat.android.models.ModerationAction

/**
 * Maps the domain model [Moderation] to the database model [ModerationEntity].
 */
internal fun Moderation.toEntity() = ModerationEntity(
    action = action.value,
    originalText = originalText,
    textHarms = textHarms,
    imageHarms = imageHarms,
    blocklistMatched = blocklistMatched,
    semanticFilterMatched = semanticFilterMatched,
    platformCircumvented = platformCircumvented,
)

/**
 * Maps the database model [ModerationEntity] to the domain model [Moderation].
 */
internal fun ModerationEntity.toDomain() = Moderation(
    action = ModerationAction.fromValue(action),
    originalText = originalText,
    textHarms = textHarms,
    imageHarms = imageHarms,
    blocklistMatched = blocklistMatched,
    semanticFilterMatched = semanticFilterMatched,
    platformCircumvented = platformCircumvented,
)
