package io.getstream.chat.android.client.models

import io.getstream.chat.android.client.errors.ChatError
import java.io.File

public data class Attachment(

    var authorName: String? = null,
    var authorLink: String? = null,
    var titleLink: String? = null,
    var thumbUrl: String? = null,
    var imageUrl: String? = null,
    var assetUrl: String? = null,
    var ogUrl: String? = null,
    var mimeType: String? = null,
    var fileSize: Int = 0,
    var title: String? = null,
    var text: String? = null,
    var type: String? = null,
    var image: String? = null,
    var url: String? = null,
    var name: String? = null,
    var fallback: String? = null,

    /**
     * The local file to upload when the attachment is sent. The [url] property
     * will be populated with the URL of the uploaded file when done.
     *
     * Leaving this property empty means that there is no file to upload for
     * this attachment.
     */
    @Transient
    var upload: File? = null,

    @Transient
    var uploadState: UploadState? = null,

    @Transient
    override var extraData: MutableMap<String, Any> = mutableMapOf(),

) : CustomObject {

    /**
     * Represents various states in attachment upload lifecycle.
     */
    public sealed class UploadState {
        /**
         * Idle state before attachment starts to upload.
         */
        public object Idle : UploadState()

        /**
         * State representing attachment upload progress.
         */
        public data class InProgress(val bytesRead: Long, val totalBytes: Long) : UploadState()

        /**
         * State indicating that the attachment was uploaded successfully
         */
        public object Success : UploadState()

        /**
         * State indicating that the attachment upload failed.
         */
        public data class Failed(val error: ChatError) : UploadState()
    }
}
