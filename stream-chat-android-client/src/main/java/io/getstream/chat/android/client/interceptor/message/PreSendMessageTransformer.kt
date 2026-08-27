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

package io.getstream.chat.android.client.interceptor.message

import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.models.Message

/**
 * Transforms an outgoing message right before it is sent to the API.
 *
 * This is the last point at which the message can be modified on the client: it runs after all attachments
 * have finished uploading (so uploader-provided data such as [io.getstream.chat.android.models.Attachment.extraData]
 * is available) and before the message is delivered to the server.
 *
 * If the transformation changes [Message.id], the SDK reconciles the locally persisted optimistic message
 * (state and offline storage) with the new id before sending.
 *
 * The transformation must be idempotent: it can run again for the same message on retries, in which case it
 * receives the already-transformed message as input.
 */
@ExperimentalStreamChatApi
public fun interface PreSendMessageTransformer {

    /**
     * Transforms the [message] before it is sent to the API.
     *
     * @param message The fully prepared message, with all attachments uploaded.
     *
     * @return The message to be sent to the API.
     */
    public suspend fun transform(message: Message): Message
}
