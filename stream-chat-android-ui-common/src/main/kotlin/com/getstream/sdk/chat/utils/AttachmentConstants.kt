package com.getstream.sdk.chat.utils

import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public object AttachmentConstants {
    public const val MB_IN_BYTES: Long = 1024 * 1024
    public const val MAX_UPLOAD_FILE_SIZE: Long = MB_IN_BYTES * 20
}
