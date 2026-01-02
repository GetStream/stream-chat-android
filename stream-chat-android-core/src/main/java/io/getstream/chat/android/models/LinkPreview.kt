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

package io.getstream.chat.android.models

import androidx.compose.runtime.Immutable

/**
 * A data class that represents a link preview.
 *
 * @property originUrl The original URL of the link.
 * @property attachment The attachment that represents the link preview.
 */
@Immutable
public data class LinkPreview(
    val originUrl: String,
    val attachment: Attachment,
) {

    public companion object {
        /**
         * An empty [LinkPreview].
         */
        public val EMPTY: LinkPreview = LinkPreview(originUrl = "", attachment = Attachment())
    }
}
