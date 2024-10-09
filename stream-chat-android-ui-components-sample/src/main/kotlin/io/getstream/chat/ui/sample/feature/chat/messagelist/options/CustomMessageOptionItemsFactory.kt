package io.getstream.chat.ui.sample.feature.chat.messagelist.options

import android.content.Context
import androidx.core.content.ContextCompat
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.client.utils.message.isSystem
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.state.messages.CustomAction
import io.getstream.chat.android.ui.feature.messages.list.MessageListView
import io.getstream.chat.android.ui.feature.messages.list.MessageListViewStyle
import io.getstream.chat.android.ui.feature.messages.list.options.message.MessageOptionItem
import io.getstream.chat.android.ui.feature.messages.list.options.message.MessageOptionItemsFactory
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.application.MessageTranslator
import io.getstream.chat.ui.sample.util.extensions.applyTint

internal object CustomMessageOptions {

    /**
     * Creates a custom message option items factory.
     */
    fun optionFactory(context: Context): MessageOptionItemsFactory {
        return CustomMessageOptionItemsFactory(context)
    }

    /**
     * Creates a custom action handler for the message list view.
     */
    fun actionHandler(
        onTranslate: (Message) -> Unit,
        onClearTranslation: (Message) -> Unit,
    ): MessageListView.CustomActionHandler {
        return CustomMessageOptionHandler(onTranslate, onClearTranslation)
    }
}

/**
 * Handles custom actions for the message list view.
 */
private class CustomMessageOptionHandler(
    private val onTranslate: (Message) -> Unit,
    private val onClearTranslation: (Message) -> Unit,
) : MessageListView.CustomActionHandler {

    override fun onCustomAction(message: Message, extraProperties: Map<String, Any>) {
        if (TranslationOption.isTranslateAction(extraProperties)) {
            onTranslate(message)
        } else if (TranslationOption.isClearTranslationAction(extraProperties)) {
            onClearTranslation(message)
        }
    }
}

/**
 * Adds custom message options to the default message options.
 */
private class CustomMessageOptionItemsFactory(
    private val context: Context,
) : MessageOptionItemsFactory {

    private val delegate = MessageOptionItemsFactory.defaultFactory(context)

    override fun createMessageOptionItems(
        selectedMessage: Message,
        currentUser: User?,
        isInThread: Boolean,
        ownCapabilities: Set<String>,
        style: MessageListViewStyle,
    ): List<MessageOptionItem> {
        return delegate.createMessageOptionItems(selectedMessage, currentUser, isInThread, ownCapabilities, style) +
            createCustomMessageOptionItems(selectedMessage, currentUser, isInThread, ownCapabilities, style)
    }

    private fun createCustomMessageOptionItems(
        selectedMessage: Message,
        currentUser: User?,
        isInThread: Boolean,
        ownCapabilities: Set<String>,
        style: MessageListViewStyle,
    ): List<MessageOptionItem> {
        val messageOptions = mutableListOf<MessageOptionItem>()
        if (selectedMessage.text.isNotEmpty()
            && !selectedMessage.isSystem()
            && !selectedMessage.isDeleted()
            && selectedMessage.user.id != currentUser?.id
        ) {
            messageOptions.add(
                TranslationOption(
                    context,
                    selectedMessage,
                )
            )
        }
        return messageOptions
    }
}

private class TranslationOption private constructor() {

    companion object {
        private const val ACTION = "action"
        private const val TRANSLATE = "translate"
        private const val CLEAR_TRANSLATION = "clear_translation"

        operator fun invoke(
            context: Context,
            selectedMessage: Message,
        ): MessageOptionItem {
            val iconColor = ContextCompat.getColor(context, R.color.stream_ui_grey)
            val icon = ContextCompat.getDrawable(context, R.drawable.ic_translate) ?: error("Drawable not found")
            val iconTinted = icon.applyTint(iconColor)

            val hasTranslation = MessageTranslator.hasTranslation(selectedMessage.id)
            return MessageOptionItem(
                optionText = ContextCompat.getString(context, when (hasTranslation) {
                    true -> R.string.message_action_clear_translation
                    else -> R.string.message_action_translate
                }),
                optionIcon = iconTinted,
                messageAction = CustomAction(
                    message = selectedMessage,
                    extraProperties = mapOf(ACTION to when (hasTranslation) {
                        true -> CLEAR_TRANSLATION
                        else -> TRANSLATE
                    }),
                ),
                isWarningItem = false,
            )
        }

        fun isTranslateAction(extra: Map<String, Any>): Boolean {
            return extra[ACTION] == TRANSLATE
        }

        fun isClearTranslationAction(extra: Map<String, Any>): Boolean {
            return extra[ACTION] == CLEAR_TRANSLATION
        }
    }
}

