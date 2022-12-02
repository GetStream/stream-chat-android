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

package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.AttachmentDto
import io.getstream.chat.android.models.Attachment

internal fun Attachment.toDto(): AttachmentDto =
    AttachmentDto(
        asset_url = assetUrl,
        author_name = authorName,
        fallback = fallback,
        file_size = fileSize,
        image = image,
        image_url = imageUrl,
        mime_type = mimeType,
        name = name,
        og_scrape_url = ogUrl,
        text = text,
        thumb_url = thumbUrl,
        title = title,
        title_link = titleLink,
        author_link = authorLink,
        type = type,
        url = url,
        original_height = originalHeight,
        original_width = originalWidth,
        extraData = extraData,
    )

internal fun AttachmentDto.toDomain(): Attachment =
    Attachment(
        assetUrl = asset_url,
        authorName = author_name,
        authorLink = author_link,
        fallback = fallback,
        fileSize = file_size,
        image = image,
        imageUrl = image_url,
        mimeType = mime_type,
        name = name,
        ogUrl = og_scrape_url,
        text = text,
        thumbUrl = thumb_url,
        title = title,
        titleLink = title_link,
        type = type,
        url = url,
        originalHeight = original_height,
        originalWidth = original_width,
        extraData = extraData.toMutableMap(),
    )
