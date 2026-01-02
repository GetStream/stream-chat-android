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

package io.getstream.chat.ui.sample.application

import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.log.taggedLogger

internal class MessageTranslator(
    private val getCurrentUser: () -> User?,
    private val autoTranslationEnabled: Boolean,
) : (MessageListItem.MessageItem) -> String {

    override fun invoke(item: MessageListItem.MessageItem): String {
        return if (autoTranslationEnabled || hasTranslation(item.message.id)) {
            getCurrentUser()?.language?.let { language ->
                item.message.getTranslation(language).ifEmpty { item.message.text }
            } ?: item.message.text
        } else {
            item.message.text
        }
    }

    companion object {
        private val logger by taggedLogger("MessageTranslator")

        /**
         * Stores message IDs that have manual translations.
         */
        private val translatedMessageIDs = hashSetOf<MessageId>()

        fun translate(messageId: MessageId) {
            logger.d { "[translate] messageId: $messageId" }
            translatedMessageIDs.add(messageId)
        }

        fun hasTranslation(messageId: MessageId): Boolean {
            return translatedMessageIDs.contains(messageId)
        }

        fun clearTranslation(messageId: MessageId) {
            logger.d { "[clearTranslation] messageId: $messageId" }
            translatedMessageIDs.remove(messageId)
        }

        fun clearTranslations() {
            logger.d { "[clearTranslations] no args" }
            translatedMessageIDs.clear()
        }
    }
}

internal typealias MessageId = String
