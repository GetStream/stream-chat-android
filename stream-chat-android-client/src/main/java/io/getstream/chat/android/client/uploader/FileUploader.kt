package io.getstream.chat.android.client.uploader

import io.getstream.chat.android.client.utils.ProgressCallback
import java.io.File

/**
 * The FileUploader is responsible for sending and deleting files from given channel
 */
public interface FileUploader {

    /**
     * Uploads a file for the given channel. Progress can be accessed via [callback].
     *
     * @return The URL of the uploaded file, or null if the upload failed.
     */
    public fun sendFile(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        file: File,
        callback: ProgressCallback,
    ): String?

    /**
     * Uploads a file for the given channel.
     *
     * @return The URL of the uploaded file, or null if the upload failed.
     */
    public fun sendFile(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        file: File,
    ): String?

    /**
     * Uploads an image for the given channel. Progress can be accessed via [callback].
     *
     * @return The URL of the uploaded image, or null if the upload failed.
     */
    public fun sendImage(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        file: File,
        callback: ProgressCallback,
    ): String?

    /**
     * Uploads an image for the given channel.
     *
     * @return The URL of the uploaded image, or null if the upload failed.
     */
    public fun sendImage(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        file: File,
    ): String?

    /**
     * Deletes the file represented by [url] from the given channel.
     */
    public fun deleteFile(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        url: String,
    )

    /**
     * Deletes the image represented by [url] from the given channel.
     */
    public fun deleteImage(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        url: String,
    )
}
