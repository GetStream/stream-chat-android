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

package io.getstream.chat.android.ai.compose.util.internal

import android.net.Uri
import android.os.Build
import java.util.Locale

/**
 * A media type that says what a file is: [mimeType] without its parameters, lower-cased. Null when
 * there is none, or when it only says the type is unknown: `application/octet-stream`, or a
 * wildcard, whether of any type or of one family such as images. Some content providers report
 * octet-stream even for a JPEG.
 */
internal fun knownMimeType(mimeType: String?): String? {
    val type = mimeType?.substringBefore(';')?.trim()?.lowercase(Locale.ROOT)
    return type?.takeIf { it.isNotEmpty() && it !in UnknownMimeTypes && !it.endsWith("/*") }
}

private val UnknownMimeTypes = setOf("application/octet-stream", "binary/octet-stream", "application/unknown")

/** The extension of [name], else of the file [uri] ends in. */
internal fun pathExtension(name: String?, uri: Uri?): String? =
    listOfNotNull(name, fileName(uri))
        .map { it.substringAfterLast('.', missingDelimiterValue = "") }
        .firstOrNull { it.isNotEmpty() && !it.contains('/') }

/** The name of the file [uri] ends in, if it names one. */
internal fun fileName(uri: Uri?): String? =
    uri?.lastPathSegment?.takeIf { it.isNotEmpty() && it != "/" }

/**
 * Whether an image of this media type, else of this extension, is one the platform decodes on
 * [sdkInt]: JPEG, PNG, WebP, GIF and BMP everywhere, HEIF from Android 9, and AVIF from Android 12.
 * A media type that says what the file is decides on its own; one that only says the type is
 * unknown leaves it to the extension.
 */
internal fun isDecodableImage(
    mimeType: String?,
    extension: String?,
    sdkInt: Int = Build.VERSION.SDK_INT,
): Boolean {
    val type = knownMimeType(mimeType)
    val format = if (type != null) {
        DecodableMimeTypes[type]
    } else {
        extension?.lowercase(Locale.ROOT)?.let(DecodableExtensions::get)
    }
    return format != null && sdkInt >= format.minSdk
}

/** The image formats the platform decodes, and the Android version each needs. */
private enum class DecodableFormat(val minSdk: Int) {
    Everywhere(minSdk = 1),
    Heif(minSdk = Build.VERSION_CODES.P),
    Avif(minSdk = Build.VERSION_CODES.S),
}

private val DecodableMimeTypes = mapOf(
    "image/jpeg" to DecodableFormat.Everywhere,
    "image/jpg" to DecodableFormat.Everywhere,
    "image/pjpeg" to DecodableFormat.Everywhere,
    "image/png" to DecodableFormat.Everywhere,
    "image/webp" to DecodableFormat.Everywhere,
    "image/gif" to DecodableFormat.Everywhere,
    "image/bmp" to DecodableFormat.Everywhere,
    "image/x-ms-bmp" to DecodableFormat.Everywhere,
    "image/heif" to DecodableFormat.Heif,
    "image/heic" to DecodableFormat.Heif,
    "image/avif" to DecodableFormat.Avif,
)

private val DecodableExtensions = mapOf(
    "jpg" to DecodableFormat.Everywhere,
    "jpeg" to DecodableFormat.Everywhere,
    "png" to DecodableFormat.Everywhere,
    "webp" to DecodableFormat.Everywhere,
    "gif" to DecodableFormat.Everywhere,
    "bmp" to DecodableFormat.Everywhere,
    "heif" to DecodableFormat.Heif,
    "heic" to DecodableFormat.Heif,
    "avif" to DecodableFormat.Avif,
)
