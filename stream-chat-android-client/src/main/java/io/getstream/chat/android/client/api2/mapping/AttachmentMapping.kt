package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.AttachmentDto
import io.getstream.chat.android.client.models.Attachment

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
        type = type,
        url = url,
        extraData = extraData,
    )

internal fun AttachmentDto.toDomain(): Attachment =
    Attachment(
        assetUrl = asset_url,
        authorName = author_name,
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
        extraData = extraData.toMutableMap(),
    )
