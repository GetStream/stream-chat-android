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

import com.squareup.moshi.JsonClass

/**
 * DB entity holding data for a message moderated by Moderation V2.
 */
@JsonClass(generateAdapter = true)
internal data class ModerationEntity(
    val action: String,
    val originalText: String,
    val textHarms: List<String>,
    val imageHarms: List<String>,
    val blocklistMatched: String?,
    val semanticFilterMatched: String?,
    val platformCircumvented: Boolean,
)
