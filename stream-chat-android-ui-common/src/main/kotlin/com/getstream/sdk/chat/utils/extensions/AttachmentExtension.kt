package com.getstream.sdk.chat.utils.extensions

import com.getstream.sdk.chat.model.GiphyInfo
import com.getstream.sdk.chat.utils.StorageHelper
import com.getstream.sdk.chat.utils.StringUtils
import io.getstream.chat.android.client.models.Attachment

private val GIPHY_INFO_DEAFULT_WIDTH = 200
private val GIPHY_INFO_DEAFULT_HEIGHT = 200

public fun Attachment.getDisplayableName(): String? {
    return StringUtils.removeTimePrefix(title ?: name ?: upload?.name, StorageHelper.TIME_FORMAT)
}

public val Attachment.imagePreviewUrl: String?
    get() = thumbUrl ?: imageUrl

public fun Attachment.giphyInfo(field: GiphyInfoType): GiphyInfo? {
    val giphyInfoMap = (extraData["giphy"] as Map<String, Any>?)?.get(field.value) as Map<String, String>?

    return giphyInfoMap?.let { map ->
        GiphyInfo(
            url = map["url"] ?: this.thumbUrl ?: "",
            width = map["width"]?.toInt() ?: GIPHY_INFO_DEAFULT_WIDTH,
            height = map["height"]?.toInt() ?: GIPHY_INFO_DEAFULT_HEIGHT,
            size = map["size"]?.toInt() ?: 0,
            frames = map["frames"]?.toInt() ?: 0
        )
    }
}

public enum class GiphyInfoType(public val value: String) {
    ORIGINAL("original")
}
