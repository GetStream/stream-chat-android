package io.getstream.chat.android.client.utils

import io.getstream.chat.android.client.errors.ChatError

/**
 * Callback to listen for attachment's upload status.
 */
public interface ProgressCallback {

    /**
     * Called when the attachment is uploaded successfully.
     *
     * @param attachmentUrl url of the uploaded attachment.
     */
    public fun onSuccess(attachmentUrl: String?)

    /**
     * Called when the attachment could not be uploaded due to cancellation, network problem or timeout etc.
     *
     * @param error payload with information of failure.
     *
     * @see ChatError
     */
    public fun onError(error: ChatError)

    /**
     * Called when the attachment upload is in progress.
     *
     * @param progress value between 0 - 100 (percentage of total attachment size)
     */
    public fun onProgress(progress: Long)
}
