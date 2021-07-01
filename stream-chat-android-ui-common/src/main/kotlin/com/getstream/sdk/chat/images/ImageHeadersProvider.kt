package com.getstream.sdk.chat.images

import io.getstream.chat.android.core.internal.InternalStreamChatApi

public interface ImageHeadersProvider {
    public fun getImageRequestHeaders(): Map<String, String>
}

@InternalStreamChatApi
public object DefaultImageHeadersProvider : ImageHeadersProvider {
    override fun getImageRequestHeaders(): Map<String, String> = emptyMap()
}
