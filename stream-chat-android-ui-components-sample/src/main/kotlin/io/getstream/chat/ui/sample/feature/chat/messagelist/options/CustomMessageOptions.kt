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

package io.getstream.chat.ui.sample.feature.chat.messagelist.options

import android.content.Context
import androidx.core.content.ContextCompat
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.client.utils.message.isSystem
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.state.messages.CustomAction
import io.getstream.chat.android.ui.common.utils.canDeleteMessage
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
        onDeleteForMe: (Message) -> Unit,
    ): MessageListView.CustomActionHandler = CustomMessageOptionHandler(
        onTranslate,
        onClearTranslation,
        onDeleteForMe,
    )
}

/**
 * Handles custom actions for the message list view.
 */
private class CustomMessageOptionHandler(
    private val onTranslate: (Message) -> Unit,
    private val onClearTranslation: (Message) -> Unit,
    private val onDeleteForMe: (Message) -> Unit,
) : MessageListView.CustomActionHandler {

    override fun onCustomAction(message: Message, extraProperties: Map<String, Any>) {
        if (TranslationOption.isTranslateAction(extraProperties)) {
            onTranslate(message)
        } else if (TranslationOption.isClearTranslationAction(extraProperties)) {
            onClearTranslation(message)
        } else if (DeleteForMeOption.isDeleteForMeAction(extraProperties)) {
            onDeleteForMe(message)
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
        val isNotEmptyMessage = selectedMessage.text.isNotEmpty()
        val isNotSystemMessage = !selectedMessage.isSystem()
        val isNotDeletedMessage = !selectedMessage.isDeleted()
        val isNotCurrentUserMessage = selectedMessage.user.id != currentUser?.id
        val shouldAddTranslationOption = isNotEmptyMessage && isNotSystemMessage &&
            isNotDeletedMessage && isNotCurrentUserMessage
        if (shouldAddTranslationOption) {
            messageOptions.add(
                TranslationOption(
                    context,
                    selectedMessage,
                ),
            )
        }
        val canDeleteMessage = canDeleteMessage(
            deleteMessageEnabled = style.deleteMessageEnabled,
            currentUser = currentUser,
            message = selectedMessage,
            ownCapabilities = ownCapabilities,
        )
        if (canDeleteMessage) {
            messageOptions.add(DeleteForMeOption(context, style, selectedMessage))
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
                optionText = ContextCompat.getString(
                    context,
                    when (hasTranslation) {
                        true -> R.string.message_action_clear_translation
                        else -> R.string.message_action_translate
                    },
                ),
                optionIcon = iconTinted,
                messageAction = CustomAction(
                    message = selectedMessage,
                    extraProperties = mapOf(
                        ACTION to when (hasTranslation) {
                            true -> CLEAR_TRANSLATION
                            else -> TRANSLATE
                        },
                    ),
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

private object DeleteForMeOption {

    private const val ACTION = "action"
    private const val DELETE_FOR_ME = "delete_for_me"

    operator fun invoke(
        context: Context,
        style: MessageListViewStyle,
        message: Message,
    ): MessageOptionItem {
        val icon = ContextCompat.getDrawable(context, style.deleteIcon) ?: error("Drawable not found")
        return MessageOptionItem(
            optionText = "Delete Message for Me",
            optionIcon = icon,
            messageAction = CustomAction(
                message = message,
                extraProperties = mapOf(ACTION to DELETE_FOR_ME),
            ),
            isWarningItem = true,
        )
    }

    fun isDeleteForMeAction(extra: Map<String, Any>): Boolean =
        extra[ACTION] == DELETE_FOR_ME
}
