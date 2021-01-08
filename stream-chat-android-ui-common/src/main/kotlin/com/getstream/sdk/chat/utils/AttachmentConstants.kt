package com.getstream.sdk.chat.utils

import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public object AttachmentConstants {
    public const val MAX_UPLOAD_FILE_SIZE: Long = 1024 * 1024 * 20 // 20 MB
}
