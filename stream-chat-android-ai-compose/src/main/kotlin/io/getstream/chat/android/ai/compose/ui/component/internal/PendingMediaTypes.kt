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

package io.getstream.chat.android.ai.compose.ui.component.internal

import android.content.ContentResolver
import io.getstream.chat.android.ai.compose.conversation.AIMessageAttachment
import io.getstream.chat.android.ai.compose.conversation.AIPendingMessage

/**
 * The message on its way with each attachment the person picked from a document provider shown as
 * what the provider says it is: a file when it isn't an image, such as a PDF. An attachment whose
 * type the provider doesn't say stays as it is, so an extensionless photo keeps its preview.
 */
internal fun AIPendingMessage.withMediaTypes(resolver: ContentResolver): AIPendingMessage {
    val typed = attachments.map { attachment ->
        val uri = attachment.uri
        if (uri == null || uri.scheme != ContentResolver.SCHEME_CONTENT || attachment.mimeType != null) {
            attachment
        } else {
            runCatching { resolver.getType(uri) }.getOrNull()
                ?.let { type -> AIMessageAttachment.from(uri, attachment.name, attachment.size, type) }
                ?: attachment
        }
    }
    return if (typed == attachments) this else copy(attachments = typed)
}
