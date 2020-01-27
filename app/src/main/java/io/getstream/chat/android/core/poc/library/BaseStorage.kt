package io.getstream.chat.android.core.poc.library

import java.io.File


abstract class BaseStorage  {

    abstract fun sendFile(
        channel: Channel,
        file: File,
        mimeType: String,
        apiKey:String,
        callback: UploadFileCallback<File, Int>
    )

    /**
     * Delete a file with a given URL.
     *
     * @param channel  the channel where needs to delete the file
     * @param url      the file URL
     * @param callback the result callback
     */
    abstract fun deleteFile(channel: Channel?, url: String?, callback: CompletableCallback?)

    /**
     * Delete a image with a given URL.
     *
     * @param channel  the channel where needs to delete the image
     * @param url      the image URL
     * @param callback the result callback
     */
    abstract fun deleteImage(channel: Channel?, url: String?, callback: CompletableCallback?)

    /**
     * signFileUrl allows you to add a token your file for authorization
     *
     * @param url
     * @return
     */
    abstract fun signFileUrl(url: String?): String?

    /**
     * signGlideUrl returns a GlidUrl for the given url string.
     * This allows you to add a token to either the headers or the query params
     *
     * @param url
     * @return
     */
    //TODO: remove glide dependency
    //abstract fun signGlideUrl(url: String?): GlideUrl?
}
