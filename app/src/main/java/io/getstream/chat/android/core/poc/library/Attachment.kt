package io.getstream.chat.android.core.poc.library

import androidx.room.TypeConverters
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.core.poc.R
import io.getstream.chat.android.core.poc.library.api.ExtraDataConverter
import java.text.DecimalFormat


class Attachment {
    var config = Config() // Local file Attach Config
    @SerializedName("title")
    @Expose
    var title: String? = null
    @SerializedName("author_name")
    @Expose
    var author: String? = null
    @SerializedName("text")
    @Expose
    var text: String? = null
    @SerializedName("type")
    @Expose
    var type: String? = null
    @SerializedName("image")
    @Expose
    var image: String? = null
    @SerializedName("url")
    @Expose
    var url: String? = null
    @SerializedName("name")
    @Expose
    var name: String? = null
    @SerializedName("title_link")
    @Expose
    var titleLink: String? = null
    @SerializedName("thumb_url")
    @Expose
    var thumbURL: String? = null
    @SerializedName("fallback")
    @Expose
    var fallback: String? = null
    @SerializedName("image_url")
    @Expose
    var imageURL: String? = null
    @SerializedName("asset_url")
    @Expose
    var assetURL: String? = null
    @SerializedName("og_scrape_url")
    @Expose
    var ogURL: String? = null
    @SerializedName("mime_type")
    @Expose
    var mime_type: String? = null
    @SerializedName("file_size")
    @Expose
    var file_size = 0
    // Additional Params
    @TypeConverters(ExtraDataConverter::class)
    var extraData: HashMap<String, Any>? = null
        private set

    fun setExtraData(extraData: HashMap<String, Any>) {
        this.extraData = HashMap(extraData)
        this.extraData!!.remove("id")
    }

    val icon: Int
        get() {
            var fileTyineRes = 0
            when (mime_type) {
                ModelType.attach_mime_pdf -> fileTyineRes = R.drawable.stream_ic_file_pdf
                ModelType.attach_mime_csv -> fileTyineRes = R.drawable.stream_ic_file_csv
                ModelType.attach_mime_tar -> fileTyineRes = R.drawable.stream_ic_file_tar
                ModelType.attach_mime_zip -> fileTyineRes = R.drawable.stream_ic_file_zip
                ModelType.attach_mime_doc, ModelType.attach_mime_docx, ModelType.attach_mime_txt -> fileTyineRes =
                    R.drawable.stream_ic_file_doc
                ModelType.attach_mime_xlsx -> fileTyineRes = R.drawable.stream_ic_file_xls
                ModelType.attach_mime_ppt -> fileTyineRes = R.drawable.stream_ic_file_ppt
                ModelType.attach_mime_mov, ModelType.attach_mime_mp4 -> fileTyineRes =
                    R.drawable.stream_ic_file_mov
                ModelType.attach_mime_mp3 -> fileTyineRes = R.drawable.stream_ic_file_mp3
                else -> if (mime_type!!.contains("audio")) {
                    fileTyineRes = R.drawable.stream_ic_file_mp3
                } else if (mime_type!!.contains("video")) {
                    fileTyineRes = R.drawable.stream_ic_file_mov
                }
            }
            return fileTyineRes
        }

    val fileSizeHumanized: String
        get() {
            val size = file_size
            if (size <= 0) return "0"
            val units = arrayOf("B", "kB", "MB", "GB", "TB")
            val digitGroups =
                (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
            return DecimalFormat("#,##0.#").format(
                size / Math.pow(
                    1024.0,
                    digitGroups.toDouble()
                )
            ).toString() + " " + units[digitGroups]
        }

    inner class Config {
        var filePath: String? = null
        var isSelected = false
        var videoLengh = 0
        var isUploaded = false
        var progress = 0
    }
}
