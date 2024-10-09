package io.getstream.chat.ui.sample.application

import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.log.taggedLogger

internal class MessageTranslator(
    private val getCurrentUser: () -> User?,
    private val autoTranslationEnabled: Boolean,
) : (MessageListItem.MessageItem) -> String {

    override fun invoke(item: MessageListItem.MessageItem): String {
        val message = item.message
        return if (autoTranslationEnabled || hasTranslation(item.message.id)) {
            logger.i { "[getTranslatedText] #1; text: ${message.text}, i18n: ${message.i18n}, id: ${message.id}" }
            getCurrentUser()?.language?.let { language ->
                item.message.getTranslation(language).ifEmpty { item.message.text }
            } ?: item.message.text
        } else {
            logger.v { "[getTranslatedText] #2; text: ${message.text}, i18n: ${message.i18n}, id: ${message.id}" }
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