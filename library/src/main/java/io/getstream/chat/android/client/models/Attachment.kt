package io.getstream.chat.android.client.models

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.api.models.CustomObject
import io.getstream.chat.android.client.parser.IgnoreDeserialisation
import io.getstream.chat.android.client.parser.IgnoreSerialisation


data class Attachment(

    @SerializedName("author_name")
    var authorName: String? = null,
    @SerializedName("title_link")
    var titleLink: String? = null,
    @SerializedName("thumb_url")
    var thumbUrl: String? = null,
    @SerializedName("image_url")
    var imageUrl: String? = null,
    @SerializedName("asset_url")
    var assetUrl: String? = null,
    @SerializedName("og_scrape_url")
    var ogUrl: String? = null,
    @SerializedName("mime_type")
    var mimeType: String? = null,
    @SerializedName("file_size")
    var fileSize: Int = 0,
    var title: String? = null,
    var text: String? = null,
    var type: String? = null,
    var image: String? = null,
    var url: String? = null,
    var name: String? = null,
    var fallback: String? = null

) : CustomObject {

    @IgnoreSerialisation
    @IgnoreDeserialisation
    override var extraData = mutableMapOf<String, Any>()
}
