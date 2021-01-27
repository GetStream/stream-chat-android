package io.getstream.chat.android.client.uploader

import io.getstream.chat.android.client.utils.ProgressCallback
import java.io.File

/**
 * The FileUploader is responsible for sending and deleting files from given channel
 */
public interface FileUploader {

    /**
     * Sends file to given channel.
     * Progress can be accessed via [callback]
     */
    public fun sendFile(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        file: File,
        callback: ProgressCallback
    ): String?

    /**
     * Sends file to given channel.
     * @return File URL when image is sent successfully, null if sending image failed
     */
    public fun sendFile(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        file: File
    ): String?

    /**
     * Sends image to given channel.
     * Progress can be accessed via [callback]
     */
    public fun sendImage(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        file: File,
        callback: ProgressCallback
    )

    /**
     * Sends image to given channel
     * @return Image URL when image is sent successfully, null if sending image failed
     */
    public fun sendImage(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        file: File
    ): String?

    /**
     * Deletes file represented by [url] from given channel
     */
    public fun deleteFile(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        url: String
    )

    /**
     * Deletes image represented by [url] from given channel
     */
    public fun deleteImage(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        url: String
    )
}
