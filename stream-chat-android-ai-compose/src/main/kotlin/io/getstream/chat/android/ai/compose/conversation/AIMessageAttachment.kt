/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ai.compose.conversation

import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Something a message carries besides its text, such as an image or a file.
 *
 * @param kind What kind of attachment it is.
 * @param uri Where it is: on the web, or on this device, such as a `content://` URI the person
 * picked.
 * @param bitmap The image itself when the app holds it, such as a photo the person just took. It
 * shows rather than the one at [uri]. Pass a small one, the size it shows at: it stays in memory
 * while the conversation shows it.
 * @param name Its name, such as the file's.
 * @param size Its size, in bytes.
 * @param mimeType Its media type, such as `application/pdf`. Without it, the type comes from the
 * extension of its name or URI.
 * @param extraData Data of its own, such as what an attachment of a kind of your own needs.
 */
public class AIMessageAttachment(
    public val kind: Kind,
    public val uri: Uri? = null,
    public val bitmap: ImageBitmap? = null,
    public val name: String? = null,
    public val size: Long? = null,
    public val mimeType: String? = null,
    public val extraData: Map<String, Any?> = emptyMap(),
) {

    override fun equals(other: Any?): Boolean = this === other || (
        other is AIMessageAttachment &&
            kind == other.kind &&
            uri == other.uri &&
            bitmap == other.bitmap &&
            name == other.name &&
            size == other.size &&
            mimeType == other.mimeType &&
            extraData == other.extraData
        )

    override fun hashCode(): Int {
        var result = kind.hashCode()
        result = 31 * result + (uri?.hashCode() ?: 0)
        result = 31 * result + (bitmap?.hashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (size?.hashCode() ?: 0)
        result = 31 * result + (mimeType?.hashCode() ?: 0)
        result = 31 * result + extraData.hashCode()
        return result
    }

    override fun toString(): String = "AIMessageAttachment(kind=${kind.rawValue}, uri=$uri, name=$name)"

    /**
     * What kind of attachment it is. It is an open set: compare against the kinds you know and
     * give the rest a fallback.
     *
     * @param rawValue The kind's name, such as `image`.
     */
    @JvmInline
    public value class Kind(public val rawValue: String) {
        override fun toString(): String = rawValue

        public companion object {
            /** An image, shown as a thumbnail. */
            public val Image: Kind = Kind("image")

            /** A file, such as a PDF, shown with its name, type and size. */
            public val File: Kind = Kind("file")
        }
    }

    public companion object {
        /** An image to load, from the web or this device. */
        public fun image(uri: Uri, name: String? = null): AIMessageAttachment =
            AIMessageAttachment(kind = Kind.Image, uri = uri, name = name)

        /**
         * An image the app holds, such as a photo the person just took. Pass a small one, the size
         * it shows at: it stays in memory while the conversation shows it.
         */
        public fun image(bitmap: ImageBitmap, name: String? = null): AIMessageAttachment =
            AIMessageAttachment(kind = Kind.Image, bitmap = bitmap, name = name)

        /** A file to open, from the web or this device. */
        public fun file(
            uri: Uri,
            name: String? = null,
            size: Long? = null,
            mimeType: String? = null,
        ): AIMessageAttachment =
            AIMessageAttachment(kind = Kind.File, uri = uri, name = name, size = size, mimeType = mimeType)
    }
}
