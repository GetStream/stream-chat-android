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

package io.getstream.chat.android.client.attachment

import io.getstream.chat.android.models.Message
import io.getstream.log.taggedLogger
import io.getstream.result.Error
import io.getstream.result.Result

internal object AttachmentsVerifier {

    private val logger by taggedLogger("Chat:AttachmentVerifier")

    internal fun verifyAttachments(result: Result<Message>): Result<Message> {
        val message = result.getOrNull() ?: return result
        logger.d { "[verifyAttachments] #uploader; uploadedAttachments: ${message.attachments}" }
        val corruptedAttachment = message.attachments.find {
            it.upload != null && it.imageUrl == null && it.assetUrl == null
        }
        return if (corruptedAttachment == null) {
            result
        } else {
            logger.e {
                "[verifyAttachments] #uploader; message(${message.id}) has corrupted attachment: $corruptedAttachment"
            }
            Result.Failure(
                Error.GenericError("Message(${message.id}) contains corrupted attachment: $corruptedAttachment"),
            )
        }
    }
}
