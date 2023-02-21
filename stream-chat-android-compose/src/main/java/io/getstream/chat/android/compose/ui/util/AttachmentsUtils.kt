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

package io.getstream.chat.android.compose.ui.util

import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.models.Attachment

/**
 * Returns a string representation for the given attachment.
 */
public val Attachment.previewText: String
    get() = title ?: name ?: fallback ?: ""

/**
 * Attachment types that represent media content.
 */
private val MEDIA_ATTACHMENT_TYPES: Collection<String> = listOf(ModelType.attach_image, ModelType.attach_giphy)

/**
 * @return If the [Attachment] is media content or not.
 */
internal fun Attachment.isMedia(): Boolean = type in MEDIA_ATTACHMENT_TYPES
