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

package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.core.internal.StreamHandsOff

/**
 * See [io.getstream.chat.android.client.parser2.adapters.AttachmentDtoAdapter] for
 * special [extraData] handling.
 */
@StreamHandsOff(
    reason = "Field names can't be changed because [CustomObjectDtoAdapter] class uses reflections to add/remove " +
        "content of [extraData] map",
)
@JsonClass(generateAdapter = true)
internal data class AttachmentDto(
    val asset_url: String?,
    val author_name: String?,
    val author_link: String?,
    val fallback: String?,
    val file_size: Int = 0,
    val image: String?,
    val image_url: String?,
    val mime_type: String?,
    val name: String?,
    val og_scrape_url: String?,
    val text: String?,
    val thumb_url: String?,
    val title: String?,
    val title_link: String?,
    val type: String?,
    val original_height: Int?,
    val original_width: Int?,

    val extraData: Map<String, Any>,
) : ExtraDataDto
