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

import androidx.compose.runtime.Immutable

/**
 * Represents a message that is pending delivery or processing.
 *
 * @property message The [Message] object containing all the content and attributes of the message.
 * @property metadata Additional metadata associated with this pending message, stored as key-value pairs.
 */
@Immutable
public data class PendingMessage(
    val message: Message,
    val metadata: Map<String, String>,
) {
    /**
     * Creates a new Builder instance with properties initialized from this PendingMessage.
     *
     * @return A Builder instance for creating a modified copy of this PendingMessage.
     */
    @SinceKotlin("99999.9")
    @Suppress("NEWER_VERSION_IN_SINCE_KOTLIN")
    public fun newBuilder(): Builder = Builder(this)

    /**
     * Builder class for constructing a [PendingMessage] instance.
     */
    public class Builder() {
        private var message: Message = Message()
        private var metadata: Map<String, String> = mapOf()

        /**
         * Creates a new Builder initialized with properties from the given PendingMessage.
         *
         * @param pendingMessage The PendingMessage to initialize this Builder with.
         */
        public constructor(pendingMessage: PendingMessage) : this() {
            message = pendingMessage.message
            metadata = pendingMessage.metadata
        }

        /**
         * Sets the message property of the PendingMessage.
         *
         * @param message The Message object to set.
         * @return This Builder instance.
         */
        public fun withMessage(message: Message): Builder = apply { this.message = message }

        /**
         * Sets the metadata property of the PendingMessage.
         *
         * @param metadata The metadata map to set.
         * @return This Builder instance.
         */
        public fun withMetadata(metadata: Map<String, String>): Builder = apply { this.metadata = metadata }

        /**
         * Builds a new PendingMessage instance with the current property values.
         *
         * @return A new PendingMessage instance.
         */
        public fun build(): PendingMessage = PendingMessage(
            message = message,
            metadata = metadata,
        )
    }
}
