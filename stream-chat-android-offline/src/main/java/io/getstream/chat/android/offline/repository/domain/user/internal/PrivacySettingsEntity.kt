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

package io.getstream.chat.android.offline.repository.domain.user.internal

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class PrivacySettingsEntity(
    val typingIndicators: TypingIndicatorsEntity? = null,
    val readReceipts: ReadReceiptsEntity? = null,
)

@JsonClass(generateAdapter = true)
internal data class TypingIndicatorsEntity(
    val enabled: Boolean,
)

@JsonClass(generateAdapter = true)
internal data class ReadReceiptsEntity(
    val enabled: Boolean,
)
