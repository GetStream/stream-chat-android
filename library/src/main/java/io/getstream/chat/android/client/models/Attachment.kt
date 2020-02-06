package io.getstream.chat.android.client.models

import com.google.gson.annotations.SerializedName


class Attachment {

    @SerializedName("author_name")
    var authorName: String? = null
    @SerializedName("title_link")
    var titleLink: String? = null
    @SerializedName("thumb_url")
    var thumbURL: String? = null
    @SerializedName("image_url")
    var imageURL: String? = null
    @SerializedName("asset_url")
    var assetURL: String? = null
    @SerializedName("og_scrape_url")
    var ogURL: String? = null
    @SerializedName("mime_type")
    var mime_type: String? = null
    @SerializedName("file_size")
    var fileSize = 0

    var title: String? = null
    var text: String? = null
    var type: String? = null
    var image: String? = null
    var url: String? = null
    var name: String? = null
    var fallback: String? = null

    var extraData = mutableMapOf<String, Any>()
}
