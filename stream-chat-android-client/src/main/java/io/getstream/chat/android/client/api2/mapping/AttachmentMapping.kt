package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.AttachmentDto
import io.getstream.chat.android.client.models.Attachment

internal fun Attachment.toDto(): AttachmentDto =
    AttachmentDto(
        author_name = authorName,
        title_link = titleLink,
        thumb_url = thumbUrl,
        image_url = imageUrl,
        asset_url = assetUrl,
        og_scrape_url = ogUrl,
        mime_type = mimeType,
        file_size = fileSize,
        title = title,
        text = text,
        type = type,
        image = image,
        url = url,
        name = name,
        fallback = fallback,
        extraData = extraData,
    )

internal fun AttachmentDto.toDomain(): Attachment =
    Attachment(
        authorName = author_name,
        titleLink = title_link,
        thumbUrl = thumb_url,
        imageUrl = image_url,
        assetUrl = asset_url,
        ogUrl = og_scrape_url,
        mimeType = mime_type,
        fileSize = file_size,
        title = title,
        text = text,
        type = type,
        image = image,
        url = url,
        name = name,
        fallback = fallback,
        extraData = extraData.toMutableMap(),
    )
