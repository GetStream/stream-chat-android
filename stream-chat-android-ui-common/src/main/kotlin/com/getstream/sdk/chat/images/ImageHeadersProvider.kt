package com.getstream.sdk.chat.images

/**
 * Provides HTTP headers for image loading requests
 */
public interface ImageHeadersProvider {
    public fun getImageRequestHeaders(): Map<String, String>
}

internal object DefaultImageHeadersProvider : ImageHeadersProvider {
    override fun getImageRequestHeaders(): Map<String, String> = emptyMap()
}
