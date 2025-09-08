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

package io.getstream.chat.android.compose.state.mediagallerypreview

import android.os.Parcelable
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import kotlinx.parcelize.Parcelize
import java.util.Date

/**
 * Class used to parcelize the minimum necessary information
 * for proper function of the Media Gallery Preview screen.
 *
 * Using it avoids having to parcelize client models and parcelizing
 * overly large models.
 *
 * @param messageId The ID of the message containing the attachments.
 * @param userId The ID of the user who sent the message.
 * @param userName The name of the user who sent the message.
 * @param userImage The image of the user who sent the message.
 * Set to false because we don't track the status inside the preview screen.
 * @param attachments The list of attachments contained in the original message.
 * @param updatedAt The date when the message was last updated.
 * @param createdAt The date when the message was created.
 */
@Parcelize
internal data class MediaGalleryPreviewActivityState(
    val messageId: String,
    val userId: String,
    val userName: String,
    val userImage: String,
    val attachments: List<MediaGalleryPreviewActivityAttachmentState>,
    val updatedAt: Date?,
    val createdAt: Date?,
) : Parcelable

/**
 * Maps [Message] to [toMediaGalleryPreviewActivityState].
 */
internal fun Message.toMediaGalleryPreviewActivityState(): MediaGalleryPreviewActivityState =
    MediaGalleryPreviewActivityState(
        messageId = this.id,
        userId = this.user.id,
        userName = this.user.name,
        userImage = this.user.image,
        attachments = this.attachments.map { it.toMediaGalleryPreviewActivityAttachmentState() },
        updatedAt = this.updatedAt,
        createdAt = this.createdAt,
    )

/**
 * Maps [toMediaGalleryPreviewActivityState] to [Message].
 */
internal fun MediaGalleryPreviewActivityState.toMessage(): Message =
    Message(
        id = this.messageId,
        user = User(
            id = this.userId,
            name = this.userName,
            image = this.userImage,
        ),
        attachments = this.attachments.map { it.toAttachment() }.toMutableList(),
        updatedAt = this.updatedAt,
        createdAt = this.createdAt,
    )
