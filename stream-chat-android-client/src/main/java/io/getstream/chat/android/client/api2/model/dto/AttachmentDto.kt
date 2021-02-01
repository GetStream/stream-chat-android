package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass

/**
 * See [io.getstream.chat.android.client.parser2.AttachmentDtoAdapter] for
 * special [extraData] handling.
 */
@JsonClass(generateAdapter = true)
internal data class AttachmentDto(
    val asset_url: String?,
    val author_name: String?,
    val fallback: String?,
    val file_size: Int,
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
    val url: String?,

    val extraData: Map<String, Any>,
)
