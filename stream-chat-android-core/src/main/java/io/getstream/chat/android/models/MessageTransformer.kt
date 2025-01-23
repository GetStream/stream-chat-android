/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

/**
 * A transformer that can be used to transform a message object before third parties can access it.
 * This is useful for adding extra data to the message object and/or encrypting/decrypting it.
 */
public fun interface MessageTransformer {

    /**
     * Transforms the [message] before returning it to the caller.
     * This can be used to add extra data to the message object and/or encrypt/decrypt it.
     *
     * @return The transformed message.
     */
    public fun transform(message: Message): Message
}

/**
 * A no-op implementation of [MessageTransformer].
 */
public object NoOpMessageTransformer : MessageTransformer {

    /**
     * Returns the [message] as is.
     */
    override fun transform(message: Message): Message = message
}
