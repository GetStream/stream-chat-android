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

package io.getstream.chat.android.ui.common.feature.messages.composer.internal

import android.os.Parcelable
import io.getstream.chat.android.models.Attachment
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.io.File

/**
 * A Parcelable subset of [Attachment] containing only the fields that are populated
 * at compose time (before upload). Fields like `imageUrl`, `thumbUrl`, `assetUrl`,
 * `uploadState`, etc. are populated after upload and are intentionally excluded.
 */
@Parcelize
internal data class ParcelableAttachment(
    val uploadPath: String?,
    val type: String?,
    val name: String?,
    val fileSize: Int,
    val mimeType: String?,
    val title: String?,
    val extraData: Map<String, @RawValue Any>,
) : Parcelable

/**
 * Converts an [Attachment] to a [ParcelableAttachment] for persistence via [SavedStateHandle].
 */
internal fun Attachment.toParcelable(): ParcelableAttachment = ParcelableAttachment(
    uploadPath = upload?.absolutePath,
    type = type,
    name = name,
    fileSize = fileSize,
    mimeType = mimeType,
    title = title,
    extraData = extraData,
)

/**
 * Converts a [ParcelableAttachment] back to an [Attachment].
 */
internal fun ParcelableAttachment.toAttachment(): Attachment = Attachment(
    upload = uploadPath?.let { File(it) },
    type = type,
    name = name,
    fileSize = fileSize,
    mimeType = mimeType,
    title = title,
    extraData = extraData,
)

/**
 * Checks whether all extra data values in the given attachments are safe to parcel.
 * Returns `true` if all values can be written to a [android.os.Parcel] without crashing.
 */
internal fun List<Attachment>.areExtraDataParcelSafe(): Boolean =
    all { attachment -> attachment.extraData.values.all { it.isParcelSafe() } }

/**
 * Recursively checks whether a value can be safely written via [android.os.Parcel.writeValue].
 */
private fun Any.isParcelSafe(): Boolean = when (this) {
    is String, is Int, is Long, is Float, is Double, is Boolean, is Byte, is Short -> true
    is BooleanArray, is ByteArray, is FloatArray, is IntArray, is LongArray, is DoubleArray -> true
    is List<*> -> all { it == null || it.isParcelSafe() }
    is Map<*, *> -> all { (k, v) -> k is String && (v == null || v.isParcelSafe()) }
    else -> false
}
