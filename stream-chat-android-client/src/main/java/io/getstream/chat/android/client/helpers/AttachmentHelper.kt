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

package io.getstream.chat.android.client.helpers

import io.getstream.chat.android.client.utils.TimeProvider
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Attachment
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

@InternalStreamChatApi
public class AttachmentHelper(private val timeProvider: TimeProvider = TimeProvider) {

    @Suppress("ReturnCount")
    public fun hasValidImageUrl(attachment: Attachment): Boolean {
        val url = attachment.imageUrl?.toHttpUrlOrNull() ?: return false
        if (url.queryParameterNames.contains(QUERY_KEY_NAME_EXPIRES).not()) {
            return true
        }
        val timestamp = url.queryParameter(QUERY_KEY_NAME_EXPIRES)?.toLongOrNull() ?: return false
        return timestamp > timeProvider.provideCurrentTimeInSeconds()
    }

    public fun hasStreamImageUrl(attachment: Attachment): Boolean {
        return attachment.imageUrl?.toHttpUrlOrNull()?.host?.let(STREAM_CDN_HOST_PATTERN::matches) ?: false
    }

    private companion object {
        private const val QUERY_KEY_NAME_EXPIRES = "Expires"
        private val STREAM_CDN_HOST_PATTERN =
            "stream-chat-+.+\\.imgix.net$|.+\\.stream-io-cdn.com$".toRegex(RegexOption.IGNORE_CASE)
    }
}
