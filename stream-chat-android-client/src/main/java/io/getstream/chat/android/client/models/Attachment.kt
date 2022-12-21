/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.models

import io.getstream.chat.android.client.errors.ChatError
import java.io.File

/**
 * Represents an attachment. Most commonly these are files, images,
 *  videos and audio recordings, but the class is flexible enough
 *  that it can represent other things as well such as a date,
 *  a given location or other things.
 *
 *  If you want to create a custom attachment we suggest you
 *   use [extraData] to store the information you need.
 *
 * @param authorName The name of the site the URL leads to.
 * @param authorLink The link to the website.
 * @param titleLink The link to the URL or the resource linked.
 * @param thumbUrl The URL for the thumbnail version of the attachment,
 *  given the attachment has a visual quality, e.g. is a video, an image,
 *  a link to a website or similar.
 * @param imageUrl The URL for the raw version of the attachment.
 * @param assetUrl The URL for the asset.
 * @param ogUrl The original link that was enriched.
 * @param mimeType The mime type of the given attachment. e.g. "image/jpeg"
 * @param fileSize The size of the given attachment.
 * @param title The title of the attachment.
 * @param text The page description.
 * @param type The type of the attachment. e.g "file", "image, "audio".
 * @param image The image attachment.
 * @param fallback Alternative description in the case of an image attachment
 * (img alt in HTML).
 * @param originalHeight The original height of the attachment.
 * Provided if the attachment is of type "image".
 * @param originalWidth The original width of the attachment.
 * Provided if the attachment is of type "image".
 * @param upload The local file that will be uploaded when the attachment
 * is sent.
 * @param uploadState The state of the upload, i.e. the current progress
 * of uploading the file.
 * @param extraData Stores various extra information that can be sent
 * when uploading the attachment or read when downloading it.
 */
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
    var originalHeight: Int? = null,
    var originalWidth: Int? = null,

    /**
     * The local file to upload when the attachment is sent. The [url] property
     * will be populated with the URL of the uploaded file when done.
     *
     * Leaving this property empty means that there is no file to upload for
     * this attachment.
     */
    var upload: File? = null,

    var uploadState: UploadState? = null,

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
        public data class InProgress(val bytesUploaded: Long, val totalBytes: Long) : UploadState()

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
