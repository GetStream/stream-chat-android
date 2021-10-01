package io.getstream.chat.android.client.utils

import io.getstream.chat.android.client.errors.ChatError

/**
 * Callback to listen for file upload status.
 */
public interface ProgressCallback {

    /**
     * Called when the attachment is uploaded successfully with an [url].
     */
    public fun onSuccess(url: String?)

    /**
     * Called when the attachment could not be uploaded due to cancellation, network problem or timeout etc with an [error].
     *
     * @see ChatError
     */
    public fun onError(error: ChatError)

    /**
     * Called when the attachment upload is in progress with [progress] in percentage.
     */
    @Deprecated(
        message = "onProgress with percentage progress is deprecated. Use onProgress(bytesUploaded: Long, totalLength: Long) instead.",
        level = DeprecationLevel.WARNING
    )
    public fun onProgress(progress: Long)

    /**
     * Called when the attachment upload is in progress with [bytesUploaded] count
     * and [totalLength] in bytes of the file.
     */
    public fun onProgress(bytesUploaded: Long, totalLength: Long) {
        onProgress((100 * bytesUploaded) / totalLength)
    }
}
