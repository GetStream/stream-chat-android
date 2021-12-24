package io.getstream.chat.android.ui.message.composer

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Function which connects [MessageComposerView] to [MessageComposerViewModel]. As a result the view renders the state
 * delivered by view model, and view model intercepts user's actions automatically.
 *
 * @param view Instance of [MessageComposerView]
 * @param lifecycleOwner [LifecycleOwner] of Activity or Fragment hosting the [MessageComposerView]
 * @param onSendMessage Callback invoked when send button is clicked. Default implementation calls view model to send the message.
 * @param onInputChanged Callback invoked after the text in message input has changed. Default implementation updates text input value in view model.
 * @param onDismissMessage Callback invoked when user dismisses the message. Default implementation clears input value in view model.
 * @param onAttachmentsSelected Callback invoked when user selects list of attachments in attachments picker.
 * @param onRemoveAttachment Callback invoked when user attempts to remove the attachment.
 * @param onMentionSuggestionSelected Callback invoked when selects one of the mention suggestions.
 * @param onCommandSuggestionSelected Callback invoked when selects one of the command suggestions.
 */
public fun MessageComposerViewModel.bindView(
    view: MessageComposerView,
    lifecycleOwner: LifecycleOwner,
    onSendMessage: (Message) -> Unit = { sendMessage(it) },
    onInputChanged: (String) -> Unit = { setMessageInput(it) },
    onDismissMessage: () -> Unit = { setMessageInput("") },
    onAttachmentsSelected: (List<Attachment>) -> Unit = { addSelectedAttachments(it) },
    onRemoveAttachment: (Attachment) -> Unit = { removeSelectedAttachment(it) },
    onMentionSuggestionSelected: (User) -> Unit = { selectMention(it) },
    onCommandSuggestionSelected: (Command) -> Unit = { selectCommand(it) },
) {
    view.onSendMessageClicked = {
        val message = buildNewMessage()
        onSendMessage(message)
    }

    view.onInputChanged = onInputChanged

    view.onMessageDismissed = onDismissMessage

    view.onAttachmentSelected = onAttachmentsSelected

    view.onAttachmentRemovedHandler = onRemoveAttachment

    view.onMentionSuggestionSelected = onMentionSuggestionSelected

    view.onCommandSuggestionSelected = onCommandSuggestionSelected

    lifecycleOwner.lifecycleScope.launch {
        messageInputState.collect {
            view.renderState(it)
        }
    }

    lifecycleOwner.lifecycleScope.launch {
        availableCommands.collect {
            view.availableCommands = it
        }
    }
}
