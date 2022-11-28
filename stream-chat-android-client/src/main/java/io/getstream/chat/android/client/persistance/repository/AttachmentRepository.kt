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

package io.getstream.chat.android.client.persistance.repository

import io.getstream.chat.android.models.Attachment
import kotlinx.coroutines.flow.Flow

/**
 * Repository to access data of attachments.
 */
public interface AttachmentRepository {

    /**
     * Observes any change in attachments for an specific message.
     */
    public fun observeAttachmentsForMessage(messageId: String): Flow<List<Attachment>>

    /**
     * Clear attachments of this repository.
     */
    public suspend fun clear()
}
