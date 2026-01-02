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

package io.getstream.chat.android.state.extensions.internal

import io.getstream.chat.android.models.Attachment

/**
 * Uses substrings to extract the file name from the attachment URL.
 *
 * Useful in situations where no attachment name or title has been provided and
 * we need a name in order to download the file to storage.
 */
internal fun Attachment.parseAttachmentNameFromUrl(): String? {
    val url = when (this.type) {
        "image" -> this.imageUrl ?: this.assetUrl ?: this.thumbUrl
        else -> this.assetUrl ?: this.imageUrl ?: this.thumbUrl
    }

    return url?.substringAfterLast(
        delimiter = "/",
        missingDelimiterValue = "",
    )?.takeIf { it.isNotBlank() }
        ?.substringBefore("?")
}
