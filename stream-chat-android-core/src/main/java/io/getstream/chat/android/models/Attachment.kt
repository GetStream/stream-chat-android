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

package io.getstream.chat.android.models

import androidx.compose.runtime.Immutable
import io.getstream.result.Error
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
 * @param name The attachment name.
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
@Immutable
public data class Attachment(
    val authorName: String? = null,
    val authorLink: String? = null,
    val titleLink: String? = null,
    val thumbUrl: String? = null,
    val imageUrl: String? = null,
    val assetUrl: String? = null,
    val ogUrl: String? = null,
    val mimeType: String? = null,
    val fileSize: Int = 0,
    val title: String? = null,
    val text: String? = null,
    val type: String? = null,
    val image: String? = null,
    val name: String? = null,
    val fallback: String? = null,
    val originalHeight: Int? = null,
    val originalWidth: Int? = null,

    /**
     * The local file to upload when the attachment is sent. The [assetUrl] property
     * will be populated with the URL of the uploaded file when done.
     *
     * Leaving this property empty means that there is no file to upload for
     * this attachment.
     */
    val upload: File? = null,

    val uploadState: UploadState? = null,

    override val extraData: Map<String, Any> = mapOf(),

) : CustomObject {

    /**
     * Represents various states in attachment upload lifecycle.
     */
    public sealed class UploadState {
        /**
         * Idle state before attachment starts to upload.
         */
        public object Idle : UploadState() {
            override fun toString(): String = "Idle"
        }

        /**
         * State representing attachment upload progress.
         */
        public data class InProgress(val bytesUploaded: Long, val totalBytes: Long) : UploadState()

        /**
         * State indicating that the attachment was uploaded successfully
         */
        public object Success : UploadState() {
            override fun toString(): String = "Success"
        }

        /**
         * State indicating that the attachment upload failed.
         */
        public data class Failed(val error: Error) : UploadState()
    }

    override fun toString(): String = StringBuilder().apply {
        append("Attachment(")
        append("mimeType=\"").append(mimeType).append("\"")
        if (authorName != null) append(", authorName=").append(authorName)
        if (authorLink != null) append(", authorLink=").append(authorLink)
        if (titleLink != null) append(", titleLink=").append(titleLink)
        if (thumbUrl != null) append(", thumbUrl=").append(thumbUrl.shorten())
        if (imageUrl != null) append(", imageUrl=").append(imageUrl.shorten())
        if (assetUrl != null) append(", assetUrl=").append(assetUrl.shorten())
        if (ogUrl != null) append(", ogUrl=").append(ogUrl.hashCode())
        if (fileSize > 0) append(", fileSize=").append(fileSize)
        if (title != null) append(", title=\"").append(title).append("\"")
        if (text != null) append(", text=\"").append(text).append("\"")
        if (type != null) append(", type=\"").append(type).append("\"")
        if (image != null) append(", image=").append(image)
        if (name != null) append(", name=").append(name)
        if (fallback != null) append(", fallback=").append(fallback)
        if (originalHeight != null) append(", origH=").append(originalHeight)
        if (originalWidth != null) append(", origW=").append(originalWidth)
        if (upload != null) append(", upload=\"").append(upload).append("\"")
        if (uploadState != null) append(", uploadState=").append(uploadState)
        if (extraData.isNotEmpty()) append(", extraData=").append(extraData)
        append(")")
    }.toString()

    private fun String.shorten(): String {
        val min = 0
        val max = 9
        if (length <= max) return this
        return substring(min..max).let { "$it..." }
    }

    @SinceKotlin("99999.9")
    @Suppress("NEWER_VERSION_IN_SINCE_KOTLIN")
    public fun newBuilder(): Builder = Builder(this)

    @Suppress("TooManyFunctions")
    public class Builder() {
        private var authorName: String? = null
        private var authorLink: String? = null
        private var titleLink: String? = null
        private var thumbUrl: String? = null
        private var imageUrl: String? = null
        private var assetUrl: String? = null
        private var ogUrl: String? = null
        private var mimeType: String? = null
        private var fileSize: Int = 0
        private var title: String? = null
        private var text: String? = null
        private var type: String? = null
        private var image: String? = null
        private var name: String? = null
        private var fallback: String? = null
        private var originalHeight: Int? = null
        private var originalWidth: Int? = null
        private var upload: File? = null
        private var uploadState: UploadState? = null
        private var extraData: Map<String, Any> = mapOf()

        public constructor(attachment: Attachment) : this() {
            authorName = attachment.authorName
            authorLink = attachment.authorLink
            titleLink = attachment.titleLink
            thumbUrl = attachment.thumbUrl
            imageUrl = attachment.imageUrl
            assetUrl = attachment.assetUrl
            ogUrl = attachment.ogUrl
            mimeType = attachment.mimeType
            fileSize = attachment.fileSize
            title = attachment.title
            text = attachment.text
            type = attachment.type
            image = attachment.image
            name = attachment.name
            fallback = attachment.fallback
            originalHeight = attachment.originalHeight
            originalWidth = attachment.originalWidth
            upload = attachment.upload
            uploadState = attachment.uploadState
            extraData = attachment.extraData
        }
        public fun withAuthorName(authorName: String?): Builder = apply { this.authorName = authorName }
        public fun withAuthorLink(authorLink: String?): Builder = apply { this.authorLink = authorLink }
        public fun withTitleLink(titleLink: String?): Builder = apply { this.titleLink = titleLink }
        public fun withThumbUrl(thumbUrl: String?): Builder = apply { this.thumbUrl = thumbUrl }
        public fun withImageUrl(imageUrl: String?): Builder = apply { this.imageUrl = imageUrl }
        public fun withAssetUrl(assetUrl: String?): Builder = apply { this.assetUrl = assetUrl }
        public fun withOgUrl(ogUrl: String?): Builder = apply { this.ogUrl = ogUrl }
        public fun withMimeType(mimeType: String?): Builder = apply { this.mimeType = mimeType }
        public fun withFileSize(fileSize: Int): Builder = apply { this.fileSize = fileSize }
        public fun withTitle(title: String?): Builder = apply { this.title = title }
        public fun withText(text: String?): Builder = apply { this.text = text }
        public fun withType(type: String?): Builder = apply { this.type = type }
        public fun withImage(image: String?): Builder = apply { this.image = image }
        public fun withName(name: String?): Builder = apply { this.name = name }
        public fun withFallback(fallback: String?): Builder = apply { this.fallback = fallback }
        public fun withOriginalHeight(originalHeight: Int?): Builder = apply { this.originalHeight = originalHeight }
        public fun withOriginalWidth(originalWidth: Int?): Builder = apply { this.originalWidth = originalWidth }
        public fun withUpload(upload: File?): Builder = apply { this.upload = upload }
        public fun withUploadState(uploadState: UploadState?): Builder = apply { this.uploadState = uploadState }
        public fun withExtraData(extraData: Map<String, Any>): Builder = apply { this.extraData = extraData }

        public fun build(): Attachment = Attachment(
            authorName = authorName,
            authorLink = authorLink,
            titleLink = titleLink,
            thumbUrl = thumbUrl,
            imageUrl = imageUrl,
            assetUrl = assetUrl,
            ogUrl = ogUrl,
            mimeType = mimeType,
            fileSize = fileSize,
            title = title,
            text = text,
            type = type,
            image = image,
            name = name,
            fallback = fallback,
            originalHeight = originalHeight,
            originalWidth = originalWidth,
            upload = upload,
            uploadState = uploadState,
            extraData = extraData,
        )
    }
}
