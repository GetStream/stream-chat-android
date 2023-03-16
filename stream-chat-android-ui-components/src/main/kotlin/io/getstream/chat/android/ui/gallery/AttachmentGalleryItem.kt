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

package io.getstream.chat.android.ui.gallery

import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.User
import java.util.Date

public data class AttachmentGalleryItem(
    val attachment: Attachment,
    val user: User,
    val createdAt: Date,
    val messageId: String,
    val cid: String,
    val isMine: Boolean,
    // TODO decide if making this nullable is preferable since it creates a smoother API transition,
    // TODO or if breaking the API in a more noticeable manner is the better alternative
    val parentMessageId: String?,
)
