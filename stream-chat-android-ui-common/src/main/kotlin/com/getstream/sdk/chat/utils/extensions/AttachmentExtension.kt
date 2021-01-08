package com.getstream.sdk.chat.utils.extensions

import com.getstream.sdk.chat.utils.StorageHelper
import com.getstream.sdk.chat.utils.StringUtils
import io.getstream.chat.android.client.models.Attachment

public fun Attachment.getDisplayableName(): String? {
    return StringUtils.removeTimePrefix(title ?: name, StorageHelper.TIME_FORMAT)
}
