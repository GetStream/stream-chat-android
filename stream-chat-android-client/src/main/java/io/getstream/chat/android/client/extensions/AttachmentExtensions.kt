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

package io.getstream.chat.android.client.extensions

import io.getstream.chat.android.models.Attachment

internal const val ATTACHMENT_TYPE_IMAGE = "image"
internal const val ATTACHMENT_TYPE_FILE = "file"

/**
 * Key identifying the upload ID of the attachment.
 */
public const val EXTRA_UPLOAD_ID: String = "uploadId"

/**
 * Key identifying the duration of the attachment.
 */
public const val EXTRA_DURATION: String = "duration"

/**
 * Key identifying the waveform data of the attachment.
 */
public const val EXTRA_WAVEFORM_DATA: String = "waveform_data"

/**
 * Checks if the attachment is an image.
 */
internal val Attachment.isImage: Boolean
    get() = mimeType?.startsWith(ATTACHMENT_TYPE_IMAGE) ?: false

/**
 * Retrieves the upload ID of the attachment.
 */
public val Attachment.uploadId: String?
    get() = extraData[EXTRA_UPLOAD_ID] as String?

/**
 * Duration of the attachment in seconds.
 */
public val Attachment.duration: Float?
    get() = (extraData[EXTRA_DURATION] as? Number)?.toFloat()

/**
 * Duration of the attachment in milliseconds.
 */
public val Attachment.durationInMs: Int?
    get() = duration?.times(1000)?.toInt()

/**
 * Waveform data of the attachment.
 */
public val Attachment.waveformData: List<Float>?
    get() = extraData[EXTRA_WAVEFORM_DATA] as? List<Float>
