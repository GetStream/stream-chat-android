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

package io.getstream.chat.android.client.uploader

/**
 * The context in which a file upload happens. Created by the SDK and handed to [FileUploader]
 * implementations; new context properties can be added over time without breaking implementations.
 *
 * @property channelType The type of the channel the file is uploaded to.
 * @property channelId The id of the channel the file is uploaded to.
 * @property userId The id of the user uploading the file.
 * @property messageId The id of the message the uploaded file belongs to, or null when the upload is not part
 * of sending a message. For message attachments this is the id the message will be sent with, known before
 * the message reaches the Stream API.
 */
public class FileUploadContext internal constructor(
    public val channelType: String,
    public val channelId: String,
    public val userId: String,
    public val messageId: String? = null,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FileUploadContext) return false
        return channelType == other.channelType &&
            channelId == other.channelId &&
            userId == other.userId &&
            messageId == other.messageId
    }

    override fun hashCode(): Int {
        var result = channelType.hashCode()
        result = 31 * result + channelId.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + messageId.hashCode()
        return result
    }

    override fun toString(): String = "FileUploadContext(channelType='$channelType', channelId='$channelId', " +
        "userId='$userId', messageId=$messageId)"
}
