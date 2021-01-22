package io.getstream.chat.android.client.models

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.parser.IgnoreDeserialisation
import io.getstream.chat.android.client.parser.IgnoreSerialisation
import java.io.File

public data class Attachment(

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
    @SerializedName("title")
    var title: String? = null,
    @SerializedName("text")
    var text: String? = null,
    @SerializedName("type")
    var type: String? = null,
    @SerializedName("image")
    var image: String? = null,
    @SerializedName("url")
    var url: String? = null,
    @SerializedName("name")
    var name: String? = null,
    @SerializedName("fallback")
    var fallback: String? = null,

    @IgnoreSerialisation
    @IgnoreDeserialisation
    var upload: File? = null,

    @IgnoreSerialisation
    @IgnoreDeserialisation
    var uploadState: UploadState? = null,

    @IgnoreSerialisation
    @IgnoreDeserialisation
    override var extraData: MutableMap<String, Any> = mutableMapOf(),

) : CustomObject {

    public sealed class UploadState {
        public object InProgress : UploadState()
        public object Success : UploadState()
        public data class Failed(val error: ChatError) : UploadState()
    }
}
