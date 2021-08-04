package io.getstream.chat.android.client.parser2.testdata

import io.getstream.chat.android.client.api2.model.dto.AttachmentDto
import org.intellij.lang.annotations.Language

internal object AttachmentDtoTestData {

    @Language("JSON")
    val json =
        """{
          "asset_url": "assetUrl",
          "author_name": "authorName",
          "author_link": "authorLink",
          "fallback": "fallback",
          "file_size": 1,
          "image": "image",
          "image_url": "imageUrl",
          "mime_type": "mimeType",
          "name": "name",
          "og_scrape_url": "ogScrapeUrl",
          "text": "text",
          "thumb_url": "thumbUrl",
          "title": "title",
          "title_link": "titleLink",
          "type": "type",
          "url": "url",
          "draft": true
        }
        """.withoutWhitespace()
    val attachment = AttachmentDto(
        asset_url = "assetUrl",
        author_name = "authorName",
        author_link = "authorLink",
        fallback = "fallback",
        file_size = 1,
        image = "image",
        image_url = "imageUrl",
        mime_type = "mimeType",
        name = "name",
        og_scrape_url = "ogScrapeUrl",
        text = "text",
        thumb_url = "thumbUrl",
        title = "title",
        title_link = "titleLink",
        type = "type",
        url = "url",
        extraData = mapOf("draft" to true),
    )

    @Language("JSON")
    val jsonWithoutExtraData =
        """{
          "file_size": 0
        }
        """.withoutWhitespace()
    val attachmentWithoutExtraData = AttachmentDto(
        asset_url = null,
        author_name = null,
        author_link = null,
        fallback = null,
        file_size = 0,
        image = null,
        image_url = null,
        mime_type = null,
        name = null,
        og_scrape_url = null,
        text = null,
        thumb_url = null,
        title = null,
        title_link = null,
        type = null,
        url = null,
        extraData = emptyMap(),
    )
}
