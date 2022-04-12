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
